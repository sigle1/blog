const TIME_REG = /\[(\d{2}):(\d{2})(?:\.(\d{1,3}))?\]/g

/**
 * 解析 LRC 歌词，返回按时间排序的行
 * @param {string} raw
 * @returns {{ time: number, text: string }[]}
 */
export function parseLrc(raw) {
  if (!raw || typeof raw !== 'string') return []
  const lines = raw.split(/\r?\n/)
  const items = []

  for (const line of lines) {
    const text = line.replace(TIME_REG, '').trim()
    if (!text) continue
    let match
    TIME_REG.lastIndex = 0
    while ((match = TIME_REG.exec(line)) !== null) {
      const min = Number(match[1])
      const sec = Number(match[2])
      const ms = match[3] ? Number(match[3].padEnd(3, '0')) : 0
      items.push({ time: min * 60 + sec + ms / 1000, text })
    }
  }

  return items.sort((a, b) => a.time - b.time)
}

/** 根据当前播放时间找到应高亮的歌词行索引 */
export function findLrcIndex(items, currentTime) {
  if (!items.length) return -1
  let idx = -1
  for (let i = 0; i < items.length; i++) {
    if (items[i].time <= currentTime) idx = i
    else break
  }
  return idx
}
