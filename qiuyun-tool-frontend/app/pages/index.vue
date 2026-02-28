<script setup lang="ts">
import { Badge } from '@/components/ui/badge'
import { Button } from '@/components/ui/button'
import { Tabs, TabsContent, TabsList, TabsTrigger } from '@/components/ui/tabs'
import { useToolStore } from '@/stores/toolStore'
import type { ToolResponse } from '@/composables/useApi'

import {
  ArrowRight,
  Binary,
  Braces,
  Calculator,
  Clock,
  Code,
  Code2,
  Crown,
  Dices,
  FileEdit,
  FileJson,
  FileMinus,
  FileText,
  FileType,
  Fingerprint,
  Flame,
  GitCompare,
  Hash,
  Heart,
  Image,
  ImageMinus,
  ImagePlus,
  Key,
  Link,
  Loader2,
  Lock,
  Plus,
  QrCode,
  Scale,
  Search,
  Star,
  Text,
  Type,
  Wrench
} from 'lucide-vue-next'

// SEO 配置
useSeoMeta({
  title: '秋云工具 - 程序员必备工具箱',
  description: '高效、简洁、实用的在线工具集合，助力开发者提升工作效率。提供JSON格式化、代码压缩、正则测试等开发常用工具。',
  keywords: '在线工具,开发工具,JSON格式化,代码压缩,正则测试,程序员工具'
})

// 使用 Pinia Store
const toolStore = useToolStore()

// 当前选中的 tab
const activeTab = ref('hot')

// 是否登录（模拟）
const isLoggedIn = ref(false)

// SSR：在服务端获取数据
// 注意：useAsyncData 会在服务端和客户端都执行，但会避免重复请求
const { data: storeData, pending, error } = await useAsyncData('tool-store-init', async () => {
  // 如果 store 已经有数据，直接返回
  if (toolStore.initialized && toolStore.tools.length > 0) {
    return {
      categories: toolStore.categories,
      tools: toolStore.tools
    }
  }

  // 否则调用 initialize 获取数据
  await toolStore.initialize()
  return {
    categories: toolStore.categories,
    tools: toolStore.tools
  }
}, {
  // 服务端渲染时使用
  server: true,
  // 客户端不重新获取（由 store 控制）
  default: () => ({
    categories: [],
    tools: []
  })
})

// 计算属性：从 store 获取数据
const categories = computed(() => toolStore.categories)
const hotTools = computed(() => toolStore.hotTools)
const newTools = computed(() => toolStore.newTools)
const categoryTools = computed(() => toolStore.categoryTools)
const loading = computed(() => toolStore.loading || pending.value)
const storeError = computed(() => toolStore.error || error.value)

// 统计数据（从 store 计算）
const stats = computed(() => ({
  totalTools: toolStore.totalTools,
  monthlyNewTools: toolStore.monthlyNewTools,
  totalVisits: toolStore.totalVisits
}))

// 获取最近访问的工具（从 localStorage 读取）
const recentTools = computed(() => {
  if (import.meta.client) {
    const recentIds = JSON.parse(localStorage.getItem('recentTools') || '[]') as string[]
    return toolStore.tools.filter((t: ToolResponse) => recentIds.includes(t.code))
      .slice(0, 8)
  }
  return []
})

// 获取收藏的工具（模拟数据）
const favoriteTools = computed(() => {
  if (!isLoggedIn.value) return []
  // 模拟收藏数据
  return toolStore.hotTools.filter((t: ToolResponse) => t.tags.includes('热门')).slice(0, 8)
})

// 所有工具（用于搜索）
const allTools = computed(() => toolStore.tools)

// 格式化访问数
const formatVisits = (visits: number) => {
  if (visits >= 10000) {
    return (visits / 10000).toFixed(1) + 'w'
  }
  if (visits >= 1000) {
    return (visits / 1000).toFixed(1) + 'k'
  }
  return visits.toString()
}

// 分类图标映射表
const categoryIconMap: Record<string, any> = {
  'Code': Code,
  'Image': Image,
  'FileText': FileText,
  'Lock': Lock,
  'Type': Type,
  'Calculator': Calculator,
}

// 工具图标映射表
const toolIconMap: Record<string, any> = {
  'Braces': Braces,
  'FileJson': FileJson,
  'Code2': Code2,
  'Search': Search,
  'Clock': Clock,
  'Binary': Binary,
  'ImageMinus': ImageMinus,
  'ImagePlus': ImagePlus,
  'Image': Image,
  'QrCode': QrCode,
  'FileType': FileType,
  'FileMinus': FileMinus,
  'FileEdit': FileEdit,
  'Hash': Hash,
  'Fingerprint': Fingerprint,
  'Link': Link,
  'Key': Key,
  'GitCompare': GitCompare,
  'Text': Text,
  'Type': Type,
  'Dices': Dices,
  'Scale': Scale,
}

// 获取分类图标组件
const getIconComponent = (iconName: string) => {
  return categoryIconMap[iconName] || Wrench
}

// 获取工具图标组件
const getToolIconComponent = (iconName: string) => {
  return toolIconMap[iconName] || Wrench
}

// 处理搜索
const handleSearch = (query: string) => {
  if (query.trim()) {
    navigateTo(`/category?search=${encodeURIComponent(query.trim())}`)
  }
}

// 获取分类下的工具数量（从工具列表计算）
const getToolCountByCategory = (categoryCode: string) => {
  return toolStore.tools.filter((t: ToolResponse) => t.category === categoryCode).length
}

// 处理工具选择
const handleToolSelect = (tool: ToolResponse) => {
  navigateTo(`/tool/${tool.code}`)
}

// 重新加载数据
const reloadData = () => {
  toolStore.initialize()
}
</script>

<template>
  <div>
    <!-- Hero Section -->
    <HeroSection 
      :tools="allTools" 
      :loading="loading"
      :stats="stats"
      @select="handleToolSelect" 
    />

    <!-- Tabs Section -->
    <section class="py-16">
      <div class="container mx-auto px-4">
        <Tabs v-model="activeTab" class="w-full">
          <TabsList class="w-full max-w-md mx-auto mb-8">
            <TabsTrigger value="hot" class="flex-1 flex items-center gap-2">
              <Flame class="w-4 h-4" />
              热门推荐
            </TabsTrigger>
            <TabsTrigger value="recent" class="flex-1 flex items-center gap-2">
              <Clock class="w-4 h-4" />
              最近访问
            </TabsTrigger>
            <TabsTrigger value="favorite" class="flex-1 flex items-center gap-2">
              <Heart class="w-4 h-4" />
              我的收藏
            </TabsTrigger>
            <TabsTrigger value="new" class="flex-1 flex items-center gap-2">
              <Plus class="w-4 h-4" />
              最新收录
            </TabsTrigger>
          </TabsList>

          <!-- 热门推荐 -->
          <TabsContent value="hot">
            <div class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4">
              <NuxtLink 
                v-for="tool in hotTools" 
                :key="tool.id"
                :to="`/tool/${tool.code}`"
                class="group p-4 bg-background rounded-xl border border-border/40 hover:border-primary/50 hover:shadow-lg transition-all"
              >
                <div class="flex items-start gap-3">
                  <div class="w-12 h-12 rounded-lg bg-primary/10 flex items-center justify-center shrink-0">
                    <component :is="getToolIconComponent(tool.icon)" class="w-6 h-6 text-primary" />
                  </div>
                  <div class="flex-1 min-w-0">
                    <div class="flex items-center gap-2 mb-1">
                      <h3 class="font-semibold text-foreground truncate">{{ tool.name }}</h3>
                      <Badge v-if="tool.isVip" variant="secondary" class="text-xs shrink-0">
                        <Crown class="w-3 h-3 mr-1" />
                        VIP
                      </Badge>
                    </div>
                    <p class="text-sm text-muted-foreground line-clamp-2">{{ tool.description }}</p>
                  </div>
                </div>
              </NuxtLink>
            </div>
          </TabsContent>

          <!-- 最近访问 -->
          <TabsContent value="recent">
            <div v-if="recentTools.length > 0" class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4">
              <NuxtLink 
                v-for="tool in recentTools" 
                :key="tool.id"
                :to="`/tool/${tool.code}`"
                class="group p-4 bg-background rounded-xl border border-border/40 hover:border-primary/50 hover:shadow-lg transition-all"
              >
                <div class="flex items-start gap-3">
                  <div class="w-12 h-12 rounded-lg bg-primary/10 flex items-center justify-center shrink-0">
                    <component :is="getToolIconComponent(tool.icon)" class="w-6 h-6 text-primary" />
                  </div>
                  <div class="flex-1 min-w-0">
                    <div class="flex items-center gap-2 mb-1">
                      <h3 class="font-semibold text-foreground truncate">{{ tool.name }}</h3>
                      <Badge v-if="tool.isVip" variant="secondary" class="text-xs shrink-0">
                        <Crown class="w-3 h-3 mr-1" />
                        VIP
                      </Badge>
                    </div>
                    <p class="text-sm text-muted-foreground line-clamp-2">{{ tool.description }}</p>
                  </div>
                </div>
              </NuxtLink>
            </div>
            <div v-else class="text-center py-16 text-muted-foreground">
              <Clock class="w-12 h-12 mx-auto mb-4 opacity-50" />
              <p>暂无最近访问记录</p>
              <p class="text-sm mt-2">使用工具后会自动记录在这里</p>
            </div>
          </TabsContent>

          <!-- 我的收藏 -->
          <TabsContent value="favorite">
            <div v-if="isLoggedIn">
              <div v-if="favoriteTools.length > 0" class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4">
                <NuxtLink 
                  v-for="tool in favoriteTools" 
                  :key="tool.id"
                  :to="`/tool/${tool.code}`"
                  class="group p-4 bg-background rounded-xl border border-border/40 hover:border-primary/50 hover:shadow-lg transition-all"
                >
                  <div class="flex items-start gap-3">
                    <div class="w-12 h-12 rounded-lg bg-primary/10 flex items-center justify-center shrink-0">
                      <component :is="getToolIconComponent(tool.icon)" class="w-6 h-6 text-primary" />
                    </div>
                    <div class="flex-1 min-w-0">
                      <div class="flex items-center gap-2 mb-1">
                        <h3 class="font-semibold text-foreground truncate">{{ tool.name }}</h3>
                        <Badge v-if="tool.isVip" variant="secondary" class="text-xs shrink-0">
                          <Crown class="w-3 h-3 mr-1" />
                          VIP
                        </Badge>
                      </div>
                      <p class="text-sm text-muted-foreground line-clamp-2">{{ tool.description }}</p>
                    </div>
                  </div>
                </NuxtLink>
              </div>
              <div v-else class="text-center py-16 text-muted-foreground">
                <Heart class="w-12 h-12 mx-auto mb-4 opacity-50" />
                <p>暂无收藏工具</p>
                <p class="text-sm mt-2">点击工具详情页的收藏按钮添加</p>
              </div>
            </div>
            <div v-else class="text-center py-16">
              <Heart class="w-12 h-12 mx-auto mb-4 text-muted-foreground opacity-50" />
              <p class="text-muted-foreground mb-4">登录后可查看收藏工具</p>
              <Button @click="navigateTo('/login')">去登录</Button>
            </div>
          </TabsContent>

          <!-- 最新收录 -->
          <TabsContent value="new">
            <div class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4">
              <NuxtLink 
                v-for="tool in newTools" 
                :key="tool.id"
                :to="`/tool/${tool.code}`"
                class="group p-4 bg-background rounded-xl border border-border/40 hover:border-primary/50 hover:shadow-lg transition-all"
              >
                <div class="flex items-start gap-3">
                  <div class="w-12 h-12 rounded-lg bg-primary/10 flex items-center justify-center shrink-0">
                    <component :is="getToolIconComponent(tool.icon)" class="w-6 h-6 text-primary" />
                  </div>
                  <div class="flex-1 min-w-0">
                    <div class="flex items-center gap-2 mb-1">
                      <h3 class="font-semibold text-foreground truncate">{{ tool.name }}</h3>
                      <Badge v-if="tool.isVip" variant="secondary" class="text-xs shrink-0">
                        <Crown class="w-3 h-3 mr-1" />
                        VIP
                      </Badge>
                    </div>
                    <p class="text-sm text-muted-foreground line-clamp-2">{{ tool.description }}</p>
                  </div>
                </div>
              </NuxtLink>
            </div>
          </TabsContent>
        </Tabs>
      </div>
    </section>

    
    <!-- Categories Section -->
    <section class="py-16 bg-muted/30">
      <div class="container mx-auto px-4">
        <!-- 标题 -->
        <div class="text-center mb-10">
          <h2 class="text-2xl font-bold text-foreground mb-2">工具分类</h2>
          <p class="text-muted-foreground">按类别快速找到你需要的工具</p>
        </div>
        
        <!-- 加载状态 -->
        <div v-if="loading" class="flex items-center justify-center py-12">
          <Loader2 class="w-8 h-8 animate-spin text-primary" />
          <span class="ml-2 text-muted-foreground">加载中...</span>
        </div>
        
        <!-- 错误状态 -->
        <div v-else-if="storeError" class="text-center py-12">
          <p class="text-red-500 mb-4">{{ storeError }}</p>
          <Button @click="reloadData">重新加载</Button>
        </div>
        
        <!-- 分类卡片网格 -->
        <div v-else class="grid grid-cols-2 sm:grid-cols-3 lg:grid-cols-6 gap-4">
          <NuxtLink 
            v-for="category in categories" 
            :key="category.id"
            :to="`/category/${category.code}`"
            class="group relative p-6 bg-background rounded-2xl border border-border/40 hover:border-primary/30 hover:shadow-xl hover:shadow-primary/5 transition-all duration-300 hover:-translate-y-1"
          >
            <!-- 背景渐变装饰 -->
            <div class="absolute inset-0 rounded-2xl bg-linear-to-br from-primary/5 to-transparent opacity-0 group-hover:opacity-100 transition-opacity duration-300" />
            
            <div class="relative flex flex-col items-center text-center">
              <!-- 图标容器 -->
              <div class="w-14 h-14 rounded-xl bg-linear-to-br from-primary/10 to-primary/5 flex items-center justify-center mb-4 group-hover:scale-110 group-hover:from-primary/20 group-hover:to-primary/10 transition-all duration-300">
                <component 
                  :is="getIconComponent(category.icon)" 
                  class="w-7 h-7 text-primary" 
                />
              </div>
              
              <!-- 分类名称 -->
              <h3 class="font-semibold text-foreground mb-1 group-hover:text-primary transition-colors">
                {{ category.name }}
              </h3>
              
              <!-- 工具数量 -->
              <span class="text-xs text-muted-foreground">
                {{ getToolCountByCategory(category.code) }} 个工具
              </span>
              
              <!-- 悬停指示器 -->
              <div class="absolute -bottom-2 left-1/2 -translate-x-1/2 w-8 h-1 rounded-full bg-primary scale-x-0 group-hover:scale-x-100 transition-transform duration-300" />
            </div>
          </NuxtLink>
        </div>
      </div>
    </section>


    <!-- Categories Tools Section -->
    <section class="py-16 bg-muted/30">
      <div class="container mx-auto px-4">
        <div v-for="categoryTool in categoryTools" :key="categoryTool.categoryCode" class="mb-12 last:mb-0">
          <!-- Category Header -->
          <div class="mb-6">
            <h2 class="text-xl font-bold text-foreground mb-2">{{ categoryTool.categoryName }}</h2>
            <p class="text-sm text-muted-foreground">
              {{ categories.find((c) => c.code === categoryTool.categoryCode)?.description || '' }}
            </p>
          </div>
          
          <!-- Tools Grid -->
          <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
            <div 
              v-for="tool in categoryTool.tools" 
              :key="tool.id"
              class="group bg-background rounded-xl border border-border/40 hover:border-primary/50 hover:shadow-lg transition-all p-5"
            >
              <!-- Tool Header -->
              <div class="flex items-start justify-between mb-3">
                <div class="flex items-center gap-3">
                  <div class="w-12 h-12 rounded-lg bg-primary/10 flex items-center justify-center">
                    <component :is="getToolIconComponent(tool.icon)" class="w-6 h-6 text-primary" />
                  </div>
                  <div>
                    <h3 class="font-semibold text-foreground">{{ tool.name }}</h3>
                    <div class="flex items-center gap-1 mt-1">
                      <div class="flex">
                        <Star v-for="i in 5" :key="i" :class="['w-3 h-3', i <= Math.round(tool.rating) ? 'text-yellow-500 fill-yellow-500' : 'text-muted-foreground']" />
                      </div>
                      <span class="text-xs text-muted-foreground">{{ tool.rating }} ({{ tool.reviewCount }}评价)</span>
                    </div>
                  </div>
                </div>
                <Badge v-if="tool.isVip" variant="secondary" class="text-xs">
                  <Crown class="w-3 h-3 mr-1" />
                  VIP
                </Badge>
              </div>
              
              <!-- Description -->
              <p class="text-sm text-muted-foreground mb-4 line-clamp-2">{{ tool.description }}</p>
              
              <!-- Actions -->
              <div class="flex items-center justify-between">
                <Button variant="ghost" size="sm" as-child>
                  <NuxtLink :to="`/tool/${tool.code}`">查看详情</NuxtLink>
                </Button>
                <Button size="sm" as-child>
                  <NuxtLink :to="`/tool/${tool.code}`">
                    立即访问
                    <ArrowRight class="w-3 h-3 ml-1" />
                  </NuxtLink>
                </Button>
              </div>
            </div>
          </div>
        </div>
      </div>
    </section>

    <!-- CTA Section -->
    <section class="py-20">
      <div class="container mx-auto px-4">
        <div class="max-w-4xl mx-auto bg-linear-to-r from-primary/10 to-primary/5 rounded-2xl p-8 lg:p-12 text-center">
          <h2 class="text-3xl font-bold text-foreground mb-4">
            解锁全部功能
          </h2>
          <p class="text-lg text-muted-foreground mb-8 max-w-2xl mx-auto">
            成为VIP会员，畅享所有高级工具，无限制使用，提升工作效率
          </p>
          <div class="flex flex-col sm:flex-row items-center justify-center gap-4">
            <Button size="lg" as-child>
              <NuxtLink to="/pricing">
                <Crown class="w-5 h-5 mr-2" />
                查看会员套餐
              </NuxtLink>
            </Button>
            <Button size="lg" variant="outline" as-child>
              <NuxtLink to="/category">
                浏览全部工具
              </NuxtLink>
            </Button>
          </div>
        </div>
      </div>
    </section>
  </div>
</template>
