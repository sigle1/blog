<template>
  <div class="note-detail-container">
    <div v-if="note" class="note-content-card">
      <h1 class="note-title">{{ note.title }}</h1>
      <div class="note-meta">
        <span>发布于 {{ formatDate(note.createdAt) }}</span>
      </div>
      <div class="note-body">
        {{ note.content }}
      </div>
      <router-link to="/notes" class="back-link">返回列表</router-link>
    </div>
    <div v-else class="loading-state">
      <p>加载中或笔记不存在...</p>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import api from '@/api'

const route = useRoute()
const note = ref(null)

onMounted(async () => {
  try {
    const response = await api.getNote(route.params.id)
    note.value = response.data
  } catch (error) {
    console.error('获取笔记详情失败:', error)
  }
})

function formatDate(value) {
  if (!value) return ''
  // 如果是数组 [year, month, day, hour, minute, second]
  if (Array.isArray(value)) {
    const [y, m, d, hh = 0, mm = 0, ss = 0] = value
    // 后端数组中月份通常是 1-12，JS Date 的 month 为 0-11，因此减 1
    return new Date(y, (m || 1) - 1, d || 1, hh, mm, ss).toLocaleString()
  }
  // 如果是时间戳数字
  if (typeof value === 'number') {
    return new Date(value).toLocaleString()
  }
  // 其它按字符串解析（ISO 格式等）
  try {
    return new Date(value).toLocaleString()
  } catch (e) {
    return String(value)
  }
}
</script>

<style scoped>
.note-detail-container {
  max-width: 800px;
  margin: 40px auto;
  padding: 20px;
  color: #fff;
}

.note-content-card {
  background-color: rgba(0, 0, 0, 0.25);
  backdrop-filter: blur(12px);
  -webkit-backdrop-filter: blur(12px);
  border-radius: 16px;
  padding: 40px;
  border: 1px solid rgba(255, 255, 255, 0.1);
}

.note-title {
  font-size: 32px;
  font-weight: 700;
  margin: 0 0 15px 0;
  text-shadow: 0 0 10px rgba(255, 255, 255, 0.5);
}

.note-meta {
  font-size: 14px;
  color: #ccc;
  margin-bottom: 30px;
  padding-bottom: 15px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.15);
}

.note-body {
  font-size: 16px;
  line-height: 1.8;
  color: #dcdcdc;
  white-space: pre-wrap; /* Preserve line breaks and spaces */
}

.back-link {
  display: inline-block;
  margin-top: 30px;
  color: #42b983;
  text-decoration: none;
  font-weight: bold;
  transition: color 0.3s;
}

.back-link:hover {
  color: #5ad59a;
}

.loading-state {
  text-align: center;
  padding: 50px;
  font-size: 18px;
  color: #ccc;
}
</style>
