<script setup lang="ts">
import { Button } from '@/components/ui/button'
import { Badge } from '@/components/ui/badge'
import { Tabs, TabsContent, TabsList, TabsTrigger } from '@/components/ui/tabs'
import { Textarea } from '@/components/ui/textarea'
import { 
  Heart, 
  Share2, 
  TrendingUp,
  Star,
  Crown,
  ChevronRight,
  MessageCircle,
  ThumbsUp,
  Clock,
  CheckCircle
} from 'lucide-vue-next'
import { getToolById, reviews } from '@/composables/useTools'

const route = useRoute()
const toolId = route.params.id as string
const tool = getToolById(toolId)

// 如果没有找到工具，显示404
if (!tool) {
  throw createError({ statusCode: 404, statusMessage: 'Tool not found' })
}

const isFavorite = ref(false)
const activeTab = ref('format')
const showRatingModal = ref(false)
const userRating = ref(0)
const userReview = ref('')
const isLoggedIn = ref(false)

const formatVisits = (visits: number) => {
  if (visits >= 10000) {
    return (visits / 10000).toFixed(1) + 'w'
  }
  if (visits >= 1000) {
    return (visits / 1000).toFixed(1) + 'k'
  }
  return visits.toString()
}

const toggleFavorite = () => {
  isFavorite.value = !isFavorite.value
}

const submitRating = () => {
  showRatingModal.value = false
  userRating.value = 0
  userReview.value = ''
}
</script>

<template>
  <div class="min-h-screen bg-background">
    <!-- Breadcrumb -->
    <div class="border-b border-border/40 bg-muted/30">
      <div class="container mx-auto px-4 py-4">
        <div class="flex items-center gap-2 text-sm text-muted-foreground">
          <NuxtLink to="/" class="hover:text-foreground">首页</NuxtLink>
          <ChevronRight class="w-4 h-4" />
          <NuxtLink to="/category" class="hover:text-foreground">分类</NuxtLink>
          <ChevronRight class="w-4 h-4" />
          <span class="text-foreground">{{ tool.name }}</span>
        </div>
      </div>
    </div>

    <div class="container mx-auto px-4 py-8">
      <!-- Tool Header -->
      <div class="flex flex-col lg:flex-row lg:items-start gap-6 mb-8">
        <div class="w-20 h-20 rounded-2xl bg-gradient-to-br from-primary/20 to-primary/10 flex items-center justify-center flex-shrink-0">
          <component :is="tool.icon" class="w-10 h-10 text-primary" />
        </div>
        
        <div class="flex-1">
          <div class="flex flex-wrap items-center gap-3 mb-2">
            <h1 class="text-3xl font-bold text-foreground">{{ tool.name }}</h1>
            <Badge v-if="tool.tags.includes('热门')" variant="destructive">HOT</Badge>
            <Badge v-if="tool.isVip" variant="secondary">
              <Crown class="w-3 h-3 mr-1" />
              VIP
            </Badge>
          </div>
          
          <p class="text-muted-foreground mb-4">{{ tool.description }}</p>
          
          <div class="flex flex-wrap items-center gap-4 text-sm text-muted-foreground">
            <div class="flex items-center gap-1">
              <TrendingUp class="w-4 h-4" />
              <span>{{ formatVisits(tool.visits) }} 次使用</span>
            </div>
            <div class="flex items-center gap-1">
              <Star class="w-4 h-4 text-yellow-500" />
              <span>{{ tool.rating }} 分</span>
            </div>
            <div class="flex items-center gap-1">
              <MessageCircle class="w-4 h-4" />
              <span>{{ tool.reviewCount }} 条评价</span>
            </div>
          </div>
        </div>
        
        <div class="flex items-center gap-2">
          <Button 
            variant="outline" 
            size="icon"
            :class="isFavorite ? 'text-red-500' : ''"
            @click="toggleFavorite"
          >
            <Heart :class="['w-4 h-4', isFavorite ? 'fill-current' : '']" />
          </Button>
          <Button variant="outline" size="icon">
            <Share2 class="w-4 h-4" />
          </Button>
        </div>
      </div>

      <!-- Tool Interface -->
      <div class="grid grid-cols-1 lg:grid-cols-3 gap-8">
        <!-- Main Tool Area -->
        <div class="lg:col-span-2">
          <div class="border border-border/40 rounded-xl overflow-hidden">
            <!-- Tool Tabs -->
            <Tabs v-model="activeTab" class="w-full">
              <TabsList class="w-full justify-start rounded-none border-b bg-muted/30 p-0">
                <TabsTrigger value="format" class="rounded-none border-b-2 border-transparent data-[state=active]:border-primary data-[state=active]:bg-background px-6 py-3">
                  格式化
                </TabsTrigger>
                <TabsTrigger value="compress" class="rounded-none border-b-2 border-transparent data-[state=active]:border-primary data-[state=active]:bg-background px-6 py-3">
                  压缩
                </TabsTrigger>
                <TabsTrigger value="escape" class="rounded-none border-b-2 border-transparent data-[state=active]:border-primary data-[state=active]:bg-background px-6 py-3">
                  转义
                </TabsTrigger>
              </TabsList>
              
              <TabsContent value="format" class="m-0 p-6">
                <div class="space-y-4">
                  <div>
                    <label class="text-sm font-medium mb-2 block">输入 JSON</label>
                    <Textarea 
                      placeholder="请输入要格式化的 JSON 数据..."
                      rows="8"
                      class="font-mono text-sm"
                    />
                  </div>
                  <div class="flex items-center gap-2">
                    <Button>
                      <CheckCircle class="w-4 h-4 mr-2" />
                      格式化
                    </Button>
                    <Button variant="outline">清空</Button>
                  </div>
                  <div>
                    <label class="text-sm font-medium mb-2 block">格式化结果</label>
                    <Textarea 
                      placeholder="格式化后的结果将显示在这里..."
                      rows="8"
                      class="font-mono text-sm bg-muted/30"
                      readonly
                    />
                  </div>
                </div>
              </TabsContent>
              
              <TabsContent value="compress" class="m-0 p-6">
                <div class="text-center py-12 text-muted-foreground">
                  压缩功能开发中...
                </div>
              </TabsContent>
              
              <TabsContent value="escape" class="m-0 p-6">
                <div class="text-center py-12 text-muted-foreground">
                  转义功能开发中...
                </div>
              </TabsContent>
            </Tabs>
          </div>

          <!-- Instructions -->
          <div class="mt-8">
            <h2 class="text-lg font-semibold mb-4">使用说明</h2>
            <div class="prose prose-sm max-w-none text-muted-foreground">
              <ol class="list-decimal list-inside space-y-2">
                <li>在输入框中粘贴或输入您的 JSON 数据</li>
                <li>点击"格式化"按钮进行格式化</li>
                <li>格式化后的结果会显示在下方输出框中</li>
                <li>您可以复制输出结果或下载为文件</li>
              </ol>
            </div>
          </div>
        </div>

        <!-- Sidebar -->
        <div class="space-y-6">
          <!-- Rating Summary -->
          <div class="border border-border/40 rounded-xl p-6">
            <h3 class="font-semibold mb-4">用户评分</h3>
            <div class="flex items-center gap-4 mb-4">
              <div class="text-4xl font-bold">{{ tool.rating }}</div>
              <div>
                <div class="flex items-center gap-1">
                  <Star v-for="i in 5" :key="i" :class="['w-4 h-4', i <= Math.round(tool.rating) ? 'text-yellow-500 fill-yellow-500' : 'text-muted-foreground']" />
                </div>
                <div class="text-sm text-muted-foreground mt-1">{{ tool.reviewCount }} 条评价</div>
              </div>
            </div>
            
            <Button 
              v-if="isLoggedIn" 
              class="w-full"
              @click="showRatingModal = true"
            >
              <Star class="w-4 h-4 mr-2" />
              我要评价
            </Button>
            <Button 
              v-else 
              variant="outline" 
              class="w-full"
              as-child
            >
              <NuxtLink to="/login">
                登录后评价
              </NuxtLink>
            </Button>
          </div>

          <!-- Reviews -->
          <div class="border border-border/40 rounded-xl p-6">
            <h3 class="font-semibold mb-4">用户评价</h3>
            <div class="space-y-4">
              <div v-for="review in reviews" :key="review.id" class="border-b border-border/40 last:border-0 pb-4 last:pb-0">
                <div class="flex items-center justify-between mb-2">
                  <div class="flex items-center gap-2">
                    <div class="w-8 h-8 rounded-full bg-primary/10 flex items-center justify-center">
                      <span class="text-sm font-medium">{{ review.userName.charAt(0) }}</span>
                    </div>
                    <span class="font-medium text-sm">{{ review.userName }}</span>
                  </div>
                  <span class="text-xs text-muted-foreground">{{ review.date }}</span>
                </div>
                <div class="flex items-center gap-1 mb-2">
                  <Star v-for="i in 5" :key="i" :class="['w-3 h-3', i <= review.rating ? 'text-yellow-500 fill-yellow-500' : 'text-muted-foreground']" />
                </div>
                <p class="text-sm text-muted-foreground">{{ review.content }}</p>
                <div class="flex items-center gap-2 mt-2">
                  <button class="flex items-center gap-1 text-xs text-muted-foreground hover:text-foreground">
                    <ThumbsUp class="w-3 h-3" />
                    {{ review.likes }}
                  </button>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- Rating Modal -->
    <Dialog :open="showRatingModal" @update:open="showRatingModal = false">
      <DialogContent class="sm:max-w-[480px]">
        <DialogHeader>
          <DialogTitle>评价 {{ tool.name }}</DialogTitle>
          <DialogDescription>
            分享您的使用体验，帮助其他用户更好地了解这个工具
          </DialogDescription>
        </DialogHeader>
        
        <div class="space-y-4 py-4">
          <div>
            <Label class="mb-2 block">您的评分</Label>
            <div class="flex items-center gap-2">
              <button
                v-for="i in 5"
                :key="i"
                @click="userRating = i"
                class="p-1 transition-colors"
              >
                <Star :class="['w-8 h-8', i <= userRating ? 'text-yellow-500 fill-yellow-500' : 'text-muted-foreground']" />
              </button>
            </div>
          </div>
          
          <div>
            <Label class="mb-2 block">评价内容（选填）</Label>
            <Textarea 
              v-model="userReview"
              placeholder="分享您的使用体验..."
              rows="4"
              maxlength="200"
            />
            <div class="text-xs text-muted-foreground mt-1 text-right">
              {{ userReview.length }}/200
            </div>
          </div>
        </div>
        
        <DialogFooter>
          <Button variant="outline" @click="showRatingModal = false">取消</Button>
          <Button @click="submitRating" :disabled="userRating === 0">提交评价</Button>
        </DialogFooter>
      </DialogContent>
    </Dialog>
  </div>
</template>

<script lang="ts">
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
} from '@/components/ui/dialog'
import { Label } from '@/components/ui/label'
</script>
