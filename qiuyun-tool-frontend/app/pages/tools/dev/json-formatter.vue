<script setup lang="ts">
import { ref } from 'vue'
import { AlertCircle, CheckCircle, Copy, Download, Check, Loader2, Braces } from 'lucide-vue-next'
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

// 操作标签
const operationLabels: Record<JsonOperation, string> = {
  [JsonOperation.FORMAT]: '格式化',
  [JsonOperation.COMPRESS]: '压缩',
  [JsonOperation.ESCAPE]: '转义',
  [JsonOperation.UNESCAPE]: '去转义'
}

// 占位符
const placeholders: Record<JsonOperation, string> = {
  [JsonOperation.FORMAT]: '{\n  "name": "张三",\n  "age": 25,\n  "email": "zhangsan@example.com"\n}',
  [JsonOperation.COMPRESS]: '{"name":"张三","age":25,"email":"zhangsan@example.com"}',
  [JsonOperation.ESCAPE]: '{"name":"张三","age":25}',
  [JsonOperation.UNESCAPE]: '"{\\"name\\":\\"张三\\",\\"age\\":25}"'
}

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
      <div class="border-b bg-muted/30">
        <div class="flex">
          <button
            v-for="(label, op) in operationLabels"
            :key="op"
            @click="activeTab = op as JsonOperation"
            class="flex items-center gap-2 px-6 py-3 border-b-2 transition-colors"
            :class="activeTab === op
              ? 'border-primary bg-background text-primary'
              : 'border-transparent text-muted-foreground hover:text-foreground'"
          >
            <Braces v-if="op === JsonOperation.FORMAT" class="w-4 h-4" />
            <span v-else-if="op === JsonOperation.COMPRESS" class="w-4 h-4 text-xs font-bold">{}</span>
            <span v-else-if="op === JsonOperation.ESCAPE" class="w-4 h-4 text-xs">\\</span>
            <span v-else class="w-4 h-4 text-xs">/</span>
            {{ label }}
          </button>
        </div>
      </div>

      <!-- 编辑区域 -->
      <div class="p-6">
        <div class="grid grid-cols-1 lg:grid-cols-2 gap-6">
          <!-- 输入区域 -->
          <div class="flex flex-col gap-3">
            <div class="flex items-center justify-between">
              <div class="flex items-center gap-2">
                <div class="w-1 h-4 bg-primary rounded-full"></div>
                <label class="text-sm font-medium">输入 JSON</label>
              </div>
              <div class="flex items-center gap-2">
                <Button @click="handleProcess" :disabled="isLoading" size="sm" class="rounded-full px-4">
                  <Loader2 v-if="isLoading" class="w-3 h-3 mr-1 animate-spin" />
                  <CheckCircle v-else class="w-3 h-3 mr-1" />
                  {{ isLoading ? '处理中...' : '执行' }}
                </Button>
                <button
                  @click="clearInput"
                  class="text-xs text-muted-foreground hover:text-foreground px-2 py-1 rounded hover:bg-muted transition-colors"
                >
                  清空
                </button>
              </div>
            </div>
            <div class="relative">
              <Textarea
                v-model="inputJson"
                :placeholder="placeholders[activeTab]"
                class="font-mono text-sm resize-none min-h-[400px] max-h-[600px] border-border/60 focus:border-primary"
              />
            </div>
          </div>

          <!-- 输出区域 -->
          <div class="flex flex-col gap-3">
            <div class="flex items-center justify-between">
              <div class="flex items-center gap-2">
                <div class="w-1 h-4 bg-green-500 rounded-full"></div>
                <label class="text-sm font-medium">{{ operationLabels[activeTab] }}结果</label>
              </div>
              <div class="flex gap-1">
                <button
                  v-if="outputJson"
                  @click="copyOutput"
                  class="text-xs text-muted-foreground hover:text-foreground px-2 py-1 rounded hover:bg-muted transition-colors flex items-center gap-1"
                >
                  <Copy class="w-3 h-3" />
                  复制
                </button>
                <button
                  v-if="outputJson"
                  @click="downloadOutput"
                  class="text-xs text-muted-foreground hover:text-foreground px-2 py-1 rounded hover:bg-muted transition-colors flex items-center gap-1"
                >
                  <Download class="w-3 h-3" />
                  下载
                </button>
              </div>
            </div>
            <Textarea
              v-model="outputJson"
              readonly
              class="font-mono text-sm resize-none bg-muted/20 min-h-[400px] max-h-[600px] border-border/60"
              placeholder="处理结果将显示在这里..."
            />
          </div>
        </div>

        <!-- 错误提示 -->
        <div v-if="error" class="mt-4 flex items-center gap-2 text-sm text-red-500 bg-red-50/80 p-3 rounded-lg border border-red-200">
          <AlertCircle class="w-4 h-4 shrink-0" />
          {{ error }}
        </div>
      </div>
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
