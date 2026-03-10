// API 相关类型定义

// 统一 API 响应格式
export interface ApiResponse<T = any> {
  code: number
  message: string
  data: T
  timestamp?: number
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
  iconColor?: string
  iconBgColor?: string
  isVip: boolean
  isHot?: boolean
  priceMode?: 'free' | 'vip'
  visits: number
  viewCount?: number
  usageCount?: number
  rating: number
  reviewCount: number
  favoriteCount?: number
  instructions?: string
  tags: string[]
  createdAt: string // ISO 8601 格式日期字符串
}
