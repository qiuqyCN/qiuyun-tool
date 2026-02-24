<script setup lang="ts">
import { Button } from '@/components/ui/button'
import { Badge } from '@/components/ui/badge'
import { 
  ArrowRight, 
  Flame, 
  Sparkles,
  Crown,
  TrendingUp,
  ChevronRight
} from 'lucide-vue-next'
import { categories, getHotTools, getNewTools } from '@/composables/useTools'

// 获取热门工具
const hotTools = computed(() => getHotTools(8))

// 获取最新工具
const newTools = computed(() => getNewTools(6))

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

// 获取图标组件
const getIconComponent = (iconName: string) => {
  const iconMap: Record<string, any> = {
    'Code': 'Code',
    'Image': 'Image',
    'FileText': 'FileText',
    'Lock': 'Lock',
    'Type': 'Type',
    'Calculator': 'Calculator',
  }
  return iconMap[iconName] || 'Wrench'
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
    <section class="py-16 bg-muted/30">
      <div class="container mx-auto px-4">
        <div class="flex items-center justify-between mb-8">
          <h2 class="text-2xl font-bold text-foreground">工具分类</h2>
          <NuxtLink to="/category" class="text-sm text-primary flex items-center gap-1 hover:underline">
            查看全部
            <ChevronRight class="w-4 h-4" />
          </NuxtLink>
        </div>
        
        <div class="grid grid-cols-2 md:grid-cols-3 lg:grid-cols-6 gap-4">
          <NuxtLink 
            v-for="category in categories" 
            :key="category.id"
            :to="`/category/${category.id}`"
            class="group p-6 bg-background rounded-xl border border-border/40 hover:border-primary/50 hover:shadow-lg transition-all"
          >
            <div class="w-12 h-12 rounded-lg bg-primary/10 flex items-center justify-center mb-4 group-hover:bg-primary/20 transition-colors">
              <component 
                :is="getIconComponent(category.icon)" 
                class="w-6 h-6 text-primary" 
              />
            </div>
            <h3 class="font-semibold text-foreground mb-1">{{ category.name }}</h3>
            <p class="text-xs text-muted-foreground line-clamp-2">{{ category.description }}</p>
          </NuxtLink>
        </div>
      </div>
    </section>

    <!-- Hot Tools Section -->
    <section class="py-16">
      <div class="container mx-auto px-4">
        <div class="flex items-center justify-between mb-8">
          <div class="flex items-center gap-2">
            <Flame class="w-6 h-6 text-orange-500" />
            <h2 class="text-2xl font-bold text-foreground">热门推荐</h2>
          </div>
          <NuxtLink to="/ranking" class="text-sm text-primary flex items-center gap-1 hover:underline">
            查看排行榜
            <ChevronRight class="w-4 h-4" />
          </NuxtLink>
        </div>
        
        <div class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4">
          <NuxtLink 
            v-for="tool in hotTools" 
            :key="tool.id"
            :to="`/tool/${tool.id}`"
            class="group p-5 bg-background rounded-xl border border-border/40 hover:border-primary/50 hover:shadow-lg transition-all"
          >
            <div class="flex items-start justify-between mb-3">
              <div class="w-10 h-10 rounded-lg bg-primary/10 flex items-center justify-center">
                <component :is="tool.icon" class="w-5 h-5 text-primary" />
              </div>
              <Badge v-if="tool.isVip" variant="secondary" class="text-xs">
                <Crown class="w-3 h-3 mr-1" />
                VIP
              </Badge>
            </div>
            <h3 class="font-semibold text-foreground mb-1">{{ tool.name }}</h3>
            <p class="text-sm text-muted-foreground mb-3 line-clamp-2">{{ tool.description }}</p>
            <div class="flex items-center justify-between text-xs text-muted-foreground">
              <div class="flex items-center gap-1">
                <TrendingUp class="w-3 h-3" />
                <span>{{ formatVisits(tool.visits) }}</span>
              </div>
              <div class="flex items-center gap-1">
                <span class="text-yellow-500">★</span>
                <span>{{ tool.rating }}</span>
              </div>
            </div>
          </NuxtLink>
        </div>
      </div>
    </section>

    <!-- New Tools Section -->
    <section class="py-16 bg-muted/30">
      <div class="container mx-auto px-4">
        <div class="flex items-center justify-between mb-8">
          <div class="flex items-center gap-2">
            <Sparkles class="w-6 h-6 text-purple-500" />
            <h2 class="text-2xl font-bold text-foreground">最新收录</h2>
          </div>
        </div>
        
        <div class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-4">
          <NuxtLink 
            v-for="tool in newTools" 
            :key="tool.id"
            :to="`/tool/${tool.id}`"
            class="group flex items-center gap-4 p-4 bg-background rounded-xl border border-border/40 hover:border-primary/50 hover:shadow-lg transition-all"
          >
            <div class="w-12 h-12 rounded-lg bg-primary/10 flex items-center justify-center flex-shrink-0">
              <component :is="tool.icon" class="w-6 h-6 text-primary" />
            </div>
            <div class="flex-1 min-w-0">
              <div class="flex items-center gap-2 mb-1">
                <h3 class="font-semibold text-foreground">{{ tool.name }}</h3>
                <Badge v-if="tool.isVip" variant="secondary" class="text-xs">
                  <Crown class="w-3 h-3" />
                </Badge>
              </div>
              <p class="text-sm text-muted-foreground truncate">{{ tool.description }}</p>
            </div>
            <ArrowRight class="w-5 h-5 text-muted-foreground group-hover:text-primary transition-colors" />
          </NuxtLink>
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
