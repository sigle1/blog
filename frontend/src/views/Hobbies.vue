<template>
  <div class="hobbies-page">
    <AnimatedMeshBackground />
    <div class="hobbies-container">
    <div class="page-header">
      <h1 class="page-title">我的爱好</h1>
      <p class="page-subtitle">探索我热衷的世界，每一个都充满故事。</p>
    </div>
    
    <div class="post-list">
      <div class="post-item" v-for="hobby in hobbies" :key="hobby.id">
        <div class="post-container">
          <!-- 爱好封面图片 -->
          <div class="post-cover" v-if="hobby.coverImage">
            <img :src="hobby.coverImage" :alt="hobby.name" class="cover-image">
          </div>
          
          <div class="post-content">
            <div class="post-tags">
              <span v-for="tag in (hobby.tags || [])" :key="tag" class="tag">{{ tag }}</span>
            </div>
            <h2 class="post-title">{{ hobby.name }}</h2>
            <div class="post-meta">
              <span class="post-views"><i class="icon-eye"></i> {{ hobby.views || 0 }}</span>
            </div>
            <p class="post-summary">{{ hobby.description }}</p>
            <div class="post-footer">
              <span class="post-date">创建于 {{ formatDate(hobby.createdAt) }}</span>
              <router-link :to="`/hobbies/${hobby.id}`" class="read-more">查看详情</router-link>
            </div>
          </div>
        </div>
      </div>
    </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import api from '@/api'
import { normalizeImageUrl } from '@/utils/image'
import AnimatedMeshBackground from '@/components/AnimatedMeshBackground.vue'

const hobbies = ref([])

function formatDate(value) {
  if (!value) return ''
  // 支持数组 [year, month, day, hour, minute, second]
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

onMounted(async () => {
  try {
    const response = await api.getHobbies()
    console.log('API Response:', response)
    
    // axios 返回的响应在 response.data 中
    let list = []
    if (response.data) {
      if (Array.isArray(response.data)) {
        list = response.data
      } else if (response.data.data && Array.isArray(response.data.data)) {
        list = response.data.data
      } else if (response.data.hobbies && Array.isArray(response.data.hobbies)) {
        list = response.data.hobbies
      }
    }
    
    console.log('Hobbies list:', list)
    
    // 处理封面图片字段映射
    list.forEach(hobby => {
      if (hobby.coverUrl && !hobby.coverImage) {
        hobby.coverImage = hobby.coverUrl
      }
      hobby.coverImage = normalizeImageUrl(hobby.coverImage || hobby.coverUrl)
    })
    hobbies.value = list
  } catch (err) {
    console.error('获取爱好列表失败:', err)
  }
})
</script>

<style scoped>

/* 伪元素图标 */
.icon-eye::before { content: '\f06e'; font-family: 'Font Awesome 5 Free'; font-weight: 900; margin-right: 5px;}
.icon-heart::before { content: '\f004'; font-family: 'Font Awesome 5 Free'; font-weight: 900; margin-right: 5px;}
.icon-comment::before { content: '\f075'; font-family: 'Font Awesome 5 Free'; font-weight: 900; margin-right: 5px;}

.hobbies-page {
  position: relative;
  min-height: calc(100vh - 60px);
  overflow: hidden;
}

.hobbies-container {
  position: relative;
  z-index: 1;
  max-width: 900px;
  margin: 20px auto;
  padding: 20px 20px 48px;
  color: #fff;
}

:global(.dark-theme) .hobbies-container {
  color: #f3f4f6;
}

:global(#app:not(.dark-theme)) .hobbies-container .page-title {
  color: #1e293b;
  text-shadow: 0 2px 12px rgba(255, 255, 255, 0.8);
}

:global(#app:not(.dark-theme)) .hobbies-container .page-subtitle {
  color: #475569;
}

.page-header {
  text-align: center;
  margin-bottom: 50px;
}

.page-title {
  font-size: 36px;
  margin-bottom: 10px;
  text-shadow: 0 0 10px rgba(255, 255, 255, 0.5);
}

.page-subtitle {
  color: #ccc;
  font-size: 16px;
  margin-bottom: 30px;
}

.post-list {
  display: grid;
  grid-template-columns: 1fr;
  gap: 40px;
}

.post-item {
  background-color: rgba(0, 0, 0, 0.3);
  backdrop-filter: blur(15px);
  -webkit-backdrop-filter: blur(15px);
  border-radius: 20px;
  overflow: hidden;
  border: 1px solid rgba(255, 255, 255, 0.1);
  transition: all 0.4s cubic-bezier(0.25, 0.8, 0.25, 1);
  color: #e0e0e0;
  position: relative;
  overflow: hidden;
  margin-bottom: 30px;
}

.post-item::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 1px;
  background: linear-gradient(90deg, transparent, rgba(255, 255, 255, 0.3), transparent);
  transform: translateX(-100%);
  transition: transform 0.6s ease;
}

.post-item:hover::before {
  transform: translateX(100%);
}

.post-item:hover {
  transform: translateY(-5px);
  box-shadow: 0 15px 35px rgba(0, 0, 0, 0.4);
  background-color: rgba(0, 0, 0, 0.4);
  border-color: rgba(255, 255, 255, 0.2);
}

/* 文章容器 */
.post-container {
  display: flex;
  flex-direction: row;
  height: 200px;
}

/* 文章封面 */
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

/* 文章内容 */
.post-content {
  flex: 1;
  padding: 20px;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
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
  transition: all 0.3s ease;
  border: 1px solid rgba(255, 255, 255, 0.1);
}

.tag:hover {
  background-color: rgba(255, 255, 255, 0.2);
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.2);
}

.post-title {
  margin: 0 0 10px 0;
  font-size: 20px;
  color: #ffffff;
  font-weight: 700;
  line-height: 1.3;
  text-shadow: 0 2px 4px rgba(0, 0, 0, 0.3);
  transition: color 0.3s ease;
}

.post-item:hover .post-title {
  color: #a0c4ff;
}

.post-meta {
  font-size: 12px;
  color: #a0a0a0;
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 10px;
  transition: color 0.3s ease;
}

.post-item:hover .post-meta {
  color: #cccccc;
}

.post-summary {
  margin: 0 0 15px 0;
  color: #dcdcdc;
  line-height: 1.6;
  font-size: 14px;
  transition: color 0.3s ease;
  flex: 1;
}

.post-item:hover .post-summary {
  color: #ffffff;
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
  transition: all 0.3s ease;
  border: 1px solid rgba(160, 196, 255, 0.3);
}

.read-more:hover {
  background-color: rgba(160, 196, 255, 0.3);
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(160, 196, 255, 0.3);
}

/* 响应式设计 */
@media (max-width: 768px) {
  .post-container {
    flex-direction: column;
    height: auto;
  }
  
  .post-cover {
    flex: 0 0 200px;
  }
  
  .post-content {
    padding: 20px;
  }
  
  .post-title {
    font-size: 18px;
  }
  
  .hobbies-container {
    padding: 0 15px;
  }
}
</style>

