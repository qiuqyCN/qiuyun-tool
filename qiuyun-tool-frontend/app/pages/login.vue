<script setup lang="ts">
import { Button } from '@/components/ui/button'
import { Input } from '@/components/ui/input'
import { Label } from '@/components/ui/label'
import { Checkbox } from '@/components/ui/checkbox'
import { useUserStore } from '@/stores/userStore'
import { Loader2 } from 'lucide-vue-next'

const userStore = useUserStore()
const router = useRouter()
const route = useRoute()

const form = reactive({
  username: '',
  password: '',
  rememberMe: false
})

const errors = reactive({
  username: '',
  password: ''
})

const isLoading = ref(false)
const errorMessage = ref('')

const validateForm = () => {
  let isValid = true
  errors.username = ''
  errors.password = ''

  if (!form.username.trim()) {
    errors.username = '请输入用户名'
    isValid = false
  }

  if (!form.password) {
    errors.password = '请输入密码'
    isValid = false
  } else if (form.password.length < 6) {
    errors.password = '密码长度不能少于6位'
    isValid = false
  }

  return isValid
}

const handleLogin = async () => {
  if (!validateForm()) return

  isLoading.value = true
  errorMessage.value = ''

  try {
    const result = await userStore.login(form.username, form.password, form.rememberMe)

    if (result.success) {
      // 登录成功，跳转到首页或之前的页面
      const redirect = route.query.redirect as string || '/'
      router.push(redirect)
    } else {
      errorMessage.value = result.message || '登录失败'
    }
  } catch (error: any) {
    errorMessage.value = error.message || '登录失败，请稍后重试'
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
        <h1 class="text-3xl font-bold text-foreground">欢迎回来</h1>
        <p class="mt-2 text-muted-foreground">
          登录您的账户以继续使用
        </p>
      </div>

      <!-- Login Form -->
      <div class="bg-card border border-border rounded-xl p-8 shadow-sm">
        <form @submit.prevent="handleLogin" class="space-y-6">
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

          <!-- Password -->
          <div class="space-y-2">
            <Label for="password">密码</Label>
            <Input
              id="password"
              v-model="form.password"
              type="password"
              placeholder="请输入密码"
              :class="{ 'border-destructive': errors.password }"
            />
            <p v-if="errors.password" class="text-sm text-destructive">{{ errors.password }}</p>
          </div>

          <!-- Remember Me -->
          <div class="flex items-center justify-between">
            <div class="flex items-center space-x-2">
              <Checkbox id="rememberMe" v-model:checked="form.rememberMe" />
              <Label for="rememberMe" class="text-sm font-normal cursor-pointer">
                记住我
              </Label>
            </div>
            <NuxtLink to="/forgot-password" class="text-sm text-primary hover:underline">
              忘记密码？
            </NuxtLink>
          </div>

          <!-- Submit Button -->
          <Button type="submit" class="w-full" :disabled="isLoading">
            <Loader2 v-if="isLoading" class="mr-2 h-4 w-4 animate-spin" />
            {{ isLoading ? '登录中...' : '登录' }}
          </Button>
        </form>

        <!-- Divider -->
        <div class="relative my-6">
          <div class="absolute inset-0 flex items-center">
            <span class="w-full border-t border-border" />
          </div>
          <div class="relative flex justify-center text-xs uppercase">
            <span class="bg-card px-2 text-muted-foreground">或者</span>
          </div>
        </div>

        <!-- Register Link -->
        <div class="text-center text-sm">
          <span class="text-muted-foreground">还没有账户？</span>
          <NuxtLink to="/register" class="text-primary hover:underline font-medium">
            立即注册
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
