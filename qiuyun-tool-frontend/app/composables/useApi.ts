// API 基础配置
const getBaseUrl = (): string => {
  const config = useRuntimeConfig()
  return config.public.apiBaseUrl as string
}

// 通用请求函数
async function fetchApi<T>(url: string, options?: RequestInit): Promise<T> {
  const baseUrl = getBaseUrl()
  const fullUrl = `${baseUrl}${url}`

  const response = await fetch(fullUrl, {
    headers: {
      'Content-Type': 'application/json',
    },
    ...options,
  })

  if (!response.ok) {
    throw new Error(`API 请求失败: ${response.status}`)
  }

  const data = await response.json()

  if (data.code !== 200) {
    throw new Error(data.message || '请求失败')
  }

  return data.data as T
}

// 分类响应类型 - 对应后端 CategoryResponse
export interface CategoryResponse {
  id: number
  code: string
  name: string
  icon: string
  description: string
}

// 工具响应类型 - 对应后端 ToolResponse
export interface ToolResponse {
  id: number
  code: string
  name: string
  description: string
  category: string
  icon: string
  isVip: boolean
  visits: number
  rating: number
  reviewCount: number
  tags: string[]
  createdAt: string // ISO 8601 格式日期字符串
}

// API 方法
export function useApi() {
  // 获取所有分类列表
  const getCategories = async (): Promise<CategoryResponse[]> => {
    return fetchApi<CategoryResponse[]>('/store/categories')
  }

  // 获取所有工具列表
  const getTools = async (): Promise<ToolResponse[]> => {
    return fetchApi<ToolResponse[]>('/store/tools')
  }

  return {
    getCategories,
    getTools,
  }
}
