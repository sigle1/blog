<template>
  <div class="archive-page">
    <div class="archive-container">
      <template v-if="!yearParam">
        <h1 class="page-title">文章归档</h1>
        <p class="page-subtitle">按发布时间浏览历史文章与笔记</p>

        <div v-if="loading" class="state-hint">加载中…</div>
        <el-empty v-else-if="!yearGroups.length" description="还没有可归档的内容" />

        <div v-else class="year-grid">
          <router-link
            v-for="g in yearGroups"
            :key="g.year"
            :to="`/archive/${g.year}`"
            class="year-card"
          >
            <span class="year-num">{{ g.year }}</span>
            <span class="year-count">{{ g.count }} 篇</span>
          </router-link>
        </div>
      </template>

      <template v-else>
        <h1 class="page-title">{{ yearParam }} 年归档</h1>
        <p class="page-subtitle">共 {{ yearItems.length }} 篇</p>
        <router-link to="/archive" class="back-link">← 全部归档</router-link>

        <div v-if="loading" class="state-hint">加载中…</div>
        <el-empty v-else-if="!yearItems.length" :description="`${yearParam} 年暂无内容`" />

        <div v-else class="post-list">
          <div class="post-item" v-for="item in yearItems" :key="`${item.kind}-${item.id}`">
            <div class="post-container" :class="{ 'has-cover': !!item.coverImage }">
              <div v-if="item.coverImage" class="post-cover">
                <img :src="item.coverImage" :alt="item.title" class="cover-image">
              </div>
              <div class="post-content" :class="{ 'no-cover': !item.coverImage }">
                <div class="post-tags">
                  <span class="tag tag-category">{{ item.category }}</span>
                  <span v-if="item.kind === 'note'" class="tag tag-kind">富文本</span>
                </div>
                <h2 class="post-title">{{ item.title }}</h2>
                <p class="post-summary">{{ item.summary }}</p>
                <div class="post-footer">
                  <span class="post-date">{{ formatDate(item.createdAt) }}</span>
                  <router-link :to="item.path" class="read-more">阅读全文</router-link>
                </div>
              </div>
            </div>
          </div>
        </div>
      </template>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, watch, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { ElEmpty } from 'element-plus'
import { loadArchiveItems, groupByYear, formatDate } from '@/utils/archiveContent'

const route = useRoute()
const loading = ref(true)
const allItems = ref([])
const yearGroups = ref([])

const yearParam = computed(() => {
  const y = route.params.year
  if (!y) return ''
  const n = parseInt(String(y), 10)
  return Number.isNaN(n) ? '' : String(n)
})

const yearItems = computed(() => {
  if (!yearParam.value) return []
  const y = parseInt(yearParam.value, 10)
  return allItems.value.filter((it) => it.year === y)
})

async function fetchData() {
  loading.value = true
  try {
    allItems.value = await loadArchiveItems()
    yearGroups.value = groupByYear(allItems.value)
  } catch (e) {
    console.error('加载归档失败:', e)
    allItems.value = []
    yearGroups.value = []
  } finally {
    loading.value = false
  }
}

watch(() => route.params.year, fetchData)

onMounted(fetchData)
</script>

<style scoped>
.archive-page {
  position: relative;
  isolation: isolate;
  min-height: calc(100vh - 120px);
  margin: -24px -20px 0;
  padding: 32px 20px 56px;
  overflow: hidden;
}

.archive-page::before {
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

:global(#app.dark-theme) .archive-page::before {
  background:
    radial-gradient(ellipse 120% 80% at 50% -30%, rgba(59, 130, 246, 0.2), transparent 55%),
    radial-gradient(ellipse 70% 50% at 100% 20%, rgba(168, 85, 247, 0.15), transparent 50%),
    linear-gradient(168deg, #0f172a 0%, #1e1b4b 45%, #0f172a 100%);
}

.archive-container {
  position: relative;
  z-index: 1;
  max-width: 900px;
  margin: 0 auto;
  padding: 12px 8px 24px;
}

.page-title {
  text-align: center;
  font-size: 36px;
  margin-bottom: 10px;
  color: #1e293b;
  text-shadow: 0 2px 12px rgba(255, 255, 255, 0.6);
}

:global(#app.dark-theme) .page-title {
  color: #f1f5f9;
  text-shadow: 0 0 10px rgba(255, 255, 255, 0.2);
}

.page-subtitle {
  text-align: center;
  color: #64748b;
  margin-bottom: 28px;
  font-size: 16px;
}

:global(#app.dark-theme) .page-subtitle {
  color: #94a3b8;
}

.state-hint {
  text-align: center;
  color: #64748b;
  padding: 40px;
}

.year-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(140px, 1fr));
  gap: 16px;
  margin-top: 8px;
}

.year-card {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 28px 16px;
  border-radius: 16px;
  text-decoration: none;
  background: rgba(255, 255, 255, 0.65);
  border: 1px solid rgba(255, 255, 255, 0.8);
  backdrop-filter: blur(12px);
  box-shadow: 0 4px 20px rgba(29, 78, 216, 0.08);
  transition: transform 0.25s ease, box-shadow 0.25s ease;
}

:global(#app.dark-theme) .year-card {
  background: rgba(30, 30, 40, 0.75);
  border-color: rgba(255, 255, 255, 0.1);
}

.year-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 12px 28px rgba(29, 78, 216, 0.15);
}

.year-num {
  font-size: 32px;
  font-weight: 700;
  color: #2563eb;
}

:global(#app.dark-theme) .year-num {
  color: #93c5fd;
}

.year-count {
  margin-top: 6px;
  font-size: 14px;
  color: #64748b;
}

.back-link {
  display: inline-block;
  margin-bottom: 20px;
  color: #2563eb;
  text-decoration: none;
  font-size: 14px;
}

.back-link:hover {
  text-decoration: underline;
}

.post-list {
  display: flex;
  flex-direction: column;
  gap: 24px;
  margin-top: 8px;
}

.post-item {
  background: rgba(255, 255, 255, 0.55);
  backdrop-filter: blur(14px);
  border-radius: 16px;
  overflow: hidden;
  border: 1px solid rgba(255, 255, 255, 0.7);
  transition: transform 0.3s ease, box-shadow 0.3s ease;
}

:global(#app.dark-theme) .post-item {
  background: rgba(0, 0, 0, 0.35);
  border-color: rgba(255, 255, 255, 0.1);
  color: #e5e7eb;
}

.post-item:hover {
  transform: translateY(-3px);
  box-shadow: 0 10px 28px rgba(0, 0, 0, 0.1);
}

.post-container {
  display: flex;
  flex-direction: row;
  min-height: 160px;
}

.post-container.has-cover .post-content {
  flex: 1;
}

.post-cover {
  flex: 0 0 38%;
  max-width: 280px;
  overflow: hidden;
}

.cover-image {
  width: 100%;
  height: 100%;
  min-height: 160px;
  object-fit: cover;
}

.post-content {
  flex: 1;
  padding: 20px;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
}

.post-content.no-cover {
  width: 100%;
}

.post-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-bottom: 8px;
}

.tag {
  font-size: 12px;
  padding: 3px 10px;
  border-radius: 12px;
  background: rgba(37, 99, 235, 0.12);
  color: #1d4ed8;
}

.tag-kind {
  background: rgba(16, 185, 129, 0.15);
  color: #047857;
}

:global(#app.dark-theme) .tag {
  background: rgba(255, 255, 255, 0.1);
  color: #e2e8f0;
}

.post-title {
  margin: 0 0 8px;
  font-size: 20px;
  font-weight: 700;
  color: #1e293b;
}

:global(#app.dark-theme) .post-title {
  color: #f8fafc;
}

.post-summary {
  margin: 0 0 12px;
  font-size: 14px;
  line-height: 1.6;
  color: #475569;
  flex: 1;
}

:global(#app.dark-theme) .post-summary {
  color: #cbd5e1;
}

.post-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 13px;
  color: #64748b;
}

.read-more {
  padding: 6px 14px;
  border-radius: 16px;
  background: rgba(37, 99, 235, 0.12);
  color: #2563eb;
  text-decoration: none;
  font-weight: 600;
  font-size: 13px;
}

.read-more:hover {
  background: rgba(37, 99, 235, 0.2);
}

@media (max-width: 768px) {
  .post-container {
    flex-direction: column;
  }

  .post-cover {
    flex: none;
    max-width: none;
  }
}
</style>
