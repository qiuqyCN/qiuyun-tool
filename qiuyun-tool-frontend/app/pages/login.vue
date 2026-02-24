<script setup lang="ts">
import { Button } from '@/components/ui/button'
import { Input } from '@/components/ui/input'
import { Label } from '@/components/ui/label'
import { Checkbox } from '@/components/ui/checkbox'
import { 
  Wrench, 
  Github, 
  Mail,
  ArrowRight,
  Eye,
  EyeOff
} from 'lucide-vue-next'

const email = ref('')
const password = ref('')
const rememberMe = ref(false)
const showPassword = ref(false)
const isLoading = ref(false)

const handleSubmit = async () => {
  isLoading.value = true
  // 模拟登录
  await new Promise(resolve => setTimeout(resolve, 1500))
  isLoading.value = false
  // 登录成功后跳转
  navigateTo('/')
}
</script>

<template>
  <div class="min-h-screen flex items-center justify-center bg-muted/30 py-12 px-4 sm:px-6 lg:px-8">
    <div class="w-full max-w-md space-y-8">
      <!-- Logo -->
      <div class="text-center">
        <NuxtLink to="/" class="inline-flex items-center justify-center gap-2">
          <div class="w-12 h-12 rounded-xl bg-primary flex items-center justify-center">
            <Wrench class="w-7 h-7 text-primary-foreground" />
          </div>
        </NuxtLink>
        <h2 class="mt-6 text-3xl font-bold text-foreground">欢迎回来</h2>
        <p class="mt-2 text-sm text-muted-foreground">
          还没有账号？
          <NuxtLink to="/register" class="font-medium text-primary hover:underline">
            立即注册
          </NuxtLink>
        </p>
      </div>

      <!-- Social Login -->
      <div class="grid grid-cols-2 gap-3">
        <Button variant="outline" class="w-full">
          <Github class="w-4 h-4 mr-2" />
          GitHub
        </Button>
        <Button variant="outline" class="w-full">
          <svg class="w-4 h-4 mr-2" viewBox="0 0 24 24" fill="currentColor">
            <path d="M12.545,10.239v3.821h5.445c-0.712,2.315-2.647,3.972-5.445,3.972c-3.332,0-6.033-2.701-6.033-6.032s2.701-6.032,6.033-6.032c1.498,0,2.866,0.549,3.921,1.453l2.814-2.814C17.503,2.988,15.139,2,12.545,2C7.021,2,2.543,6.477,2.543,12s4.478,10,10.002,10c8.396,0,10.249-7.85,9.426-11.748L12.545,10.239z"/>
          </svg>
          Google
        </Button>
      </div>

      <div class="relative">
        <div class="absolute inset-0 flex items-center">
          <span class="w-full border-t border-border" />
        </div>
        <div class="relative flex justify-center text-xs uppercase">
          <span class="bg-muted/30 px-2 text-muted-foreground">或者使用邮箱登录</span>
        </div>
      </div>

      <!-- Form -->
      <form class="space-y-6" @submit.prevent="handleSubmit">
        <div class="space-y-2">
          <Label for="email">邮箱</Label>
          <Input 
            id="email" 
            v-model="email"
            type="email" 
            placeholder="name@example.com"
            required
          />
        </div>

        <div class="space-y-2">
          <Label for="password">密码</Label>
          <div class="relative">
            <Input 
              id="password" 
              v-model="password"
              :type="showPassword ? 'text' : 'password'"
              placeholder="请输入密码"
              required
            />
            <button
              type="button"
              @click="showPassword = !showPassword"
              class="absolute right-3 top-1/2 -translate-y-1/2 text-muted-foreground hover:text-foreground"
            >
              <Eye v-if="!showPassword" class="w-4 h-4" />
              <EyeOff v-else class="w-4 h-4" />
            </button>
          </div>
        </div>

        <div class="flex items-center justify-between">
          <div class="flex items-center space-x-2">
            <Checkbox id="remember" v-model:checked="rememberMe" />
            <Label for="remember" class="text-sm font-normal">记住我</Label>
          </div>
          <NuxtLink to="/forgot-password" class="text-sm text-primary hover:underline">
            忘记密码？
          </NuxtLink>
        </div>

        <Button type="submit" class="w-full" :disabled="isLoading">
          {{ isLoading ? '登录中...' : '登录' }}
          <ArrowRight v-if="!isLoading" class="w-4 h-4 ml-2" />
        </Button>
      </form>

      <p class="text-center text-xs text-muted-foreground">
        登录即表示您同意我们的
        <NuxtLink to="/terms" class="underline">服务条款</NuxtLink>
        和
        <NuxtLink to="/privacy" class="underline">隐私政策</NuxtLink>
      </p>
    </div>
  </div>
</template>
