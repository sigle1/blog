<template>
  <div class="message-detail-page">
    <div class="container">
      <div class="content-wrapper">
        <main class="main-content" v-loading="loading">
          <template v-if="message">
            <article class="message-card">
              <header class="message-header">
                <div class="message-avatar" :style="{ backgroundColor: getUserColor(message.nickname) }">
                  {{ getAvatarText(message.nickname) }}
                </div>
                <div class="message-meta">
                  <span class="message-author">{{ message.nickname || '游客' }}</span>
                  <time class="message-time">{{ formatDateTime(message.createdAt) }}</time>
                </div>
              </header>
              <div class="message-body">{{ message.content }}</div>
              <div class="message-stats">
                <button type="button" class="stat-btn" @click="scrollToComments">
                  <span class="stat-icon">💬</span>
                  <span>{{ commentCount }}</span>
                </button>
                <button
                  type="button"
                  class="stat-btn"
                  :class="{ active: likedLocal }"
                  :disabled="likedLocal"
                  @click="likeMessage"
                >
                  <span class="stat-icon">👍</span>
                  <span>{{ message.likes || 0 }}</span>
                </button>
                <button
                  type="button"
                  class="stat-btn"
                  :class="{ active: favoritedLocal }"
                  :disabled="favoritedLocal"
                  @click="favoriteMessage"
                >
                  <span class="stat-icon">⭐</span>
                  <span>{{ message.favorites || 0 }}</span>
                </button>
              </div>
            </article>

            <section id="comments" class="comments-section">
              <h2 class="comments-title">
                <span class="comments-title-icon">💬</span>
                评论({{ commentCount }})
              </h2>

              <div v-if="topLevelComments.length" class="comment-list">
                <div v-for="item in topLevelComments" :key="item.id" class="comment-item">
                  <div class="comment-avatar" :style="{ backgroundColor: getUserColor(item.nickname) }">
                    {{ getAvatarText(item.nickname) }}
                  </div>
                  <div class="comment-main">
                    <div class="comment-head">
                      <span class="comment-name" :style="{ color: getUserColor(item.nickname) }">
                        {{ item.nickname || '游客' }}
                      </span>
                      <span class="comment-date">{{ formatDateTime(item.createdAt) }}</span>
                    </div>
                    <p class="comment-text">{{ item.content }}</p>
                    <div class="comment-actions">
                      <button type="button" class="link-btn" @click="likeComment(item.id)">
                        赞 {{ item.likes || 0 }}
                      </button>
                      <button type="button" class="link-btn" @click="setReplyTarget(item)">回复</button>
                    </div>
                    <div v-if="replyMap[item.id]?.length" class="reply-list">
                      <div v-for="reply in replyMap[item.id]" :key="reply.id" class="reply-item">
                        <span class="reply-name" :style="{ color: getUserColor(reply.nickname) }">
                          {{ reply.nickname || '游客' }}
                        </span>
                        <span class="reply-text">：{{ reply.content }}</span>
                        <div class="reply-meta">
                          <span>{{ formatDateTime(reply.createdAt) }}</span>
                          <button type="button" class="link-btn" @click="likeComment(reply.id)">
                            赞 {{ reply.likes || 0 }}
                          </button>
                          <button type="button" class="link-btn" @click="setReplyTarget(item, reply)">回复</button>
                        </div>
                      </div>
                    </div>
                  </div>
                </div>
              </div>

              <div class="comment-compose">
                <div v-if="replyingToId" class="reply-hint">
                  回复 {{ replyTargetName }}
                  <button type="button" class="link-btn" @click="clearReplyTarget">取消</button>
                </div>
                <textarea
                  ref="commentTextareaRef"
                  v-model="commentForm.content"
                  class="comment-textarea"
                  rows="5"
                  maxlength="500"
                  placeholder="留下你的精彩评论吧!"
                />
                <div class="compose-toolbar">
                  <div class="emoji-wrap">
                    <button type="button" class="emoji-btn" title="表情" @click="showEmoji = !showEmoji">😊</button>
                    <div v-if="showEmoji" class="emoji-panel">
                      <button
                        v-for="e in emojis"
                        :key="e"
                        type="button"
                        class="emoji-item"
                        @click="insertEmoji(e)"
                      >{{ e }}</button>
                    </div>
                  </div>
                  <div class="compose-actions">
                    <button type="button" class="btn-preview" @click="previewOpen = true">预览</button>
                    <button
                      type="button"
                      class="btn-publish"
                      :disabled="commentSubmitting"
                      @click="submitComment"
                    >发布</button>
                  </div>
                </div>
              </div>
            </section>

            <router-link to="/messages" class="back-list">← 返回留言列表</router-link>
          </template>
          <el-empty v-else-if="!loading" description="留言不存在" />
        </main>
        <BlogSidebar />
      </div>
    </div>

    <el-dialog v-model="previewOpen" title="评论预览" width="520px">
      <p class="preview-body">{{ previewContent }}</p>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage, ElEmpty, ElDialog } from 'element-plus'
import api from '@/api'
import BlogSidebar from '@/components/BlogSidebar.vue'

const route = useRoute()
const loading = ref(true)
const message = ref(null)
const comments = ref([])
const commentSubmitting = ref(false)
const previewOpen = ref(false)
const showEmoji = ref(false)
const replyingToId = ref(null)
const replyTargetName = ref('')
const commentTextareaRef = ref(null)
const likedLocal = ref(false)
const favoritedLocal = ref(false)

const commentForm = ref({
  nickname: localStorage.getItem('guestNickname') || '',
  content: ''
})

const emojis = ['😀', '😊', '👍', '❤️', '🎉', '🌸', '✨', '🙏']

const commentCount = computed(() => {
  if (message.value?.commentCount != null) return message.value.commentCount
  return comments.value.length
})

const topLevelComments = computed(() => comments.value.filter((c) => !c.parentId))

const replyMap = computed(() => {
  const map = {}
  comments.value
    .filter((c) => c.parentId)
    .forEach((c) => {
      if (!map[c.parentId]) map[c.parentId] = []
      map[c.parentId].push(c)
    })
  return map
})

const previewContent = computed(() => (commentForm.value.content || '').trim() || '（暂无内容）')

function getAvatarText(nickname) {
  const name = (nickname || '游').trim()
  return name.charAt(0).toUpperCase()
}

function getUserColor(nickname) {
  const colors = ['#f59e0b', '#2563eb', '#db2777', '#7c3aed', '#0d9488', '#ea580c']
  const str = (nickname || '游客').trim()
  let hash = 0
  for (let i = 0; i < str.length; i += 1) {
    hash = (hash * 31 + str.charCodeAt(i)) >>> 0
  }
  return colors[hash % colors.length]
}

function formatDateTime(value) {
  if (!value) return ''
  try {
    const d = new Date(value)
    if (Number.isNaN(d.getTime())) return String(value)
    const y = d.getFullYear()
    const m = String(d.getMonth() + 1).padStart(2, '0')
    const day = String(d.getDate()).padStart(2, '0')
    const h = String(d.getHours()).padStart(2, '0')
    const min = String(d.getMinutes()).padStart(2, '0')
    const s = String(d.getSeconds()).padStart(2, '0')
    return `${y}-${m}-${day} ${h}:${min}:${s}`
  } catch {
    return String(value)
  }
}

function scrollToComments() {
  document.getElementById('comments')?.scrollIntoView({ behavior: 'smooth' })
}

function insertEmoji(emoji) {
  commentForm.value.content += emoji
  showEmoji.value = false
}

function setReplyTarget(comment, reply) {
  replyingToId.value = comment.id
  replyTargetName.value = reply?.nickname || comment.nickname || '游客'
}

function clearReplyTarget() {
  replyingToId.value = null
  replyTargetName.value = ''
}

async function loadMessage() {
  const res = await api.getMessage(route.params.id)
  message.value = res.data
  likedLocal.value = localStorage.getItem(`msg-liked-${route.params.id}`) === '1'
  favoritedLocal.value = localStorage.getItem(`msg-fav-${route.params.id}`) === '1'
}

async function loadComments() {
  const res = await api.getMessageComments(route.params.id)
  comments.value = Array.isArray(res.data) ? res.data : []
}

async function likeMessage() {
  if (likedLocal.value) return
  try {
    await api.likeMessage(route.params.id)
    localStorage.setItem(`msg-liked-${route.params.id}`, '1')
    likedLocal.value = true
    await loadMessage()
  } catch {
    ElMessage.error('点赞失败')
  }
}

async function favoriteMessage() {
  if (favoritedLocal.value) return
  try {
    await api.favoriteMessage(route.params.id)
    localStorage.setItem(`msg-fav-${route.params.id}`, '1')
    favoritedLocal.value = true
    await loadMessage()
  } catch {
    ElMessage.error('收藏失败')
  }
}

async function likeComment(commentId) {
  try {
    await api.likeMessageComment(route.params.id, commentId)
    await loadComments()
    await loadMessage()
  } catch {
    ElMessage.error('点赞失败')
  }
}

async function submitComment() {
  const content = (commentForm.value.content || '').trim()
  if (!content) {
    ElMessage.warning('请输入评论内容')
    return
  }
  const nickname = (commentForm.value.nickname || '').trim() || '游客'
  localStorage.setItem('guestNickname', nickname)
  commentSubmitting.value = true
  try {
    await api.addMessageComment(route.params.id, {
      nickname,
      content,
      parentId: replyingToId.value
    })
    ElMessage.success('评论成功')
    commentForm.value.content = ''
    clearReplyTarget()
    await Promise.all([loadComments(), loadMessage()])
  } catch (error) {
    if (error?.response?.status === 429) {
      ElMessage.warning('评论太频繁，请稍后再试')
    } else {
      ElMessage.error('评论失败')
    }
  } finally {
    commentSubmitting.value = false
  }
}

onMounted(async () => {
  try {
    await Promise.all([loadMessage(), loadComments()])
  } catch {
    message.value = null
  } finally {
    loading.value = false
  }
})
</script>

<style scoped>
.message-detail-page {
  padding: 24px 0 48px;
  min-height: calc(100vh - 60px);
}

.container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 20px;
}

.content-wrapper {
  display: flex;
  gap: 24px;
  align-items: flex-start;
}

.main-content {
  flex: 1;
  min-width: 0;
}

.message-card {
  background: var(--card-bg, #fff);
  border-radius: 12px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06);
  padding: 24px 28px;
  margin-bottom: 20px;
}

.message-header {
  display: flex;
  align-items: center;
  gap: 14px;
  margin-bottom: 16px;
}

.message-avatar {
  width: 48px;
  height: 48px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  font-weight: 700;
  font-size: 18px;
  flex-shrink: 0;
}

.message-author {
  display: block;
  font-size: 16px;
  font-weight: 600;
  color: var(--text-color);
}

.message-time {
  font-size: 13px;
  color: #909399;
}

.message-body {
  font-size: 15px;
  line-height: 1.8;
  color: var(--text-color);
  white-space: pre-wrap;
  word-break: break-word;
  margin-bottom: 20px;
}

.message-stats {
  display: flex;
  gap: 24px;
  padding-top: 16px;
  border-top: 1px solid var(--border-color, #eee);
}

.stat-btn {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  border: none;
  background: transparent;
  color: #606266;
  font-size: 14px;
  cursor: pointer;
  padding: 4px 8px;
  border-radius: 6px;
  transition: color 0.2s, background 0.2s;
}

.stat-btn:hover:not(:disabled) {
  color: #409eff;
  background: #ecf5ff;
}

.stat-btn.active {
  color: #e6a23c;
}

.stat-btn:disabled {
  cursor: default;
  opacity: 0.85;
}

.stat-icon {
  font-size: 18px;
}

.comments-section {
  background: var(--card-bg, #fff);
  border-radius: 12px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06);
  padding: 20px 28px 28px;
}

.comments-title {
  display: flex;
  align-items: center;
  gap: 8px;
  margin: 0 0 20px;
  font-size: 18px;
  font-weight: 600;
}

.comments-title-icon {
  font-size: 20px;
}

.comment-list {
  margin-bottom: 24px;
}

.comment-item {
  display: flex;
  gap: 12px;
  padding: 16px 0;
  border-bottom: 1px solid var(--border-color, #f0f0f0);
}

.comment-item:last-child {
  border-bottom: none;
}

.comment-avatar {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  flex-shrink: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  font-weight: 600;
  font-size: 15px;
}

.comment-name {
  font-weight: 600;
  margin-right: 10px;
}

.comment-date {
  font-size: 12px;
  color: #909399;
}

.comment-text {
  margin: 8px 0;
  line-height: 1.7;
  color: var(--text-color);
  white-space: pre-wrap;
}

.comment-actions,
.reply-meta {
  display: flex;
  align-items: center;
  gap: 12px;
  font-size: 13px;
}

.link-btn {
  border: none;
  background: none;
  color: #409eff;
  cursor: pointer;
  padding: 0;
  font-size: 13px;
}

.link-btn:hover {
  text-decoration: underline;
}

.reply-list {
  margin-top: 10px;
  padding: 10px 12px;
  background: #f5f7fa;
  border-radius: 8px;
  font-size: 13px;
}

:global(.dark-theme) .reply-list {
  background: #2d2d2d;
}

.reply-item + .reply-item {
  margin-top: 8px;
}

.reply-name {
  font-weight: 600;
}

.comment-compose {
  margin-top: 8px;
}

.reply-hint {
  font-size: 13px;
  color: #606266;
  margin-bottom: 8px;
}

.comment-textarea {
  width: 100%;
  box-sizing: border-box;
  border: 1px solid #dcdfe6;
  border-radius: 8px;
  padding: 12px 14px;
  font-size: 14px;
  line-height: 1.6;
  resize: vertical;
  font-family: inherit;
  color: var(--text-color);
  background: var(--card-bg, #fff);
}

.comment-textarea:focus {
  outline: none;
  border-color: #409eff;
}

.compose-toolbar {
  display: flex;
  justify-content: space-between;
  align-items: flex-end;
  margin-top: 10px;
}

.emoji-wrap {
  position: relative;
}

.emoji-btn {
  border: none;
  background: none;
  font-size: 22px;
  cursor: pointer;
  padding: 4px;
}

.emoji-panel {
  position: absolute;
  bottom: 100%;
  left: 0;
  margin-bottom: 6px;
  display: flex;
  flex-wrap: wrap;
  gap: 4px;
  padding: 8px;
  background: #fff;
  border: 1px solid #e4e7ed;
  border-radius: 8px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  z-index: 10;
}

:global(.dark-theme) .emoji-panel {
  background: #2d2d2d;
  border-color: #404040;
}

.emoji-item {
  border: none;
  background: none;
  font-size: 20px;
  cursor: pointer;
  padding: 4px;
}

.compose-actions {
  display: flex;
  gap: 10px;
}

.btn-preview {
  padding: 8px 20px;
  border: 1px solid #dcdfe6;
  border-radius: 6px;
  background: #fff;
  color: #606266;
  font-size: 14px;
  cursor: pointer;
}

.btn-publish {
  padding: 8px 24px;
  border: none;
  border-radius: 6px;
  background: #95d475;
  color: #fff;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
}

.btn-publish:hover:not(:disabled) {
  background: #85c965;
}

.btn-publish:disabled {
  opacity: 0.65;
  cursor: not-allowed;
}

.back-list {
  display: inline-block;
  margin-top: 20px;
  color: #409eff;
  text-decoration: none;
  font-size: 14px;
}

.back-list:hover {
  text-decoration: underline;
}

.preview-body {
  white-space: pre-wrap;
  line-height: 1.7;
  margin: 0;
}

@media (max-width: 768px) {
  .content-wrapper {
    flex-direction: column;
  }
}
</style>
