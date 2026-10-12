// API基础配置
const BASE_URL = '/api'

// 请求拦截器
const requestInterceptor = (config) => {
  const token = localStorage.getItem('adminToken')
  // 如果config.token为false，则不携带token
  if (token && config.token !== false) {
    config.headers = config.headers || {}
    config.headers['Authorization'] = `Bearer ${token}`
  }
  return config
}

// 响应拦截器
const responseInterceptor = (response) => {
  // 检查响应结构
  if (response.hasOwnProperty('success')) {
    // 后端返回的结构：{success: boolean, token: string, user: object, message: string}
    if (response.success) {
      return response
    } else {
      return Promise.reject(new Error(response.message || '请求失败'))
    }
  } else if (response.hasOwnProperty('code')) {
    // 标准结构：{code: number, data: any, message: string}
    if (response.code === 401) {
      // 授权过期，跳转到登录页
      localStorage.removeItem('adminToken')
      localStorage.removeItem('isAdmin')
      window.location.href = '/admin/login'
      return Promise.reject(new Error('授权已过期'))
    }
    if (response.code !== 200) {
      return Promise.reject(new Error(response.message || '请求失败'))
    }
    return response
  }
  // 其他结构，直接返回
  return response
}

// 错误拦截器
const errorInterceptor = (error) => {
  console.error('请求错误:', error)
  return Promise.reject(error)
}

// 通用请求方法
export function request(config) {
  const defaultConfig = {
    method: 'GET',
    headers: {
      'Content-Type': 'application/json'
    }
  }
  
  const finalConfig = {
    ...defaultConfig,
    ...config
  }
  
  // 请求拦截
  const interceptedConfig = requestInterceptor(finalConfig)
  
  // 只传递 fetch API 支持的属性
  const fetchConfig = {
    method: interceptedConfig.method,
    headers: interceptedConfig.headers,
    body: interceptedConfig.body
  }
  
  return fetch(`${BASE_URL}${interceptedConfig.url}`, fetchConfig)
    .then(async response => {
      const data = await response.json()
      if (!response.ok) {
        // 如果是401错误，执行授权过期逻辑
        if (response.status === 401) {
          localStorage.removeItem('adminToken')
          localStorage.removeItem('isAdmin')
          window.location.href = '/admin/login'
          throw new Error(data.message || '授权已过期')
        }
        throw new Error(data.message || `HTTP error! status: ${response.status}`)
      }
      return data
    })
    .then(responseInterceptor)
    .catch(errorInterceptor)
}

// GET请求
export function get(url, config = {}) {
  return request({
    url,
    method: 'GET',
    ...config
  })
}

// POST请求
export function post(url, data, config = {}) {
  return request({
    url,
    method: 'POST',
    body: JSON.stringify(data),
    ...config
  })
}

// PUT请求
export function put(url, data, config = {}) {
  return request({
    url,
    method: 'PUT',
    body: JSON.stringify(data),
    ...config
  })
}

// DELETE请求
export function del(url, config = {}) {
  return request({
    url,
    method: 'DELETE',
    ...config
  })
}