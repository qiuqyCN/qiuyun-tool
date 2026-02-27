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
  CheckCircle,
  Copy,
  Download,
  Braces,
  AlertCircle
} from 'lucide-vue-next'
import { getToolById } from '@/composables/useTools'

const tool = getToolById('json-formatter')

// 记录最近访问
onMounted(() => {
  if (import.meta.client) {
    const recentIds = JSON.parse(localStorage.getItem('recentTools') || '[]') as string[]
    const filteredIds = recentIds.filter(id => id !== 'json-formatter')
    filteredIds.unshift('json-formatter')
    const newRecentIds = filteredIds.slice(0, 20)
    localStorage.setItem('recentTools', JSON.stringify(newRecentIds))
  }
})

const isFavorite = ref(false)
const activeTab = ref('format')
const inputJson = ref('')
const outputJson = ref('')
const error = ref('')

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

const formatJson = () => {
  if (!inputJson.value.trim()) {
    error.value = '请输入 JSON 数据'
    return
  }
  
  try {
    const parsed = JSON.parse(inputJson.value)
    outputJson.value = JSON.stringify(parsed, null, 2)
    error.value = ''
  } catch (e) {
    error.value = 'JSON 格式错误：' + (e as Error).message
    outputJson.value = ''
  }
}

const compressJson = () => {
  if (!inputJson.value.trim()) {
    error.value = '请输入 JSON 数据'
    return
  }
  
  try {
    const parsed = JSON.parse(inputJson.value)
    outputJson.value = JSON.stringify(parsed)
    error.value = ''
  } catch (e) {
    error.value = 'JSON 格式错误：' + (e as Error).message
    outputJson.value = ''
  }
}

const escapeJson = () => {
  if (!inputJson.value.trim()) {
    error.value = '请输入 JSON 数据'
    return
  }
  
  try {
    const parsed = JSON.parse(inputJson.value)
    const stringified = JSON.stringify(parsed)
    outputJson.value = JSON.stringify(stringified)
    error.value = ''
  } catch (e) {
    error.value = 'JSON 格式错误：' + (e as Error).message
    outputJson.value = ''
  }
}

const unescapeJson = () => {
  if (!inputJson.value.trim()) {
    error.value = '请输入 JSON 数据'
    return
  }
  
  try {
    const unescaped = JSON.parse(inputJson.value)
    if (typeof unescaped === 'string') {
      outputJson.value = JSON.stringify(JSON.parse(unescaped), null, 2)
      error.value = ''
    } else {
      error.value = '输入不是有效的转义 JSON 字符串'
    }
  } catch (e) {
    error.value = '格式错误：' + (e as Error).message
    outputJson.value = ''
  }
}

const clearInput = () => {
  inputJson.value = ''
  outputJson.value = ''
  error.value = ''
}

const copyOutput = () => {
  if (outputJson.value) {
    navigator.clipboard.writeText(outputJson.value)
  }
}

const downloadOutput = () => {
  if (outputJson.value) {
    const blob = new Blob([outputJson.value], { type: 'application/json' })
    const url = URL.createObjectURL(blob)
    const a = document.createElement('a')
    a.href = url
    a.download = 'formatted.json'
    a.click()
    URL.revokeObjectURL(url)
  }
}

const handleProcess = () => {
  switch (activeTab.value) {
    case 'format':
      formatJson()
      break
    case 'compress':
      compressJson()
      break
    case 'escape':
      escapeJson()
      break
    case 'unescape':
      unescapeJson()
      break
  }
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
          <span class="text-foreground">{{ tool?.name }}</span>
        </div>
      </div>
    </div>

    <div class="container mx-auto px-4 py-8">
      <!-- Tool Header -->
      <div class="flex flex-col lg:flex-row lg:items-start gap-6 mb-8">
        <div class="w-20 h-20 rounded-2xl bg-linear-to-br from-primary/20 to-primary/10 flex items-center justify-center shrink-0">
          <Braces class="w-10 h-10 text-primary" />
        </div>
        
        <div class="flex-1">
          <div class="flex flex-wrap items-center gap-3 mb-2">
            <h1 class="text-3xl font-bold text-foreground">{{ tool?.name }}</h1>
            <Badge v-if="tool?.tags?.includes('热门')" variant="destructive">HOT</Badge>
            <Badge v-if="tool?.isVip" variant="secondary">
              <Crown class="w-3 h-3 mr-1" />
              VIP
            </Badge>
          </div>
          
          <p class="text-muted-foreground mb-4">{{ tool?.description }}</p>
          
          <div class="flex flex-wrap items-center gap-4 text-sm text-muted-foreground">
            <div class="flex items-center gap-1">
              <TrendingUp class="w-4 h-4" />
              <span>{{ formatVisits(tool?.visits || 0) }} 次使用</span>
            </div>
            <div class="flex items-center gap-1">
              <Star class="w-4 h-4 text-yellow-500" />
              <span>{{ tool?.rating }} 分</span>
            </div>
            <div class="flex items-center gap-1">
              <MessageCircle class="w-4 h-4" />
              <span>{{ tool?.reviewCount }} 条评价</span>
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
                <TabsTrigger value="unescape" class="rounded-none border-b-2 border-transparent data-[state=active]:border-primary data-[state=active]:bg-background px-6 py-3">
                  去转义
                </TabsTrigger>
              </TabsList>
              
              <TabsContent value="format" class="m-0 p-6">
                <div class="space-y-4">
                  <div>
                    <label class="text-sm font-medium mb-2 block">输入 JSON</label>
                    <Textarea 
                      v-model="inputJson"
                      placeholder="请输入要格式化的 JSON 数据...&#10;例如：{&quot;name&quot;:&quot;张三&quot;,&quot;age&quot;:25}"
                      rows="8"
                      class="font-mono text-sm"
                    />
                  </div>
                  <div class="flex items-center gap-2">
                    <Button @click="formatJson">
                      <CheckCircle class="w-4 h-4 mr-2" />
                      格式化
                    </Button>
                    <Button variant="outline" @click="clearInput">清空</Button>
                  </div>
                  <div v-if="error" class="flex items-center gap-2 text-sm text-red-500">
                    <AlertCircle class="w-4 h-4" />
                    {{ error }}
                  </div>
                  <div v-if="outputJson">
                    <div class="flex items-center justify-between mb-2">
                      <label class="text-sm font-medium">格式化结果</label>
                      <div class="flex items-center gap-2">
                        <Button variant="ghost" size="sm" @click="copyOutput">
                          <Copy class="w-4 h-4 mr-1" />
                          复制
                        </Button>
                        <Button variant="ghost" size="sm" @click="downloadOutput">
                          <Download class="w-4 h-4 mr-1" />
                          下载
                        </Button>
                      </div>
                    </div>
                    <Textarea 
                      v-model="outputJson"
                      rows="8"
                      class="font-mono text-sm bg-muted/30"
                      readonly
                    />
                  </div>
                </div>
              </TabsContent>
              
              <TabsContent value="compress" class="m-0 p-6">
                <div class="space-y-4">
                  <div>
                    <label class="text-sm font-medium mb-2 block">输入 JSON</label>
                    <Textarea 
                      v-model="inputJson"
                      placeholder="请输入要压缩的 JSON 数据..."
                      rows="8"
                      class="font-mono text-sm"
                    />
                  </div>
                  <div class="flex items-center gap-2">
                    <Button @click="compressJson">
                      <CheckCircle class="w-4 h-4 mr-2" />
                      压缩
                    </Button>
                    <Button variant="outline" @click="clearInput">清空</Button>
                  </div>
                  <div v-if="error" class="flex items-center gap-2 text-sm text-red-500">
                    <AlertCircle class="w-4 h-4" />
                    {{ error }}
                  </div>
                  <div v-if="outputJson">
                    <div class="flex items-center justify-between mb-2">
                      <label class="text-sm font-medium">压缩结果</label>
                      <div class="flex items-center gap-2">
                        <Button variant="ghost" size="sm" @click="copyOutput">
                          <Copy class="w-4 h-4 mr-1" />
                          复制
                        </Button>
                        <Button variant="ghost" size="sm" @click="downloadOutput">
                          <Download class="w-4 h-4 mr-1" />
                          下载
                        </Button>
                      </div>
                    </div>
                    <Textarea 
                      v-model="outputJson"
                      rows="8"
                      class="font-mono text-sm bg-muted/30"
                      readonly
                    />
                  </div>
                </div>
              </TabsContent>
              
              <TabsContent value="escape" class="m-0 p-6">
                <div class="space-y-4">
                  <div>
                    <label class="text-sm font-medium mb-2 block">输入 JSON</label>
                    <Textarea 
                      v-model="inputJson"
                      placeholder="请输入要转义的 JSON 数据..."
                      rows="8"
                      class="font-mono text-sm"
                    />
                  </div>
                  <div class="flex items-center gap-2">
                    <Button @click="escapeJson">
                      <CheckCircle class="w-4 h-4 mr-2" />
                      转义
                    </Button>
                    <Button variant="outline" @click="clearInput">清空</Button>
                  </div>
                  <div v-if="error" class="flex items-center gap-2 text-sm text-red-500">
                    <AlertCircle class="w-4 h-4" />
                    {{ error }}
                  </div>
                  <div v-if="outputJson">
                    <div class="flex items-center justify-between mb-2">
                      <label class="text-sm font-medium">转义结果</label>
                      <div class="flex items-center gap-2">
                        <Button variant="ghost" size="sm" @click="copyOutput">
                          <Copy class="w-4 h-4 mr-1" />
                          复制
                        </Button>
                        <Button variant="ghost" size="sm" @click="downloadOutput">
                          <Download class="w-4 h-4 mr-1" />
                          下载
                        </Button>
                      </div>
                    </div>
                    <Textarea 
                      v-model="outputJson"
                      rows="8"
                      class="font-mono text-sm bg-muted/30"
                      readonly
                    />
                  </div>
                </div>
              </TabsContent>

              <TabsContent value="unescape" class="m-0 p-6">
                <div class="space-y-4">
                  <div>
                    <label class="text-sm font-medium mb-2 block">输入转义后的 JSON 字符串</label>
                    <Textarea 
                      v-model="inputJson"
                      placeholder="请输入要去除转义的 JSON 字符串..."
                      rows="8"
                      class="font-mono text-sm"
                    />
                  </div>
                  <div class="flex items-center gap-2">
                    <Button @click="unescapeJson">
                      <CheckCircle class="w-4 h-4 mr-2" />
                      去转义
                    </Button>
                    <Button variant="outline" @click="clearInput">清空</Button>
                  </div>
                  <div v-if="error" class="flex items-center gap-2 text-sm text-red-500">
                    <AlertCircle class="w-4 h-4" />
                    {{ error }}
                  </div>
                  <div v-if="outputJson">
                    <div class="flex items-center justify-between mb-2">
                      <label class="text-sm font-medium">去转义结果</label>
                      <div class="flex items-center gap-2">
                        <Button variant="ghost" size="sm" @click="copyOutput">
                          <Copy class="w-4 h-4 mr-1" />
                          复制
                        </Button>
                        <Button variant="ghost" size="sm" @click="downloadOutput">
                          <Download class="w-4 h-4 mr-1" />
                          下载
                        </Button>
                      </div>
                    </div>
                    <Textarea 
                      v-model="outputJson"
                      rows="8"
                      class="font-mono text-sm bg-muted/30"
                      readonly
                    />
                  </div>
                </div>
              </TabsContent>
            </Tabs>
          </div>

          <!-- Instructions -->
          <div class="mt-8">
            <h2 class="text-lg font-semibold mb-4">使用说明</h2>
            <div class="prose prose-sm max-w-none text-muted-foreground">
              <ul class="list-disc list-inside space-y-2">
                <li><strong>格式化</strong>：将压缩的 JSON 数据转换为易读的格式，自动添加缩进和换行</li>
                <li><strong>压缩</strong>：去除 JSON 中的空白字符，减小数据体积</li>
                <li><strong>转义</strong>：将 JSON 字符串转义，适用于在代码中使用</li>
                <li><strong>去转义</strong>：将转义后的 JSON 字符串还原为正常格式</li>
                <li>支持复制结果到剪贴板或下载为 .json 文件</li>
              </ul>
            </div>
          </div>
        </div>

        <!-- Sidebar -->
        <div class="space-y-6">
          <!-- Related Tools -->
          <div class="border border-border/40 rounded-xl p-6">
            <h3 class="font-semibold mb-4">相关工具</h3>
            <div class="space-y-3">
              <NuxtLink 
                v-for="relatedTool in [
                  { id: 'json-to-yaml', name: 'JSON转YAML', icon: 'FileJson' },
                  { id: 'code-beautify', name: '代码美化', icon: 'Code2' },
                  { id: 'regex-tester', name: '正则测试', icon: 'Search' },
                ]" 
                :key="relatedTool.id"
                :to="`/tool/${relatedTool.id}`"
                class="flex items-center gap-3 p-3 rounded-lg hover:bg-muted transition-colors"
              >
                <div class="w-10 h-10 rounded-lg bg-primary/10 flex items-center justify-center">
                  <component :is="relatedTool.icon" class="w-5 h-5 text-primary" />
                </div>
                <span class="text-sm font-medium">{{ relatedTool.name }}</span>
              </NuxtLink>
            </div>
          </div>

          <!-- Tips -->
          <div class="border border-border/40 rounded-xl p-6 bg-muted/30">
            <h3 class="font-semibold mb-4">小贴士</h3>
            <ul class="text-sm text-muted-foreground space-y-2">
              <li>• JSON 格式要求键名必须用双引号包裹</li>
              <li>• 字符串值也必须用双引号包裹</li>
              <li>• 最后一个属性后面不能加逗号</li>
              <li>• 支持嵌套对象和数组</li>
            </ul>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>
