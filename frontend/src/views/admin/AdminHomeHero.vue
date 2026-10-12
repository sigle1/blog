<template>
  <div class="admin-home-hero">
    <el-card v-loading="loading" shadow="hover">
      <template #header>
        <div class="card-header">
          <span>首页壁纸与标题</span>
          <el-button type="primary" :loading="saving" @click="save">保存</el-button>
        </div>
      </template>
      <p class="hint">
        每行一条背景地址（与保存到数据库的字符串一致；上传接口返回的 <code>/uploads/...</code> 会直接使用）。支持<strong>静图</strong>、<strong>动图</strong>（<code>.gif</code>、动图 <code>.webp</code>）与<strong>视频</strong>混排轮播；动图走图片上传即可，首页会用 <code>&lt;img&gt;</code> 自动播放动画。
        视频请使用 <code>.mp4</code> / <code>.webm</code> / <code>.mov</code>（单文件不超过 120MB，与 Spring <code>multipart</code> 上限一致）。
        <strong>壁纸引擎里带 <code>scene.pkg</code> 的场景壁纸</strong>没有通用网页格式，不能直接上传还原同款效果；若只有 <code>preview.gif</code>，可以上传但通常分辨率较低。要高清又流畅，请改用录屏/导出得到的视频，或接受高清静图。
      </p>

      <el-form label-width="120px" class="hero-form">
        <el-form-item label="主标题">
          <el-input v-model="form.heroTitle" placeholder="首页大图中央标题，如：琉璃月" maxlength="64" show-word-limit />
        </el-form-item>
        <el-form-item label="引导页标题">
          <el-input v-model="form.introTitle" placeholder="首次进入站点时的欢迎标题" maxlength="128" show-word-limit />
        </el-form-item>
        <el-form-item label="背景轮播">
          <el-input v-model="form.bannerText" type="textarea" :rows="5" placeholder="每行一个地址：静图、GIF/WebP 动图或视频 URL（/uploads/xxx 或外链）" />
          <el-upload
            class="row-upload"
            action="/api/upload"
            :show-file-list="false"
            :headers="uploadHeaders"
            accept="image/*,video/mp4,video/webm,video/quicktime,.mp4,.webm,.mov"
            :on-success="onBannerUpload"
          >
            <el-button type="primary" plain size="small">上传图片、动图或视频并追加链接</el-button>
          </el-upload>
        </el-form-item>
        <el-form-item label="副标题轮播">
          <el-input v-model="form.subtitlesText" type="textarea" :rows="6" placeholder="每行一句，会在大图下方轮播显示" />
        </el-form-item>

        <el-divider content-position="left">首页侧栏作者卡片</el-divider>
        <el-form-item label="显示昵称">
          <el-input v-model="form.authorDisplayName" placeholder="如：黑虎阿昊" maxlength="64" show-word-limit />
        </el-form-item>
        <el-form-item label="签名/格言">
          <el-input v-model="form.authorMotto" type="textarea" :rows="2" placeholder="侧栏一句话介绍" maxlength="512" show-word-limit />
        </el-form-item>
        <el-form-item label="头像">
          <div class="avatar-row">
            <el-input v-model="form.authorAvatar" placeholder="/uploads/xxx 或完整图片 URL，留空则用默认头像" />
            <el-upload
              class="avatar-upload"
              action="/api/upload"
              :show-file-list="false"
              :headers="uploadHeaders"
              accept="image/*"
              :on-success="onAvatarUpload"
            >
              <el-button type="primary" plain size="small">上传头像</el-button>
            </el-upload>
          </div>
          <p class="hint subtle">文章数 / 分类数（爱好数）/ 评论数由数据库自动统计，无需填写。</p>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { reactive, ref, onMounted, computed } from 'vue'
import { ElMessage } from 'element-plus'
import api from '@/api'
import { extractUploadUrl, normalizeImageUrl } from '@/utils/image'

const form = reactive({
  heroTitle: '',
  introTitle: '',
  bannerText: '',
  subtitlesText: '',
  authorDisplayName: '',
  authorMotto: '',
  authorAvatar: ''
})

const saving = ref(false)
const loading = ref(false)

const uploadHeaders = computed(() => {
  const token = localStorage.getItem('adminToken')
  return token ? { Authorization: `Bearer ${token}` } : {}
})

function onBannerUpload(response) {
  const url = extractUploadUrl(response)
  if (!url) {
    ElMessage.error('未获取到图片地址')
    return
  }
  const line = form.bannerText.trim()
  form.bannerText = line ? `${line}\n${url}` : url
  ElMessage.success('已追加到壁纸列表')
}

function onAvatarUpload(response) {
  const url = extractUploadUrl(response)
  if (!url) {
    ElMessage.error('未获取到头像地址')
    return
  }
  form.authorAvatar = url
  ElMessage.success('已设置头像地址')
}

async function load() {
  loading.value = true
  try {
    const res = await api.getHomeHeroAdmin()
    const c = res.data || {}
    form.heroTitle = c.heroTitle || ''
    form.introTitle = c.introTitle || ''
    form.bannerText = Array.isArray(c.bannerImages)
      ? c.bannerImages.map((u) => normalizeImageUrl(String(u))).filter(Boolean).join('\n')
      : ''
    form.subtitlesText = Array.isArray(c.heroSubtitles) ? c.heroSubtitles.join('\n') : ''
    form.authorDisplayName = c.authorDisplayName || ''
    form.authorMotto = c.authorMotto || ''
    form.authorAvatar = normalizeImageUrl(c.authorAvatar || '')
  } catch (e) {
    console.error(e)
    ElMessage.error('加载配置失败（若尚未建表，请在后端执行 migration_home_hero_settings.sql）')
  } finally {
    loading.value = false
  }
}

async function save() {
  const heroTitle = form.heroTitle.trim()
  if (!heroTitle) {
    ElMessage.warning('请填写主标题')
    return
  }
  const bannerImages = form.bannerText
    .split(/\r?\n/)
    .map((s) => normalizeImageUrl(s.trim()))
    .filter(Boolean)
  const heroSubtitles = form.subtitlesText.split(/\r?\n/).map((s) => s.trim()).filter(Boolean)
  if (bannerImages.length === 0) {
    ElMessage.warning('请至少填写一条背景地址（图片或视频），或上传文件')
    return
  }
  if (heroSubtitles.length === 0) {
    ElMessage.warning('请至少填写一句副标题')
    return
  }
  saving.value = true
  try {
    await api.updateHomeHero({
      heroTitle,
      introTitle: form.introTitle.trim() || '欢迎来到琉璃月',
      bannerImages,
      heroSubtitles,
      authorDisplayName: form.authorDisplayName.trim(),
      authorMotto: form.authorMotto.trim(),
      authorAvatar: normalizeImageUrl(form.authorAvatar.trim())
    })
    ElMessage.success('已保存，刷新首页即可看到效果')
    await load()
  } catch (e) {
    console.error(e)
    const msg =
      e?.response?.data?.message ||
      e?.response?.data?.error ||
      (typeof e?.message === 'string' ? e.message : '')
    ElMessage.error(msg ? `保存失败：${msg}` : '保存失败')
  } finally {
    saving.value = false
  }
}

onMounted(load)
</script>

<style scoped>
.admin-home-hero {
  max-width: 800px;
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
.row-upload {
  margin-top: 8px;
}
.hero-form :deep(.el-textarea__inner) {
  font-family: ui-monospace, monospace;
  font-size: 13px;
}
.avatar-row {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 10px;
  width: 100%;
}
.avatar-row .el-input {
  flex: 1;
  min-width: 200px;
}
.hint.subtle {
  margin: 8px 0 0;
  font-size: 12px;
  color: #909399;
}
</style>
