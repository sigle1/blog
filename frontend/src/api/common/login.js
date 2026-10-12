import { get, post } from '../common'

// 登录参数
export function loginParams(username, password) {
  return {
    username,
    password,
    type: 'account'
  }
}

/** QQ 登录是否已配置 */
export function getQqLoginConfig() {
  return get('/auth/qq/config', { token: false })
}

// 登录API
export function loginApi(username, password) {
  return post('/auth/login', loginParams(username, password), {
    // 登录时不携带token
    token: false,
    // 开启全局loading
    loading: true
  })
}

// 退出登录API
export function logoutApi() {
  return post('/auth/logout', {}, {
    // 退出登录时携带token
    token: true
  })
}