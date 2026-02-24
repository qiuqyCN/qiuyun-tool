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
  ChevronRight
} from 'lucide-vue-next'
import { rankings, getToolById } from '@/composables/useTools'

const activeTab = ref('daily')

const rankingData = computed(() => {
  return rankings[activeTab.value as keyof typeof rankings].map(item => ({
    ...item,
    tool: getToolById(item.toolId)
  })).filter(item => item.tool)
})

const getRankIcon = (rank: number) => {
  if (rank === 1) return Medal
  if (rank === 2) return Award
  if (rank === 3) return Trophy
  return null
}

const getRankClass = (rank: number) => {
  if (rank === 1) return 'text-yellow-500'
  if (rank === 2) return 'text-gray-400'
  if (rank === 3) return 'text-amber-600'
  return 'text-muted-foreground'
}

const getRankBgClass = (rank: number) => {
  if (rank === 1) return 'bg-yellow-500/10 border-yellow-500/30'
  if (rank === 2) return 'bg-gray-400/10 border-gray-400/30'
  if (rank === 3) return 'bg-amber-600/10 border-amber-600/30'
  return 'bg-muted/30 border-border/40'
}

const formatVisits = (visits: number) => {
  if (visits >= 10000) {
    return (visits / 10000).toFixed(1) + 'w'
  }
  if (visits >= 1000) {
    return (visits / 1000).toFixed(1) + 'k'
  }
  return visits.toString()
}
</script>

<template>
  <div class="min-h-screen bg-background">
    <!-- Header -->
    <div class="border-b border-border/40 bg-muted/30">
      <div class="container mx-auto px-4 py-12">
        <div class="flex items-center gap-2 text-sm text-muted-foreground mb-4">
          <NuxtLink to="/" class="hover:text-foreground">首页</NuxtLink>
          <ChevronRight class="w-4 h-4" />
          <span class="text-foreground">排行榜</span>
        </div>
        
        <div class="flex items-center gap-4">
          <div class="w-16 h-16 rounded-2xl bg-gradient-to-br from-yellow-400 to-orange-500 flex items-center justify-center">
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
        <TabsList class="grid w-full max-w-md mx-auto grid-cols-3 mb-8">
          <TabsTrigger value="daily">
            <Flame class="w-4 h-4 mr-2" />
            日榜
          </TabsTrigger>
          <TabsTrigger value="weekly">
            <TrendingUp class="w-4 h-4 mr-2" />
            周榜
          </TabsTrigger>
          <TabsTrigger value="monthly">
            <Trophy class="w-4 h-4 mr-2" />
            月榜
          </TabsTrigger>
        </TabsList>

        <TabsContent value="daily" class="mt-0">
          <RankingList :rankings="rankingData" />
        </TabsContent>
        
        <TabsContent value="weekly" class="mt-0">
          <RankingList :rankings="rankingData" />
        </TabsContent>
        
        <TabsContent value="monthly" class="mt-0">
          <RankingList :rankings="rankingData" />
        </TabsContent>
      </Tabs>
    </div>
  </div>
</template>

<script lang="ts">
// 子组件
import { Crown, Star, TrendingUp } from 'lucide-vue-next'

const RankingList = defineComponent({
  props: {
    rankings: {
      type: Array as PropType<Array<any>>,
      required: true
    }
  },
  setup(props) {
    const getRankIcon = (rank: number) => {
      if (rank === 1) return Medal
      if (rank === 2) return Award
      if (rank === 3) return Trophy
      return null
    }

    const getRankClass = (rank: number) => {
      if (rank === 1) return 'text-yellow-500'
      if (rank === 2) return 'text-gray-400'
      if (rank === 3) return 'text-amber-600'
      return 'text-muted-foreground'
    }

    const getRankBgClass = (rank: number) => {
      if (rank === 1) return 'bg-yellow-500/10 border-yellow-500/30'
      if (rank === 2) return 'bg-gray-400/10 border-gray-400/30'
      if (rank === 3) return 'bg-amber-600/10 border-amber-600/30'
      return 'bg-muted/30 border-border/40'
    }

    const formatVisits = (visits: number) => {
      if (visits >= 10000) {
        return (visits / 10000).toFixed(1) + 'w'
      }
      if (visits >= 1000) {
        return (visits / 1000).toFixed(1) + 'k'
      }
      return visits.toString()
    }

    return { getRankIcon, getRankClass, getRankBgClass, formatVisits, Crown, Star, TrendingUp }
  },
  template: `
    <div class="max-w-3xl mx-auto space-y-4">
      <NuxtLink
        v-for="item in rankings"
        :key="item.tool.id"
        :to="\`/tool/\${item.tool.id}\`"
        :class="[
          'group flex items-center gap-4 p-4 rounded-xl border transition-all hover:shadow-lg',
          getRankBgClass(item.rank)
        ]"
      >
        <!-- Rank -->
        <div class="flex-shrink-0 w-12 h-12 flex items-center justify-center">
          <component
            v-if="getRankIcon(item.rank)"
            :is="getRankIcon(item.rank)"
            :class="['w-8 h-8', getRankClass(item.rank)]"
          />
          <span v-else :class="['text-2xl font-bold', getRankClass(item.rank)]">
            {{ item.rank }}
          </span>
        </div>

        <!-- Icon -->
        <div class="w-14 h-14 rounded-xl bg-gradient-to-br from-primary/20 to-primary/10 flex items-center justify-center flex-shrink-0">
          <component :is="item.tool.icon" class="w-7 h-7 text-primary" />
        </div>

        <!-- Info -->
        <div class="flex-1 min-w-0">
          <div class="flex items-center gap-2 mb-1">
            <h3 class="font-semibold text-foreground">{{ item.tool.name }}</h3>
            <Badge v-if="item.tool.isVip" variant="secondary" class="text-xs">
              <Crown class="w-3 h-3 mr-1" />
              VIP
            </Badge>
          </div>
          <p class="text-sm text-muted-foreground truncate">{{ item.tool.description }}</p>
        </div>

        <!-- Stats -->
        <div class="hidden sm:flex flex-col items-end gap-1">
          <div class="flex items-center gap-1 text-sm font-medium text-foreground">
            <TrendingUp class="w-4 h-4 text-green-500" />
            {{ formatVisits(item.visits) }}
          </div>
          <div class="flex items-center gap-1 text-sm text-muted-foreground">
            <Star class="w-4 h-4 text-yellow-500" />
            {{ item.tool.rating }}
          </div>
        </div>

        <!-- Arrow -->
        <ChevronRight class="w-5 h-5 text-muted-foreground group-hover:text-primary transition-colors" />
      </NuxtLink>
    </div>
  `
})
</script>
