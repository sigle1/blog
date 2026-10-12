<template>
  <div class="create-hobby-container">
    <h1 class="page-title">创建新爱好</h1>
    <el-card class="form-card">
      <el-form 
        ref="hobbyFormRef" 
        :model="hobby" 
        :rules="rules" 
        label-position="top"
        v-loading="loading"
      >
        <el-form-item label="爱好名称" prop="name">
          <el-input v-model="hobby.name" placeholder="例如：摄影、编程" />
        </el-form-item>
        
        <el-form-item label="简单描述" prop="description">
          <el-input 
            v-model="hobby.description" 
            type="textarea" 
            :rows="4" 
            placeholder="简单介绍一下这个爱好" 
          />
        </el-form-item>

        <el-form-item label="封面图片" prop="coverUrl">
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
              <img v-if="hobby.coverUrl" :src="hobby.coverUrl" class="cover-preview">
              <el-icon v-else class="cover-uploader-icon"><Plus /></el-icon>
            </el-upload>
            <el-input v-model="hobby.coverUrl" placeholder="或输入图片URL" style="margin-top: 10px;" />
          </div>
        </el-form-item>

        <el-form-item class="form-actions">
          <el-button type="primary" :loading="submitting" @click="submitHobby" class="submit-btn">
            创建
          </el-button>
          <el-button @click="router.back()">返回</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, computed } from 'vue'
import { useRouter } from 'vue-router'
import api from '@/api'
import { ElMessage } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import { extractUploadUrl } from '@/utils/image'

const router = useRouter()
const hobbyFormRef = ref(null)
const loading = ref(false)
const submitting = ref(false)

const hobby = reactive({
  name: '',
  description: '',
  coverUrl: ''
})

const rules = {
  name: [
    { required: true, message: '请输入爱好名称', trigger: 'blur' },
    { min: 2, max: 20, message: '名称长度在 2 到 20 个字符', trigger: 'blur' }
  ],
  description: [
    { required: true, message: '请输入爱好描述', trigger: 'blur' }
  ]
}

const uploadHeaders = computed(() => {
  const token = localStorage.getItem('adminToken') || localStorage.getItem('token')
  return token ? { Authorization: `Bearer ${token}` } : {}
})

const handleImageUploadSuccess = (response) => {
  hobby.coverUrl = extractUploadUrl(response)
  ElMessage.success('图片上传成功')
}

const handleImageUploadError = () => {
  ElMessage.error('图片上传失败')
}

const submitHobby = async () => {
  if (!hobbyFormRef.value) return
  
  try {
    await hobbyFormRef.value.validate()
    submitting.value = true
    await api.createHobby(hobby)
    ElMessage.success('爱好创建成功！')
    router.push('/hobbies')
  } catch (error) {
    if (error.name === 'ValidationError') return
    console.error('创建爱好失败:', error)
    ElMessage.error(error.response?.data?.message || '创建失败，请稍后重试。')
  } finally {
    submitting.value = false
  }
}
</script>

<style scoped>
.create-hobby-container {
  max-width: 600px;
  margin: 40px auto;
  padding: 0 20px;
  color: #fff;
}

.page-title {
  text-align: center;
  font-size: 32px;
  margin-bottom: 30px;
  text-shadow: 0 0 10px rgba(255, 255, 255, 0.5);
}

.form-card {
  background-color: rgba(0, 0, 0, 0.25) !important;
  backdrop-filter: blur(12px);
  border: 1px solid rgba(255, 255, 255, 0.1) !important;
  border-radius: 16px;
}

:deep(.el-form-item__label) {
  color: #ccc !important;
  font-weight: bold;
}

:deep(.el-input__wrapper),
:deep(.el-textarea__inner) {
  background-color: rgba(255, 255, 255, 0.1) !important;
  box-shadow: 0 0 0 1px rgba(255, 255, 255, 0.2) inset !important;
}

:deep(.el-input__inner),
:deep(.el-textarea__inner) {
  color: #fff !important;
}

.cover-upload-container {
  width: 100%;
}

.cover-uploader {
  border: 1px dashed rgba(255, 255, 255, 0.3);
  border-radius: 8px;
  cursor: pointer;
  position: relative;
  overflow: hidden;
  transition: border-color 0.3s;
  width: 200px;
  height: 200px;
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
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.form-actions {
  margin-top: 30px;
  display: flex;
  justify-content: center;
}

.submit-btn {
  padding: 12px 30px;
  font-size: 16px;
  font-weight: bold;
}
</style>
