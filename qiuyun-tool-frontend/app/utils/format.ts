/**
 * 格式化数字（转换为 w/k 格式）
 * @param num 数字
 * @param options 配置选项
 * @returns 格式化后的字符串
 * 
 * @example
 * formatNumber(1500) // '1.5k'
 * formatNumber(15000) // '1.5w'
 * formatNumber(1500, { suffix: '+' }) // '1.5k+'
 */
export function formatNumber(
  num: number,
  options: { decimals?: number; suffix?: string } = {}
): string {
  const { decimals = 1, suffix = '' } = options

  if (num >= 10000) {
    return (num / 10000).toFixed(decimals).replace(/\.0$/, '') + 'w' + suffix
  }
  if (num >= 1000) {
    return (num / 1000).toFixed(decimals).replace(/\.0$/, '') + 'k' + suffix
  }
  return num + suffix
}

/**
 * 格式化访问数（简化版，不带小数）
 * @param visits 访问数
 * @returns 格式化后的字符串
 * 
 * @example
 * formatVisits(1500) // '1.5k'
 * formatVisits(15000) // '1.5w'
 */
export function formatVisits(visits: number): string {
  if (visits >= 10000) {
    return (visits / 10000).toFixed(1) + 'w'
  }
  if (visits >= 1000) {
    return (visits / 1000).toFixed(1) + 'k'
  }
  return visits.toString()
}

/**
 * 格式化文件大小
 * @param bytes 字节数
 * @returns 格式化后的字符串
 * 
 * @example
 * formatFileSize(1024) // '1 KB'
 * formatFileSize(1024 * 1024) // '1 MB'
 */
export function formatFileSize(bytes: number): string {
  if (bytes === 0) return '0 B'
  const k = 1024
  const sizes = ['B', 'KB', 'MB', 'GB', 'TB']
  const i = Math.floor(Math.log(bytes) / Math.log(k))
  return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i]
}

/**
 * 格式化日期
 * @param date 日期
 * @returns 格式化后的字符串
 * 
 * @example
 * formatDate(new Date()) // '2024-01-01'
 */
export function formatDate(date: Date | string): string {
  const d = typeof date === 'string' ? new Date(date) : date
  return d.toLocaleDateString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit'
  })
}

/**
 * 格式化时间
 * @param date 日期
 * @returns 格式化后的字符串
 * 
 * @example
 * formatTime(new Date()) // '14:30:00'
 */
export function formatTime(date: Date | string): string {
  const d = typeof date === 'string' ? new Date(date) : date
  return d.toLocaleTimeString('zh-CN', {
    hour: '2-digit',
    minute: '2-digit',
    second: '2-digit'
  })
}

/**
 * 格式化日期时间
 * @param date 日期
 * @returns 格式化后的字符串
 * 
 * @example
 * formatDateTime(new Date()) // '2024-01-01 14:30:00'
 */
export function formatDateTime(date: Date | string): string {
  return `${formatDate(date)} ${formatTime(date)}`
}
