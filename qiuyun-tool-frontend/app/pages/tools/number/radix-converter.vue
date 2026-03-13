<script setup lang="ts">
import { ref, computed, watch } from 'vue'
import { Binary, Copy, Check, AlertCircle, Trash2, Hash, Calculator } from 'lucide-vue-next'
import { useToolExecutor } from '~/composables/useToolExecutor'
import { ToolType } from '~/types/tool'

// 请求参数
interface RadixConverterParams {
  input: string
  fromBase: number
  toBase?: number
}

// 响应结果
interface RadixConverterResult {
  success: boolean
  input: string
  fromBase: number
  decimalValue: string
  results: Record<string, string>
  errorMessage?: string
}

// 状态
const inputValue = ref('')
const fromBase = ref(10)
const result = ref<RadixConverterResult | null>(null)
const error = ref('')

// Toast 提示
const toast = ref({ show: false, message: '' })
const showToast = (message: string) => {
  toast.value.message = message
  toast.value.show = true
  setTimeout(() => toast.value.show = false, 2000)
}

// 使用工具执行器
const { execute, isLoading } = useToolExecutor<RadixConverterParams, RadixConverterResult>({
  toolCode: 'radix-converter',
  toolType: ToolType.INSTANT,
  onSuccess: (res) => {
    if (res.success) {
      result.value = res
      error.value = ''
    } else {
      error.value = res.errorMessage || '转换失败'
      result.value = null
    }
  },
  onError: (err) => {
    error.value = err
    result.value = null
  }
})

// 执行转换
const convert = async () => {
  if (!inputValue.value.trim()) {
    error.value = '请输入数值'
    return
  }

  await execute({
    input: inputValue.value,
    fromBase: fromBase.value
  })
}

// 复制结果
const copyResult = async (text: string) => {
  try {
    await navigator.clipboard.writeText(text)
    showToast('已复制')
  } catch {
    showToast('复制失败')
  }
}

// 清空
const clearAll = () => {
  inputValue.value = ''
  result.value = null
  error.value = ''
}

// 进制选项
const baseOptions = [
  { value: 2, label: '二进制 (Binary)', prefix: '0b', example: '1010' },
  { value: 8, label: '八进制 (Octal)', prefix: '0o', example: '12' },
  { value: 10, label: '十进制 (Decimal)', prefix: '', example: '10' },
  { value: 16, label: '十六进制 (Hex)', prefix: '0x', example: 'A' }
]

// 获取当前进制的示例
const currentExample = computed(() => {
  const option = baseOptions.find(opt => opt.value === fromBase.value)
  return option?.example || ''
})

// 获取当前进制的说明
const currentBaseDesc = computed(() => {
  const option = baseOptions.find(opt => opt.value === fromBase.value)
  return option?.label || ''
})

// 监听输入变化，自动转换
watch([inputValue, fromBase], () => {
  if (inputValue.value) {
    convert()
  }
}, { deep: true })
</script>

<template>
  <NuxtLayout name="tool" tool-code="radix-converter">
    <div class="border border-border/40 rounded-xl overflow-hidden">
      <!-- 配置选项 -->
      <div class="p-4 border-b bg-muted/20">
        <div class="flex flex-wrap items-center gap-4">
          <!-- 源进制选择 -->
          <div class="flex items-center gap-2">
            <span class="text-sm text-muted-foreground">源进制:</span>
            <select
              v-model="fromBase"
              class="px-3 py-1.5 text-sm border rounded bg-background"
            >
              <option v-for="opt in baseOptions" :key="opt.value" :value="opt.value">
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
          <div class="flex items-center justify-between mb-2">
            <div class="flex items-center gap-2">
              <div class="w-1 h-4 bg-primary rounded-full"></div>
              <label class="text-sm font-medium">输入数值 ({{ currentBaseDesc }})</label>
            </div>
          </div>
          <input
            v-model="inputValue"
            type="text"
            :placeholder="`输入${currentBaseDesc}数值，例如: ${currentExample}`"
            class="w-full px-4 py-2.5 border rounded-lg font-mono text-sm focus:outline-none focus:ring-2 focus:ring-primary/20 focus:border-primary"
          />
          <div class="text-xs text-muted-foreground mt-1">
            支持 2-36 进制，使用数字 0-9 和字母 A-Z
          </div>
        </div>

        <!-- 错误提示 -->
        <div
          v-if="error"
          class="flex items-center gap-2 text-sm text-red-500 bg-red-50/80 p-3 rounded-lg border border-red-200"
        >
          <AlertCircle class="w-4 h-4 shrink-0" />
          {{ error }}
        </div>

        <!-- 转换结果 -->
        <div v-if="result?.results" class="space-y-3">
          <div class="flex items-center gap-2">
            <div class="w-1 h-4 bg-green-500 rounded-full"></div>
            <label class="text-sm font-medium">转换结果</label>
          </div>

          <div class="grid grid-cols-1 md:grid-cols-2 gap-3">
            <!-- 二进制 -->
            <div class="p-3 bg-muted/20 rounded-lg border">
              <div class="flex items-center justify-between mb-2">
                <span class="text-xs text-muted-foreground">二进制 (Binary)</span>
                <button
                  @click="copyResult(result.results['2'] || '')"
                  class="text-xs text-primary hover:underline"
                >
                  复制  
                </button>
              </div>
              <code class="font-mono text-sm break-all">{{ result.results['2'] || '' }}</code>
            </div>

            <!-- 八进制 -->
            <div class="p-3 bg-muted/20 rounded-lg border">
              <div class="flex items-center justify-between mb-2">
                <span class="text-xs text-muted-foreground">八进制 (Octal)</span>
                <button
                  @click="copyResult(result.results['8'] || '')"
                  class="text-xs text-primary hover:underline"
                >
                  复制
                </button>
              </div>
              <code class="font-mono text-sm break-all">{{ result.results['8'] || '' }}</code>
            </div>

            <!-- 十进制 -->
            <div class="p-3 bg-blue-50 rounded-lg border border-blue-200">
              <div class="flex items-center justify-between mb-2">
                <span class="text-xs text-blue-600">十进制 (Decimal)</span>
                <button
                  @click="copyResult(result.results['10'] || '')"
                  class="text-xs text-blue-600 hover:underline"
                >
                  复制
                </button>
              </div>
              <code class="font-mono text-sm break-all text-blue-700">{{ result.results['10'] || '' }}</code>
            </div>

            <!-- 十六进制 -->
            <div class="p-3 bg-muted/20 rounded-lg border">
              <div class="flex items-center justify-between mb-2">
                <span class="text-xs text-muted-foreground">十六进制 (Hex)</span>
                <button
                  @click="copyResult(result.results['16'] || '')"
                  class="text-xs text-primary hover:underline"
                >
                  复制
                </button>
              </div>
              <code class="font-mono text-sm break-all">{{ result.results['16'] || '' }}</code>
            </div>
          </div>
        </div>

        <!-- 进制对照表 -->
        <div class="p-4 bg-muted/20 rounded-lg">
          <h4 class="text-sm font-medium mb-3">常用进制对照表</h4>
          <div class="overflow-x-auto">
            <table class="w-full text-sm">
              <thead>
                <tr class="border-b">
                  <th class="text-left py-2 text-muted-foreground font-normal">十进制</th>
                  <th class="text-left py-2 text-muted-foreground font-normal">二进制</th>
                  <th class="text-left py-2 text-muted-foreground font-normal">八进制</th>
                  <th class="text-left py-2 text-muted-foreground font-normal">十六进制</th>
                </tr>
              </thead>
              <tbody>
                <tr class="border-b border-dashed">
                  <td class="py-2 font-mono">0</td>
                  <td class="py-2 font-mono">0</td>
                  <td class="py-2 font-mono">0</td>
                  <td class="py-2 font-mono">0</td>
                </tr>
                <tr class="border-b border-dashed">
                  <td class="py-2 font-mono">1</td>
                  <td class="py-2 font-mono">1</td>
                  <td class="py-2 font-mono">1</td>
                  <td class="py-2 font-mono">1</td>
                </tr>
                <tr class="border-b border-dashed">
                  <td class="py-2 font-mono">2</td>
                  <td class="py-2 font-mono">10</td>
                  <td class="py-2 font-mono">2</td>
                  <td class="py-2 font-mono">2</td>
                </tr>
                <tr class="border-b border-dashed">
                  <td class="py-2 font-mono">8</td>
                  <td class="py-2 font-mono">1000</td>
                  <td class="py-2 font-mono">10</td>
                  <td class="py-2 font-mono">8</td>
                </tr>
                <tr class="border-b border-dashed">
                  <td class="py-2 font-mono">10</td>
                  <td class="py-2 font-mono">1010</td>
                  <td class="py-2 font-mono">12</td>
                  <td class="py-2 font-mono">A</td>
                </tr>
                <tr class="border-b border-dashed">
                  <td class="py-2 font-mono">16</td>
                  <td class="py-2 font-mono">10000</td>
                  <td class="py-2 font-mono">20</td>
                  <td class="py-2 font-mono">10</td>
                </tr>
                <tr>
                  <td class="py-2 font-mono">255</td>
                  <td class="py-2 font-mono">11111111</td>
                  <td class="py-2 font-mono">377</td>
                  <td class="py-2 font-mono">FF</td>
                </tr>
              </tbody>
            </table>
          </div>
        </div>

        <!-- 说明 -->
        <div class="p-3 bg-blue-50/50 dark:bg-blue-900/20 rounded-lg border border-blue-100 dark:border-blue-800">
          <div class="text-xs text-blue-600 dark:text-blue-400 font-medium mb-2">进制说明:</div>
          <ul class="text-xs text-gray-600 dark:text-gray-300 space-y-1 list-disc list-inside">
            <li><strong class="text-gray-800 dark:text-gray-200">二进制</strong>：基数为 2，使用数字 0-1，计算机底层使用的进制</li>
            <li><strong class="text-gray-800 dark:text-gray-200">八进制</strong>：基数为 8，使用数字 0-7，常用于 Unix 文件权限</li>
            <li><strong class="text-gray-800 dark:text-gray-200">十进制</strong>：基数为 10，使用数字 0-9，日常生活中使用的进制</li>
            <li><strong class="text-gray-800 dark:text-gray-200">十六进制</strong>：基数为 16，使用数字 0-9 和字母 A-F，常用于内存地址、颜色值</li>
            <li>支持 2-36 进制的任意转换，使用数字 0-9 和字母 A-Z 表示数值</li>
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
