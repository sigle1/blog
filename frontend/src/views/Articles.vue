<template>
  <div class="articles-page">
    <div class="articles-container">
      <h1 class="page-title">文章</h1>
      <p class="page-subtitle">博客正文与富文本笔记，按分类浏览。</p>

      <div class="category-bar">
        <button
          v-for="cat in categoryTabs"
          :key="cat"
          type="button"
          class="category-chip"
          :class="{ active: activeCategory === cat }"
          @click="selectCategory(cat)"
        >
          {{ cat }}
        </button>
      </div>

      <div class="post-list">
        <div class="post-item" v-for="item in filteredItems" :key="`${item.kind}-${item.id}`">
          <div class="post-container" :class="{ 'has-cover': !!item.coverImage }">
            <div v-if="item.coverImage" class="post-cover">
              <img :src="item.coverImage" :alt="item.title" class="cover-image">
            </div>
            <div class="post-content" :class="{ 'no-cover': !item.coverImage }">
              <div class="post-tags">
                <span class="tag tag-category">{{ item.category }}</span>
                <span v-if="item.kind === 'note'" class="tag tag-kind">富文本</span>
                <span v-for="tag in (item.tags || []).slice(0, 3)" :key="tag" class="tag">{{ tag }}</span>
              </div>
              <h2 class="post-title">{{ item.title }}</h2>
              <p class="post-summary">{{ item.summary }}</p>
              <div class="post-footer">
                <span class="post-date">发布于 {{ formatDate(item.createdAt) }}</span>
                <router-link :to="item.path" class="read-more">阅读全文</router-link>
              </div>
            </div>
          </div>
        </div>
      </div>

      <div v-if="!loading && filteredItems.length === 0" class="empty-state">
        <p>该分类下还没有内容。</p>
      </div>
      <div v-if="loading" class="empty-state">
        <p>加载中…</p>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, watch, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import api from '@/api'
import { normalizeImageUrl } from '@/utils/image'

const route = useRoute()
const router = useRouter()

const loading = ref(true)
const items = ref([])
const activeCategory = ref('全部')

function queryCategory() {
  const q = route.query.category
  if (Array.isArray(q)) return (q[0] || '').trim()
  return typeof q === 'string' ? q.trim() : ''
}

function stripHtml(html) {
  if (!html) return ''
  const s = String(html).replace(/<[^>]+>/g, ' ').replace(/\s+/g, ' ').trim()
  return s.length > 120 ? `${s.slice(0, 120)}…` : s
}

function parseNoteList(raw) {
  if (!raw) return []
  if (Array.isArray(raw)) return raw
  if (raw.data && Array.isArray(raw.data)) return raw.data
  if (raw.notes && Array.isArray(raw.notes)) return raw.notes
  return []
}

function formatDate(value) {
  if (!value) return ''
  if (Array.isArray(value)) {
    const [y, m = 1, d = 1, hh = 0, mm = 0, ss = 0] = value
    return new Date(y, (m || 1) - 1, d, hh, mm, ss).toLocaleDateString()
  }
  if (typeof value === 'number') {
    return new Date(value).toLocaleDateString()
  }
  try {
    return new Date(value).toLocaleDateString()
  } catch (e) {
    return String(value)
  }
}

function toTime(value) {
  if (!value) return 0
  if (Array.isArray(value)) {
    const [y, m = 1, d = 1, hh = 0, mm = 0, ss = 0] = value
    return new Date(y, (m || 1) - 1, d, hh, mm, ss).getTime()
  }
  if (typeof value === 'number') return value
  const t = new Date(value).getTime()
  return Number.isNaN(t) ? 0 : t
}

const categoryTabs = computed(() => {
  const set = new Set()
  for (const it of items.value) {
    if (it.category) set.add(it.category)
  }
  const rest = [...set].sort((a, b) => a.localeCompare(b, 'zh-CN'))
  return ['全部', ...rest]
})

const filteredItems = computed(() => {
  if (activeCategory.value === '全部') return items.value
  return items.value.filter((it) => it.category === activeCategory.value)
})

function selectCategory(cat) {
  activeCategory.value = cat
  if (cat === '全部') {
    router.replace({ path: '/articles', query: {} })
  } else {
    router.replace({ path: '/articles', query: { category: cat } })
  }
}

watch(
  () => route.query.category,
  () => {
    const c = queryCategory()
    if (c && categoryTabs.value.includes(c)) {
      activeCategory.value = c
    } else if (!c) {
      activeCategory.value = '全部'
    }
  },
  { immediate: true }
)

watch(categoryTabs, (tabs) => {
  const c = queryCategory()
  if (c && tabs.includes(c)) activeCategory.value = c
})

onMounted(async () => {
  loading.value = true
  try {
    const [postsRes, notesRes] = await Promise.all([api.getPosts(), api.getNotes()])
    const posts = Array.isArray(postsRes.data) ? postsRes.data : []
    const notes = parseNoteList(notesRes.data)

    const fromPosts = posts.map((p) => ({
      kind: 'post',
      id: p.id,
      title: p.title,
      summary: (p.summary || '').trim() || stripHtml(p.content),
      coverImage: normalizeImageUrl(p.coverImage || p.coverUrl || p.cover_title),
      category: p.category && String(p.category).trim() ? p.category.trim() : '随笔',
      tags: p.tags,
      createdAt: p.createdAt,
      path: `/post/${p.id}`
    }))

    const fromNotes = notes.map((n) => ({
      kind: 'note',
      id: n.id,
      title: n.title,
      summary: stripHtml(n.content),
      coverImage: '',
      category: n.category && String(n.category).trim() ? n.category.trim() : '学习笔记',
      tags: [],
      createdAt: n.createdAt,
      path: `/notes/${n.id}`
    }))

    items.value = [...fromPosts, ...fromNotes].sort((a, b) => toTime(b.createdAt) - toTime(a.createdAt))

    const c = queryCategory()
    if (c && categoryTabs.value.includes(c)) activeCategory.value = c
  } catch (err) {
    console.error('加载文章列表失败:', err)
  } finally {
    loading.value = false
  }
})
</script>

<style scoped>
.articles-page {
  position: relative;
  isolation: isolate;
  min-height: calc(100vh - 120px);
  margin: -24px -20px 0;
  padding: 32px 20px 56px;
  overflow: hidden;
}

.articles-page::before {
  content: '';
  position: absolute;
  inset: 0;
  z-index: 0;
  pointer-events: none;
  background:
    radial-gradient(ellipse 120% 80% at 50% -30%, rgba(160, 196, 255, 0.45), transparent 55%),
    radial-gradient(ellipse 70% 50% at 100% 20%, rgba(255, 182, 193, 0.22), transparent 50%),
    radial-gradient(ellipse 60% 45% at 0% 90%, rgba(147, 197, 253, 0.3), transparent 50%),
    linear-gradient(168deg, #eef2ff 0%, #faf5ff 38%, #f0fdfa 100%);
}

.articles-container {
  position: relative;
  z-index: 1;
  max-width: 900px;
  margin: 0 auto;
  padding: 12px 8px 24px;
  color: #fff;
}

.page-title {
  text-align: center;
  font-size: 36px;
  margin-bottom: 10px;
  text-shadow: 0 0 10px rgba(255, 255, 255, 0.5);
}

.page-subtitle {
  text-align: center;
  color: #ccc;
  margin-bottom: 24px;
  font-size: 16px;
}

.category-bar {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  justify-content: center;
  margin-bottom: 36px;
}

.category-chip {
  background: rgba(255, 255, 255, 0.08);
  border: 1px solid rgba(255, 255, 255, 0.15);
  color: #ddd;
  padding: 8px 16px;
  border-radius: 999px;
  font-size: 13px;
  cursor: pointer;
  transition: all 0.25s ease;
}

.category-chip:hover {
  background: rgba(160, 196, 255, 0.15);
  border-color: rgba(160, 196, 255, 0.35);
  color: #fff;
}

.category-chip.active {
  background: rgba(160, 196, 255, 0.25);
  border-color: rgba(160, 196, 255, 0.5);
  color: #a0c4ff;
  font-weight: 600;
}

.post-list {
  display: grid;
  grid-template-columns: 1fr;
  gap: 40px;
}

.post-item {
  background-color: rgba(0, 0, 0, 0.3);
  backdrop-filter: blur(15px);
  border-radius: 20px;
  overflow: hidden;
  border: 1px solid rgba(255, 255, 255, 0.1);
  transition: all 0.4s cubic-bezier(0.25, 0.8, 0.25, 1);
  color: #e0e0e0;
  margin-bottom: 30px;
}

.post-item:hover {
  transform: translateY(-5px);
  box-shadow: 0 15px 35px rgba(0, 0, 0, 0.4);
  background-color: rgba(0, 0, 0, 0.4);
  border-color: rgba(255, 255, 255, 0.2);
}

.post-container {
  display: flex;
  flex-direction: row;
  min-height: 200px;
}

.post-container.has-cover {
  height: 200px;
}

.post-cover {
  flex: 0 0 40%;
  overflow: hidden;
  position: relative;
}

.cover-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.5s ease;
}

.post-item:hover .cover-image {
  transform: scale(1.05);
}

.post-content {
  flex: 1;
  padding: 20px;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
}

.post-content.no-cover {
  flex: 0 0 100%;
}

.post-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-bottom: 10px;
}

.tag {
  display: inline-block;
  background-color: rgba(255, 255, 255, 0.1);
  color: #ddd;
  padding: 4px 12px;
  border-radius: 15px;
  font-size: 12px;
  font-weight: bold;
  border: 1px solid rgba(255, 255, 255, 0.1);
}

.tag-category {
  background-color: rgba(160, 196, 255, 0.2);
  color: #a0c4ff;
  border-color: rgba(160, 196, 255, 0.35);
}

.tag-kind {
  background-color: rgba(255, 200, 180, 0.15);
  color: #ffc8b4;
  border-color: rgba(255, 200, 180, 0.3);
}

.post-title {
  margin: 0 0 10px 0;
  font-size: 20px;
  color: #ffffff;
  font-weight: 700;
  line-height: 1.3;
}

.post-item:hover .post-title {
  color: #a0c4ff;
}

.post-summary {
  margin: 0 0 15px 0;
  color: #dcdcdc;
  line-height: 1.6;
  font-size: 14px;
  flex: 1;
}

.post-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 12px;
  color: #a0a0a0;
}

.read-more {
  display: inline-block;
  background-color: rgba(160, 196, 255, 0.2);
  color: #a0c4ff;
  padding: 6px 16px;
  border-radius: 20px;
  font-size: 12px;
  font-weight: bold;
  text-decoration: none;
  border: 1px solid rgba(160, 196, 255, 0.3);
}

.empty-state {
  text-align: center;
  padding: 80px 0;
  background-color: rgba(0, 0, 0, 0.2);
  border-radius: 16px;
  margin-top: 30px;
}

@media (max-width: 768px) {
  .post-container {
    flex-direction: column;
    height: auto;
    min-height: 0;
  }
  .post-container.has-cover {
    height: auto;
  }
  .post-cover {
    flex: 0 0 200px;
  }
  .articles-page {
    margin: -16px -12px 0;
    padding: 20px 12px 40px;
  }
}
</style>

<style>
#app:not(.dark-theme) .articles-page::before {
  background:
    radial-gradient(ellipse 120% 80% at 50% -30%, rgba(129, 140, 248, 0.35), transparent 55%),
    radial-gradient(ellipse 70% 50% at 100% 15%, rgba(251, 207, 232, 0.35), transparent 50%),
    linear-gradient(168deg, #e8eefc 0%, #f5f0ff 40%, #ecfdf5 100%);
}

#app:not(.dark-theme) .articles-container {
  color: #334155;
}

#app:not(.dark-theme) .articles-container .page-title {
  color: #0f172a;
  text-shadow: 0 1px 0 rgba(255, 255, 255, 0.8);
}

#app:not(.dark-theme) .articles-container .page-subtitle {
  color: #64748b;
}

#app:not(.dark-theme) .articles-container .category-chip {
  background: rgba(255, 255, 255, 0.75);
  border-color: rgba(148, 163, 184, 0.35);
  color: #475569;
}

#app:not(.dark-theme) .articles-container .category-chip.active {
  background: linear-gradient(135deg, #dbeafe, #e0e7ff);
  border-color: rgba(99, 102, 241, 0.45);
  color: #3730a3;
}

#app:not(.dark-theme) .articles-container .post-item {
  background: rgba(255, 255, 255, 0.82);
  border-color: rgba(226, 232, 240, 0.95);
  color: #334155;
}

#app:not(.dark-theme) .articles-container .post-title {
  color: #0f172a;
}

#app:not(.dark-theme) .articles-container .post-summary {
  color: #475569;
}

#app:not(.dark-theme) .articles-container .read-more {
  background: linear-gradient(135deg, #e0e7ff, #dbeafe);
  color: #3730a3;
}

#app.dark-theme .articles-page::before {
  background:
    radial-gradient(ellipse 100% 70% at 50% -25%, rgba(96, 165, 250, 0.18), transparent 55%),
    linear-gradient(168deg, #0b1020 0%, #12182e 42%, #0d1526 100%);
}
</style>
