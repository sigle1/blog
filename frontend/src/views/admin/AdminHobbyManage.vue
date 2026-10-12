<template>
  <div class="admin-hobby-manage">
    <div class="manage-toolbar">
      <el-button @click="goHobbiesList">← 返回爱好列表</el-button>
      <h2 class="manage-title">{{ hobbyName || '爱好' }} · 专栏与动态</h2>
      <p class="manage-desc">专栏文章与随手动态仅在此维护；前台爱好页只展示内容。</p>
    </div>

    <el-tabs v-model="activeTab" class="manage-tabs">
      <el-tab-pane label="专栏文章" name="articles">
        <div class="pane-actions">
          <el-button type="primary" @click="goCreateArticle">发布新文章</el-button>
          <el-button @click="loadAll">刷新列表</el-button>
        </div>
        <el-table :data="articles" v-loading="loading" style="width: 100%">
          <el-table-column prop="id" label="ID" width="72" />
          <el-table-column prop="title" label="标题" min-width="200" show-overflow-tooltip />
          <el-table-column label="阅读量" width="88">
            <template #default="{ row }">{{ row.views ?? 0 }}</template>
          </el-table-column>
          <el-table-column label="发布时间" width="170">
            <template #default="{ row }">{{ formatDate(row.createdAt) }}</template>
          </el-table-column>
          <el-table-column label="操作" width="120" fixed="right">
            <template #default="{ row }">
              <el-button type="primary" link @click="openPublicArticle(row.id)">前台查看</el-button>
            </template>
          </el-table-column>
        </el-table>
        <p v-if="!loading && articles.length === 0" class="hint-muted">暂无文章，点击「发布新文章」添加。</p>
      </el-tab-pane>

      <el-tab-pane label="随手动态" name="notes">
        <el-form class="note-form" @submit.prevent="submitNote">
          <el-input
            v-model="newNoteContent"
            type="textarea"
            :rows="4"
            placeholder="短句、灵感、打卡……会显示在爱好详情页的「随手动态」时间线。"
            maxlength="500"
            show-word-limit
          />
          <el-button type="primary" class="note-submit" :loading="addingNote" :disabled="!newNoteContent.trim()" @click="submitNote">
            发布动态
          </el-button>
        </el-form>

        <el-timeline v-if="contents.length" class="note-timeline">
          <el-timeline-item
            v-for="c in contents"
            :key="c.id"
            :timestamp="formatDate(c.createdAt)"
            placement="top"
          >
            <div class="note-line">{{ c.content }}</div>
          </el-timeline-item>
        </el-timeline>
        <p v-else class="hint-muted">暂无动态，在上方输入并发布。</p>
      </el-tab-pane>
    </el-tabs>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElButton, ElTabs, ElTabPane, ElTable, ElTableColumn, ElForm, ElInput, ElTimeline, ElTimelineItem, ElMessage } from 'element-plus'
import api from '@/api'

const route = useRoute()
const router = useRouter()

const hobbyId = ref(route.params.id)
const hobbyName = ref('')
const activeTab = ref('articles')
const loading = ref(false)
const articles = ref([])
const contents = ref([])
const newNoteContent = ref('')
const addingNote = ref(false)

function formatDate(value) {
  if (!value) return ''
  if (Array.isArray(value)) {
    const [y, m, d, hh = 0, mm = 0] = value
    return `${y}-${String(m).padStart(2, '0')}-${String(d).padStart(2, '0')} ${String(hh).padStart(2, '0')}:${String(mm).padStart(2, '0')}`
  }
  try {
    return new Date(value).toLocaleString('zh-CN')
  } catch {
    return String(value)
  }
}

async function loadAll() {
  loading.value = true
  try {
    const detail = await api.getHobby(hobbyId.value)
    const data = detail.data
    const h = data.hobby || data
    hobbyName.value = h.name || ''
    contents.value = data.contents || h.contents || []

    const artRes = await api.getArticles(hobbyId.value)
    articles.value = Array.isArray(artRes.data) ? artRes.data : []
  } catch (e) {
    console.error(e)
    ElMessage.error('加载失败')
  } finally {
    loading.value = false
  }
}

function goHobbiesList() {
  router.push('/admin/hobbies')
}

function goCreateArticle() {
  router.push(`/admin/hobbies/${hobbyId.value}/articles/create`)
}

function openPublicArticle(articleId) {
  const url = `/hobbies/${hobbyId.value}/articles/${articleId}`
  window.open(url, '_blank', 'noopener,noreferrer')
}

async function submitNote() {
  const text = newNoteContent.value.trim()
  if (!text) return
  addingNote.value = true
  try {
    await api.addContentToHobby(hobbyId.value, { content: text })
    ElMessage.success('已发布')
    newNoteContent.value = ''
    await loadAll()
  } catch (e) {
    console.error(e)
    ElMessage.error('发布失败')
  } finally {
    addingNote.value = false
  }
}

onMounted(loadAll)
</script>

<style scoped>
.admin-hobby-manage {
  padding: 0 8px 24px;
}

.manage-toolbar {
  margin-bottom: 20px;
}

.manage-title {
  margin: 12px 0 8px;
  font-size: 20px;
  font-weight: 600;
  color: #303133;
}

.manage-desc {
  margin: 0;
  font-size: 13px;
  color: #909399;
  line-height: 1.5;
}

.manage-tabs {
  margin-top: 8px;
}

.pane-actions {
  display: flex;
  gap: 10px;
  margin-bottom: 14px;
}

.note-form {
  max-width: 640px;
  margin-bottom: 24px;
}

.note-submit {
  margin-top: 12px;
}

.note-timeline {
  max-width: 720px;
  padding-top: 8px;
}

.note-line {
  white-space: pre-wrap;
  font-size: 14px;
  color: #606266;
  line-height: 1.6;
}

.hint-muted {
  font-size: 13px;
  color: #909399;
  margin-top: 12px;
}
</style>
