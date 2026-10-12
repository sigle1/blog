<template>
  <div class="admin-music">
    <el-card shadow="hover">
      <template #header>
        <div class="card-header">
          <span>音乐管理</span>
          <el-button type="primary" @click="showUpload = true">上传曲目</el-button>
        </div>
      </template>

      <p class="hint">
        上传的曲目会出现在博客底部播放器与「音乐」页面。支持 MP3、M4A、OGG、WAV 等；可选封面与 LRC 歌词。
        若列表为空，前台将使用默认演示歌单。
      </p>

      <el-table v-loading="loading" :data="tracks" stripe empty-text="暂无曲目，点击右上角上传">
        <el-table-column label="#" width="50">
          <template #default="{ $index }">{{ $index + 1 }}</template>
        </el-table-column>
        <el-table-column label="封面" width="72">
          <template #default="{ row }">
            <img v-if="row.cover" :src="normalizeUrl(row.cover)" alt="" class="cover-thumb" />
            <span v-else class="cover-placeholder">♪</span>
          </template>
        </el-table-column>
        <el-table-column prop="title" label="标题" min-width="140" show-overflow-tooltip />
        <el-table-column prop="artist" label="歌手" min-width="100" show-overflow-tooltip />
        <el-table-column label="排序" width="120">
          <template #default="{ row, $index }">
            <el-button link :disabled="$index === 0" @click="moveUp($index)">↑</el-button>
            <el-button link :disabled="$index === tracks.length - 1" @click="moveDown($index)">↓</el-button>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="160" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="openEdit(row)">编辑</el-button>
            <el-button link type="danger" @click="remove(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 上传 -->
    <el-dialog v-model="showUpload" title="上传曲目" width="480px" destroy-on-close @closed="resetUpload">
      <el-form label-width="80px">
        <el-form-item label="音频" required>
          <input ref="audioInputRef" type="file" accept="audio/*,.mp3,.m4a,.ogg,.wav,.flac" @change="onAudioPick" />
          <div v-if="uploadForm.file" class="file-name">{{ uploadForm.file.name }}</div>
        </el-form-item>
        <el-form-item label="标题">
          <el-input v-model="uploadForm.title" placeholder="留空则使用文件名" />
        </el-form-item>
        <el-form-item label="歌手">
          <el-input v-model="uploadForm.artist" placeholder="可选" />
        </el-form-item>
        <el-form-item label="封面">
          <input type="file" accept="image/*" @change="onCoverPick" />
        </el-form-item>
        <el-form-item label="歌词">
          <input type="file" accept=".lrc,.txt" @change="onLrcPick" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showUpload = false">取消</el-button>
        <el-button type="primary" :loading="uploading" @click="submitUpload">上传</el-button>
      </template>
    </el-dialog>

    <!-- 编辑 -->
    <el-dialog v-model="showEdit" title="编辑曲目" width="480px" destroy-on-close>
      <el-form v-if="editForm" label-width="80px">
        <el-form-item label="标题">
          <el-input v-model="editForm.title" />
        </el-form-item>
        <el-form-item label="歌手">
          <el-input v-model="editForm.artist" />
        </el-form-item>
        <el-form-item label="封面 URL">
          <el-input v-model="editForm.coverUrl" placeholder="/uploads/music/covers/..." />
        </el-form-item>
        <el-form-item label="歌词 URL">
          <el-input v-model="editForm.lrcPath" placeholder="/uploads/music/lrc/..." />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showEdit = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="submitEdit">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import api from '@/api'
import { normalizeImageUrl } from '@/utils/image'

const loading = ref(false)
const uploading = ref(false)
const saving = ref(false)
const tracks = ref([])
const showUpload = ref(false)
const showEdit = ref(false)
const editForm = ref(null)
const audioInputRef = ref(null)

const uploadForm = ref({
  file: null,
  cover: null,
  lrc: null,
  title: '',
  artist: ''
})

function normalizeUrl(url) {
  return normalizeImageUrl(url) || url
}

async function load() {
  loading.value = true
  try {
    const res = await api.getMusicTracksAdmin()
    tracks.value = Array.isArray(res.data) ? res.data : []
  } catch (e) {
    console.error(e)
    ElMessage.error('加载失败（请先执行 migration_site_music_track.sql）')
  } finally {
    loading.value = false
  }
}

function onAudioPick(e) {
  uploadForm.value.file = e.target.files?.[0] || null
}

function onCoverPick(e) {
  uploadForm.value.cover = e.target.files?.[0] || null
}

function onLrcPick(e) {
  uploadForm.value.lrc = e.target.files?.[0] || null
}

function resetUpload() {
  uploadForm.value = { file: null, cover: null, lrc: null, title: '', artist: '' }
  if (audioInputRef.value) audioInputRef.value.value = ''
}

async function submitUpload() {
  if (!uploadForm.value.file) {
    ElMessage.warning('请选择音频文件')
    return
  }
  uploading.value = true
  try {
    const fd = new FormData()
    fd.append('file', uploadForm.value.file)
    if (uploadForm.value.title.trim()) fd.append('title', uploadForm.value.title.trim())
    if (uploadForm.value.artist.trim()) fd.append('artist', uploadForm.value.artist.trim())
    if (uploadForm.value.cover) fd.append('cover', uploadForm.value.cover)
    if (uploadForm.value.lrc) fd.append('lrc', uploadForm.value.lrc)
    await api.uploadMusicTrack(fd)
    ElMessage.success('上传成功')
    showUpload.value = false
    await load()
    const { loadSiteMusicPlaylist } = await import('@/utils/musicPlaylist')
    await loadSiteMusicPlaylist()
  } catch (e) {
    const msg = e?.response?.data?.message || e?.message
    ElMessage.error(msg || '上传失败')
  } finally {
    uploading.value = false
  }
}

function openEdit(row) {
  editForm.value = {
    id: row.id,
    title: row.title,
    artist: row.artist || '',
    coverUrl: row.cover || '',
    lrcPath: row.lrc || ''
  }
  showEdit.value = true
}

async function submitEdit() {
  if (!editForm.value?.title?.trim()) {
    ElMessage.warning('标题不能为空')
    return
  }
  saving.value = true
  try {
    await api.updateMusicTrack(editForm.value.id, {
      title: editForm.value.title.trim(),
      artist: editForm.value.artist.trim(),
      coverUrl: editForm.value.coverUrl.trim(),
      lrcPath: editForm.value.lrcPath.trim()
    })
    ElMessage.success('已保存')
    showEdit.value = false
    await load()
  } catch (e) {
    const msg = e?.response?.data?.message || e?.message
    ElMessage.error(msg || '保存失败')
  } finally {
    saving.value = false
  }
}

async function remove(row) {
  try {
    await ElMessageBox.confirm(`确定删除「${row.title}」？`, '删除曲目', { type: 'warning' })
    await api.deleteMusicTrack(row.id)
    ElMessage.success('已删除')
    await load()
    const { loadSiteMusicPlaylist } = await import('@/utils/musicPlaylist')
    await loadSiteMusicPlaylist()
  } catch (e) {
    if (e !== 'cancel') {
      ElMessage.error(e?.response?.data?.message || '删除失败')
    }
  }
}

async function saveOrder() {
  const ids = tracks.value.map((t) => t.id)
  await api.reorderMusicTracks(ids)
}

async function moveUp(index) {
  if (index <= 0) return
  const list = [...tracks.value]
  ;[list[index - 1], list[index]] = [list[index], list[index - 1]]
  tracks.value = list
  try {
    await saveOrder()
  } catch {
    ElMessage.error('排序保存失败')
    await load()
  }
}

async function moveDown(index) {
  if (index >= tracks.value.length - 1) return
  const list = [...tracks.value]
  ;[list[index], list[index + 1]] = [list[index + 1], list[index]]
  tracks.value = list
  try {
    await saveOrder()
  } catch {
    ElMessage.error('排序保存失败')
    await load()
  }
}

onMounted(load)
</script>

<style scoped>
.admin-music {
  max-width: 960px;
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
.cover-thumb {
  width: 40px;
  height: 40px;
  border-radius: 6px;
  object-fit: cover;
}
.cover-placeholder {
  display: inline-flex;
  width: 40px;
  height: 40px;
  align-items: center;
  justify-content: center;
  background: #f0f2f5;
  border-radius: 6px;
  color: #909399;
}
.file-name {
  font-size: 12px;
  color: #909399;
  margin-top: 6px;
}
</style>
