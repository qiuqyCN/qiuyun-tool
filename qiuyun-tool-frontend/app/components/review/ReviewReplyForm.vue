<template>
  <div class="ml-13 mt-3 p-3 bg-card border border-border rounded-lg">
    <Textarea
      v-model="content"
      rows="2"
      placeholder="写下你的回复..."
      maxlength="500"
      class="min-h-[60px] resize-vertical pb-2"
    />

    <!-- Emoji 选择器弹窗 -->
    <EmojiPicker
      v-model="showEmojiPicker"
      class-name="mt-2"
      @select="insertEmoji"
    />

    <div class="flex justify-between items-center mt-2">
      <!-- 左侧工具按钮 -->
      <div class="flex items-center gap-1">
        <!-- Emoji 按钮 -->
        <button
          class="flex items-center gap-1.5 px-3 py-1.5 text-sm text-muted-foreground hover:text-foreground hover:bg-muted rounded-md transition-colors"
          :class="{ 'text-primary bg-primary/10': showEmojiPicker }"
          @click="toggleEmojiPicker"
        >
          <Smile :size="16" />
          <span>表情</span>
        </button>
      </div>

      <!-- 右侧按钮组 -->
      <div class="flex items-center gap-2">
        <span class="text-xs text-muted-foreground">{{ content.length }}/500</span>
        <Button variant="outline" size="sm" @click="$emit('cancel')">取消</Button>
        <Button
          size="sm"
          :disabled="!canSubmit || submitting"
          @click="submit"
        >
          {{ submitting ? '发送中...' : '发送' }}
        </Button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { Smile } from 'lucide-vue-next'
import EmojiPicker from './EmojiPicker.vue'
import type { SubmitReviewRequest } from '~/types/review'

interface Props {
  parentId: number
  toolId: number
}

const props = defineProps<Props>()

const emit = defineEmits<{
  submit: [data: SubmitReviewRequest]
  cancel: []
}>()

const content = ref('')
const submitting = ref(false)
const showEmojiPicker = ref(false)

const canSubmit = computed(() => content.value.trim().length > 0)

// 切换 emoji 选择器
const toggleEmojiPicker = () => {
  showEmojiPicker.value = !showEmojiPicker.value
}

// 插入 emoji
const insertEmoji = (emoji: string) => {
  if (content.value.length < 500) {
    content.value += emoji
  }
}

const submit = async () => {
  if (!canSubmit.value || submitting.value) return

  submitting.value = true
  try {
    emit('submit', {
      toolId: props.toolId,
      content: content.value.trim(),
      parentId: props.parentId
    })
    content.value = ''
    showEmojiPicker.value = false
  } finally {
    submitting.value = false
  }
}
</script>
