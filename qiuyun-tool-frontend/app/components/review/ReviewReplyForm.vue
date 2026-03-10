<template>
  <div class="ml-13 mt-3 p-3 bg-card border border-border rounded-lg">
    <Textarea
      v-model="content"
      rows="2"
      placeholder="写下你的回复..."
      maxlength="500"
      class="min-h-[60px] resize-vertical"
    />
    <div class="flex justify-between items-center mt-2">
      <span class="text-xs text-muted-foreground">{{ content.length }}/500</span>
      <div class="flex gap-2">
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

const canSubmit = computed(() => content.value.trim().length > 0)

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
  } finally {
    submitting.value = false
  }
}
</script>
