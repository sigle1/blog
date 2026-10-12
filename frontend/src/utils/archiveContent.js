import api from '@/api'
import { normalizeImageUrl } from '@/utils/image'

export function stripHtml(html) {
  if (!html) return ''
  const s = String(html).replace(/<[^>]+>/g, ' ').replace(/\s+/g, ' ').trim()
  return s.length > 120 ? `${s.slice(0, 120)}…` : s
}

export function parseNoteList(raw) {
  if (!raw) return []
  if (Array.isArray(raw)) return raw
  if (raw.data && Array.isArray(raw.data)) return raw.data
  if (raw.notes && Array.isArray(raw.notes)) return raw.notes
  return []
}

export function toTime(value) {
  if (!value) return 0
  if (Array.isArray(value)) {
    const [y, m = 1, d = 1, hh = 0, mm = 0, ss = 0] = value
    return new Date(y, (m || 1) - 1, d, hh, mm, ss).getTime()
  }
  if (typeof value === 'number') return value
  const t = new Date(value).getTime()
  return Number.isNaN(t) ? 0 : t
}

export function getYearFromDate(value) {
  if (!value) return null
  if (Array.isArray(value)) return value[0] || null
  if (typeof value === 'number') return new Date(value).getFullYear()
  try {
    const y = new Date(value).getFullYear()
    return Number.isNaN(y) ? null : y
  } catch {
    return null
  }
}

export function formatDate(value) {
  if (!value) return ''
  if (Array.isArray(value)) {
    const [y, m = 1, d = 1, hh = 0, mm = 0, ss = 0] = value
    return new Date(y, (m || 1) - 1, d, hh, mm, ss).toLocaleDateString('zh-CN')
  }
  if (typeof value === 'number') {
    return new Date(value).toLocaleDateString('zh-CN')
  }
  try {
    return new Date(value).toLocaleDateString('zh-CN')
  } catch {
    return String(value)
  }
}

/** 拉取文章 + 笔记，合并为归档条目 */
export async function loadArchiveItems() {
  const [postsRes, notesRes] = await Promise.all([api.getPosts(), api.getNotes()])
  const posts = Array.isArray(postsRes.data) ? postsRes.data : []
  const notes = parseNoteList(notesRes.data)

  const fromPosts = posts.map((p) => ({
    kind: 'post',
    id: p.id,
    title: p.title,
    summary: (p.summary || '').trim() || stripHtml(p.content),
    coverImage: normalizeImageUrl(p.coverImage || p.coverUrl || p.cover_title),
    category: p.category && String(p.category).trim() ? p.category.trim() : '随笔',
    tags: p.tags,
    createdAt: p.createdAt,
    year: getYearFromDate(p.createdAt),
    path: `/post/${p.id}`
  }))

  const fromNotes = notes.map((n) => ({
    kind: 'note',
    id: n.id,
    title: n.title,
    summary: stripHtml(n.content),
    coverImage: '',
    category: n.category && String(n.category).trim() ? n.category.trim() : '学习笔记',
    tags: [],
    createdAt: n.createdAt,
    year: getYearFromDate(n.createdAt),
    path: `/notes/${n.id}`
  }))

  return [...fromPosts, ...fromNotes]
    .filter((it) => it.year)
    .sort((a, b) => toTime(b.createdAt) - toTime(a.createdAt))
}

/** 从条目列表得到年份及篇数，降序 */
export function groupByYear(items) {
  const map = new Map()
  for (const it of items) {
    if (!it.year) continue
    if (!map.has(it.year)) map.set(it.year, [])
    map.get(it.year).push(it)
  }
  return [...map.entries()]
    .sort((a, b) => b[0] - a[0])
    .map(([year, list]) => ({ year, count: list.length, items: list }))
}

export async function loadArchiveYears() {
  const items = await loadArchiveItems()
  return groupByYear(items).map((g) => g.year)
}
