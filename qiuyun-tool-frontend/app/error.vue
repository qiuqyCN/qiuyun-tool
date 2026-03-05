<script setup lang="ts">
import { Button } from '@/components/ui/button'
import { AlertCircle, Home, RefreshCcw } from 'lucide-vue-next'

const props = defineProps<{
  error: {
    statusCode: number
    statusMessage: string
    message: string
    description?: string
    data?: any
  }
}>()

const handleError = () => {
  clearError({ redirect: '/' })
}

const refreshPage = () => {
  reloadNuxtApp()
}

// 获取错误信息
const errorTitle = computed(() => {
  switch (props.error.statusCode) {
    case 404:
      return '页面未找到'
    case 500:
      return '服务器错误'
    case 403:
      return '访问被拒绝'
    case 401:
      return '未授权'
    default:
      return '出错了'
  }
})

const errorMessage = computed(() => {
  return props.error.statusMessage || props.error.message || '发生了未知错误'
})
</script>

<template>
  <div class="min-h-screen bg-background flex items-center justify-center p-4">
    <div class="max-w-md w-full text-center">
      <!-- 错误图标 -->
      <div class="w-24 h-24 bg-destructive/10 rounded-full flex items-center justify-center mx-auto mb-6">
        <AlertCircle class="w-12 h-12 text-destructive" />
      </div>

      <!-- 错误代码 -->
      <div class="text-6xl font-bold text-muted-foreground mb-2">
        {{ error.statusCode || 500 }}
      </div>

      <!-- 错误标题 -->
      <h1 class="text-2xl font-bold text-foreground mb-2">
        {{ errorTitle }}
      </h1>

      <!-- 错误描述 -->
      <p class="text-muted-foreground mb-8">
        {{ errorMessage }}
      </p>

      <!-- 详细错误信息（开发环境显示）>
      <div v-if="error.data && process.dev" class="mb-8 p-4 bg-muted rounded-lg text-left overflow-auto">
        <pre class="text-xs text-muted-foreground">{{ JSON.stringify(error.data, null, 2) }}</pre>
      </div -->

      <!-- 操作按钮 -->
      <div class="flex flex-col sm:flex-row items-center justify-center gap-4">
        <Button @click="handleError">
          <Home class="w-4 h-4 mr-2" />
          返回首页
        </Button>
        <Button variant="outline" @click="refreshPage">
          <RefreshCcw class="w-4 h-4 mr-2" />
          刷新页面
        </Button>
      </div>
    </div>
  </div>
</template>
