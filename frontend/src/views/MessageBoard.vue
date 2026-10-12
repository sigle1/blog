<template>
  <div class="message-board-page">
    <div class="container">
      <div class="content-wrapper">
        <main class="main-content">
          <section class="publish-card">
            <h2 class="page-title">留言板</h2>
            <p class="page-desc">写下你的留言，支持点赞、收藏与评论互动</p>
            <div class="publish-form">
              <input
                v-model="form.nickname"
                class="publish-input"
                type="text"
                maxlength="50"
                placeholder="昵称（可选）"
              >
              <textarea
                v-model="form.content"
                class="publish-textarea"
                rows="4"
                maxlength="500"
                placeholder="欢迎大家在此留言……"
              />
              <div class="publish-actions">
                <button type="button" class="btn-publish" :disabled="submitting" @click="submitMessage">
                  {{ submitting ? '发布中…' : '发布留言' }}
                </button>
              </div>
            </div>
          </section>

          <section class="message-list" v-loading="loading">
            <el-empty v-if="!messages.length && !loading" description="还没有留言，快来第一条吧" />
            <router-link
              v-for="item in messages"
              :key="item.id"
              :to="`/messages/${item.id}`"
              class="message-item"
            >
              <div class="item-avatar" :style="{ backgroundColor: getUserColor(item.nickname) }">
                {{ getAvatarText(item.nickname) }}
              </div>
              <div class="item-body">
                <div class="item-head">
                  <span class="item-author">{{ item.nickname || '游客' }}</span>
                  <time class="item-time">{{ formatDateTime(item.createdAt) }}</time>
                </div>
                <p class="item-content">{{ excerpt(item.content) }}</p>
                <div class="item-stats">
                  <span>💬 {{ item.commentCount ?? 0 }}</span>
                  <span>👍 {{ item.likes ?? 0 }}</span>
                  <span>⭐ {{ item.favorites ?? 0 }}</span>
                </div>
              </div>
            </router-link>
          </section>
        </main>
        <BlogSidebar />
      </div>
    </div>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElEmpty } from 'element-plus'
import api from '@/api'
import BlogSidebar from '@/components/BlogSidebar.vue'

const router = useRouter()
const loading = ref(false)
const submitting = ref(false)
const messages = ref([])
const form = ref({
  nickname: localStorage.getItem('guestNickname') || '',
  content: ''
})

function getAvatarText(nickname) {
  return (nickname || '游').trim().charAt(0).toUpperCase()
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
    return d.toLocaleString('zh-CN', { hour12: false })
  } catch {
    return String(value)
  }
}

function excerpt(text, max = 120) {
  const t = (text || '').trim()
  return t.length > max ? `${t.slice(0, max)}…` : t
}

const loadMessages = async () => {
  loading.value = true
  try {
    const response = await api.getMessages()
    messages.value = Array.isArray(response.data) ? response.data : []
  } catch (error) {
    console.error('获取留言失败:', error)
    ElMessage.error('获取留言失败')
  } finally {
    loading.value = false
  }
}

const submitMessage = async () => {
  const content = (form.value.content || '').trim()
  if (!content) {
    ElMessage.warning('请输入留言内容')
    return
  }
  const nickname = (form.value.nickname || '').trim() || '游客'
  localStorage.setItem('guestNickname', nickname)
  submitting.value = true
  try {
    const res = await api.createMessage({ nickname, content })
    form.value.content = ''
    ElMessage.success('留言成功')
    const id = res.data?.id
    if (id) {
      router.push(`/messages/${id}`)
    } else {
      await loadMessages()
    }
  } catch (error) {
    console.error('留言失败:', error)
    ElMessage.error('留言失败')
  } finally {
    submitting.value = false
  }
}

onMounted(loadMessages)
</script>

<style scoped>
.message-board-page {
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

.publish-card {
  background: var(--card-bg, #fff);
  border-radius: 12px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06);
  padding: 24px 28px;
  margin-bottom: 20px;
}

.page-title {
  margin: 0 0 8px;
  font-size: 22px;
}

.page-desc {
  margin: 0 0 16px;
  font-size: 14px;
  color: #909399;
}

.publish-form {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.publish-input,
.publish-textarea {
  width: 100%;
  box-sizing: border-box;
  border: 1px solid #dcdfe6;
  border-radius: 8px;
  padding: 10px 14px;
  font-size: 14px;
  font-family: inherit;
  color: var(--text-color);
  background: var(--card-bg, #fff);
}

.publish-textarea {
  resize: vertical;
  min-height: 100px;
}

.publish-input:focus,
.publish-textarea:focus {
  outline: none;
  border-color: #409eff;
}

.publish-actions {
  display: flex;
  justify-content: flex-end;
}

.btn-publish {
  padding: 10px 28px;
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

.message-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.message-item {
  display: flex;
  gap: 14px;
  padding: 20px 24px;
  background: var(--card-bg, #fff);
  border-radius: 12px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06);
  text-decoration: none;
  color: inherit;
  transition: transform 0.2s, box-shadow 0.2s;
}

.message-item:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(0, 0, 0, 0.08);
}

.item-avatar {
  width: 44px;
  height: 44px;
  border-radius: 50%;
  flex-shrink: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  font-weight: 700;
}

.item-head {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 12px;
  margin-bottom: 8px;
}

.item-author {
  font-weight: 600;
  color: var(--text-color);
}

.item-time {
  font-size: 12px;
  color: #909399;
  flex-shrink: 0;
}

.item-content {
  margin: 0 0 12px;
  line-height: 1.7;
  color: #606266;
  font-size: 14px;
}

:global(.dark-theme) .item-content {
  color: #c0c4cc;
}

.item-stats {
  display: flex;
  gap: 16px;
  font-size: 13px;
  color: #909399;
}

@media (max-width: 768px) {
  .content-wrapper {
    flex-direction: column;
  }
}
</style>
