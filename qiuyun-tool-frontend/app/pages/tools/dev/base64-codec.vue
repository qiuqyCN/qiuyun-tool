<script setup lang="ts">
import { ref, computed, watch } from 'vue'
import { ArrowRightLeft, Copy, Check, AlertCircle, Trash2, FileCode, FileText } from 'lucide-vue-next'
import { useToolExecutor } from '~/composables/useToolExecutor'
import { ToolType } from '~/types/tool'

// 操作类型
enum OperationType {
  ENCODE = 'encode',
  DECODE = 'decode'
}

// 请求参数
interface Base64Params {
  operation: string
  input: string
  charset: string
  urlSafe: boolean
}

// 响应结果
interface Base64Result {
  success: boolean
  operation: string
  input: string
  output: string
  inputLength: number
  outputLength: number
  errorMessage?: string
}

// 状态
const activeTab = ref<OperationType>(OperationType.ENCODE)
const inputText = ref('')
const charset = ref('UTF-8')
const urlSafe = ref(false)
const result = ref<Base64Result | null>(null)
const error = ref('')

// Toast 提示
const toast = ref({ show: false, message: '' })
const showToast = (message: string) => {
  toast.value.message = message
  toast.value.show = true
  setTimeout(() => toast.value.show = false, 2000)
}

// 使用工具执行器
const { execute, isLoading } = useToolExecutor<Base64Params, Base64Result>({
  toolCode: 'base64-codec',
  toolType: ToolType.INSTANT,
  onSuccess: (res) => {
    if (res.success) {
      result.value = res
      error.value = ''
    } else {
      error.value = res.errorMessage || '操作失败'
      result.value = null
    }
  },
  onError: (err) => {
    error.value = err
    result.value = null
  }
})

// 执行编解码
const processBase64 = async () => {
  if (!inputText.value.trim()) {
    error.value = '请输入内容'
    return
  }

  await execute({
    operation: activeTab.value,
    input: inputText.value,
    charset: charset.value,
    urlSafe: urlSafe.value
  })
}

// 切换操作
const switchOperation = (op: OperationType) => {
  activeTab.value = op
  // 如果有结果，交换输入输出
  if (result.value?.output) {
    inputText.value = result.value.output
    processBase64()
  } else {
    result.value = null
    error.value = ''
  }
}

// 复制结果
const copyResult = async () => {
  if (result.value?.output) {
    try {
      await navigator.clipboard.writeText(result.value.output)
      showToast('已复制到剪贴板')
    } catch {
      showToast('复制失败')
    }
  }
}

// 清空
const clearAll = () => {
  inputText.value = ''
  result.value = null
  error.value = ''
}

// 字符集选项
const charsetOptions = [
  { value: 'UTF-8', label: 'UTF-8 (推荐)' },
  { value: 'GBK', label: 'GBK (中文)' },
  { value: 'ISO-8859-1', label: 'ISO-8859-1 (西欧)' },
  { value: 'ASCII', label: 'ASCII' }
]

// 输入框占位符
const inputPlaceholder = computed(() => {
  return activeTab.value === OperationType.ENCODE
    ? '输入要编码的文本内容...'
    : '输入要解码的Base64字符串...'
})

// 输出框标签
const outputLabel = computed(() => {
  return activeTab.value === OperationType.ENCODE
    ? 'Base64 编码结果'
    : '解码后的文本'
})

// 监听输入变化，自动处理
watch([inputText, charset, urlSafe], () => {
  if (inputText.value) {
    processBase64()
  }
}, { deep: true })
</script>

<template>
  <NuxtLayout name="tool" tool-code="base64-codec">
    <div class="border border-border/40 rounded-xl overflow-hidden">
      <!-- 操作切换 Tabs -->
      <div class="border-b bg-muted/20">
        <div class="flex">
          <button
            @click="switchOperation(OperationType.ENCODE)"
            class="flex items-center gap-2 px-6 py-3 border-b-2 transition-colors"
            :class="activeTab === OperationType.ENCODE
              ? 'border-primary bg-background text-primary'
              : 'border-transparent text-muted-foreground hover:text-foreground'"
          >
            <FileText class="w-4 h-4" />
            编码 (Encode)
          </button>
          <button
            @click="switchOperation(OperationType.DECODE)"
            class="flex items-center gap-2 px-6 py-3 border-b-2 transition-colors"
            :class="activeTab === OperationType.DECODE
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
              <option v-for="opt in charsetOptions" :key="opt.value" :value="opt.value">
                {{ opt.label }}
              </option>
            </select>
          </div>

          <!-- URL安全选项 -->
          <label class="flex items-center gap-2 text-sm cursor-pointer">
            <input
              v-model="urlSafe"
              type="checkbox"
              class="rounded border-gray-300"
            />
            <span>URL安全 (将 <code class="bg-muted px-1 rounded">+</code> 替换为 <code class="bg-muted px-1 rounded">-</code>，<code class="bg-muted px-1 rounded">/</code> 替换为 <code class="bg-muted px-1 rounded">_</code>)</span>
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

        <!-- 输入输出区域 -->
        <div class="grid grid-cols-1 lg:grid-cols-2 gap-4">
          <!-- 输入区域 -->
          <div class="flex flex-col gap-2">
            <div class="flex items-center gap-2">
              <div class="w-1 h-4 bg-primary rounded-full"></div>
              <label class="text-sm font-medium">
                {{ activeTab === OperationType.ENCODE ? '输入文本' : '输入 Base64' }}
              </label>
            </div>
            <Textarea
              v-model="inputText"
              :placeholder="inputPlaceholder"
              rows="10"
              class="font-mono text-sm resize-none"
            />
          </div>

          <!-- 输出区域 -->
          <div class="flex flex-col gap-2">
            <div class="flex items-center justify-between">
              <div class="flex items-center gap-2">
                <div class="w-1 h-4 bg-green-500 rounded-full"></div>
                <label class="text-sm font-medium">{{ outputLabel }}</label>
              </div>
              <button
                v-if="result?.output"
                @click="copyResult"
                class="text-xs text-primary hover:underline flex items-center gap-1"
              >
                <Copy class="w-3 h-3" />
                复制结果
              </button>
            </div>
            <Textarea
              :value="result?.output || ''"
              readonly
              rows="10"
              class="font-mono text-sm resize-none bg-muted/20"
              :placeholder="activeTab === OperationType.ENCODE ? 'Base64 编码结果将显示在这里...' : '解码后的文本将显示在这里...'"
            />
          </div>
        </div>

        <!-- 统计信息 -->
        <div v-if="result" class="flex items-center gap-4 text-xs text-muted-foreground p-3 bg-muted/20 rounded-lg">
          <span>输入长度: <strong class="text-foreground">{{ result.inputLength }}</strong> 字符</span>
          <span>输出长度: <strong class="text-foreground">{{ result.outputLength }}</strong> 字符</span>
          <span v-if="result.inputLength > 0">
            膨胀率: <strong class="text-foreground">{{ ((result.outputLength / result.inputLength - 1) * 100).toFixed(1) }}%</strong>
          </span>
        </div>

        <!-- 错误提示 -->
        <div
          v-if="error"
          class="flex items-center gap-2 text-sm text-red-500 bg-red-50/80 p-3 rounded-lg border border-red-200"
        >
          <AlertCircle class="w-4 h-4 flex-shrink-0" />
          {{ error }}
        </div>

        <!-- 说明 -->
        <div class="p-3 bg-blue-50/50 rounded-lg border border-blue-100">
          <div class="text-xs text-blue-600 font-medium mb-2">Base64 说明:</div>
          <ul class="text-xs text-muted-foreground space-y-1 list-disc list-inside">
            <li>Base64 是一种基于 64 个可打印字符来表示二进制数据的编码方式</li>
            <li>编码后数据体积会增加约 33%</li>
            <li>常用于在 URL、Cookie、邮件中传输二进制数据</li>
            <li>URL 安全模式会将 <code class="bg-muted px-1 rounded">+</code> 和 <code class="bg-muted px-1 rounded">/</code> 替换为 <code class="bg-muted px-1 rounded">-</code> 和 <code class="bg-muted px-1 rounded">_</code>，避免 URL 转义问题</li>
          </ul>
        </div>
      </div>
    </div>

    <!-- Toast 提示 -->
    <div
      v-if="toast.show"
      class="fixed bottom-4 right-4 bg-green-500 text-white px-4 py-2 rounded-lg shadow-lg flex items-center gap-2 z-50"
    >
      <Check class="w-4 h-4" />
      {{ toast.message }}
    </div>
  </NuxtLayout>
</template>
