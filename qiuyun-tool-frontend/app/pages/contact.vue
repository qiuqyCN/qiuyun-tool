<script setup lang="ts">
import { Button } from '@/components/ui/button'
import { Input } from '@/components/ui/input'
import { Textarea } from '@/components/ui/textarea'
import { ChevronRight, Mail, MapPin, Phone, Clock, Send, MessageSquare } from 'lucide-vue-next'

useHead({
  title: '联系我们 - 秋云工具',
  meta: [
    { name: 'description', content: '如有任何问题或建议，欢迎通过以下方式联系我们。' }
  ]
})

const formData = ref({
  name: '',
  email: '',
  subject: '',
  message: ''
})

const isSubmitting = ref(false)
const isSubmitted = ref(false)

const handleSubmit = async () => {
  isSubmitting.value = true
  // 模拟提交
  await new Promise(resolve => setTimeout(resolve, 1000))
  isSubmitting.value = false
  isSubmitted.value = true
  formData.value = { name: '', email: '', subject: '', message: '' }
}
</script>

<template>
  <div class="min-h-screen bg-background">
    <!-- Hero Section -->
    <div class="border-b border-border/40 bg-muted/30">
      <div class="container mx-auto px-4 py-16">
        <div class="flex items-center gap-2 text-sm text-muted-foreground mb-4">
          <NuxtLink to="/" class="hover:text-foreground">首页</NuxtLink>
          <ChevronRight class="w-4 h-4" />
          <span class="text-foreground">联系我们</span>
        </div>
        <h1 class="text-4xl font-bold text-foreground mb-4">联系我们</h1>
        <p class="text-xl text-muted-foreground max-w-2xl">
          如有任何问题、建议或合作意向，欢迎随时与我们联系
        </p>
      </div>
    </div>

    <div class="container mx-auto px-4 py-12">
      <div class="grid grid-cols-1 lg:grid-cols-2 gap-12">
        <!-- Contact Info -->
        <div>
          <h2 class="text-2xl font-bold mb-6">联系方式</h2>
          <div class="space-y-6">
            <div class="flex items-start gap-4">
              <div class="w-12 h-12 rounded-lg bg-primary/10 flex items-center justify-center shrink-0">
                <Mail class="w-6 h-6 text-primary" />
              </div>
              <div>
                <h3 class="font-semibold mb-1">电子邮箱</h3>
                <p class="text-muted-foreground">97208294@qq.com</p>
                <p class="text-sm text-muted-foreground">工作日 24 小时内回复</p>
              </div>
            </div>
            <div class="flex items-start gap-4">
              <div class="w-12 h-12 rounded-lg bg-primary/10 flex items-center justify-center shrink-0">
                <MessageSquare class="w-6 h-6 text-primary" />
              </div>
              <div>
                <h3 class="font-semibold mb-1">微信</h3>
                <p class="text-muted-foreground">qiuyun-sa</p>
                <p class="text-sm text-muted-foreground">请注明来意</p>
              </div>
            </div>
            <div class="flex items-start gap-4">
              <div class="w-12 h-12 rounded-lg bg-primary/10 flex items-center justify-center shrink-0">
                <Clock class="w-6 h-6 text-primary" />
              </div>
              <div>
                <h3 class="font-semibold mb-1">工作时间</h3>
                <p class="text-muted-foreground">周一至周五 9:00-18:00</p>
                <p class="text-sm text-muted-foreground">节假日除外</p>
              </div>
            </div>
          </div>

          <!-- Social Links -->
          <div class="mt-12">
            <h3 class="font-semibold mb-4">关注我们</h3>
            <div class="flex gap-4">
              <a
                href="https://github.com/qiuqyCN/qiuyun-tool"
                target="_blank"
                rel="noopener noreferrer"
                class="w-20 h-10 rounded-lg bg-muted flex items-center justify-center hover:bg-primary/10 transition-colors"
              >
                <span class="text-sm font-bold">GitHub</span>
              </a>
              <a
                href="mailto:97208294@qq.com"
                class="w-20 h-10 rounded-lg bg-muted flex items-center justify-center hover:bg-primary/10 transition-colors"
              >
                <span class="text-sm font-bold">邮箱</span>
              </a>
              <a
                href="https://space.bilibili.com/4295116"
                target="_blank"
                rel="noopener noreferrer"
                class="w-20 h-10 rounded-lg bg-muted flex items-center justify-center hover:bg-primary/10 transition-colors"
              >
                <span class="text-sm font-bold">Bilibili</span>
              </a>
            </div>
          </div>
        </div>

        <!-- Contact Form -->
        <div class="border border-border/40 rounded-xl p-8">
          <h2 class="text-2xl font-bold mb-6">发送消息</h2>
          
          <div v-if="isSubmitted" class="text-center py-12">
            <div class="w-16 h-16 rounded-full bg-green-100 flex items-center justify-center mx-auto mb-4">
              <Send class="w-8 h-8 text-green-600" />
            </div>
            <h3 class="text-xl font-semibold mb-2">发送成功！</h3>
            <p class="text-muted-foreground mb-4">我们会尽快回复您的消息</p>
            <Button variant="outline" @click="isSubmitted = false">发送新消息</Button>
          </div>

          <form v-else @submit.prevent="handleSubmit" class="space-y-4">
            <div class="grid grid-cols-1 sm:grid-cols-2 gap-4">
              <div>
                <label class="text-sm font-medium mb-2 block">您的姓名</label>
                <Input v-model="formData.name" placeholder="请输入姓名" required />
              </div>
              <div>
                <label class="text-sm font-medium mb-2 block">电子邮箱</label>
                <Input v-model="formData.email" type="email" placeholder="请输入邮箱" required />
              </div>
            </div>
            <div>
              <label class="text-sm font-medium mb-2 block">主题</label>
              <Input v-model="formData.subject" placeholder="请输入消息主题" required />
            </div>
            <div>
              <label class="text-sm font-medium mb-2 block">消息内容</label>
              <Textarea v-model="formData.message" rows="6" placeholder="请输入您的问题或建议..." required />
            </div>
            <Button type="su                                            bmit" class="w-full" :disabled="isSubmitting">
              <Send class="w-4 h-4 mr-2" />
              {{ isSubmitting ? '发送中...' : '发送消息' }}
            </Button>
          </form>
        </div>
      </div>
    </div>
  </div>
</template>
