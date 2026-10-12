<template>
  <div class="publish-container">
    <h1 class="page-title">发布新笔记</h1>
    <p class="subtitle">在这里记录你的灵感与所学。</p>
    
    <el-card class="editor-card" v-loading="loading">
      <el-input 
        v-model="title" 
        placeholder="输入笔记标题..." 
        class="title-input-wrapper"
        input-style="font-size: 24px; font-weight: bold; background: transparent; border: none; border-bottom: 1px solid rgba(255, 255, 255, 0.2); padding-bottom: 15px; margin-bottom: 20px; color: #fff;"
      />
      
      <div class="quill-wrapper">
        <QuillEditor 
          v-model:content="content"
          theme="snow" 
          :toolbar="toolbarOptions"
          content-type="html"
          placeholder="在这里开始写作..."
          style="height: 400px;"
        />
      </div>

      <div class="form-actions">
        <el-button 
          type="primary" 
          size="large" 
          :loading="submitting" 
          @click="publish" 
          class="publish-btn"
        >
          确认发布
        </el-button>
        <el-button size="large" @click="router.back()">返回</el-button>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import api from '@/api'
import { QuillEditor } from '@vueup/vue-quill'
import '@vueup/vue-quill/dist/vue-quill.snow.css'
import { ElMessage } from 'element-plus'

const router = useRouter()
const title = ref('')
const content = ref('')
const loading = ref(false)
const submitting = ref(false)

// 定义富文本编辑器的工具栏选项
const toolbarOptions = [
  ['bold', 'italic', 'underline', 'strike'],
  ['blockquote', 'code-block'],
  [{ 'header': 1 }, { 'header': 2 }],
  [{ 'list': 'ordered'}, { 'list': 'bullet' }],
  [{ 'script': 'sub'}, { 'script': 'super' }],
  [{ 'indent': '-1'}, { 'indent': '+1' }],
  [{ 'direction': 'rtl' }],
  [{ 'size': ['small', false, 'large', 'huge'] }],
  [{ 'header': [1, 2, 3, 4, 5, 6, false] }],
  [{ 'color': [] }, { 'background': [] }],
  [{ 'font': [] }],
  [{ 'align': [] }],
  ['clean'],
  ['link', 'image', 'video']
];

const publish = async () => {
  if (!title.value.trim() || !content.value || content.value === '<p><br></p>') {
    ElMessage.warning('标题和内容都不能为空哦！')
    return
  }

  try {
    submitting.value = true
    await api.createNote({
      title: title.value.trim(),
      content: content.value
    })
    ElMessage.success('笔记发布成功！')
    router.push('/notes')
  } catch (error) {
    console.error('发布笔记失败:', error)
    ElMessage.error('发布失败，请稍后再试。')
  } finally {
    submitting.value = false
  }
}
</script>

<style>
/* 调整 Quill 编辑器的样式以适应我们的暗色主题 */
.quill-wrapper .ql-toolbar {
  border-top-left-radius: 8px;
  border-top-right-radius: 8px;
  border-color: rgba(255, 255, 255, 0.2) !important;
  background-color: rgba(255, 255, 255, 0.05);
}
.quill-wrapper .ql-container {
  border-bottom-left-radius: 8px;
  border-bottom-right-radius: 8px;
  border-color: rgba(255, 255, 255, 0.2) !important;
  font-size: 16px;
  color: #fff;
}
.quill-wrapper .ql-editor {
  padding: 20px;
}
.quill-wrapper .ql-editor::before {
  color: #888;
  font-style: normal !important;
}
.quill-wrapper .ql-snow .ql-stroke {
  stroke: #ccc;
}
.quill-wrapper .ql-snow .ql-picker-label {
  color: #ccc;
}
.quill-wrapper .ql-snow .ql-fill, .ql-snow .ql-stroke.ql-fill {
  fill: #ccc;
}
</style>

<style scoped>
.publish-container {
  max-width: 900px;
  margin: 0 auto;
  padding: 40px 20px;
  color: #fff;
}

.page-title {
  text-align: center;
  font-size: 36px;
  margin-bottom: 10px;
  text-shadow: 0 0 10px rgba(255, 255, 255, 0.5);
}

.subtitle {
  text-align: center;
  color: #ccc;
  margin-bottom: 40px;
  font-size: 16px;
}

.editor-card {
  background-color: rgba(0, 0, 0, 0.25) !important;
  backdrop-filter: blur(12px);
  border: 1px solid rgba(255, 255, 255, 0.1) !important;
  border-radius: 16px;
  padding: 20px;
}

.title-input-wrapper :deep(.el-input__wrapper) {
  background: transparent !important;
  box-shadow: none !important;
  padding: 0 !important;
}

.quill-wrapper {
  margin-bottom: 30px;
}

.form-actions {
  display: flex;
  justify-content: center;
  gap: 20px;
  margin-top: 20px;
}

.publish-btn {
  padding: 0 40px;
  font-weight: bold;
}
</style>
