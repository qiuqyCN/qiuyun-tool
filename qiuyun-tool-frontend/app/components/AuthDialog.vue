<script setup lang="ts">
import {
  Dialog,
  DialogContent,
} from '@/components/ui/dialog'
import { Button } from '@/components/ui/button'
import { Input } from '@/components/ui/input'
import { Checkbox } from '@/components/ui/checkbox'
import { Loader2, Eye, EyeOff, User, Mail, Lock, ArrowRight } from 'lucide-vue-next'
import { useUserStore } from '@/stores/userStore'

const props = defineProps<{
  open: boolean
  defaultTab?: 'login' | 'register'
}>()

const emit = defineEmits<{
  'update:open': [value: boolean]
}>()

const userStore = useUserStore()
const activeTab = ref<'login' | 'register'>(props.defaultTab || 'login')

// 监听 props 变化，更新标签页
watch(() => props.defaultTab, (newTab) => {
  if (newTab) {
    activeTab.value = newTab
  }
})

// 登录表单
const loginForm = reactive({
  username: '',
  password: '',
  rememberMe: false
})

// 注册表单
const registerForm = reactive({
  username: '',
  email: '',
  password: '',
  confirmPassword: '',
  agreeTerms: false
})

// 状态
const isLoading = ref(false)
const errorMessage = ref('')
const showPassword = ref(false)
const showConfirmPassword = ref(false)

// 验证错误
const errors = reactive<Record<string, string>>({})

// 切换标签页
const switchTab = (tab: 'login' | 'register') => {
  activeTab.value = tab
  errorMessage.value = ''
  clearErrors()
}

// 清除错误
const clearErrors = () => {
  Object.keys(errors).forEach(key => delete errors[key])
}

// 验证登录表单
const validateLoginForm = () => {
  clearErrors()
  let isValid = true

  if (!loginForm.username.trim()) {
    errors.username = '请输入用户名'
    isValid = false
  }

  if (!loginForm.password) {
    errors.password = '请输入密码'
    isValid = false
  }

  return isValid
}

// 验证注册表单
const validateRegisterForm = () => {
  clearErrors()
  let isValid = true

  if (!registerForm.username.trim()) {
    errors.username = '请输入用户名'
    isValid = false
  } else if (registerForm.username.length < 3) {
    errors.username = '用户名至少3个字符'
    isValid = false
  }

  if (!registerForm.email.trim()) {
    errors.email = '请输入邮箱'
    isValid = false
  } else if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(registerForm.email)) {
    errors.email = '请输入有效的邮箱地址'
    isValid = false
  }

  if (!registerForm.password) {
    errors.password = '请输入密码'
    isValid = false
  } else if (registerForm.password.length < 6) {
    errors.password = '密码至少6个字符'
    isValid = false
  }

  if (!registerForm.confirmPassword) {
    errors.confirmPassword = '请确认密码'
    isValid = false
  } else if (registerForm.password !== registerForm.confirmPassword) {
    errors.confirmPassword = '两次输入的密码不一致'
    isValid = false
  }

  if (!registerForm.agreeTerms) {
    errors.agreeTerms = '请阅读并同意用户协议和隐私政策'
    isValid = false
  }

  return isValid
}

// 处理登录
const handleLogin = async () => {
  if (!validateLoginForm()) return

  isLoading.value = true
  errorMessage.value = ''

  try {
    const result = await userStore.login(
      loginForm.username,
      loginForm.password,
      loginForm.rememberMe
    )

    if (result.success) {
      emit('update:open', false)
      // 重置表单
      loginForm.username = ''
      loginForm.password = ''
      loginForm.rememberMe = false
    } else {
      errorMessage.value = result.message || '登录失败'
    }
  } catch (error: any) {
    errorMessage.value = error.message || '登录失败，请重试'
  } finally {
    isLoading.value = false
  }
}

// 处理注册
const handleRegister = async () => {
  if (!validateRegisterForm()) return

  isLoading.value = true
  errorMessage.value = ''

  try {
    const result = await userStore.register(
      registerForm.username,
      registerForm.email,
      registerForm.password,
      registerForm.confirmPassword
    )

    if (result.success) {
      // 注册成功，切换到登录页
      switchTab('login')
      errorMessage.value = '注册成功，请登录'
      // 重置表单
      registerForm.username = ''
      registerForm.email = ''
      registerForm.password = ''
      registerForm.confirmPassword = ''
      registerForm.agreeTerms = false
    } else {
      errorMessage.value = result.message || '注册失败'
    }
  } catch (error: any) {
    errorMessage.value = error.message || '注册失败，请重试'
  } finally {
    isLoading.value = false
  }
}

// 关闭弹窗
const handleClose = () => {
  emit('update:open', false)
  errorMessage.value = ''
  clearErrors()
}
</script>

<template>
  <Dialog :open="open" @update:open="handleClose">
    <DialogContent class="sm:max-w-[440px] p-0 gap-0 overflow-hidden border-0 shadow-2xl">
      <!-- 左侧装饰背景 -->
      <div class="absolute left-0 top-0 bottom-0 w-1.5 bg-linear-to-b from-primary via-primary/80 to-primary/60"></div>

      <div class="flex flex-col">
        <!-- 头部区域 -->
        <div class="px-8 pt-8 pb-4">
          <div class="flex items-center gap-3 mb-2">
            <div class="w-10 h-10 rounded-xl bg-primary/10 flex items-center justify-center">
              <img src="/logo.svg" alt="秋云工具" class="w-6 h-6" />
            </div>
            <div>
              <h1 class="text-xl font-bold text-foreground">秋云工具</h1>
              <p class="text-xs text-muted-foreground">高效便捷的在线工具平台</p>
            </div>
          </div>
        </div>

        <!-- 标签切换 - 胶囊样式 -->
        <div class="px-8 mb-6">
          <div class="flex p-1 bg-muted rounded-xl">
            <button
              @click="switchTab('login')"
              class="flex-1 py-2.5 text-sm font-medium rounded-lg transition-all duration-200"
              :class="activeTab === 'login' 
                ? 'bg-background text-foreground shadow-sm' 
                : 'text-muted-foreground hover:text-foreground'"
            >
              登录
            </button>
            <button
              @click="switchTab('register')"
              class="flex-1 py-2.5 text-sm font-medium rounded-lg transition-all duration-200"
              :class="activeTab === 'register' 
                ? 'bg-background text-foreground shadow-sm' 
                : 'text-muted-foreground hover:text-foreground'"
            >
              注册
            </button>
          </div>
        </div>

        <!-- 登录表单 -->
        <div v-if="activeTab === 'login'" class="px-8 pb-8">
          <form @submit.prevent="handleLogin" class="space-y-4">
            <!-- 错误提示 -->
            <div
              v-if="errorMessage"
              class="p-3 text-sm text-destructive bg-destructive/10 rounded-xl border border-destructive/20 flex items-center gap-2"
            >
              <span class="w-1.5 h-1.5 rounded-full bg-destructive"></span>
              {{ errorMessage }}
            </div>

            <!-- 用户名 -->
            <div class="space-y-1.5">
              <label class="text-sm font-medium text-foreground">用户名</label>
              <div class="relative">
                <User class="absolute left-3.5 top-1/2 -translate-y-1/2 h-4 w-4 text-muted-foreground" />
                <Input
                  v-model="loginForm.username"
                  type="text"
                  placeholder="请输入用户名"
                  class="pl-10 h-11 rounded-xl border-input bg-background"
                  :class="{ 'border-destructive focus-visible:ring-destructive': errors.username }"
                />
              </div>
              <p v-if="errors.username" class="text-xs text-destructive flex items-center gap-1">
                <span class="w-1 h-1 rounded-full bg-destructive"></span>
                {{ errors.username }}
              </p>
            </div>

            <!-- 密码 -->
            <div class="space-y-1.5">
              <label class="text-sm font-medium text-foreground">密码</label>
              <div class="relative">
                <Lock class="absolute left-3.5 top-1/2 -translate-y-1/2 h-4 w-4 text-muted-foreground" />
                <Input
                  v-model="loginForm.password"
                  :type="showPassword ? 'text' : 'password'"
                  placeholder="请输入密码"
                  class="pl-10 pr-10 h-11 rounded-xl border-input bg-background"
                  :class="{ 'border-destructive focus-visible:ring-destructive': errors.password }"
                />
                <button
                  type="button"
                  @click="showPassword = !showPassword"
                  class="absolute right-3.5 top-1/2 -translate-y-1/2 text-muted-foreground hover:text-foreground transition-colors"
                >
                  <Eye v-if="showPassword" class="h-4 w-4" />
                  <EyeOff v-else class="h-4 w-4" />
                </button>
              </div>
              <p v-if="errors.password" class="text-xs text-destructive flex items-center gap-1">
                <span class="w-1 h-1 rounded-full bg-destructive"></span>
                {{ errors.password }}
              </p>
            </div>

            <!-- 记住我 & 忘记密码 -->
            <div class="flex items-center justify-between py-1">
              <div class="flex items-center space-x-2">
                <Checkbox 
                  id="rememberMe" 
                  v-model:checked="loginForm.rememberMe"
                  class="rounded-md border-2 data-[state=checked]:bg-primary data-[state=checked]:border-primary"
                />
                <label for="rememberMe" class="text-sm text-muted-foreground cursor-pointer select-none">
                  记住我
                </label>
              </div>
              <NuxtLink to="/forgot-password" class="text-sm text-primary hover:text-primary/80 font-medium transition-colors">
                忘记密码？
              </NuxtLink>
            </div>

            <!-- 登录按钮 -->
            <Button 
              type="submit" 
              class="w-full h-11 rounded-xl font-medium text-base shadow-lg shadow-primary/25 hover:shadow-xl hover:shadow-primary/30 transition-all"
              :disabled="isLoading"
            >
              <Loader2 v-if="isLoading" class="mr-2 h-4 w-4 animate-spin" />
              <span v-else class="flex items-center gap-2">
                登录
                <ArrowRight class="h-4 w-4" />
              </span>
            </Button>
          </form>

          <!-- 注册提示 -->
          <div class="mt-6 text-center">
            <span class="text-sm text-muted-foreground">还没有账户？</span>
            <button
              @click="switchTab('register')"
              class="text-sm text-primary hover:text-primary/80 font-medium ml-1 transition-colors"
            >
              立即注册
            </button>
          </div>
        </div>

        <!-- 注册表单 -->
        <div v-else class="px-8 pb-8">
          <form @submit.prevent="handleRegister" class="space-y-4">
            <!-- 错误提示 -->
            <div
              v-if="errorMessage"
              class="p-3 text-sm text-destructive bg-destructive/10 rounded-xl border border-destructive/20 flex items-center gap-2"
            >
              <span class="w-1.5 h-1.5 rounded-full bg-destructive"></span>
              {{ errorMessage }}
            </div>

            <!-- 用户名 -->
            <div class="space-y-1.5">
              <label class="text-sm font-medium text-foreground">用户名</label>
              <div class="relative">
                <User class="absolute left-3.5 top-1/2 -translate-y-1/2 h-4 w-4 text-muted-foreground" />
                <Input
                  v-model="registerForm.username"
                  type="text"
                  placeholder="请输入用户名（至少3个字符）"
                  class="pl-10 h-11 rounded-xl border-input bg-background"
                  :class="{ 'border-destructive focus-visible:ring-destructive': errors.username }"
                />
              </div>
              <p v-if="errors.username" class="text-xs text-destructive flex items-center gap-1">
                <span class="w-1 h-1 rounded-full bg-destructive"></span>
                {{ errors.username }}
              </p>
            </div>

            <!-- 邮箱 -->
            <div class="space-y-1.5">
              <label class="text-sm font-medium text-foreground">电子邮箱</label>
              <div class="relative">
                <Mail class="absolute left-3.5 top-1/2 -translate-y-1/2 h-4 w-4 text-muted-foreground" />
                <Input
                  v-model="registerForm.email"
                  type="email"
                  placeholder="请输入邮箱地址"
                  class="pl-10 h-11 rounded-xl border-input bg-background"
                  :class="{ 'border-destructive focus-visible:ring-destructive': errors.email }"
                />
              </div>
              <p v-if="errors.email" class="text-xs text-destructive flex items-center gap-1">
                <span class="w-1 h-1 rounded-full bg-destructive"></span>
                {{ errors.email }}
              </p>
            </div>

            <!-- 密码 -->
            <div class="space-y-1.5">
              <label class="text-sm font-medium text-foreground">密码</label>
              <div class="relative">
                <Lock class="absolute left-3.5 top-1/2 -translate-y-1/2 h-4 w-4 text-muted-foreground" />
                <Input
                  v-model="registerForm.password"
                  :type="showPassword ? 'text' : 'password'"
                  placeholder="至少6个字符"
                  class="pl-10 pr-10 h-11 rounded-xl border-input bg-background"
                  :class="{ 'border-destructive focus-visible:ring-destructive': errors.password }"
                />
                <button
                  type="button"
                  @click="showPassword = !showPassword"
                  class="absolute right-3.5 top-1/2 -translate-y-1/2 text-muted-foreground hover:text-foreground transition-colors"
                >
                  <Eye v-if="showPassword" class="h-4 w-4" />
                  <EyeOff v-else class="h-4 w-4" />
                </button>
              </div>
              <p v-if="errors.password" class="text-xs text-destructive flex items-center gap-1">
                <span class="w-1 h-1 rounded-full bg-destructive"></span>
                {{ errors.password }}
              </p>
            </div>

            <!-- 确认密码 -->
            <div class="space-y-1.5">
              <label class="text-sm font-medium text-foreground">确认密码</label>
              <div class="relative">
                <Lock class="absolute left-3.5 top-1/2 -translate-y-1/2 h-4 w-4 text-muted-foreground" />
                <Input
                  v-model="registerForm.confirmPassword"
                  :type="showConfirmPassword ? 'text' : 'password'"
                  placeholder="请再次输入密码"
                  class="pl-10 pr-10 h-11 rounded-xl border-input bg-background"
                  :class="{ 'border-destructive focus-visible:ring-destructive': errors.confirmPassword }"
                />
                <button
                  type="button"
                  @click="showConfirmPassword = !showConfirmPassword"
                  class="absolute right-3.5 top-1/2 -translate-y-1/2 text-muted-foreground hover:text-foreground transition-colors"
                >
                  <Eye v-if="showConfirmPassword" class="h-4 w-4" />
                  <EyeOff v-else class="h-4 w-4" />
                </button>
              </div>
              <p v-if="errors.confirmPassword" class="text-xs text-destructive flex items-center gap-1">
                <span class="w-1 h-1 rounded-full bg-destructive"></span>
                {{ errors.confirmPassword }}
              </p>
            </div>

            <!-- 用户协议 -->
            <div class="space-y-1.5">
              <div class="flex items-start space-x-2">
                <Checkbox 
                  id="agreeTerms" 
                  v-model:checked="registerForm.agreeTerms"
                  class="mt-0.5 rounded-md border-2 data-[state=checked]:bg-primary data-[state=checked]:border-primary"
                />
                <label for="agreeTerms" class="text-xs text-muted-foreground cursor-pointer leading-relaxed">
                  我已阅读并同意
                  <NuxtLink to="/terms" class="text-primary hover:underline font-medium" @click="handleClose">用户协议</NuxtLink>
                  和
                  <NuxtLink to="/privacy" class="text-primary hover:underline font-medium" @click="handleClose">隐私政策</NuxtLink>
                </label>
              </div>
              <p v-if="errors.agreeTerms" class="text-xs text-destructive flex items-center gap-1">
                <span class="w-1 h-1 rounded-full bg-destructive"></span>
                {{ errors.agreeTerms }}
              </p>
            </div>

            <!-- 注册按钮 -->
            <Button 
              type="submit" 
              class="w-full h-11 rounded-xl font-medium text-base shadow-lg shadow-primary/25 hover:shadow-xl hover:shadow-primary/30 transition-all"
              :disabled="isLoading"
            >
              <Loader2 v-if="isLoading" class="mr-2 h-4 w-4 animate-spin" />
              <span v-else class="flex items-center gap-2">
                注册
                <ArrowRight class="h-4 w-4" />
              </span>
            </Button>
          </form>

          <!-- 登录提示 -->
          <div class="mt-6 text-center">
            <span class="text-sm text-muted-foreground">已有账户？</span>
            <button
              @click="switchTab('login')"
              class="text-sm text-primary hover:text-primary/80 font-medium ml-1 transition-colors"
            >
              立即登录
            </button>
          </div>
        </div>
      </div>
    </DialogContent>
  </Dialog>
</template>
