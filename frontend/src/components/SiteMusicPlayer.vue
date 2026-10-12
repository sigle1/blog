<template>
  <div class="site-music-root">
    <div v-if="music.currentTrack && music.barVisible" class="music-bar">
      <button type="button" class="bar-info" @click="music.openFullPlayer">
        <div class="bar-cover" :class="{ spinning: music.isPlaying }">
          <img v-if="coverUrl" :src="coverUrl" alt="" />
          <span v-else class="cover-fallback">♪</span>
        </div>
        <div class="bar-text">
          <span class="bar-title">{{ music.currentTrack.title }}</span>
          <span class="bar-artist"> - {{ music.currentTrack.artist }}</span>
        </div>
      </button>

      <div class="bar-actions">
        <button type="button" class="ring-btn" aria-label="播放/暂停" @click="music.toggle">
          <svg class="ring-svg" viewBox="0 0 72 72">
            <circle cx="36" cy="36" r="32" class="ring-bg" />
            <circle
              cx="36" cy="36" r="32"
              class="ring-progress"
              :style="{ strokeDashoffset: ringOffset }"
            />
          </svg>
          <span class="ring-icon">{{ music.isPlaying ? '⏸' : '▶' }}</span>
        </button>
        <button type="button" class="list-btn" aria-label="播放列表" @click="music.togglePlaylist">☰</button>
      </div>
    </div>

    <MusicFullPlayer
      :visible="music.showFullPlayer"
      @close="music.closeFullPlayer"
    />
    <MusicPlaylistPanel />
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useMusicStore } from '../stores/music'
import MusicFullPlayer from './MusicFullPlayer.vue'
import MusicPlaylistPanel from './MusicPlaylistPanel.vue'

const music = useMusicStore()

const coverUrl = computed(() => music.currentTrack?.cover || '')

const ringOffset = computed(() => {
  const circumference = 2 * Math.PI * 32
  return circumference * (1 - music.progress)
})
</script>

<style scoped>
.site-music-root {
  --music-bar-height: 66px;
}

.music-bar {
  position: fixed;
  left: 0;
  right: 0;
  bottom: 0;
  z-index: 200;
  height: var(--music-bar-height);
  display: flex;
  align-items: center;
  padding: 0 16px 0 12px;
  gap: 8px;
  background: var(--card-bg, #fff);
  border-top: 1px solid var(--border-color, #eaeaea);
  box-shadow: 0 -4px 20px rgba(15, 23, 42, 0.08);
}

.dark-theme .music-bar {
  box-shadow: 0 -4px 20px rgba(0, 0, 0, 0.35);
}

.bar-info {
  flex: 1;
  min-width: 0;
  display: flex;
  align-items: center;
  gap: 12px;
  border: none;
  background: transparent;
  cursor: pointer;
  padding: 6px 4px;
  text-align: left;
}

.bar-cover {
  width: 44px;
  height: 44px;
  border-radius: 50%;
  overflow: hidden;
  flex-shrink: 0;
  background: linear-gradient(135deg, #6366f1, #a855f7);
  display: flex;
  align-items: center;
  justify-content: center;
}

.bar-cover.spinning {
  animation: spin 12s linear infinite;
}

@keyframes spin {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}

.bar-cover img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.cover-fallback {
  color: #fff;
  font-size: 18px;
}

.bar-text {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  font-size: 14px;
  color: var(--text-color, #111);
}

.bar-title {
  font-weight: 600;
}

.bar-artist {
  opacity: 0.65;
  font-size: 13px;
}

.bar-actions {
  display: flex;
  align-items: center;
  gap: 12px;
  flex-shrink: 0;
}

.ring-btn {
  position: relative;
  width: 48px;
  height: 48px;
  border: none;
  background: transparent;
  cursor: pointer;
  padding: 0;
}

.ring-svg {
  width: 100%;
  height: 100%;
  transform: rotate(-90deg);
}

.ring-bg {
  fill: none;
  stroke: var(--border-color, #e5e7eb);
  stroke-width: 3;
}

.ring-progress {
  fill: none;
  stroke: #6366f1;
  stroke-width: 3;
  stroke-linecap: round;
  stroke-dasharray: 201;
  transition: stroke-dashoffset 0.15s linear;
}

.ring-icon {
  position: absolute;
  inset: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 16px;
  color: var(--text-color, #333);
}

.list-btn {
  border: none;
  background: transparent;
  cursor: pointer;
  font-size: 20px;
  color: var(--text-color, #333);
  padding: 8px;
  line-height: 1;
}

.list-btn:hover {
  color: #6366f1;
}
</style>
