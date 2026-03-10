<template>
  <div class="mt-10">
    <h2 class="text-xl font-semibold text-foreground mb-5">
      用户评价
      <span v-if="stats?.totalCount" class="text-sm font-normal text-muted-foreground ml-2">({{ stats.totalCount }})</span>
    </h2>

    <!-- 评论统计 -->
    <ReviewStats :stats="stats" />

    <!-- 评论表单 - 新建评论 -->
    <ReviewForm
      v-if="isLoggedIn && !hasUserReviewed && !editingReview"
      :tool-id="toolId"
      @submit="handleSubmit"
    />

    <!-- 编辑评论表单 -->
    <div v-if="editingReview" ref="editFormRef" class="mb-6 p-4 border border-border rounded-lg bg-card">
      <p class="text-sm text-muted-foreground mb-3">编辑评论</p>
      <ReviewForm
        :tool-id="toolId"
        :initial-data="editingReviewData"
        :is-edit="true"
        @submit="handleEditSubmit"
        @cancel="cancelEdit"
      />
    </div>

    <!-- 登录提示 -->
    <div v-else-if="!isLoggedIn" class="py-10 px-8 text-center bg-muted/30 rounded-xl mb-6">
      <p class="text-muted-foreground mb-4">登录后即可发表评论</p>
      <Button @click="showLoginModal">立即登录</Button>
    </div>

    <!-- 已评论提示 + 编辑按钮 -->
    <div v-else-if="hasUserReviewed && !editingReview" class="py-6 px-8 text-center bg-muted/30 rounded-xl mb-6">
      <p class="text-muted-foreground mb-4">您已评价过该工具</p>
      <Button variant="outline" @click="startEdit">编辑评价</Button>
    </div>

    <!-- 排序选项 -->
    <div class="flex gap-2 mb-5 pb-4 border-b border-border/40">
      <button
        v-for="option in sortOptions"
        :key="option.value"
        class="px-4 py-1.5 text-sm rounded-md transition-colors"
        :class="sortBy === option.value ? 'bg-primary/10 text-primary font-medium' : 'text-muted-foreground hover:bg-muted hover:text-foreground'"
        @click="setSortBy(option.value)"
      >
        {{ option.label }}
      </button>
    </div>

    <!-- 评论列表 -->
    <div class="min-h-[200px]">
      <ReviewItem
        v-for="review in reviews"
        :key="review.id"
        :review="review"
        @like="handleLike"
        @delete="handleDelete"
        @reply="handleReply"
        @load-replies="handleLoadReplies"
        @edit="handleEditFromItem"
      />

      <!-- 加载状态 -->
      <div v-if="loading" class="flex flex-col items-center py-10 text-muted-foreground">
        <div class="w-8 h-8 border-3 border-border border-t-primary rounded-full animate-spin mb-3" />
        <span>加载中...</span>
      </div>

      <!-- 空状态 -->
      <div v-else-if="reviews.length === 0" class="flex flex-col items-center py-16 text-muted-foreground">
        <MessageSquare :size="48" class="mb-4 opacity-50" />
        <p class="text-sm">暂无评价，快来发表第一条评论吧！</p>
      </div>

      <!-- 加载更多 -->
      <button
        v-else-if="hasMore"
        class="w-full py-3 mt-5 border border-border/40 bg-card text-muted-foreground text-sm rounded-lg hover:bg-muted/50 transition-colors"
        @click="loadMore"
      >
        加载更多
      </button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { MessageSquare } from 'lucide-vue-next'
import type { SubmitReviewRequest, ReviewSortType, Review } from '~/types/review'

interface Props {
  toolId?: number
}

const props = defineProps<Props>()

const userStore = useUserStore()
const toast = useToast()

const {
  reviews,
  loading,
  hasMore,
  sortBy,
  stats,
  submitReview,
  editReview,
  toggleLike,
  deleteReview,
  setSortBy: setSort,
  loadMore,
  hasReviewed,
  fetchReviews,
  fetchStats
} = useReview({ toolId: props.toolId })

const sortOptions = [
  { label: '最新', value: 'newest' as ReviewSortType },
  { label: '最热', value: 'hottest' as ReviewSortType }
]

const isLoggedIn = computed(() => userStore.isLoggedIn)
const hasUserReviewed = ref(false)
const editingReview = ref(false)
const editingReviewData = ref<{
  reviewId: number
  rating: number
  content: string
  imageUrls: string[]
} | null>(null)

// 检查是否已评论
const checkHasReviewed = async () => {
  if (isLoggedIn.value) {
    hasUserReviewed.value = await hasReviewed()
  } else {
    hasUserReviewed.value = false
  }
}

// 组件挂载时检查
onMounted(() => {
  checkHasReviewed()
})

// 监听登录状态变化
watch(isLoggedIn, () => {
  checkHasReviewed()
})

// 切换排序
const setSortBy = (sort: ReviewSortType) => {
  setSort(sort)
}

// 提交评论
const handleSubmit = async (data: SubmitReviewRequest) => {
  try {
    await submitReview(data)
    hasUserReviewed.value = true
    toast.success('评论发表成功！')
  } catch (error: any) {
    toast.error(error.message || '评论发表失败')
  }
}

// 开始编辑（从"编辑评价"按钮）
const startEdit = () => {
  // 找到用户自己的评论
  const userReview = reviews.value.find(r => r.isOwner)
  if (userReview) {
    startEditReview(userReview)
  }
}

// 从评论项开始编辑
const handleEditFromItem = (reviewId: number) => {
  const review = reviews.value.find(r => r.id === reviewId)
  if (review) {
    startEditReview(review)
  }
}

// 开始编辑评论
const editFormRef = ref<HTMLElement>()

const startEditReview = (review: Review) => {
  editingReviewData.value = {
    reviewId: review.id,
    rating: review.rating || 5,
    content: review.content,
    imageUrls: review.imageUrls || []
  }
  editingReview.value = true
  // 滚动到编辑表单
  nextTick(() => {
    editFormRef.value?.scrollIntoView({ behavior: 'smooth', block: 'center' })
  })
}

// 取消编辑
const cancelEdit = () => {
  editingReview.value = false
  editingReviewData.value = null
}

// 提交编辑
const handleEditSubmit = async (data: SubmitReviewRequest) => {
  if (!editingReviewData.value) return

  try {
    await editReview(editingReviewData.value.reviewId, data)
    editingReview.value = false
    editingReviewData.value = null
    toast.success('评论修改成功！')
  } catch (error: any) {
    toast.error(error.message || '评论修改失败')
  }
}

// 点赞
const handleLike = async (reviewId: number) => {
  if (!isLoggedIn.value) {
    showLoginModal()
    return
  }

  try {
    await toggleLike(reviewId)
  } catch (error: any) {
    toast.error(error.message || '操作失败')
  }
}

// 删除评论
const handleDelete = async (reviewId: number) => {
  try {
    await deleteReview(reviewId)
    // 重新检查用户是否还有评论
    hasUserReviewed.value = await hasReviewed()
    // 如果正在编辑被删除的评论，取消编辑状态
    if (editingReview.value && editingReviewData.value?.reviewId === reviewId) {
      editingReview.value = false
      editingReviewData.value = null
    }
    toast.success('评论已删除')
  } catch (error: any) {
    toast.error(error.message || '删除失败')
  }
}

// 回复评论
const handleReply = async (data: SubmitReviewRequest) => {
  if (!isLoggedIn.value) {
    showLoginModal()
    return
  }

  try {
    await submitReview(data)
    toast.success('回复成功！')
  } catch (error: any) {
    toast.error(error.message || '回复失败')
  }
}

// 加载更多回复
const handleLoadReplies = (reviewId: number) => {
  // 这里可以实现加载更多回复的逻辑
  console.log('加载回复:', reviewId)
}

// 显示登录弹窗
const showLoginModal = () => {
  userStore.setLoginModalVisible(true)
}
</script>
