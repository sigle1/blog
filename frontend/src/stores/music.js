import { defineStore } from 'pinia'

/** @typedef {'off' | 'one' | 'all'} RepeatMode */

export const useMusicStore = defineStore('music', {
  state: () => ({
    playlist: [],
    playlistLoaded: false,
    playlistLoading: false,
    playlistLoadError: '',
    currentIndex: 0,
    isPlaying: false,
    volume: 0.85,
    currentTime: 0,
    duration: 0,
    /** @type {RepeatMode} */
    repeatMode: 'all',
    showFullPlayer: false,
    showPlaylist: false,
    /** 迷你条是否可见（无曲目时可隐藏） */
    barVisible: true,
    /** @internal 由 SiteMusicPlayer 注册 */
    _seekHandler: null
  }),
  getters: {
    currentTrack(state) {
      return state.playlist[state.currentIndex] ?? null
    },
    progress(state) {
      if (!state.duration) return 0
      return Math.min(1, state.currentTime / state.duration)
    },
    repeatModeLabel(state) {
      if (state.repeatMode === 'one') return '单曲循环'
      if (state.repeatMode === 'all') return '列表循环'
      return '顺序播放'
    }
  },
  actions: {
    initPlaylist(list) {
      if (!Array.isArray(list)) return
      this.playlist = list
      this.currentIndex = 0
      this.isPlaying = false
      this.currentTime = 0
      this.duration = 0
      this.playlistLoaded = true
      this.playlistLoadError = ''
    },
    setPlaylistLoadError(message) {
      this.playlistLoaded = true
      this.playlistLoadError = message || '歌单加载失败'
    },
    play() {
      this.isPlaying = true
    },
    pause() {
      this.isPlaying = false
    },
    toggle() {
      this.isPlaying = !this.isPlaying
    },
    selectTrack(index) {
      if (index < 0 || index >= this.playlist.length) return
      this.currentIndex = index
      this.currentTime = 0
      this.isPlaying = true
    },
    next() {
      if (this.playlist.length === 0) return
      this.currentIndex = (this.currentIndex + 1) % this.playlist.length
      this.currentTime = 0
      this.isPlaying = true
    },
    prev() {
      if (this.playlist.length === 0) return
      if (this.currentTime > 3) {
        this.currentTime = 0
        return
      }
      this.currentIndex = (this.currentIndex - 1 + this.playlist.length) % this.playlist.length
      this.currentTime = 0
      this.isPlaying = true
    },
    toggleRepeatMode() {
      const order = /** @type {RepeatMode[]} */ (['off', 'all', 'one'])
      const i = order.indexOf(this.repeatMode)
      this.repeatMode = order[(i + 1) % order.length]
    },
    setVolume(v) {
      this.volume = Math.max(0, Math.min(1, v))
    },
    setTimes(current, total) {
      this.currentTime = current
      this.duration = total
    },
    openFullPlayer() {
      this.showFullPlayer = true
    },
    closeFullPlayer() {
      this.showFullPlayer = false
    },
    togglePlaylist() {
      this.showPlaylist = !this.showPlaylist
    },
    closePlaylist() {
      this.showPlaylist = false
    },
    registerSeekHandler(fn) {
      this._seekHandler = fn
    },
    unregisterSeekHandler() {
      this._seekHandler = null
    },
    seekByRatio(ratio) {
      if (typeof this._seekHandler === 'function') {
        this._seekHandler(ratio)
      }
    }
  }
})
