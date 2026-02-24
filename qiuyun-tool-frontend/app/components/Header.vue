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
  Trophy
} from 'lucide-vue-next'

const colorMode = useColorMode()
const isMobileMenuOpen = ref(false)

// 模拟用户登录状态
const isLoggedIn = ref(false)
const user = ref({
  name: '张三',
  avatar: '',
  membership: '包年会员'
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

const logout = () => {
  isLoggedIn.value = false
}
</script>

<template>
  <header class="sticky top-0 z-50 w-full border-b border-border/40 bg-background/95 backdrop-blur supports-[backdrop-filter]:bg-background/60">
    <div class="container mx-auto px-4 h-16 flex items-center justify-between">
      <!-- Logo -->
      <NuxtLink to="/" class="flex items-center gap-2 mr-6">
        <img src="/logo.svg" alt="秋云工具" class="w-9 h-9 bg-sky-400 text-white px-1 py-1 rounded-md" />
        <span class="text-2xl font-bold text-foreground">秋云工具</span>
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

        <!-- Desktop User Menu -->
        <div class="hidden md:flex items-center gap-2">
          <template v-if="isLoggedIn">
            <DropdownMenu>
              <DropdownMenuTrigger as-child>
                <Button variant="ghost" class="relative h-9 w-9 rounded-full">
                  <div class="flex h-9 w-9 items-center justify-center rounded-full bg-primary text-primary-foreground text-sm font-medium">
                    {{ user.name.charAt(0) }}
                  </div>
                </Button>
              </DropdownMenuTrigger>
              <DropdownMenuContent class="w-56" align="end">
                <div class="flex items-center gap-2 p-2">
                  <div class="flex h-8 w-8 items-center justify-center rounded-full bg-primary text-primary-foreground text-sm font-medium">
                    {{ user.name.charAt(0) }}
                  </div>
                  <div class="flex flex-col">
                    <span class="text-sm font-medium">{{ user.name }}</span>
                    <span class="text-xs text-muted-foreground">{{ user.membership }}</span>
                  </div>
                </div>
                <DropdownMenuSeparator />
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
                  <NuxtLink to="/pricing" class="cursor-pointer">
                    <Crown class="mr-2 h-4 w-4" />
                    会员中心
                  </NuxtLink>
                </DropdownMenuItem>
                <DropdownMenuSeparator />
                <DropdownMenuItem @click="logout" class="cursor-pointer">
                  <LogOut class="mr-2 h-4 w-4" />
                  退出登录
                </DropdownMenuItem>
              </DropdownMenuContent>
            </DropdownMenu>
          </template>
          <template v-else>
            <Button variant="ghost" as-child>
              <NuxtLink to="/login"><span class="text-sm font-medium">登录</span></NuxtLink>
            </Button>
            <Button as-child>
              <NuxtLink to="/register"><span class="text-sm font-medium">注册</span></NuxtLink>
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
        <template v-if="isLoggedIn">
          <NuxtLink 
            to="/user" 
            class="px-3 py-2 text-sm font-medium text-muted-foreground rounded-md hover:bg-accent hover:text-accent-foreground"
            @click="isMobileMenuOpen = false"
          >
            个人中心
          </NuxtLink>
          <button 
            class="px-3 py-2 text-sm font-medium text-muted-foreground rounded-md hover:bg-accent hover:text-accent-foreground text-left"
            @click="logout(); isMobileMenuOpen = false"
          >
            退出登录
          </button>
        </template>
        <template v-else>
          <NuxtLink 
            to="/login" 
            class="px-3 py-2 text-sm font-medium text-muted-foreground rounded-md hover:bg-accent hover:text-accent-foreground"
            @click="isMobileMenuOpen = false"
          >
            登录
          </NuxtLink>
          <NuxtLink 
            to="/register" 
            class="px-3 py-2 text-sm font-medium bg-primary text-primary-foreground rounded-md"
            @click="isMobileMenuOpen = false"
          >
            注册
          </NuxtLink>
        </template>
      </nav>
    </div>
  </header>
</template>
