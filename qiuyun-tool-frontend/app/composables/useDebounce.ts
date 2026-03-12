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
 * <input @input="debouncedFn