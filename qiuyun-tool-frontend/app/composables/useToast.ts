/**
 * Toast 提示工具
 */
export interface ToastOptions {
  message: string
  duration?: number
  position?: 'top' | 'center' | 'bottom'
}

export function useToast() {
  const show = (message: string, options: Partial<ToastOptions> = {}) => {
    // 这里可以实现具体的 toast 显示逻辑
    // 暂时使用 alert 或 console
    console.log(`[Toast] ${message}`)
  }

  const success = (message: string, options: Partial<ToastOptions> = {}) => {
    console.log(`[Success] ${message}`)
    // 可以在这里实现成功提示的样式
    show(message, { ...options })
  }

  const error = (message: string, options: Partial<ToastOptions> = {}) => {
    console.error(`[Error] ${message}`)
    // 可以在这里实现错误提示的样式
    show(message, { ...options })
  }

  const warning = (message: string, options: Partial<ToastOptions> = {}) => {
    console.warn(`[Warning] ${message}`)
    show(message, { ...options })
  }

  const info = (message: string, options: Partial<ToastOptions> = {}) => {
    console.info(`[Info] ${message}`)
    show(message, { ...options })
  }

  return {
    show,
    success,
    error,
    warning,
    info
  }
}
