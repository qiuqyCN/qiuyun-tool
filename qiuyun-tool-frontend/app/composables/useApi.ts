import type { ApiResponse } from '~/types/api'
import type { Review, ReviewStats, SubmitReviewRequest } from '~/types/review'
import type { UserInfo } from '~/stores/userStore'

/**
 * 统一 API 封装
 * 提供分类清晰的 API 调用方法
 */
export function useApi() {
  const { $api } = useNuxtApp()

  // 收藏相关 API
  const favorites = {
    /**
     * 获取收藏列表
     */
    getList: (page = 0, size = 8) =>
      $api('/api/favorites', {
        params: { page, size }
      }) as Promise<ApiResponse<any>>,

    /**
     * 检查是否已收藏
     */
    check: (toolId: number) =>
      $api(`/api/favorites/check/${toolId}`) as Promise<ApiResponse<{ isFavorite: boolean }>>,

    /**
     * 切换收藏状态
     */
    toggle: (toolId: number) =>
      $api(`/api/favorites/toggle/${toolId}`, {
        method: 'POST'
      }) as Promise<ApiResponse<{ isFavorite: boolean; favoriteCount: number }>>
  }

  // 评论相关 API
  const reviews = {
    /**
     * 获取评论列表
     */
    getList: (toolId: number, page = 1, size = 10, sort: 'newest' | 'hottest' = 'newest') =>
      $api(`/api/reviews/tool/${toolId}`, {
        method: 'GET',
        params: { page, size, sort }
      }) as Promise<ApiResponse<any>>,

    /**
     * 获取评论统计
     */
    getStats: (toolId: number) =>
      $api(`/api/reviews/tool/${toolId}/stats`, {
        method: 'GET'
      }) as Promise<ApiResponse<ReviewStats>>,

    /**
     * 提交评论
     */
    create: (data: SubmitReviewRequest & { toolId: number }) =>
      $api('/api/reviews', {
        method: 'POST',
        body: data
      }) as Promise<ApiResponse<Review>>,

    /**
     * 编辑评论
     */
    update: (reviewId: number, data: SubmitReviewRequest & { toolId: number }) =>
      $api(`/api/reviews/${reviewId}`, {
        method: 'PUT',
        body: data
      }) as Promise<ApiResponse<Review>>,

    /**
     * 删除评论
     */
    delete: (reviewId: number) =>
      $api(`/api/reviews/${reviewId}`, {
        method: 'DELETE'
      }) as Promise<ApiResponse<void>>,

    /**
     * 点赞/取消点赞
     */
    toggleLike: (reviewId: number) =>
      $api(`/api/reviews/${reviewId}/like`, {
        method: 'POST'
      }) as Promise<ApiResponse<void>>,

    /**
     * 检查是否已评论
     */
    hasReviewed: (toolId: number) =>
      $api(`/api/reviews/tool/${toolId}/has-reviewed`, {
        method: 'GET'
      }) as Promise<ApiResponse<boolean>>
  }

  // 认证相关 API
  const auth = {
    /**
     * 登录
     */
    login: (username: string, password: string, rememberMe = false) =>
      $api('/api/auth/login', {
        method: 'POST',
        body: { username, password, rememberMe }
      }) as Promise<ApiResponse<{ accessToken: string; refreshToken: string; user: UserInfo }>>,

    /**
     * 注册
     */
    register: (username: string, email: string, password: string, confirmPassword: string) =>
      $api('/api/auth/register', {
        method: 'POST',
        body: { username, email, password, confirmPassword }
      }) as Promise<ApiResponse<{ message: string }>>,

    /**
     * 退出登录
     */
    logout: () =>
      $api('/api/auth/logout', { method: 'POST' }) as Promise<ApiResponse<void>>,

    /**
     * 获取当前用户信息
     */
    getCurrentUser: () =>
      $api('/api/auth/me') as Promise<ApiResponse<UserInfo>>,

    /**
     * 刷新 Token
     */
    refreshToken: (refreshToken: string) =>
      $api('/api/auth/refresh', {
        method: 'POST',
        headers: {
          Authorization: `Bearer ${refreshToken}`
        }
      }) as Promise<ApiResponse<{ accessToken: string; refreshToken: string; user: UserInfo }>>
  }

  // 上传相关 API
  const upload = {
    /**
     * 上传图片
     */
    image: (file: File) => {
      const formData = new FormData()
      formData.append('file', file)
      return $api('/api/upload/image', {
        method: 'POST',
        body: formData
      }) as Promise<ApiResponse<{ url: string }>>
    }
  }

  // 工具执行相关 API
  const tools = {
    /**
     * 上传文件
     */
    uploadFile: (file: File, toolCode: string, onProgress?: (percent: number) => void) => {
      const formData = new FormData()
      formData.append('file', file)
      formData.append('toolCode', toolCode)

      return $api('/api/tools/upload', {
        method: 'POST',
        body: formData,
        onUploadProgress: (progressEvent: { loaded: number; total?: number }) => {
          if (progressEvent.total && onProgress) {
            const percent = Math.round((progressEvent.loaded / progressEvent.total) * 100)
            onProgress(percent)
          }
        }
      }) as Promise<ApiResponse<any>>
    },

    /**
     * 执行工具
     */
    execute: <T = any, R = any>(request: { toolCode: string; params: T }) =>
      $api('/api/tools/execute', {
        method: 'POST',
        body: request
      }) as Promise<ApiResponse<any>>,

    /**
     * 获取任务状态
     */
    getTaskStatus: (taskId: string) =>
      $api(`/api/tools/tasks/${taskId}`) as Promise<ApiResponse<any>>,

    /**
     * 取消任务
     */
    cancelTask: (taskId: string) =>
      $api(`/api/tools/tasks/${taskId}/cancel`, {
        method: 'POST'
      }) as Promise<ApiResponse<void>>
  }

  return {
    favorites,
    reviews,
    auth,
    upload,
    tools
  }
}
