<template>
  <div class="admin-notes">
    <el-card shadow="hover" class="notes-card">
        <template #header>
          <div class="card-header">
            <span>笔记管理</span>
            <el-button type="primary" @click="handleAddNote">
              <el-icon><Plus /></el-icon>
              添加笔记
            </el-button>
          </div>
        </template>
        <el-table :data="notes" style="width: 100%" v-loading="loading">
          <el-table-column prop="id" label="ID" width="80" />
          <el-table-column prop="title" label="标题" />
          <el-table-column prop="createdAt" label="创建时间" width="180" />
          <el-table-column label="操作" width="180" fixed="right">
            <template #default="{ row }">
              <el-button size="small" type="primary" @click="handleEditNote(row)">
                <el-icon><Edit /></el-icon>
                编辑
              </el-button>
              <el-button size="small" type="danger" @click="handleDeleteNote(row.id)">
                <el-icon><Delete /></el-icon>
                删除
              </el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-card>
      
      <!-- 添加/编辑笔记对话框 -->
      <el-dialog
        v-model="dialogVisible"
        :title="dialogTitle"
        width="500px"
      >
      <el-form ref="noteFormRef" :model="noteForm" :rules="rules" label-width="80px">
        <el-form-item label="标题" prop="title">
          <el-input v-model="noteForm.title" placeholder="请输入笔记标题" />
        </el-form-item>
        <el-form-item label="内容" prop="content">
          <el-input
            v-model="noteForm.content"
            type="textarea"
            :rows="6"
            placeholder="请输入笔记内容"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" :loading="saveLoading" @click="handleSaveNote">保存</el-button>
        </span>
      </template>
      </el-dialog>
    </div>
</template>

<script>
import { ref, reactive, onMounted } from 'vue'
import { ElCard, ElTable, ElTableColumn, ElButton, ElDialog, ElForm, ElFormItem, ElInput, ElIcon, ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Edit, Delete } from '@element-plus/icons-vue'
import api from '@/api'

export default {
  name: 'AdminNotes',
  components: {
    ElCard,
    ElTable,
    ElTableColumn,
    ElButton,
    ElDialog,
    ElForm,
    ElFormItem,
    ElInput,
    ElIcon,
    Plus,
    Edit,
    Delete
  },
  setup() {
    const notes = ref([])
    const loading = ref(false)
    const saveLoading = ref(false)
    const noteFormRef = ref(null)
    const dialogVisible = ref(false)
    const dialogTitle = ref('添加笔记')
    
    const noteForm = reactive({
      id: '',
      title: '',
      content: ''
    })
    
    const rules = {
      title: [{ required: true, message: '请输入笔记标题', trigger: 'blur' }],
      content: [{ required: true, message: '请输入笔记内容', trigger: 'blur' }]
    }
    
    // 加载笔记列表
    const loadNotes = async () => {
      loading.value = true
      try {
        const response = await api.getNotes()
        let list = []
        if (response.data) {
          if (Array.isArray(response.data)) {
            list = response.data
          } else if (response.data.data && Array.isArray(response.data.data)) {
            list = response.data.data
          } else if (response.data.notes && Array.isArray(response.data.notes)) {
            list = response.data.notes
          }
        }
        notes.value = list
      } catch (err) {
        console.error('获取笔记列表失败:', err)
        ElMessage.error('获取笔记列表失败')
      } finally {
        loading.value = false
      }
    }

    onMounted(loadNotes)
    
    const handleAddNote = () => {
      dialogTitle.value = '添加笔记'
      Object.assign(noteForm, { id: '', title: '', content: '' })
      dialogVisible.value = true
      if (noteFormRef.value) noteFormRef.value.clearValidate()
    }
    
    const handleEditNote = (row) => {
      dialogTitle.value = '编辑笔记'
      Object.assign(noteForm, row)
      dialogVisible.value = true
    }
    
    const handleDeleteNote = (id) => {
      ElMessageBox.confirm('确定要删除这个笔记吗？', '删除确认', {
        type: 'warning'
      }).then(async () => {
        try {
          await api.deleteNote(id)
          ElMessage.success('删除成功')
          loadNotes()
        } catch (err) {
          ElMessage.error('删除失败')
        }
      }).catch(() => {})
    }
    
    const handleSaveNote = async () => {
      if (!noteFormRef.value) return
      
      await noteFormRef.value.validate(async (valid) => {
        if (valid) {
          saveLoading.value = true
          try {
            if (noteForm.id) {
              await api.updateNote(noteForm.id, noteForm)
              ElMessage.success('更新成功')
            } else {
              await api.createNote(noteForm)
              ElMessage.success('添加成功')
            }
            dialogVisible.value = false
            loadNotes()
          } catch (err) {
            ElMessage.error('保存失败')
          } finally {
            saveLoading.value = false
          }
        }
      })
    }
    
    return {
      notes, loading, saveLoading, noteFormRef,
      dialogVisible, dialogTitle, noteForm, rules,
      handleAddNote, handleEditNote, handleDeleteNote, handleSaveNote
    }
  }
}
</script>

<style scoped>
.admin-notes {
  width: 100%;
}

.notes-card {
  margin-bottom: 24px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}
</style>