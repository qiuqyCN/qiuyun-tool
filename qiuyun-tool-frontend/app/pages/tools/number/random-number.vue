<script setup lang="ts">
import { ref, computed, watch } from 'vue'
import { Dices, Copy, Check, AlertCircle, Trash2, RefreshCw, Hash, Type, Lock, Sparkles, ToggleLeft, List } from 'lucide-vue-next'
import { useToolExecutor } from '~/composables/useToolExecutor'
import { ToolType } from '~/types/tool'

// 生成类型
enum GeneratorType {
  INTEGER = 'integer',
  FLOAT = 'float',
  PASSWORD = 'password',
  STRING = 'string',
  UUID = 'uuid',
  BOOLEAN = 'boolean',
  CHOICE = 'choice'
}

// 请求参数
interface RandomNumberParams {
  type: string
  count: number
  minValue?: number
  maxValue?: number
  decimalPlaces?: number
  length?: number
  useUppercase?: boolean
  useLowercase?: boolean
  useNumbers?: boolean
  useSymbols?: boolean
  charSet?: string
  choices?: string[]
}

// 响应结果
interface RandomNumberResult {
  success: boolean
  type: string
  count: number
  results: string[]
  errorMessage?: string
}

// 状态
const activeType = ref<GeneratorType>(GeneratorType.INTEGER)
const count = ref(1)
const minValue = ref(1)
const maxValue = ref(100)
const decimalPlaces = ref(2)
const length = ref(16)
const useUppercase = ref(true)
const useLowercase = ref(true)
const useNumbers = ref(true)
const useSymbols = ref(false)
const charSet = ref('alphanumeric')
const choices = ref('')
const results = ref<string[]>([])
const error = ref('')

// Toast 提示
const toast = ref({ show: false, message: '' })
const showToast = (message: string) => {
  toast.value.message = message
  toast.value.show = true
  setTimeout(() => toast.value.show = false, 2000)
}

// 使用工具执行器
const { execute, isLoading } = useToolExecutor<RandomNumberParams, RandomNumberResult>({
  toolCode: 'random-number',
  toolType: ToolType.INSTANT,
  onSuccess: (res) => {
    if (res.success) {
      results.value = res.results
      error.value = ''
    } else {
      error.value = res.errorMessage || '生成失败'
      results.value = []
    }
  },
  onError: (err) => {
    error.value = err
    results.value = []
  }
})

// 执行生成
const generate = async () => {
  const params: RandomNumberParams = {
    type: activeType.value,
    count: count.value
  }

  switch (activeType.value) {
    case GeneratorType.INTEGER:
      params.minValue = minValue.value
      params.maxValue = maxValue.value
      break
    case GeneratorType.FLOAT:
      params.minValue = minValue.value
      params.maxValue = maxValue.value
      params.decimalPlaces = decimalPlaces.value
      break
    case GeneratorType.PASSWORD:
      params.length = length.value
      params.useUppercase = useUppercase.value
      params.useLowercase = useLowercase.value
      params.useNumbers = useNumbers.value
      params.useSymbols = useSymbols.value
      break
    case GeneratorType.STRING:
      params.length = length.value
      params.charSet = charSet.value
      break
    case GeneratorType.CHOICE:
      params.choices = choices.value.split('\n').filter(s => s.trim())
      break
  }

  await execute(params)
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

// 复制所有结果
const copyAllResults = async () => {
  try {
    await navigator.clipboard.writeText(results.value.join('\n'))
    showToast('已复制所有结果')
  } catch {
    showToast('复制失败')
  }
}

// 清空
const clearAll = () => {
  results.value = []
  error.value = ''
}

// 类型选项
const typeOptions = [
  { value: GeneratorType.INTEGER, label: '整数', icon: Hash },
  { value: GeneratorType.FLOAT, label: '浮点数', icon: Hash },
  { value: GeneratorType.PASSWORD, label: '密码', icon: Lock },
  { value: GeneratorType.STRING, label: '字符串', icon: Type },
  { value: GeneratorType.UUID, label: 'UUID', icon: Sparkles },
  { value: GeneratorType.BOOLEAN, label: '布尔值', icon: ToggleLeft },
  { value: GeneratorType.CHOICE, label: '随机选择', icon: List }
]

// 字符集选项
const charSetOptions = [
  { value: 'alpha', label: '字母 (A-Z, a-z)' },
  { value: 'alphanumeric', label: '字母数字 (A-Z, a-z, 0-9)' },
  { value: 'numeric', label: '数字 (0-9)' },
  { value: 'hex', label: '十六进制 (0-9, a-f)' }
]

// 切换类型时重置结果
const switchType = (type: GeneratorType) => {
  activeType.value = type
  results.value = []
  error.value = ''
}
</script>

<template>
  <NuxtLayout name="tool" tool-code="random-number">
    <div class="border border-border/40 rounded-xl overflow-hidden">
      <!-- 类型选择 -->
      <div class="border-b bg-muted/20">
        <div class="flex flex-wrap">
          <button
            v-for="opt in typeOptions"
            :key="opt.value"
            @click="switchType(opt.value)"
            class="flex items-center gap-1.5 px-4 py-2.5 border-b-2 transition-colors text-sm"
            :class="activeType === opt.value
              ? 'border-primary bg-background text-primary'
              : 'border-transparent text-muted-foreground hover:text-foreground'"
          >
            <component :is="opt.icon" class="w-3.5 h-3.5" />
            {{ opt.label }}
          </button>
        </div>
      </div>

      <div class="p-6 space-y-4">
        <!-- 配置选项 -->
        <div class="p-4 bg-muted/30 rounded-lg space-y-4">
          <!-- 通用：生成数量 -->
          <div class="flex items-center gap-4">
            <span class="text-sm text-muted-foreground w-20">生成数量:</span>
            <input
              v-model.number="count"
              type="number"
              min="1"
              max="100"
              class="w-20 px-2 py-1 border rounded text-sm"
            />
            <span class="text-xs text-muted-foreground">(1-100)</span>
          </div>

          <!-- 整数/浮点数：范围 -->
          <template v-if="activeType === GeneratorType.INTEGER || activeType === GeneratorType.FLOAT">
            <div class="flex items-center gap-4">
              <span class="text-sm text-muted-foreground w-20">范围:</span>
              <input
                v-model.number="minValue"
                type="number"
                class="w-24 px-2 py-1 border rounded text-sm"
                placeholder="最小值"
              />
              <span class="text-muted-foreground">~</span>
              <input
                v-model.number="maxValue"
                type="number"
                class="w-24 px-2 py-1 border rounded text-sm"
                placeholder="最大值"
              />
            </div>
          </template>

          <!-- 浮点数：小数位数 -->
          <div v-if="activeType === GeneratorType.FLOAT" class="flex items-center gap-4">
            <span class="text-sm text-muted-foreground w-20">小数位数:</span>
            <input
              v-model.number="decimalPlaces"
              type="number"
              min="0"
              max="10"
              class="w-20 px-2 py-1 border rounded text-sm"
            />
          </div>

          <!-- 密码/字符串：长度 -->
          <div v-if="activeType === GeneratorType.PASSWORD || activeType === GeneratorType.STRING" class="flex items-center gap-4">
            <span class="text-sm text-muted-foreground w-20">长度:</span>
            <input
              v-model.number="length"
              type="number"
              min="1"
              max="128"
              class="w-24 px-2 py-1 border rounded text-sm"
            />
          </div>

          <!-- 密码：字符选项 -->
          <div v-if="activeType === GeneratorType.PASSWORD" class="flex items-center gap-4 flex-wrap">
            <span class="text-sm text-muted-foreground w-20">包含:</span>
            <label class="flex items-center gap-1.5 text-sm cursor-pointer">
              <input v-model="useUppercase" type="checkbox" class="rounded" />
              <span>大写字母</span>
            </label>
            <label class="flex items-center gap-1.5 text-sm cursor-pointer">
              <input v-model="useLowercase" type="checkbox" class="rounded" />
              <span>小写字母</span>
            </label>
            <label class="flex items-center gap-1.5 text-sm cursor-pointer">
              <input v-model="useNumbers" type="checkbox" class="rounded" />
              <span>数字</span>
            </label>
            <label class="flex items-center gap-1.5 text-sm cursor-pointer">
              <input v-model="useSymbols" type="checkbox" class="rounded" />
              <span>特殊符号</span>
            </label>
          </div>

          <!-- 字符串：字符集 -->
          <div v-if="activeType === GeneratorType.STRING" class="flex items-center gap-4">
            <span class="text-sm text-muted-foreground w-20">字符集:</span>
            <select v-model="charSet" class="px-2 py-1 border rounded text-sm">
              <option v-for="opt in charSetOptions" :key="opt.value" :value="opt.value">
                {{ opt.label }}
              </option>
            </select>
          </div>

          <!-- 随机选择：选项列表 -->
          <div v-if="activeType === GeneratorType.CHOICE" class="space-y-2">
            <span class="text-sm text-muted-foreground">选项列表 (每行一个):</span>
            <Textarea
              v-model="choices"
              placeholder="输入选项，每行一个&#10;例如：&#10;选项A&#10;选项B&#10;选项C"
              rows="4"
              class="text-sm resize-none"
            />
          </div>
        </div>

        <!-- 操作按钮 -->
        <div class="flex items-center gap-3">
          <Button @click="generate" :disabled="isLoading">
            <RefreshCw v-if="isLoading" class="w-4 h-4 mr-2 animate-spin" />
            <Dices v-else class="w-4 h-4 mr-2" />
            生成
          </Button>
          <Button variant="outline" @click="clearAll" :disabled="results.length === 0">
            <Trash2 class="w-4 h-4 mr-2" />
            清空
          </Button>
        </div>

        <!-- 错误提示 -->
        <div
          v-if="error"
          class="flex items-center gap-2 text-sm text-red-500 bg-red-50/80 p-3 rounded-lg border border-red-200"
        >
          <AlertCircle class="w-4 h-4 shrink-0" />
          {{ error }}
        </div>

        <!-- 生成结果 -->
        <div v-if="results.length > 0" class="space-y-3">
          <div class="flex items-center justify-between">
            <div class="flex items-center gap-2">
              <div class="w-1 h-4 bg-green-500 rounded-full"></div>
              <label class="text-sm font-medium">生成结果</label>
              <span class="text-xs text-muted-foreground">({{ results.length }} 个)</span>
            </div>
            <button
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
              class="flex items-center gap-2 p-3 bg-muted/20 rounded-lg border"
            >
              <span class="text-xs text-muted-foreground w-8">{{ index + 1 }}.</span>
              <code class="font-mono text-sm flex-1 break-all">{{ result }}</code>
              <button
                @click="copyResult(result)"
                class="p-1.5 text-muted-foreground hover:text-foreground hover:bg-muted rounded"
                title="复制"
              >
                <Copy class="w-4 h-4" />
              </button>
            </div>
          </div>
        </div>

        <!-- 说明 -->
        <div class="p-3 bg-blue-50/50 dark:bg-blue-900/20 rounded-lg border border-blue-100 dark:border-blue-800">
          <div class="text-xs text-blue-600 dark:text-blue-400 font-medium mb-2">功能说明:</div>
          <ul class="text-xs text-gray-600 dark:text-gray-300 space-y-1 list-disc list-inside">
            <li><strong class="text-gray-800 dark:text-gray-200">整数</strong>：生成指定范围内的随机整数</li>
            <li><strong class="text-gray-800 dark:text-gray-200">浮点数</strong>：生成指定范围内的随机小数</li>
            <li><strong class="text-gray-800 dark:text-gray-200">密码</strong>：生成包含字母、数字、符号的随机密码</li>
            <li><strong class="text-gray-800 dark:text-gray-200">字符串</strong>：按指定字符集生成随机字符串</li>
            <li><strong class="text-gray-800 dark:text-gray-200">UUID</strong>：生成标准 UUID v4 格式</li>
            <li><strong class="text-gray-800 dark:text-gray-200">布尔值</strong>：随机生成 true 或 false</li>
            <li><strong class="text-gray-800 dark:text-gray-200">随机选择</strong>：从选项列表中随机选择一个</li>
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
