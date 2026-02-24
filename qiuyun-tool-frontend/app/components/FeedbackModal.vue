<script setup lang="ts">
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogHeader,
  DialogTitle,
} from '@/components/ui/dialog'
import { Button } from '@/components/ui/button'
import { Input } from '@/components/ui/input'
import { Label } from '@/components/ui/label'
import { Textarea } from '@/components/ui/textarea'
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from '@/components/ui/select'
import { MessageCircle, CheckCircle } from 'lucide-vue-next'

const props = defineProps<{
  open: boolean
}>()

const emit = defineEmits<{
  'update:open': [value: boolean]
}>()

const feedbackTypes = [
  { value: 'bug', label: '功能异常' },
  { value: 'feature', label: '功能建议' },
  { value: 'content', label: '内容问题' },
  { value: 'other', label: '其他问题' },
]

const form = ref({
  type: '',
  title: '',
  content: '',
  contact: '',
})

const isSubmitting = ref(false)
const isSuccess = ref(false)

const handleSubmit = async () => {
  isSubmitting.value = true
  // 模拟提交
  await new Promise(resolve => setTimeout(resolve, 1000))
  isSubmitting.value = false
  isSuccess.value = true
  
  // 重置表单
  setTimeout(() => {
    isSuccess.value = false
    form.value = { type: '', title: '', content: '', contact: '' }
    emit('update:open', false)
  }, 2000)
}

const handleClose = () => {
  emit('update:open', false)
  isSuccess.value = false
  form.value = { type: '', title: '', content: '', contact: '' }
}
</script>

<template>
  <Dialog :open="open" @update:open="handleClose">
    <DialogContent class="sm:max-w-[480px]">
      <DialogHeader>
        <DialogTitle class="flex items-center gap-2">
          <MessageCircle class="w-5 h-5" />
          问题反馈
        </DialogTitle>
        <DialogDescription>
          请描述您遇到的问题或建议，我们会尽快处理。
        </DialogDescription>
      </DialogHeader>

      <div v-if="isSuccess" class="py-8 text-center">
        <CheckCircle class="w-16 h-16 text-green-500 mx-auto mb-4" />
        <h3 class="text-lg font-semibold text-foreground mb-2">提交成功</h3>
        <p class="text-sm text-muted-foreground">感谢您的反馈，我们会尽快处理！</p>
      </div>

      <form v-else @submit.prevent="handleSubmit" class="space-y-4">
        <div class="space-y-2">
          <Label for="type">反馈类型</Label>
          <Select v-model="form.type" required>
            <SelectTrigger>
              <SelectValue placeholder="请选择反馈类型" />
            </SelectTrigger>
            <SelectContent>
              <SelectItem 
                v-for="type in feedbackTypes" 
                :key="type.value" 
                :value="type.value"
              >
                {{ type.label }}
              </SelectItem>
            </SelectContent>
          </Select>
        </div>

        <div class="space-y-2">
          <Label for="title">标题</Label>
          <Input 
            id="title" 
            v-model="form.title" 
            placeholder="请简要描述问题"
            required
          />
        </div>

        <div class="space-y-2">
          <Label for="content">详细描述</Label>
          <Textarea 
            id="content" 
            v-model="form.content" 
            placeholder="请详细描述您遇到的问题或建议..."
            rows="4"
            required
          />
        </div>

        <div class="space-y-2">
          <Label for="contact">联系方式（选填）</Label>
          <Input 
            id="contact" 
            v-model="form.contact" 
            placeholder="邮箱或手机号，方便我们联系您"
          />
        </div>

        <div class="flex justify-end gap-2 pt-4">
          <Button type="button" variant="outline" @click="handleClose">
            取消
          </Button>
          <Button type="submit" :disabled="isSubmitting">
            {{ isSubmitting ? '提交中...' : '提交反馈' }}
          </Button>
        </div>
      </form>
    </DialogContent>
  </Dialog>
</template>
