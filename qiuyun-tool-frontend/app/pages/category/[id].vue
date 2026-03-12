<script setup lang="ts">
import { Button } from '@/components/ui/button'
import { Input } from '@/components/ui/input'
import { Badge } from '@/components/ui/badge'
import { useToolStore } from '@/stores/toolStore'
import type { ToolResponse } from '@/types/api'
import { 
  Search, 
  Grid3X3, 
  List,
  Crown,
  TrendingUp,
  Star,
  ChevronRight,
  Filter,
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
  Scale,
  ArrowRightLeft,
  Coffee,
  FileCode
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
  'ArrowRightLeft': ArrowRightLeft,
  'Coffee': Coffee,
  'FileCode': FileCode,
}

// 获取分类图标组件
const getIconComponent = (iconName: string) => {
  return categoryIconMap[iconName] || Wrench
}

// 获取工具图标组件
const getToolIconComponent = (iconName: string) => {
  return toolIconMap[iconName] || Wrench
}

const route = useRoute()
const router = useRouter()

// 使用 Pinia Store
const toolStore = useToolStore()

// SSR：确保 store 已初始化
await useAsyncData('category-store-init', async () => {
  if (!toolStore.initialized || toolStore.tools.length === 0) {
    await toolStore.initialize()
  }
  return true
}, {
  server: true
})

const categoryId = computed(() => route.params.id as string || 'all')
const searchQuery = ref('')

// 价格模式筛选：免费和VIP复选框，默认都选中
const showFree = ref(true)
const showVip = ref(true)

// 排序方式：visits-访问量, rating-评分, newest-最新创建, name-名称
type SortMode = 'visits' | 'rating' | 'newest' | 'name'
const sortMode = ref<SortMode>('visits')

// 从 localStorage 读取视图模式，默认 grid
const viewMode = ref<'grid' | 'list'>('grid')

// 页面加载时恢复视图模式
onMounted(() => {
  if (import.meta.client) {
    const savedViewMode = localStorage.getItem('categoryViewMode')
    if (savedViewMode === 'grid' || savedViewMode === 'list') {
      viewMode.value = savedViewMode
    }
  }
})

// 监听视图模式变化并保存到 localStorage
watch(viewMode, (newMode) => {
  if (import.meta.client) {
    localStorage.setItem('categoryViewMode', newMode)
  }
})

// 从 store 获取分类列表
const categories = computed(() => toolStore.categories)

// 从 store 获取所有工具
const allTools = computed(() => toolStore.tools)

// 当前分类
const currentCategory = computed(() => {
  if (categoryId.value === 'all') {
    return { code: 'all', name: '全部工具', description: '浏览所有可用工具' }
  }
  const cat = categories.value.find(c => c.code === categoryId.value)
  return cat || { code: categoryId.value, name: '未知分类', description: '' }
})

// 获取分类下的工具数量
const getToolCountByCategory = (categoryCode: string): number => {
  if (categoryCode === 'all') {
    return allTools.value.length
  }
  return allTools.value.filter(t => t.category === categoryCode).length
}

// 获取指定分类的工具列表
const getToolsByCategory = (categoryCode: string): ToolResponse[] => {
  if (categoryCode === 'all') {
    return allTools.value
  }
  return allTools.value.filter(t => t.category === categoryCode)
}

// 工具列表
const filteredTools = computed(() => {
  let result = getToolsByCategory(categoryId.value)

  // 价格模式筛选（复选框逻辑）
  if (!showFree.value && !showVip.value) {
    // 如果都没选中，显示空
    result = []
  } else if (showFree.value && !showVip.value) {
    // 只显示免费
    result = result.filter(tool => !tool.isVip)
  } else if (!showFree.value && showVip.value) {
    // 只显示VIP
    result = result.filter(tool => tool.isVip)
  }
  // 如果都选中，显示全部（不筛选）

  // 搜索关键词筛选
  if (searchQuery.value) {
    const keyword = searchQuery.value.toLowerCase()
    result = result.filter(tool =>
      tool.name.toLowerCase().includes(keyword) ||
      tool.description.toLowerCase().includes(keyword)
    )
  }

  // 排序
  result = [...result].sort((a, b) => {
    switch (sortMode.value) {
      case 'visits':
        return (b.visits || 0) - (a.visits || 0)
      case 'rating':
        return (b.rating || 0) - (a.rating || 0)
      case 'newest':
        // 使用 createdAt 排序，如果没有则使用 id 倒序
        if (a.createdAt && b.createdAt) {
          return new Date(b.createdAt).getTime() - new Date(a.createdAt).getTime()
        }
        return b.id - a.id
      case 'name':
        return a.name.localeCompare(b.name, 'zh-CN')
      default:
        return 0
    }
  })

  return result
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

// 切换分类
const switchCategory = (code: string) => {
  if (code === 'all') {
    router.push('/category')
  } else {
    router.push(`/category/${code}`)
  }
}
</script>

<template>
  <div class="min-h-screen bg-background">
    <!-- Header -->
    <div class="border-b border-border/40 bg-muted/30">
      <div class="container mx-auto px-4 py-8">
        <div class="flex items-center gap-2 text-sm text-muted-foreground mb-4">
          <NuxtLink to="/" class="hover:text-foreground">首页</NuxtLink>
          <ChevronRight class="w-4 h-4" />
          <span class="text-foreground">分类</span>
          <template v-if="currentCategory && currentCategory.code !== 'all'">
            <ChevronRight class="w-4 h-4" />
            <span class="text-foreground">{{ currentCategory.name }}</span>
          </template>
        </div>
        
        <h1 class="text-3xl font-bold text-foreground mb-2">
          {{ currentCategory?.name || '全部工具' }}
        </h1>
        <p class="text-muted-foreground">
          {{ currentCategory?.description || '浏览所有可用工具' }}
        </p>
      </div>
    </div>

    <div class="container mx-auto px-4 py-8">
      <div class="flex flex-col lg:flex-row gap-8">
        <!-- Sidebar -->
        <aside class="lg:w-64 shrink-0">
          <div class="sticky top-24 space-y-6">
            <!-- 分类筛选 -->
            <div>
              <div class="flex items-center gap-2 mb-4">
                <Filter class="w-4 h-4" />
                <span class="font-semibold">分类</span>
              </div>

              <div class="space-y-1">
                <button
                  @click="switchCategory('all')"
                  class="w-full flex items-center gap-3 px-4 py-3 rounded-lg text-left transition-colors"
                  :class="categoryId === 'all' ? 'bg-primary text-primary-foreground' : 'hover:bg-muted text-foreground'"
                >
                  <Grid3X3 class="w-5 h-5" />
                  <span>全部工具</span>
                  <span class="ml-auto text-sm opacity-70">{{ getToolCountByCategory('all') }}</span>
                </button>

                <button
                  v-for="category in categories"
                  :key="category.code"
                  @click="switchCategory(category.code)"
                  class="w-full flex items-center gap-3 px-4 py-3 rounded-lg text-left transition-colors"
                  :class="categoryId === category.code ? 'bg-primary text-primary-foreground' : 'hover:bg-muted text-foreground'"
                >
                  <component :is="getIconComponent(category.icon)" class="w-5 h-5" />
                  <span>{{ category.name }}</span>
                  <span class="ml-auto text-sm opacity-70">
                    {{ getToolCountByCategory(category.code) }}
                  </span>
                </button>
              </div>
            </div>

            <!-- 价格筛选 -->
            <div>
              <div class="flex items-center gap-2 mb-4">
                <Crown class="w-4 h-4" />
                <span class="font-semibold">价格</span>
              </div>

              <div class="space-y-2 px-2">
                <label class="flex items-center gap-3 cursor-pointer group">
                  <div class="relative flex items-center">
                    <input
                      v-model="showFree"
                      type="checkbox"
                      class="peer sr-only"
                    />
                    <div class="w-5 h-5 border-2 border-border rounded transition-colors peer-checked:bg-primary peer-checked:border-primary peer-focus:ring-2 peer-focus:ring-primary/20"></div>
                    <svg class="absolute w-3 h-3 text-primary-foreground opacity-0 peer-checked:opacity-100 left-1 top-1" viewBox="0 0 14 14" fill="none">
                      <path d="M3 7L6 10L11 4" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                    </svg>
                  </div>
                  <span class="text-sm text-foreground group-hover:text-foreground/80">免费</span>
                </label>

                <label class="flex items-center gap-3 cursor-pointer group">
                  <div class="relative flex items-center">
                    <input
                      v-model="showVip"
                      type="checkbox"
                      class="peer sr-only"
                    />
                    <div class="w-5 h-5 border-2 border-border rounded transition-colors peer-checked:bg-primary peer-checked:border-primary peer-focus:ring-2 peer-focus:ring-primary/20"></div>
                    <svg class="absolute w-3 h-3 text-primary-foreground opacity-0 peer-checked:opacity-100 left-1 top-1" viewBox="0 0 14 14" fill="none">
                      <path d="M3 7L6 10L11 4" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                    </svg>
                  </div>
                  <span class="text-sm text-foreground group-hover:text-foreground/80 flex items-center gap-1">
                    <Crown class="w-3 h-3 text-yellow-500" />
                    VIP
                  </span>
                </label>
              </div>
            </div>
          </div>
        </aside>

        <!-- Main Content -->
        <div class="flex-1">
          <!-- Toolbar -->
          <div class="flex flex-col sm:flex-row items-start sm:items-center justify-between gap-4 mb-6">
            <div class="relative flex-1 max-w-md w-full">
              <Search class="absolute left-3 top-1/2 -translate-y-1/2 w-4 h-4 text-muted-foreground" />
              <Input
                v-model="searchQuery"
                type="text"
                placeholder="搜索工具..."
                class="pl-10"
              />
            </div>

            <div class="flex items-center gap-5 flex-wrap">

              <span class="text-sm text-muted-foreground">
                共 {{ filteredTools.length }} 个工具
              </span>
              <!-- 视图切换 -->
              <div class="flex items-center gap-1">
                <button
                  @click="viewMode = 'grid'"
                  class="p-2 rounded-lg transition-colors"
                  :class="viewMode === 'grid' ? 'bg-muted text-foreground' : 'text-muted-foreground hover:text-foreground hover:bg-muted/50'"
                  title="网格视图"
                >
                  <Grid3X3 class="w-4 h-4" />
                </button>
                <button
                  @click="viewMode = 'list'"
                  class="p-2 rounded-lg transition-colors"
                  :class="viewMode === 'list' ? 'bg-muted text-foreground' : 'text-muted-foreground hover:text-foreground hover:bg-muted/50'"
                  title="列表视图"
                >
                  <List class="w-4 h-4" />
                </button>
              </div>
               <!-- 排序方式分段控制器 -->
              <div class="flex items-center gap-1">
                <span class="text-sm text-muted-foreground mr-1">排序：</span>
                <button
                  @click="sortMode = 'name'"
                  class="px-3 py-1.5 text-sm transition-colors relative"
                  :class="sortMode === 'name' ? 'text-foreground font-medium' : 'text-muted-foreground hover:text-foreground'"
                >
                  名称
                  <span
                    v-if="sortMode === 'name'"
                    class="absolute bottom-0 left-1/2 -translate-x-1/2 w-4 h-0.5 bg-primary rounded-full"
                  />
                </button>
                <button
                  @click="sortMode = 'visits'"
                  class="px-3 py-1.5 text-sm transition-colors relative"
                  :class="sortMode === 'visits' ? 'text-foreground font-medium' : 'text-muted-foreground hover:text-foreground'"
                >
                  最热
                  <span
                    v-if="sortMode === 'visits'"
                    class="absolute bottom-0 left-1/2 -translate-x-1/2 w-4 h-0.5 bg-primary rounded-full"
                  />
                </button>
                <button
                  @click="sortMode = 'newest'"
                  class="px-3 py-1.5 text-sm transition-colors relative"
                  :class="sortMode === 'newest' ? 'text-foreground font-medium' : 'text-muted-foreground hover:text-foreground'"
                >
                  最新
                  <span
                    v-if="sortMode === 'newest'"
                    class="absolute bottom-0 left-1/2 -translate-x-1/2 w-4 h-0.5 bg-primary rounded-full"
                  />
                </button>
                <button
                  @click="sortMode = 'rating'"
                  class="px-3 py-1.5 text-sm transition-colors relative"
                  :class="sortMode === 'rating' ? 'text-foreground font-medium' : 'text-muted-foreground hover:text-foreground'"
                >
                  推荐
                  <span
                    v-if="sortMode === 'rating'"
                    class="absolute bottom-0 left-1/2 -translate-x-1/2 w-4 h-0.5 bg-primary rounded-full"
                  />
                </button>
              </div>
            </div>
          </div>

          <!-- Grid View -->
          <div v-if="viewMode === 'grid'" class="grid grid-cols-1 sm:grid-cols-2 xl:grid-cols-3 gap-4">
            <NuxtLink
              v-for="tool in filteredTools"
              :key="tool.id"
              :to="`/${tool.category}/${tool.code}`"
              class="group p-5 bg-background rounded-xl border border-border/40 hover:border-primary/50 hover:shadow-lg transition-all"
            >
              <!-- 工具头部：图标 + 标题 + 标签 -->
              <div class="flex items-start gap-4 mb-3">
                <div
                  class="w-14 h-14 rounded-xl flex items-center justify-center shrink-0"
                  :style="{ backgroundColor: tool.iconBgColor || 'hsl(var(--primary) / 0.1)' }"
                >
                  <component
                    :is="getToolIconComponent(tool.icon)"
                    class="w-7 h-7"
                    :style="{ color: tool.iconColor || 'hsl(var(--primary))' }"
                  />
                </div>
                <div class="flex-1 min-w-0">
                  <div class="flex items-center gap-2 mb-1">
                    <h3 class="font-semibold text-foreground truncate">{{ tool.name }}</h3>
                    <Badge v-if="tool.isVip" variant="secondary" class="text-xs shrink-0">
                      <Crown class="w-3 h-3 mr-1" />
                      VIP
                    </Badge>
                    <Badge v-else variant="outline" class="text-xs shrink-0">免费</Badge>
                  </div>
                  <div class="flex items-center gap-1">
                    <div class="flex">
                      <Star v-for="i in 5" :key="i" :class="['w-3 h-3', i <= Math.round(tool.rating) ? 'text-yellow-500 fill-yellow-500' : 'text-muted-foreground']" />
                    </div>
                    <span class="text-xs text-muted-foreground">{{ tool.rating }} ({{ tool.reviewCount }}评价)</span>
                  </div>
                </div>
              </div>
              
              <p class="text-sm text-muted-foreground mb-4 line-clamp-2">{{ tool.description }}</p>
              
              <!-- 标签 -->
              <div v-if="tool.tags && tool.tags.length > 0" class="flex flex-wrap gap-2 mb-4">
                <Badge v-for="tag in tool.tags.slice(0, 3)" :key="tag" variant="outline" class="text-xs">
                  {{ tag }}
                </Badge>
              </div>
              
              <!-- 底部按钮 -->
              <div class="flex items-center justify-between pt-4 border-t border-border/40">
                <Button variant="ghost" size="sm" class="text-sm">
                  查看详情
                </Button>
                <Button size="sm" class="gap-1">
                  立即访问
                  <ChevronRight class="w-4 h-4" />
                </Button>
              </div>
            </NuxtLink>
          </div>

          <!-- List View -->
          <div v-else class="space-y-3">
            <NuxtLink
              v-for="tool in filteredTools"
              :key="tool.id"
              :to="`/${tool.category}/${tool.code}`"
              class="group flex items-center gap-4 p-4 bg-background rounded-xl border border-border/40 hover:border-primary/50 hover:shadow-lg transition-all"
            >
              <div
                class="w-14 h-14 rounded-xl flex items-center justify-center shrink-0"
                :style="{ backgroundColor: tool.iconBgColor || 'hsl(var(--primary) / 0.1)' }"
              >
                <component
                  :is="getToolIconComponent(tool.icon)"
                  class="w-7 h-7"
                  :style="{ color: tool.iconColor || 'hsl(var(--primary))' }"
                />
              </div>
              
              <div class="flex-1 min-w-0">
                <div class="flex items-center gap-2 mb-1">
                  <h3 class="font-semibold text-foreground">{{ tool.name }}</h3>
                  <Badge v-if="tool.tags.includes('热门')" variant="destructive" class="text-xs">
                    HOT
                  </Badge>
                  <Badge v-if="tool.isVip" variant="secondary" class="text-xs">
                    <Crown class="w-3 h-3 mr-1" />
                    VIP
                  </Badge>
                </div>
                <p class="text-sm text-muted-foreground truncate">{{ tool.description }}</p>
              </div>
              
              <div class="hidden sm:flex items-center gap-4 text-sm text-muted-foreground">
                <div class="flex items-center gap-1">
                  <TrendingUp class="w-4 h-4" />
                  <span>{{ formatVisits(tool.visits) }}</span>
                </div>
                <div class="flex items-center gap-1">
                  <Star class="w-4 h-4 text-yellow-500" />
                  <span>{{ tool.rating }}</span>
                </div>
              </div>
              
              <ChevronRight class="w-5 h-5 text-muted-foreground group-hover:text-primary transition-colors" />
            </NuxtLink>
          </div>

          <!-- Empty State -->
          <div v-if="filteredTools.length === 0" class="text-center py-16">
            <div class="w-16 h-16 rounded-full bg-muted flex items-center justify-center mx-auto mb-4">
              <Search class="w-8 h-8 text-muted-foreground" />
            </div>
            <h3 class="text-lg font-semibold text-foreground mb-2">没有找到相关工具</h3>
            <p class="text-muted-foreground">换个关键词试试，或浏览其他分类</p>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>
