<template>
  <div class="post-detail-container">
    <div v-if="post" class="post-layout">
      <div class="post-main">
        <div class="post-content">
          <div v-if="post.coverImage" class="post-hero">
            <img :src="post.coverImage" :alt="post.title" class="post-hero-image">
          </div>
          <div class="post-header">
            <h1 class="post-title">{{ post.title }}</h1>
            <div class="post-meta">
              <span v-if="post.category" class="meta-item meta-category">{{ post.category }}</span>
              <span class="meta-item">
                <i class="icon-calendar"></i> {{ formatDate(post.createdAt) }}
              </span>
              <span class="meta-item">
                <i class="icon-eye"></i> {{ post.views || 0 }} 阅读
              </span>
              <span class="meta-item">
                <i class="icon-comment"></i> {{ post.comments || 0 }} 评论
              </span>
              <span class="meta-item" v-if="post.tags && post.tags.length">
                <i class="icon-tag"></i> 
                <span v-for="tag in post.tags" :key="tag" class="tag">{{ tag }}</span>
              </span>
            </div>
          </div>
          
          <div class="post-body" v-html="compiledMarkdown"></div>
          
          <div class="post-footer">
            <router-link to="/articles" class="back-link">返回文章</router-link>
          </div>
        </div>
      </div>

      <aside class="comments-sidebar">
        <div class="comments-section">
          <div class="comments-header">
            <h2 class="comments-title">评论区</h2>
            <span class="comments-count">{{ comments.length }} 条</span>
          </div>

          <div v-if="topLevelComments.length" class="comment-list">
            <div v-for="item in visibleComments" :key="item.id" class="comment-item">
              <div class="comment-avatar">{{ getAvatarText(item.nickname) }}</div>
              <div class="comment-main">
                <div class="comment-head">
                  <span class="comment-name" :style="{ color: getUserColor(item.nickname) }">{{ item.nickname || '游客' }}</span>
                  <div class="comment-actions">
                    <span class="comment-date">{{ formatRelativeTime(item.createdAt) }}</span>
                    <el-button
                      text
                      type="primary"
                      size="small"
                      @click="likeComment(item.id)"
                    >
                      点赞 {{ item.likes || 0 }}
                    </el-button>
                    <el-button
                      text
                      type="primary"
                      size="small"
                      @click="setReplyTarget(item)"
                    >
                      回复
                    </el-button>
                    <el-button
                      v-if="isAdmin"
                      text
                      type="danger"
                      size="small"
                      @click="deleteComment(item.id)"
                    >
                      删除
                    </el-button>
                  </div>
                </div>
                <p class="comment-content">{{ item.content }}</p>
                <div v-if="replyMap[item.id] && replyMap[item.id].length" class="reply-list">
                  <div v-for="reply in replyMap[item.id]" :key="reply.id" class="reply-item">
                    <span class="reply-name" :style="{ color: getUserColor(reply.nickname) }">{{ reply.nickname || '游客' }}</span>
                    <span class="reply-text">{{ reply.content }}</span>
                    <div class="reply-meta">
                      <span>{{ formatRelativeTime(reply.createdAt) }}</span>
                      <el-button text type="primary" size="small" @click="likeComment(reply.id)">赞 {{ reply.likes || 0 }}</el-button>
                      <el-button text type="primary" size="small" @click="setReplyTarget(item, reply)">回复</el-button>
                      <el-button v-if="isAdmin" text type="danger" size="small" @click="deleteComment(reply.id)">删除</el-button>
                    </div>
                  </div>
                </div>
              </div>
            </div>
            <el-button
              v-if="topLevelComments.length > previewCount"
              text
              type="primary"
              class="toggle-comments-btn"
              @click="showAllComments = !showAllComments"
            >
              {{ showAllComments ? '收起评论' : `展开全部评论 (${topLevelComments.length})` }}
            </el-button>
          </div>
          <el-empty v-else description="还没有评论，来抢沙发吧" />

          <div class="comment-form">
            <div v-if="replyingToId" class="reply-target">
              回复：{{ replyTargetName }}
              <el-button text type="danger" size="small" @click="clearReplyTarget">取消</el-button>
            </div>
            <el-input
              v-model="commentForm.nickname"
              placeholder="昵称（不填则为游客）"
              maxlength="50"
              clearable
            />
            <el-input
              v-model="commentForm.content"
              type="textarea"
              :rows="4"
              placeholder="写下你的评论..."
              maxlength="500"
              show-word-limit
            />
            <el-button type="primary" :loading="commentSubmitting" @click="submitComment">发表评论</el-button>
          </div>
        </div>
      </aside>
    </div>
    <div v-else-if="loading" class="loading-state">
      <el-skeleton :rows="10" animated />
    </div>
    <div v-else class="error-state">
      <el-empty description="无法加载文章，或文章不存在" />
      <router-link to="/articles" class="back-link">返回文章</router-link>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRoute } from 'vue-router'
import api from '@/api'
import { ElSkeleton, ElEmpty, ElInput, ElButton, ElMessage } from 'element-plus'
import { normalizeImageUrl } from '@/utils/image'
import { renderArticleMarkdown } from '@/utils/markdown'

const route = useRoute()
const post = ref(null)
const loading = ref(true)
const comments = ref([])
const commentSubmitting = ref(false)
const isAdmin = ref(!!localStorage.getItem('adminToken'))
const showAllComments = ref(false)
const previewCount = 3
const replyingToId = ref(null)
const replyTargetName = ref('')
const commentForm = ref({
  nickname: '',
  content: ''
})

const loadPostDetail = async () => {
  const response = await api.getPost(route.params.id)
  const data = response.data || {}
  post.value = {
    ...data,
    coverImage: normalizeImageUrl(data.coverImage || data.coverUrl || data.coverTitle)
  }
}

onMounted(async () => {
  try {
    await loadPostDetail()
    await loadComments()
  } catch (error) {
    console.error('获取文章详情失败:', error)
  } finally {
    loading.value = false
  }
})

const loadComments = async () => {
  try {
    const response = await api.getPostComments(route.params.id)
    comments.value = Array.isArray(response.data) ? response.data : []
  } catch (error) {
    console.error('获取评论失败:', error)
    comments.value = []
  }
}

const submitComment = async () => {
  const content = (commentForm.value.content || '').trim()
  if (!content) {
    ElMessage.warning('请输入评论内容')
    return
  }

  commentSubmitting.value = true
  try {
    await api.addPostComment(route.params.id, {
      nickname: (commentForm.value.nickname || '').trim() || '游客',
      content,
      parentId: replyingToId.value
    })
    ElMessage.success('评论成功')
    commentForm.value.content = ''
    clearReplyTarget()
    await Promise.all([loadComments(), loadPostDetail()])
  } catch (error) {
    console.error('发表评论失败:', error)
    if (error?.response?.status === 429) {
      ElMessage.warning('评论太频繁了，请稍后再试')
    } else {
      ElMessage.error('发表评论失败')
    }
  } finally {
    commentSubmitting.value = false
  }
}

const deleteComment = async (commentId) => {
  try {
    await api.adminDeletePostComment(route.params.id, commentId)
    ElMessage.success('评论已删除')
    await Promise.all([loadComments(), loadPostDetail()])
  } catch (error) {
    console.error('删除评论失败:', error)
    ElMessage.error('删除评论失败')
  }
}

const likeComment = async (commentId) => {
  try {
    await api.likePostComment(route.params.id, commentId)
    await loadComments()
  } catch (error) {
    console.error('点赞失败:', error)
    ElMessage.error('点赞失败')
  }
}

const setReplyTarget = (comment, reply) => {
  replyingToId.value = comment.id
  replyTargetName.value = (reply?.nickname || comment.nickname || '游客')
}

const clearReplyTarget = () => {
  replyingToId.value = null
  replyTargetName.value = ''
}

const compiledMarkdown = computed(() => {
  if (post.value && post.value.content) {
    return renderArticleMarkdown(post.value.content)
  }
  return ''
})

const topLevelComments = computed(() => comments.value.filter(item => !item.parentId))

const replyMap = computed(() => {
  const map = {}
  comments.value
    .filter(item => item.parentId)
    .forEach(item => {
      if (!map[item.parentId]) map[item.parentId] = []
      map[item.parentId].push(item)
    })
  return map
})

const visibleComments = computed(() => {
  if (showAllComments.value) {
    return topLevelComments.value
  }
  return topLevelComments.value.slice(0, previewCount)
})

const getAvatarText = (nickname) => {
  const name = (nickname || '游').trim()
  return name.charAt(0).toUpperCase()
}

const getUserColor = (nickname) => {
  const colors = ['#2563eb', '#db2777', '#7c3aed', '#0d9488', '#ea580c', '#16a34a']
  const str = (nickname || '游客').trim()
  let hash = 0
  for (let i = 0; i < str.length; i += 1) {
    hash = (hash * 31 + str.charCodeAt(i)) >>> 0
  }
  return colors[hash % colors.length]
}

const formatRelativeTime = (value) => {
  if (!value) return ''
  const date = new Date(value)
  if (Number.isNaN(date.getTime())) return String(value)
  const diff = Date.now() - date.getTime()
  const minute = 60 * 1000
  const hour = 60 * minute
  const day = 24 * hour
  if (diff < minute) return '刚刚'
  if (diff < hour) return `${Math.floor(diff / minute)}分钟前`
  if (diff < day) return `${Math.floor(diff / hour)}小时前`
  if (diff < 7 * day) return `${Math.floor(diff / day)}天前`
  return formatDate(value)
}

function formatDate(value) {
  if (!value) return ''
  if (Array.isArray(value)) {
    const [y, m, d, hh = 0, mm = 0, ss = 0] = value
    return `${y}-${String(m).padStart(2, '0')}-${String(d).padStart(2, '0')} ${String(hh).padStart(2, '0')}:${String(mm).padStart(2, '0')}`
  }
  try {
    const date = new Date(value)
    return date.toLocaleString()
  } catch (e) {
    return String(value)
  }
}
</script>

<style scoped>
.post-detail-container {
  max-width: 1280px;
  margin: 40px auto;
  padding: 24px;
}

.post-layout {
  display: grid;
  grid-template-columns: minmax(0, 1fr) 360px;
  gap: 20px;
  align-items: start;
}

.post-main {
  min-width: 0;
}

.post-content {
  background-color: #ffffff;
  border-radius: 16px;
  border: 1px solid #e8edf5;
  color: #1f2937;
  padding: 40px;
}

.post-hero {
  margin: -40px -40px 30px;
  border-radius: 16px 16px 0 0;
  overflow: hidden;
  background-color: #eaf4ff;
}

.post-hero-image {
  width: 100%;
  height: 320px;
  object-fit: contain;
  object-position: center;
  display: block;
}

.post-header {
  margin-bottom: 40px;
  text-align: center;
  border-bottom: 1px solid #e8edf5;
  padding-bottom: 30px;
}

.post-title {
  font-size: 36px;
  font-weight: 700;
  margin-bottom: 20px;
  color: #1f2937;
}

.post-meta {
  display: flex;
  justify-content: center;
  flex-wrap: wrap;
  gap: 20px;
  color: #6b7280;
  font-size: 14px;
}

.meta-item {
  display: flex;
  align-items: center;
  gap: 5px;
}

.meta-category {
  background: #eef4ff;
  color: #2563eb;
  padding: 4px 12px;
  border-radius: 999px;
  font-weight: 600;
}

.tag {
  background-color: #eef4ff;
  padding: 2px 8px;
  border-radius: 4px;
  margin-left: 5px;
  font-size: 12px;
  color: #2563eb;
}

.post-body {
  line-height: 1.8;
  font-size: 17px;
  color: #374151;
}

.post-body :deep(h2) {
  font-size: 26px;
  margin-top: 40px;
  margin-bottom: 20px;
  color: #a0c4ff;
  border-bottom: 1px solid rgba(160, 196, 255, 0.3);
  padding-bottom: 10px;
}

.post-body :deep(h3) {
  font-size: 22px;
  margin-top: 30px;
  margin-bottom: 15px;
  color: #bde0fe;
}

.post-body :deep(p) {
  margin-bottom: 20px;
}

.post-body :deep(img) {
  max-width: 100%;
  height: auto;
  display: block;
  border-radius: 8px;
  margin: 20px auto;
  box-shadow: 0 5px 15px rgba(0, 0, 0, 0.3);
}

.post-body :deep(code) {
  background-color: rgba(255, 255, 255, 0.1);
  padding: 2px 6px;
  border-radius: 4px;
  font-family: 'Fira Code', 'Courier New', Courier, monospace;
  color: #ffadad;
}

.post-body :deep(pre) {
  background-color: #111827;
  padding: 20px;
  border-radius: 12px;
  overflow-x: auto;
  margin: 25px 0;
  border: 1px solid #1f2937;
}

.post-body :deep(pre code) {
  background-color: transparent;
  padding: 0;
  color: #e5e7eb;
}

.post-footer {
  margin-top: 60px;
  text-align: center;
}

.comments-section {
  background: #ffffff;
  border: 1px solid #e8edf5;
  border-radius: 16px;
  padding: 16px;
  display: flex;
  flex-direction: column;
  gap: 12px;
  max-height: calc(100vh - 120px);
}

.comments-sidebar {
  position: sticky;
  top: 80px;
}

.comments-title {
  font-size: 20px;
  margin: 0;
  color: #1f2937;
}

.comments-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 4px 4px 10px;
  border-bottom: 1px solid #eef2f7;
}

.comments-count {
  font-size: 13px;
  color: #6b7280;
}

.comment-form {
  display: flex;
  flex-direction: column;
  gap: 10px;
  padding-top: 10px;
  border-top: 1px solid #eef2f7;
  background: #fff;
}

.comment-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
  overflow-y: auto;
  padding-right: 4px;
}

.toggle-comments-btn {
  align-self: flex-start;
  margin-top: 6px;
}

.comment-item {
  background-color: #f8fbff;
  border: 1px solid #e8edf5;
  border-radius: 14px;
  padding: 12px;
  display: flex;
  gap: 10px;
}

.comment-head {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 6px;
}

.comment-actions {
  display: flex;
  align-items: center;
  gap: 8px;
}

.comment-avatar {
  width: 34px;
  height: 34px;
  flex: 0 0 34px;
  border-radius: 50%;
  background: linear-gradient(135deg, #60a5fa, #2563eb);
  color: #fff;
  font-size: 14px;
  font-weight: 700;
  display: flex;
  align-items: center;
  justify-content: center;
}

.comment-main {
  min-width: 0;
  width: 100%;
}

.comment-name {
  font-weight: 600;
}

.comment-date {
  font-size: 12px;
  color: #6b7280;
}

.comment-content {
  margin: 0;
  color: #374151;
  line-height: 1.7;
}

.reply-list {
  margin-top: 8px;
  padding: 8px;
  background: #ffffff;
  border: 1px solid #e5edf8;
  border-radius: 10px;
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.reply-item {
  padding: 8px;
  border-radius: 8px;
  background: #f9fbff;
}

.reply-name {
  font-weight: 600;
  margin-right: 6px;
}

.reply-text {
  color: #374151;
}

.reply-meta {
  margin-top: 4px;
  display: flex;
  align-items: center;
  gap: 8px;
  color: #6b7280;
  font-size: 12px;
}

.reply-target {
  padding: 6px 10px;
  border-radius: 8px;
  background: #eff6ff;
  color: #1d4ed8;
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.back-link {
  display: inline-block;
  padding: 10px 25px;
  background-color: rgba(160, 196, 255, 0.2);
  color: #a0c4ff;
  text-decoration: none;
  border-radius: 30px;
  font-weight: 600;
  transition: all 0.3s ease;
  border: 1px solid rgba(160, 196, 255, 0.3);
}

.back-link:hover {
  background-color: #a0c4ff;
  color: #000;
  transform: translateY(-2px);
  box-shadow: 0 5px 15px rgba(160, 196, 255, 0.4);
}

.loading-state {
  padding: 60px 0;
}

.error-state {
  text-align: center;
  padding: 60px 0;
}

@media (max-width: 1024px) {
  .post-layout {
    grid-template-columns: 1fr;
  }

  .comments-sidebar {
    position: static;
  }
}
</style>
