<script setup lang="ts">
import { ref, watch } from 'vue'
import { Hash, Copy, Check, AlertCircle, Trash2, Type } from 'lucide-vue-next'
import { useToolExecutor } from '~/composables/useToolExecutor'
import { useToast } from '~/composables/useToast'
import { useClipboard } from '~/composables/useClipboard'
import { ToolType } from '~/types/tool'
import { CHARSET_OPTIONS } from '~/constants/tool'

// 请求参数
interface Md5Params {
  input: string
  bitLength: number
  uppercase: boolean
  charset: string
}

// 单个结果
interface Md5Result {
  input: string
  output: string
}

// 响应结果
interface Md5Response {
  success: boolean
  bitLength: number
  uppercase: boolean
  results: Md5Result[]
  count: number
  errorMessage?: string
}

// 状态
const inputText = ref('')
const bitLength = ref(32)
const uppercase = ref(false)
const charset = ref('UTF-8')
const results = ref<Md5Result[]>([])
const error = ref('')

// 使用通用 composables
const { toast, showSuccess } = useToast()
const { copy, copyMultiple } = useClipboard(showSuccess)

// 使用工具执行器
const { execute, isLoading } = useToolExecutor<Md5Params, Md5Response>({
  toolCode: 'md5-encrypt',
  toolType: ToolType.INSTANT,
  onSuccess: (res) => {
    if (res.success) {
      results.value = res.results
      error.value = ''
    } else {
      error.value = res.errorMessage || '加密失败'
      results.value = []
    }
  },
  onError: (err) => {
    error.value = err
    results.value = []
  }
})

// 执行加密
const encrypt = async () => {
  if (!inputText.value.trim()) {
    error.value = '请输入要加密的内容'
    return
  }

  await execute({
    input: inputText.value,
    bitLength: bitLength.value,
    uppercase: uppercase.value,
    charset: charset.value
  })
}

// 复制所有结果
const copyAllResults = async () => {
  const texts = results.value.map(r => `${r.input}: ${r.output}`)
  await copyMultiple(texts, '已复制所有结果')
}

// 清空
const clearAll = () => {
  inputText.value = ''
  results.value = []
  error.value = ''
}

// 监听输入变化，自动加密
watch([inputText, bitLength, uppercase, charset], () => {
  if (inputText.value) {
    encrypt()
  }
}, { deep: true })
</script>

<template>
  <NuxtLayout name="tool" tool-code="md5-encrypt">
    <div class="border border-border/40 rounded-xl overflow-hidden">
      <!-- 配置选项 -->
      <div class="p-4 border-b bg-muted/20">
        <div class="flex flex-wrap items-center gap-4">
          <!-- 位数选择 -->
          <div class="flex items-center gap-2">
            <span class="text-sm text-muted-foreground">位数:</span>
            <div class="flex gap-1">
              <button
                @click="bitLength = 32"
                class="px-3 py-1.5 text-sm rounded transition-colors"
                :class="bitLength === 32 ? 'bg-primary text-primary-foreground' : 'bg-muted hover:bg-muted/80'"
              >
                32位
              </button>
              <button
                @click="bitLength = 16"
                class="px-3 py-1.5 text-sm rounded transition-colors"
                :class="bitLength === 16 ? 'bg-primary text-primary-foreground' : 'bg-muted hover:bg-muted/80'"
              >
                16位
              </button>
            </div>
          </div>

          <!-- 大小写选择 -->
          <div class="flex items-center gap-2">
            <span class="text-sm text-muted-foreground">输出格式:</span>
            <div class="flex gap-1">
              <button
                @click="uppercase = false"
                class="px-3 py-1.5 text-sm rounded transition-colors"
                :class="!uppercase ? 'bg-primary text-primary-foreground' : 'bg-muted hover:bg-muted/80'"
              >
                小写
              </button>
              <button
                @click="uppercase = true"
                class="px-3 py-1.5 text-sm rounded transition-colors"
                :class="uppercase ? 'bg-primary text-primary-foreground' : 'bg-muted hover:bg-muted/80'"
              >
                大写
              </button>
            </div>
          </div>

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
      </div>

      <div class="p-6 space-y-4">
        <!-- 输入区域 -->
        <div>
          <div class="flex items-center gap-2 mb-2">
            <div class="w-1 h-4 bg-primary rounded-full"></div>
            <label class="text-sm font-medium">输入文本</label>
            <span class="text-xs text-muted-foreground">(支持多行批量加密)</span>
          </div>
          <Textarea
            v-model="inputText"
            placeholder="输入要加密的文本内容，支持多行批量加密..."
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

        <!-- 加密结果 -->
        <div v-if="results.length > 0" class="space-y-3">
          <div class="flex items-center justify-between">
            <div class="flex items-center gap-2">
              <div class="w-1 h-4 bg-green-500 rounded-full"></div>
              <label class="text-sm font-medium">MD5 加密结果</label>
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
                <span class="text-xs text-muted-foreground">MD5:</span>
                <code class="text-sm font-mono bg-primary/5 text-primary px-2 py-0.5 rounded flex-1">{{ result.output }}</code>
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
        <div class="p-3 bg-blue-50/50 rounded-lg border border-blue-100">
          <div class="text-xs text-blue-600 font-medium mb-2">MD5 说明:</div>
          <ul class="text-xs text-muted-foreground space-y-1 list-disc list-inside">
            <li>MD5 是一种广泛使用的哈希算法，产生 128 位（16字节）的哈希值</li>
            <li>32位输出：完整的 32 个十六进制字符</li>
            <li>16位输出：取中间 16 个字符（第9-24位）</li>
            <li>MD5 是单向哈希，无法从结果反推原文</li>
            <li>常用于密码存储、文件校验、数据完整性验证</li>
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
