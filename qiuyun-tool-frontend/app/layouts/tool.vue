<template>
  <div class="min-h-screen bg-background">
    <!-- Breadcrumb -->
    <div class="border-b border-border/40 bg-muted/30">
      <div class="container mx-auto px-4 py-4">
        <div class="flex items-center gap-2 text-sm text-muted-foreground">
          <NuxtLink to="/" class="hover:text-foreground">首页</NuxtLink>
          <ChevronRight class="w-4 h-4" />
          <NuxtLink :to="`/category/${category?.code}`" class="hover:text-foreground">{{ category?.name }}</NuxtLink>
          <ChevronRight class="w-4 h-4" />
          <span class="text-foreground">{{ tool?.name }}</span>
        </div>
      </div>
    </div>

    <div class="container mx-auto px-4 py-8">
      <!-- Tool Header -->
      <div class="flex flex-col lg:flex-row lg:items-start gap-6 mb-8">
        <div
          class="w-20 h-20 rounded-2xl flex items-center justify-center shrink-0"
          :style="{ backgroundColor: tool?.iconBgColor || 'hsl(var(--primary) / 0.1)' }"
        >
          <component
            :is="getToolIcon(tool?.icon)"
            class="w-10 h-10"
            :style="{ color: tool?.iconColor || 'hsl(var(--primary))' }"
          />
        </div>

        <div class="flex-1">
          <div class="flex flex-wrap items-center gap-3 mb-2">
            <h1 class="text-3xl font-bold text-foreground">{{ tool?.name }}</h1>
            <Badge v-if="tool?.isHot" variant="destructive">HOT</Badge>
            <Badge v-if="tool?.priceMode === 'vip'" variant="secondary" class="bg-gradient-to-r from-amber-400 to-orange-500 text-white border-0">
              <Crown class="w-3 h-3 mr-1" />
              VIP
            </Badge>
            <Badge v-else variant="secondary" class="bg-green-100 text-green-700 border-0">
              <CheckCircle class="w-3 h-3 mr-1" />
              免费
            </Badge>
          </div>

          <p class="text-muted-foreground mb-4">{{ tool?.description }}</p>

          <div class="flex flex-wrap items-center gap-4 text-sm text-muted-foreground">
            <div class="flex items-center gap-1">
              <TrendingUp class="w-4 h-4" />
              <span>{{ formatNumber(tool?.viewCount || 0) }} 次访问</span>
            </div>
            <div class="flex items-center gap-1">
              <Users class="w-4 h-4" />
              <span>{{ formatNumber(tool?.usageCount || 0) }} 次使用</span>
            </div>
            <div class="flex items-center gap-1">
              <Star class="w-4 h-4 text-yellow-500" />
              <span>{{ tool?.rating?.toFixed(1) || '5.0' }} 分</span>
            </div>
            <div class="flex items-center gap-1">
              <MessageCircle class="w-4 h-4" />
              <span>{{ tool?.reviewCount || 0 }} 条评价</span>
            </div>
          </div>
        </div>

        <div class="flex items-center gap-2">
          <Button
            variant="outline"
            size="icon"
            :class="isFavorite ? 'text-red-500' : ''"
            @click="toggleFavorite"
          >
            <Heart class="w-4 h-4" :class="isFavorite ? 'fill-current' : ''" />
          </Button>
          <Button variant="outline" size="icon" @click="shareTool">
            <Share2 class="w-4 h-4" />
          </Button>
          <Button variant="outline" @click="showFeedback = true">
            <MessageSquare class="w-4 h-4 mr-2" />
            反馈
          </Button>
        </div>
      </div>

      <!-- Main Content -->
      <div class="grid grid-cols-1 lg:grid-cols-3 gap-8">
        <!-- Left: Tool Execution Area (Slot) -->
        <div class="lg:col-span-2">
          <slot />
        </div>

        <!-- Right: Info Panel -->
        <div class="space-y-6">
          <!-- Statistics -->
          <div class="border border-border/40 rounded-xl p-6">
            <h3 class="font-semibold mb-4">工具统计</h3>
            <div class="grid grid-cols-2 gap-4">
              <div class="text-center p-4 bg-muted/30 rounded-lg">
                <Eye class="w-6 h-6 text-primary mx-auto mb-2" />
                <div class="text-2xl font-bold">{{ formatNumber(tool?.viewCount || 0) }}</div>
                <div class="text-sm text-muted-foreground">总访问量</div>
              </div>
              <div class="text-center p-4 bg-muted/30 rounded-lg">
                <Users class="w-6 h-6 text-green-600 mx-auto mb-2" />
                <div class="text-2xl font-bold">{{ formatNumber(tool?.usageCount || 0) }}</div>
                <div class="text-sm text-muted-foreground">使用次数</div>
              </div>
              <div class="text-center p-4 bg-muted/30 rounded-lg">
                <Star class="w-6 h-6 text-yellow-500 mx-auto mb-2" />
                <div class="text-2xl font-bold">{{ tool?.rating?.toFixed(1) || '5.0' }}</div>
                <div class="text-sm text-muted-foreground">用户评分</div>
              </div>
              <div class="text-center p-4 bg-muted/30 rounded-lg">
                <Heart class="w-6 h-6 text-red-500 mx-auto mb-2" />
                <div class="text-2xl font-bold">{{ formatNumber(tool?.favoriteCount || 0) }}</div>
                <div class="text-sm text-muted-foreground">收藏数</div>
              </div>
            </div>
          </div>

          <!-- Instructions -->
          <div v-if="tool?.instructions" class="border border-border/40 rounded-xl p-6">
            <h3 class="font-semibold mb-4">使用说明</h3>
            <div class="prose prose-sm max-w-none text-muted-foreground" v-html="tool.instructions"></div>
          </div>

          <!-- Reviews -->
          <div class="border border-border/40 rounded-xl p-6">
            <div class="flex items-center justify-between mb-4">
              <h3 class="font-semibold">用户评价</h3>
              <Button v-if="userStore.isLoggedIn" variant="ghost" size="sm" @click="showReviewModal = true">
                <PenLine class="w-4 h-4 mr-1" />
                写评价
              </Button>
              <Button v-else variant="ghost" size="sm" @click="showLoginModal = true">
                登录后评价
              </Button>
            </div>

            <!-- Rating Summary -->
            <div class="flex items-center gap-4 mb-4 pb-4 border-b">
              <div class="text-3xl font-bold">{{ averageRating.toFixed(1) }}</div>
              <div>
                <div class="flex items-center gap-1">
                  <Star
                    v-for="i in 5"
                    :key="i"
                    class="w-4 h-4"
                    :class="i <= Math.round(averageRating) ? 'text-yellow-400 fill-yellow-400' : 'text-muted-foreground'"
                  />
                </div>
                <div class="text-sm text-muted-foreground">{{ reviews.length }} 条评价</div>
              </div>
            </div>

            <!-- Review List -->
            <div class="space-y-4 max-h-96 overflow-y-auto">
              <div
                v-for="review in displayedReviews"
                :key="review.id"
                class="border-b border-border/40 last:border-0 pb-4 last:pb-0"
              >
                <div class="flex items-start gap-3">
                  <Avatar class="w-8 h-8">
                    <AvatarImage :src="review.userAvatar" :alt="review.userName" />
                    <AvatarFallback>{{ review.userName.charAt(0) }}</AvatarFallback>
                  </Avatar>
                  <div class="flex-1 min-w-0">
                    <div class="flex items-center justify-between">
                      <span class="font-medium text-sm">{{ review.userName }}</span>
                      <span class="text-xs text-muted-foreground">{{ formatDate(review.createdAt) }}</span>
                    </div>
                    <div class="flex items-center gap-1 my-1">
                      <Star
                        v-for="i in 5"
                        :key="i"
                        class="w-3 h-3"
                        :class="i <= review.rating ? 'text-yellow-400 fill-yellow-400' : 'text-muted-foreground'"
                      />
                    </div>
                    <p class="text-sm text-muted-foreground">{{ review.content }}</p>
                    <div class="flex items-center gap-4 mt-2">
                      <button
                        class="flex items-center gap-1 text-xs text-muted-foreground hover:text-foreground"
                        @click="likeReview(review.id)"
                      >
                        <ThumbsUp class="w-3 h-3" />
                        {{ review.likes }}
                      </button>
                    </div>
                  </div>
                </div>
              </div>
            </div>

            <!-- Load More -->
            <div v-if="hasMoreReviews" class="text-center mt-4">
              <Button variant="ghost" size="sm" :loading="loadingReviews" @click="loadMoreReviews">
                加载更多
              </Button>
            </div>
          </div>

          <!-- Related Tools -->
          <div v-if="relatedTools.length > 0" class="border border-border/40 rounded-xl p-6">
            <h3 class="font-semibold mb-4">相关工具</h3>
            <div class="space-y-3">
              <NuxtLink
                v-for="related in relatedTools"
                :key="related.code"
                :to="`/${related.categoryCode}/${related.code}`"
                class="flex items-center gap-3 p-3 rounded-lg hover:bg-muted transition-colors group"
              >
                <div
                  class="w-10 h-10 rounded-lg flex items-center justify-center flex-shrink-0"
                  :style="{ backgroundColor: related.iconBgColor || 'hsl(var(--primary) / 0.1)' }"
                >
                  <component
                    :is="getToolIcon(related.icon)"
                    class="w-5 h-5"
                    :style="{ color: related.iconColor || 'hsl(var(--primary))' }"
                  />
                </div>
                <div class="flex-1 min-w-0">
                  <div class="text-sm font-medium group-hover:text-primary transition-colors truncate">
                    {{ related.name }}
                  </div>
                  <div class="text-xs text-muted-foreground truncate">{{ related.description }}</div>
                </div>
                <ChevronRight class="w-4 h-4 text-muted-foreground" />
              </NuxtLink>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- Review Modal -->
    <Dialog v-model:open="showReviewModal">
      <DialogContent class="sm:max-w-md">
        <DialogHeader>
          <DialogTitle>评价 {{ tool?.name }}</DialogTitle>
          <DialogDescription>分享您的使用体验，帮助其他用户</DialogDescription>
        </DialogHeader>
        <div class="space-y-4 py-4">
          <div>
            <Label class="mb-2 block">您的评分</Label>
            <div class="flex items-center gap-2">
              <button
                v-for="i in 5"
                :key="i"
                @click="userRating = i"
                class="p-1 transition-colors"
              >
                <Star
                  class="w-8 h-8"
                  :class="i <= userRating ? 'text-yellow-400 fill-yellow-400' : 'text-muted-foreground'"
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
            <div class="text-xs text-muted-foreground mt-1 text-right">
              {{ userReview.length }}/500
            </div>
          </div>
        </div>
        <DialogFooter>
          <Button variant="outline" @click="showReviewModal = false">取消</Button>
          <Button :disabled="userRating === 0" :loading="submittingReview" @click="submitReview">
            提交评价
          </Button>
        </DialogFooter>
      </DialogContent>
    </Dialog>

    <!-- Feedback Modal -->
    <Dialog v-model:open="showFeedback">
      <DialogContent class="sm:max-w-md">
        <DialogHeader>
          <DialogTitle>问题反馈</DialogTitle>
          <DialogDescription>如果您在使用工具过程中遇到问题，请告诉我们</DialogDescription>
        </DialogHeader>
        <div class="space-y-4 py-4">
          <div>
            <Label>反馈类型</Label>
            <Select v-model="feedbackType">
              <SelectTrigger>
                <SelectValue placeholder="请选择反馈类型" />
              </SelectTrigger>
              <SelectContent>
                <SelectItem value="bug">功能异常</SelectItem>
                <SelectItem value="suggestion">功能建议</SelectItem>
                <SelectItem value="other">其他问题</SelectItem>
              </SelectContent>
            </Select>
          </div>
          <div>
            <Label>详细描述</Label>
            <Textarea
              v-model="feedbackContent"
              placeholder="请详细描述您遇到的问题..."
              rows="4"
            />
          </div>
        </div>
        <DialogFooter>
          <Button variant="outline" @click="showFeedback = false">取消</Button>
          <Button :disabled="!feedbackType || !feedbackContent.trim()" @click="submitFeedback">
            提交反馈
          </Button>
        </DialogFooter>
      </DialogContent>
    </Dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, h } from 'vue'
import { useUserStore } from '~/stores/userStore'
import {
  ChevronRight,
  Crown,
  CheckCircle,
  TrendingUp,
  Users,
  Star,
  MessageCircle,
  Heart,
  Share2,
  MessageSquare,
  Eye,
  PenLine,
  ThumbsUp,
  Wrench,
  Braces,
  FileJson,
  Code2,
  Search,
  Image,
  FileText,
  Lock,
  Type,
  Calculator,
  Settings,
  Sparkles
} from 'lucide-vue-next'

// 图标映射表
const iconMap: Record<string, any> = {
  'lucide:braces': Braces,
  'lucide:file-json': FileJson,
  'lucide:code-2': Code2,
  'lucide:search': Search,
  'lucide:image': Image,
  'lucide:file-text': FileText,
  'lucide:lock': Lock,
  'lucide:type': Type,
  'lucide:calculator': Calculator,
  'lucide:wrench': Wrench,
  'lucide:settings': Settings,
  'lucide:sparkles': Sparkles,
  'lucide:tool': Wrench
}

// 获取图标组件
const getToolIcon = (iconName?: string) => {
  if (!iconName) return Wrench
  return iconMap[iconName] || Wrench
}

// Props
interface ToolInfo {
  code: string
  name: string
  description: string
  icon?: string
  iconColor?: string
  iconBgColor?: string
  priceMode: 'free' | 'vip'
  isHot?: boolean
  viewCount: number
  usageCount: number
  rating: number
  favoriteCount: number
  reviewCount: number
  instructions?: string
}

interface CategoryInfo {
  code: string
  name: string
}

interface RelatedTool {
  code: string
  name: string
  description: string
  icon?: string
  iconColor?: string
  iconBgColor?: string
  categoryCode: string
}

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

const props = defineProps<{
  tool: ToolInfo
  category: CategoryInfo
  relatedTools?: RelatedTool[]
}>()

const userStore = useUserStore()

// 收藏
const isFavorite = ref(false)
const toggleFavorite = () => {
  if (!userStore.isLoggedIn) {
    // 显示登录弹窗
    return
  }
  isFavorite.value = !isFavorite.value
  // TODO: 调用API
}

// 分享
const shareTool = async () => {
  const url = window.location.href
  const title = props.tool?.name || '秋云工具'

  if (navigator.share) {
    try {
      await navigator.share({ title, url })
    } catch (err) {
      // 用户取消
    }
  } else {
    try {
      await navigator.clipboard.writeText(url)
      // 显示成功提示
    } catch (err) {
      // 显示失败提示
    }
  }
}

// 格式化数字
const formatNumber = (num: number): string => {
  if (num >= 10000) {
    return (num / 10000).toFixed(1) + 'w'
  }
  if (num >= 1000) {
    return (num / 1000).toFixed(1) + 'k'
  }
  return num.toString()
}

// 评价
const reviews = ref<Review[]>([
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

const showReviewModal = ref(false)
const userRating = ref(0)
const userReview = ref('')
const submittingReview = ref(false)
const loadingReviews = ref(false)
const reviewPage = ref(1)
const reviewsPerPage = 5

const averageRating = computed(() => {
  if (reviews.value.length === 0) return 5.0
  const sum = reviews.value.reduce((acc, r) => acc + r.rating, 0)
  return sum / reviews.value.length
})

const displayedReviews = computed(() => {
  return reviews.value.slice(0, reviewPage.value * reviewsPerPage)
})

const hasMoreReviews = computed(() => {
  return displayedReviews.value.length < reviews.value.length
})

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

const loadMoreReviews = async () => {
  loadingReviews.value = true
  reviewPage.value++
  // TODO: 调用API加载更多
  loadingReviews.value = false
}

const likeReview = async (reviewId: string) => {
  if (!userStore.isLoggedIn) {
    // 显示登录弹窗
    return
  }
  // TODO: 调用API点赞
}

const submitReview = async () => {
  if (userRating.value === 0) return

  submittingReview.value = true
  try {
    // TODO: 调用API提交
    const newReview: Review = {
      id: Date.now().toString(),
      userId: userStore.userInfo?.id || '',
      userName: userStore.userInfo?.nickname || '匿名用户',
      userAvatar: userStore.userInfo?.avatar,
      rating: userRating.value,
      content: userReview.value,
      likes: 0,
      createdAt: new Date().toISOString()
    }
    reviews.value.unshift(newReview)
    userRating.value = 0
    userReview.value = ''
    showReviewModal.value = false
  } finally {
    submittingReview.value = false
  }
}

// 反馈
const showFeedback = ref(false)
const feedbackType = ref('')
const feedbackContent = ref('')

const submitFeedback = async () => {
  // TODO: 调用API提交反馈
  showFeedback.value = false
  feedbackType.value = ''
  feedbackContent.value = ''
}

// 相关工具
const relatedTools = computed(() => props.relatedTools || [])

// 登录弹窗
const showLoginModal = ref(false)
</script>
