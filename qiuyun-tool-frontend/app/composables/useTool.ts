import type {
  ToolExecuteRequest,
  ToolExecuteResponse,
  FileUploadResponse,
  ToolProgress,
  ApiResponse
} from '~/types/tool'

export function useTool() {
  const { $api } = useNuxtApp()
  const config = useRuntimeConfig()

  // 上传文件
  const uploadFile = async (
    file: File,
    toolCode: string,
    onProgress?: (percent: number) => void
  ): Promise<FileUploadResponse> => {
    const formData = new FormData()
    formData.append('file', file)
    formData.append('toolCode', toolCode)

    return await $api('/tools/upload', {
      method: 'POST',
      body: formData,
      onUploadProgress: (progressEvent: { loaded: number; total?: number }) => {
        if (progressEvent.total && onProgress) {
          const percent = Math.round((progressEvent.loaded / progressEvent.total) * 100)
          onProgress(percent)
        }
      }
    })
  }

  // 执行工具
  const executeTool = async <T = any, R = any>(
    request: ToolExecuteRequest<T>
  ): Promise<ToolExecuteResponse<R>> => {
    const response = await $api<ApiResponse<ToolExecuteResponse<R>>>('/tools/execute', {
      method: 'POST',
      body: request
    })
    return response.data
  }

  // 获取任务状态
  const getTaskStatus = async <T = any>(
    taskId: string
  ): Promise<ToolExecuteResponse<T>> => {
    return await $api(`/tools/tasks/${taskId}`)
  }

  // 取消任务
  const cancelTask = async (taskId: string): Promise<void> => {
    await $api(`/tools/tasks/${taskId}/cancel`, {
      method: 'POST'
    })
  }

  // 下载文件
  const downloadFile = (fileId: string): string => {
    return `${config.public.apiBaseUrl}/tools/files/${fileId}`
  }

  // 下载任务结果
  const downloadResult = (taskId: string): string => {
    return `${config.public.apiBaseUrl}/tools/tasks/${taskId}/download`
  }

  // 创建 SSE 连接获取进度
  const createProgressStream = (
    taskId: string,
    callbacks: {
      onProgress?: (progress: ToolProgress) => void
      onComplete?: (progress: ToolProgress) => void
      onError?: (error: string) => void
    }
  ): EventSource => {
    const eventSource = new EventSource(
      `${config.public.apiBaseUrl}/tools/tasks/${taskId}/progress`
    )

    eventSource.addEventListener('progress', (event: MessageEvent) => {
      const data = JSON.parse(event.data)
      callbacks.onProgress?.(data)
    })

    eventSource.addEventListener('complete', (event: MessageEvent) => {
      const data = JSON.parse(event.data)
      callbacks.onComplete?.(data)
      eventSource.close()
    })

    eventSource.addEventListener('error', (event: MessageEvent) => {
      const data = event.data ? JSON.parse(event.data) : { message: '连接错误' }
      callbacks.onError?.(data.message || '连接错误')
      eventSource.close()
    })

    eventSource.onerror = () => {
      // 浏览器会自动重连，如果连接彻底失败会触发 error 事件
    }

    return eventSource
  }

  return {
    uploadFile,
    executeTool,
    getTaskStatus,
    cancelTask,
    downloadFile,
    downloadResult,
    createProgressStream
  }
}
