<script setup lang="ts">
import { Button } from '@/components/ui/button'
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuSeparator,
  DropdownMenuTrigger,
} from '@/components/ui/dropdown-menu'
import { 
  User, 
  LogOut, 
  Heart, 
  Crown, 
  Settings,
  Menu,
  X,
  Moon,
  Sun,
  Wrench,
  Home,
  Grid3X3,
  CreditCard,
  Trophy,
  Github,
  FileText,
  Star,
  History,
  Bell
} from 'lucide-vue-next'
import { useUserStore } from '@/stores/userStore'

const colorMode = useColorMode()
const isMobileMenuOpen = ref(false)
const userStore = useUserStore()

// 弹窗状态
const authDialogOpen = ref(false)
const authDialogTab = ref<'login' | 'register'>('login')

// 打开登录弹窗
const openLoginDialog = () => {
  authDialogTab.value = 'login'
  authDialogOpen.value = true
  isMobileMenuOpen.value = false
}

// 打开注册弹窗
const openRegisterDialog = () => {
  authDialogTab.value = 'register'
  authDialogOpen.value = true
  isMobileMenuOpen.value = false
}

// 滚动状态
const isScrolled = ref(false)

// 监听滚动 - 使用较大阈值避免抖动
onMounted(() => {
  const handleScroll = () => {
    isScrolled.value = window.scrollY > 56
  }
  
  window.addEventListener('scroll', handleScroll)
  handleScroll() // 初始化检查
  
  onUnmounted(() => {
    window.removeEventListener('scroll', handleScroll)
  })
})

const navItems = [
  { name: '首页', path: '/', icon: Home },
  { name: '分类', path: '/category', icon: Grid3X3 },
  { name: '定价', path: '/pricing', icon: CreditCard },
  { name: '排行榜', path: '/ranking', icon: Trophy },
]

const toggleTheme = () => {
  colorMode.preference = colorMode.value === 'dark' ? 'light' : 'dark'
}

const logout = async () => {
  await userStore.logout()
  navigateTo('/')
}

// 获取用户头像显示文字
const getAvatarText = (name: string) => {
  return name ? name.charAt(0).toUpperCase() : 'U'
}
</script>

<template>
  <header 
    class="sticky top-0 z-50 w-full bg-background/95 backdrop-blur supports-backdrop-filter:bg-background/60 transition-all duration-300 ease-in-out"
    :class="isScrolled ? 'border-b border-border/40' : 'border-b border-transparent'"
  >
    <div 
      class="container mx-auto px-4 flex items-center justify-between transition-all duration-300 ease-in-out"
      :class="isScrolled ? 'h-14' : 'h-16'"
    >
      <!-- Logo -->
      <NuxtLink to="/" class="flex items-center gap-2 mr-6">
        <img src="/logo.svg" alt="秋云工具" class="bg-sky-400 text-white px-1 py-1 rounded-md transition-all duration-300" :class="isScrolled ? 'w-8 h-8' : 'w-9 h-9'" />
        <span class="font-bold text-foreground transition-all duration-300" :class="isScrolled ? 'text-lg' : 'text-xl'">秋云工具</span>
      </NuxtLink>

      <!-- Desktop Navigation -->
      <nav class="hidden md:flex items-center gap-6 flex-1">
        <NuxtLink 
          v-for="item in navItems" 
          :key="item.path"
          :to="item.path"
          class="flex items-center gap-2 text-base font-medium text-muted-foreground transition-colors hover:text-foreground"
          :class="{ 'text-foreground': $route.path === item.path }"
        >
          <component :is="item.icon" class="w-4 h-4" />
          {{ item.name }}
        </NuxtLink>
      </nav>

      <!-- Right Side -->
      <div class="flex items-center gap-4">
        <!-- Theme Toggle -->
        <Button
          variant="ghost"
          size="icon"
          class="h-9 w-9"
          @click="toggleTheme"
        >
          <Sun v-if="colorMode.value === 'dark'" class="h-6 w-6" />
          <Moon v-else class="h-6 w-6" />
          <span class="sr-only">切换主题</span>
        </Button>

        <!-- GitHub Link -->
        <Button
          variant="ghost"
          size="icon"
          class="h-9 w-9"
          as-child
        >
          <a href="https://github.com/qiuqyCN/qiuyun-tool" target="_blank" rel="noopener noreferrer">
            <Github class="h-6 w-6" />
            <span class="sr-only">GitHub</span>
          </a>
        </Button>

        <!-- Desktop User Menu -->
        <div class="hidden md:flex items-center gap-6">
          <template v-if="userStore.isLoggedIn">
            <!-- 通知图标 -->
            <Button variant="ghost" size="icon" class="h-9 w-9 relative">
              <Bell class="h-5 w-5" />
              <span class="sr-only">通知</span>
              <span class="absolute top-1 right-1 w-2 h-2 bg-red-500 rounded-full"></span>
            </Button>

            <DropdownMenu>
              <DropdownMenuTrigger as-child>
                <Button variant="ghost" class="relative h-9 w-9 rounded-full p-0">
                  <img 
                    v-if="userStore.currentUser?.avatar" 
                    :src="userStore.currentUser.avatar" 
                    :alt="userStore.currentUser.nickname"
                    class="h-9 w-9 rounded-full object-cover"
                  />
                  <div 
                    v-else 
                    class="flex h-9 w-9 items-center justify-center rounded-full bg-primary text-primary-foreground text-sm font-medium"
                  >
                    {{ getAvatarText(userStore.currentUser?.nickname || userStore.currentUser?.username || '') }}
                  </div>
                </Button>
              </DropdownMenuTrigger>
              <DropdownMenuContent class="w-64" align="end">
                <!-- 用户信息头部 -->
                <div class="flex items-center gap-3 p-3 border-b border-border/40">
                  <img 
                    v-if="userStore.currentUser?.avatar" 
                    :src="userStore.currentUser.avatar" 
                    :alt="userStore.currentUser.nickname"
                    class="h-10 w-10 rounded-full object-cover"
                  />
                  <div 
                    v-else 
                    class="flex h-10 w-10 items-center justify-center rounded-full bg-primary text-primary-foreground text-sm font-medium"
                  >
                    {{ getAvatarText(userStore.currentUser?.nickname || userStore.currentUser?.username || '') }}
                  </div>
                  <div class="flex flex-col min-w-0">
                    <span class="text-sm font-medium truncate">{{ userStore.currentUser?.nickname || userStore.currentUser?.username }}</span>
                    <span class="text-xs text-muted-foreground">{{ userStore.currentUser?.email }}</span>
                  </div>
                </div>

                <!-- 菜单项 -->
                <DropdownMenuItem as-child>
                  <NuxtLink to="/user" class="cursor-pointer">
                    <User class="mr-2 h-4 w-4" />
                    个人中心
                  </NuxtLink>
                </DropdownMenuItem>
                <DropdownMenuItem as-child>
                  <NuxtLink to="/user/favorites" class="cursor-pointer">
                    <Heart class="mr-2 h-4 w-4" />
                    我的收藏
                  </NuxtLink>
                </DropdownMenuItem>
                <DropdownMenuItem as-child>
                  <NuxtLink to="/user/history" class="cursor-pointer">
                    <History class="mr-2 h-4 w-4" />
                    最近使用
                  </NuxtLink>
                </DropdownMenuItem>
                <DropdownMenuItem as-child>
                  <NuxtLink to="/user/reviews" class="cursor-pointer">
                    <Star class="mr-2 h-4 w-4" />
                    我的评价
                  </NuxtLink>
                </DropdownMenuItem>
                <DropdownMenuSeparator />
                <DropdownMenuItem as-child>
                  <NuxtLink to="/pricing" class="cursor-pointer">
                    <Crown class="mr-2 h-4 w-4" />
                    <span v-if="userStore.isVip" class="text-yellow-500">VIP会员</span>
                    <span v-else>开通会员</span>
                  </NuxtLink>
                </DropdownMenuItem>
                <DropdownMenuSeparator />
                <DropdownMenuItem as-child>
                  <NuxtLink to="/user/settings" class="cursor-pointer">
                    <Settings class="mr-2 h-4 w-4" />
                    账号设置
                  </NuxtLink>
                </DropdownMenuItem>
                <DropdownMenuItem @click="logout" class="cursor-pointer text-destructive focus:text-destructive">
                  <LogOut class="mr-2 h-4 w-4" />
                  退出登录
                </DropdownMenuItem>
              </DropdownMenuContent>
            </DropdownMenu>
          </template>
          <template v-else>
            <Button variant="ghost" class="cursor-pointer" @click="openLoginDialog">
              <span class="text-sm font-medium">登录</span>
            </Button>
            <Button class="cursor-pointer" @click="openRegisterDialog">
              <span class="text-sm font-medium">注册</span>
            </Button>
          </template>
        </div>

        <!-- Mobile Menu Button -->
        <Button 
          variant="ghost" 
          size="icon" 
          class="md:hidden h-9 w-9"
          @click="isMobileMenuOpen = !isMobileMenuOpen"
        >
          <Menu v-if="!isMobileMenuOpen" class="h-5 w-5" />
          <X v-else class="h-5 w-5" />
        </Button>
      </div>
    </div>

    <!-- Mobile Menu -->
    <div 
      v-if="isMobileMenuOpen" 
      class="md:hidden border-t border-border/40 bg-background"
    >
      <nav class="container mx-auto px-4 py-4 flex flex-col gap-2">
        <NuxtLink 
          v-for="item in navItems" 
          :key="item.path"
          :to="item.path"
          class="flex items-center gap-2 px-3 py-2 text-sm font-medium text-muted-foreground rounded-md hover:bg-accent hover:text-accent-foreground"
          :class="{ 'bg-accent text-accent-foreground': $route.path === item.path }"
          @click="isMobileMenuOpen = false"
        >
          <component :is="item.icon" class="w-4 h-4" />
          {{ item.name }}
        </NuxtLink>
        <div class="border-t border-border/40 my-2"></div>
        <template v-if="userStore.isLoggedIn">
          <div class="flex items-center gap-3 px-3 py-2">
            <img 
              v-if="userStore.currentUser?.avatar" 
              :src="userStore.currentUser.avatar" 
              class="h-8 w-8 rounded-full object-cover"
            />
            <div 
              v-else 
              class="flex h-8 w-8 items-center justify-center rounded-full bg-primary text-primary-foreground text-xs font-medium"
            >
              {{ getAvatarText(userStore.currentUser?.nickname || userStore.currentUser?.username || '') }}
            </div>
            <div class="flex flex-col">
              <span class="text-sm font-medium">{{ userStore.currentUser?.nickname || userStore.currentUser?.username }}</span>
              <span class="text-xs text-muted-foreground">{{ userStore.currentUser?.email }}</span>
            </div>
          </div>
          <NuxtLink 
            to="/user" 
            class="flex items-center gap-2 px-3 py-2 text-sm font-medium text-muted-foreground rounded-md hover:bg-accent hover:text-accent-foreground"
            @click="isMobileMenuOpen = false"
          >
            <User class="w-4 h-4" />
            个人中心
          </NuxtLink>
          <NuxtLink 
            to="/user/favorites" 
            class="flex items-center gap-2 px-3 py-2 text-sm font-medium text-muted-foreground rounded-md hover:bg-accent hover:text-accent-foreground"
            @click="isMobileMenuOpen = false"
          >
            <Heart class="w-4 h-4" />
            我的收藏
          </NuxtLink>
          <NuxtLink 
            to="/user/history" 
            class="flex items-center gap-2 px-3 py-2 text-sm font-medium text-muted-foreground rounded-md hover:bg-accent hover:text-accent-foreground"
            @click="isMobileMenuOpen = false"
          >
            <History class="w-4 h-4" />
            最近使用
          </NuxtLink>
          <NuxtLink 
            to="/user/reviews" 
            class="flex items-center gap-2 px-3 py-2 text-sm font-medium text-muted-foreground rounded-md hover:bg-accent hover:text-accent-foreground"
            @click="isMobileMenuOpen = false"
          >
            <Star class="w-4 h-4" />
            我的评价
          </NuxtLink>
          <NuxtLink 
            to="/pricing" 
            class="flex items-center gap-2 px-3 py-2 text-sm font-medium text-muted-foreground rounded-md hover:bg-accent hover:text-accent-foreground"
            @click="isMobileMenuOpen = false"
          >
            <Crown class="w-4 h-4" />
            {{ userStore.isVip ? 'VIP会员' : '开通会员' }}
          </NuxtLink>
          <NuxtLink 
            to="/user/settings" 
            class="flex items-center gap-2 px-3 py-2 text-sm font-medium text-muted-foreground rounded-md hover:bg-accent hover:text-accent-foreground"
            @click="isMobileMenuOpen = false"
          >
            <Settings class="w-4 h-4" />
            账号设置
          </NuxtLink>
          <button 
            class="flex items-center gap-2 px-3 py-2 text-sm font-medium text-destructive rounded-md hover:bg-destructive/10 text-left"
            @click="logout(); isMobileMenuOpen = false"
          >
            <LogOut class="w-4 h-4" />
            退出登录
          </button>
        </template>
        <template v-else>
          <button 
            class="px-3 py-2 text-sm font-medium text-muted-foreground rounded-md hover:bg-accent hover:text-accent-foreground text-left cursor-pointer"
            @click="openLoginDialog"
          >
            登录
          </button>
          <button 
            class="px-3 py-2 text-sm font-medium bg-primary text-primary-foreground rounded-md text-left cursor-pointer"
            @click="openRegisterDialog"
          >
            注册
          </button>
        </template>
      </nav>
    </div>

    <!-- 登录/注册弹窗 -->
    <AuthDialog v-model:open="authDialogOpen" :default-tab="authDialogTab" />
  </header>
</template>
