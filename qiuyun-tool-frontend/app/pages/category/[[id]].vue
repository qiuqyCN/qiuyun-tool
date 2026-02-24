<script setup lang="ts">
import { Button } from '@/components/ui/button'
import { Input } from '@/components/ui/input'
import { Badge } from '@/components/ui/badge'
import { 
  Search, 
  Grid3X3, 
  List,
  Crown,
  TrendingUp,
  Star,
  ChevronRight,
  Filter
} from 'lucide-vue-next'
import { categories, getTools, tools } from '@/composables/useTools'

const route = useRoute()
const router = useRouter()

const categoryId = computed(() => route.params.id as string || 'all')
const searchQuery = ref('')
const viewMode = ref<'grid' | 'list'>('grid')

// 当前分类
const currentCategory = computed(() => {
  if (categoryId.value === 'all') {
    return { id: 'all', name: '全部工具', description: '浏览所有可用工具' }
  }
  return categories.find(c => c.id === categoryId.value)
})

// 工具列表
const filteredTools = computed(() => {
  let result = getTools(categoryId.value)
  
  if (searchQuery.value) {
    const keyword = searchQuery.value.toLowerCase()
    result = result.filter(tool => 
      tool.name.toLowerCase().includes(keyword) ||
      tool.description.toLowerCase().includes(keyword)
    )
  }
  
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
const switchCategory = (id: string) => {
  if (id === 'all') {
    router.push('/category')
  } else {
    router.push(`/category/${id}`)
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
          <template v-if="currentCategory && currentCategory.id !== 'all'">
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
        <aside class="lg:w-64 flex-shrink-0">
          <div class="sticky top-24">
            <div class="flex items-center gap-2 mb-4">
              <Filter class="w-4 h-4" />
              <span class="font-semibold">筛选</span>
            </div>
            
            <div class="space-y-1">
              <button
                @click="switchCategory('all')"
                class="w-full flex items-center gap-3 px-4 py-3 rounded-lg text-left transition-colors"
                :class="categoryId === 'all' ? 'bg-primary text-primary-foreground' : 'hover:bg-muted text-foreground'"
              >
                <Grid3X3 class="w-5 h-5" />
                <span>全部工具</span>
                <span class="ml-auto text-sm opacity-70">{{ tools.length }}</span>
              </button>
              
              <button
                v-for="category in categories"
                :key="category.id"
                @click="switchCategory(category.id)"
                class="w-full flex items-center gap-3 px-4 py-3 rounded-lg text-left transition-colors"
                :class="categoryId === category.id ? 'bg-primary text-primary-foreground' : 'hover:bg-muted text-foreground'"
              >
                <component :is="category.icon" class="w-5 h-5" />
                <span>{{ category.name }}</span>
                <span class="ml-auto text-sm opacity-70">
                  {{ tools.filter(t => t.category === category.id).length }}
                </span>
              </button>
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
            
            <div class="flex items-center gap-2">
              <span class="text-sm text-muted-foreground">
                共 {{ filteredTools.length }} 个工具
              </span>
              <div class="flex items-center border rounded-lg overflow-hidden">
                <button
                  @click="viewMode = 'grid'"
                  class="p-2 transition-colors"
                  :class="viewMode === 'grid' ? 'bg-primary text-primary-foreground' : 'hover:bg-muted'"
                >
                  <Grid3X3 class="w-4 h-4" />
                </button>
                <button
                  @click="viewMode = 'list'"
                  class="p-2 transition-colors"
                  :class="viewMode === 'list' ? 'bg-primary text-primary-foreground' : 'hover:bg-muted'"
                >
                  <List class="w-4 h-4" />
                </button>
              </div>
            </div>
          </div>

          <!-- Grid View -->
          <div v-if="viewMode === 'grid'" class="grid grid-cols-1 sm:grid-cols-2 xl:grid-cols-3 gap-4">
            <NuxtLink 
              v-for="tool in filteredTools" 
              :key="tool.id"
              :to="`/tool/${tool.id}`"
              class="group p-5 bg-background rounded-xl border border-border/40 hover:border-primary/50 hover:shadow-lg transition-all"
            >
              <div class="flex items-start justify-between mb-3">
                <div class="w-12 h-12 rounded-xl bg-gradient-to-br from-primary/20 to-primary/10 flex items-center justify-center">
                  <component :is="tool.icon" class="w-6 h-6 text-primary" />
                </div>
                <div class="flex items-center gap-2">
                  <Badge v-if="tool.tags.includes('热门')" variant="destructive" class="text-xs">
                    HOT
                  </Badge>
                  <Badge v-if="tool.isVip" variant="secondary" class="text-xs">
                    <Crown class="w-3 h-3 mr-1" />
                    VIP
                  </Badge>
                </div>
              </div>
              
              <h3 class="font-semibold text-foreground mb-2">{{ tool.name }}</h3>
              <p class="text-sm text-muted-foreground mb-4 line-clamp-2">{{ tool.description }}</p>
              
              <div class="flex items-center justify-between pt-4 border-t border-border/40">
                <div class="flex items-center gap-3 text-xs text-muted-foreground">
                  <div class="flex items-center gap-1">
                    <TrendingUp class="w-3 h-3" />
                    <span>{{ formatVisits(tool.visits) }}</span>
                  </div>
                  <div class="flex items-center gap-1">
                    <Star class="w-3 h-3 text-yellow-500" />
                    <span>{{ tool.rating }}</span>
                  </div>
                </div>
                <span class="text-xs text-muted-foreground">{{ tool.reviewCount }} 评价</span>
              </div>
            </NuxtLink>
          </div>

          <!-- List View -->
          <div v-else class="space-y-3">
            <NuxtLink 
              v-for="tool in filteredTools" 
              :key="tool.id"
              :to="`/tool/${tool.id}`"
              class="group flex items-center gap-4 p-4 bg-background rounded-xl border border-border/40 hover:border-primary/50 hover:shadow-lg transition-all"
            >
              <div class="w-14 h-14 rounded-xl bg-gradient-to-br from-primary/20 to-primary/10 flex items-center justify-center flex-shrink-0">
                <component :is="tool.icon" class="w-7 h-7 text-primary" />
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
