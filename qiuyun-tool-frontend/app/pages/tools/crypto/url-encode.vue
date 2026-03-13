<script setup lang="ts">
import { ref, computed, watch } from 'vue'
import { Link, Copy, Check, AlertCircle, Trash2, FileCode, FileText } from 'lucide-vue-next'
import { useToolExecutor } from '~/composables/useToolExecutor'
import { useToast } from '~/composables/useToast'
import { useClipboard } from '~/composables/useClipboard'
import { ToolType } from '~/types/tool'
import { CodecOperation } from '~/types/tool-common'
import { CHARSET_OPTIONS } from '~/constants/tool'

// 请求参数
interface UrlCodecParams {
  operation: string
  input: string
  charset: string
  encodeSpaceAsPlus: boolean
}

// 单个结果
interface UrlCodecResult {
  input: string
  output: string
}

// 响应结果
interface UrlCodecResponse {
  success: boolean
  operation: string
  charset: string
  results: UrlCodecResult[]
  count: number
  errorMessage?: string
}

// 状态
const activeTab = ref<CodecOperation>(CodecOperation.ENCODE)
const inputText = ref('')
const charset = ref('UTF-8')
const encodeSpaceAsPlus = ref(true)
const results = ref<UrlCodecResult[]>([])
const error = ref('')

// 使用通用 composables
const { toast, showSuccess } = useToast()
const { copy, copyMultiple } = useClipboard(showSuccess)

// 使用工具执行器
const { execute, isLoading } = useToolExecutor<UrlCodecParams, UrlCodecResponse>({
  toolCode: 'url-encode',
  toolType: ToolType.INSTANT,
  onSuccess: (res) => {
    if (res.success) {
      results.value = res.results
      error.value = ''
    } else {
      error.value = res.errorMessage || '操作失败'
      results.value = []
    }
  },
  onError: (err) => {
    error.value = err
    results.value = []
  }
})

// 执行编解码
const processUrl = async () => {
  if (!inputText.value.trim()) {
    error.value = '请输入内容'
    return
  }

  await execute({
    operation: activeTab.value,
    input: inputText.value,
    charset: charset.value,
    encodeSpaceAsPlus: encodeSpaceAsPlus.value
  })
}

// 切换操作
const switchOperation = (op: CodecOperation) => {
  activeTab.value = op
  // 如果有结果，交换输入输出
  if (results.value.length > 0 && results.value[0].output) {
    inputText.value = results.value[0].output
    processUrl()
  } else {
    results.value = []
    error.value = ''
  }
}

// 复制所有结果
const copyAllResults = async () => {
  const texts = results.value.map(r => r.output)
  await copyMultiple(texts, '已复制所有结果')
}

// 清空
const clearAll = () => {
  inputText.value = ''
  results.value = []
  error.value = ''
}

// 输入框占位符
const inputPlaceholder = computed(() => {
  return activeTab.value === CodecOperation.ENCODE
    ? '输入要编码的文本或URL...\n例如: Hello World 或 https://example.com?key=中文'
    : '输入要解码的URL编码字符串...\n例如: Hello%20World'
})

// 监听输入变化，自动处理
watch([inputText, charset, encodeSpaceAsPlus], () => {
  if (inputText.value) {
    processUrl()
  }
}, { deep: true })
</script>

<template>
  <NuxtLayout name="tool" tool-code="url-encode">
    <div class="border border-border/40 rounded-xl overflow-hidden">
      <!-- 操作切换 Tabs -->
      <div class="border-b bg-muted/20">
        <div class="flex">
          <button
            @click="switchOperation(CodecOperation.ENCODE)"
            class="flex items-center gap-2 px-6 py-3 border-b-2 transition-colors"
            :class="activeTab === CodecOperation.ENCODE
              ? 'border-primary bg-background text-primary'
              : 'border-transparent text-muted-foreground hover:text-foreground'"
          >
            <FileText class="w-4 h-4" />
            编码 (Encode)
          </button>
          <button
            @click="switchOperation(CodecOperation.DECODE)"
            class="flex items-center gap-2 px-6 py-3 border-b-2 transition-colors"
            :class="activeTab === CodecOperation.DECODE
              ? 'border-primary bg-background text-primary'
              : 'border-transparent text-muted-foreground hover:text-foreground'"
          >
            <FileCode class="w-4 h-4" />
            解码 (Decode)
          </button>
        </div>
      </div>

      <div class="p-6 space-y-4">
        <!-- 配置选项 -->
        <div class="flex flex-wrap items-center gap-4 p-3 bg-muted/30 rounded-lg">
          <!-- 字符集选择 -->
          <div class="flex items-center gap-2">
            <span class="text-sm text-muted-foreground">字符集:</span>
            <select
              v-model="charset"
              class="px-3 py-1.5 text-sm border rounded bg-background"
            >
              <option v-for="opt in CHARSET_OPTIONS" :key="opt.value" :value="opt.value">
                {{ opt.label }}
              </option>
            </select>
          </div>

          <!-- 空格编码选项 -->
          <label
            v-if="activeTab === CodecOperation.ENCODE"
            class="flex items-center gap-2 text-sm cursor-pointer"
          >
            <input
              v-model="encodeSpaceAsPlus"
              type="checkbox"
              class="rounded border-gray-300"
            />
            <span>空格编码为 <code class="bg-muted px-1 rounded">+</code> (否则为 <code class="bg-muted px-1 rounded">%20</code>)</span>
          </label>

          <div class="ml-auto">
            <button
              @click="clearAll"
              class="text-xs text-muted-foreground hover:text-foreground px-2 py-1 rounded hover:bg-muted transition-colors flex items-center gap-1"
            >
              <Trash2 class="w-3 h-3" />
              清空
            </button>
          </div>
        </div>

        <!-- 输入区域 -->
        <div>
          <div class="flex items-center gap-2 mb-2">
            <div class="w-1 h-4 bg-primary rounded-full"></div>
            <label class="text-sm font-medium">
              {{ activeTab === CodecOperation.ENCODE ? '输入文本' : '输入编码字符串' }}
            </label>
            <span class="text-xs text-muted-foreground">(支持多行批量处理)</span>
          </div>
          <Textarea
            v-model="inputText"
            :placeholder="inputPlaceholder"
            rows="6"
            class="font-mono text-sm resize-none"
          />
        </div>

        <!-- 错误提示 -->
        <div
          v-if="error"
          class="flex items-center gap-2 text-sm text-red-500 bg-red-50/80 p-3 rounded-lg border border-red-200"
        >
          <AlertCircle class="w-4 h-4 shrink-0" />
          {{ error }}
        </div>

        <!-- 处理结果 -->
        <div v-if="results.length > 0" class="space-y-3">
          <div class="flex items-center justify-between">
            <div class="flex items-center gap-2">
              <div class="w-1 h-4 bg-green-500 rounded-full"></div>
              <label class="text-sm font-medium">
                {{ activeTab === CodecOperation.ENCODE ? 'URL 编码结果' : '解码结果' }}
              </label>
              <span class="text-xs text-muted-foreground">({{ results.length }} 个)</span>
            </div>
            <button
              v-if="results.length > 1"
              @click="copyAllResults"
              class="text-xs text-primary hover:underline flex items-center gap-1"
            >
              <Copy class="w-3 h-3" />
              复制全部
            </button>
          </div>

          <div class="space-y-2">
            <div
              v-for="(result, index) in results"
              :key="index"
              class="p-3 bg-muted/20 rounded-lg border"
            >
              <div class="flex items-center gap-2 mb-2">
                <span class="text-xs text-muted-foreground">原文:</span>
                <code class="text-sm bg-white px-2 py-0.5 rounded border flex-1 truncate">{{ result.input }}</code>
              </div>
              <div class="flex items-center gap-2">
                <span class="text-xs text-muted-foreground">
                  {{ activeTab === CodecOperation.ENCODE ? '编码:' : '解码:' }}
                </span>
                <code class="text-sm font-mono bg-primary/5 text-primary px-2 py-0.5 rounded flex-1 break-all">{{ result.output }}</code>
                <button
                  @click="copy(result.output)"
                  class="p-1.5 text-muted-foreground hover:text-foreground hover:bg-muted rounded"
                  title="复制"
                >
                  <Copy class="w-4 h-4" />
                </button>
              </div>
            </div>
          </div>
        </div>

        <!-- 说明 -->
        <div class="p-3 bg-blue-50/50 dark:bg-blue-900/20 rounded-lg border border-blue-100 dark:border-blue-800">
          <div class="text-xs text-blue-600 dark:text-blue-400 font-medium mb-2">URL 编解码说明:</div>
          <ul class="text-xs text-gray-600 dark:text-gray-300 space-y-1 list-disc list-inside">
            <li>URL 编码将特殊字符转换为 <code class="bg-gray-200 dark:bg-gray-600 px-1 rounded">%XX</code> 格式，使其可以在 URL 中安全传输</li>
            <li>中文字符会被编码为 <code class="bg-gray-200 dark:bg-gray-600 px-1 rounded">%E4%B8%AD%E6%96%87</code> 这样的格式</li>
            <li>空格可以编码为 <code class="bg-gray-200 dark:bg-gray-600 px-1 rounded">+</code> (application/x-www-form-urlencoded) 或 <code class="bg-gray-200 dark:bg-gray-600 px-1 rounded">%20</code></li>
            <li>常用编码字符: <code class="bg-gray-200 dark:bg-gray-600 px-1 rounded">空格 %20</code> <code class="bg-gray-200 dark:bg-gray-600 px-1 rounded">& %26</code> <code class="bg-gray-200 dark:bg-gray-600 px-1 rounded">= %3D</code> <code class="bg-gray-200 dark:bg-gray-600 px-1 rounded">? %3F</code> <code class="bg-gray-200 dark:bg-gray-600 px-1 rounded"># %23</code></li>
          </ul>
        </div>
      </div>
    </div>

    <!-- Toast 提示 -->
    <div
      v-if="toast.show"
      class="fixed bottom-4 right-4 bg-green-500 text-white px-4 py-2 rounded-lg shadow-lg flex items-center gap-2 z-50"
      :class="toast.type === 'error' ? 'bg-red-500' : toast.type === 'info' ? 'bg-blue-500' : 'bg-green-500'"
    >
      <Check class="w-4 h-4" />
      {{ toast.message }}
    </div>
  </NuxtLayout>
</template>
