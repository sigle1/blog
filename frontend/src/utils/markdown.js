import { marked } from 'marked'
import { normalizeImageUrl } from './image'

marked.setOptions({ gfm: true, breaks: false })

/**
 * 规范化 Markdown 图片语法中的相对路径，便于经 Vite/Nginx 代理访问。
 */
function normalizeMarkdownImageUrls(md) {
  return md.replace(/!\[([^\]]*)\]\(([^)\s]+)(\s+["'][^"']*["'])?\)/g, (full, alt, url, titlePart) => {
    const u = normalizeImageUrl(url.trim())
    return titlePart ? `![${alt}](${u}${titlePart})` : `![${alt}](${u})`
  })
}

/**
 * 将 HTML 中 img 的 src 规范为以 / 开头的站内路径或保留绝对 URL。
 */
function normalizeHtmlImgSrc(html) {
  if (!html || typeof html !== 'string') return ''
  return html.replace(/<img\b[^>]*\bsrc=["']([^"']+)["']/gi, (match, src) => {
    const u = normalizeImageUrl(src.trim())
    return match.replace(src, u)
  })
}

/** 文章正文：Markdown → HTML，并修正图片地址 */
export function renderArticleMarkdown(markdown) {
  if (!markdown) return ''
  const md = normalizeMarkdownImageUrls(String(markdown))
  const html = marked.parse(md)
  return normalizeHtmlImgSrc(html)
}
