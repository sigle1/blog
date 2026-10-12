<template>
  <audio
    v-if="music.playlist.length"
    ref="audioRef"
    preload="metadata"
    @timeupdate="onTimeUpdate"
    @loadedmetadata="onLoadedMetadata"
    @ended="onEnded"
    @error="onAudioError"
  />
</template>

<script setup>
import { ref, watch, onMounted, onBeforeUnmount } from 'vue'
import { useMusicStore } from '../stores/music'

const music = useMusicStore()
const audioRef = ref(null)

function loadSource() {
  const a = audioRef.value
  const src = music.currentTrack?.src
  if (!a || !src) return
  a.volume = music.volume
  if (a.src !== src) {
    a.src = src
    a.load()
  }
}

watch(
  () => [music.currentIndex, music.playlist],
  () => {
    loadSource()
    if (music.isPlaying) queueMicrotask(() => playWhenReady())
  },
  { deep: true }
)

watch(
  () => music.isPlaying,
  (v) => {
    const a = audioRef.value
    if (!a) return
    if (v) playWhenReady()
    else a.pause()
  }
)

watch(
  () => music.volume,
  (v) => {
    if (audioRef.value) audioRef.value.volume = v
  }
)

function playWhenReady() {
  const a = audioRef.value
  if (!a) return
  const p = a.play()
  if (p && typeof p.catch === 'function') {
    p.catch(() => music.pause())
  }
}

function onTimeUpdate() {
  const a = audioRef.value
  if (!a) return
  music.setTimes(a.currentTime, a.duration || music.duration)
}

function onLoadedMetadata() {
  const a = audioRef.value
  if (!a) return
  music.setTimes(a.currentTime, a.duration || 0)
}

function seekByRatio(ratio) {
  const a = audioRef.value
  if (!a || !music.duration) return
  a.currentTime = ratio * music.duration
  music.setTimes(a.currentTime, music.duration)
}

function onAudioError() {
  music.pause()
}

function onEnded() {
  if (music.repeatMode === 'one') {
    const a = audioRef.value
    if (a) {
      a.currentTime = 0
      void a.play().catch(() => music.pause())
    }
    return
  }
  if (music.repeatMode === 'all') {
    music.next()
    return
  }
  music.pause()
}

onMounted(() => {
  loadSource()
  music.registerSeekHandler(seekByRatio)
})

onBeforeUnmount(() => {
  music.unregisterSeekHandler()
})
</script>
