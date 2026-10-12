import { reactive, computed } from 'vue'

// 用户状态
const state = reactive({
  token: localStorage.getItem('adminToken') || '',
  isAdmin: localStorage.getItem('isAdmin') === 'true',
  userInfo: null
})

// 计算属性
const isLoggedIn = computed(() => !!state.token)
const isAdminUser = computed(() => state.isAdmin)

// 设置token
function setToken(token) {
  state.token = token
  localStorage.setItem('adminToken', token)
}

// 设置管理员状态
function setAdminStatus(isAdmin) {
  state.isAdmin = isAdmin
  localStorage.setItem('isAdmin', isAdmin ? 'true' : 'false')
}

// 设置用户信息
function setUserInfo(userInfo) {
  state.userInfo = userInfo
}

// 清除用户状态
function clearUserState() {
  state.token = ''
  state.isAdmin = false
  state.userInfo = null
  localStorage.removeItem('adminToken')
  localStorage.removeItem('isAdmin')
}

// 导出用户状态和方法
export function useUserStore() {
  return {
    state,
    isLoggedIn,
    isAdminUser,
    setToken,
    setAdminStatus,
    setUserInfo,
    clearUserState
  }
}