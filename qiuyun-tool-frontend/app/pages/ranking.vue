<script setup lang="ts">
import { Button } from '@/components/ui/button'
import { Badge } from '@/components/ui/badge'
import { Tabs, TabsContent, TabsList, TabsTrigger } from '@/components/ui/tabs'
import {
  Trophy,
  Flame,
  TrendingUp,
  Crown,
  Medal,
  Award,
  ChevronRight,
  Star,
  Users,
  Eye,
  Heart,
  Braces,
  FileJson,
  Code2,
  Search,
  Clock,
  Binary,
  ImageMinus,
  ImagePlus,
  Image,
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
  Type,
  Dices,
  Scale,
  Wrench
} from 'lucide-vue-next'

// 排行榜类型
const activeTab = ref('realtime')
const loading = ref(false)
const rankingData = ref<RankingItem[]>([])

// 图标映射
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

const getToolIconComponent = (iconName: string) => {
  return toolIconMap[iconName] || Wrench
}

// 排行榜数据接口
interface RankingItem {
  rank: number
  toolId: number
  toolCode: string
  toolName: string
  description: string
  icon: string
  iconColor: string
  iconBgColor: string
  category: string
  isVip: boolean
  rating: number
  visitCount: number
  usageCount: number
  favoriteCount: number
  hotScore: number
}

// 获取排行榜数据
const fetchRankings = async () => {
  loading.value = true
  try {
    const config = useRuntimeConfig()
    const response = await $fetch('/rankings', {
      baseURL: config.public.apiBaseUrl,
      params: {
        type: activeTab.value,
        limit: 10
      }
    })

    if (response.code === 200) {
      rankingData.value = response.data || []
    }
  } catch (error) {
    console.error('获取排行榜失败:', error)
  } finally {
    loading.value = false
  }
}

// 监听标签切换
watch(activeTab, () => {
  fetchRankings()
})

// 页面加载时获取数据
onMounted(() => {
  fetchRankings()
})

// 获取排名图标
const getRankIcon = (rank: number) => {
  if (rank === 1) return Medal
  if (rank === 2) return Award
  if (rank === 3) return Trophy
  return null
}

// 获取排名样式类
const getRankClass = (rank: number) => {
  if (rank === 1) return 'text-yellow-500'
  if (rank === 2) return 'text-gray-400'
  if (rank === 3) return 'text-amber-600'
  return 'text-muted-foreground'
}

// 获取排名背景样式类
const getRankBgClass = (rank: number) => {
  if (rank === 1) return 'bg-yellow-500/10 border-yellow-500/30'
  if (rank === 2) return 'bg-gray-400/10 border-gray-400/30'
  if (rank === 3) return 'bg-amber-600/10 border-amber-600/30'
  return 'bg-muted/30 border-border/40'
}

// 格式化数字
const formatNumber = (num: number) => {
  if (num >= 10000) {
    return (num / 10000).toFixed(1) + 'w'
  }
  if (num >= 1000) {
    return (num / 1000).toFixed(1) + 'k'
  }
  return num.toString()
}

// 获取榜单标题
const getRankingTitle = (type: string) => {
  switch (type) {
    case 'realtime': return '实时榜'
    case 'daily': return '日榜'
    case 'weekly': return '周榜'
    case 'monthly': return '月榜'
    default: return ''
  }
}

// 获取榜单描述
const getRankingDesc = (type: string) => {
  switch (type) {
    case 'realtime': return '今日实时热度排行'
    case 'daily': return '昨日热度排行'
    case 'weekly': return '上周热度排行'
    case 'monthly': return '上月热度排行'
    default: return ''
  }
}
</script>

<template>
  <div class="min-h-screen bg-background">
    <div class="border-b border-border/40 bg-muted/30">
      <div class="container mx-auto px-4 py-12">
        <div class="flex items-center gap-2 text-sm text-muted-foreground mb-4">
          <NuxtLink to="/" class="hover:text-foreground">首页</NuxtLink>
          <ChevronRight class="w-4 h-4" />
          <span class="text-foreground">排行榜</span>
        </div>

        <div class="flex items-center gap-4">
          <div class="w-16 h-16 rounded-2xl bg-linear-to-br from-yellow-400 to-orange-500 flex items-center justify-center">
            <Trophy class="w-8 h-8 text-white" />
          </div>
          <div>
            <h1 class="text-3xl font-bold text-foreground">工具热度榜</h1>
            <p class="text-muted-foreground">发现最受欢迎的工具</p>
          </div>
        </div>
      </div>
    </div>

    <div class="container mx-auto px-4 py-8">
      <Tabs v-model="activeTab" class="w-full">
        <TabsList class="grid w-full max-w-lg mx-auto grid-cols-4 mb-8">
          <TabsTrigger value="realtime">
            <Flame class="w-4 h-4 mr-2" />
            实时
          </TabsTrigger>
          <TabsTrigger value="daily">
            <TrendingUp class="w-4 h-4 mr-2" />
            日榜
          </TabsTrigger>
          <TabsTrigger value="weekly">
            <Award class="w-4 h-4 mr-2" />
            周榜
          </TabsTrigger>
          <TabsTrigger value="monthly">
            <Trophy class="w-4 h-4 mr-2" />
            月榜
          </TabsTrigger>
        </TabsList>

        <!-- 榜单说明 -->
        <div class="text-center mb-6">
          <h2 class="text-xl font-semibold text-foreground">{{ getRankingTitle(activeTab) }}</h2>
          <p class="text-sm text-muted-foreground">{{ getRankingDesc(activeTab) }}</p>
          <p class="text-xs text-muted-foreground mt-1">
            热度分 = 使用×60% + 浏览×30% + 收藏×10%
          </p>
        </div>

        <!-- 加载状态 -->
        <div v-if="loading" class="max-w-3xl mx-auto space-y-4">
          <div v-for="i in 5" :key="i" class="flex items-center gap-4 p-4 rounded-xl bg-muted/30 animate-pulse">
            <div class="w-12 h-12 bg-muted rounded-full"></div>
            <div class="w-14 h-14 bg-muted rounded-xl"></div>
            <div class="flex-1 space-y-2">
              <div class="w-32 h-4 bg-muted rounded"></div>
              <div class="w-48 h-3 bg-muted rounded"></div>
            </div>
          </div>
        </div>

        <!-- 空状态 -->
        <div v-else-if="rankingData.length === 0" class="text-center py-16">
          <div class="w-16 h-16 rounded-full bg-muted flex items-center justify-center mx-auto mb-4">
            <Trophy class="w-8 h-8 text-muted-foreground" />
          </div>
          <h3 class="text-lg font-semibold text-foreground mb-2">暂无数据</h3>
          <p class="text-muted-foreground">该榜单暂时还没有数据</p>
        </div>

        <!-- 榜单内容 -->
        <div v-else class="max-w-3xl mx-auto space-y-4">
          <NuxtLink
            v-for="item in rankingData"
            :key="item.toolId"
            :to="`/${item.category}/${item.toolCode}`"
            :class="[
              'group flex items-center gap-4 p-4 rounded-xl border transition-all hover:shadow-lg',
              getRankBgClass(item.rank)
            ]"
          >
            <!-- 排名 -->
            <div class="shrink-0 w-12 h-12 flex items-center justify-center">
              <component
                v-if="getRankIcon(item.rank)"
                :is="getRankIcon(item.rank)"
                :class="['w-8 h-8', getRankClass(item.rank)]"
              />
              <span v-else :class="['text-2xl font-bold', getRankClass(item.rank)]">
                {{ item.rank }}
              </span>
            </div>

            <!-- 工具图标 -->
            <div
              class="w-14 h-14 rounded-xl flex items-center justify-center shrink-0"
              :style="{ backgroundColor: item.iconBgColor || 'hsl(var(--primary) / 0.1)' }"
            >
              <component
                :is="getToolIconComponent(item.icon)"
                class="w-7 h-7"
                :style="{ color: item.iconColor || 'hsl(var(--primary))' }"
              />
            </div>

            <!-- 工具信息 -->
            <div class="flex-1 min-w-0">
              <div class="flex items-center gap-2 mb-1">
                <h3 class="font-semibold text-foreground">{{ item.toolName }}</h3>
                <Badge v-if="item.isVip" variant="secondary" class="text-xs">
                  <Crown class="w-3 h-3 mr-1" />
                  VIP
                </Badge>
              </div>
              <p class="text-sm text-muted-foreground truncate">{{ item.description }}</p>
            </div>

            <!-- 统计数据 -->
            <div class="hidden sm:flex flex-col items-end gap-1">
              <div class="flex items-center gap-3 text-xs text-muted-foreground">
                <span class="flex items-center gap-1" title="使用次数">
                  <Users class="w-3 h-3" />
                  {{ formatNumber(item.usageCount) }}
                </span>
                <span class="flex items-center gap-1" title="浏览次数">
                  <Eye class="w-3 h-3" />
                  {{ formatNumber(item.visitCount) }}
                </span>
                <span class="flex items-center gap-1" title="收藏数">
                  <Heart class="w-3 h-3" />
                  {{ formatNumber(item.favoriteCount) }}
                </span>
              </div>
              <div class="flex items-center gap-1 text-sm font-medium text-foreground">
                <TrendingUp class="w-4 h-4 text-green-500" />
                {{ item.hotScore.toFixed(0) }} 热度
              </div>
              <div class="flex items-center gap-1 text-xs text-muted-foreground">
                <Star class="w-3 h-3 text-yellow-500" />
                {{ item.rating?.toFixed(1) || '5.0' }}
              </div>
            </div>

            <ChevronRight class="w-5 h-5 text-muted-foreground group-hover:text-primary transition-colors" />
          </NuxtLink>
        </div>
      </Tabs>
    </div>
  </div>
</template>
