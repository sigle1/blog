<template>
  <div class="admin-hobbies">
    <el-card shadow="hover" class="hobbies-card">
      <template #header>
        <div class="card-header">
          <span>爱好管理</span>
          <el-button type="primary" @click="handleAddHobby">
            <el-icon><Plus /></el-icon>
            添加爱好
          </el-button>
        </div>
      </template>

      <el-table :data="hobbies" style="width: 100%" v-loading="loading">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="name" label="名称" width="150" />
        <el-table-column prop="description" label="描述" show-overflow-tooltip />
        <el-table-column label="封面图片" width="120">
          <template #default="{ row }">
            <el-image 
              v-if="row.coverUrl" 
              :src="row.coverUrl" 
              style="width: 50px; height: 50px"
              fit="cover"
              :preview-src-list="[row.coverUrl]"
              preview-teleported
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
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" size="small" @click="handleEditHobby(row)">
              <el-icon><Edit /></el-icon>
              编辑
            </el-button>
            <el-button type="danger" size="small" @click="handleDeleteHobby(row.id)">
              <el-icon><Delete /></el-icon>
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 添加/编辑爱好对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="500px"
    >
      <el-form ref="hobbyFormRef" :model="hobbyForm" :rules="rules" label-width="80px">
        <el-form-item label="名称" prop="name">
          <el-input v-model="hobbyForm.name" placeholder="请输入爱好名称" />
        </el-form-item>
        <el-form-item label="描述" prop="description">
          <el-input
            v-model="hobbyForm.description"
            type="textarea"
            :rows="4"
            placeholder="请输入爱好描述"
          />
        </el-form-item>
        <el-form-item label="封面图片">
          <el-upload
            class="avatar-uploader"
            action="/api/upload"
            :show-file-list="false"
            :headers="uploadHeaders"
            :on-success="handleImageUploadSuccess"
            :on-error="handleImageUploadError"
            accept="image/*"
          >
            <img v-if="hobbyForm.coverUrl" :src="hobbyForm.coverUrl" class="avatar">
            <el-icon v-else class="avatar-uploader-icon"><Plus /></el-icon>
          </el-upload>
          <div class="upload-tip">点击图片更换</div>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" :loading="saveLoading" @click="handleSaveHobby">保存</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script>
import { ref, reactive, onMounted, computed } from 'vue'
import { 
  ElCard, ElTable, ElTableColumn, ElButton, ElDialog, 
  ElForm, ElFormItem, ElInput, ElIcon, ElMessage, 
  ElMessageBox, ElImage, ElUpload 
} from 'element-plus'
import { Plus, Edit, Delete, Picture } from '@element-plus/icons-vue'
import api from '@/api'
import { extractUploadUrl, normalizeImageUrl } from '@/utils/image'

export default {
  name: 'AdminHobbies',
  components: {
    ElCard, ElTable, ElTableColumn, ElButton, ElDialog, 
    ElForm, ElFormItem, ElInput, ElIcon, ElImage, ElUpload,
    Plus, Edit, Delete, Picture
  },
  setup() {
    const hobbies = ref([])
    const loading = ref(false)
    const saveLoading = ref(false)
    const hobbyFormRef = ref(null)
    const dialogVisible = ref(false)
    const dialogTitle = ref('添加爱好')
    
    const hobbyForm = reactive({
      id: '',
      name: '',
      description: '',
      coverUrl: ''
    })
    
    const rules = {
      name: [{ required: true, message: '请输入爱好名称', trigger: 'blur' }],
      description: [{ required: true, message: '请输入爱好描述', trigger: 'blur' }]
    }

    const uploadHeaders = computed(() => {
      const token = localStorage.getItem('adminToken') || localStorage.getItem('token')
      return token ? { Authorization: `Bearer ${token}` } : {}
    })
    
    // 加载爱好列表
    const loadHobbies = async () => {
      loading.value = true
      try {
        const response = await api.getHobbies()
        let list = []
        if (response.data) {
          if (Array.isArray(response.data)) {
            list = response.data
          } else if (response.data.data && Array.isArray(response.data.data)) {
            list = response.data.data
          } else if (response.data.hobbies && Array.isArray(response.data.hobbies)) {
            list = response.data.hobbies
          }
        }
        hobbies.value = list.map(item => ({
          ...item,
          coverUrl: normalizeImageUrl(item.coverUrl || item.coverImage)
        }))
      } catch (err) {
        console.error('获取爱好列表失败:', err)
        ElMessage.error('获取爱好列表失败')
      } finally {
        loading.value = false
      }
    }

    onMounted(loadHobbies)
    
    const handleAddHobby = () => {
      dialogTitle.value = '添加爱好'
      Object.assign(hobbyForm, { id: '', name: '', description: '', coverUrl: '' })
      dialogVisible.value = true
      if (hobbyFormRef.value) hobbyFormRef.value.clearValidate()
    }
    
    const handleEditHobby = (row) => {
      dialogTitle.value = '编辑爱好'
      Object.assign(hobbyForm, row)
      dialogVisible.value = true
    }
    
    const handleDeleteHobby = (id) => {
      ElMessageBox.confirm('确定要删除这个爱好吗？', '删除确认', {
        type: 'warning'
      }).then(async () => {
        try {
          await api.deleteHobby(id)
          ElMessage.success('删除成功')
          loadHobbies()
        } catch (err) {
          ElMessage.error('删除失败')
        }
      }).catch(() => {})
    }
    
    const handleImageUploadSuccess = (response) => {
      hobbyForm.coverUrl = extractUploadUrl(response)
      ElMessage.success('图片上传成功')
    }
    
    const handleImageUploadError = () => {
      ElMessage.error('图片上传失败')
    }
    
    const handleSaveHobby = async () => {
      if (!hobbyFormRef.value) return
      
      await hobbyFormRef.value.validate(async (valid) => {
        if (valid) {
          saveLoading.value = true
          try {
            const data = {
              name: hobbyForm.name,
              description: hobbyForm.description,
              coverUrl: hobbyForm.coverUrl
            }
            if (hobbyForm.id) {
              await api.updateHobby(hobbyForm.id, data)
              ElMessage.success('更新成功')
            } else {
              await api.createHobby(data)
              ElMessage.success('添加成功')
            }
            dialogVisible.value = false
            loadHobbies()
          } catch (err) {
            ElMessage.error('保存失败')
          } finally {
            saveLoading.value = false
          }
        }
      })
    }
    
    return {
      hobbies, loading, saveLoading, hobbyFormRef,
      dialogVisible, dialogTitle, hobbyForm, rules,
      uploadHeaders,
      handleAddHobby, handleEditHobby, handleDeleteHobby,
      handleSaveHobby, handleImageUploadSuccess, handleImageUploadError
    }
  }
}
</script>

<style scoped>
.admin-hobbies {
  padding: 20px;
}
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.avatar-uploader {
  border: 1px dashed #d9d9d9;
  border-radius: 6px;
  cursor: pointer;
  position: relative;
  overflow: hidden;
  width: 100px;
  height: 100px;
  display: flex;
  justify-content: center;
  align-items: center;
}
.avatar-uploader:hover {
  border-color: #409eff;
}
.avatar-uploader-icon {
  font-size: 28px;
  color: #8c939d;
}
.avatar {
  width: 100px;
  height: 100px;
  display: block;
  object-fit: cover;
}
.upload-tip {
  font-size: 12px;
  color: #909399;
  margin-top: 5px;
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