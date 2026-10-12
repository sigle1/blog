<template>
  <div class="music-page">
    <div v-if="loading" class="music-state">
      <p class="state-title">正在加载歌单…</p>
      <p class="state-hint">请求 /api/music/tracks</p>
    </div>

    <div v-else-if="!music.playlist.length" class="music-state music-empty">
      <p class="state-title">暂无音乐</p>
      <p v-if="music.playlistLoadError" class="state-error">{{ music.playlistLoadError }}</p>
      <p class="state-hint">
        请先在管理后台「音乐管理」上传曲目，并确认数据库已执行
        <code>migration_site_music_track.sql</code>。
      </p>
      <el-button type="primary" :loading="loading" @click="reload">重新加载</el-button>
    </div>

    <template v-else>
      <MusicFullPlayer :visible="true" page-mode />
      <section class="track-section">
        <h2 class="section-title">播放列表 · {{ music.playlist.length }} 首</h2>
        <ul class="track-list">
          <li
            v-for="(track, i) in music.playlist"
            :key="track.id ?? i"
            class="track-row"
            :class="{ active: i === music.currentIndex }"
            @click="music.selectTrack(i)"
          >
            <span class="idx">{{ i + 1 }}</span>
            <span class="title">{{ track.title }}</span>
            <span class="artist">{{ track.artist }}</span>
            <span v-if="i === music.currentIndex && music.isPlaying" class="playing">播放中</span>
          </li>
        </ul>
      </section>
    </template>

    <MusicPlaylistPanel />
  </div>
</template>

<script setup>
import { ref, onMounted, onBeforeUnmount } from 'vue'
import { useMusicStore } from '../stores/music'
import { loadSiteMusicPlaylist } from '../utils/musicPlaylist'
import MusicFullPlayer from '../components/MusicFullPlayer.vue'
import MusicPlaylistPanel from '../components/MusicPlaylistPanel.vue'

const music = useMusicStore()
const loading = ref(false)

async function reload() {
  loading.value = true
  try {
    await loadSiteMusicPlaylist()
  } finally {
    loading.value = false
  }
}

onMounted(async () => {
  music.barVisible = false
  await reload()
})

onBeforeUnmount(() => {
  music.barVisible = true
})
</script>

<style scoped>
.music-page {
  max-width: 560px;
  margin: 0 auto;
  padding: 16px 16px 48px;
  min-height: calc(100vh - 180px);
}

.music-state {
  margin: 48px auto;
  max-width: 420px;
  padding: 32px 24px;
  text-align: center;
  background: var(--card-bg, #fff);
  border: 1px solid var(--border-color, #eaeaea);
  border-radius: 12px;
  box-shadow: 0 4px 20px rgba(15, 23, 42, 0.06);
}

.state-title {
  font-size: 18px;
  font-weight: 600;
  color: var(--text-color, #111);
  margin: 0 0 12px;
}

.state-error {
  font-size: 13px;
  color: #dc2626;
  margin: 0 0 10px;
  line-height: 1.5;
}

.state-hint {
  font-size: 13px;
  color: #64748b;
  line-height: 1.6;
  margin: 0 0 16px;
}

.state-hint code {
  font-size: 12px;
  padding: 1px 6px;
  background: #f1f5f9;
  border-radius: 4px;
}

.track-section {
  margin-top: 20px;
  padding: 16px;
  background: var(--card-bg, #fff);
  border: 1px solid var(--border-color, #eaeaea);
  border-radius: 12px;
}

.section-title {
  font-size: 15px;
  font-weight: 600;
  margin: 0 0 12px;
  color: var(--text-color, #111);
}

.track-list {
  list-style: none;
  margin: 0;
  padding: 0;
}

.track-row {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 10px 8px;
  border-radius: 8px;
  cursor: pointer;
  transition: background 0.15s ease;
}

.track-row:hover {
  background: var(--hover-color, #f8fafc);
}

.track-row.active {
  background: rgba(99, 102, 241, 0.12);
}

.idx {
  width: 24px;
  text-align: center;
  font-size: 12px;
  color: #94a3b8;
  flex-shrink: 0;
}

.title {
  flex: 1;
  font-size: 14px;
  font-weight: 500;
  color: var(--text-color, #111);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.artist {
  font-size: 12px;
  color: #64748b;
  max-width: 100px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.playing {
  font-size: 11px;
  padding: 2px 8px;
  border-radius: 999px;
  background: #6366f1;
  color: #fff;
  flex-shrink: 0;
}
</style>
