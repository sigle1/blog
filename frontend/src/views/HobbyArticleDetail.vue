<template>
  <div class="article-detail-page">
    <article v-if="article" class="article-paper">
      <div v-if="coverSrc" class="article-hero">
        <img :src="coverSrc" :alt="article.title" class="article-hero-img" />
      </div>
      <div class="article-inner">
        <h1 class="article-title">{{ article.title }}</h1>
        <div class="article-meta">
          <span class="meta-item">发布于 {{ formatDate(article.createdAt) }}</span>
          <span class="meta-item" v-if="article.views !== undefined">阅读 {{ article.views }}</span>
          <div class="article-tags" v-if="article.tags && article.tags.length">
            <span v-for="tag in article.tags" :key="tag" class="tag">{{ tag }}</span>
          </div>
        </div>
        <div class="article-body" v-html="compiledMarkdown"></div>
        <router-link v-if="hobbyId" :to="`/hobbies/${hobbyId}`" class="back-link">返回爱好详情</router-link>
      </div>
    </article>
    <div v-else-if="loading" class="state-card">
      <p>正在加载文章...</p>
    </div>
    <div v-else class="state-card">
      <p>无法加载文章，或文章不存在。</p>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRoute } from 'vue-router'
import api from '@/api'
import { renderArticleMarkdown } from '@/utils/markdown'
import { normalizeImageUrl } from '@/utils/image'

const route = useRoute()
const article = ref(null)
const hobbyId = ref(route.params.hobbyId)
const loading = ref(true)

const coverSrc = computed(() => {
  if (!article.value) return ''
  const a = article.value
  const raw = a.coverImage || a.coverUrl || a.coverTitle || a.cover_image
  return normalizeImageUrl(raw)
})

function formatDate(value) {
  if (!value) return ''
  if (Array.isArray(value)) {
    const [y, m, d, hh = 0, mm = 0, ss = 0] = value
    return `${y}-${String(m).padStart(2, '0')}-${String(d).padStart(2, '0')} ${String(hh).padStart(2, '0')}:${String(mm).padStart(2, '0')}`
  }
  try {
    const date = new Date(value)
    return date.toLocaleString()
  } catch (e) {
    return String(value)
  }
}

onMounted(async () => {
  try {
    const response = await api.getArticleForHobby(route.params.hobbyId, route.params.articleId)
    article.value = response.data
  } catch (error) {
    console.error('获取文章详情失败:', error)
  } finally {
    loading.value = false
  }
})

const compiledMarkdown = computed(() => {
  if (article.value && article.value.content) {
    return renderArticleMarkdown(article.value.content)
  }
  return ''
})
</script>

<style scoped>
.article-detail-page {
  position: relative;
  isolation: isolate;
  min-height: calc(100vh - 100px);
  margin: -24px -20px 0;
  padding: 28px 20px 56px;
  overflow: hidden;
}

.article-detail-page::before {
  content: '';
  position: absolute;
  inset: 0;
  z-index: 0;
  pointer-events: none;
  background:
    radial-gradient(ellipse 100% 70% at 50% -20%, rgba(129, 140, 248, 0.18), transparent 55%),
    radial-gradient(ellipse 55% 40% at 0% 85%, rgba(125, 211, 252, 0.16), transparent 50%),
    linear-gradient(175deg, #f1f5f9 0%, #fafafa 45%, #f8fafc 100%);
}

.article-paper {
  position: relative;
  z-index: 1;
  max-width: 720px;
  margin: 0 auto;
  background: #fff;
  border-radius: 16px;
  border: 1px solid rgba(226, 232, 240, 0.95);
  box-shadow:
    0 1px 2px rgba(15, 23, 42, 0.04),
    0 12px 40px rgba(15, 23, 42, 0.08);
  overflow: hidden;
}

.article-hero {
  width: 100%;
  height: min(48vw, 400px);
  min-height: 200px;
  max-height: 420px;
  background: #e2e8f0;
  overflow: hidden;
}

.article-hero-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  object-position: center;
  display: block;
}

.article-inner {
  padding: 28px 32px 36px;
}

.article-title {
  font-size: clamp(1.5rem, 4vw, 2rem);
  font-weight: 800;
  line-height: 1.25;
  margin: 0 0 16px;
  text-align: left;
  color: #0f172a;
  letter-spacing: -0.02em;
}

.article-meta {
  display: flex;
  justify-content: flex-start;
  align-items: center;
  flex-wrap: wrap;
  gap: 12px 20px;
  margin-bottom: 28px;
  padding-bottom: 20px;
  border-bottom: 1px solid #e2e8f0;
  color: #64748b;
  font-size: 14px;
}

.meta-item {
  display: inline-flex;
  align-items: center;
}

.article-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.tag {
  background: #f1f5f9;
  color: #475569;
  padding: 4px 10px;
  border-radius: 999px;
  font-size: 12px;
  font-weight: 600;
  border: 1px solid #e2e8f0;
}

.article-body {
  line-height: 1.85;
  font-size: 17px;
  color: #334155;
}

.article-body :deep(h1),
.article-body :deep(h2),
.article-body :deep(h3) {
  color: #0f172a;
  font-weight: 700;
  margin-top: 1.5em;
  margin-bottom: 0.6em;
  line-height: 1.3;
}

.article-body :deep(h2) {
  font-size: 1.35rem;
  padding-bottom: 0.35em;
  border-bottom: 1px solid #e2e8f0;
}

.article-body :deep(p) {
  margin-bottom: 1.1em;
}

.article-body :deep(ul),
.article-body :deep(ol) {
  margin: 0 0 1.1em 1.25em;
  padding: 0;
}

.article-body :deep(li) {
  margin-bottom: 0.35em;
}

.article-body :deep(img) {
  max-width: 100%;
  height: auto;
  display: block;
  margin: 1.25em auto;
  border-radius: 10px;
  border: 1px solid #e2e8f0;
}

.article-body :deep(a) {
  color: #4f46e5;
  text-decoration: underline;
  text-underline-offset: 3px;
}

.article-body :deep(code) {
  background: #f1f5f9;
  color: #0f172a;
  padding: 2px 6px;
  border-radius: 6px;
  font-size: 0.9em;
  font-family: ui-monospace, 'Cascadia Code', 'Courier New', monospace;
}

.article-body :deep(pre) {
  background: #0f172a;
  color: #e2e8f0;
  padding: 16px 18px;
  border-radius: 12px;
  overflow-x: auto;
  margin: 1.25em 0;
  border: 1px solid #1e293b;
}

.article-body :deep(pre code) {
  background: transparent;
  color: inherit;
  padding: 0;
}

.article-body :deep(blockquote) {
  margin: 1.25em 0;
  padding: 12px 16px;
  border-left: 4px solid #c7d2fe;
  background: #f8fafc;
  color: #475569;
  border-radius: 0 10px 10px 0;
}

.back-link {
  display: inline-flex;
  align-items: center;
  margin-top: 36px;
  padding: 10px 20px;
  background: linear-gradient(135deg, #eef2ff, #e0e7ff);
  color: #3730a3;
  text-decoration: none;
  border-radius: 999px;
  font-size: 14px;
  font-weight: 600;
  border: 1px solid rgba(129, 140, 248, 0.35);
  transition: box-shadow 0.2s ease;
}

.back-link:hover {
  box-shadow: 0 4px 14px rgba(99, 102, 241, 0.18);
}

.state-card {
  position: relative;
  z-index: 1;
  max-width: 720px;
  margin: 48px auto 0;
  padding: 40px;
  text-align: center;
  background: rgba(255, 255, 255, 0.9);
  border-radius: 16px;
  border: 1px solid #e2e8f0;
  color: #64748b;
  font-size: 16px;
}
</style>

<style>
#app.dark-theme .article-detail-page::before {
  background:
    radial-gradient(ellipse 100% 60% at 50% -15%, rgba(99, 102, 241, 0.18), transparent 55%),
    linear-gradient(175deg, #0f1419 0%, #151b2e 55%, #0c1220 100%);
}

#app.dark-theme .article-paper {
  background: rgba(30, 41, 59, 0.92);
  border-color: rgba(71, 85, 105, 0.45);
  box-shadow: 0 12px 40px rgba(0, 0, 0, 0.35);
}

#app.dark-theme .article-hero {
  background: rgba(15, 23, 42, 0.9);
}

#app.dark-theme .article-title {
  color: #f8fafc;
}

#app.dark-theme .article-meta {
  color: #94a3b8;
  border-bottom-color: rgba(71, 85, 105, 0.5);
}

#app.dark-theme .tag {
  background: rgba(51, 65, 85, 0.8);
  color: #e2e8f0;
  border-color: rgba(71, 85, 105, 0.5);
}

#app.dark-theme .article-body {
  color: #cbd5e1;
}

#app.dark-theme .article-body h1,
#app.dark-theme .article-body h2,
#app.dark-theme .article-body h3 {
  color: #f1f5f9;
}

#app.dark-theme .article-body h2 {
  border-bottom-color: rgba(71, 85, 105, 0.5);
}

#app.dark-theme .article-body a {
  color: #a5b4fc;
}

#app.dark-theme .article-body code {
  background: rgba(15, 23, 42, 0.9);
  color: #e2e8f0;
}

#app.dark-theme .article-body blockquote {
  background: rgba(15, 23, 42, 0.55);
  color: #cbd5e1;
  border-left-color: #6366f1;
}

#app.dark-theme .article-body img {
  border-color: rgba(71, 85, 105, 0.5);
}

#app.dark-theme .back-link {
  background: rgba(79, 70, 229, 0.25);
  color: #c7d2fe;
  border-color: rgba(129, 140, 248, 0.4);
}

#app.dark-theme .state-card {
  background: rgba(30, 41, 59, 0.75);
  border-color: rgba(71, 85, 105, 0.45);
  color: #94a3b8;
}
</style>
