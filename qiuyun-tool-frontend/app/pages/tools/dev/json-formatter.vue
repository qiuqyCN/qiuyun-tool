<script setup lang="ts">
import { ref } from 'vue'
import { AlertCircle, CheckCircle, Copy, Download, Check } from 'lucide-vue-next'

// JSON操作类型枚举
enum JsonOperation {
  FORMAT = 'format',
  COMPRESS = 'compress',
  ESCAPE = 'escape',
  UNESCAPE = 'unescape'
}

// Toast 提示状态
const toast = ref({
  show: false,
  message: ''
})

// 显示提示
const showToast = (message: string) => {
  toast.value.message = message
  toast.value.show = true
  setTimeout(() => {
    toast.value.show = false
  }, 2000)
}

// 工具信息
const toolInfo = {
  code: 'json-formatter',
  name: 'JSON格式化',
  description: 'JSON数据的格式化、压缩、转义等操作',
  icon: 'lucide:braces',
  iconColor: '#4F46E5',
  iconBgColor: '#EEF2FF',
  priceMode: 'free' as const,
  isHot: true,
  viewCount: 125432,
  usageCount: 98765,
  rating: 4.8,
  favoriteCount: 1234,
  reviewCount: 328,
  instructions: `<ol class="list-decimal list-inside space-y-2">
    <li><strong>格式化</strong>：将压缩的 JSON 数据转换为易读的格式，自动添加缩进和换行</li>
    <li><strong>压缩</strong>：去除 JSON 中的空白字符，减小数据体积</li>
    <li><strong>转义</strong>：将 JSON 字符串转义，适用于在代码中使用</li>
    <li><strong>去转义</strong>：将转义后的 JSON 字符串还原为正常格式</li>
    <li>支持复制结果到剪贴板或下载为 .json 文件</li>
  </ol>`
}

// 分类信息
const categoryInfo = {
  code: 'dev',
  name: '开发工具'
}

// 相关工具
const relatedTools = [
  {
    code: 'json-to-yaml',
    name: 'JSON转YAML',
    description: '将JSON数据转换为YAML格式',
    icon: 'lucide:file-json',
    iconColor: '#7C3AED',
    iconBgColor: '#F3E8FF',
    categoryCode: 'dev'
  },
  {
    code: 'code-beautify',
    name: '代码美化',
    description: 'HTML/CSS/JS代码格式化',
    icon: 'lucide:code-2',
    iconColor: '#059669',
    iconBgColor: '#D1FAE5',
    categoryCode: 'dev'
  },
  {
    code: 'regex-tester',
    name: '正则测试',
    description: '在线正则表达式测试工具',
    icon: 'lucide:search',
    iconColor: '#DC2626',
    iconBgColor: '#FEE2E2',
    categoryCode: 'dev'
  }
]

// 状态
const activeTab = ref<JsonOperation>(JsonOperation.FORMAT)
const inputJson = ref('')
const outputJson = ref('')
const error = ref('')

// 格式化 JSON
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

// 压缩 JSON
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

// 转义 JSON
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

// 去转义 JSON
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

// 清空
const clearInput = () => {
  inputJson.value = ''
  outputJson.value = ''
  error.value = ''
}

// 复制结果
const copyOutput = async () => {
  if (outputJson.value) {
    try {
      await navigator.clipboard.writeText(outputJson.value)
      showToast('复制成功！')
    } catch (err) {
      showToast('复制失败，请手动复制')
    }
  }
}

// 下载结果
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

// 处理
const handleProcess = () => {
  switch (activeTab.value) {
    case JsonOperation.FORMAT:
      formatJson()
      break
    case JsonOperation.COMPRESS:
      compressJson()
      break
    case JsonOperation.ESCAPE:
      escapeJson()
      break
    case JsonOperation.UNESCAPE:
      unescapeJson()
      break
  }
}
</script>

<template>
  <NuxtLayout name="tool" :tool="toolInfo" :category="categoryInfo" :related-tools="relatedTools">
    <!-- 工具执行区域 -->
    <div class="border border-border/40 rounded-xl overflow-hidden">
      <!-- 工具 Tabs -->
      <Tabs v-model="activeTab" class="w-full">
        <TabsList class="w-full justify-start rounded-none border-b bg-muted/30 p-0">
          <TabsTrigger
            :value="JsonOperation.FORMAT"
            class="rounded-none border-b-2 border-transparent data-[state=active]:border-primary data-[state=active]:bg-background px-6 py-3"
          >
            格式化
          </TabsTrigger>
          <TabsTrigger
            :value="JsonOperation.COMPRESS"
            class="rounded-none border-b-2 border-transparent data-[state=active]:border-primary data-[state=active]:bg-background px-6 py-3"
          >
            压缩
          </TabsTrigger>
          <TabsTrigger
            :value="JsonOperation.ESCAPE"
            class="rounded-none border-b-2 border-transparent data-[state=active]:border-primary data-[state=active]:bg-background px-6 py-3"
          >
            转义
          </TabsTrigger>
          <TabsTrigger
            :value="JsonOperation.UNESCAPE"
            class="rounded-none border-b-2 border-transparent data-[state=active]:border-primary data-[state=active]:bg-background px-6 py-3"
          >
            去转义
          </TabsTrigger>
        </TabsList>

        <!-- 格式化 -->
        <TabsContent :value="JsonOperation.FORMAT" class="m-0 p-6">
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

        <!-- 压缩 -->
        <TabsContent :value="JsonOperation.COMPRESS" class="m-0 p-6">
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

        <!-- 转义 -->
        <TabsContent :value="JsonOperation.ESCAPE" class="m-0 p-6">
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

        <!-- 去转义 -->
        <TabsContent :value="JsonOperation.UNESCAPE" class="m-0 p-6">
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

    <!-- Toast 提示 -->
    <Transition
      enter-active-class="transition duration-300 ease-out"
      enter-from-class="transform translate-y-2 opacity-0"
      enter-to-class="transform translate-y-0 opacity-100"
      leave-active-class="transition duration-200 ease-in"
      leave-from-class="transform translate-y-0 opacity-100"
      leave-to-class="transform translate-y-2 opacity-0"
    >
      <div
        v-if="toast.show"
        class="fixed bottom-8 left-1/2 -translate-x-1/2 z-50 flex items-center gap-2 px-4 py-2.5 bg-foreground text-background rounded-lg shadow-lg"
      >
        <Check class="w-4 h-4 text-green-400" />
        <span class="text-sm font-medium">{{ toast.message }}</span>
      </div>
    </Transition>
  </NuxtLayout>
</template>
