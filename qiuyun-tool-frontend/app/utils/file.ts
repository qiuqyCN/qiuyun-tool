/**
 * 生成时间戳字符串
 * 格式: YYYYMMDDhhmmss (年月日时分秒)
 * @returns 时间戳字符串，如 "20250312143025"
 */
export function generateTimestamp(): string {
  const now = new Date()
  return now.getFullYear().toString() +
    String(now.getMonth() + 1).padStart(2, '0') +
    String(now.getDate()).padStart(2, '0') +
    String(now.getHours()).padStart(2, '0') +
    String(now.getMinutes()).padStart(2, '0') +
    String(now.getSeconds()).padStart(2, '0')
}

/**
 * 生成带时间戳的文件名
 * @param toolName 工具名称
 * @param extension 文件扩展名
 * @returns 文件名，如 "markdown-20250312143025.html"
 */
export function generateFileName(toolName: string, extension: string): string {
  const timestamp = generateTimestamp()
  return `${toolName}-${timestamp}.${extension}`
}

/**
 * 触发文件下载
 * @param content 文件内容
 * @param fileName 文件名
 * @param mimeType MIME类型
 */
export function downloadFile(content: string | Blob, fileName: string, mimeType: string): void {
  let blob: Blob

  if (content instanceof Blob) {
    blob = content
  } else {
    blob = new Blob([content], { type: mimeType })
  }

  const url = URL.createObjectURL(blob)
  const link = document.createElement('a')
  link.href = url
  link.download = fileName
  document.body.appendChild(link)
  link.click()
  document.body.removeChild(link)
  URL.revokeObjectURL(url)
}

/**
 * 下载 Base64 编码的文件
 * @param base64Content Base64 编码的内容
 * @param fileName 文件名
 * @param mimeType MIME类型
 */
export function downloadBase64File(base64Content: string, fileName: string, mimeType: string): void {
  const byteCharacters = atob(base64Content)
  const byteNumbers = new Array(byteCharacters.length)
  for (let i = 0; i < byteCharacters.length; i++) {
    byteNumbers[i] = byteCharacters.charCodeAt(i)
  }
  const byteArray = new Uint8Array(byteNumbers)
  const blob = new Blob([byteArray], { type: mimeType })
  downloadFile(blob, fileName, mimeType)
}
