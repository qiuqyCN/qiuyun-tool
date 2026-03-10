/**
 * 上传图片到服务器
 * @param file 图片文件
 * @returns 图片URL
 */
export async function uploadFile(file: File): Promise<string> {
  const { $api } = useNuxtApp()

  // 验证文件类型
  if (!file.type.startsWith('image/')) {
    throw new Error('只能上传图片文件')
  }

  // 验证文件大小（最大5MB）
  if (file.size > 5 * 1024 * 1024) {
    throw new Error('图片大小不能超过5MB')
  }

  const formData = new FormData()
  formData.append('file', file)

  interface UploadResponse {
    code: number
    message: string
    data: {
      url: string
    }
  }

  const response = await $api('/upload/image', {
    method: 'POST',
    body: formData
  }) as UploadResponse

  if (response.code !== 200) {
    throw new Error(response.message || '上传失败')
  }

  return response.data.url
}
