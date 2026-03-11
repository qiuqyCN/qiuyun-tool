/**
 * 剪贴板操作 composable
 * 提供复制文本到剪贴板的功能
 */
export function useClipboard(showToast?: (message: string, type?: 'success' | 'error') => void) {
  /**
   * 复制文本到剪贴板
   * @param text 要复制的文本
   * @param successMsg 成功提示消息
   * @param failMsg 失败提示消息
   */
  const copy = async (text: string, successMsg = '已复制', failMsg = '复制失败'): Promise<boolean> => {
    try {
      await navigator.clipboard.writeText(text)
      showToast?.(successMsg, 'success')
      return true
    } catch {
      showToast?.(failMsg, 'error')
      return false
    }
  }

  /**
   * 复制多个文本到剪贴板（用换行分隔）
   * @param texts 文本数组
   * @param successMsg 成功提示消息
   * @param failMsg 失败提示消息
   */
  const copyMultiple = async (texts: string[], successMsg = '已复制所有内容', failMsg = '复制失败'): Promise<boolean> => {
    return copy(texts.join('\n'), successMsg, failMsg)
  }

  return {
    copy,
    copyMultiple
  }
}
