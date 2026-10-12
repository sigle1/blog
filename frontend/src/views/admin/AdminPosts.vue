<template>
  <div class="admin-posts">
    <el-card shadow="hover" class="posts-card">
      <template #header>
        <div class="card-header">
          <span>文章管理</span>
          <div class="header-actions">
            <el-button type="primary" @click="$router.push('/admin/posts/create')">
              <el-icon><Plus /></el-icon>
              发布文章
            </el-button>
          </div>
        </div>
      </template>

      <!-- 搜索和筛选 -->
      <div class="search-filter">
        <el-form :inline="true" :model="searchForm" class="demo-form-inline">
          <el-form-item>
            <el-input v-model="searchForm.keyword" placeholder="搜索标题或摘要" clearable @clear="handleSearch" />
          </el-form-item>
          <el-form-item>
            <el-select v-model="searchForm.sort" placeholder="排序" style="width: 120px">
              <el-option label="按时间" value="created_at" />
              <el-option label="按浏览量" value="views" />
              <el-option label="按点赞数" value="likes" />
            </el-select>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="handleSearch">搜索</el-button>
            <el-button @click="handleReset">重置</el-button>
          </el-form-item>
        </el-form>
      </div>

      <el-table :data="posts" style="width: 100%" v-loading="loading">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column label="缩略图" width="120">
          <template #default="{ row }">
            <el-image 
              v-if="row.coverImage" 
              :src="row.coverImage" 
              style="width: 60px; height: 40px"
              fit="cover"
            >
              <template #error>
                <div class="image-slot">
                  <el-icon><Picture /></el-icon>
                </div>
              </template>
            </el-image>
            <span v-else>无</span>
          </template>
        </el-table-column>
        <el-table-column prop="title" label="标题" min-width="200" show-overflow-tooltip />
        <el-table-column label="标签" width="180">
          <template #default="{ row }">
            <el-tag v-for="tag in row.tags" :key="tag" size="small" style="margin-right: 5px">
              {{ tag }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="views" label="浏览量" width="100" />
        <el-table-column prop="likes" label="点赞数" width="100" />
        <el-table-column prop="createdAt" label="发布时间" width="180">
          <template #default="{ row }">
            {{ formatDate(row.createdAt) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button size="small" type="primary" @click="editPost(row.id)">编辑</el-button>
            <el-button size="small" type="info" @click="previewPost(row.id)">预览</el-button>
            <el-button size="small" type="danger" @click="deletePost(row.id)">删除</el-button>
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
import { ref, reactive, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Picture } from '@element-plus/icons-vue'
import api from '@/api'
import { normalizeImageUrl } from '@/utils/image'

const router = useRouter()

const posts = ref([])
const loading = ref(false)
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)

const searchForm = reactive({
  keyword: '',
  sort: 'created_at'
})

const loadPosts = async () => {
  loading.value = true
  try {
    const response = await api.getPosts()
    let list = []
    if (response.data) {
      if (Array.isArray(response.data)) {
        list = response.data
      } else if (response.data.data && Array.isArray(response.data.data)) {
        list = response.data.data
      } else if (response.data.posts && Array.isArray(response.data.posts)) {
        list = response.data.posts
      }
    }
    
    // 应用本地搜索和排序
    list = list.map(item => ({
      ...item,
      coverImage: normalizeImageUrl(item.coverImage || item.coverUrl || item.coverTitle)
    }))

    let filteredPosts = list
    if (searchForm.keyword) {
      const kw = searchForm.keyword.toLowerCase()
      filteredPosts = filteredPosts.filter(p => 
        p.title.toLowerCase().includes(kw) || 
        (p.summary && p.summary.toLowerCase().includes(kw))
      )
    }

    filteredPosts.sort((a, b) => {
      if (searchForm.sort === 'created_at') {
        return new Date(b.createdAt) - new Date(a.createdAt)
      } else if (searchForm.sort === 'views') {
        return (b.views || 0) - (a.views || 0)
      } else if (searchForm.sort === 'likes') {
        return (b.likes || 0) - (a.likes || 0)
      }
      return 0
    })

    total.value = filteredPosts.length
    const start = (currentPage.value - 1) * pageSize.value
    posts.value = filteredPosts.slice(start, start + pageSize.value)
  } catch (err) {
    console.error('获取文章列表失败:', err)
    ElMessage.error('获取文章列表失败')
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  currentPage.value = 1
  loadPosts()
}

const handleReset = () => {
  searchForm.keyword = ''
  searchForm.sort = 'created_at'
  handleSearch()
}

const handleSizeChange = (val) => {
  pageSize.value = val
  loadPosts()
}

const handleCurrentChange = (val) => {
  currentPage.value = val
  loadPosts()
}

const editPost = (id) => {
  router.push(`/admin/posts/edit/${id}`)
}

const previewPost = (id) => {
  // 假设前台文章详情页路径为 /hobbies/:hobbyId/articles/:articleId
  // 或者是一个通用的 /post/:id。这里需要根据实际路由调整
  // 由于 Post 实体可能不直接属于某个 hobby，我们先用 /post/:id
  window.open(`/post/${id}`, '_blank')
}

const deletePost = (id) => {
  ElMessageBox.confirm('确定要删除这篇文章吗？', '删除确认', {
    type: 'warning'
  }).then(async () => {
    try {
      await api.deletePost(id)
      ElMessage.success('删除成功')
      loadPosts()
    } catch (err) {
      ElMessage.error('删除失败')
    }
  }).catch(() => {})
}

const formatDate = (value) => {
  if (!value) return ''
  return new Date(value).toLocaleString('zh-CN')
}

onMounted(loadPosts)
</script>

<style scoped>
.admin-posts {
  padding: 20px;
}
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.search-filter {
  margin-bottom: 20px;
}
.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}
.image-slot {
  display: flex;
  justify-content: center;
  align-items: center;
  width: 100%;
  height: 100%;
  background: #f5f7fa;
  color: #909399;
}
</style>
