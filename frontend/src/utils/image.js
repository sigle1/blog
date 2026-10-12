const ABSOLUTE_URL_REG = /^https?:\/\//i
const VIDEO_BANNER_EXT = /\.(mp4|webm|mov)(\?.*)?$/i

/** 首页轮播背景：根据 URL 判断是否为视频（与后端允许的扩展名一致） */
export function isVideoBannerUrl(url) {
  if (!url || typeof url !== 'string') return false
  return VIDEO_BANNER_EXT.test(url.trim())
}

/**
 * 相对地址规范化：与后台/管理端保存的字符串一致使用，不做路径改写。
 * 仅 trim；`http(s)`、`data:` 原样；其它相对路径若没有前导 `/` 则补一个，便于浏览器从站点根解析。
 */
export function normalizeImageUrl(url) {
  if (!url || typeof url !== 'string') return ''
  const value = url.trim()
  if (!value) return ''
  if (ABSOLUTE_URL_REG.test(value) || value.startsWith('data:')) return value
  return value.startsWith('/') ? value : `/${value}`
}

export function extractUploadUrl(response) {
  if (!response || typeof response !== 'object') return ''
  const rawUrl =
    response.filePath ||
    response.url ||
    response.data?.filePath ||
    response.data?.url ||
    response.data
  return normalizeImageUrl(rawUrl)
}
