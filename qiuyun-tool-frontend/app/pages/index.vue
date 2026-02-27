<script setup lang="ts">
import { Button } from '@/components/ui/button'
import { Badge } from '@/components/ui/badge'
import { Tabs, TabsList, TabsTrigger, TabsContent } from '@/components/ui/tabs'
import {
  Tooltip,
  TooltipContent,
  TooltipProvider,
  TooltipTrigger,
} from '@/components/ui/tooltip'
import { 
  ArrowRight, 
  Flame, 
  Sparkles,
  Crown,
  TrendingUp,
  ChevronRight,
  Clock,
  Heart,
  Plus
} from 'lucide-vue-next'
import { categories, getHotTools, getNewTools, tools } from '@/composables/useTools'

// 当前选中的 tab
const activeTab = ref('hot')

// 是否登录（模拟）
const isLoggedIn = ref(false)

// 获取热门工具
const hotTools = computed(() => getHotTools(8))

// 获取最新工具
const newTools = computed(() => getNewTools(8))

// 获取最近访问的工具（从 localStorage 读取）
const recentTools = computed(() => {
  if (import.meta.client) {
    const recentIds = JSON.parse(localStorage.getItem('recentTools') || '[]') as string[]
    return recentIds
      .map(id => tools.find(t => t.id === id))
      .filter((t): t is typeof tools[0] => !!t)
      .slice(0, 8)
  }
  return []
})

// 获取收藏的工具（模拟数据）
const favoriteTools = computed(() => {
  if (!isLoggedIn.value) return []
  // 模拟收藏数据
  return tools.filter(t => t.tags.includes('热门')).slice(0, 8)
})

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

// 导入所有需要的图标
import { 
  Code, 
  Image, 
  FileText, 
  Lock, 
  Type, 
  Calculator,
  Wrench,
  Braces,
  FileJson,
  Code2,
  Search,
  Clock,
  Binary,
  ImageMinus,
  ImagePlus,
  QrCode,
  FileType,
  FileMinus,
  FileEdit,
  Hash,
  Fingerprint,
  Link,
  Key,
  GitCompare,
  Text,
  Dices,
  Scale
} from 'lucide-vue-next'

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
</script>

<template>
  <div>
    <!-- Hero Section -->
    <HeroSection @search="handleSearch" />

    <!-- Categories Section -->
    <section class="py-12 bg-muted/30">
      <div class="container mx-auto px-4">
        <TooltipProvider>
          <div class="flex flex-wrap items-center justify-center gap-3">
            <Tooltip v-for="category in categories" :key="category.id">
              <TooltipTrigger as-child>
                <NuxtLink 
                  :to="`/category/${category.id}`"
                  class="group flex items-center gap-2 px-4 py-2 bg-background rounded-full border border-border/40 hover:border-primary/50 hover:shadow-sm transition-all"
                >
                  <div class="w-6 h-6 rounded-md bg-primary/10 flex items-center justify-center group-hover:bg-primary/20 transition-colors">
                    <component 
                      :is="getIconComponent(category.icon)" 
                      class="w-3.5 h-3.5 text-primary" 
                    />
                  </div>
                  <span class="text-sm font-medium text-foreground">{{ category.name }}</span>
                </NuxtLink>
              </TooltipTrigger>
              <TooltipContent>
                <p>{{ category.description }}</p>
              </TooltipContent>
            </Tooltip>
          </div>
        </TooltipProvider>
      </div>
    </section>

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
                :to="`/tool/${tool.id}`"
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
                :to="`/tool/${tool.id}`"
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
                  :to="`/tool/${tool.id}`"
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
                :to="`/tool/${tool.id}`"
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

    <!-- Categories Tools Section -->
    <section class="py-16 bg-muted/30">
      <div class="container mx-auto px-4">
        <div v-for="category in categories" :key="category.id" class="mb-12 last:mb-0">
          <!-- Category Header -->
          <div class="mb-6">
            <h2 class="text-xl font-bold text-foreground mb-2">{{ category.name }}</h2>
            <p class="text-sm text-muted-foreground">{{ category.description }}</p>
          </div>
          
          <!-- Tools Grid -->
          <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
            <div 
              v-for="tool in tools.filter(t => t.category === category.id).slice(0, 6)" 
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
                  <NuxtLink :to="`/tool/${tool.id}`">查看详情</NuxtLink>
                </Button>
                <Button size="sm" as-child>
                  <NuxtLink :to="`/tool/${tool.id}`">
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
        <div class="max-w-4xl mx-auto bg-gradient-to-r from-primary/10 to-primary/5 rounded-2xl p-8 lg:p-12 text-center">
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
