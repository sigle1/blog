import axios from 'axios'

const apiClient = axios.create({
  baseURL: '/api',
  timeout: 20000,
  headers: {
    'Content-Type': 'application/json'
  }
})

// 添加请求拦截器
apiClient.interceptors.request.use(
  (config) => {
    console.log('发送请求:', config)
    const token = localStorage.getItem('adminToken')
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  (error) => {
    return Promise.reject(error)
  }
)

// 添加响应拦截器
apiClient.interceptors.response.use(
  (response) => {
    console.log('收到响应:', response)
    return response
  },
  (error) => {
    console.error('响应错误:', error)
    if (error.response && error.response.status === 401) {
      // 授权过期，清除本地存储并跳转到登录页
      localStorage.removeItem('adminToken')
      localStorage.removeItem('isAdmin')
      window.location.href = '/admin/login'
    }
    return Promise.reject(error)
  }
)

export default {
  getPosts() {
    return apiClient.get('/posts')
  },
  getPost(id) {
    return apiClient.get(`/posts/${id}`)
  },
  getPostComments(postId) {
    return apiClient.get(`/posts/${postId}/comments`)
  },
  addPostComment(postId, comment) {
    return apiClient.post(`/posts/${postId}/comments`, comment)
  },
  likePostComment(postId, commentId) {
    return apiClient.post(`/posts/${postId}/comments/${commentId}/like`)
  },
  adminDeletePostComment(postId, commentId) {
    return apiClient.delete(`/admin/posts/${postId}/comments/${commentId}`)
  },
  adminGetComments(params = {}) {
    return apiClient.get('/admin/comments', { params })
  },
  getMessages() {
    return apiClient.get('/messages')
  },
  getMessage(id) {
    return apiClient.get(`/messages/${id}`)
  },
  likeMessage(id) {
    return apiClient.post(`/messages/${id}/like`)
  },
  favoriteMessage(id) {
    return apiClient.post(`/messages/${id}/favorite`)
  },
  getMessageComments(messageId) {
    return apiClient.get(`/messages/${messageId}/comments`)
  },
  addMessageComment(messageId, comment) {
    return apiClient.post(`/messages/${messageId}/comments`, comment)
  },
  likeMessageComment(messageId, commentId) {
    return apiClient.post(`/messages/${messageId}/comments/${commentId}/like`)
  },
  createMessage(message) {
    return apiClient.post('/messages', message)
  },
  adminGetMessages(params = {}) {
    return apiClient.get('/admin/messages', { params })
  },
  adminDeleteMessage(id) {
    return apiClient.delete(`/admin/messages/${id}`)
  },
  getHobbies() {
    return apiClient.get('/hobbies')
  },
  createHobby(hobby) {
    return apiClient.post('/hobbies', hobby)
  },
  getHobby(id) {
    return apiClient.get(`/hobbies/${id}`)
  },
  addContentToHobby(id, content) {
    return apiClient.post(`/hobbies/${id}/contents`, content)
  },
  addArticleToHobby(hobbyId, article) {
    return apiClient.post(`/hobbies/${hobbyId}/articles`, article)
  },
  getArticles(hobbyId) {
    return apiClient.get(`/hobbies/${hobbyId}/articles`)
  },
  getArticleForHobby(hobbyId, articleId) {
    return apiClient.get(`/hobbies/${hobbyId}/articles/${articleId}`)
  },
  getNotes() {
    return apiClient.get('/notes')
  },
  getNote(id) {
    return apiClient.get(`/notes/${id}`)
  },
  createNote(note) {
    return apiClient.post('/notes', note)
  },
  updateNote(id, note) {
    return apiClient.put(`/notes/${id}`, note)
  },
  deleteNote(id) {
    return apiClient.delete(`/notes/${id}`)
  },
  createPost(post) {
    return apiClient.post('/posts', post)
  },
  updatePost(id, post) {
    return apiClient.put(`/posts/${id}`, post)
  },
  deletePost(id) {
    return apiClient.delete(`/posts/${id}`)
  },
  updateHobby(id, hobby) {
    return apiClient.put(`/hobbies/${id}`, hobby)
  },
  deleteHobby(id) {
    return apiClient.delete(`/hobbies/${id}`)
  },
  getHomeHero() {
    return apiClient.get('/home/hero')
  },
  getHomeHeroAdmin() {
    return apiClient.get('/admin/home/hero')
  },
  updateHomeHero(config) {
    return apiClient.put('/admin/home/hero', config)
  },
  getAboutContent() {
    return apiClient.get('/about/content')
  },
  getAboutContentAdmin() {
    return apiClient.get('/admin/about/content')
  },
  updateAboutContent(body) {
    return apiClient.put('/admin/about/content', body)
  },
  getSiteSettings() {
    return apiClient.get('/site/settings')
  },
  getSiteSettingsAdmin() {
    return apiClient.get('/admin/site/settings')
  },
  updateSiteSettings(body) {
    return apiClient.put('/admin/site/settings', body)
  },
  getMusicTracks() {
    return apiClient.get('/music/tracks')
  },
  getMusicTracksAdmin() {
    return apiClient.get('/admin/music/tracks')
  },
  uploadMusicTrack(formData) {
    return apiClient.post('/admin/music/tracks', formData, {
      headers: { 'Content-Type': 'multipart/form-data' }
    })
  },
  updateMusicTrack(id, body) {
    return apiClient.put(`/admin/music/tracks/${id}`, body)
  },
  reorderMusicTracks(ids) {
    return apiClient.put('/admin/music/tracks/order', ids)
  },
  deleteMusicTrack(id) {
    return apiClient.delete(`/admin/music/tracks/${id}`)
  }
  // 在这里可以添加更多后端的API调用
}
