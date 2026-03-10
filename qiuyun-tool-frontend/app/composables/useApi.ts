// 统一 API 封装 - 支持 SSR/客户端，自动处理认证和响应格式

import type { CategoryResponse, ToolResponse, ApiResponse } from '~/types/api'

export type { CategoryResponse, ToolResponse }

/**
 * 使用统一封装的 API 客户端
 * 自动处理：baseURL、认证、Token 刷新、响应格式
 */
export function useApi() {
  const { $api } = useNuxtApp()

  /**
   * 通用请求方法 - 自动处理后端响应格式 { code, message, data }
   */
  async function request<T>(url: string, options?: any): Promise<T> {
    const response: ApiResponse = await $api(url, options)

    // 后端返回格式: { code, message, data }
    if (response.code !== 200) {
      throw new Error(response.message || '请求失败')
    }

    return response.data as T
  }

  /**
   * 获取所有分类列表
   */
  async function getCategories(): Promise<CategoryResponse[]> {
    return request<CategoryResponse[]>('/store/categories')
  }

  /**
   * 获取所有工具列表
   */
  async function getTools(): Promise<ToolResponse[]> {
    return request<ToolResponse[]>('/store/tools')
  }

  return {
    request,
    getCategories,
    getTools,
  }
}

/**
 * 服务端数据获取 - 用于 SSR
 * 在 setup 中使用，自动处理服务端渲染
 */
export async function useApiData<T>(
  key: string,
  url: string,
  options?: any
) {
  const { $api } = useNuxtApp()

  return await useAsyncData<T>(key, async () => {
    const response: ApiResponse = await $api(url, options)

    if (response.code !== 200) {
      throw new Error(response.message || '请求失败')
    }

    return response.data as T
  }, {
    server: true,
    default: () => null as T,
  })
}
