<template>
  <div class="admin-layout">
    <!-- 侧边栏 -->
    <div class="admin-sidebar" :class="{ 'sidebar-collapsed': collapsed }">
      <div class="sidebar-header">
        <div class="logo">
          <span class="logo-text">ruyu-blog</span>
        </div>
        <el-button 
          link 
          @click="toggleSidebar" 
          class="collapse-btn"
          size="large"
        >
          <span v-if="collapsed">📋</span>
          <span v-else>📃</span>
        </el-button>
      </div>
      <div class="sidebar-menu">
        <el-menu
          :default-active="activeMenu"
          class="sidebar-menu"
          @select="handleMenuSelect"
          background-color="#f0f2f5"
          text-color="#303133"
          active-text-color="#409EFF"
        >
          <el-menu-item index="/admin">
            <template #icon>
              <span>🏠</span>
            </template>
            <span>管理首页</span>
          </el-menu-item>
          <el-menu-item index="__site_home__">
            <template #icon>
              <span>🌐</span>
            </template>
            <span>查看博客</span>
          </el-menu-item>
          <el-menu-item index="/admin/home-hero">
            <template #icon>
              <span>🖼️</span>
            </template>
            <span>首页壁纸</span>
          </el-menu-item>
          <el-menu-item index="/admin/about">
            <template #icon>
              <span>ℹ️</span>
            </template>
            <span>关于页</span>
          </el-menu-item>
          <el-menu-item index="/admin/site">
            <template #icon>
              <span>🔖</span>
            </template>
            <span>站点图标</span>
          </el-menu-item>
          <el-menu-item index="/admin/music">
            <template #icon>
              <span>🎵</span>
            </template>
            <span>音乐管理</span>
          </el-menu-item>
          <el-menu-item index="/admin/hobbies">
            <template #icon>
              <span>🎯</span>
            </template>
            <span>爱好管理</span>
          </el-menu-item>
          <el-menu-item index="/admin/notes">
            <template #icon>
              <span>📝</span>
            </template>
            <span>笔记管理</span>
          </el-menu-item>
          <el-menu-item index="/admin/posts">
            <template #icon>
              <span>📄</span>
            </template>
            <span>文章管理</span>
          </el-menu-item>
          <el-menu-item index="/admin/comments">
            <template #icon>
              <span>💬</span>
            </template>
            <span>评论管理</span>
          </el-menu-item>
          <el-menu-item index="/admin/messages">
            <template #icon>
              <span>📮</span>
            </template>
            <span>留言管理</span>
          </el-menu-item>
        </el-menu>
      </div>
    </div>
    <!-- 主内容区 -->
    <div class="admin-content">
      <!-- 顶部导航栏 -->
      <div class="admin-header">
        <div class="header-left">
          <el-button 
            link 
            @click="toggleSidebar" 
            class="menu-btn"
            size="large"
          >
            <span>☰</span>
          </el-button>
          <h1 class="page-title">{{ pageTitle }}</h1>
        </div>
        <div class="header-right">
          <router-link to="/" class="back-site-btn">
            <span class="back-site-icon">🏠</span>
            <span class="back-site-text">查看博客</span>
          </router-link>
          <el-dropdown trigger="click">
            <span class="user-dropdown">
              <el-avatar :size="32">
                <img src="@/assets/avatars/0a9f517c44d4399a9d922f46527e87d8.jpg" alt="用户头像">
              </el-avatar>
              <span class="user-name">管理员</span>
              <el-icon class="el-icon--right"><arrow-down /></el-icon>
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item @click="handleProfile">个人资料</el-dropdown-item>
                <el-dropdown-item @click="handleLogout">退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </div>
      <!-- 内容区域 -->
      <div class="content-wrapper">
        <router-view />
      </div>
      <footer class="admin-site-footer">
        <SiteBeianLinks variant="admin" />
      </footer>
    </div>
  </div>
</template>

<script>
import { ref, computed, onMounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMenu, ElMenuItem, ElButton, ElDropdown, ElDropdownMenu, ElDropdownItem, ElAvatar, ElIcon } from 'element-plus'
import { ArrowDown } from '@element-plus/icons-vue'
import SiteBeianLinks from './SiteBeianLinks.vue'

export default {
  name: 'AdminLayout',
  components: {
    SiteBeianLinks,
    ElMenu,
    ElMenuItem,
    ElButton,
    ElDropdown,
    ElDropdownMenu,
    ElDropdownItem,
    ElAvatar,
    ElIcon,
    ArrowDown
  },
  setup() {
    const route = useRoute()
    const router = useRouter()
    const collapsed = ref(false)
    
    // 计算当前激活的菜单
    const activeMenu = computed(() => {
      const p = route.path
      if (p === '/admin/home-hero') return '/admin/home-hero'
      if (p === '/admin/about') return '/admin/about'
      if (p.startsWith('/admin/hobbies')) return '/admin/hobbies'
      if (p.startsWith('/admin/posts')) return '/admin/posts'
      return p
    })
    
    // 计算页面标题
    const pageTitle = computed(() => {
      const p = route.path
      if (route.name === 'AdminHobbyManage') return '爱好专栏与动态'
      if (route.name === 'AdminCreateHobbyArticle') return '发布专栏文章'
      if (route.name === 'AdminHomeHero') return '首页壁纸与标题'
      if (route.name === 'AdminAbout') return '关于页'
      if (route.name === 'AdminSite') return '站点图标'
      if (route.name === 'AdminMusic') return '音乐管理'
      const titleMap = {
        '/admin': '管理端首页',
        '/admin/home-hero': '首页壁纸与标题',
        '/admin/about': '关于页',
        '/admin/site': '站点图标',
        '/admin/music': '音乐管理',
        '/admin/hobbies': '爱好管理',
        '/admin/notes': '笔记管理',
        '/admin/posts': '文章管理',
        '/admin/comments': '评论管理',
        '/admin/messages': '留言管理',
        '/admin/posts/create': '发布文章'
      }
      if (titleMap[p]) return titleMap[p]
      if (p.startsWith('/admin/posts/edit')) return '编辑文章'
      return '管理端'
    })
    
    // 切换侧边栏
    const toggleSidebar = () => {
      collapsed.value = !collapsed.value
      localStorage.setItem('sidebarCollapsed', collapsed.value)
    }
    
    // 处理菜单选择
    const handleMenuSelect = (index) => {
      if (index === '__site_home__') {
        router.push('/')
        return
      }
      router.push(index)
    }
    
    // 处理个人资料
    const handleProfile = () => {
      ElMessage.info('个人资料功能开发中')
    }
    
    // 处理退出登录
    const handleLogout = () => {
      localStorage.removeItem('adminToken')
      localStorage.removeItem('isAdmin')
      ElMessage.success('退出登录成功')
      router.push('/admin/login')
    }
    
    // 监听路由变化
    watch(() => route.path, (newPath) => {
      // 可以在这里添加路由变化的处理逻辑
    })
    
    onMounted(() => {
      // 从本地存储恢复侧边栏状态
      const savedCollapsed = localStorage.getItem('sidebarCollapsed')
      if (savedCollapsed !== null) {
        collapsed.value = savedCollapsed === 'true'
      }
    })
    
    return {
      collapsed,
      activeMenu,
      pageTitle,
      toggleSidebar,
      handleMenuSelect,
      handleProfile,
      handleLogout
    }
  }
}
</script>

<style scoped>
.admin-layout {
  display: flex;
  height: 100vh;
  overflow: hidden;
  background: #f0f2f5;
}

/* 侧边栏 */
.admin-sidebar {
  width: 240px;
  background: white;
  box-shadow: 0 0 10px rgba(0, 0, 0, 0.05);
  transition: width 0.3s ease;
  display: flex;
  flex-direction: column;
  z-index: 100;
}

.admin-sidebar.sidebar-collapsed {
  width: 80px;
}

.sidebar-header {
  height: 60px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 20px;
  border-bottom: 1px solid #f0f0f0;
}

.logo {
  display: flex;
  align-items: center;
}

.logo-text {
  font-size: 18px;
  font-weight: 600;
  color: #409EFF;
  white-space: nowrap;
}

.admin-sidebar.sidebar-collapsed .logo-text {
  display: none;
}

.collapse-btn {
  font-size: 18px;
  padding: 0;
  margin-left: 10px;
}

.sidebar-menu {
  flex: 1;
  overflow-y: auto;
  padding: 20px 0;
}

.sidebar-menu .el-menu {
  border-right: none;
}

.sidebar-menu .el-menu-item {
  height: 50px;
  line-height: 50px;
  margin: 0 10px;
  border-radius: 8px;
  margin-bottom: 8px;
}

.sidebar-menu .el-menu-item .el-icon {
  font-size: 20px;
  margin-right: 12px;
}

.admin-sidebar.sidebar-collapsed .sidebar-menu .el-menu-item span {
  display: none;
}

.admin-sidebar.sidebar-collapsed .sidebar-menu .el-menu-item .el-icon {
  margin-right: 0;
}

/* 主内容区 */
.admin-content {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

/* 顶部导航栏 */
.admin-header {
  height: 60px;
  background: white;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.05);
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 20px;
  z-index: 99;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 16px;
}

.menu-btn {
  font-size: 20px;
  padding: 0;
}

.page-title {
  font-size: 18px;
  font-weight: 600;
  color: #303133;
  margin: 0;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 16px;
}

.back-site-btn {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 8px 14px;
  border-radius: 8px;
  font-size: 14px;
  color: #606266;
  text-decoration: none;
  border: 1px solid #dcdfe6;
  background: #fff;
  transition: color 0.2s ease, border-color 0.2s ease, background-color 0.2s ease;
}

.back-site-btn:hover {
  color: #409eff;
  border-color: #c6e2ff;
  background: #ecf5ff;
}

.back-site-icon {
  font-size: 16px;
  line-height: 1;
}

.user-dropdown {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  padding: 8px 12px;
  border-radius: 8px;
  transition: background-color 0.3s ease;
}

.user-dropdown:hover {
  background-color: #f0f2f5;
}

.user-name {
  font-size: 14px;
  color: #303133;
  white-space: nowrap;
}

/* 内容区域 */
.content-wrapper {
  flex: 1;
  min-height: 0;
  padding: 24px;
  overflow-y: auto;
  background: #f0f2f5;
}

.admin-site-footer {
  flex-shrink: 0;
  text-align: center;
  padding: 10px 16px 12px;
  font-size: 12px;
  background: #fff;
  border-top: 1px solid #ebeef5;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .admin-sidebar {
    position: fixed;
    left: 0;
    top: 0;
    height: 100vh;
    transform: translateX(-100%);
  }
  
  .admin-sidebar.sidebar-collapsed {
    transform: translateX(0);
    width: 80px;
  }
  
  .admin-content {
    margin-left: 0;
  }
  
  .page-title {
    font-size: 16px;
  }
  
  .content-wrapper {
    padding: 16px;
  }
}

/* 滚动条样式 */
.sidebar-menu::-webkit-scrollbar {
  width: 6px;
}

.sidebar-menu::-webkit-scrollbar-track {
  background: #f1f1f1;
}

.sidebar-menu::-webkit-scrollbar-thumb {
  background: #c1c1c1;
  border-radius: 3px;
}

.sidebar-menu::-webkit-scrollbar-thumb:hover {
  background: #a1a1a1;
}

.content-wrapper::-webkit-scrollbar {
  width: 8px;
}

.content-wrapper::-webkit-scrollbar-track {
  background: #f1f1f1;
}

.content-wrapper::-webkit-scrollbar-thumb {
  background: #c1c1c1;
  border-radius: 4px;
}

.content-wrapper::-webkit-scrollbar-thumb:hover {
  background: #a1a1a1;
}
</style>