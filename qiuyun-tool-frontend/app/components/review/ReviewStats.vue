<template>
  <div class="p-5 bg-muted/50 rounded-xl mb-6">
    <!-- 评分概览 -->
    <div class="flex flex-col sm:flex-row gap-8 sm:gap-10 items-center">
      <div class="flex flex-col items-center gap-2">
        <span class="text-5xl font-bold text-foreground leading-none">{{ formatRating(stats?.averageRating) }}</span>
        <StarRating :rating="stats?.averageRating || 0" :readonly="true" />
        <span class="text-sm text-muted-foreground">{{ stats?.totalCount || 0 }} 条评价</span>
      </div>

      <!-- 星级分布 -->
      <div class="flex-1 flex flex-col gap-2 min-w-[200px] w-full">
        <div
          v-for="star in [5, 4, 3, 2, 1]"
          :key="star"
          class="flex items-center gap-3"
        >
          <span class="w-8 text-sm text-muted-foreground text-right">{{ star }}星</span>
          <div class="flex-1 h-2 bg-muted rounded-full overflow-hidden">
            <div
              class="h-full bg-linear-to-r from-amber-500 to-amber-400 rounded-full transition-all duration-300"
              :style="{ width: `${getPercentage(star)}%` }"
            />
          </div>
          <span class="w-10 text-sm text-muted-foreground text-right">{{ getCount(star) }}</span>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import StarRating from './StarRating.vue'
import type { ReviewStats } from '~/types/review'

interface Props {
  stats: ReviewStats | null
}

const props = defineProps<Props>()

// 格式化评分
const formatRating = (rating?: number) => {
  if (!rating) return '0.0'
  return rating.toFixed(1)
}

// 获取各星级数量
const getCount = (star: number): number => {
  if (!props.stats) return 0
  switch (star) {
    case 5: return props.stats.fiveStarCount
    case 4: return props.stats.fourStarCount
    case 3: return props.stats.threeStarCount
    case 2: return props.stats.twoStarCount
    case 1: return props.stats.oneStarCount
    default: return 0
  }
}

// 获取各星级百分比
const getPercentage = (star: number): number => {
  if (!props.stats || props.stats.totalCount === 0) return 0
  const count = getCount(star)
  return (count / props.stats.totalCount) * 100
}
</script>
