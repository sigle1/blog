import api from '@/api'
import { defaultMusicPlaylist } from '@/config/musicPlaylist'
import { useMusicStore } from '@/stores/music'
import { normalizeImageUrl } from '@/utils/image'

/** 将后端曲目转为播放器格式 */
export function mapApiTrack(t) {
  return {
    id: t.id,
    title: t.title || '未命名',
    artist: t.artist || '',
    src: normalizeTrackSrc(t.src),
    cover: normalizeImageUrl(t.cover || ''),
    lrc: t.lrc || ''
  }
}

function normalizeTrackSrc(src) {
  if (!src || typeof src !== 'string') return ''
  const value = src.trim()
  if (!value) return ''
  if (/^https?:\/\//i.test(value)) return value
  return value.startsWith('/') ? value : `/${value}`
}

/** 从后端加载歌单；后台有数据则用后台，否则仅在开发环境保留演示曲目 */
export async function loadSiteMusicPlaylist() {
  const music = useMusicStore()
  music.playlistLoading = true
  try {
    const res = await api.getMusicTracks()
    const list = Array.isArray(res.data) ? res.data : (Array.isArray(res) ? res : [])
    if (list.length > 0) {
      music.initPlaylist(list.map(mapApiTrack))
      return list
    }
    if (import.meta.env.DEV) {
      music.initPlaylist([...defaultMusicPlaylist])
      music.playlistLoadError = '后台暂无曲目，开发环境使用演示歌单'
      return []
    }
    music.initPlaylist([])
    music.playlistLoadError = '后台暂无曲目，请在管理端上传音乐'
    return []
  } catch (e) {
    console.warn('加载站点歌单失败', e)
    const msg = e?.response?.data?.message || e?.message || '歌单接口不可用'
    if (import.meta.env.DEV) {
      music.initPlaylist([...defaultMusicPlaylist])
      music.setPlaylistLoadError(`后台歌单加载失败（${msg}），开发环境使用演示歌单`)
    } else {
      music.initPlaylist([])
      music.setPlaylistLoadError(msg)
    }
    return []
  } finally {
    music.playlistLoading = false
  }
}
