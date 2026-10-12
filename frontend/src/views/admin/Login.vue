<template>
  <div class="login-container">
    <div class="bubble-container" v-if="!isDark">
      <canvas ref="bubbleCanvas" class="bubble-canvas"></canvas>
    </div>
    <div class="login-content flex-center">
      <div class="login-main rounded">
        <!-- 登录头部 -->
        <div class="login-header flex-between px-4 mb-1">
          <div class="login-title flex-end">
            <span class="login-title-text">ruyu-blog</span>
            <span class="login-title-desc">欢迎进入个人博客后台</span>
          </div>
          <div class="login-actions flex-center relative z-11">
            <router-link to="/" class="back-btn mr-4">
              <span class="text-lg">🏠</span>
              <span class="ml-1">返回博客</span>
            </router-link>
            <span 
              class="flex-center cursor-pointer text-2xl"
              @click="toggleTheme"
            >
              <span v-if="!isDark">🌙</span>
              <span v-else>☀️</span>
            </span>
          </div>
        </div>
        <el-divider margin="0" />
        <!-- 登录主体 -->
        <div class="login-body flex min-h-[520px]">
          <!-- 登录框左侧 -->
          <div class="login-left min-h-[520px] flex-center" v-if="!isMobile">
            <div class="login-left-content">
              <h3 class="login-left-title">管理端系统</h3>
              <p class="login-left-desc">专业的博客管理平台</p>
              <div class="login-left-features">
                <div class="feature-item">
                  <span class="feature-icon">📝</span>
                  <span class="feature-text">内容管理</span>
                </div>
                <div class="feature-item">
                  <span class="feature-icon">🎨</span>
                  <span class="feature-text">主题定制</span>
                </div>
                <div class="feature-item">
                  <span class="feature-icon">🔒</span>
                  <span class="feature-text">安全防护</span>
                </div>
              </div>
            </div>
          </div>
          <el-divider direction="vertical" margin="0" class="login-divider min-h-[520px]" v-if="!isMobile" />
          <!-- 登录框右侧 -->
          <div class="login-right px-5 w-[335px] flex-center flex-col relative z-11">
            <div class="text-center py-6 text-2xl">
              欢迎登录后台系统
            </div>
            <el-form ref="loginFormRef" :model="loginForm" :rules="rules" label-width="0">
              <!-- 错误提示 -->
              <el-alert 
                v-if="errorAlert" 
                :title="errorMessage" 
                type="error" 
                show-icon 
                class="mb-4"
              />
              <el-form-item prop="username">
                <el-input
                  v-model="loginForm.username"
                  placeholder="请输入用户名"
                  size="large"
                  prefix-icon="User"
                  autocomplete="off"
                  @keyup.enter="handleLogin"
                />
              </el-form-item>
              <el-form-item prop="password">
                <el-input
                  v-model="loginForm.password"
                  type="password"
                  placeholder="请输入密码"
                  size="large"
                  prefix-icon="Lock"
                  autocomplete="current-password"
                  @keyup.enter="handleLogin"
                />
              </el-form-item>
              <el-form-item>
                <el-button 
                  type="primary" 
                  block 
                  :loading="loading" 
                  size="large" 
                  @click="handleLogin"
                  class="login-btn"
                >
                  登录
                </el-button>
              </el-form-item>
              <el-form-item v-if="qqLoginEnabled">
                <el-button
                  block
                  size="large"
                  class="qq-login-btn"
                  @click="handleQqLogin"
                >
                  QQ 登录
                </el-button>
              </el-form-item>
            </el-form>
          </div>
        </div>
      </div>
    </div>
    <footer class="login-icp-footer">
      <SiteBeianLinks variant="login" />
    </footer>
  </div>
</template>

<script>
import { ref, reactive, onMounted, onBeforeUnmount, computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage, ElAlert, ElDivider, ElForm, ElFormItem, ElInput, ElButton } from 'element-plus'
import { getQqLoginConfig, loginApi } from '../../api/common/login'
import SiteBeianLinks from '../../components/SiteBeianLinks.vue'

export default {
  name: 'AdminLogin',
  components: {
    SiteBeianLinks,
    ElAlert,
    ElDivider,
    ElForm,
    ElFormItem,
    ElInput,
    ElButton
  },
  setup() {
    const router = useRouter()
    const route = useRoute()
    const loginFormRef = ref(null)
    const loading = ref(false)
    const errorAlert = ref(false)
    const errorMessage = ref('')
    const bubbleCanvas = ref(null)
    const isDark = ref(localStorage.getItem('theme') === 'dark')
    const isMobile = ref(window.innerWidth < 768)
    const qqLoginEnabled = ref(false)
    
    const loginForm = reactive({
      username: '',
      password: ''
    })
    
    const rules = {
      username: [
        { required: true, message: '请输入用户名', trigger: 'blur' }
      ],
      password: [
        { required: true, message: '请输入密码', trigger: 'blur' }
      ]
    }

    // 切换主题
    const toggleTheme = () => {
      isDark.value = !isDark.value
      localStorage.setItem('theme', isDark.value ? 'dark' : 'light')
      updateTheme()
    }

    // 更新主题
    const updateTheme = () => {
      if (isDark.value) {
        document.documentElement.classList.add('dark-theme')
      } else {
        document.documentElement.classList.remove('dark-theme')
      }
    }

    // 初始化气泡动画
    const initBubbleAnimation = () => {
      if (!bubbleCanvas.value || isDark.value) return
      
      const canvas = bubbleCanvas.value
      const ctx = canvas.getContext('2d')
      canvas.width = window.innerWidth
      canvas.height = window.innerHeight
      
      const bubbles = []
      for (let i = 0; i < 50; i++) {
        bubbles.push({
          x: Math.random() * canvas.width,
          y: Math.random() * canvas.height,
          radius: Math.random() * 5 + 1,
          speed: Math.random() * 0.5 + 0.2,
          color: `rgba(${Math.random() * 100 + 155}, ${Math.random() * 100 + 155}, ${Math.random() * 100 + 155}, ${Math.random() * 0.5 + 0.1})`
        })
      }
      
      const animate = () => {
        if (!ctx) return
        
        ctx.clearRect(0, 0, canvas.width, canvas.height)
        
        bubbles.forEach(bubble => {
          bubble.y -= bubble.speed
          if (bubble.y < -bubble.radius) {
            bubble.y = canvas.height + bubble.radius
            bubble.x = Math.random() * canvas.width
          }
          
          ctx.beginPath()
          ctx.arc(bubble.x, bubble.y, bubble.radius, 0, Math.PI * 2)
          ctx.fillStyle = bubble.color
          ctx.fill()
        })
        
        requestAnimationFrame(animate)
      }
      
      animate()
    }

    // 处理窗口大小变化
    const handleResize = () => {
      isMobile.value = window.innerWidth < 768
      initBubbleAnimation()
    }

    const handleQqLogin = () => {
      window.location.href = '/api/auth/qq'
    }

    const finishLoginWithToken = (token) => {
      localStorage.setItem('adminToken', token)
      localStorage.setItem('isAdmin', 'true')
      const redirect = route.query.redirect
      if (redirect) {
        router.replace(decodeURIComponent(redirect))
      } else {
        router.replace('/admin')
      }
    }

    const handleQqCallback = () => {
      const token = route.query.token
      if (token && route.query.login === 'qq') {
        ElMessage.success('QQ 登录成功')
        finishLoginWithToken(token)
        return true
      }
      const err = route.query.error
      if (err) {
        errorAlert.value = true
        errorMessage.value = route.query.message || 'QQ 登录失败'
        ElMessage.error(errorMessage.value)
      }
      return false
    }

    const loadQqConfig = async () => {
      try {
        const res = await getQqLoginConfig()
        qqLoginEnabled.value = !!res?.enabled
      } catch {
        qqLoginEnabled.value = false
      }
    }

    const handleLogin = async () => {
      if (!loginFormRef.value) return
      
      try {
        await loginFormRef.value.validate()
        loading.value = true
        errorAlert.value = false
        
        // 调用登录API
        const response = await loginApi(loginForm.username, loginForm.password)
        
        ElMessage.success('登录成功')
        loading.value = false
        finishLoginWithToken(response.token)
      } catch (error) {
        console.error('登录失败:', error)
        errorAlert.value = true
        errorMessage.value = error.message || '网络错误，请稍后重试'
        ElMessage.error(errorMessage.value)
        loading.value = false
      }
    }

    onMounted(() => {
      updateTheme()
      initBubbleAnimation()
      window.addEventListener('resize', handleResize)
      if (!handleQqCallback()) {
        loadQqConfig()
      }
    })

    onBeforeUnmount(() => {
      window.removeEventListener('resize', handleResize)
    })

    return {
      loginForm,
      rules,
      loginFormRef,
      loading,
      errorAlert,
      errorMessage,
      bubbleCanvas,
      isDark,
      isMobile,
      qqLoginEnabled,
      handleLogin,
      handleQqLogin,
      toggleTheme
    }
  }
}
</script>

<style scoped>
.login-container {
  display: flex;
  flex-direction: column;
  height: 100vh;
  overflow: hidden;
  transition: background-color 0.3s ease;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  z-index: 9999;
}

.login-container:deep(.dark-theme) {
  background: linear-gradient(135deg, #2c3e50 0%, #4b6cb7 100%);
}

.bubble-container {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  z-index: 1;
}

.bubble-canvas {
  width: 100%;
  height: 100%;
}

.login-content {
  position: relative;
  z-index: 10;
  flex: 1;
  min-height: 0;
  display: flex;
  justify-content: center;
  align-items: center;
  padding: 20px;
}

.login-icp-footer {
  position: relative;
  z-index: 10;
  flex-shrink: 0;
  text-align: center;
  padding: 8px 16px 16px;
  font-size: 13px;
}

.login-main {
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.1);
  border-radius: 8px;
  overflow: hidden;
  background: white;
  transition: all 0.3s ease;
}

.login-container:deep(.dark-theme) .login-main {
  background: #1f2937;
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.3);
}

.login-header {
  height: 60px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 16px;
  border-bottom: 1px solid #f0f0f0;
}

.login-container:deep(.dark-theme) .login-header {
  border-bottom: 1px solid #374151;
}

.login-title {
  display: flex;
  align-items: center;
}

.login-title-text {
  font-size: 20px;
  font-weight: 600;
  color: #303133;
  margin-right: 12px;
}

.login-container:deep(.dark-theme) .login-title-text {
  color: #f3f4f6;
}

.login-title-desc {
  font-size: 14px;
  color: #909399;
}

.login-container:deep(.dark-theme) .login-title-desc {
  color: #9ca3af;
}

.login-actions {
  display: flex;
  align-items: center;
  height: 40px;
  line-height: 40px;
}

.back-btn {
  display: flex;
  align-items: center;
  padding: 6px 12px;
  border-radius: 4px;
  color: #606266;
  text-decoration: none;
  transition: all 0.3s;
  font-size: 14px;
  margin-right: 16px;
}

.back-btn:hover {
  background-color: rgba(255, 255, 255, 0.1);
  color: #409eff;
}

.login-container:deep(.dark-theme) .back-btn {
  color: #c0c4cc;
}

.login-container:deep(.dark-theme) .back-btn:hover {
  background-color: rgba(255, 255, 255, 0.05);
  color: #409eff;
}

.login-body {
  display: flex;
  min-height: 520px;
}

.login-left {
  min-height: 520px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #f9fafb;
  flex: 1;
  min-width: 300px;
}

.login-container:deep(.dark-theme) .login-left {
  background: #111827;
}

.login-left-content {
  padding: 40px;
  text-align: center;
}

.login-left-title {
  font-size: 24px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 16px;
}

.login-container:deep(.dark-theme) .login-left-title {
  color: #f3f4f6;
}

.login-left-desc {
  font-size: 16px;
  color: #606266;
  margin-bottom: 32px;
}

.login-container:deep(.dark-theme) .login-left-desc {
  color: #d1d5db;
}

.login-left-features {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.feature-item {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 12px;
  padding: 12px;
  border-radius: 8px;
  background: white;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
}

.login-container:deep(.dark-theme) .feature-item {
  background: #1f2937;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.2);
}

.feature-icon {
  font-size: 24px;
}

.feature-text {
  font-size: 16px;
  color: #303133;
}

.login-container:deep(.dark-theme) .feature-text {
  color: #f3f4f6;
}

.login-divider {
  min-height: 520px;
}

.login-right {
  width: 335px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 20px;
  background: white;
}

.login-container:deep(.dark-theme) .login-right {
  background: #1f2937;
}

.login-right > div:first-child {
  font-size: 20px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 24px;
}

.login-container:deep(.dark-theme) .login-right > div:first-child {
  color: #f3f4f6;
}

.login-btn {
  width: 100%;
  padding: 12px;
  font-size: 16px;
  margin-top: 24px;
}

.qq-login-btn {
  width: 100%;
  padding: 12px;
  font-size: 16px;
  border-color: #12b7f5;
  color: #12b7f5;
}

.qq-login-btn:hover {
  background: rgba(18, 183, 245, 0.08);
  border-color: #0ea5e0;
  color: #0ea5e0;
}

.el-form-item {
  margin-bottom: 20px;
  width: 100%;
}

.el-input {
  height: 48px;
}

.el-input__wrapper {
  border-radius: 8px;
}

/* 响应式设计 */
@media (max-width: 767px) {
  .login-body {
    flex-direction: column;
  }
  
  .login-left {
    min-height: 200px;
    min-width: 100%;
  }
  
  .login-right {
    width: 100%;
    padding: 24px;
  }
  
  .login-title-desc {
    display: none;
  }
  
  .login-left-content {
    padding: 20px;
  }
  
  .login-left-features {
    flex-direction: row;
    flex-wrap: wrap;
  }
  
  .feature-item {
    flex: 1;
    min-width: 100px;
  }
}

@media (min-width: 768px) and (max-width: 991px) {
  .login-left {
    min-width: 250px;
  }
  
  .login-right {
    width: 300px;
  }
}

/* 动画效果 */
@keyframes fadeIn {
  from {
    opacity: 0;
    transform: translateY(20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.login-main {
  animation: fadeIn 0.5s ease-in-out;
}

/* 工具类 */
.flex-center {
  display: flex;
  align-items: center;
  justify-content: center;
}

.flex-between {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.flex-end {
  display: flex;
  align-items: center;
}

.rounded {
  border-radius: 8px;
}

.px-4 {
  padding-left: 16px;
  padding-right: 16px;
}

.mb-1 {
  margin-bottom: 4px;
}

.py-6 {
  padding-top: 24px;
  padding-bottom: 24px;
}

.text-2xl {
  font-size: 20px;
}

.text-center {
  text-align: center;
}

.min-h-\[520px\] {
  min-height: 520px;
}

.w-\[335px\] {
  width: 335px;
}

.px-5 {
  padding-left: 20px;
  padding-right: 20px;
}

.z-11 {
  z-index: 11;
}

.relative {
  position: relative;
}

.cursor-pointer {
  cursor: pointer;
}

.text-2xl {
  font-size: 20px;
}
</style>