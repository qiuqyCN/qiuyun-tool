<script setup lang="ts">
import { ref } from 'vue'
import { AlertCircle, CheckCircle, Copy, Download, Check, Loader2 } from 'lucide-vue-next'
import { useToolExecutor } from '~/composables/useToolExecutor'
import { ToolType } from '~/types/tool'

// JSON操作类型枚举
enum JsonOperation {
  FORMAT = 'format',
  COMPRESS = 'compress',
  ESCAPE = 'escape',
  UNESCAPE = 'unescape'
}

// 请求参数类型
interface JsonFormatParams {
  operation: string
  input: string
}

// 响应结果类型
interface JsonFormatResult {
  success: boolean
  result: string
  operation: string
  errorMessage?: string
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

// 状态
const activeTab = ref<JsonOperation>(JsonOperation.FORMAT)
const inputJson = ref('')
const outputJson = ref('')
const error = ref('')

// 使用工具执行器
const { execute, isLoading } = useToolExecutor<JsonFormatParams, JsonFormatResult>({
  toolCode: 'json-formatter',
  toolType: ToolType.INSTANT,
  onSuccess: (result) => {
    if (result.success) {
      outputJson.value = result.result
      error.value = ''
    } else {
      error.value = result.errorMessage || '处理失败'
      outputJson.value = ''
    }
  },
  onError: (err) => {
    error.value = err
    outputJson.value = ''
  }
})

// 执行 JSON 处理
const processJson = async (operation: JsonOperation) => {
  if (!inputJson.value.trim()) {
    error.value = '请输入 JSON 数据'
    return
  }

  await execute({
    operation,
    input: inputJson.value
  })
}

// 格式化 JSON
const formatJson = () => processJson(JsonOperation.FORMAT)

// 压缩 JSON
const compressJson = () => processJson(JsonOperation.COMPRESS)

// 转义 JSON
const escapeJson = () => processJson(JsonOperation.ESCAPE)

// 去转义 JSON
const unescapeJson = () => processJson(JsonOperation.UNESCAPE)

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
  <NuxtLayout name="tool" tool-code="json-formatter">
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
              <Button @click="formatJson" :disabled="isLoading">
                <Loader2 v-if="isLoading" class="w-4 h-4 mr-2 animate-spin" />
                <CheckCircle v-else class="w-4 h-4 mr-2" />
                {{ isLoading ? '处理中...' : '格式化' }}
              </Button>
              <Button variant="outline" @click="clearInput" :disabled="isLoading">清空</Button>
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
              <Button @click="compressJson" :disabled="isLoading">
                <Loader2 v-if="isLoading" class="w-4 h-4 mr-2 animate-spin" />
                <CheckCircle v-else class="w-4 h-4 mr-2" />
                {{ isLoading ? '处理中...' : '压缩' }}
              </Button>
              <Button variant="outline" @click="clearInput" :disabled="isLoading">清空</Button>
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
              <Button @click="escapeJson" :disabled="isLoading">
                <Loader2 v-if="isLoading" class="w-4 h-4 mr-2 animate-spin" />
                <CheckCircle v-else class="w-4 h-4 mr-2" />
                {{ isLoading ? '处理中...' : '转义' }}
              </Button>
              <Button variant="outline" @click="clearInput" :disabled="isLoading">清空</Button>
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
              <Button @click="unescapeJson" :disabled="isLoading">
                <Loader2 v-if="isLoading" class="w-4 h-4 mr-2 animate-spin" />
                <CheckCircle v-else class="w-4 h-4 mr-2" />
                {{ isLoading ? '处理中...' : '去转义' }}
              </Button>
              <Button variant="outline" @click="clearInput" :disabled="isLoading">清空</Button>
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
