<template>
  <div class="home-page">
    <div class="banner">
      <div class="banner-slideshow">
        <HeroBackdrop
          v-if="bannerImages.length > 0"
          :items="bannerImages"
          :active-index="currentImageIndex"
          variant="banner"
        />
        <div v-else class="banner-bg-fallback" aria-hidden="true" />
      </div>
      <div class="banner-content">
        <h1 class="title">{{ heroTitle }}</h1>
        <p class="subtitle" :key="currentSubtitle">{{ currentSubtitle }}</p>
      </div>
      <div class="wave">
        <svg
          data-v-33191232=""
          xmlns="http://www.w3.org/2000/svg"
          viewBox="0 0 1440 320"
          class="wave-svg"
        >
          <path
            data-v-33191232=""
            fill-opacity="1"
            d="M0,128L80,138.7C160,149,320,171,480,170.7C640,171,800,149,960,133.3C1120,117,1280,107,1360,101.3L1440,96L1440,320L1360,320C1280,320,1120,320,960,320C800,320,640,320,480,320C320,320,160,320,80,320L0,320Z"
          ></path>
        </svg>
      </div>
    </div>
    <div class="home-layout">
      <div class="container">
        <div class="content-wrapper">
          <!-- 主内容区 -->
          <div class="main-content">
            <div class="post-list">
              <div class="post-item" v-for="post in posts" :key="post.id">
                <div class="post-container">
                  <!-- 文章封面图片 -->
                  <div class="post-cover" v-if="post.coverImage">
                    <img :src="post.coverImage" :alt="post.title" class="cover-image">
                  </div>
                  
                  <div class="post-content">
                    <div class="post-tags">
                      <span v-for="tag in post.tags" :key="tag" class="tag">{{ tag }}</span>
                    </div>
                    <h2 class="post-title">{{ post.title }}</h2>
                    <div class="post-meta">
                      <span class="post-views"><i class="icon-eye"></i> {{ post.views }}</span>
                      <span class="post-likes"><i class="icon-heart"></i> {{ post.likes || 0 }}</span>
                      <span class="post-comments"><i class="icon-comment"></i> {{ post.comments || 0 }}</span>
                    </div>
                    <p class="post-summary">{{ post.summary }}</p>
                    <div class="post-footer">
                      <span class="post-date">发布于 {{ formatDate(post.createdAt) }}</span>
                      <router-link :to="`/post/${post.id}`" class="read-more">阅读更多</router-link>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </div>
          
          <!-- 侧边栏 -->
          <aside class="sidebar">
            <!-- 作者信息 -->
            <div class="sidebar-widget author-card">
              <img :src="authorAvatarSrc" alt="Author Avatar" class="author-avatar">
              <h3 class="author-name">{{ authorDisplayName }}</h3>
              <p class="author-bio">{{ authorMotto }}</p>
              <div class="author-stats">
                <div class="stat-item">
                  <span class="stat-number">{{ postCountDisplay }}</span>
                  <span class="stat-label">文章数</span>
                </div>
                <div class="stat-item">
                  <span class="stat-number">{{ categoryCountDisplay }}</span>
                  <span class="stat-label">分类数</span>
                </div>
                <div class="stat-item">
                  <span class="stat-number">{{ commentCountDisplay }}</span>
                  <span class="stat-label">评论数</span>
                </div>
              </div>
            </div>
            
            <!-- 公告 -->
            <div class="sidebar-widget notice-card">
              <h3 class="widget-title">📢 公告</h3>
              <p class="notice-content">欢迎指出网站的不足，给我提供意见</p>
            </div>
            
            <!-- 电子时钟 -->
            <div class="sidebar-widget clock-card">
              <h3 class="widget-title">🕒 电子时钟</h3>
              <div class="clock-display">
                <span class="clock-time">{{ currentTime }}</span>
                <span class="clock-date">{{ currentDate }}</span>
              </div>
            </div>
            
            <!-- 随机文章 -->
            <div class="sidebar-widget random-posts">
              <h3 class="widget-title">📚 随机文章</h3>
              <ul class="post-list">
                <li v-for="post in randomPosts" :key="post.id">
                  <router-link :to="`/post/${post.id}`">{{ post.title }}</router-link>
                </li>
              </ul>
            </div>
          </aside>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onBeforeUnmount } from 'vue'
import api from '@/api'
import { normalizeImageUrl } from '@/utils/image'
import HeroBackdrop from '@/components/HeroBackdrop.vue'
import defaultAuthorAvatar from '@/assets/avatars/0a9f517c44d4399a9d922f46527e87d8.jpg'

const posts = ref([])
const currentTime = ref('')
const currentDate = ref('')
const randomPosts = ref([])

/** 首页大图与侧栏：来自 GET /api/home/hero（壁纸多为后台上传后的 /uploads/...） */
const heroTitle = ref('琉璃月')
const bannerImages = ref([])
const currentImageIndex = ref(0)
let slideshowInterval = null

const DEFAULT_SUBTITLES = [
  '落花人独立，微雨燕双飞。',
  '人生若只如初见，何事秋风悲画扇。',
  '一川烟草，满城风絮，梅子黄时雨。',
  '疏影横斜水清浅，暗香浮动月黄昏。'
]
const subtitles = ref([...DEFAULT_SUBTITLES])
const currentSubtitle = ref(subtitles.value[0])
let subtitleInterval = null

const authorDisplayName = ref('黑虎阿昊')
const authorMotto = ref('不经一番寒彻骨，怎得梅花扑鼻香')
const authorAvatar = ref('')
const postCount = ref(0)
const categoryCount = ref(0)
const commentCount = ref(0)

const authorAvatarSrc = computed(() => {
  const u = normalizeImageUrl(authorAvatar.value)
  return u || defaultAuthorAvatar
})
const postCountDisplay = computed(() => postCount.value ?? 0)
const categoryCountDisplay = computed(() => categoryCount.value ?? 0)
const commentCountDisplay = computed(() => commentCount.value ?? 0)

function applyHomeHero(data) {
  const c = data && typeof data === 'object' ? data : {}
  heroTitle.value = (c.heroTitle && String(c.heroTitle).trim()) || '琉璃月'
  const subs = Array.isArray(c.heroSubtitles) ? c.heroSubtitles.map((s) => String(s).trim()).filter(Boolean) : []
  subtitles.value = subs.length > 0 ? subs : [...DEFAULT_SUBTITLES]
  currentSubtitle.value = subtitles.value[0]
  const raw = Array.isArray(c.bannerImages) ? c.bannerImages : []
  bannerImages.value = raw.map((u) => normalizeImageUrl(String(u))).filter(Boolean)
  currentImageIndex.value = 0
  authorDisplayName.value = (c.authorDisplayName && String(c.authorDisplayName).trim()) || '黑虎阿昊'
  authorMotto.value = (c.authorMotto && String(c.authorMotto).trim()) || '不经一番寒彻骨，怎得梅花扑鼻香'
  authorAvatar.value = c.authorAvatar != null ? String(c.authorAvatar).trim() : ''
  postCount.value = typeof c.postCount === 'number' ? c.postCount : Number(c.postCount) || 0
  categoryCount.value = typeof c.categoryCount === 'number' ? c.categoryCount : Number(c.categoryCount) || 0
  commentCount.value = typeof c.commentCount === 'number' ? c.commentCount : Number(c.commentCount) || 0
}

const startSubtitleRotation = () => {
  let subtitleIndex = 0
  subtitleInterval = setInterval(() => {
    subtitleIndex = (subtitleIndex + 1) % subtitles.value.length
    currentSubtitle.value = subtitles.value[subtitleIndex]
  }, 5000) // 与图片轮播同步
}

const stopSubtitleRotation = () => {
  if (subtitleInterval) {
    clearInterval(subtitleInterval)
    subtitleInterval = null
  }
}


const startSlideshow = () => {
  slideshowInterval = setInterval(() => {
    currentImageIndex.value = (currentImageIndex.value + 1) % bannerImages.value.length
  }, 5000) // 每5秒切换一次
}

const stopSlideshow = () => {
  if (slideshowInterval) {
    clearInterval(slideshowInterval)
    slideshowInterval = null
  }
}


// 更新时钟
const updateClock = () => {
  const now = new Date()
  currentTime.value = now.toLocaleTimeString('zh-CN')
  currentDate.value = now.toLocaleDateString('zh-CN', { 
    year: 'numeric', 
    month: 'long', 
    day: 'numeric',
    weekday: 'long'
  })
}


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
  updateClock()
  setInterval(updateClock, 1000)
  try {
    const [response, heroRes] = await Promise.all([api.getPosts(), api.getHomeHero().catch(() => null)])
    if (heroRes?.data) {
      applyHomeHero(heroRes.data)
    }
    console.log('API Response:', response)
    
    // axios 返回的响应在 response.data 中
    let list = []
    if (response.data) {
      if (Array.isArray(response.data)) {
        list = response.data
      } else if (response.data.data && Array.isArray(response.data.data)) {
        list = response.data.data
      } else if (response.data.posts && Array.isArray(response.data.posts)) {
        list = response.data.posts
      }
    }
    
    console.log('Posts list:', list)
    
    // 设置随机文章
    if (list.length > 0) {
      const shuffled = [...list].sort(() => 0.5 - Math.random())
      randomPosts.value = shuffled.slice(0, 5)
    }
    
    // 统一 createdAt 为可比较的时间戳（支持字符串/数字）
    list.forEach(p => {
      // 处理封面图片字段映射
      if (p.coverTitle && !p.coverImage) {
        p.coverImage = p.coverTitle
      }
      p.coverImage = normalizeImageUrl(p.coverImage || p.coverUrl || p.coverTitle)
      if (Array.isArray(p.createdAt)) {
        const [y, m = 1, d = 1, hh = 0, mm = 0, ss = 0] = p.createdAt
        p._ts = new Date(y, (m || 1) - 1, d, hh, mm, ss).getTime()
      } else if (typeof p.createdAt === 'number') {
        p._ts = p.createdAt
      } else {
        const t = new Date(p.createdAt).getTime()
        p._ts = isNaN(t) ? 0 : t
      }
    })
    // 按时间降序，取最新三篇用于首页展示
    posts.value = list.sort((a, b) => (b._ts || 0) - (a._ts || 0)).slice(0, 3)
  } catch (err) {
    console.error('获取文章列表失败:', err)
    // 如果获取失败，posts将为空数组
  }
  if (bannerImages.value.length > 0) {
    startSlideshow()
  }
  startSubtitleRotation()
})

onBeforeUnmount(() => {
  stopSlideshow()
  stopSubtitleRotation()
})
</script>

<style scoped>
.content-wrapper {
  display: flex;
  gap: 20px;
}
.main-content {
  flex: 1;
}
.sidebar {
  width: 300px;
}

/* 伪元素图标 */
.icon-eye::before { content: '\f06e'; font-family: 'Font Awesome 5 Free'; font-weight: 900; margin-right: 5px;}
.icon-heart::before { content: '\f004'; font-family: 'Font Awesome 5 Free'; font-weight: 900; margin-right: 5px;}
.icon-comment::before { content: '\f075'; font-family: 'Font Awesome 5 Free'; font-weight: 900; margin-right: 5px;}

/* 平滑滚动 */
html {
  scroll-behavior: smooth;
}

.home-page {
  height: 100%;
  width: 100%;
}

.banner {
  height: 100vh;
  width: 100%;
  position: relative;
  overflow: hidden;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  text-align: center;
  color: white;
}

.banner-slideshow {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  z-index: 0;
}

.banner-bg-fallback {
  position: absolute;
  inset: 0;
  background: linear-gradient(135deg, #1e3a8a 0%, #4c1d95 45%, #831843 100%);
}

.banner-content {
  z-index: 1;
}

.title {
  font-size: 4rem;
  font-weight: bold;
  text-shadow: 2px 2px 8px rgba(0, 0, 0, 0.7);
  margin: 0;
}

.subtitle {
  font-size: 1.5rem;
  margin-top: 1rem;
  text-shadow: 1px 1px 4px rgba(0, 0, 0, 0.7);
  background-color: rgba(0, 0, 0, 0.3);
  padding: 0.5rem 1rem;
  border-radius: 8px;
  display: inline-block;
  transition: all 0.5s ease-in-out;
}

.wave {
  position: absolute;
  bottom: 0;
  left: 0;
  width: 100%;
  overflow: hidden;
  line-height: 0;
}

.wave-svg {
  position: relative;
  display: block;
  width: calc(100% + 1.3px);
  height: 96px;
  fill: var(--bg-color);
}

.home-layout {
  background-color: var(--bg-color);
}

.main-content {
  width: 100%;
  max-width: 900px;
  margin: 0 auto;
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
  
  .main-content {
    padding: 0 15px;
  }
}
</style>




