/**
 * 工具操作组合式函数
 * 提供工具执行、任务管理、文件下载等前端操作功能
 */
import type {
  ToolExecuteResponse,
  ToolProgress,
  ApiResponse
} from '~/types/tool'

export function useTool() {
  const { $api } = useNuxtApp()
  const config = useRuntimeConfig()

  /**
   * 执行工具
   * @param toolCode 工具代码
   * @param params 工具参数（可选）
   * @param files 上传的文件列表（可选）
   * @param onUploadProgress 上传进度回调（可选）
   * @returns 工具执行响应
   */
  const executeTool = async <R = any>(
    toolCode: string,
    params?: any,
    files?: File[],
    onUploadProgress?: (percent: number) => void
  ): Promise<ToolExecuteResponse<R>> => {
    const formData = new FormData()
    formData.append('toolCode', toolCode)
    
    if (params) {
      formData.append('params', JSON.stringify(params))
    }
    
    if (files && files.length > 0) {
      files.forEach(file => {
        formData.append('files', file)
      })
    }

    const response = await $api<ApiResponse<ToolExecuteResponse<R>>>('/api/tools/execute', {
      method: 'POST',
      body: formData,
      onUploadProgress: (progressEvent: { loaded: number; total?: number }) => {
        if (progressEvent.total && onUploadProgress) {
          const percent = Math.round((progressEvent.loaded / progressEvent.total) * 100)
          onUploadProgress(percent)
        }
      }
    })

    if (response.code !== 200) {
      throw new Error(response.message || '执行失败')
    }

    if (!response.data) {
      throw new Error(response.message || '执行失败，返回数据为空')
    }

    return response.data
  }

  /**
   * 获取任务状态
   * @param taskId 任务ID
   * @returns 任务状态响应
   */
  const getTaskStatus = async <T = any>(
    taskId: string
  ): Promise<ToolExecuteResponse<T>> => {
    return await $api(`/tools/tasks/${taskId}`)
  }

  /**
   * 取消任务
   * @param taskId 任务ID
   */
  const cancelTask = async (taskId: string): Promise<void> => {
    await $api(`/tools/tasks/${taskId}/cancel`, {
      method: 'POST'
    })
  }

  /**
   * 下载文件
   * @param fileId 文件ID
   * @returns 文件下载URL
   */
  const downloadFile = (fileId: string): string => {
    return `${config.public.apiBaseUrl}/tools/files/${fileId}`
  }

  /**
   * 下载任务结果
   * @param taskId 任务ID
   * @returns 结果下载URL
   */
  const downloadResult = (taskId: string): string => {
    return `${config.public.apiBaseUrl}/tools/tasks/${taskId}/download`
  }

  /**
   * 创建 SSE 连接获取进度
   * @param taskId 任务ID
   * @param callbacks 回调函数对象
   * @returns EventSource 实例
   */
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

    // 监听进度事件
    eventSource.addEventListener('progress', (event: MessageEvent) => {
      const data = JSON.parse(event.data)
      callbacks.onProgress?.(data)
    })

    // 监听完成事件
    eventSource.addEventListener('complete', (event: MessageEvent) => {
      const data = JSON.parse(event.data)
      callbacks.onComplete?.(data)
      eventSource.close()
    })

    // 监听错误事件
    eventSource.addEventListener('error', (event: MessageEvent) => {
      const data = event.data ? JSON.parse(event.data) : { message: '连接错误' }
      callbacks.onError?.(data.message || '连接错误')
      eventSource.close()
    })

    // 处理连接错误
    eventSource.onerror = () => {
    }

    return eventSource
  }

  return {
    executeTool,
    getTaskStatus,
    cancelTask,
    downloadFile,
    downloadResult,
    createProgressStream
  }
}
