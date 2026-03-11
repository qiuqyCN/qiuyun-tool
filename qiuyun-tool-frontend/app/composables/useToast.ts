/**
 * Toast 提示 composable
 * 提供全局的 Toast 提示功能
 */
export function useToast() {
  const toast = ref({
    show: false,
    message: '',
    type: 'success' as 'success' | 'error' | 'info'
  })

  const showToast = (message: string, type: 'success' | 'error' | 'info' = 'success', duration = 2000) => {
    toast.value.message = message
    toast.value.type = type
    toast.value.show = true
    setTimeout(() => {
      toast.value.show = false
    }, duration)
  }

  const showSuccess = (message: string, duration?: number) => showToast(message, 'success', duration)
  const showError = (message: string, duration?: number) => showToast(message, 'error', duration)
  const showInfo = (message: string, duration?: number) => showToast(message, 'info', duration)

  return {
    toast,
    showToast,
    showSuccess,
    showError,
    showInfo
  }
}
