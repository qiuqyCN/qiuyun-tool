import type {
  ToolExecuteRequest,
  ToolExecuteResponse,
  ToolProgress,
  TaskStatus,
  ToolType
} from '~/types/tool'

export interface UseToolExecutorOptions<T = any, R = any> {
  toolCode: string
  toolType: ToolType
  onProgress?: (progress: ToolProgress) => void
  onSuccess?: (result: R) => void
  onError?: (error: string) => void
}

export function useToolExecutor<T = any, R = any>(options: UseToolExecutorOptions<T, R>) {
  const { executeTool, createProgressStream, cancelTask } = useTool()

  const taskId = ref<string>('')
  const status = ref<TaskStatus>('pending')
  const progress = ref<ToolProgress | null>(null)
  const result = ref<R | null>(null)
  const error = ref<string>('')
  const isLoading = ref(false)
  const isComplete = ref(false)

  let eventSource: EventSource | null = null

  // 执行工具
  const execute = async (params: T, fileId?: string) => {
    isLoading.value = true
    isComplete.value = false
    error.value = ''
    result.value = null
    progress.value = null

    try {
      const request: ToolExecuteRequest<T> = {
        toolCode: options.toolCode,
        fileId,
        params
      }

      const response = await executeTool<T, R>(request)
      taskId.value = response.taskId
      status.value = response.status

      if (options.toolType === 'instant') {
        // 即时处理类型，直接返回结果
        isComplete.value = true
        isLoading.value = false
        result.value = response.result
        options.onSuccess?.(response.result)
        return response
      } else {
        // 文件处理或异步类型，需要监听进度
        startProgressStream(response.taskId)
        return response
      }
    } catch (err: any) {
      isLoading.value = false
      error.value = err?.message || '执行失败'
      options.onError?.(error.value)
      throw err
    }
  }

  // 开始监听进度
  const startProgressStream = (tid: string) => {
    eventSource = createProgressStream(tid, {
      onProgress: (p) => {
        progress.value = p
        status.value = 'processing'
        options.onProgress?.(p)
      },
      onComplete: (p) => {
        progress.value = p
        status.value = 'completed'
        isComplete.value = true
        isLoading.value = false
        result.value = p.data as R
        options.onSuccess?.(p.data as R)
      },
      onError: (msg) => {
        status.value = 'failed'
        isLoading.value = false
        error.value = msg
        options.onError?.(msg)
      }
    })
  }

  // 取消任务
  const cancel = async () => {
    if (taskId.value && status.value === 'processing') {
      try {
        await cancelTask(taskId.value)
        eventSource?.close()
        status.value = 'failed'
        isLoading.value = false
        error.value = '已取消'
      } catch (err) {
        console.error('取消任务失败:', err)
      }
    }
  }

  // 清理资源
  const cleanup = () => {
    eventSource?.close()
    eventSource = null
  }

  // 组件卸载时清理
  onUnmounted(() => {
    cleanup()
  })

  return {
    taskId: readonly(taskId),
    status: readonly(status),
    progress: readonly(progress),
    result: readonly(result),
    error: readonly(error),
    isLoading: readonly(isLoading),
    isComplete: readonly(isComplete),
    execute,
    cancel,
    cleanup
  }
}
