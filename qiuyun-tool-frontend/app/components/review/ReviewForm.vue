<template>
  <div class="p-5 bg-card border border-border/40 rounded-xl mb-6">
    <h3 class="text-lg font-semibold mb-5 text-foreground">
      {{ isEdit ? '编辑评论' : '发表评论' }}
    </h3>

    <!-- 评分选择 -->
    <div class="mb-5">
      <label class="block text-sm font-medium text-muted-foreground mb-2">评分</label>
      <div class="flex items-center gap-3">
        <StarRating
          :rating="form.rating"
          :readonly="false"
          @change="form.rating = $event"
        />
        <span v-if="form.rating > 0" class="text-sm text-amber-500">{{ ratingText }}</span>
      </div>
    </div>

    <!-- 评论内容 -->
    <div class="mb-0">
      <label class="block text-sm font-medium text-muted-foreground mb-2">评论内容</label>
      <div class="relative">
        <Textarea
          v-model="form.content"
          rows="4"
          placeholder="分享您的使用体验..."
          maxlength="500"
          class="resize-none pb-12"
        />
            <!-- 字符计数 -->
        <span class="absolute bottom-2 right-3 text-xs text-muted-foreground">{{ form.content.length }}/500</span>
      </div>
    </div>

    <!-- 已选图片预览（在输入框下方） -->
    <div v-if="form.imageUrls.length > 0" class="mt-2 flex flex-wrap gap-2">
      <div
        v-for="(url, index) in form.imageUrls"
        :key="index"
        class="relative w-20 h-20 rounded-lg overflow-hidden group border border-border/50"
      >
        <img :src="url" alt="" class="w-full h-full object-cover">
        <button
          class="absolute top-1 right-1 w-5 h-5 rounded-full bg-black/60 text-white flex items-center justify-center opacity-0 group-hover:opacity-100 transition-opacity"
          @click="removeImage(index)"
        >
          <X :size="12" />
        </button>
      </div>
    </div>

    <!-- Emoji 选择器弹窗 -->
    <div
      v-if="showEmojiPicker"
      ref="emojiPickerRef"
      class="mt-2 p-3 bg-popover border border-border rounded-xl shadow-lg relative z-50"
    >
      <!-- Emoji 分类标签 -->
      <div class="flex gap-1 mb-3 border-b border-border pb-2">
        <button
          v-for="category in emojiCategories"
          :key="category.name"
          class="px-3 py-1 text-sm rounded-md transition-colors"
          :class="currentEmojiCategory === category.name ? 'bg-primary text-primary-foreground' : 'text-muted-foreground hover:bg-muted'"
          @click="currentEmojiCategory = category.name"
        >
          {{ category.icon }}
        </button>
      </div>
      <!-- Emoji 网格 -->
      <div class="grid grid-cols-10 gap-1 max-h-48 overflow-y-auto">
        <button
          v-for="emoji in currentEmojis"
          :key="emoji"
          class="p-1.5 text-xl hover:bg-muted rounded-md transition-colors flex items-center justify-center"
          @click="insertEmoji(emoji)"
        >
          {{ emoji }}
        </button>
      </div>
    </div>

    <!-- 底部工具栏（Bilibili风格） -->
    <div class="flex items-center justify-between mt-3">
      <!-- 左侧工具按钮 -->
      <div class="flex items-center gap-1">
        <!-- Emoji 按钮 -->
        <button
          class="flex items-center gap-1.5 px-3 py-1.5 text-sm text-muted-foreground hover:text-foreground hover:bg-muted rounded-md transition-colors"
          :class="{ 'text-primary bg-primary/10': showEmojiPicker }"
          @click="toggleEmojiPicker"
        >
          <Smile :size="18" />
          <span>表情</span>
        </button>
        <!-- 图片上传按钮 -->
        <button
          class="flex items-center gap-1.5 px-3 py-1.5 text-sm text-muted-foreground hover:text-foreground hover:bg-muted rounded-md transition-colors"
          :class="{ 'text-primary bg-primary/10': form.imageUrls.length > 0 }"
          @click="triggerUpload"
        >
          <ImageIcon :size="18" />
          <span>图片</span>
          <span v-if="form.imageUrls.length > 0" class="text-xs">({{ form.imageUrls.length }}/9)</span>
        </button>
      </div>

      <!-- 右侧提交按钮 -->
      <div class="flex items-center gap-2">
        <Button
          v-if="isEdit"
          variant="ghost"
          size="sm"
          :disabled="submitting"
          @click="cancel"
        >
          取消
        </Button>
        <Button
          size="sm"
          :disabled="!canSubmit || submitting"
          @click="submit"
        >
          {{ submitting ? '提交中...' : (isEdit ? '保存' : '发布') }}
        </Button>
      </div>
    </div>

    <!-- 上传中提示 -->
    <div
      v-if="uploading"
      class="mt-3 p-3 bg-muted/50 rounded-lg flex items-center gap-3"
    >
      <div class="w-5 h-5 border-2 border-primary/30 border-t-primary rounded-full animate-spin" />
      <span class="text-sm text-muted-foreground">图片上传中...</span>
    </div>

    <input
      ref="fileInput"
      type="file"
      accept="image/*"
      multiple
      class="hidden"
      @change="handleFileChange"
    >
  </div>
</template>

<script setup lang="ts">
import { Smile, X, Image as ImageIcon } from 'lucide-vue-next'
import { onClickOutside } from '@vueuse/core'
import StarRating from './StarRating.vue'
import type { SubmitReviewRequest } from '~/types/review'

// Emoji 分类
const emojiCategories = [
  { name: '小黄脸', icon: '😀', emojis: ['😀', '😃', '😄', '😁', '😆', '😅', '🤣', '😂', '🙂', '🙃', '😉', '😊', '😇', '🥰', '😍', '🤩', '😘', '😗', '😚', '😙', '😋', '😛', '😜', '🤪', '😝', '🤑', '🤗', '🤭', '🤫', '🤔', '🤐', '🤨', '😐', '😑', '😶', '😏', '😒', '🙄', '😬', '🤥', '😌', '😔', '😪', '🤤', '😴', '😷', '🤒', '🤕', '🤢', '🤮', '🤧', '🥵', '🥶', '🥴', '😵', '🤯', '🤠', '🥳', '😎', '🤓', '🧐', '😕', '😟', '🙁', '☹️', '😮', '😯', '😲', '😳', '🥺', '😦', '😧', '😨', '😰', '😥', '😢', '😭', '😱', '😖', '😣', '😞', '😓', '😩', '😫', '🥱', '😤', '😡', '😠', '🤬', '😈', '👿', '💀', '☠️', '💩', '🤡', '👹', '👺', '👻', '👽', '👾', '🤖', '😺', '😸', '😹', '😻', '😼', '😽', '🙀', '😿', '😾'] },
  { name: '手势', icon: '👍', emojis: ['👍', '👎', '👏', '🙌', '🤝', '🤞', '✌️', '🤟', '🤘', '👌', '🤏', '👈', '👉', '👆', '👇', '☝️', '✋', '🤚', '🖐️', '🖖', '👋', '🤙', '💪', '🦾', '🖕', '✍️', '🤳', '💅', '🦵', '🦿', '🦶', '👂', '🦻', '👃', '🧠', '🦷', '🦴', '👀', '👁️', '👅', '👄', '💋', '🩸'] },
  { name: '爱心', icon: '❤️', emojis: ['❤️', '🧡', '💛', '💚', '💙', '💜', '🖤', '🤍', '🤎', '💔', '❣️', '💕', '💞', '💓', '💗', '💖', '💘', '💝', '💟', '☮️', '✝️', '☪️', '🕉️', '☸️', '✡️', '🔯', '🕎', '☯️', '☦️', '🛐', '⛎', '♈', '♉', '♊', '♋', '♌', '♍', '♎', '♏', '♐', '♑', '♒', '♓', '🆔', '⚛️', '🉑', '☢️', '☣️', '📴', '📳', '🈶', '🈚', '🈸', '🈺', '🈷️', '✴️', '🆚', '💮', '🉐', '㊙️', '㊗️', '🈴', '🈵', '🈹', '🈲', '🅰️', '🅱️', '🆎', '🆑', '🅾️', '🆘', '❌', '⭕', '🛑', '⛔', '📛', '🚫', '💯', '💢', '♨️', '🚷', '🚯', '🚳', '🚱', '🔞', '📵', '🚭', '❗', '❕', '❓', '❔', '‼️', '⁉️', '🔅', '🔆', '〽️', '⚠️', '🚸', '🔱', '⚜️', '🔰', '♻️', '✅', '🈯', '💹', '❇️', '✳️', '❎', '🌐', '💠', 'Ⓜ️', '🌀', '💤', '🏧', '🚾', '♿', '🅿️', '🈳', '🈂', '🛂', '🛃', '🛄', '🛅', '🛗', '🚹', '🚺', '🚼', '⚧', '🚻', '🚮', '🎦', '📶', '🈁', '🔣', 'ℹ️', '🔤', '🔡', '🔠', '🆖', '🆗', '🆙', '🆒', '🆕', '🆓', '0️⃣', '1️⃣', '2️⃣', '3️⃣', '4️⃣', '5️⃣', '6️⃣', '7️⃣', '8️⃣', '9️⃣', '🔟', '🔢', '#️⃣', '*️⃣', '⏏️', '▶️', '⏸️', '⏯️', '⏹️', '⏺️', '⏭️', '⏮️', '⏩', '⏪', '⏫', '⏬', '◀️', '🔼', '🔽', '➡️', '⬅️', '⬆️', '⬇️', '↗️', '↘️', '↙️', '↖️', '↕️', '↔️', '↪️', '↩️', '⤴️', '⤵️', '🔀', '🔁', '🔂', '🔄', '🔃', '🎵', '🎶', '➕', '➖', '➗', '✖️', '💲', '💱', '™️', '©️', '®️', '〰️', '➰', '➿', '🔚', '🔙', '🔛', '🔝', '🔜', '✔️', '☑️', '🔘', '🔴', '🟠', '🟡', '🟢', '🔵', '🟣', '⚫', '⚪', '🟤', '🔺', '🔻', '🔸', '🔹', '🔶', '🔷', '🔳', '🔲', '▪️', '▫️', '◾', '◽', '◼️', '◻️', '🟥', '🟧', '🟨', '🟩', '🟦', '🟪', '⬛', '⬜', '🟫', '🔈', '🔇', '🔉', '🔊', '🔔', '🔕', '📣', '📢', '💬', '💭', '🗯️', '♠️', '♣️', '♥️', '♦️', '🃏', '🎴', '🀄', '🕐', '🕑', '🕒', '🕓', '🕔', '🕕', '🕖', '🕗', '🕘', '🕙', '🕚', '🕛', '🕜', '🕝', '🕞', '🕟', '🕠', '🕡', '🕢', '🕣', '🕤', '🕥', '🕦', '🕧'] },
  { name: '动物', icon: '🐱', emojis: ['🐶', '🐱', '🐭', '🐹', '🐰', '🦊', '🐻', '🐼', '🐨', '🐯', '🦁', '🐮', '🐷', '🐽', '🐸', '🐵', '🙈', '🙉', '🙊', '🐒', '🐔', '🐧', '🐦', '🐤', '🐣', '🐥', '🦆', '🦅', '🦉', '🦇', '🐺', '🐗', '🐴', '🦄', '🐝', '🐛', '🦋', '🐌', '🐞', '🐜', '🦟', '🦗', '🕷️', '🕸️', '🦂', '🐢', '🐍', '🦎', '🦖', '🦕', '🐙', '🦑', '🦐', '🦞', '🦀', '🐡', '🐠', '🐟', '🐬', '🐳', '🐋', '🦈', '🐊', '🐅', '🐆', '🦓', '🦍', '🦧', '🐘', '🦛', '🦏', '🐪', '🐫', '🦒', '🦘', '🐃', '🐂', '🐄', '🐎', '🐖', '🐏', '🐑', '🦙', '🐐', '🦌', '🐕', '🐩', '🦮', '🐕‍🦺', '🐈', '🐈‍⬛', '🐓', '🦃', '🦚', '🦜', '🦢', '🦩', '🕊️', '🐇', '🦝', '🦨', '🦡', '🦦', '🦥', '🐁', '🐀', '🐿️', '🦔'] },
  { name: '食物', icon: '🍎', emojis: ['🍏', '🍎', '🍐', '🍊', '🍋', '🍌', '🍉', '🍇', '🍓', '🍈', '🍒', '🍑', '🍍', '🥝', '🥑', '🍅', '🍆', '🥒', '🥕', '🌽', '🌶️', '🥬', '🥦', '🧄', '🧅', '🍄', '🥜', '🌰', '🍞', '🥐', '🥖', '🥨', '🥯', '🥞', '🧇', '🧀', '🍖', '🍗', '🥩', '🥓', '🍔', '🍟', '🍕', '🌭', '🥪', '🌮', '🌯', '🥙', '🧆', '🥚', '🍳', '🥘', '🍲', '🥣', '🥗', '🍿', '🧈', '🧂', '🥫', '🍱', '🍘', '🍙', '🍚', '🍛', '🍜', '🍝', '🍠', '🍢', '🍣', '🍤', '🍥', '🍡', '🍦', '🍧', '🍨', '🍩', '🍪', '🎂', '🍰', '🧁', '🥧', '🍫', '🍬', '🍭', '🍮', '🍯', '🍼', '🥛', '☕', '🍵', '🧃', '🥤', '🍶', '🍺', '🍻', '🥂', '🍷', '🥃', '🍸', '🍹', '🧉', '🍾', '🧊', '🥄', '🍴', '🍽️', '🥣', '🥡', '🥢', '🧂'] },
]

interface Props {
  toolId: number
  isEdit?: boolean
  initialData?: {
    rating: number
    content: string
    imageUrls: string[]
  } | null
}

const props = withDefaults(defineProps<Props>(), {
  isEdit: false,
  initialData: null
})

const emit = defineEmits<{
  submit: [data: SubmitReviewRequest]
  cancel: []
}>()

const { $api } = useNuxtApp()
const toast = useToast()
const fileInput = ref<HTMLInputElement>()
const emojiPickerRef = ref<HTMLElement>()
const submitting = ref(false)
const uploading = ref(false)
const showEmojiPicker = ref(false)
const currentEmojiCategory = ref('小黄脸')

// 点击外部关闭 emoji 选择器
onClickOutside(emojiPickerRef, () => {
  showEmojiPicker.value = false
})

const form = reactive<SubmitReviewRequest & { imageUrls: string[] }>({
  rating: props.initialData?.rating || 0,
  content: props.initialData?.content || '',
  imageUrls: props.initialData?.imageUrls || [],
  toolId: props.toolId
})

// 当前分类的 emoji
const currentEmojis = computed(() => {
  const category = emojiCategories.find(c => c.name === currentEmojiCategory.value)
  return category?.emojis || []
})

// 监听 initialData 变化（编辑时更新表单）
watch(() => props.initialData, (newData) => {
  if (newData) {
    form.rating = newData.rating
    form.content = newData.content
    form.imageUrls = [...newData.imageUrls]
  }
}, { immediate: true })

// 评分文本
const ratingText = computed(() => {
  const texts = ['', '非常差', '差', '一般', '好', '非常好']
  return texts[form.rating] || ''
})

// 是否可以提交
const canSubmit = computed(() => {
  return form.rating > 0 && form.content.trim().length > 0
})

// 切换 emoji 选择器
const toggleEmojiPicker = () => {
  showEmojiPicker.value = !showEmojiPicker.value
}

// 插入 emoji
const insertEmoji = (emoji: string) => {
  if (form.content.length < 500) {
    form.content += emoji
  }
}

// 触发文件选择
const triggerUpload = () => {
  fileInput.value?.click()
}

// 处理文件选择
const handleFileChange = async (e: Event) => {
  const files = (e.target as HTMLInputElement).files
  if (!files || files.length === 0) return

  uploading.value = true

  try {
    for (let i = 0; i < files.length; i++) {
      if (form.imageUrls.length >= 9) {
        toast.warning('最多只能上传9张图片')
        break
      }

      const file = files[i]
      if (!file) continue

      // 验证文件类型
      if (!file.type.startsWith('image/')) {
        toast.error(`${file.name} 不是图片文件`)
        continue
      }

      // 验证文件大小（5MB）
      if (file.size > 5 * 1024 * 1024) {
        toast.error(`${file.name} 超过5MB限制`)
        continue
      }

      // 上传图片到服务器
      const formData = new FormData()
      formData.append('file', file)

      const response = await $api('/upload/image', {
        method: 'POST',
        body: formData
      }) as { code: number; data: { url: string }; message: string }

      if (response.code === 200 && response.data?.url) {
        form.imageUrls.push(response.data.url)
        toast.success('图片上传成功')
      } else {
        toast.error(response.message || '上传失败')
      }
    }
  } catch (error: any) {
    console.error('上传图片失败:', error)
    toast.error(error.message || '上传图片失败')
  } finally {
    uploading.value = false
    // 清空input
    if (fileInput.value) {
      fileInput.value.value = ''
    }
  }
}

// 移除图片
const removeImage = (index: number) => {
  form.imageUrls.splice(index, 1)
}

// 取消编辑
const cancel = () => {
  emit('cancel')
}

// 提交评论
const submit = async () => {
  if (!canSubmit.value || submitting.value) return

  submitting.value = true
  try {
    emit('submit', { ...form })
    // 如果不是编辑模式，重置表单
    if (!props.isEdit) {
      form.rating = 0
      form.content = ''
      form.imageUrls = []
      showEmojiPicker.value = false
    }
  } finally {
    submitting.value = false
  }
}

</script>
