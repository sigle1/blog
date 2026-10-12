<template>
  <div class="admin-about">
    <el-card v-loading="loading" shadow="hover">
      <template #header>
        <div class="card-header">
          <span>关于页 · 自我介绍</span>
          <el-button type="primary" :loading="saving" @click="save">保存</el-button>
        </div>
      </template>
      <p class="hint">
        此处内容会显示在访客端 <strong>/about</strong> 的「自我介绍」区块。留空则使用前端内置默认文案。
      </p>
      <el-input
        v-model="intro"
        type="textarea"
        :rows="16"
        placeholder="支持换行；可写多段介绍、联系方式说明等"
        maxlength="20000"
        show-word-limit
      />
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import api from '@/api'

const intro = ref('')
const loading = ref(false)
const saving = ref(false)

async function load() {
  loading.value = true
  try {
    const res = await api.getAboutContentAdmin()
    const t = res.data && res.data.intro
    intro.value = typeof t === 'string' ? t : ''
  } catch (e) {
    console.error('[管理端关于页] 加载失败', e)
    ElMessage.error('加载失败：请确认后端已启动并已执行 about 表迁移 SQL')
  } finally {
    loading.value = false
  }
}

async function save() {
  saving.value = true
  try {
    await api.updateAboutContent({ intro: intro.value })
    ElMessage.success('已保存')
  } catch (e) {
    console.error('[管理端关于页] 保存失败', e)
    ElMessage.error('保存失败')
  } finally {
    saving.value = false
  }
}

onMounted(load)
</script>

<style scoped>
.admin-about {
  max-width: 900px;
}

.card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}

.hint {
  margin: 0 0 16px;
  font-size: 14px;
  line-height: 1.6;
  color: #64748b;
}
</style>
