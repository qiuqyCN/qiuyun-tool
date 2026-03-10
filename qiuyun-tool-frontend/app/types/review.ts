import type { ReviewStatus, ReviewType } from './enums'

/**
 * 评论类型定义
 */

// 评论
export interface Review {
  id: number
  toolId: number
  userId: number
  userNickname: string
  userAvatar: string
  rating: number
  content: string
  imageUrls: string[]
  likeCount: number
  replyCount: number
  isLiked: boolean
  isOwner: boolean
  isAdmin?: boolean
  isVip?: boolean
  reviewType: ReviewType
  status: ReviewStatus
  replies?: Review[]
  createdAt: string
}

// 评论统计
export interface ReviewStats {
  toolId: number
  averageRating: number
  totalCount: number
  fiveStarCount: number
  fourStarCount: number
  threeStarCount: number
  twoStarCount: number
  oneStarCount: number
  ratingPercentages: Record<number, number>
}

// 提交评论请求
export interface SubmitReviewRequest {
  toolId?: number
  rating?: number
  content: string
  parentId?: number
  imageUrls?: string[]
}

// 评论排序类型
export type ReviewSortType = 'newest' | 'hottest'
