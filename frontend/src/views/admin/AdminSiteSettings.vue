<template>
  <div class="admin-site-settings">
    <el-card v-loading="loading" shadow="hover">
      <template #header>
        <div class="card-header">
          <span>站点图标（浏览器标签）</span>
          <el-button type="primary" :loading="saving" @click="save">保存</el-button>
        </div>
      </template>

      <p class="hint">
        上传图片后保存，访客打开任意页面时标签栏会显示该图标。支持 PNG、JPG、GIF、WebP、SVG、ICO，建议正方形，边长 32～128 像素。
        留空并保存则恢复为默认 <code>/favicon.svg</code>。
      </p>

      <el-form label-width="120px">
        <el-form-item label="当前预览">
          <div class="preview-box">
            <img :src="previewSrc" alt="站点图标预览" class="preview-img" @error="onPreviewError">
            <span class="preview-label">标签栏效果示意</span>
          </div>
        </el-form-item>
        <el-form-item label="图标地址">
          <el-input
            v-model="form.faviconUrl"
            placeholder="/uploads/xxx.png 或完整 URL，留空用默认图标"
            clearable
          />
          <el-upload
            class="row-upload"
            action="/api/upload"
            :show-file-list="false"
            :headers="uploadHeaders"
            accept="image/*,.ico,.svg"
            :on-success="onUploadSuccess"
          >
            <el-button type="primary" plain size="small">上传图片并填入地址</el-button>
          </el-upload>
        </el-form-item>
        <el-form-item>
          <el-button plain @click="useDefault">恢复默认图标</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import api from '@/api'
import { extractUploadUrl, normalizeImageUrl } from '@/utils/image'
import { applyFavicon } from '@/utils/favicon'

const DEFAULT = '/favicon.svg'
const loading = ref(false)
const saving = ref(false)
const form = ref({ faviconUrl: '' })
const previewBroken = ref(false)

const uploadHeaders = computed(() => {
  const token = localStorage.getItem('adminToken')
  return token ? { Authorization: `Bearer ${token}` } : {}
})

const previewSrc = computed(() => {
  if (previewBroken.value) return DEFAULT
  const u = normalizeImageUrl(form.value.faviconUrl) || DEFAULT
  return `${u}${u.includes('?') ? '&' : '?'}t=${Date.now()}`
})

function onPreviewError() {
  previewBroken.value = true
}

function onUploadSuccess(response) {
  const url = extractUploadUrl(response)
  if (!url) {
    ElMessage.error('未获取到图片地址')
    return
  }
  form.value.faviconUrl = url
  previewBroken.value = false
  ElMessage.success('已填入，请点击保存生效')
}

function useDefault() {
  form.value.faviconUrl = ''
  previewBroken.value = false
}

async function load() {
  loading.value = true
  try {
    const res = await api.getSiteSettingsAdmin()
    form.value.faviconUrl = res.data?.faviconUrl || ''
    previewBroken.value = false
  } catch (e) {
    console.error(e)
    ElMessage.error('加载失败（若未建表请执行 migration_site_settings.sql）')
  } finally {
    loading.value = false
  }
}

async function save() {
  saving.value = true
  try {
    const faviconUrl = (form.value.faviconUrl || '').trim()
    await api.updateSiteSettings({ faviconUrl })
    ElMessage.success('已保存')
    await load()
    applyFavicon(faviconUrl || DEFAULT)
  } catch (e) {
    const msg = e?.response?.data?.message || e?.message
    ElMessage.error(msg ? `保存失败：${msg}` : '保存失败')
  } finally {
    saving.value = false
  }
}

onMounted(load)
</script>

<style scoped>
.admin-site-settings {
  max-width: 640px;
}
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.hint {
  font-size: 13px;
  color: #606266;
  line-height: 1.6;
  margin: 0 0 16px;
}
.hint code {
  font-size: 12px;
  padding: 1px 6px;
  background: #f4f4f5;
  border-radius: 4px;
}
.preview-box {
  display: flex;
  align-items: center;
  gap: 16px;
}
.preview-img {
  width: 48px;
  height: 48px;
  border-radius: 8px;
  object-fit: contain;
  background: #f5f7fa;
  border: 1px solid #ebeef5;
  padding: 4px;
}
.preview-label {
  font-size: 13px;
  color: #909399;
}
.row-upload {
  margin-top: 8px;
}
</style>
