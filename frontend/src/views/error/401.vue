<template>
  <div class="error-container">
    <div class="error-content">
      <div class="error-icon">🔑</div>
      <h1 class="error-title">401</h1>
      <h2 class="error-subtitle">授权已过期</h2>
      <p class="error-description">您的登录状态已过期，请重新登录</p>
      <div class="error-actions">
        <el-button type="primary" @click="goToLogin">重新登录</el-button>
        <el-button @click="goToHome">返回首页</el-button>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: 'Error401',
  methods: {
    goToLogin() {
      this.$router.push('/admin/login')
    },
    goToHome() {
      this.$router.push('/')
    }
  }
}
</script>

<style scoped>
.error-container {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 100vh;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.error-content {
  text-align: center;
  background: white;
  padding: 60px 40px;
  border-radius: 12px;
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.1);
  max-width: 500px;
  animation: fadeIn 0.5s ease-in-out;
}

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

.error-icon {
  font-size: 80px;
  margin-bottom: 20px;
}

.error-title {
  font-size: 72px;
  font-weight: bold;
  color: #303133;
  margin-bottom: 10px;
}

.error-subtitle {
  font-size: 24px;
  color: #606266;
  margin-bottom: 20px;
}

.error-description {
  font-size: 16px;
  color: #909399;
  margin-bottom: 40px;
}

.error-actions {
  display: flex;
  gap: 15px;
  justify-content: center;
}

.error-actions .el-button {
  padding: 12px 30px;
  font-size: 16px;
}
</style>