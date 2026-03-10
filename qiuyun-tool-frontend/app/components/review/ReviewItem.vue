<template>
  <div class="py-5 border-b border-border/40" :class="{ 'p-4 bg-muted/30 rounded-lg mt-3 border-b-0': isReply }">
    <!-- 用户信息 -->
    <div class="flex items-center gap-3 mb-3">
      <img :src="review.userAvatar || '/default-avatar.png'" class="w-10 h-10 rounded-full object-cover" alt="">
      <div class="flex-1 flex flex-col">
        <div class="flex items-center gap-2">
          <span class="text-sm font-medium text-foreground">{{ review.userNickname || '用户' }}</span>
          <span v-if="review.reviewType === ReviewType.REPLY" class="text-xs px-1.5 py-0.5 bg-primary/10 text-primary rounded">回复</span>
        </div>
        <span class="text-xs text-muted-foreground">{{ formatTime(review.createdAt) }}</span>
      </div>
      <StarRating v-if="review.rating" :rating="review.rating" :readonly="true" />
    </div>

    <!-- 评论内容 -->
    <div class="ml-13 mb-3">
      <p class="text-sm leading-relaxed text-foreground whitespace-pre-wrap">{{ review.content }}</p>

      <!-- 图片列表 - 只有评论显示图片 -->
      <div v-if="review.imageUrls?.length && review.reviewType !== ReviewType.REPLY" class="flex flex-wrap gap-2 mt-3">
        <img
          v-for="(url, index) in review.imageUrls"
          :key="index"
          :src="url"
          alt=""
          class="w-24 h-24 object-cover rounded-lg cursor-pointer hover:opacity-90 transition-opacity"
          @click="previewImage(url)"
        >
      </div>
    </div>

    <!-- 操作栏 -->
    <div class="flex gap-4 ml-13">
      <button
        class="flex items-center gap-1 px-3 py-1.5 text-sm text-muted-foreground hover:bg-muted hover:text-foreground rounded-md transition-colors"
        :class="{ 'text-primary': review.isLiked }"
        @click="handleLike"
      >
        <ThumbsUp :size="16" :class="{ 'fill-primary': review.isLiked }" />
        <span>{{ review.likeCount || 0 }}</span>
      </button>

      <button
        v-if="!isReply"
        class="flex items-center gap-1 px-3 py-1.5 text-sm text-muted-foreground hover:bg-muted hover:text-foreground rounded-md transition-colors"
        @click="showReplyForm = !showReplyForm"
      >
        <MessageCircle :size="16" />
        <span>{{ review.replyCount || 0 }}</span>
      </button>

      <!-- 编辑按钮 - 只有自己的评论显示 -->
      <button
        v-if="review.isOwner && !isReply"
        class="flex items-center gap-1 px-3 py-1.5 text-sm text-muted-foreground hover:bg-muted hover:text-primary rounded-md transition-colors"
        @click="handleEdit"
      >
        <Pencil :size="16" />
        <span>编辑</span>
      </button>

      <button
        v-if="review.isOwner"
        class="flex items-center gap-1 px-3 py-1.5 text-sm text-muted-foreground hover:bg-muted hover:text-destructive rounded-md transition-colors"
        @click="handleDelete"
      >
        <Trash2 :size="16" />
        <span>删除</span>
      </button>
    </div>

    <!-- 回复列表 -->
    <div v-if="!isReply && review.replies?.length" class="ml-13 mt-4">
      <ReviewItem
        v-for="reply in review.replies"
        :key="reply.id"
        :review="reply"
        :is-reply="true"
        @like="$emit('like', $event)"
        @delete="$emit('delete', $event)"
      />

      <button
        v-if="review.replyCount > review.replies.length"
        class="mt-3 px-4 py-2 text-sm text-primary hover:underline"
        @click="$emit('loadReplies', review.id)"
      >
        查看更多回复 ({{ review.replyCount - review.replies.length }})
      </button>
    </div>

    <!-- 回复表单 -->
    <ReviewReplyForm
      v-if="showReplyForm"
      :parent-id="review.id"
      :tool-id="review.toolId"
      @submit="handleReplySubmit"
      @cancel="showReplyForm = false"
    />
  </div>
</template>

<script setup lang="ts">
import { ThumbsUp, MessageCircle, Trash2, Pencil } from 'lucide-vue-next'
import StarRating from './StarRating.vue'
import type { Review, SubmitReviewRequest } from '~/types/review'
import { ReviewType } from '~/types/enums'

interface Props {
  review: Review
  isReply?: boolean
}

const props = withDefaults(defineProps<Props>(), {
  isReply: false
})

const emit = defineEmits<{
  like: [reviewId: number]
  delete: [reviewId: number]
  reply: [data: SubmitReviewRequest]
  loadReplies: [reviewId: number]
  edit: [reviewId: number]
}>()

const showReplyForm = ref(false)

// 格式化时间
const formatTime = (time: string) => {
  const date = new Date(time)
  const now = new Date()
  const diff = now.getTime() - date.getTime()

  const minutes = Math.floor(diff / 60000)
  const hours = Math.floor(diff / 3600000)
  const days = Math.floor(diff / 86400000)

  if (minutes < 1) return '刚刚'
  if (minutes < 60) return `${minutes}分钟前`
  if (hours < 24) return `${hours}小时前`
  if (days < 30) return `${days}天前`

  return date.toLocaleDateString('zh-CN')
}

// 点赞
const handleLike = () => {
  emit('like', props.review.id)
}

// 编辑
const handleEdit = () => {
  emit('edit', props.review.id)
}

// 删除
const handleDelete = () => {
  if (confirm('确定要删除这条评论吗？')) {
    emit('delete', props.review.id)
  }
}

// 提交回复
const handleReplySubmit = (data: SubmitReviewRequest) => {
  emit('reply', data)
  showReplyForm.value = false
}

// 预览图片
const previewImage = (url: string) => {
  // 可以实现图片预览功能
  window.open(url, '_blank')
}
</script>
