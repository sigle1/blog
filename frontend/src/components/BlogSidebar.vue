<template>
  <aside class="sidebar">
    <div class="sidebar-widget author-card">
      <div class="author-card-cover" aria-hidden="true" />
      <img :src="authorAvatarSrc" alt="Author Avatar" class="author-avatar">
      <h3 class="author-name">{{ authorDisplayName }}</h3>
      <p class="author-bio">{{ authorMotto }}</p>
      <div class="author-stats">
        <div class="stat-item">
          <span class="stat-number">{{ postCount }}</span>
          <span class="stat-label">文章数</span>
        </div>
        <div class="stat-item">
          <span class="stat-number">{{ categoryCount }}</span>
          <span class="stat-label">分类数</span>
        </div>
        <div class="stat-item">
          <span class="stat-number">{{ commentCount }}</span>
          <span class="stat-label">评论数</span>
        </div>
      </div>
    </div>

    <div class="sidebar-widget notice-card">
      <h3 class="widget-title">📢 公告</h3>
      <p class="notice-content">{{ noticeText }}</p>
    </div>

    <div class="sidebar-widget clock-card">
      <h3 class="widget-title">🕒 电子时钟</h3>
      <div class="clock-display">
        <span class="clock-time">{{ currentTime }}</span>
        <span class="clock-date">{{ currentDate }}</span>
      </div>
    </div>

    <div class="sidebar-widget random-posts">
      <h3 class="widget-title">📚 随机文章</h3>
      <ul class="post-list">
        <li v-for="post in randomPosts" :key="post.id">
          <router-link :to="`/post/${post.id}`" class="random-post-link">
            <img
              v-if="post.coverImage"
              :src="post.coverImage"
              :alt="post.title"
              class="random-post-thumb"
            >
            <span class="random-post-title">{{ post.title }}</span>
          </router-link>
        </li>
      </ul>
      <p v-if="!randomPosts.length" class="random-empty">暂无文章</p>
    </div>
  </aside>
</template>

<script setup>
import { ref, computed, onMounted, onBeforeUnmount } from 'vue'
import api from '@/api'
import { normalizeImageUrl } from '@/utils/image'
import defaultAuthorAvatar from '@/assets/avatars/0a9f517c44d4399a9d922f46527e87d8.jpg'

defineProps({
  noticeText: {
    type: String,
    default: '欢迎指出网站的不足，给我提供意见'
  }
})

const authorDisplayName = ref('黑虎阿昊')
const authorMotto = ref('不经一番寒彻骨，怎得梅花扑鼻香')
const authorAvatar = ref('')
const postCount = ref(0)
const categoryCount = ref(0)
const commentCount = ref(0)
const randomPosts = ref([])
const currentTime = ref('')
const currentDate = ref('')
let clockTimer = null

const authorAvatarSrc = computed(() => {
  const u = normalizeImageUrl(authorAvatar.value)
  return u || defaultAuthorAvatar
})

function updateClock() {
  const now = new Date()
  currentTime.value = now.toLocaleTimeString('zh-CN', { hour12: false })
  currentDate.value = now.toLocaleDateString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    weekday: 'long'
  })
}

onMounted(async () => {
  updateClock()
  clockTimer = setInterval(updateClock, 1000)
  try {
    const [heroRes, postsRes] = await Promise.all([
      api.getHomeHero().catch(() => null),
      api.getPosts().catch(() => null)
    ])
    const c = heroRes?.data || {}
    authorDisplayName.value = (c.authorDisplayName && String(c.authorDisplayName).trim()) || '黑虎阿昊'
    authorMotto.value = (c.authorMotto && String(c.authorMotto).trim()) || '不经一番寒彻骨，怎得梅花扑鼻香'
    authorAvatar.value = c.authorAvatar != null ? String(c.authorAvatar).trim() : ''
    postCount.value = Number(c.postCount) || 0
    categoryCount.value = Number(c.categoryCount) || 0
    commentCount.value = Number(c.commentCount) || 0

    let list = []
    const data = postsRes?.data
    if (Array.isArray(data)) list = data
    else if (data?.data && Array.isArray(data.data)) list = data.data
    else if (data?.posts && Array.isArray(data.posts)) list = data.posts
    list.forEach((p) => {
      p.coverImage = normalizeImageUrl(p.coverImage || p.coverUrl || p.coverTitle)
    })
    if (list.length) {
      randomPosts.value = [...list].sort(() => 0.5 - Math.random()).slice(0, 5)
    }
  } catch (e) {
    console.error(e)
  }
})

onBeforeUnmount(() => {
  if (clockTimer) clearInterval(clockTimer)
})
</script>

<style scoped>
.sidebar {
  width: 300px;
  flex-shrink: 0;
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.author-card {
  position: relative;
  text-align: center;
  padding-top: 48px !important;
  overflow: hidden;
}

.author-card-cover {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 88px;
  background: linear-gradient(135deg, #1e3a5f 0%, #312e81 50%, #4c1d95 100%);
}

.author-avatar {
  position: relative;
  z-index: 1;
  width: 72px;
  height: 72px;
  border-radius: 50%;
  margin: 0 auto 12px;
  border: 3px solid var(--card-bg, #fff);
  object-fit: cover;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.12);
}

.author-name {
  font-size: 18px;
  font-weight: bold;
  margin: 0 0 8px;
}

.author-bio {
  font-size: 13px;
  color: #666;
  margin: 0 0 16px;
  line-height: 1.5;
  padding: 0 8px;
}

:global(.dark-theme) .author-bio {
  color: #aaa;
}

.author-stats {
  display: flex;
  justify-content: space-around;
  padding-top: 12px;
  border-top: 1px solid var(--border-color, #eaeaea);
}

.stat-number {
  display: block;
  font-size: 18px;
  font-weight: bold;
  color: var(--primary-color, #a0c4ff);
}

.stat-label {
  display: block;
  font-size: 12px;
  color: #666;
  margin-top: 4px;
}

:global(.dark-theme) .stat-label {
  color: #aaa;
}

.random-post-link {
  display: flex;
  align-items: center;
  gap: 10px;
  color: var(--text-color);
  text-decoration: none;
  font-size: 14px;
}

.random-post-link:hover {
  color: var(--primary-color);
}

.random-post-thumb {
  width: 48px;
  height: 36px;
  object-fit: cover;
  border-radius: 4px;
  flex-shrink: 0;
}

.random-post-title {
  flex: 1;
  line-height: 1.4;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
}

.random-empty {
  margin: 0;
  font-size: 13px;
  color: #999;
}

@media (max-width: 768px) {
  .sidebar {
    width: 100%;
  }
}
</style>
