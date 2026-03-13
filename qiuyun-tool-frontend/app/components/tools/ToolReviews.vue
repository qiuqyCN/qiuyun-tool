<template>
  <div class="space-y-6">
    <!-- 评分摘要 -->
    <div class="flex items-center gap-6 pb-6 border-b border-gray-200">
      <div class="text-center">
        <div class="text-4xl font-bold text-gray-900">{{ averageRating.toFixed(1) }}</div>
        <div class="flex items-center gap-1 mt-1">
          <Icon
            v-for="i in 5"
            :key="i"
            name="lucide:star"
            class="w-4 h-4"
            :class="i <= Math.round(averageRating) ? 'text-yellow-400 fill-yellow-400' : 'text-gray-300'"
          />
        </div>
        <div class="text-sm text-gray-500 mt-1">{{ totalReviews }} 条评价</div>
      </div>
      <div class="flex-1 space-y-1">
        <div v-for="star in 5" :key="star" class="flex items-center gap-2">
          <span class="text-sm text-gray-500 w-8">{{ 6 - star }}星</span>
          <div class="flex-1 h-2 bg-gray-200 rounded-full overflow-hidden">
            <div
              class="h-full bg-yellow-400 rounded-full"
              :style="{ width: `${getRatingPercentage(6 - star)}%` }"
            />
          </div>
          <span class="text-sm text-gray-500 w-12 text-right">{{ getRatingCount(6 - star) }}</span>
        </div>
      </div>
    </div>

    <!-- 评价列表 -->
    <div class="space-y-4">
      <div
        v-for="review in reviews"
        :key="review.id"
        class="border-b border-gray-100 last:border-0 pb-4 last:pb-0"
      >
        <div class="flex items-start gap-3">
          <Avatar class="w-10 h-10">
            <AvatarImage :src="review.userAvatar || ''" :alt="review.userName" />
            <AvatarFallback>{{ review.userName.charAt(0) }}</AvatarFallback>
          </Avatar>
          <div class="flex-1">
            <div class="flex items-center justify-between">
              <div class="flex items-center gap-2">
                <span class="font-medium text-gray-900">{{ review.userName }}</span>
                <div class="flex items-center gap-0.5">
                  <Icon
                    v-for="i in 5"
                    :key="i"
                    name="lucide:star"
                    class="w-3 h-3"
                    :class="i <= review.rating ? 'text-yellow-400 fill-yellow-400' : 'text-gray-300'"
                  />
                </div>
              </div>
              <span class="text-sm text-gray-400">{{ formatDate(review.createdAt) }}</span>
            </div>
            <p class="text-gray-600 mt-2">{{ review.content }}</p>
            <div class="flex items-center gap-4 mt-3">
              <button
                class="flex items-center gap-1 text-sm text-gray-500 hover:text-gray-700 transition-colors"
                @click="likeReview(review.id)"
              >
                <Icon name="lucide:thumbs-up" class="w-4 h-4" />
                <span>{{ review.likes }}</span>
              </button>
              <button
                class="flex items-center gap-1 text-sm text-gray-500 hover:text-gray-700 transition-colors"
              >
                <Icon name="lucide:message-circle" class="w-4 h-4" />
                <span>回复</span>
              </button>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 加载更多 -->
    <div v-if="hasMore" class="text-center">
      <Button variant="outline" :loading="loading" @click="loadMore">
        加载更多
      </Button>
    </div>

    <!-- 写评价 -->
    <div v-if="userStore.isLoggedIn" class="pt-6 border-t border-gray-200">
      <h4 class="font-medium text-gray-900 mb-4">写评价</h4>
      <div class="space-y-4">
        <div>
          <Label class="mb-2 block">您的评分</Label>
          <div class="flex items-center gap-2">
            <button
              v-for="i in 5"
              :key="i"
              @click="userRating = i"
              class="p-1 transition-colors"
            >
              <Icon
                name="lucide:star"
                class="w-8 h-8"
                :class="i <= userRating ? 'text-yellow-400 fill-yellow-400' : 'text-gray-300'"
              />
            </button>
          </div>
        </div>
        <div>
          <Label class="mb-2 block">评价内容</Label>
          <Textarea
            v-model="userReview"
            placeholder="分享您的使用体验..."
            rows="4"
            maxlength="500"
          />
          <div class="text-xs text-gray-400 mt-1 text-right">
            {{ userReview.length }}/500
          </div>
        </div>
        <Button
          :disabled="userRating === 0 || !userReview.trim()"
          :loading="submitting"
          @click="submitReview"
        >
          提交评价
        </Button>
      </div>
    </div>
    <div v-else class="text-center py-6 border-t border-gray-200">
      <p class="text-gray-500 mb-4">登录后可以发表评价</p>
      <Button @click="showLoginDialog = true">登录</Button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { useUserStore } from '~/stores/userStore'

interface Review {
  id: string
  userId: string
  userName: string
  userAvatar?: string
  rating: number
  content: string
  likes: number
  createdAt: string
}

interface Props {
  toolCode: string
}

const props = defineProps<Props>()

const userStore = useUserStore()

// 评价列表
const reviews = ref<Review[]>([
  // 示例数据
  {
    id: '1',
    userId: '1',
    userName: '张三',
    rating: 5,
    content: '非常好用的工具，界面简洁，处理速度快！',
    likes: 12,
    createdAt: '2026-03-01T10:00:00Z'
  },
  {
    id: '2',
    userId: '2',
    userName: '李四',
    rating: 4,
    content: '功能很强大，但是希望能增加更多格式支持。',
    likes: 8,
    createdAt: '2026-02-28T15:30:00Z'
  }
])

const loading = ref(false)
const hasMore = ref(true)
const page = ref(1)

// 用户评分
const userRating = ref(0)
const userReview = ref('')
const submitting = ref(false)
const showLoginDialog = ref(false)

// 计算平均分
const averageRating = computed(() => {
  if (reviews.value.length === 0) return 5.0
  const sum = reviews.value.reduce((acc, r) => acc + r.rating, 0)
  return sum / reviews.value.length
})

// 总评价数
const totalReviews = computed(() => reviews.value.length)

// 获取某个星级的数量
const getRatingCount = (star: number): number => {
  return reviews.value.filter(r => r.rating === star).length
}

// 获取某个星级的百分比
const getRatingPercentage = (star: number): number => {
  if (totalReviews.value === 0) return 0
  return (getRatingCount(star) / totalReviews.value) * 100
}

// 格式化日期
const formatDate = (dateStr: string): string => {
  const date = new Date(dateStr)
  const now = new Date()
  const diff = now.getTime() - date.getTime()
  const days = Math.floor(diff / (1000 * 60 * 60 * 24))

  if (days === 0) {
    const hours = Math.floor(diff / (1000 * 60 * 60))
    if (hours === 0) {
      const minutes = Math.floor(diff / (1000 * 60))
      return minutes === 0 ? '刚刚' : `${minutes}分钟前`
    }
    return `${hours}小时前`
  }
  if (days === 1) return '昨天'
  if (days < 7) return `${days}天前`
  if (days < 30) return `${Math.floor(days / 7)}周前`

  return date.toLocaleDateString('zh-CN')
}

// 加载更多
const loadMore = async () => {
  loading.value = true
  page.value++
  // TODO: 调用API加载更多评价
  loading.value = false
}

// 点赞评价
const likeReview = async (reviewId: string) => {
  if (!userStore.isLoggedIn) {
    showLoginDialog.value = true
    return
  }
  // TODO: 调用API点赞
}

// 提交评价
const submitReview = async () => {
  if (userRating.value === 0 || !userReview.value.trim()) return

  submitting.value = true
  try {
    // TODO: 调用API提交评价
    const newReview: Review = {
      id: Date.now().toString(),
      userId: userStore.currentUser?.id?.toString() || '',
      userName: userStore.currentUser?.nickname || '匿名用户',
      userAvatar: userStore.currentUser?.avatar,
      rating: userRating.value,
      content: userReview.value,
      likes: 0,
      createdAt: new Date().toISOString()
    }
    reviews.value.unshift(newReview)
    userRating.value = 0
    userReview.value = ''
  } finally {
    submitting.value = false
  }
}
</script>
