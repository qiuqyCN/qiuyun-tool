// 工具类型
export enum ToolType {
  INSTANT = 'instant',
  FILE_PROCESS = 'file_process',
  ASYNC = 'async'
}

// 任务状态
export enum TaskStatus {
  PENDING = 'pending',
  PROCESSING = 'processing',
  COMPLETED = 'completed',
  FAILED = 'failed'
}

// 工具执行请求
export interface ToolExecuteRequest<T = any> {
  toolCode: string
  fileId?: string
  params: T
}

// 工具执行响应
export interface ToolExecuteResponse<T = any> {
  taskId: string
  status: TaskStatus
  result: T
  downloadUrl?: string
  progress: number
  message?: string
}

// 进度信息
export interface ToolProgress {
  percent: number
  message: string
  data?: any
  completed?: boolean
}

// 文件上传响应
export interface FileUploadResponse {
  fileId: string
  fileName: string
  fileSize: number
  contentType: string
}

// 任务信息
export interface TaskInfo {
  taskId: string
  toolCode: string
  status: TaskStatus
  progress: number
  result?: any
  errorMessage?: string
  createdAt: string
  completedAt?: string
}

// 表单字段配置
export interface ToolParamConfig {
  name: string
  label: string
  type: 'text' | 'textarea' | 'number' | 'slider' | 'select' | 'radio' | 'checkbox' | 'checkboxGroup' | 'switch' | 'color' | 'date' | 'datetime'
  required?: boolean
  placeholder?: string
  description?: string
  defaultValue?: any
  rows?: number
  min?: number
  max?: number
  step?: number
  unit?: string
  options?: { label: string; value: string | number | boolean }[]
}

// 工具定义
export interface Tool {
  id: string
  code: string
  name: string
  description: string
  type: ToolType
  priceMode: 'free' | 'vip'
  icon?: string
  iconColor?: string
  iconBgColor?: string
  categoryId: string
  categoryName: string
  needFile: boolean
  acceptFileTypes?: string
  maxFileSize?: number
  resultType?: 'text' | 'json' | 'file' | 'image' | 'table' | 'custom'
  paramsConfig: ToolParamConfig[]
  instructions?: string
  tags?: string[]
  viewCount: number
  usageCount: number
  rating: number
  favoriteCount: number
  reviewCount: number
  createdAt: string
  updatedAt: string
}

// 工具配置
export interface ToolConfig {
  code: string
  name: string
  description?: string
  type: ToolType
  icon?: string
  maxFileSize?: number
  allowedFileTypes?: string[]
  component?: string
}

// SSE 事件类型
export type SSEEventType = 'progress' | 'complete' | 'error'

export interface SSEEvent {
  type: SSEEventType
  data: ToolProgress
}
