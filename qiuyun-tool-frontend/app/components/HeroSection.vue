<script setup lang="ts">
import { Button } from '@/components/ui/button'
import { Input } from '@/components/ui/input'
import { Badge } from '@/components/ui/badge'
import { Search, Sparkles } from 'lucide-vue-next'

const searchQuery = ref('')

const emit = defineEmits<{
  search: [query: string]
}>()

const handleSearch = () => {
  emit('search', searchQuery.value)
}
</script>

<template>
  <section class="relative py-10 lg:py-16 overflow-hidden">
    <!-- Background Grid -->
    <div class="absolute inset-0 -z-10">
      <div class="absolute inset-0 bg-[linear-gradient(to_right,#38bdf808_1px,transparent_1px),linear-gradient(to_bottom,#38bdf808_1px,transparent_1px)] bg-[size:24px_24px]"></div>
      <div class="absolute left-0 right-0 top-0 -z-10 m-auto h-[310px] w-[310px] rounded-full bg-sky-400 opacity-20 blur-[100px]"></div>
    </div>
    
    <div class="container mx-auto px-4 relative">
      <div class="max-w-3xl mx-auto text-center">
        <Badge variant="secondary" class="mb-4">
          <Sparkles class="w-3 h-3 mr-1" />
          程序员必备工具箱
        </Badge>
        <h1 class="text-4xl lg:text-6xl font-bold text-foreground mb-6 text-transparent bg-clip-text bg-gradient-to-r from-cyan-200 to-blue-600">
          秋云工具
        </h1>
        <p class="text-lg lg:text-xl text-muted-foreground mb-8">
          高效、简洁、实用的在线工具集合<br class="hidden sm:block" />
          助力开发者提升工作效率
        </p>
        
        <!-- Search Box -->
        <div class="max-w-xl mx-auto relative">
          <div class="relative">
            <Search class="absolute left-3 top-1/2 -translate-y-1/2 w-5 h-5 text-muted-foreground" />
            <Input 
              v-model="searchQuery"
              type="text" 
              placeholder="搜索工具..."
              class="pl-10 pr-24 h-12 text-base"
              @keyup.enter="handleSearch"
            />
            <Button 
              class="absolute right-1 top-1/2 -translate-y-1/2 h-10"
              @click="handleSearch"
            >
              搜索
            </Button>
          </div>
        </div>

        <!-- Quick Stats -->
        <div class="flex items-center justify-center gap-8 mt-8 text-sm text-muted-foreground">
          <div class="flex items-center gap-2">
            <span class="font-semibold text-foreground">30+</span>
            <span>实用工具</span>
          </div>
          <div class="flex items-center gap-2">
            <span class="font-semibold text-foreground">100w+</span>
            <span>月使用量</span>
          </div>
          <div class="flex items-center gap-2">
            <span class="font-semibold text-foreground">4.9</span>
            <span>用户评分</span>
          </div>
        </div>
      </div>
    </div>
  </section>
</template>
