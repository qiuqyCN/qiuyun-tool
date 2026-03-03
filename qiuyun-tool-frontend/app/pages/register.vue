<script setup lang="ts">
import { Button } from '@/components/ui/button'
import { Input } from '@/components/ui/input'
import { Label } from '@/components/ui/label'
import { useUserStore } from '@/stores/userStore'
import { Loader2, CheckCircle } from 'lucide-vue-next'

const userStore = useUserStore()
const router = useRouter()

const form = reactive({
  username: '',
  email: '',
  password: '',
  confirmPassword: ''
})

const errors = reactive({
  username: '',
  email: '',
  password: '',
  confirmPassword: ''
})

const isLoading = ref(false)
const isSuccess = ref(false)
const errorMessage = ref('')

const validateForm = () => {
  let isValid = true
  errors.username = ''
  errors.email = ''
  errors.password = ''
  errors.confirmPassword = ''

  if (!form.username.trim()) {
    errors.username = '请输入用户名'
    isValid = false
  } else if (form.username.length < 3) {
    errors.username = '用户名长度不能少于3位'
    isValid = false
  }

  if (!form.email.trim()) {
    errors.email = '请输入邮箱'
    isValid = false
  } else if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(form.email)) {
    errors.email = '请输入有效的邮箱地址'
    isValid = false
  }

  if (!form.password) {
    errors.password = '请输入密码'
    isValid = false
  } else if (form.password.length < 6) {
    errors.password = '密码长度不能少于6位'
    isValid = false
  }

  if (!form.confirmPassword) {
    errors.confirmPassword = '请确认密码'
    isValid = false
  } else if (form.password !== form.confirmPassword) {
    errors.confirmPassword = '两次输入的密码不一致'
    isValid = false
  }

  return isValid
}

const handleRegister = async () => {
  if (!validateForm()) return

  isLoading.value = true
  errorMessage.value = ''

  try {
    const result = await userStore.register(
      form.username,
      form.email,
      form.password,
      form.confirmPassword
    )

    if (result.success) {
      isSuccess.value = true
      // 3秒后跳转到登录页
      setTimeout(() => {
        router.push('/login')
      }, 3000)
    } else {
      errorMessage.value = result.message || '注册失败'
    }
  } catch (error: any) {
    errorMessage.value = error.message || '注册失败，请稍后重试'
  } finally {
    isLoading.value = false
  }
}

// 如果已登录，跳转到首页
onMounted(() => {
  if (userStore.isLoggedIn) {
    router.push('/')
  }
})
</script>

<template>
  <div class="min-h-screen bg-background flex items-center justify-center px-4">
    <div class="w-full max-w-md space-y-8">
      <!-- Header -->
      <div class="text-center">
        <h1 class="text-3xl font-bold text-foreground">创建账户</h1>
        <p class="mt-2 text-muted-foreground">
          注册一个新账户开始使用
        </p>
      </div>

      <!-- Register Form -->
      <div class="bg-card border border-border rounded-xl p-8 shadow-sm">
        <!-- Success State -->
        <div v-if="isSuccess" class="text-center py-8">
          <div class="w-16 h-16 bg-green-100 rounded-full flex items-center justify-center mx-auto mb-4">
            <CheckCircle class="w-8 h-8 text-green-600" />
          </div>
          <h2 class="text-xl font-semibold text-foreground mb-2">注册成功！</h2>
          <p class="text-muted-foreground mb-4">您的账户已创建成功</p>
          <p class="text-sm text-muted-foreground">
            {{ 3 }} 秒后自动跳转到登录页...
          </p>
          <Button class="mt-4" @click="router.push('/login')">
            立即登录
          </Button>
        </div>

        <form v-else @submit.prevent="handleRegister" class="space-y-6">
          <!-- Error Message -->
          <div v-if="errorMessage" class="p-3 text-sm text-destructive bg-destructive/10 rounded-lg">
            {{ errorMessage }}
          </div>

          <!-- Username -->
          <div class="space-y-2">
            <Label for="username">用户名</Label>
            <Input
              id="username"
              v-model="form.username"
              type="text"
              placeholder="请输入用户名"
              :class="{ 'border-destructive': errors.username }"
            />
            <p v-if="errors.username" class="text-sm text-destructive">{{ errors.username }}</p>
          </div>

          <!-- Email -->
          <div class="space-y-2">
            <Label for="email">邮箱</Label>
            <Input
              id="email"
              v-model="form.email"
              type="email"
              placeholder="请输入邮箱"
              :class="{ 'border-destructive': errors.email }"
            />
            <p v-if="errors.email" class="text-sm text-destructive">{{ errors.email }}</p>
          </div>

          <!-- Password -->
          <div class="space-y-2">
            <Label for="password">密码</Label>
            <Input
              id="password"
              v-model="form.password"
              type="password"
              placeholder="请输入密码（至少6位）"
              :class="{ 'border-destructive': errors.password }"
            />
            <p v-if="errors.password" class="text-sm text-destructive">{{ errors.password }}</p>
          </div>

          <!-- Confirm Password -->
          <div class="space-y-2">
            <Label for="confirmPassword">确认密码</Label>
            <Input
              id="confirmPassword"
              v-model="form.confirmPassword"
              type="password"
              placeholder="请再次输入密码"
              :class="{ 'border-destructive': errors.confirmPassword }"
            />
            <p v-if="errors.confirmPassword" class="text-sm text-destructive">{{ errors.confirmPassword }}</p>
          </div>

          <!-- Submit Button -->
          <Button type="submit" class="w-full" :disabled="isLoading">
            <Loader2 v-if="isLoading" class="mr-2 h-4 w-4 animate-spin" />
            {{ isLoading ? '注册中...' : '注册' }}
          </Button>
        </form>

        <!-- Divider -->
        <div v-if="!isSuccess" class="relative my-6">
          <div class="absolute inset-0 flex items-center">
            <span class="w-full border-t border-border" />
          </div>
          <div class="relative flex justify-center text-xs uppercase">
            <span class="bg-card px-2 text-muted-foreground">或者</span>
          </div>
        </div>

        <!-- Login Link -->
        <div v-if="!isSuccess" class="text-center text-sm">
          <span class="text-muted-foreground">已有账户？</span>
          <NuxtLink to="/login" class="text-primary hover:underline font-medium">
            立即登录
          </NuxtLink>
        </div>
      </div>

      <!-- Back to Home -->
      <div class="text-center">
        <NuxtLink to="/" class="text-sm text-muted-foreground hover:text-foreground">
          ← 返回首页
        </NuxtLink>
      </div>
    </div>
  </div>
</template>
