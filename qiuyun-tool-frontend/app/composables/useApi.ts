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
  toolCount: number
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
}

// 分类工具列表响应类型 - 对应后端 HomeDataResponse.CategoryToolsResponse
export interface CategoryToolsResponse {
  categoryCode: string
  categoryName: string
  tools: ToolResponse[]
}

// 统计数据响应类型
export interface StatsResponse {
  totalTools: number
  dailyActiveUsers: number
  monthlyVisits: number
}

// 首页数据响应类型 - 对应后端 HomeDataResponse
export interface HomeDataResponse {
  categories: CategoryResponse[]
  hotTools: ToolResponse[]
  newTools: ToolResponse[]
  categoryTools: CategoryToolsResponse[]
  stats: StatsResponse
}

// 为了保持向后兼容，保留旧名称作为别名
export type Category = CategoryResponse
export type Tool = ToolResponse
export type CategoryTools = CategoryToolsResponse
export type HomeData = HomeDataResponse

// API 方法
export function useApi() {
  // 获取首页数据
  const getHomeData = async (): Promise<HomeDataResponse> => {
    return fetchApi<HomeDataResponse>('/home')
  }

  return {
    getHomeData,
  }
}
