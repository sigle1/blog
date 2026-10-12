<template>
  <Teleport to="body" :disabled="pageMode">
    <Transition name="full-player">
      <div v-if="visible" class="full-player" :class="{ 'is-page': pageMode }">
        <div class="full-player-bg" :style="bgStyle" />
        <div class="full-player-inner">
          <header class="full-head">
            <button v-if="!pageMode" type="button" class="head-btn" aria-label="收起" @click="emit('close')">▾</button>
            <span class="head-title">{{ pageMode ? '音乐角' : '正在播放' }}</span>
            <button type="button" class="head-btn" aria-label="播放列表" @click="music.togglePlaylist">☰</button>
          </header>

          <div class="cover-wrap">
            <div class="cover-disc" :class="{ spinning: music.isPlaying }">
              <img v-if="coverUrl" :src="coverUrl" alt="" class="cover-img" />
              <div v-else class="cover-placeholder">♪</div>
            </div>
          </div>

          <div class="track-info">
            <h2 class="track-title">{{ music.currentTrack?.title ?? '未选择曲目' }}</h2>
            <p class="track-artist">{{ music.currentTrack?.artist ?? '' }}</p>
          </div>

          <div v-if="lrcLines.length" class="lyric-box" ref="lyricBoxRef">
            <p
              v-for="(line, i) in lrcLines"
              :key="i"
              class="lyric-line"
              :class="{ active: i === activeLrcIndex }"
            >{{ line.text }}</p>
          </div>

          <div class="player-bottom">
            <div class="seek-row">
              <span class="time">{{ formatTime(music.currentTime) }}</span>
              <div class="seek-track" @click="onSeek">
                <div class="seek-fill" :style="{ width: pct + '%' }" />
              </div>
              <span class="time">{{ formatTime(music.duration) }}</span>
            </div>

            <div class="controls">
              <button type="button" class="ctrl" :title="music.repeatModeLabel" @click="music.toggleRepeatMode">
                {{ repeatIcon }}
              </button>
              <button type="button" class="ctrl" aria-label="上一首" @click="music.prev">⏮</button>
              <button type="button" class="ctrl play" aria-label="播放/暂停" @click="music.toggle">
                {{ music.isPlaying ? '⏸' : '▶' }}
              </button>
              <button type="button" class="ctrl" aria-label="下一首" @click="music.next">⏭</button>
              <button type="button" class="ctrl" aria-label="播放列表" @click="music.togglePlaylist">☆</button>
            </div>

            <label class="volume-row">
              <span>🔊</span>
              <input v-model.number="volProxy" type="range" min="0" max="100" step="1" />
            </label>
          </div>
        </div>
      </div>
    </Transition>
  </Teleport>
</template>

<script setup>
import { computed, ref, watch, onMounted } from 'vue'
import { useMusicStore } from '../stores/music'
import { formatTime } from '../utils/formatTime'
import { parseLrc, findLrcIndex } from '../utils/lrcParser'

const props = defineProps({
  visible: { type: Boolean, default: false },
  /** 嵌入 /music 页面时为 true，不显示收起按钮 */
  pageMode: { type: Boolean, default: false }
})

const emit = defineEmits(['close'])

const music = useMusicStore()
const lyricBoxRef = ref(null)
const lrcLines = ref([])
const activeLrcIndex = ref(-1)

const pct = computed(() => Math.round(music.progress * 100))

const volProxy = computed({
  get: () => Math.round(music.volume * 100),
  set: (n) => music.setVolume(n / 100)
})

const coverUrl = computed(() => music.currentTrack?.cover || '')

const bgStyle = computed(() => ({
  background: 'linear-gradient(160deg, rgba(99,102,241,0.85) 0%, rgba(15,23,42,0.92) 55%, rgba(0,0,0,0.95) 100%)'
}))

const repeatIcon = computed(() => {
  if (music.repeatMode === 'one') return '🔂'
  if (music.repeatMode === 'all') return '🔁'
  return '➡'
})

async function loadLyrics() {
  lrcLines.value = []
  activeLrcIndex.value = -1
  const track = music.currentTrack
  if (!track?.lrc) return
  try {
    let raw = track.lrc
    if (raw.startsWith('http') || raw.startsWith('/')) {
      const res = await fetch(raw)
      raw = await res.text()
    }
    lrcLines.value = parseLrc(raw)
  } catch {
    lrcLines.value = []
  }
}

watch(
  () => [music.currentIndex, music.currentTrack?.lrc],
  () => loadLyrics(),
  { immediate: true }
)

watch(
  () => music.currentTime,
  (t) => {
    activeLrcIndex.value = findLrcIndex(lrcLines.value, t)
    const box = lyricBoxRef.value
    const line = box?.querySelector('.lyric-line.active')
    if (line && box) {
      line.scrollIntoView({ block: 'center', behavior: 'smooth' })
    }
  }
)

function onSeek(e) {
  const rect = e.currentTarget.getBoundingClientRect()
  const ratio = Math.max(0, Math.min(1, (e.clientX - rect.left) / rect.width))
  music.seekByRatio(ratio)
}

onMounted(() => loadLyrics())
</script>

<style scoped>
.full-player {
  position: fixed;
  inset: 0;
  z-index: 250;
  color: #fff;
  overflow: hidden;
}

.full-player.is-page {
  position: relative;
  inset: auto;
  z-index: 1;
  min-height: calc(100vh - 120px);
  border-radius: 16px;
  margin: 0 auto;
  max-width: 480px;
}

.full-player-bg {
  position: absolute;
  inset: 0;
  filter: blur(0);
}

.full-player-inner {
  position: relative;
  z-index: 1;
  height: 100%;
  min-height: inherit;
  display: flex;
  flex-direction: column;
  padding: 12px 20px 28px;
}

.full-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  min-height: 44px;
}

.head-btn {
  border: none;
  background: rgba(255, 255, 255, 0.12);
  color: #fff;
  width: 36px;
  height: 36px;
  border-radius: 50%;
  cursor: pointer;
  font-size: 16px;
}

.head-btn:hover {
  background: rgba(255, 255, 255, 0.22);
}

.head-title {
  font-size: 14px;
  font-weight: 600;
  letter-spacing: 0.04em;
  opacity: 0.9;
}

.cover-wrap {
  display: flex;
  justify-content: center;
  padding: 24px 0 16px;
}

.cover-disc {
  width: min(240px, 62vw);
  height: min(240px, 62vw);
  border-radius: 50%;
  overflow: hidden;
  box-shadow: 0 16px 48px rgba(0, 0, 0, 0.45);
  border: 4px solid rgba(255, 255, 255, 0.15);
  background: rgba(255, 255, 255, 0.08);
}

.cover-disc.spinning {
  animation: spin 18s linear infinite;
}

@keyframes spin {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}

.cover-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.cover-placeholder {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 64px;
  opacity: 0.7;
}

.track-info {
  text-align: center;
  padding: 0 8px 12px;
}

.track-title {
  font-size: 20px;
  font-weight: 700;
  margin: 0 0 6px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.track-artist {
  margin: 0;
  font-size: 14px;
  opacity: 0.75;
}

.lyric-box {
  flex: 1;
  overflow-y: auto;
  text-align: center;
  padding: 8px 12px;
  min-height: 80px;
  max-height: 28vh;
  mask-image: linear-gradient(transparent, #000 12%, #000 88%, transparent);
}

.lyric-line {
  margin: 8px 0;
  font-size: 14px;
  opacity: 0.45;
  transition: opacity 0.2s ease, transform 0.2s ease;
}

.lyric-line.active {
  opacity: 1;
  font-size: 16px;
  font-weight: 600;
  transform: scale(1.02);
}

.player-bottom {
  margin-top: auto;
  padding-top: 12px;
}

.seek-row {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 16px;
}

.time {
  font-size: 11px;
  opacity: 0.75;
  width: 38px;
  flex-shrink: 0;
}

.seek-track {
  flex: 1;
  height: 4px;
  border-radius: 999px;
  background: rgba(255, 255, 255, 0.25);
  cursor: pointer;
  overflow: hidden;
}

.seek-fill {
  height: 100%;
  border-radius: 999px;
  background: #fff;
  pointer-events: none;
}

.controls {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 20px;
  margin-bottom: 14px;
}

.ctrl {
  border: none;
  background: transparent;
  color: #fff;
  font-size: 20px;
  cursor: pointer;
  padding: 8px;
  line-height: 1;
  opacity: 0.9;
}

.ctrl:hover {
  opacity: 1;
  transform: scale(1.08);
}

.ctrl.play {
  font-size: 36px;
  width: 56px;
  height: 56px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(255, 255, 255, 0.15);
  border-radius: 50%;
}

.volume-row {
  display: flex;
  align-items: center;
  gap: 10px;
  font-size: 14px;
  opacity: 0.85;
}

.volume-row input {
  flex: 1;
  accent-color: #fff;
}

.full-player-enter-active,
.full-player-leave-active {
  transition: opacity 0.3s ease, transform 0.3s ease;
}

.full-player-enter-from,
.full-player-leave-to {
  opacity: 0;
  transform: translateY(24px);
}
</style>
