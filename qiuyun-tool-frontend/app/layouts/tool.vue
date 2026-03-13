<template>
  <div class="min-h-screen bg-background">
    <!-- Breadcrumb -->
    <div class="border-b border-border/40 bg-muted/30">
      <div class="container mx-auto px-4 py-4">
        <div class="flex items-center gap-2 text-sm text-muted-foreground">
          <NuxtLink to="/" class="hover:text-foreground">首页</NuxtLink>
          <ChevronRight class="w-4 h-4" />
          <NuxtLink :to="`/category/${category?.code}`" class="hover:text-foreground">{{ category?.name }}</NuxtLink>
          <ChevronRight class="w-4 h-4" />
          <span class="text-foreground">{{ tool?.name }}</span>
        </div>
      </div>
    </div>

    <div class="container mx-auto px-4 py-8">
      <!-- Tool Header -->
      <div class="flex flex-col lg:flex-row lg:items-start gap-6 mb-8">
        <div
          class="w-20 h-20 rounded-2xl flex items-center justify-center shrink-0"
          :style="{ backgroundColor: tool?.iconBgColor || 'hsl(var(--primary) / 0.1)' }"
        >
          <component
            :is="getToolIcon(tool?.icon)"
            class="w-10 h-10"
            :style="{ color: tool?.iconColor || 'hsl(var(--primary))' }"
          />
        </div>

        <div class="flex-1">
          <div class="flex flex-wrap items-center gap-3 mb-2">
            <h1 class="text-3xl font-bold text-foreground">{{ tool?.name }}</h1>
            <Badge v-if="tool?.isHot" variant="destructive">HOT</Badge>
            <Badge v-if="tool?.priceMode === 'vip'" variant="secondary" class="bg-linear-to-r from-amber-400 to-orange-500 text-white border-0">
              <Crown class="w-3 h-3 mr-1" />
              VIP
            </Badge>
            <Badge v-else variant="secondary" class="bg-green-100 text-green-700 border-0">
              <CheckCircle class="w-3 h-3 mr-1" />
              免费
            </Badge>
          </div>

          <p class="text-muted-foreground mb-4">{{ tool?.description }}</p>

          <div class="flex flex-wrap items-center gap-4 text-sm text-muted-foreground">
            <div class="flex items-center gap-1">
              <TrendingUp class="w-4 h-4" />
              <span>{{ formatNumber(tool?.viewCount || 0) }} 次访问</span>
            </div>
            <div class="flex items-center gap-1">
              <Users class="w-4 h-4" />
              <span>{{ formatNumber(tool?.usageCount || 0) }} 次使用</span>
            </div>
            <div class="flex items-center gap-1">
              <Star class="w-4 h-4 text-yellow-500" />
              <span>{{ tool?.rating?.toFixed(1) || '5.0' }} 分</span>
            </div>
            <div class="flex items-center gap-1">
              <MessageCircle class="w-4 h-4" />
              <span>{{ tool?.reviewCount || 0 }} 条评价</span>
            </div>
            <div class="flex items-center gap-1">
              <Heart class="w-4 h-4 text-red-500" />
              <span>{{ formatNumber(tool?.favoriteCount || 0) }} 收藏</span>
            </div>
          </div>
        </div>

        <div class="flex items-center gap-2">
          <Button
            variant="outline"
            size="icon"
            :class="isFavorite ? 'text-red-500' : ''"
            :disabled="favoriteLoading"
            @click="toggleFavorite"
          >
            <Heart class="w-4 h-4" :class="isFavorite ? 'fill-current' : ''" />
          </Button>
          <Button variant="outline" size="icon" @click="shareTool">
            <Share2 class="w-4 h-4" />
          </Button>
          <Button variant="outline" @click="showFeedback = true">
            <MessageSquare class="w-4 h-4 mr-2" />
            反馈
          </Button>
        </div>
      </div>

      <!-- Main Content -->
      <div class="grid grid-cols-1 lg:grid-cols-3 gap-8">
        <!-- Left: Tool Execution Area + Reviews -->
        <div class="lg:col-span-2 space-y-6">
          <!-- Tool Execution Area (Slot) -->
          <div>
            <slot />
          </div>

          <!-- Reviews -->
          <div class="border border-border/40 rounded-xl p-6">
            <ToolReviews :tool-id="toolId" />
          </div>
        </div>

        <!-- Right: Info Panel -->
        <div class="space-y-6">
          <!-- Instructions -->
          <div v-if="tool?.instructions" class="border border-border/40 rounded-xl p-6">
            <h3 class="font-semibold mb-4">使用说明</h3>
            <div class="prose prose-sm max-w-none text-muted-foreground" v-html="tool.instructions"></div>
          </div>

          <!-- Related Tools -->
          <div v-if="relatedTools.length > 0" class="border border-border/40 rounded-xl p-6">
            <h3 class="font-semibold mb-4">相关工具</h3>
            <div class="space-y-3">
              <NuxtLink
                v-for="related in relatedTools"
                :key="related.code"
                :to="`/${related.category}/${related.code}`"
                class="flex items-center gap-3 p-3 rounded-lg hover:bg-muted transition-colors group"
              >
                <div
                  class="w-10 h-10 rounded-lg flex items-center justify-center shrink-0"
                  :style="{ backgroundColor: related.iconBgColor || 'hsl(var(--primary) / 0.1)' }"
                >
                  <component
                    :is="getToolIcon(related.icon)"
                    class="w-5 h-5"
                    :style="{ color: related.iconColor || 'hsl(var(--primary))' }"
                  />
                </div>
                <div class="flex-1 min-w-0">
                  <div class="text-sm font-medium group-hover:text-primary transition-colors truncate">
                    {{ related.name }}
                  </div>
                  <div class="text-xs text-muted-foreground truncate">{{ related.description }}</div>
                </div>
                <ChevronRight class="w-4 h-4 text-muted-foreground" />
              </NuxtLink>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- Feedback Modal -->
    <Dialog v-model:open="showFeedback">
      <DialogContent class="sm:max-w-md">
        <DialogHeader>
          <DialogTitle>问题反馈</DialogTitle>
          <DialogDescription>如果您在使用工具过程中遇到问题，请告诉我们</DialogDescription>
        </DialogHeader>
        <div class="space-y-4 py-4">
          <div>
            <Label>反馈类型</Label>
            <Select v-model="feedbackType">
              <SelectTrigger>
                <SelectValue placeholder="请选择反馈类型" />
              </SelectTrigger>
              <SelectContent>
                <SelectItem value="bug">功能异常</SelectItem>
                <SelectItem value="suggestion">功能建议</SelectItem>
                <SelectItem value="other">其他问题</SelectItem>
              </SelectContent>
            </Select>
          </div>
          <div>
            <Label>详细描述</Label>
            <Textarea
              v-model="feedbackContent"
              placeholder="请详细描述您遇到的问题..."
              rows="4"
            />
          </div>
        </div>
        <DialogFooter>
          <Button variant="outline" @click="showFeedback = false">取消</Button>
          <Button :disabled="!feedbackType || !feedbackContent.trim()" @click="submitFeedback">
            提交反馈
          </Button>
        </DialogFooter>
      </DialogContent>
    </Dialog>

    <!-- Auth Dialog -->
    <AuthDialog
      :open="userStore.isLoginModalVisible"
      @update:open="userStore.setLoginModalVisible($event)"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, computed, h, onMounted } from 'vue'
import { useUserStore } from '~/stores/userStore'
import { useToolStore } from '~/stores/toolStore'
import ToolReviews from '~/components/review/ToolReviews.vue'
import {
  ChevronRight,
  Crown,
  CheckCircle,
  TrendingUp,
  Users,
  Star,
  MessageCircle,
  Heart,
  Share2,
  MessageSquare,
  Eye,
  Wrench,
  Braces,
  FileJson,
  Code2,
  Search,
  Image,
  FileText,
  Lock,
  Type,
  Calculator,
  Settings,
  Sparkles,
  Code,
  Clock,
  Binary,
  ImageMinus,
  ImagePlus,
  FileEdit,
  Hash,
  Link,
  GitCompare,
  Text,
  Dices
} from 'lucide-vue-next'

// 图标映射表 - 支持 PascalCase (后端存储格式) 和 kebab-case
const iconMap: Record<string, any> = {
  // PascalCase (后端存储格式)
  'Braces': Braces,
  'FileJson': FileJson,
  'Code2': Code2,
  'Search': Search,
  'Image': Image,
  'FileText': FileText,
  'Lock': Lock,
  'Type': Type,
  'Calculator': Calculator,
  'Wrench': Wrench,
  'Settings': Settings,
  'Sparkles': Sparkles,
  'Code': Code,
  'Clock': Clock,
  'Binary': Binary,
  'ImageMinus': ImageMinus,
  'ImagePlus': ImagePlus,
  'FileEdit': FileEdit,
  'Hash': Hash,
  'Link': Link,
  'GitCompare': GitCompare,
  'Text': Text,
  'Dices': Dices,
  // kebab-case (兼容格式)
  'lucide:braces': Braces,
  'lucide:file-json': FileJson,
  'lucide:code-2': Code2,
  'lucide:search': Search,
  'lucide:image': Image,
  'lucide:file-text': FileText,
  'lucide:lock': Lock,
  'lucide:type': Type,
  'lucide:calculator': Calculator,
  'lucide:wrench': Wrench,
  'lucide:settings': Settings,
  'lucide:sparkles': Sparkles,
  'lucide:code': Code,
  'lucide:clock': Clock,
  'lucide:binary': Binary,
  'lucide:image-minus': ImageMinus,
  'lucide:image-plus': ImagePlus,
  'lucide:file-edit': FileEdit,
  'lucide:hash': Hash,
  'lucide:link': Link,
  'lucide:git-compare': GitCompare,
  'lucide:text': Text,
  'lucide:dices': Dices,
  'lucide:tool': Wrench
}

// 获取图标组件
const getToolIcon = (iconName?: string) => {
  if (!iconName) return Wrench
  // 直接匹配 PascalCase
  if (iconMap[iconName]) return iconMap[iconName]
  // 尝试转换为 kebab-case 匹配
  const kebabName = 'lucide:' + iconName.replace(/([a-z0-9])([A-Z])/g, '$1-$2').toLowerCase()
  return iconMap[kebabName] || Wrench
}

// Props - 只接收 toolCode，其他信息从 store 获取
const props = defineProps<{
  toolCode: string
}>()

// Store
const userStore = useUserStore()
const toolStore = useToolStore()

// SSR：确保 store 已初始化（服务端渲染时获取数据）
await useAsyncData('tool-layout-init', async () => {
  if (!toolStore.initialized || toolStore.tools.length === 0) {
    await toolStore.initialize()
  }
  return true
}, {
  server: true
})

// 从 store 获取工具信息
const tool = computed(() => toolStore.getToolByCode(props.toolCode))

// 从 store 获取分类信息
const category = computed(() => {
  if (!tool.value) return null
  return toolStore.categories.find(cat => cat.code === tool.value?.category)
})

// 工具ID
const toolId = computed(() => tool.value?.id)

// 相关工具（同分类的其他工具，排除当前工具）
const relatedTools = computed(() => {
  if (!tool.value) return []
  return toolStore.tools
    .filter(t => t.category === tool.value?.category && t.code !== props.toolCode)
    .slice(0, 5)
})

// 收藏
const isFavorite = ref(false)
const favoriteLoading = ref(false)

// 获取收藏状态
const fetchFavoriteStatus = async () => {
  if (!toolId.value) return

  try {
    const { $api } = useNuxtApp()
    const response = await $api(`/favorites/check/${toolId.value}`) as { code: number; data: { isFavorite: boolean } }

    if (response.code === 200) {
      isFavorite.value = response.data.isFavorite
    }
  } catch (error) {
    console.error('获取收藏状态失败:', error)
  }
}

// 切换收藏状态
const toggleFavorite = async () => {
  if (!userStore.isLoggedIn) {
    userStore.setLoginModalVisible(true)
    return
  }

  if (!toolId.value || favoriteLoading.value) return

  favoriteLoading.value = true
  try {
    const { $api } = useNuxtApp()
    const response = await $api(`/favorites/toggle/${toolId.value}`, {
      method: 'POST'
    }) as { code: number; data: { isFavorite: boolean; favoriteCount: number } }

    if (response.code === 200) {
      isFavorite.value = response.data.isFavorite
      // 更新工具的收藏数
      if (tool.value) {
        tool.value.favoriteCount = response.data.favoriteCount
      }
    } else if (response.code === 401) {
      userStore.setLoginModalVisible(true)
    }
  } catch (error: any) {
    if (error.response?.status === 401) {
      userStore.setLoginModalVisible(true)
    } else {
      console.error('切换收藏状态失败:', error)
    }
  } finally {
    favoriteLoading.value = false
  }
}

// 页面加载时获取收藏状态
onMounted(() => {
  if (userStore.isLoggedIn && toolId.value) {
    fetchFavoriteStatus()
  }
})

// 分享
const shareTool = async () => {
  const url = window.location.href
  const title = tool.value?.name || '秋云工具'

  if (navigator.share) {
    try {
      await navigator.share({ title, url })
    } catch (err) {
      // 用户取消
    }
  } else {
    try {
      await navigator.clipboard.writeText(url)
      // 显示成功提示
    } catch (err) {
      // 显示失败提示
    }
  }
}

// 格式化数字
const formatNumber = (num: number): string => {
  if (num >= 10000) {
    return (num / 10000).toFixed(1) + 'w'
  }
  if (num >= 1000) {
    return (num / 1000).toFixed(1) + 'k'
  }
  return num.toString()
}

// 反馈
const showFeedback = ref(false)
const feedbackType = ref('')
const feedbackContent = ref('')

const submitFeedback = async () => {
  // TODO: 调用API提交反馈
  showFeedback.value = false
  feedbackType.value = ''
  feedbackContent.value = ''
}
</script>
