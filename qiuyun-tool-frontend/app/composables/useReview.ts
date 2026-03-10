import type { Review, ReviewStats, SubmitReviewRequest } from '~/types/review'

export interface UseReviewOptions {
  toolId: number
}

interface ApiResponse<T = any> {
  code: number
  message: string
  data: T
}

export function useReview(options: UseReviewOptions) {
  const { toolId } = options
  const { $api } = useNuxtApp()

  // 评论列表
  const reviews = ref<Review[]>([])
  const loading = ref(false)
  const hasMore = ref(true)
  const page = ref(1)
  const pageSize = 10
  const sortBy = ref<'newest' | 'hottest'>('newest')

  // 评论统计
  const stats = ref<ReviewStats | null>(null)

  // 获取评论列表
  const fetchReviews = async (reset = false) => {
    if (loading.value) return
    if (reset) {
      page.value = 1
      reviews.value = []
      hasMore.value = true
    }
    if (!hasMore.value && !reset) return

    loading.value = true
    try {
      const response = await $api(`/reviews/tool/${toolId}`, {
        method: 'GET',
        params: {
          page: page.value,
          size: pageSize,
          sort: sortBy.value
        }
      }) as ApiResponse

      if (response.code === 200 && response.data) {
        const newReviews = response.data.content || []
        if (reset) {
          reviews.value = newReviews
        } else {
          reviews.value.push(...newReviews)
        }
        hasMore.value = !response.data.last
        page.value++
      }
    } catch (error) {
      console.error('获取评论失败:', error)
    } finally {
      loading.value = false
    }
  }

  // 获取评论统计
  const fetchStats = async () => {
    try {
      const response = await $api(`/reviews/tool/${toolId}/stats`, {
        method: 'GET'
      }) as ApiResponse
      if (response.code === 200) {
        stats.value = response.data
      }
    } catch (error) {
      console.error('获取评论统计失败:', error)
    }
  }

  // 提交评论
  const submitReview = async (data: SubmitReviewRequest) => {
    const response = await $api('/reviews', {
      method: 'POST',
      body: {
        ...data,
        toolId
      }
    }) as ApiResponse

    if (response.code === 200) {
      // 重新获取评论列表和统计
      await fetchReviews(true)
      await fetchStats()
      return response.data
    }
    throw new Error(response.message || '提交评论失败')
  }

  // 编辑评论
  const editReview = async (reviewId: number, data: SubmitReviewRequest) => {
    const response = await $api(`/reviews/${reviewId}`, {
      method: 'PUT',
      body: {
        ...data,
        toolId
      }
    }) as ApiResponse

    if (response.code === 200) {
      // 更新本地评论数据
      const index = reviews.value.findIndex(r => r.id === reviewId)
      if (index !== -1) {
        reviews.value[index] = { ...reviews.value[index], ...response.data }
      }
      // 重新获取统计
      await fetchStats()
      return response.data
    }
    throw new Error(response.message || '修改评论失败')
  }

  // 递归查找评论
  const findReviewById = (reviewId: number, reviewList: Review[] = reviews.value): Review | null => {
    for (const review of reviewList) {
      if (review.id === reviewId) {
        return review
      }
      // 在回复中查找
      if (review.replies?.length) {
        const found = findReviewById(reviewId, review.replies)
        if (found) return found
      }
    }
    return null
  }

  // 点赞/取消点赞
  const toggleLike = async (reviewId: number) => {
    const response = await $api(`/reviews/${reviewId}/like`, {
      method: 'POST'
    }) as ApiResponse

    if (response.code === 200) {
      // 更新本地状态（支持嵌套回复）
      const review = findReviewById(reviewId)
      if (review) {
        review.isLiked = !review.isLiked
        review.likeCount += review.isLiked ? 1 : -1
      }
      return true
    }
    throw new Error(response.message || '操作失败')
  }

  // 递归删除评论
  const removeReviewById = (reviewId: number, reviewList: Review[]): boolean => {
    const index = reviewList.findIndex(r => r.id === reviewId)
    if (index !== -1) {
      reviewList.splice(index, 1)
      return true
    }
    // 在回复中查找删除
    for (const review of reviewList) {
      if (review.replies?.length) {
        if (removeReviewById(reviewId, review.replies)) {
          // 减少父评论的回复数
          if (review.replyCount > 0) {
            review.replyCount--
          }
          return true
        }
      }
    }
    return false
  }

  // 删除评论
  const deleteReview = async (reviewId: number) => {
    const response = await $api(`/reviews/${reviewId}`, {
      method: 'DELETE'
    }) as ApiResponse

    if (response.code === 200) {
      // 从列表中移除（支持嵌套回复）
      removeReviewById(reviewId, reviews.value)
      await fetchStats()
      return true
    }
    throw new Error(response.message || '删除失败')
  }

  // 检查是否已评论
  const hasReviewed = async () => {
    try {
      const response = await $api(`/reviews/tool/${toolId}/has-reviewed`, {
        method: 'GET'
      }) as ApiResponse
      return response.code === 200 ? response.data : false
    } catch {
      return false
    }
  }

  // 切换排序
  const setSortBy = (sort: 'newest' | 'hottest') => {
    sortBy.value = sort
    fetchReviews(true)
  }

  // 加载更多
  const loadMore = () => {
    fetchReviews(false)
  }

  // 初始化
  onMounted(() => {
    fetchReviews()
    fetchStats()
  })

  return {
    reviews,
    loading,
    hasMore,
    sortBy,
    stats,
    submitReview,
    editReview,
    toggleLike,
    deleteReview,
    setSortBy,
    loadMore,
    hasReviewed,
    fetchReviews,
    fetchStats
  }
}
