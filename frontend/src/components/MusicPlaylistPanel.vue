<template>
  <Transition name="playlist-slide">
    <div v-if="music.showPlaylist" class="playlist-mask" @click.self="music.closePlaylist">
      <div class="playlist-panel">
        <div class="playlist-head">
          <span class="playlist-title">播放列表 · {{ music.playlist.length }} 首</span>
          <button type="button" class="icon-btn" aria-label="关闭" @click="music.closePlaylist">✕</button>
        </div>
        <ul class="playlist-body">
          <li
            v-for="(track, i) in music.playlist"
            :key="i"
            class="playlist-row"
            :class="{ active: i === music.currentIndex }"
            @click="onSelect(i)"
          >
            <span class="row-idx">{{ i + 1 }}</span>
            <div class="row-info">
              <span class="row-title">{{ track.title }}</span>
              <span class="row-artist">{{ track.artist }}</span>
            </div>
            <span v-if="i === music.currentIndex && music.isPlaying" class="row-playing">♪</span>
          </li>
        </ul>
      </div>
    </div>
  </Transition>
</template>

<script setup>
import { useMusicStore } from '../stores/music'

const music = useMusicStore()

function onSelect(index) {
  music.selectTrack(index)
  music.closePlaylist()
}
</script>

<style scoped>
.playlist-mask {
  position: fixed;
  inset: 0;
  z-index: 260;
  background: rgba(15, 23, 42, 0.45);
  display: flex;
  align-items: flex-end;
  justify-content: center;
}

.playlist-panel {
  width: min(480px, 100%);
  max-height: min(70vh, 520px);
  background: var(--card-bg, #fff);
  border-radius: 16px 16px 0 0;
  box-shadow: 0 -8px 32px rgba(15, 23, 42, 0.2);
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.playlist-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16px 18px 12px;
  border-bottom: 1px solid var(--border-color, #eaeaea);
}

.playlist-title {
   font-weight: 600;
  color: var(--text-color, #111);
}

.icon-btn {
  border: none;
  background: transparent;
  cursor: pointer;
  font-size: 16px;
  color: var(--text-color, #333);
  padding: 4px 8px;
  border-radius: 6px;
}

.icon-btn:hover {
  background: var(--hover-color, #f0f0f0);
}

.playlist-body {
  list-style: none;
  margin: 0;
  padding: 8px 0 24px;
  overflow-y: auto;
}

.playlist-row {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 18px;
  cursor: pointer;
  transition: background 0.15s ease;
}

.playlist-row:hover {
  background: var(--hover-color, #f8fafc);
}

.playlist-row.active {
  background: rgba(99, 102, 241, 0.12);
}

.row-idx {
  width: 24px;
  text-align: center;
  font-size: 13px;
  color: #94a3b8;
  flex-shrink: 0;
}

.row-info {
  flex: 1;
  min-width: 0;
}

.row-title {
  display: block;
  font-size: 14px;
  font-weight: 500;
  color: var(--text-color, #111);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.row-artist {
  display: block;
  font-size: 12px;
  color: #64748b;
  margin-top: 2px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.row-playing {
  color: #6366f1;
  font-size: 14px;
  animation: pulse 1s ease-in-out infinite;
}

@keyframes pulse {
  0%, 100% { opacity: 1; }
  50% { opacity: 0.4; }
}

.playlist-slide-enter-active,
.playlist-slide-leave-active {
  transition: opacity 0.25s ease;
}

.playlist-slide-enter-active .playlist-panel,
.playlist-slide-leave-active .playlist-panel {
  transition: transform 0.28s ease;
}

.playlist-slide-enter-from,
.playlist-slide-leave-to {
  opacity: 0;
}

.playlist-slide-enter-from .playlist-panel,
.playlist-slide-leave-to .playlist-panel {
  transform: translateY(100%);
}
</style>
