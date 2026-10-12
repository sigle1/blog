<template>
  <div class="create-post">
    <el-card shadow="hover">
      <template #header>
        <div class="card-header">
          <span>{{ isEdit ? '编辑文章' : '发布文章' }}</span>
          <el-button @click="goBack">返回列表</el-button>
        </div>
      </template>

      <el-form 
        ref="postFormRef" 
        :model="form" 
        :rules="rules" 
        label-width="80px"
        v-loading="loading"
      >
        <el-form-item label="标题" prop="title">
          <el-input v-model="form.title" placeholder="请输入文章标题" />
        </el-form-item>

        <el-form-item label="摘要" prop="summary">
          <el-input 
            v-model="form.summary" 
            type="textarea" 
            :rows="3" 
            placeholder="请输入文章摘要" 
          />
        </el-form-item>

        <el-form-item label="分类" prop="category">
          <el-select v-model="form.category" placeholder="选择或输入分类" filterable allow-create default-first-option style="width: 100%">
            <el-option label="随笔" value="随笔" />
            <el-option label="技术" value="技术" />
            <el-option label="生活" value="生活" />
          </el-select>
        </el-form-item>

        <el-form-item label="标签" prop="tags">
          <el-input v-model="form.tags" placeholder="请输入标签，用逗号分隔" />
        </el-form-item>

        <el-form-item label="封面" prop="coverImage">
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
              <img v-if="form.coverImage" :src="form.coverImage" class="cover-preview">
              <el-icon v-else class="cover-uploader-icon"><Plus /></el-icon>
            </el-upload>
            <el-input v-model="form.coverImage" placeholder="或输入图片URL" style="margin-top: 10px;" />
          </div>
        </el-form-item>

        <el-form-item label="内容" prop="content">
          <p class="content-hint">支持 Markdown。可用「插入正文图片」上传，会自动插入 <code>![](地址)</code>；也可手写 <code>![说明](/uploads/xxx.png)</code>。</p>
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
            v-model="form.content"
            type="textarea"
            :rows="15"
            placeholder="请输入文章内容（Markdown）"
          />
        </el-form-item>

        <el-form-item>
          <el-button type="primary" :loading="submitting" @click="submitForm">
            {{ isEdit ? '更新' : '发布' }}
          </el-button>
          <el-button @click="goBack">取消</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed, nextTick } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import api from '@/api'
import { extractUploadUrl, normalizeImageUrl } from '@/utils/image'

const router = useRouter()
const route = useRoute()

const postFormRef = ref(null)
const contentInputRef = ref(null)
const isEdit = ref(false)
const postId = ref(null)
const loading = ref(false)
const submitting = ref(false)

const form = reactive({
  title: '',
  category: '随笔',
  summary: '',
  content: '',
  tags: '',
  coverImage: ''
})

const rules = {
  title: [{ required: true, message: '请输入文章标题', trigger: 'blur' }],
  content: [{ required: true, message: '请输入文章内容', trigger: 'blur' }]
}

const uploadHeaders = computed(() => {
  const token = localStorage.getItem('adminToken') || localStorage.getItem('token')
  return token ? { Authorization: `Bearer ${token}` } : {}
})

const goBack = () => {
  router.push('/admin/posts')
}

const loadPost = async () => {
  if (!postId.value) return
  loading.value = true
  try {
    const response = await api.getPost(postId.value)
    if (response.data) {
      const post = response.data
      form.title = post.title || ''
      form.category = post.category && String(post.category).trim() ? post.category.trim() : '随笔'
      form.summary = post.summary || ''
      form.content = post.content || ''
      form.tags = Array.isArray(post.tags) ? post.tags.join(',') : (post.tags || '')
      form.coverImage = normalizeImageUrl(post.coverImage || post.coverUrl || post.coverTitle)
    }
  } catch (err) {
    console.error('获取文章详情失败:', err)
    ElMessage.error('获取文章详情失败')
  } finally {
    loading.value = false
  }
}

const handleImageUploadSuccess = (response) => {
  form.coverImage = extractUploadUrl(response)
  ElMessage.success('图片上传成功')
}

const handleImageUploadError = () => {
  ElMessage.error('图片上传失败')
}

const beforeContentImageUpload = (file) => {
  const ok = file.type && file.type.startsWith('image/')
  if (!ok) {
    ElMessage.warning('请选择图片文件')
    return false
  }
  const max = 8 * 1024 * 1024
  if (file.size > max) {
    ElMessage.warning('图片不能超过 8MB')
    return false
  }
  return true
}

const insertIntoContent = (snippet) => {
  const inst = contentInputRef.value
  const ta = inst?.textarea
  const cur = form.content || ''
  if (ta && typeof ta.selectionStart === 'number') {
    const start = ta.selectionStart
    const end = ta.selectionEnd
    form.content = cur.slice(0, start) + snippet + cur.slice(end)
    nextTick(() => {
      ta.focus()
      const pos = start + snippet.length
      ta.setSelectionRange(pos, pos)
    })
  } else {
    form.content = cur + snippet
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

const submitForm = async () => {
  if (!postFormRef.value) return
  
  await postFormRef.value.validate(async (valid) => {
    if (valid) {
      submitting.value = true
      try {
        const tags = form.tags ? form.tags.split(',').map(t => t.trim()).filter(t => t) : []
        const postData = {
          title: form.title,
          category: (form.category || '随笔').trim(),
          summary: form.summary,
          content: form.content,
          coverImage: form.coverImage,
          tags
        }
        
        if (isEdit.value) {
          await api.updatePost(postId.value, postData)
          ElMessage.success('更新成功')
        } else {
          await api.createPost(postData)
          ElMessage.success('发布成功')
        }
        router.push('/admin/posts')
      } catch (err) {
        console.error('保存文章失败:', err)
        ElMessage.error('保存失败')
      } finally {
        submitting.value = false
      }
    }
  })
}

onMounted(() => {
  const id = route.params.id
  if (id) {
    isEdit.value = true
    postId.value = id
    loadPost()
  }
})
</script>

<style scoped>
.create-post {
  padding: 20px;
}
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.cover-upload-container {
  display: flex;
  flex-direction: column;
  align-items: flex-start;
}
.cover-uploader {
  border: 1px dashed #d9d9d9;
  border-radius: 6px;
  cursor: pointer;
  position: relative;
  overflow: hidden;
  width: 178px;
  height: 100px;
  display: flex;
  justify-content: center;
  align-items: center;
}
.cover-uploader:hover {
  border-color: #409eff;
}
.cover-uploader-icon {
  font-size: 28px;
  color: #8c939d;
}
.cover-preview {
  width: 178px;
  height: 100px;
  object-fit: cover;
}
.content-hint {
  margin: 0 0 8px;
  font-size: 13px;
  color: #606266;
  line-height: 1.5;
}
.content-hint code {
  font-size: 12px;
  padding: 1px 4px;
  background: #f4f4f5;
  border-radius: 4px;
}
.content-toolbar {
  margin-bottom: 8px;
}
.inline-upload {
  display: inline-block;
}
</style>
