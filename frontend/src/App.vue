<template>
  <div id="app" :class="{ 'admin-app': isAdminRoute, 'dark-theme': isDark, 'has-music-bar': showMusicBar }">
    <!-- 游客界面 -->
    <template v-if="!isAdminRoute">
      <header class="blog-header">
        <div class="header-container">
          <div class="header-left">
            <router-link to="/" class="logo">
              <span class="logo-text">黑虎阿昊</span>
            </router-link>
            <nav class="main-nav" :class="{ 'is-open': mobileMenuOpen }">
              <router-link to="/" class="nav-item" :class="{ active: route.path === '/' }">首页</router-link>
              <div class="nav-dropdown">
                <span class="nav-item" :class="{ active: route.path.startsWith('/archive') }">归档 ▾</span>
                <div class="dropdown-menu">
                  <router-link to="/archive">全部归档</router-link>
                  <router-link v-for="y in archiveYears" :key="y" :to="`/archive/${y}`">{{ y }}</router-link>
                </div>
              </div>
              <router-link to="/hobbies" class="nav-item" :class="{ active: route.path.startsWith('/hobbies') }">我的爱好</router-link>
              <router-link
                to="/articles"
                class="nav-item"
                :class="{ active: route.path.startsWith('/articles') || route.path.startsWith('/notes') || route.path.startsWith('/post') }"
              >文章</router-link>
              <router-link to="/messages" class="nav-item" :class="{ active: route.path.startsWith('/messages') }">留言板</router-link>
              <router-link to="/music" class="nav-item" :class="{ active: route.path.startsWith('/music') }">音乐</router-link>
              <router-link to="/friends" class="nav-item" :class="{ active: route.path.startsWith('/friends') }">友链</router-link>
              <router-link to="/about" class="nav-item" :class="{ active: route.path.startsWith('/about') }">关于</router-link>
            </nav>
          </div>
          <div class="header-right">
            <button class="header-btn mobile-menu-btn" @click="toggleMobileMenu">
              <span v-if="!mobileMenuOpen">☰</span>
              <span v-else>✕</span>
            </button>
            <button class="header-btn search-btn">
              <span>🔍</span>
            </button>
            <button class="header-btn theme-btn" @click="toggleTheme">
              <span v-if="!isDark">🌙</span>
              <span v-else>☀️</span>
            </button>
            <router-link
              v-if="isAdminLoggedIn"
              to="/admin"
              class="header-btn login-btn"
            >管理后台</router-link>
            <router-link v-else to="/admin/login" class="header-btn login-btn">登录</router-link>
          </div>
        </div>
      </header>
      
      
      <main class="blog-main">
        <router-view v-slot="{ Component }">
          <transition name="fade" mode="out-in">
            <component :is="Component" />
          </transition>
        </router-view>
      </main>
      <footer class="site-footer">
        <div class="site-footer-inner">
          <SiteBeianLinks />
        </div>
      </footer>
      <SiteMusicPlayer />
      <MusicAudioEngine />
    </template>
    
    <!-- 管理端界面 -->
    <template v-else>
      <router-view v-slot="{ Component }">
        <transition name="fade" mode="out-in">
          <component :is="Component" />
        </transition>
      </router-view>
    </template>
  </div>
</template>

<script setup>
import { RouterLink, RouterView } from 'vue-router'
import { computed, ref, onMounted, watch } from 'vue'
import { useRoute } from 'vue-router'
import SiteMusicPlayer from './components/SiteMusicPlayer.vue'
import MusicAudioEngine from './components/MusicAudioEngine.vue'
import SiteBeianLinks from './components/SiteBeianLinks.vue'
import { loadArchiveYears } from '@/utils/archiveContent'
import { loadSiteMusicPlaylist } from '@/utils/musicPlaylist'
import { useMusicStore } from '@/stores/music'
import { storeToRefs } from 'pinia'

const route = useRoute()
const musicStore = useMusicStore()
const { currentTrack, barVisible } = storeToRefs(musicStore)
const isAdminRoute = computed(() => route.path.startsWith('/admin'))
const showMusicBar = computed(() => !isAdminRoute.value && !!currentTrack.value && barVisible.value)
const isAdminLoggedIn = computed(() => {
  return !!localStorage.getItem('adminToken') && localStorage.getItem('isAdmin') === 'true'
})
const isDark = ref(localStorage.getItem('theme') === 'dark')
const mobileMenuOpen = ref(false)
const archiveYears = ref([])

onMounted(async () => {
  try {
    archiveYears.value = await loadArchiveYears()
  } catch (e) {
    console.error('加载归档年份失败:', e)
    archiveYears.value = []
  }
  if (!isAdminRoute.value) {
    await loadSiteMusicPlaylist()
  }
})

watch(isAdminRoute, async (admin) => {
  if (!admin) {
    await loadSiteMusicPlaylist()
  }
})

// 主题切换
const toggleTheme = () => {
  isDark.value = !isDark.value
  localStorage.setItem('theme', isDark.value ? 'dark' : 'light')
}

const toggleMobileMenu = () => {
  mobileMenuOpen.value = !mobileMenuOpen.value
}

</script>

<style> /* Changed to global style to apply background */
@import url('https://fonts.googleapis.com/css2?family=Noto+Sans+SC:wght@400;700&display=swap');

:root {
  --primary-color: #a0c4ff;
  --secondary-color: #ffc8dd;
  --text-color: #333333;
  --bg-color: #f5f5f5;
  --card-bg: #ffffff;
  --border-color: #eaeaea;
  --hover-color: #f0f0f0;
}

/* 深色主题 */
.dark-theme {
  --text-color: #e0e0e0;
  --bg-color: #1a1a1a;
  --card-bg: #2d2d2d;
  --border-color: #404040;
  --hover-color: #3a3a3a;
}

* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

body {
  font-family: 'Noto Sans SC', sans-serif;
  background-color: var(--bg-color);
  color: var(--text-color);
  line-height: 1.6;
  min-height: 100vh;
  transition: background-color 0.3s ease, color 0.3s ease;
}

#app {
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
}

/* 管理端界面样式 */
#app.admin-app {
  background-color: #f5f7fa;
  color: #303133;
}

/* 博客头部：全宽渐层 + 细纹理，避免大片留白过于单调 */
.blog-header {
  position: sticky;
  top: 0;
  z-index: 100;
  overflow: hidden;
  border-bottom: 1px solid rgba(160, 196, 255, 0.35);
  background-color: rgba(255, 255, 255, 0.88);
  background-image:
    radial-gradient(ellipse 120% 80% at 0% 0%, rgba(160, 196, 255, 0.28), transparent 55%),
    radial-gradient(ellipse 100% 90% at 100% 0%, rgba(255, 200, 221, 0.22), transparent 50%),
    radial-gradient(circle at 50% 120%, rgba(224, 231, 255, 0.45), transparent 42%),
    repeating-linear-gradient(
      -12deg,
      transparent,
      transparent 14px,
      rgba(255, 255, 255, 0.06) 14px,
      rgba(255, 255, 255, 0.06) 15px
    );
  backdrop-filter: blur(14px) saturate(1.08);
  -webkit-backdrop-filter: blur(14px) saturate(1.08);
  box-shadow: 0 4px 28px rgba(29, 78, 216, 0.07);
  transition: background-color 0.3s ease, box-shadow 0.3s ease;
}

.blog-header::after {
  content: '';
  position: absolute;
  left: 0;
  right: 0;
  bottom: 0;
  height: 2px;
  background: linear-gradient(
    90deg,
    transparent 0%,
    rgba(160, 196, 255, 0.55) 22%,
    rgba(255, 200, 221, 0.5) 78%,
    transparent 100%
  );
  pointer-events: none;
}

.dark-theme .blog-header {
  border-bottom-color: rgba(96, 165, 250, 0.25);
  background-color: rgba(22, 22, 26, 0.92);
  background-image:
    radial-gradient(ellipse 120% 80% at 0% 0%, rgba(59, 130, 246, 0.18), transparent 55%),
    radial-gradient(ellipse 100% 90% at 100% 0%, rgba(168, 85, 247, 0.14), transparent 50%),
    radial-gradient(circle at 50% 120%, rgba(30, 58, 138, 0.35), transparent 45%);
  box-shadow: 0 4px 28px rgba(0, 0, 0, 0.35);
}

.dark-theme .blog-header::after {
  background: linear-gradient(
    90deg,
    transparent 0%,
    rgba(96, 165, 250, 0.45) 25%,
    rgba(192, 132, 252, 0.4) 75%,
    transparent 100%
  );
}

.header-container {
  position: relative;
  z-index: 1;
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 20px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  height: 60px;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 40px;
}

.logo {
  text-decoration: none;
}

.logo-text {
  font-size: 20px;
  font-weight: 800;
  letter-spacing: 0.02em;
  background: linear-gradient(115deg, #1e3a8a 0%, #2563eb 42%, #7c3aed 100%);
  -webkit-background-clip: text;
  background-clip: text;
  color: transparent;
  transition: filter 0.3s ease, opacity 0.3s ease;
}

.logo:hover .logo-text {
  filter: brightness(1.08);
}

.dark-theme .logo-text {
  background: linear-gradient(115deg, #bfdbfe 0%, #93c5fd 40%, #e9d5ff 100%);
  -webkit-background-clip: text;
  background-clip: text;
  color: transparent;
}

.main-nav {
  display: flex;
  align-items: center;
  gap: 20px;
}

.nav-item {
  color: var(--text-color);
  text-decoration: none;
  font-size: 14px;
  font-weight: 500;
  padding: 8px 12px;
  border-radius: 999px;
  transition: all 0.3s ease;
  cursor: pointer;
}

.nav-item:hover {
  background-color: #eff6ff;
  color: #1d4ed8;
}

.nav-item.active {
  background-color: #dbeafe;
  color: #1d4ed8;
  font-weight: 600;
}

.nav-dropdown {
  position: relative;
}

.dropdown-menu {
  position: absolute;
  top: 100%;
  left: 0;
  background-color: var(--card-bg);
  border: 1px solid var(--border-color);
  border-radius: 4px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  padding: 8px 0;
  min-width: 120px;
  display: none;
  z-index: 1000;
}

.nav-dropdown:hover .dropdown-menu {
  display: block;
}

.dropdown-menu a {
  display: block;
  padding: 8px 16px;
  color: var(--text-color);
  text-decoration: none;
  font-size: 14px;
  transition: background-color 0.3s ease;
}

.dropdown-menu a:hover {
  background-color: var(--hover-color);
}

.dropdown-menu a.active {
  color: #1d4ed8;
  font-weight: 600;
  background-color: #eff6ff;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 15px;
}

.mobile-menu-btn {
  display: none;
}

.header-btn {
  background-color: transparent;
  border: 1px solid var(--border-color);
  border-radius: 4px;
  padding: 6px 12px;
  color: var(--text-color);
  font-size: 14px;
  cursor: pointer;
  transition: all 0.3s ease;
  text-decoration: none;
  display: inline-flex;
  align-items: center;
  gap: 5px;
}

.header-btn:hover {
  background-color: var(--hover-color);
}

.login-btn {
  background-color: var(--primary-color);
  color: white;
  border-color: var(--primary-color);
}

.login-btn:hover {
  background-color: #8bb8ff;
  border-color: #8bb8ff;
  color: white;
}

.banner-slideshow {
  position: relative;
  width: 100%;
  height: 100%;
}

.banner-img {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  object-fit: cover;
  opacity: 0;
  transition: opacity 1s ease, transform 0.5s ease;
  z-index: 1;
}

.banner-img.active {
  opacity: 1;
  z-index: 2;
}

.top-banner:hover .banner-img {
  transform: scale(1.05);
}

.banner-content {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  text-align: center;
  z-index: 10;
  animation: fadeInDown 0.8s ease;
}

.banner-title {
  font-size: 64px;
  font-weight: bold;
  color: white;
  text-shadow: 0 4px 20px rgba(0, 0, 0, 0.7);
  margin: 0;
  letter-spacing: 2px;
}

@keyframes fadeInDown {
  from {
    opacity: 0;
    transform: translate(-50%, -60%);
  }
  to {
    opacity: 1;
    transform: translate(-50%, -50%);
  }
}

/* 博客主内容（顶栏为 sticky 仍在文档流占位，勿再加 padding-top，否则首页全屏图上方会出现一条 bg 色空隙） */
#app.has-music-bar {
  --music-bar-height: 66px;
}

.blog-main {
  padding: 0 0 24px;
  background-color: var(--bg-color);
  transition: background-color 0.3s ease;
}

#app.has-music-bar .blog-main {
  padding-bottom: calc(24px + var(--music-bar-height, 66px));
}

#app.has-music-bar .site-footer {
  padding-bottom: calc(28px + var(--music-bar-height, 66px));
}

.site-footer {
  background-color: var(--bg-color);
  border-top: 1px solid var(--border-color);
  padding: 14px 20px 28px;
  transition: background-color 0.3s ease, border-color 0.3s ease;
}

.site-footer-inner {
  max-width: 1200px;
  margin: 0 auto;
  text-align: center;
}

.container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 20px;
}

.content-wrapper {
  display: grid;
  grid-template-columns: 1fr 300px;
  gap: 30px;
}

.main-content {
  background-color: var(--card-bg);
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  padding: 30px;
  transition: all 0.3s ease;
}

.sidebar {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.sidebar-widget {
  background-color: var(--card-bg);
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  padding: 20px;
  transition: all 0.3s ease;
}

.sidebar-widget:hover {
  transform: translateY(-5px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
}

.widget-title {
  font-size: 16px;
  font-weight: bold;
  color: var(--text-color);
  margin-bottom: 15px;
  padding-bottom: 10px;
  border-bottom: 1px solid var(--border-color);
}

/* 作者卡片 */
.author-card {
  text-align: center;
}

.author-avatar {
  width: 100px;
  height: 100px;
  border-radius: 50%;
  margin-bottom: 15px;
  border: 3px solid var(--primary-color);
  box-shadow: 0 0 15px rgba(160, 196, 255, 0.5);
  transition: all 0.3s ease;
}

.author-avatar:hover {
  transform: scale(1.1) rotate(5deg);
  box-shadow: 0 0 20px rgba(160, 196, 255, 0.7);
}

.author-name {
  font-size: 18px;
  font-weight: bold;
  color: var(--text-color);
  margin: 0 0 10px 0;
}

.author-bio {
  font-size: 14px;
  color: #666;
  margin-bottom: 20px;
}

.dark-theme .author-bio {
  color: #aaa;
}

.author-stats {
  display: flex;
  justify-content: space-around;
  padding-top: 15px;
  border-top: 1px solid var(--border-color);
}

.stat-item {
  text-align: center;
}

.stat-number {
  display: block;
  font-size: 18px;
  font-weight: bold;
  color: var(--primary-color);
}

.stat-label {
  display: block;
  font-size: 12px;
  color: #666;
  margin-top: 5px;
}

.dark-theme .stat-label {
  color: #aaa;
}

/* 公告卡片 */
.notice-content {
  font-size: 14px;
  color: var(--text-color);
  line-height: 1.6;
}

/* 时钟卡片 */
.clock-display {
  text-align: center;
}

.clock-time {
  display: block;
  font-size: 24px;
  font-weight: bold;
  color: var(--primary-color);
  margin-bottom: 5px;
}

.clock-date {
  display: block;
  font-size: 14px;
  color: #666;
}

.dark-theme .clock-date {
  color: #aaa;
}

/* 随机文章 */
.post-list {
  list-style: none;
}

.post-list li {
  margin-bottom: 10px;
  padding-bottom: 10px;
  border-bottom: 1px solid var(--border-color);
  transition: all 0.3s ease;
}

.post-list li:last-child {
  margin-bottom: 0;
  padding-bottom: 0;
  border-bottom: none;
}

.post-list li:hover {
  transform: translateX(5px);
}

.post-list a {
  color: var(--text-color);
  text-decoration: none;
  font-size: 14px;
  transition: color 0.3s ease;
}

.post-list a:hover {
  color: var(--primary-color);
}

/* 路由切换动画 */
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.3s ease, transform 0.3s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
  transform: translateY(20px);
}

/* 按钮悬停动画 */
.el-button {
  transition: all 0.3s ease;
}

.el-button:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
}

/* 卡片加载动画 */
.el-card {
  transition: all 0.3s ease;
}

.el-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 6px 16px rgba(0, 0, 0, 0.12);
}

/* 表单输入动画 */
.el-input__wrapper {
  transition: all 0.3s ease;
}

.el-input__wrapper.is-focus {
  box-shadow: 0 0 0 2px rgba(64, 158, 255, 0.2) !important;
}

/* 侧边栏动画 */
.admin-sidebar {
  transition: all 0.3s ease;
}

.admin-content {
  transition: all 0.3s ease;
}

/* 统计卡片动画 */
.el-statistic {
  transition: all 0.3s ease;
}

.el-statistic:hover {
  transform: scale(1.02);
}

/* 时间线动画 */
.el-timeline-item {
  animation: slideIn 0.5s ease-out;
}

@keyframes slideIn {
  from {
    opacity: 0;
    transform: translateX(-20px);
  }
  to {
    opacity: 1;
    transform: translateX(0);
  }
}

/* 表格行悬停动画 */
.el-table__row {
  transition: all 0.2s ease;
}

.el-table__row:hover {
  background-color: rgba(64, 158, 255, 0.05) !important;
}

/* 对话框动画 */
.el-dialog__wrapper {
  animation: dialogFadeIn 0.3s ease;
}

@keyframes dialogFadeIn {
  from {
    opacity: 0;
  }
  to {
    opacity: 1;
  }
}

.el-dialog {
  animation: dialogSlideIn 0.3s ease;
}

@keyframes dialogSlideIn {
  from {
    opacity: 0;
    transform: translate(-50%, -60%);
  }
  to {
    opacity: 1;
    transform: translate(-50%, -50%);
  }
}

/* 响应式设计 */
@media (max-width: 768px) {
  .header-container {
    padding: 0 15px;
  }

  .mobile-menu-btn {
    display: inline-flex;
  }
  
  .header-left {
    gap: 20px;
  }
  
  .main-nav {
    position: absolute;
    top: 60px;
    left: 0;
    right: 0;
    background: var(--card-bg);
    border-bottom: 1px solid var(--border-color);
    padding: 12px 15px;
    display: none;
    flex-direction: column;
    align-items: stretch;
    gap: 8px;
  }

  .main-nav.is-open {
    display: flex;
  }
  
  .nav-item {
    font-size: 14px;
    padding: 8px 12px;
    text-align: left;
  }

  .nav-dropdown:hover .dropdown-menu {
    display: none;
  }

  .nav-dropdown .dropdown-menu {
    position: static;
    display: block;
    border: none;
    box-shadow: none;
    padding: 4px 0 0 10px;
    min-width: auto;
  }
  
  .top-banner {
    height: 250px;
  }
  
  .banner-title {
    font-size: 32px;
  }
  
  .content-wrapper {
    grid-template-columns: 1fr;
    gap: 20px;
  }
  
  .main-content {
    padding: 20px;
  }
  
  .sidebar-widget {
    padding: 15px;
  }
}

@media (max-width: 480px) {
  .header-right {
    gap: 10px;
  }
  
  .header-btn {
    padding: 4px 8px;
    font-size: 12px;
  }
  
  .top-banner {
    height: 200px;
  }
  
  .banner-title {
    font-size: 24px;
  }
  
  .main-content {
    padding: 15px;
  }
}
</style>




