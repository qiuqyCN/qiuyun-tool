import { ref, readonly } from 'vue'

export interface UseDebounceOptions {
  /** 延迟时间（毫秒），默认 300ms */
  delay?: number
  /** 是否立即执行第一次调用 */
  immediate?: boolean
}

/**
 * 防抖 Composable
 * @param fn 要防抖的函数
 * @param options 防抖选项
 * @returns 防抖包装后的函数和控制方法
 * 
 * @example
 * ```ts
 * // 基本用法
 * const { debouncedFn } = useDebounce((value: string) => {
 *   console.log('搜索:', value)
 * }, { delay: 500 })
 * 
 * // 在模板中使用
 * <input @input="debouncedFn($event.target.value)" />
 * 
 * // 手动取消
 * const { debouncedFn, cancel } = useDebounce(() => {
 *   // 执行某些操作
 * }, { delay: 1000 })
 * 
 * // 组件卸载前取消
 * onUnmounted(() => {
 *   cancel()
 * })
 * ```
 */
export function useDebounce<T extends (...args: any[]) => any>(
  fn: T,
  options: UseDebounceOptions = {}
) {
  const { delay = 300, immediate = false } = options
  
  const isPending = ref(false)
  let timeoutId: ReturnType<typeof setTimeout> | null = null
  
  const debouncedFn = (...args: Parameters<T>) => {
    isPending.value = true
    
    if (timeoutId) {
      clearTimeout(timeoutId)
    }
    
    const shouldCallNow = immediate && !timeoutId
    
    timeoutId = setTimeout(() => {
      timeoutId = null
      isPending.value = false
      if (!immediate) {
        fn(...args)
      }
    }, delay)
    
    if (shouldCallNow) {
      fn(...args)
    }
  }
  
  /**
   * 取消待执行的防抖调用
   */
  const cancel = () => {
    if (timeoutId) {
      clearTimeout(timeoutId)
      timeoutId = null
      isPending.value = false
    }
  }
  
  /**
   * 立即执行函数并取消待执行的调用
   */
  const flush = (...args: Parameters<T>) => {
    cancel()
    fn(...args)
  }
  
  return {
    debouncedFn,
    cancel,
    flush,
    isPending: readonly(isPending)
  }
}

/**
 * 防抖 Ref
 * 创建一个带有防抖功能的 ref，值变化时会延迟触发回调
 * 
 * @example
 * ```ts
 * const searchQuery = useDebouncedRef('', {
 *   delay: 500,
 *   onChange: (value) => {
 *     console.log('搜索:', value)
 *   }
 * })
 * 
 * // 在模板中绑定
 * <input v-model="searchQuery" />
 * ```
 */
export interface UseDebouncedRefOptions extends UseDebounceOptions {
  onChange?: (value: string) => void
}

export function useDebouncedRef(
  initialValue: string = '',
  options: UseDebouncedRefOptions = {}
) {
  const { delay = 300, onChange } = options
  
  const innerValue = ref(initialValue)
  const debouncedValue = ref(initialValue)
  let timeoutId: ReturnType<typeof setTimeout> | null = null
  
  const setValue = (newValue: string) => {
    innerValue.value = newValue
    
    if (timeoutId) {
      clearTimeout(timeoutId)
    }
    
    timeoutId = setTimeout(() => {
      debouncedValue.value = newValue
      onChange?.(newValue)
      timeoutId = null
    }, delay)
  }
  
  const cancel = () => {
    if (timeoutId) {
      clearTimeout(timeoutId)
      timeoutId = null
    }
  }
  
  return {
    innerValue: readonly(innerValue),
    debouncedValue: readonly(debouncedValue),
    setValue,
    cancel
  }
}

/**
 * 使用 lodash 风格的防抖（如果项目已安装 lodash-es）
 * 这是一个更简单的包装器，适用于复杂的防抖场景
 */
export function useDebounceFn<T extends (...args: any[]) => any>(
  fn: T,
  delay: number = 300
) {
  let timeoutId: ReturnType<typeof setTimeout> | null = null
  
  const debounced = (...args: Parameters<T>) => {
    if (timeoutId) {
      clearTimeout(timeoutId)
    }
    
    timeoutId = setTimeout(() => {
      fn(...args)
      timeoutId = null
    }, delay)
  }
  
  debounced.cancel = () => {
    if (timeoutId) {
      clearTimeout(timeoutId)
      timeoutId = null
    }
  }
  
  debounced.flush = (...args: Parameters<T>) => {
    debounced.cancel()
    fn(...args)
  }
  
  return debounced
}
