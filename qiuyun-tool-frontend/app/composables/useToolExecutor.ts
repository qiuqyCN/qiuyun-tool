import {
  type ToolExecuteResponse,
  type ToolProgress,
  type ProcessLogEntry,
  TaskStatus,
  type ToolType
} from '~/types/tool'

export interface UseToolExecutorOptions<T = any, R = any> {
  toolCode: string
  toolType: ToolType
  onProgress?: (progress: ToolProgress) => void
  onLog?: (log: ProcessLogEntry) => void
  onSuccess?: (result: R) => void
  onError?: (error: string) => void
}

export function useToolExecutor<T = any, R = any>(options: UseToolExecutorOptions<T, R>) {
  const { executeTool, createProgressStream, cancelTask } = useTool()

  const taskId = ref<string>('')
  const status = ref<TaskStatus>(TaskStatus.PENDING)
  const progress = ref<ToolProgress | null>(null)
  const logs = ref<ProcessLogEntry[]>([])
  const result = ref<R | null>(null)
  const error = ref<string>('')
  const isLoading = ref(false)
  const isComplete = ref(false)

  let eventSource: EventSource | null = null

  const execute = async (params?: T, files?: File[], onUploadProgress?: (percent: number) => void) => {
    isLoading.value = true
    isComplete.value = false
    error.value = ''
    result.value = null
    progress.value = null
    logs.value = []

    try {
      const response = await executeTool<R>(options.toolCode, params, files, onUploadProgress)
      taskId.value = response.taskId
      status.value = response.status

      if (options.toolType === 'instant') {
        isComplete.value = true
        isLoading.value = false
        result.value = response.result
        options.onSuccess?.(response.result)
        return response
      } else {
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

  const startProgressStream = (tid: string) => {
    eventSource = createProgressStream(tid, {
      onProgress: (p) => {
        progress.value = p
        status.value = TaskStatus.PROCESSING
        options.onProgress?.(p)
      },
      onLog: (log) => {
        logs.value.push(log)
        options.onLog?.(log)
      },
      onComplete: (p) => {
        progress.value = p
        status.value = TaskStatus.COMPLETED
        isComplete.value = true
        isLoading.value = false
        result.value = p.data as R
        options.onSuccess?.(p.data as R)
      },
      onError: (msg) => {
        status.value = TaskStatus.FAILED
        isLoading.value = false
        error.value = msg
        options.onError?.(msg)
      }
    })
  }

  const cancel = async () => {
    if (taskId.value && status.value === TaskStatus.PROCESSING) {
      try {
        await cancelTask(taskId.value)
        eventSource?.close()
        status.value = TaskStatus.FAILED
        isLoading.value = false
        error.value = '已取消'
      } catch (err) {
        console.error('取消任务失败:', err)
      }
    }
  }

  const cleanup = () => {
    eventSource?.close()
    eventSource = null
  }

  onUnmounted(() => {
    cleanup()
  })

  return {
    taskId: readonly(taskId),
    status: readonly(status),
    progress: readonly(progress),
    logs: readonly(logs),
    result: readonly(result),
    error: readonly(error),
    isLoading: readonly(isLoading),
    isComplete: readonly(isComplete),
    execute,
    cancel,
    cleanup
  }
}
