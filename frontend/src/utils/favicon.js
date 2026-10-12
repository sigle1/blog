import { normalizeImageUrl } from '@/utils/image'

const DEFAULT_FAVICON = '/favicon.svg'

function mimeFromUrl(url) {
  const path = (url || '').split('?')[0].toLowerCase()
  if (path.endsWith('.svg')) return 'image/svg+xml'
  if (path.endsWith('.ico')) return 'image/x-icon'
  if (path.endsWith('.jpg') || path.endsWith('.jpeg')) return 'image/jpeg'
  if (path.endsWith('.gif')) return 'image/gif'
  if (path.endsWith('.webp')) return 'image/webp'
  return 'image/png'
}

/** 更新浏览器标签页图标（带时间戳避免缓存） */
export function applyFavicon(url) {
  const base = normalizeImageUrl(url) || DEFAULT_FAVICON
  const href = `${base}${base.includes('?') ? '&' : '?'}v=${Date.now()}`
  const type = mimeFromUrl(base)

  const rels = ['icon', 'shortcut icon', 'apple-touch-icon']
  rels.forEach((rel) => {
    let el = document.querySelector(`link[rel="${rel}"]`)
    if (!el) {
      el = document.createElement('link')
      el.rel = rel
      document.head.appendChild(el)
    }
    el.href = href
    if (rel === 'icon' || rel === 'shortcut icon') {
      el.type = type
    }
  })
}

export async function loadAndApplySiteFavicon(api) {
  try {
    const res = await api.getSiteSettings()
    const url = res.data?.faviconUrl
    applyFavicon(url || DEFAULT_FAVICON)
  } catch {
    applyFavicon(DEFAULT_FAVICON)
  }
}
