<script setup lang="ts">
import { Button } from '@/components/ui/button'
import { Input } from '@/components/ui/input'
import { Label } from '@/components/ui/label'
import { Checkbox } from '@/components/ui/checkbox'
import { 
  Wrench, 
  Github, 
  ArrowRight,
  Eye,
  EyeOff,
  CheckCircle
} from 'lucide-vue-next'

const name = ref('')
const email = ref('')
const password = ref('')
const confirmPassword = ref('')
const agreeTerms = ref(false)
const showPassword = ref(false)
const showConfirmPassword = ref(false)
const isLoading = ref(false)
const isSuccess = ref(false)

const handleSubmit = async () => {
  if (password.value !== confirmPassword.value) {
    alert('两次输入的密码不一致')
    return
  }
  
  isLoading.value = true
  // 模拟注册
  await new Promise(resolve => setTimeout(resolve, 1500))
  isLoading.value = false
  isSuccess.value = true
  
  // 注册成功后跳转
  setTimeout(() => {
    navigateTo('/login')
  }, 2000)
}
</script>

<template>
  <div class="min-h-screen flex items-center justify-center bg-muted/30 py-12 px-4 sm:px-6 lg:px-8">
    <div class="w-full max-w-md space-y-8">
      <!-- Success State -->
      <div v-if="isSuccess" class="text-center py-12">
        <div class="w-20 h-20 rounded-full bg-green-100 flex items-center justify-center mx-auto mb-6">
          <CheckCircle class="w-10 h-10 text-green-600" />
        </div>
        <h2 class="text-2xl font-bold text-foreground mb-2">注册成功！</h2>
        <p class="text-muted-foreground mb-6">欢迎加入秋云工具，正在跳转到登录页面...</p>
      </div>

      <!-- Register Form -->
      <template v-else>
        <!-- Logo -->
        <div class="text-center">
          <NuxtLink to="/" class="inline-flex items-center justify-center gap-2">
            <div class="w-12 h-12 rounded-xl bg-primary flex items-center justify-center">
              <Wrench class="w-7 h-7 text-primary-foreground" />
            </div>
          </NuxtLink>
          <h2 class="mt-6 text-3xl font-bold text-foreground">创建账号</h2>
          <p class="mt-2 text-sm text-muted-foreground">
            已有账号？
            <NuxtLink to="/login" class="font-medium text-primary hover:underline">
              立即登录
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
            <span class="bg-muted/30 px-2 text-muted-foreground">或者使用邮箱注册</span>
          </div>
        </div>

        <!-- Form -->
        <form class="space-y-4" @submit.prevent="handleSubmit">
          <div class="space-y-2">
            <Label for="name">用户名</Label>
            <Input 
              id="name" 
              v-model="name"
              type="text" 
              placeholder="请输入用户名"
              required
            />
          </div>

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
                placeholder="请输入密码（至少6位）"
                minlength="6"
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

          <div class="space-y-2">
            <Label for="confirmPassword">确认密码</Label>
            <div class="relative">
              <Input 
                id="confirmPassword" 
                v-model="confirmPassword"
                :type="showConfirmPassword ? 'text' : 'password'"
                placeholder="请再次输入密码"
                required
              />
              <button
                type="button"
                @click="showConfirmPassword = !showConfirmPassword"
                class="absolute right-3 top-1/2 -translate-y-1/2 text-muted-foreground hover:text-foreground"
              >
                <Eye v-if="!showConfirmPassword" class="w-4 h-4" />
                <EyeOff v-else class="w-4 h-4" />
              </button>
            </div>
          </div>

          <div class="flex items-start space-x-2">
            <Checkbox id="terms" v-model:checked="agreeTerms" class="mt-1" />
            <Label for="terms" class="text-sm font-normal leading-relaxed">
              我已阅读并同意
              <NuxtLink to="/terms" class="text-primary hover:underline">服务条款</NuxtLink>
              和
              <NuxtLink to="/privacy" class="text-primary hover:underline">隐私政策</NuxtLink>
            </Label>
          </div>

          <Button 
            type="submit" 
            class="w-full" 
            :disabled="isLoading || !agreeTerms"
          >
            {{ isLoading ? '注册中...' : '创建账号' }}
            <ArrowRight v-if="!isLoading" class="w-4 h-4 ml-2" />
          </Button>
        </form>
      </template>
    </div>
  </div>
</template>
