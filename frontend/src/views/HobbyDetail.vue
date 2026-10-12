<template>
  <div class="hobby-detail-page">
  <div class="hobby-detail-container">
    <div v-if="hobby" class="hobby-header">
      <img :src="hobby.coverImage || 'https://placehold.co/1200x400/333/fff?text=' + hobby.name" alt="Hobby Cover" class="hobby-hero-img">
      <div class="hobby-info">
        <h1 class="hobby-name">{{ hobby.name }}</h1>
        <p class="hobby-description">{{ hobby.description }}</p>
      </div>
    </div>
    <div v-else>
      <p>加载中...</p>
    </div>

    <div v-if="hobby">
      <div class="content-section">
        <div class="section-header">
          <h2 class="section-title">相关文章</h2>
          <el-button type="primary" size="small" @click="router.push(`/hobbies/${hobby.id}/articles/create`)">
            <el-icon><Plus /></el-icon>
            撰写新文章
          </el-button>
        </div>
        <div class="post-list">
          <div class="post-item" v-for="article in hobby.articles" :key="article.id">
            <div class="post-container">
              <!-- 文章封面图片 -->
              <div class="post-cover" v-if="article.coverImage">
                <img :src="article.coverImage" :alt="article.title" class="post-thumb-img">
              </div>
              
              <div class="post-content">
                <div class="post-tags">
                  <span v-for="tag in (article.tags || [])" :key="tag" class="tag">{{ tag }}</span>
                </div>
                <h2 class="post-title">{{ article.title }}</h2>
                <div class="post-meta">
                  <span class="post-views"><i class="icon-eye"></i> {{ article.views || 0 }}</span>
                </div>
                <p class="post-summary">{{ (article.content || '').substring(0, 100) }}{{ (article.content || '').length > 100 ? '...' : '' }}</p>
                <div class="post-footer">
                  <span class="post-date">发布于 {{ formatDate(article.createdAt) }}</span>
                  <router-link :to="`/hobbies/${hobby.id}/articles/${article.id}`" class="read-more">阅读全文</router-link>
                </div>
              </div>
            </div>
          </div>
          <div v-if="!hobby.articles || hobby.articles.length === 0" class="empty-state">
            <p>暂无相关文章</p>
          </div>
        </div>
      </div>

      <div class="content-section">
        <h2 class="section-title">心情随笔</h2>
        <div class="content-list">
          <div v-for="item in hobby.contents" :key="item.id" class="content-item">
            <p>{{ item.content }}</p>
            <span class="content-date">{{ formatDate(item.createdAt) }}</span>
          </div>
          <div v-if="!hobby.contents || hobby.contents.length === 0" class="empty-state">
              <p>暂无心情随笔</p>
          </div>
        </div>
      </div>
      <router-link to="/hobbies" class="back-link">返回爱好列表</router-link>
    </div>
  </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElButton, ElIcon } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import api from '@/api'
import { normalizeImageUrl } from '@/utils/image'

const route = useRoute()
const router = useRouter()
const hobby = ref(null)

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

const fetchHobby = async () => {
  try {
    const response = await api.getHobby(route.params.id)
    // 后端返回结构为 { hobby, contents }
    const data = response.data
    const hobbyData = data.hobby || data
    
    // 处理字段映射
    if (hobbyData.coverUrl && !hobbyData.coverImage) {
      hobbyData.coverImage = hobbyData.coverUrl
    }
    hobbyData.coverImage = normalizeImageUrl(hobbyData.coverImage || hobbyData.coverUrl)
    
    hobby.value = hobbyData
    // 日志：对比路由参数 id 与后端返回的 hobby.id
    console.log('route.params.id:', route.params.id)
    console.log('hobby.id:', hobby.value && hobby.value.id)
    // attach contents
    const contents = data.contents || hobbyData.contents || []
    hobby.value.contents = contents
    // 拉取该爱好的文章列表（单独接口）
    try {
      const artRes = await api.getArticles(route.params.id)
      const articles = artRes.data || []
      // 处理文章的标签和封面
      hobby.value.articles = articles.map(article => ({
        ...article,
        coverImage: normalizeImageUrl(article.coverImage || article.coverUrl || article.coverTitle)
      }))
    } catch (err) {
      hobby.value.articles = hobby.value.articles || []
      console.error('获取文章列表失败:', err)
    }
  } catch (error) {
    // 如果资源不存在，给出友好提示并回到爱好列表
    if (error && error.response && error.response.status === 404) {
      alert('未找到该爱好（可能已被删除或ID错误），将返回爱好列表。')
      router.replace('/hobbies')
      return
    }
    console.error('获取爱好详情失败:', error)
  }
}

onMounted(fetchHobby)
</script>

<style scoped>

/* 伪元素图标 */
.icon-eye::before { content: '\f06e'; font-family: 'Font Awesome 5 Free'; font-weight: 900; margin-right: 5px;}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.hobby-detail-page {
  position: relative;
  isolation: isolate;
  min-height: calc(100vh - 100px);
  margin: -24px -20px 0;
  padding: 28px 20px 48px;
  overflow: hidden;
}

.hobby-detail-page::before {
  content: '';
  position: absolute;
  inset: 0;
  z-index: 0;
  pointer-events: none;
  background:
    radial-gradient(ellipse 100% 70% at 50% -20%, rgba(129, 140, 248, 0.22), transparent 55%),
    radial-gradient(ellipse 55% 40% at 0% 85%, rgba(125, 211, 252, 0.2), transparent 50%),
    linear-gradient(175deg, #f1f5f9 0%, #faf5ff 45%, #f0fdf9 100%);
}

.hobby-detail-container {
  position: relative;
  z-index: 1;
  max-width: 900px;
  margin: 0 auto;
  padding: 8px 12px 24px;
  color: #0f172a;
}

.hobby-header {
  position: relative;
  margin-bottom: 28px;
  border-radius: 16px;
  overflow: hidden;
  border: 1px solid rgba(226, 232, 240, 0.95);
  box-shadow: 0 4px 20px rgba(15, 23, 42, 0.08);
}

.hobby-hero-img {
  width: 100%;
  height: clamp(160px, 32vw, 280px);
  max-height: 280px;
  object-fit: cover;
  object-position: center;
  display: block;
  vertical-align: middle;
}

.hobby-info {
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  padding: 20px 22px 14px;
  background: linear-gradient(to top, rgba(15, 23, 42, 0.82) 0%, rgba(15, 23, 42, 0.15) 65%, transparent 100%);
}

.hobby-name {
  font-size: clamp(1.35rem, 3.5vw, 1.75rem);
  font-weight: 700;
  margin: 0 0 6px 0;
  color: #fff;
  text-shadow: 0 2px 12px rgba(0, 0, 0, 0.45);
}

.hobby-description {
  font-size: 14px;
  line-height: 1.5;
  color: rgba(255, 255, 255, 0.92);
  max-width: 700px;
  margin: 0;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.content-section {
  background: rgba(255, 255, 255, 0.78);
  backdrop-filter: blur(12px);
  -webkit-backdrop-filter: blur(12px);
  padding: 22px 24px;
  border-radius: 16px;
  margin-bottom: 20px;
  border: 1px solid rgba(226, 232, 240, 0.95);
  box-shadow: 0 2px 12px rgba(15, 23, 42, 0.06);
}

.section-title {
  font-size: 1.2rem;
  font-weight: 700;
  color: #0f172a;
  margin-bottom: 16px;
  padding-bottom: 10px;
  border-bottom: 1px solid #e2e8f0;
}

.content-list .content-item {
  background-color: #f8fafc;
  padding: 14px 16px;
  border-radius: 12px;
  margin-bottom: 10px;
  border: 1px solid #e2e8f0;
}

.content-item p {
  margin: 0 0 8px 0;
  color: #334155;
  line-height: 1.55;
}

.content-date {
  font-size: 12px;
  color: #94a3b8;
}

.empty-state {
  text-align: center;
  padding: 28px;
  color: #64748b;
  background-color: #f1f5f9;
  border-radius: 12px;
  border: 1px dashed #cbd5e1;
}

.back-link {
  display: inline-block;
  margin-top: 16px;
  padding: 8px 18px;
  background: linear-gradient(135deg, #e0e7ff, #dbeafe);
  color: #3730a3;
  text-decoration: none;
  border-radius: 999px;
  font-size: 14px;
  font-weight: 600;
  border: 1px solid rgba(129, 140, 248, 0.35);
}

.back-link:hover {
  box-shadow: 0 4px 14px rgba(99, 102, 241, 0.2);
}

/* 文章列表样式 */
.post-list {
  display: grid;
  grid-template-columns: 1fr;
  gap: 40px;
}

.post-item {
  background: rgba(255, 255, 255, 0.9);
  backdrop-filter: blur(12px);
  -webkit-backdrop-filter: blur(12px);
  border-radius: 16px;
  overflow: hidden;
  border: 1px solid #e2e8f0;
  transition: all 0.35s ease;
  color: #334155;
  position: relative;
  margin-bottom: 20px;
  box-shadow: 0 2px 8px rgba(15, 23, 42, 0.05);
}

.post-item::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 1px;
  background: linear-gradient(90deg, transparent, rgba(99, 102, 241, 0.25), transparent);
  transform: translateX(-100%);
  transition: transform 0.6s ease;
}

.post-item:hover::before {
  transform: translateX(100%);
}

.post-item:hover {
  transform: translateY(-3px);
  box-shadow: 0 12px 28px rgba(15, 23, 42, 0.1);
  border-color: #c7d2fe;
}

/* 文章容器：不锁死高度，避免摘要区把「阅读全文」挤出可视区域 */
.post-container {
  display: flex;
  flex-direction: row;
  align-items: stretch;
  min-height: 0;
}

/* 文章封面：固定比例区域，图片完整纳入画幅（略留边） */
.post-cover {
  flex: 0 0 42%;
  max-width: 320px;
  min-width: 140px;
  align-self: stretch;
  overflow: hidden;
  position: relative;
  background: #e2e8f0;
  border-radius: 16px 0 0 16px;
}

.post-item:hover .post-thumb-img {
  transform: scale(1.05);
}

.post-thumb-img {
  width: 100%;
  height: 100%;
  min-height: 220px;
  object-fit: contain;
  object-position: center;
  display: block;
  transition: transform 0.5s ease;
}

/* 文章内容 */
.post-content {
  flex: 1;
  min-width: 0;
  padding: 20px;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  gap: 8px;
}

.post-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-bottom: 10px;
}

.tag {
  display: inline-block;
  background-color: #f1f5f9;
  color: #475569;
  padding: 4px 12px;
  border-radius: 15px;
  font-size: 12px;
  font-weight: 600;
  border: 1px solid #e2e8f0;
}

.post-title {
  margin: 0 0 10px 0;
  font-size: 18px;
  color: #0f172a;
  font-weight: 700;
  line-height: 1.3;
  transition: color 0.2s ease;
}

.post-item:hover .post-title {
  color: #4f46e5;
}

.post-meta {
  font-size: 12px;
  color: #94a3b8;
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 10px;
}

.post-summary {
  margin: 0 0 15px 0;
  color: #475569;
  line-height: 1.6;
  font-size: 14px;
  flex: 1;
}

.post-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  flex-wrap: wrap;
  gap: 10px;
  margin-top: auto;
  padding-top: 8px;
  font-size: 12px;
  color: #94a3b8;
  flex-shrink: 0;
}

.read-more {
  display: inline-block;
  background: linear-gradient(135deg, #e0e7ff, #dbeafe);
  color: #3730a3;
  padding: 6px 16px;
  border-radius: 20px;
  font-size: 12px;
  font-weight: 700;
  text-decoration: none;
  border: 1px solid rgba(129, 140, 248, 0.35);
}

.read-more:hover {
  box-shadow: 0 4px 12px rgba(99, 102, 241, 0.2);
}

/* 响应式设计 */
@media (max-width: 768px) {
  .post-container {
    flex-direction: column;
  }
  
  .post-cover {
    flex: 0 0 auto;
    max-width: none;
    width: 100%;
    min-height: 200px;
    border-radius: 16px 16px 0 0;
  }

  .post-thumb-img {
    min-height: 200px;
    max-height: 280px;
  }
  
  .post-content {
    padding: 20px;
  }
  
  .post-title {
    font-size: 18px;
  }
  
  .hobby-detail-page {
    margin: -16px -12px 0;
    padding: 20px 12px 36px;
  }

  .hobby-detail-container {
    padding: 4px 8px 16px;
  }
}
</style>

<style>
#app.dark-theme .hobby-detail-page::before {
  background:
    radial-gradient(ellipse 100% 60% at 50% -15%, rgba(99, 102, 241, 0.2), transparent 55%),
    linear-gradient(175deg, #0f1419 0%, #151b2e 55%, #0c1220 100%);
}

#app.dark-theme .hobby-detail-container {
  color: #e2e8f0;
}

#app.dark-theme .hobby-header {
  border-color: rgba(71, 85, 105, 0.5);
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.35);
}

#app.dark-theme .content-section {
  background: rgba(30, 41, 59, 0.55);
  border-color: rgba(71, 85, 105, 0.45);
}

#app.dark-theme .section-title {
  color: #f1f5f9;
  border-bottom-color: rgba(71, 85, 105, 0.6);
}

#app.dark-theme .content-list .content-item {
  background: rgba(15, 23, 42, 0.5);
  border-color: rgba(71, 85, 105, 0.4);
}

#app.dark-theme .content-item p {
  color: #e2e8f0;
}

#app.dark-theme .empty-state {
  background: rgba(30, 41, 59, 0.4);
  color: #94a3b8;
  border-color: rgba(100, 116, 139, 0.35);
}

#app.dark-theme .post-item {
  background: rgba(30, 41, 59, 0.65);
  border-color: rgba(71, 85, 105, 0.5);
  color: #e2e8f0;
}

#app.dark-theme .post-cover {
  background: rgba(15, 23, 42, 0.85);
}

#app.dark-theme .post-title {
  color: #f8fafc;
}

#app.dark-theme .post-item:hover .post-title {
  color: #a5b4fc;
}

#app.dark-theme .post-summary {
  color: #cbd5e1;
}

#app.dark-theme .tag {
  background: rgba(51, 65, 85, 0.8);
  color: #e2e8f0;
  border-color: rgba(71, 85, 105, 0.5);
}

#app.dark-theme .back-link {
  background: rgba(79, 70, 229, 0.25);
  color: #c7d2fe;
  border-color: rgba(129, 140, 248, 0.4);
}
</style>
