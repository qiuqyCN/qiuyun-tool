<script setup lang="ts">
import { ref, computed, watch } from 'vue'
import { Clock, Copy, Check, AlertCircle, ArrowRightLeft, Calendar, Hash } from 'lucide-vue-next'
import { useToolExecutor } from '~/composables/useToolExecutor'
import { ToolType } from '~/types/tool'

// 操作类型
enum OperationType {
  TIMESTAMP_TO_DATE = 'timestamp-to-date',
  DATE_TO_TIMESTAMP = 'date-to-timestamp'
}

// 时间戳单位
enum UnitType {
  SECONDS = 'seconds',
  MILLISECONDS = 'milliseconds'
}

// 请求参数
interface TimestampParams {
  operation: string
  timestamp?: number
  timestamps?: number[]
  dateString?: string
  dateFormat: string
  unit: string
}

// 转换结果
interface ConversionResult {
  input: string
  output?: string
  timestampSec?: number
  timestampMs?: number
  dateTimes: Record<string, string>
  success: boolean
  errorMessage?: string
}

// 响应结果
interface TimestampResult {
  success: boolean
  operation: string
  results: ConversionResult[]
  errorMessage?: string
}

// 状态
const activeTab = ref<OperationType>(OperationType.TIMESTAMP_TO_DATE)
const inputValue = ref('')
const unit = ref<UnitType>(UnitType.MILLISECONDS)
const dateFormat = ref('yyyy-MM-dd HH:mm:ss')
const results = ref<ConversionResult[]>([])
const error = ref('')

// 当前时间戳
const currentTimestamp = computed(() => {
  return Math.floor(Date.now() / 1000)
})

const currentTimestampMs = computed(() => {
  return Date.now()
})

// Toast 提示
const toast = ref({ show: false, message: '' })
const showToast = (message: string) => {
  toast.value.message = message
  toast.value.show = true
  setTimeout(() => toast.value.show = false, 2000)
}

// 使用工具执行器
const { execute, isLoading } = useToolExecutor<TimestampParams, TimestampResult>({
  toolCode: 'timestamp-converter',
  toolType: ToolType.INSTANT,
  onSuccess: (result) => {
    if (result.success) {
      results.value = result.results
      error.value = ''
    } else {
      error.value = result.errorMessage || '转换失败'
      results.value = []
    }
  },
  onError: (err) => {
    error.value = err
    results.value = []
  }
})

// 执行转换
const convert = async () => {
  if (!inputValue.value.trim()) {
    error.value = '请输入要转换的值'
    return
  }

  if (activeTab.value === OperationType.TIMESTAMP_TO_DATE) {
    // 时间戳转日期
    const timestamps = inputValue.value
      .split(/[\n,;\s]+/)
      .map(s => s.trim())
      .filter(s => s)
      .map(s => parseInt(s))
      .filter(n => !isNaN(n))

    if (timestamps.length === 0) {
      error.value = '请输入有效的时间戳'
      return
    }

    await execute({
      operation: activeTab.value,
      timestamps: timestamps.length === 1 ? undefined : timestamps,
      timestamp: timestamps.length === 1 ? timestamps[0] : undefined,
      unit: unit.value,
      dateFormat: dateFormat.value
    })
  } else {
    // 日期转时间戳
    await execute({
      operation: activeTab.value,
      dateString: inputValue.value,
      dateFormat: dateFormat.value,
      unit: unit.value
    })
  }
}

// 使用当前时间戳
const useCurrentTimestamp = () => {
  if (unit.value === UnitType.SECONDS) {
    inputValue.value = String(currentTimestamp.value)
  } else {
    inputValue.value = String(currentTimestampMs.value)
  }
  convert()
}

// 使用当前时间
const useCurrentDate = () => {
  const now = new Date()
  const formatMap: Record<string, () => string> = {
    'yyyy-MM-dd HH:mm:ss': () => {
      return now.toLocaleString('zh-CN', {
        year: 'numeric',
        month: '2-digit',
        day: '2-digit',
        hour: '2-digit',
        minute: '2-digit',
        second: '2-digit',
        hour12: false
      }).replace(/\//g, '-')
    },
    'yyyy-MM-dd': () => {
      return now.toISOString().split('T')[0]
    },
    'yyyy/MM/dd HH:mm:ss': () => {
      return now.toLocaleString('zh-CN', {
        year: 'numeric',
        month: '2-digit',
        day: '2-digit',
        hour: '2-digit',
        minute: '2-digit',
        second: '2-digit',
        hour12: false
      })
    }
  }
  
  inputValue.value = (formatMap[dateFormat.value] || formatMap['yyyy-MM-dd HH:mm:ss'])()
  convert()
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
const clearInput = () => {
  inputValue.value = ''
  results.value = []
  error.value = ''
}

// 切换标签
const switchTab = (tab: OperationType) => {
  activeTab.value = tab
  clearInput()
}

// 日期格式选项
const dateFormatOptions = [
  { value: 'yyyy-MM-dd HH:mm:ss', label: '标准格式 (2026-03-11 10:30:00)' },
  { value: 'yyyy-MM-dd', label: '日期格式 (2026-03-11)' },
  { value: 'yyyy/MM/dd HH:mm:ss', label: '斜杠格式 (2026/03/11 10:30:00)' },
  { value: 'dd/MM/yyyy HH:mm:ss', label: '欧洲格式 (11/03/2026 10:30:00)' },
  { value: 'MM-dd-yyyy HH:mm:ss', label: '美国格式 (03-11-2026 10:30:00)' },
  { value: 'yyyy年MM月dd日 HH:mm:ss', label: '中文格式 (2026年03月11日 10:30:00)' }
]

// 时区标签
const timezoneLabels: Record<string, string> = {
  'UTC': 'UTC 标准时间',
  'Local': '本地时间',
  'Asia/Shanghai': '北京时间',
  'Asia/Tokyo': '东京时间',
  'America/New_York': '纽约时间',
  'Europe/London': '伦敦时间'
}

// 输入框占位符
const inputPlaceholder = computed(() => {
  if (activeTab.value === OperationType.TIMESTAMP_TO_DATE) {
    return unit.value === UnitType.SECONDS 
      ? '输入 Unix 时间戳（秒），支持批量输入多个，用空格/逗号/换行分隔\n例如: 1705312200'
      : '输入 Unix 时间戳（毫秒），支持批量输入多个，用空格/逗号/换行分隔\n例如: 1705312200000'
  }
  return '输入日期时间\n例如: 2024-01-15 10:30:00'
})

// 监听输入变化，自动转换
watch([inputValue, unit, dateFormat], () => {
  if (inputValue.value) {
    convert()
  }
}, { deep: true })
</script>

<template>
  <NuxtLayout name="tool" tool-code="timestamp-converter">
    <div class="border border-border/40 rounded-xl overflow-hidden">
      <!-- 操作切换 Tabs -->
      <div class="border-b bg-muted/20">
        <div class="flex">
          <button
            @click="switchTab(OperationType.TIMESTAMP_TO_DATE)"
            class="flex items-center gap-2 px-6 py-3 border-b-2 transition-colors"
            :class="activeTab === OperationType.TIMESTAMP_TO_DATE
              ? 'border-primary bg-background text-primary'
              : 'border-transparent text-muted-foreground hover:text-foreground'"
          >
            <Hash class="w-4 h-4" />
            时间戳 → 日期
          </button>
          <button
            @click="switchTab(OperationType.DATE_TO_TIMESTAMP)"
            class="flex items-center gap-2 px-6 py-3 border-b-2 transition-colors"
            :class="activeTab === OperationType.DATE_TO_TIMESTAMP
              ? 'border-primary bg-background text-primary'
              : 'border-transparent text-muted-foreground hover:text-foreground'"
          >
            <Calendar class="w-4 h-4" />
            日期 → 时间戳
          </button>
        </div>
      </div>

      <div class="p-6 space-y-4">
        <!-- 配置选项 -->
        <div class="flex flex-wrap items-center gap-4 p-3 bg-muted/30 rounded-lg">
          <!-- 单位选择 -->
          <div class="flex items-center gap-2">
            <span class="text-sm text-muted-foreground">时间戳单位:</span>
            <div class="flex gap-1">
              <button
                @click="unit = UnitType.SECONDS"
                class="px-3 py-1 text-sm rounded transition-colors"
                :class="unit === UnitType.SECONDS ? 'bg-primary text-primary-foreground' : 'bg-muted hover:bg-muted/80'"
              >
                秒 (s)
              </button>
              <button
                @click="unit = UnitType.MILLISECONDS"
                class="px-3 py-1 text-sm rounded transition-colors"
                :class="unit === UnitType.MILLISECONDS ? 'bg-primary text-primary-foreground' : 'bg-muted hover:bg-muted/80'"
              >
                毫秒 (ms)
              </button>
            </div>
          </div>

          <!-- 日期格式 -->
          <div class="flex items-center gap-2">
            <span class="text-sm text-muted-foreground">日期格式:</span>
            <select
              v-model="dateFormat"
              class="px-3 py-1 text-sm border rounded bg-background"
            >
              <option v-for="opt in dateFormatOptions" :key="opt.value" :value="opt.value">
                {{ opt.label }}
              </option>
            </select>
          </div>

          <div class="ml-auto flex items-center gap-2">
            <button
              v-if="activeTab === OperationType.TIMESTAMP_TO_DATE"
              @click="useCurrentTimestamp"
              class="text-xs text-muted-foreground hover:text-foreground px-2 py-1 rounded hover:bg-muted transition-colors"
            >
              使用当前时间戳
            </button>
            <button
              v-else
              @click="useCurrentDate"
              class="text-xs text-muted-foreground hover:text-foreground px-2 py-1 rounded hover:bg-muted transition-colors"
            >
              使用当前时间
            </button>
            <button
              @click="clearInput"
              class="text-xs text-muted-foreground hover:text-foreground px-2 py-1 rounded hover:bg-muted transition-colors"
            >
              清空
            </button>
          </div>
        </div>

        <!-- 输入区域 -->
        <div>
          <label class="text-sm font-medium mb-2 block">
            {{ activeTab === OperationType.TIMESTAMP_TO_DATE ? '输入时间戳' : '输入日期时间' }}
          </label>
          <Textarea
            v-model="inputValue"
            :placeholder="inputPlaceholder"
            rows="4"
            class="font-mono text-sm resize-none"
          />
        </div>

        <!-- 错误提示 -->
        <div
          v-if="error"
          class="flex items-center gap-2 text-sm text-red-500 bg-red-50/80 p-3 rounded-lg border border-red-200"
        >
          <AlertCircle class="w-4 h-4 flex-shrink-0" />
          {{ error }}
        </div>

        <!-- 转换结果 -->
        <div v-if="results.length > 0" class="space-y-3">
          <div class="flex items-center gap-2">
            <div class="w-1 h-4 bg-green-500 rounded-full"></div>
            <label class="text-sm font-medium">转换结果</label>
          </div>

          <div
            v-for="(result, index) in results"
            :key="index"
            class="p-4 bg-muted/20 rounded-lg border space-y-3"
          >
            <!-- 输入值 -->
            <div class="flex items-center gap-2 text-sm">
              <span class="text-muted-foreground">输入:</span>
              <code class="bg-muted px-2 py-0.5 rounded font-mono">{{ result.input }}</code>
            </div>

            <!-- 时间戳转日期结果 -->
            <template v-if="activeTab === OperationType.TIMESTAMP_TO_DATE">
              <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-2">
                <div
                  v-for="(dateTime, tz) in result.dateTimes"
                  :key="tz"
                  class="p-2 bg-white rounded border"
                >
                  <div class="text-xs text-muted-foreground mb-1">{{ timezoneLabels[tz] || tz }}</div>
                  <div class="font-mono text-sm">{{ dateTime }}</div>
                </div>
              </div>
              <div class="flex items-center gap-4 text-xs text-muted-foreground">
                <span>秒: {{ result.timestampSec }}</span>
                <span>毫秒: {{ result.timestampMs }}</span>
                <button
                  @click="copyResult(String(result.timestampMs))"
                  class="text-primary hover:underline"
                >
                  复制毫秒时间戳
                </button>
              </div>
            </template>

            <!-- 日期转时间戳结果 -->
            <template v-else>
              <div class="flex items-center gap-4">
                <div class="p-3 bg-white rounded border flex-1">
                  <div class="text-xs text-muted-foreground mb-1">秒级时间戳</div>
                  <div class="font-mono text-lg">{{ result.timestampSec }}</div>
                </div>
                <div class="p-3 bg-white rounded border flex-1">
                  <div class="text-xs text-muted-foreground mb-1">毫秒级时间戳</div>
                  <div class="font-mono text-lg">{{ result.timestampMs }}</div>
                </div>
              </div>
              <div class="flex gap-2">
                <button
                  @click="copyResult(String(result.timestampSec))"
                  class="text-xs text-primary hover:underline"
                >
                  复制秒级时间戳
                </button>
                <button
                  @click="copyResult(String(result.timestampMs))"
                  class="text-xs text-primary hover:underline"
                >
                  复制毫秒级时间戳
                </button>
              </div>
            </template>
          </div>
        </div>

        <!-- 当前时间参考 -->
        <div class="p-3 bg-blue-50/50 rounded-lg border border-blue-100">
          <div class="text-xs text-blue-600 font-medium mb-2">当前时间参考:</div>
          <div class="grid grid-cols-2 md:grid-cols-4 gap-2 text-xs">
            <div class="flex items-center gap-1">
              <span class="text-muted-foreground">秒:</span>
              <code class="bg-white px-1.5 py-0.5 rounded border font-mono">{{ currentTimestamp }}</code>
            </div>
            <div class="flex items-center gap-1">
              <span class="text-muted-foreground">毫秒:</span>
              <code class="bg-white px-1.5 py-0.5 rounded border font-mono">{{ currentTimestampMs }}</code>
            </div>
            <div class="flex items-center gap-1 col-span-2">
              <span class="text-muted-foreground">本地时间:</span>
              <span>{{ new Date().toLocaleString('zh-CN') }}</span>
            </div>
          </div>
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
