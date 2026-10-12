<template>
  <div class="create-article-container">
    <el-card shadow="hover" class="article-card">
      <template #header>
        <div class="card-header">
          <span class="page-title">为 {{ hobbyName }} 撰写新文章</span>
        </div>
      </template>

      <el-form 
        ref="articleFormRef" 
        :model="article" 
        :rules="rules" 
        label-width="80px"
        v-loading="loading"
      >
        <el-form-item label="文章标题" prop="title">
          <el-input v-model="article.title" placeholder="给文章起个名字" />
        </el-form-item>
        
        <el-form-item label="标签" prop="tags">
          <el-input v-model="article.tags" placeholder="请输入标签，用逗号分隔" />
        </el-form-item>

        <el-form-item label="封面图片" prop="coverImage">
          <div class="cover-upload-container">
            <el-upload
              class="cover-uploader"
              action="/api/upload"
              :show-file-list="false"
              :headers="uploadHeaders"
              :on-success="handleImageUploadSuccess"
              :on-error="handleImageUploadError"
              accept="image/*"
            >
              <img v-if="article.coverImage" :src="article.coverImage" class="cover-preview">
              <el-icon v-else class="cover-uploader-icon"><Plus /></el-icon>
            </el-upload>
            <el-input v-model="article.coverImage" placeholder="或输入图片URL" style="margin-top: 10px;" />
          </div>
        </el-form-item>

        <el-form-item label="文章内容" prop="content">
          <p class="content-hint">支持 Markdown；「插入正文图片」会上传并插入 <code>![](地址)</code>。</p>
          <div class="content-toolbar">
            <el-upload
              class="inline-upload"
              action="/api/upload"
              :show-file-list="false"
              :headers="uploadHeaders"
              accept="image/*"
              :before-upload="beforeContentImageUpload"
              :on-success="handleContentImageSuccess"
              :on-error="handleImageUploadError"
            >
              <el-button type="primary" plain size="small">插入正文图片</el-button>
            </el-upload>
          </div>
          <el-input
            ref="contentInputRef"
            v-model="article.content"
            type="textarea"
            :rows="15"
            placeholder="在这里写下你的故事（Markdown）..."
          />
        </el-form-item>

        <el-form-item>
          <el-button type="primary" :loading="submitting" @click="submitArticle" class="submit-btn">
            发布文章
          </el-button>
          <el-button @click="goBack">返回</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted, reactive, computed, nextTick } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import api from '@/api'
import { extractUploadUrl } from '@/utils/image'

const contentInputRef = ref(null)

const route = useRoute()
const router = useRouter()

const hobbyId = ref(route.params.hobbyId)
const hobbyName = ref('')
const loading = ref(false)
const submitting = ref(false)
const articleFormRef = ref(null)

const article = reactive({
  title: '',
  content: '',
  coverImage: '',
  tags: ''
})

const rules = {
  title: [{ required: true, message: '请输入文章标题', trigger: 'blur' }],
  content: [{ required: true, message: '请输入文章内容', trigger: 'blur' }]
}

const uploadHeaders = computed(() => {
  const token = localStorage.getItem('adminToken') || localStorage.getItem('token')
  return token ? { Authorization: `Bearer ${token}` } : {}
})

onMounted(async () => {
  loading.value = true
  try {
    const response = await api.getHobby(hobbyId.value)
    const data = response.data
    hobbyName.value = data.name || (data.hobby && data.hobby.name) || '未知爱好'
  } catch (error) {
    console.error('获取爱好信息失败:', error)
    hobbyName.value = '未知爱好'
  } finally {
    loading.value = false
  }
})

const handleImageUploadSuccess = (response) => {
  article.coverImage = extractUploadUrl(response)
  ElMessage.success('图片上传成功')
}

const handleImageUploadError = () => {
  ElMessage.error('图片上传失败')
}

const beforeContentImageUpload = (file) => {
  if (!file.type || !file.type.startsWith('image/')) {
    ElMessage.warning('请选择图片文件')
    return false
  }
  if (file.size > 8 * 1024 * 1024) {
    ElMessage.warning('图片不能超过 8MB')
    return false
  }
  return true
}

const insertIntoContent = (snippet) => {
  const ta = contentInputRef.value?.textarea
  const cur = article.content || ''
  if (ta && typeof ta.selectionStart === 'number') {
    const start = ta.selectionStart
    const end = ta.selectionEnd
    article.content = cur.slice(0, start) + snippet + cur.slice(end)
    nextTick(() => {
      ta.focus()
      const pos = start + snippet.length
      ta.setSelectionRange(pos, pos)
    })
  } else {
    article.content = cur + snippet
  }
}

const handleContentImageSuccess = (response) => {
  const url = extractUploadUrl(response)
  if (!url) {
    ElMessage.error('未获取到图片地址')
    return
  }
  insertIntoContent(`\n\n![](${url})\n\n`)
  ElMessage.success('已插入图片')
}

const submitArticle = async () => {
  if (!articleFormRef.value) return
  
  await articleFormRef.value.validate(async (valid) => {
    if (valid) {
      submitting.value = true
      try {
        const payload = {
          ...article,
          views: 0,
          tags: article.tags ? article.tags.split(',').map(t => t.trim()).filter(t => t) : []
        }
        await api.addArticleToHobby(hobbyId.value, payload)
        ElMessage.success('文章发布成功！')
        if (route.path.startsWith('/admin')) {
          router.push(`/admin/hobbies/${hobbyId.value}/manage`)
        } else {
          router.push(`/hobbies/${hobbyId.value}`)
        }
      } catch (error) {
        console.error('发布文章失败:', error)
        ElMessage.error('发布失败，请稍后重试。')
      } finally {
        submitting.value = false
      }
    }
  })
}

const goBack = () => {
  if (route.path.startsWith('/admin')) {
    router.push(`/admin/hobbies/${hobbyId.value}/manage`)
  } else {
    router.back()
  }
}
</script>

<style scoped>
.create-article-container {
  max-width: 900px;
  margin: 40px auto;
  padding: 0 20px;
}

.article-card {
  background-color: rgba(255, 255, 255, 0.05);
  backdrop-filter: blur(10px);
  border: 1px solid rgba(255, 255, 255, 0.1);
  color: #fff;
}

.card-header {
  display: flex;
  justify-content: center;
  align-items: center;
}

.page-title {
  font-size: 24px;
  font-weight: bold;
  color: #42b983;
}

.cover-upload-container {
  display: flex;
  flex-direction: column;
  align-items: flex-start;
}

.cover-uploader {
  border: 1px dashed rgba(255, 255, 255, 0.2);
  border-radius: 8px;
  cursor: pointer;
  position: relative;
  overflow: hidden;
  width: 200px;
  height: 120px;
  display: flex;
  justify-content: center;
  align-items: center;
  background-color: rgba(255, 255, 255, 0.05);
  transition: border-color 0.3s;
}

.cover-uploader:hover {
  border-color: #42b983;
}

.cover-uploader-icon {
  font-size: 28px;
  color: #8c939d;
}

.cover-preview {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.submit-btn {
  width: 120px;
}

:deep(.el-form-item__label) {
  color: #ccc;
}

:deep(.el-input__wrapper), :deep(.el-textarea__inner) {
  background-color: rgba(255, 255, 255, 0.1) !important;
  box-shadow: 0 0 0 1px rgba(255, 255, 255, 0.2) inset !important;
  color: #fff !important;
}

:deep(.el-input__inner) {
  color: #fff !important;
}

:deep(.el-card__header) {
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
}

.content-hint {
  margin: 0 0 8px;
  font-size: 13px;
  color: #aaa;
  line-height: 1.5;
}
.content-hint code {
  font-size: 12px;
  padding: 1px 4px;
  background: rgba(0, 0, 0, 0.25);
  border-radius: 4px;
}
.content-toolbar {
  margin-bottom: 8px;
}
.inline-upload {
  display: inline-block;
}
</style>
