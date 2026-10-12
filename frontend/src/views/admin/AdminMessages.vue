<template>
  <div class="admin-messages">
    <el-card shadow="hover">
      <template #header>
        <div class="card-header">
          <span>留言管理</span>
        </div>
      </template>

      <div class="search-filter">
        <el-form :inline="true">
          <el-form-item>
            <el-input
              v-model="keyword"
              placeholder="按昵称/内容搜索"
              clearable
              @clear="handleSearch"
            />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="handleSearch">搜索</el-button>
            <el-button @click="handleReset">重置</el-button>
          </el-form-item>
        </el-form>
      </div>

      <el-table :data="list" v-loading="loading" style="width: 100%">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="nickname" label="昵称" width="140" />
        <el-table-column prop="content" label="留言内容" min-width="320" show-overflow-tooltip />
        <el-table-column prop="createdAt" label="时间" width="180">
          <template #default="{ row }">{{ formatDate(row.createdAt) }}</template>
        </el-table-column>
        <el-table-column label="操作" width="100" fixed="right">
          <template #default="{ row }">
            <el-button type="danger" size="small" @click="deleteMessage(row.id)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-container">
        <el-pagination
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          :total="total"
          :page-sizes="[10, 20, 50]"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import api from '@/api'

const loading = ref(false)
const list = ref([])
const keyword = ref('')
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)

const loadMessages = async () => {
  loading.value = true
  try {
    const response = await api.adminGetMessages({
      keyword: keyword.value || undefined,
      page: currentPage.value,
      size: pageSize.value
    })
    const data = response.data || {}
    list.value = Array.isArray(data.list) ? data.list : []
    total.value = Number(data.total || 0)
  } catch (error) {
    console.error('获取留言失败:', error)
    ElMessage.error('获取留言失败')
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  currentPage.value = 1
  loadMessages()
}

const handleReset = () => {
  keyword.value = ''
  handleSearch()
}

const handleSizeChange = (size) => {
  pageSize.value = size
  currentPage.value = 1
  loadMessages()
}

const handleCurrentChange = (page) => {
  currentPage.value = page
  loadMessages()
}

const deleteMessage = async (id) => {
  ElMessageBox.confirm('确定删除这条留言吗？', '删除确认', { type: 'warning' })
    .then(async () => {
      await api.adminDeleteMessage(id)
      ElMessage.success('删除成功')
      loadMessages()
    })
    .catch(() => {})
}

const formatDate = (value) => {
  if (!value) return ''
  return new Date(value).toLocaleString('zh-CN')
}

onMounted(loadMessages)
</script>

<style scoped>
.admin-messages {
  padding: 20px;
}

.card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.search-filter {
  margin-bottom: 16px;
}

.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}
</style>
