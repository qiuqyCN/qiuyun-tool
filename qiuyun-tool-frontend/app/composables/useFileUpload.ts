import type { FileUploadResponse } from '~/types/tool'

export interface UseFileUploadOptions {
  maxFileSize?: number // 字节
  allowedTypes?: string[]
  toolCode: string
}

export function useFileUpload(options: UseFileUploadOptions) {
  const { uploadFile } = useTool()

  const file = ref<File | null>(null)
  const fileId = ref<string>('')
  const uploadProgress = ref(0)
  const isUploading = ref(false)
  const error = ref<string>('')

  // 选择文件
  const selectFile = (selectedFile: File) => {
    error.value = ''

    // 检查文件大小
    if (options.maxFileSize && selectedFile.size > options.maxFileSize) {
      error.value = `文件大小不能超过 ${formatFileSize(options.maxFileSize)}`
      return false
    }

    // 检查文件类型
    if (options.allowedTypes && options.allowedTypes.length > 0) {
      const isAllowed = options.allowedTypes.some(type => {
        if (type.includes('*')) {
          return selectedFile.type.startsWith(type.replace('/*', ''))
        }
        return selectedFile.type === type
      })
      if (!isAllowed) {
        error.value = `不支持的文件类型，请上传 ${options.allowedTypes.join(', ')} 格式的文件`
        return false
      }
    }

    file.value = selectedFile
    return true
  }

  // 上传文件
  const upload = async (): Promise<FileUploadResponse | null> => {
    if (!file.value) {
      error.value = '请先选择文件'
      return null
    }

    isUploading.value = true
    uploadProgress.value = 0
    error.value = ''

    try {
      const response = await uploadFile(
        file.value,
        options.toolCode,
        (percent) => {
          uploadProgress.value = percent
        }
      )

      fileId.value = response.fileId
      return response
    } catch (err: any) {
      error.value = err?.message || '上传失败'
      return null
    } finally {
      isUploading.value = false
    }
  }

  // 清除文件
  const clear = () => {
    file.value = null
    fileId.value = ''
    uploadProgress.value = 0
    error.value = ''
  }

  // 格式化文件大小
  const formatFileSize = (bytes: number): string => {
    if (bytes === 0) return '0 Bytes'
    const k = 1024
    const sizes = ['Bytes', 'KB', 'MB', 'GB']
    const i = Math.floor(Math.log(bytes) / Math.log(k))
    return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i]
  }

  return {
    file: readonly(file),
    fileId: readonly(fileId),
    uploadProgress: readonly(uploadProgress),
    isUploading: readonly(isUploading),
    error: readonly(error),
    selectFile,
    upload,
    clear,
    formatFileSize
  }
}
