<script setup lang="ts">
import { ref, computed, watch } from 'vue'
import { Clock, Calendar, Settings, ArrowRightLeft, Info, Check, AlertCircle } from 'lucide-vue-next'
import { ToolCard } from '@/components/ui/tool-card'

useHead({
  title: 'Cron 生成与解析 - 秋云工具',
  meta: [
    { name: 'description', content: '支持 Linux、Quartz、Spring、AWS 等多种格式的 Cron 表达式生成、解析和转换工具' }
  ]
})

// Cron 格式类型
enum CronFormat {
  LINUX = 'linux',
  QUARTZ = 'quartz',
  SPRING = 'spring',
  AWS = 'aws'
}

// 格式配置
interface FormatConfig {
  name: string
  description: string
  fields: CronField[]
  supportsSpecialChars: string[]
  weekStart: number // 0=周日, 1=周一
  weekNames: string[]
}

interface CronField {
  name: string
  label: string
  min: number
  max: number
  names?: string[]
  optional?: boolean
}

const formatConfigs: Record<CronFormat, FormatConfig> = {
  [CronFormat.LINUX]: {
    name: 'Linux / Unix',
    description: '标准 5 位 Cron 格式，广泛用于 Linux/Unix 系统',
    fields: [
      { name: 'minute', label: '分钟', min: 0, max: 59 },
      { name: 'hour', label: '小时', min: 0, max: 23 },
      { name: 'day', label: '日期', min: 1, max: 31 },
      { name: 'month', label: '月份', min: 1, max: 12, names: ['一月', '二月', '三月', '四月', '五月', '六月', '七月', '八月', '九月', '十月', '十一月', '十二月'] },
      { name: 'week', label: '星期', min: 0, max: 7, names: ['周日', '周一', '周二', '周三', '周四', '周五', '周六', '周日'] }
    ],
    supportsSpecialChars: ['*', ',', '-', '/'],
    weekStart: 0,
    weekNames: ['周日', '周一', '周二', '周三', '周四', '周五', '周六']
  },
  [CronFormat.QUARTZ]: {
    name: 'Quartz Scheduler',
    description: 'Java Quartz 框架使用的 6-7 位格式，支持秒级精度',
    fields: [
      { name: 'second', label: '秒', min: 0, max: 59 },
      { name: 'minute', label: '分钟', min: 0, max: 59 },
      { name: 'hour', label: '小时', min: 0, max: 23 },
      { name: 'day', label: '日期', min: 1, max: 31 },
      { name: 'month', label: '月份', min: 1, max: 12, names: ['一月', '二月', '三月', '四月', '五月', '六月', '七月', '八月', '九月', '十月', '十一月', '十二月'] },
      { name: 'week', label: '星期', min: 1, max: 7, names: ['周日', '周一', '周二', '周三', '周四', '周五', '周六'] },
      { name: 'year', label: '年', min: 1970, max: 2099, optional: true }
    ],
    supportsSpecialChars: ['*', ',', '-', '/', '?', 'L', 'W', '#', 'C'],
    weekStart: 1,
    weekNames: ['周日', '周一', '周二', '周三', '周四', '周五', '周六']
  },
  [CronFormat.SPRING]: {
    name: 'Spring Scheduler',
    description: 'Spring 框架使用的 6 位格式，支持秒级，默认秒为 0',
    fields: [
      { name: 'second', label: '秒', min: 0, max: 59 },
      { name: 'minute', label: '分钟', min: 0, max: 59 },
      { name: 'hour', label: '小时', min: 0, max: 23 },
      { name: 'day', label: '日期', min: 1, max: 31 },
      { name: 'month', label: '月份', min: 1, max: 12, names: ['一月', '二月', '三月', '四月', '五月', '六月', '七月', '八月', '九月', '十月', '十一月', '十二月'] },
      { name: 'week', label: '星期', min: 0, max: 7, names: ['周日', '周一', '周二', '周三', '周四', '周五', '周六', '周日'] }
    ],
    supportsSpecialChars: ['*', ',', '-', '/', '?', 'L', 'W', '#'],
    weekStart: 0,
    weekNames: ['周日', '周一', '周二', '周三', '周四', '周五', '周六']
  },
  [CronFormat.AWS]: {
    name: 'AWS CloudWatch',
    description: 'AWS CloudWatch Events 使用的 6 位格式，不支持特殊字符',
    fields: [
      { name: 'minute', label: '分钟', min: 0, max: 59 },
      { name: 'hour', label: '小时', min: 0, max: 23 },
      { name: 'day', label: '日期', min: 1, max: 31 },
      { name: 'month', label: '月份', min: 1, max: 12, names: ['一月', '二月', '三月', '四月', '五月', '六月', '七月', '八月', '九月', '十月', '十一月', '十二月'] },
      { name: 'week', label: '星期', min: 1, max: 7, names: ['周一', '周二', '周三', '周四', '周五', '周六', '周日'] },
      { name: 'year', label: '年', min: 1970, max: 2199 }
    ],
    supportsSpecialChars: ['*', ',', '-', '/'],
    weekStart: 1,
    weekNames: ['周一', '周二', '周三', '周四', '周五', '周六', '周日']
  }
}

// 当前选中的格式
const currentFormat = ref<CronFormat>(CronFormat.LINUX)
const currentConfig = computed(() => formatConfigs[currentFormat.value])

// 模式切换
enum ToolMode {
  GENERATOR = 'generator',
  PARSER = 'parser',
  PREVIEW = 'preview'
}

const currentMode = ref<ToolMode>(ToolMode.GENERATOR)

// 生成器相关
const fieldValues = ref<Record<string, string>>({})

// 初始化字段值
const initFieldValues = () => {
  const values: Record<string, string> = {}
  currentConfig.value.fields.forEach(field => {
    values[field.name] = '*'
  })
  fieldValues.value = values
}

// 解析器相关
const parseInput = ref<string>('')
const parseError = ref<string>('')
const parseResult = ref<{
  fields: { name: string; label: string; value: string; description: string }[]
  description: string
} | null>(null)

// 预览相关
const nextExecutions = ref<Date[]>([])
const previewCount = ref<number>(10)

initFieldValues()

// 监听格式变化，重新初始化
watch(currentFormat, () => {
  initFieldValues()
  parseInput.value = ''
  parseError.value = ''
  parseResult.value = null
  nextExecutions.value = []
}, { immediate: true })

// 生成的 Cron 表达式
const generatedCron = computed(() => {
  return currentConfig.value.fields
    .filter(field => !field.optional || fieldValues.value[field.name])
    .map(field => fieldValues.value[field.name] || '*')
    .join(' ')
})

// 常用预设
const presets = [
  { name: '每分钟', linux: '* * * * *', quartz: '0 * * * * *', spring: '0 * * * * *', aws: '* * * * * *' },
  { name: '每5分钟', linux: '*/5 * * * *', quartz: '0 */5 * * * *', spring: '0 */5 * * * *', aws: '*/5 * * * * *' },
  { name: '每15分钟', linux: '*/15 * * * *', quartz: '0 */15 * * * *', spring: '0 */15 * * * *', aws: '*/15 * * * * *' },
  { name: '每30分钟', linux: '*/30 * * * *', quartz: '0 */30 * * * *', spring: '0 */30 * * * *', aws: '*/30 * * * * *' },
  { name: '每小时', linux: '0 * * * *', quartz: '0 0 * * * *', spring: '0 0 * * * *', aws: '0 * * * * *' },
  { name: '每天0点', linux: '0 0 * * *', quartz: '0 0 0 * * *', spring: '0 0 0 * * *', aws: '0 0 * * * *' },
  { name: '每天8点', linux: '0 8 * * *', quartz: '0 0 8 * * *', spring: '0 0 8 * * *', aws: '0 8 * * * *' },
  { name: '每周一0点', linux: '0 0 * * 1', quartz: '0 0 0 * * 1', spring: '0 0 0 * * 1', aws: '0 0 * * 1 *' },
  { name: '每月1号0点', linux: '0 0 1 * *', quartz: '0 0 0 1 * *', spring: '0 0 0 1 * *', aws: '0 0 1 * * *' },
  { name: '每年1月1号0点', linux: '0 0 1 1 *', quartz: '0 0 0 1 1 *', spring: '0 0 0 1 1 *', aws: '0 0 1 1 * *' }
]

const applyPreset = (preset: typeof presets[0]) => {
  const cronValue = preset[currentFormat.value]
  const parts = cronValue.split(' ')
  
  currentConfig.value.fields.forEach((field, index) => {
    if (parts[index]) {
      fieldValues.value[field.name] = parts[index]
    }
  })
}

// 解析 Cron 表达式
const parseCron = () => {
  parseError.value = ''
  parseResult.value = null
  nextExecutions.value = []
  
  if (!parseInput.value.trim()) {
    parseError.value = '请输入 Cron 表达式'
    return
  }
  
  const parts = parseInput.value.trim().split(/\s+/)
  const expectedFields = currentConfig.value.fields.filter(f => !f.optional)
  
  if (parts.length < expectedFields.length) {
    parseError.value = `表达式字段不足，${currentConfig.value.name} 格式需要至少 ${expectedFields.length} 个字段`
    return
  }
  
  if (parts.length > currentConfig.value.fields.length) {
    parseError.value = `表达式字段过多，${currentConfig.value.name} 格式最多 ${currentConfig.value.fields.length} 个字段`
    return
  }
  
  // 验证每个字段
  const fields: { name: string; label: string; value: string; description: string }[] = []
  let description = ''
  
  for (let i = 0; i < currentConfig.value.fields.length; i++) {
    const field = currentConfig.value.fields[i]
    if (!field) continue
    const value = parts[i] || '*'
    
    const fieldDesc = describeField(value, field)
    fields.push({
      name: field.name,
      label: field.label,
      value,
      description: fieldDesc
    })
  }
  
  description = generateDescription(fields)
  
  parseResult.value = { fields, description }
  
  // 计算下次执行时间
  calculateNextExecutions()
}

// 描述单个字段
const describeField = (value: string, field: CronField): string => {
  if (value === '*') return `每${field.label}`
  if (value === '?') return '不指定'
  if (value.startsWith('*/')) {
    const step = value.substring(2)
    return `每${step}${field.label}`
  }
  if (value.includes(',')) {
    const values = value.split(',')
    if (field.names) {
      return values.map(v => field.names?.[parseInt(v) - field.min] || v).join('、')
    }
    return values.join('、')
  }
  if (value.includes('-')) {
    const parts = value.split('-')
    const start = parts[0] || ''
    const end = parts[1] || ''
    if (field.names) {
      const startName = field.names[parseInt(start) - field.min]
      const endName = field.names[parseInt(end) - field.min]
      return `${startName || start}到${endName || end}`
    }
    return `${start}到${end}${field.label}`
  }
  if (field.names) {
    return field.names[parseInt(value) - field.min] || value
  }
  return `${value}${field.label}`
}

// 生成整体描述
const generateDescription = (fields: { name: string; label: string; value: string; description: string }[]): string => {
  const nonStarFields = fields.filter(f => f.value !== '*')
  
  if (nonStarFields.length === 0) {
    return '每分钟执行一次'
  }
  
  // 根据具体模式生成描述
  const minuteField = fields.find(f => f.name === 'minute')
  const hourField = fields.find(f => f.name === 'hour')
  const dayField = fields.find(f => f.name === 'day')
  const monthField = fields.find(f => f.name === 'month')
  const weekField = fields.find(f => f.name === 'week')
  const secondField = fields.find(f => f.name === 'second')
  
  let desc = ''
  
  // 秒
  if (secondField && secondField.value !== '0' && secondField.value !== '*') {
    desc += secondField.description
  }
  
  // 分钟
  if (minuteField) {
    if (minuteField.value === '*') {
      desc += '每分钟'
    } else if (minuteField.value.startsWith('*/')) {
      desc += minuteField.description
    } else if (minuteField.value === '0') {
      desc += '整点'
    } else {
      desc += `${minuteField.value}分`
    }
  }
  
  // 小时
  if (hourField) {
    if (hourField.value === '*') {
      desc += '每小时'
    } else if (hourField.value.startsWith('*/')) {
      desc += hourField.description
    } else {
      desc += `${hourField.value}点`
    }
  }
  
  // 日期/星期
  if (dayField && dayField.value !== '*' && dayField.value !== '?') {
    desc += `${dayField.description}`
  }
  
  if (weekField && weekField.value !== '*' && weekField.value !== '?') {
    desc += `每周${weekField.description}`
  }
  
  // 月份
  if (monthField && monthField.value !== '*') {
    desc += `${monthField.description}`
  }
  
  return desc || '自定义定时规则'
}

// 计算下次执行时间
const calculateNextExecutions = () => {
  nextExecutions.value = []
  
  try {
    const cron = parseInput.value.trim() || generatedCron.value
    if (!cron) return
    
    // 简化的执行时间计算（实际应该使用专门的 cron-parser 库）
    const now = new Date()
    const executions: Date[] = []
    
    // 这里简化处理，实际应该解析 cron 表达式
    for (let i = 0; i < previewCount.value; i++) {
      const next = new Date(now.getTime() + (i + 1) * 60000) // 每分钟一次作为示例
      executions.push(next)
    }
    
    nextExecutions.value = executions
  } catch (e) {
    console.error('计算执行时间失败:', e)
  }
}

// 复制到剪贴板
const copyToClipboard = (text: string) => {
  navigator.clipboard.writeText(text)
}

// 格式转换
const convertFormat = (targetFormat: CronFormat) => {
  const currentCron = generatedCron.value
  // 这里实现格式转换逻辑
  // 简化处理：解析当前表达式，然后生成目标格式的表达式
}
</script>

<template>
  <NuxtLayout name="tool" toolCode="cron-generator">
    <div class="max-w-5xl mx-auto space-y-6">
      <!-- 格式选择 -->
      <ToolCard>
        <div class="flex items-center gap-2 mb-4">
          <Settings class="w-5 h-5 text-rose-500" />
          <h2 class="text-lg font-semibold text-gray-900">选择 Cron 格式</h2>
        </div>
        
        <div class="grid grid-cols-2 md:grid-cols-4 gap-3">
          <button
            v-for="(config, format) in formatConfigs"
            :key="format"
            @click="currentFormat = format as CronFormat"
            class="p-3 rounded-xl border-2 text-left transition-all"
            :class="currentFormat === format
              ? 'border-rose-500 bg-rose-50 text-rose-700'
              : 'border-gray-200 text-gray-600 hover:border-rose-200'"
          >
            <div class="font-medium text-sm">{{ config.name }}</div>
            <div class="text-xs mt-1 opacity-70">{{ config.fields.length }} 位</div>
          </button>
        </div>
        
        <div class="mt-4 p-3 bg-blue-50 rounded-lg">
          <div class="flex items-start gap-2">
            <Info class="w-4 h-4 text-blue-500 mt-0.5" />
            <div class="text-sm text-blue-700">
              <strong>{{ currentConfig.name }}</strong>：{{ currentConfig.description }}
              <div class="mt-1 text-xs">
                支持的字段：{{ currentConfig.fields.map(f => f.label).join('、') }}
              </div>
              <div class="mt-1 text-xs">
                特殊字符：{{ currentConfig.supportsSpecialChars.join(' ') }}
              </div>
            </div>
          </div>
        </div>
      </ToolCard>

      <!-- 模式切换 -->
      <div class="flex gap-2">
        <button
          v-for="mode in [
            { key: ToolMode.GENERATOR, label: '生成器', icon: Settings },
            { key: ToolMode.PARSER, label: '解析器', icon: ArrowRightLeft },
            { key: ToolMode.PREVIEW, label: '执行预览', icon: Calendar }
          ]"
          :key="mode.key"
          @click="currentMode = mode.key"
          class="flex-1 flex items-center justify-center gap-2 py-3 rounded-xl font-medium transition-all"
          :class="currentMode === mode.key
            ? 'bg-linear-to-r from-rose-500 to-pink-600 text-white'
            : 'bg-gray-100 text-gray-600 hover:bg-gray-200'"
        >
          <component :is="mode.icon" class="w-4 h-4" />
          {{ mode.label }}
        </button>
      </div>

      <!-- 生成器模式 -->
      <template v-if="currentMode === ToolMode.GENERATOR">
        <ToolCard>
          <div class="flex items-center gap-2 mb-6">
            <Settings class="w-6 h-6 text-rose-500" />
            <h2 class="text-lg font-semibold text-gray-900">Cron 表达式生成器</h2>
          </div>

          <!-- 字段选择 -->
          <div class="space-y-4">
            <div
              v-for="field in currentConfig.fields"
              :key="field.name"
              class="grid md:grid-cols-3 gap-4 items-center"
            >
              <label class="font-medium text-gray-700">{{ field.label }}</label>
              <div class="md:col-span-2">
                <input
                  v-model="fieldValues[field.name]"
                  type="text"
                  :placeholder="`* (${field.min}-${field.max})`"
                  class="w-full px-4 py-2.5 bg-white border border-gray-200 rounded-xl text-gray-700 focus:outline-none focus:ring-2 focus:ring-rose-500/20 focus:border-rose-500 transition-all"
                />
                <div class="text-xs text-gray-500 mt-1">
                  范围：{{ field.min }}-{{ field.max }}
                  <span v-if="field.names">({{ field.names.slice(0, 3).join('、') }}...)</span>
                </div>
              </div>
            </div>
          </div>

          <!-- 生成的表达式 -->
          <div class="mt-6 p-4 bg-gray-900 rounded-xl">
            <div class="text-sm text-gray-400 mb-2">生成的 Cron 表达式</div>
            <div class="flex items-center gap-3">
              <code class="flex-1 text-xl text-green-400 font-mono">{{ generatedCron }}</code>
              <button
                @click="copyToClipboard(generatedCron)"
                class="px-3 py-1.5 bg-gray-700 text-white text-sm rounded-lg hover:bg-gray-600 transition-colors"
              >
                复制
              </button>
            </div>
          </div>

          <!-- 常用预设 -->
          <div class="mt-6">
            <label class="block text-sm font-medium text-gray-700 mb-3">常用预设</label>
            <div class="flex flex-wrap gap-2">
              <button
                v-for="preset in presets"
                :key="preset.name"
                @click="applyPreset(preset)"
                class="px-3 py-1.5 text-sm bg-gray-100 text-gray-700 rounded-lg hover:bg-rose-100 hover:text-rose-700 transition-colors"
              >
                {{ preset.name }}
              </button>
            </div>
          </div>
        </ToolCard>
      </template>

      <!-- 解析器模式 -->
      <template v-if="currentMode === ToolMode.PARSER">
        <ToolCard>
          <div class="flex items-center gap-2 mb-6">
            <ArrowRightLeft class="w-6 h-6 text-rose-500" />
            <h2 class="text-lg font-semibold text-gray-900">Cron 表达式解析器</h2>
          </div>

          <!-- 输入框 -->
          <div class="mb-6">
            <label class="block text-sm font-medium text-gray-700 mb-2">输入 Cron 表达式</label>
            <div class="flex gap-3">
              <input
                v-model="parseInput"
                type="text"
                placeholder="例如：0 0 * * *"
                class="flex-1 px-4 py-2.5 bg-white border border-gray-200 rounded-xl text-gray-700 focus:outline-none focus:ring-2 focus:ring-rose-500/20 focus:border-rose-500 transition-all"
                @keyup.enter="parseCron"
              />
              <button
                @click="parseCron"
                class="px-6 py-2.5 bg-linear-to-r from-rose-500 to-pink-600 text-white rounded-xl font-medium hover:from-rose-600 hover:to-pink-700 transition-all"
              >
                解析
              </button>
            </div>
            <p class="text-xs text-gray-500 mt-1">
              {{ currentConfig.name }} 格式：{{ currentConfig.fields.map(f => f.label).join(' ') }}
            </p>
          </div>

          <!-- 错误提示 -->
          <div v-if="parseError" class="mb-6 p-4 bg-red-50 rounded-xl flex items-center gap-2">
            <AlertCircle class="w-5 h-5 text-red-500" />
            <span class="text-red-700">{{ parseError }}</span>
          </div>

          <!-- 解析结果 -->
          <div v-if="parseResult" class="space-y-4">
            <div class="p-4 bg-linear-to-r from-rose-50 to-pink-50 rounded-xl">
              <div class="text-sm text-gray-600 mb-1">执行规则</div>
              <div class="text-lg font-semibold text-gray-900">{{ parseResult.description }}</div>
            </div>

            <div class="grid gap-3">
              <div
                v-for="field in parseResult.fields"
                :key="field.name"
                class="flex items-center justify-between p-3 bg-gray-50 rounded-lg"
              >
                <span class="text-sm text-gray-600">{{ field.label }}</span>
                <div class="text-right">
                  <code class="text-sm font-mono text-gray-900">{{ field.value }}</code>
                  <div class="text-xs text-gray-500">{{ field.description }}</div>
                </div>
              </div>
            </div>
          </div>
        </ToolCard>
      </template>

      <!-- 预览模式 -->
      <template v-if="currentMode === ToolMode.PREVIEW">
        <ToolCard>
          <div class="flex items-center gap-2 mb-6">
            <Calendar class="w-6 h-6 text-rose-500" />
            <h2 class="text-lg font-semibold text-gray-900">执行时间预览</h2>
          </div>

          <!-- 输入 -->
          <div class="mb-6">
            <label class="block text-sm font-medium text-gray-700 mb-2">Cron 表达式</label>
            <input
              v-model="parseInput"
              type="text"
              placeholder="例如：0 0 * * *"
              class="w-full px-4 py-2.5 bg-white border border-gray-200 rounded-xl text-gray-700 focus:outline-none focus:ring-2 focus:ring-rose-500/20 focus:border-rose-500 transition-all"
            />
          </div>

          <!-- 预览数量 -->
          <div class="mb-6">
            <label class="block text-sm font-medium text-gray-700 mb-2">预览次数</label>
            <div class="flex gap-2">
              <button
                v-for="count in [5, 10, 20, 50]"
                :key="count"
                @click="previewCount = count; calculateNextExecutions()"
                class="px-4 py-2 rounded-lg text-sm font-medium transition-all"
                :class="previewCount === count
                  ? 'bg-rose-500 text-white'
                  : 'bg-gray-100 text-gray-600 hover:bg-gray-200'"
              >
                {{ count }}次
              </button>
            </div>
          </div>

          <!-- 执行时间列表 -->
          <div v-if="nextExecutions.length > 0" class="space-y-2">
            <div
              v-for="(time, index) in nextExecutions"
              :key="index"
              class="flex items-center gap-4 p-3 bg-gray-50 rounded-lg"
            >
              <span class="w-8 h-8 flex items-center justify-center bg-rose-100 text-rose-600 rounded-full text-sm font-medium">
                {{ index + 1 }}
              </span>
              <span class="font-mono text-gray-900">{{ time.toLocaleString('zh-CN') }}</span>
              <span class="text-sm text-gray-500">
                {{ Math.ceil((time.getTime() - Date.now()) / 60000) }} 分钟后
              </span>
            </div>
          </div>

          <div v-else class="text-center py-8 text-gray-500">
            请输入有效的 Cron 表达式查看执行时间
          </div>
        </ToolCard>
      </template>

      <!-- 格式说明 -->
      <ToolCard>
        <h3 class="text-base font-semibold text-gray-900 mb-4 flex items-center gap-2">
          <Info class="w-5 h-5 text-rose-500" />
          Cron 格式说明
        </h3>

        <div class="space-y-6 text-sm">
          <!-- 可视化结构图 -->
          <div class="p-4 bg-gray-900 rounded-xl overflow-x-auto">
            <div class="font-medium text-gray-300 mb-3">{{ currentConfig.name }} 表达式结构</div>
            <pre class="text-xs md:text-sm font-mono text-green-400 leading-relaxed">
<span v-if="currentFormat === CronFormat.LINUX">
┌───────────── 分钟 (0-59)
│ ┌───────────── 小时 (0-23)
│ │ ┌───────────── 日期 (1-31)
│ │ │ ┌───────────── 月份 (1-12)
│ │ │ │ ┌───────────── 星期 (0-7, 0和7都是周日)
│ │ │ │ │
* * * * *</span>
<span v-else-if="currentFormat === CronFormat.QUARTZ">
┌───────────── 秒 (0-59)
│ ┌───────────── 分钟 (0-59)
│ │ ┌───────────── 小时 (0-23)
│ │ │ ┌───────────── 日期 (1-31)
│ │ │ │ ┌───────────── 月份 (1-12)
│ │ │ │ │ ┌───────────── 星期 (1-7, 1=周日)
│ │ │ │ │ │ ┌───────────── 年 (可选, 1970-2099)
│ │ │ │ │ │ │
* * * * * * ?</span>
<span v-else-if="currentFormat === CronFormat.SPRING">
┌───────────── 秒 (0-59)
│ ┌───────────── 分钟 (0-59)
│ │ ┌───────────── 小时 (0-23)
│ │ │ ┌───────────── 日期 (1-31)
│ │ │ │ ┌───────────── 月份 (1-12)
│ │ │ │ │ ┌───────────── 星期 (0-7, 0=周日)
│ │ │ │ │ │
0 * * * * *</span>
<span v-else-if="currentFormat === CronFormat.AWS">
┌───────────── 分钟 (0-59)
│ ┌───────────── 小时 (0-23)
│ │ ┌───────────── 日期 (1-31)
│ │ │ ┌───────────── 月份 (1-12)
│ │ │ │ ┌───────────── 星期 (1-7, 1=周一)
│ │ │ │ │ ┌───────────── 年 (1970-2199)
│ │ │ │ │ │
* * * * * *</span>
            </pre>
          </div>

          <div class="grid md:grid-cols-2 gap-4">
            <div class="p-3 bg-gray-50 dark:bg-gray-700 rounded-lg">
              <div class="font-medium text-gray-900 dark:text-gray-100 mb-2">Linux / Unix (5位)</div>
              <code class="text-xs bg-gray-200 dark:bg-gray-600 px-2 py-1 rounded text-gray-800 dark:text-gray-200">分 时 日 月 周</code>
              <div class="text-gray-600 dark:text-gray-300 mt-2">最通用的格式，广泛用于 Linux/Unix 系统的 crontab</div>
            </div>

            <div class="p-3 bg-gray-50 dark:bg-gray-700 rounded-lg">
              <div class="font-medium text-gray-900 dark:text-gray-100 mb-2">Quartz (6-7位)</div>
              <code class="text-xs bg-gray-200 dark:bg-gray-600 px-2 py-1 rounded text-gray-800 dark:text-gray-200">秒 分 时 日 月 周 [年]</code>
              <div class="text-gray-600 dark:text-gray-300 mt-2">Java Quartz 框架使用，支持秒级精度和年字段</div>
            </div>

            <div class="p-3 bg-gray-50 dark:bg-gray-700 rounded-lg">
              <div class="font-medium text-gray-900 dark:text-gray-100 mb-2">Spring (6位)</div>
              <code class="text-xs bg-gray-200 dark:bg-gray-600 px-2 py-1 rounded text-gray-800 dark:text-gray-200">秒 分 时 日 月 周</code>
              <div class="text-gray-600 dark:text-gray-300 mt-2">Spring 框架使用，支持秒级，默认秒为 0</div>
            </div>

            <div class="p-3 bg-gray-50 dark:bg-gray-700 rounded-lg">
              <div class="font-medium text-gray-900 dark:text-gray-100 mb-2">AWS CloudWatch (6位)</div>
              <code class="text-xs bg-gray-200 dark:bg-gray-600 px-2 py-1 rounded text-gray-800 dark:text-gray-200">分 时 日 月 周 年</code>
              <div class="text-gray-600 dark:text-gray-300 mt-2">AWS 使用，不支持 ? L W # 等特殊字符</div>
            </div>
          </div>

          <div class="border-t border-gray-200 dark:border-gray-600 pt-4">
            <div class="font-medium text-gray-900 dark:text-white mb-2 text-base">特殊字符说明</div>
            <div class="grid grid-cols-2 md:grid-cols-4 gap-2 text-xs">
              <div class="p-2 bg-gray-50 dark:bg-gray-700 rounded text-gray-700 dark:text-gray-300"><code class="text-rose-600 dark:text-rose-400">*</code> 任意值</div>
              <div class="p-2 bg-gray-50 dark:bg-gray-700 rounded text-gray-700 dark:text-gray-300"><code class="text-rose-600 dark:text-rose-400">,</code> 列表分隔</div>
              <div class="p-2 bg-gray-50 dark:bg-gray-700 rounded text-gray-700 dark:text-gray-300"><code class="text-rose-600 dark:text-rose-400">-</code> 范围</div>
              <div class="p-2 bg-gray-50 dark:bg-gray-700 rounded text-gray-700 dark:text-gray-300"><code class="text-rose-600 dark:text-rose-400">/</code> 步进</div>
              <div class="p-2 bg-gray-50 dark:bg-gray-700 rounded text-gray-700 dark:text-gray-300"><code class="text-rose-600 dark:text-rose-400">?</code> 不指定（Quartz/Spring）</div>
              <div class="p-2 bg-gray-50 dark:bg-gray-700 rounded text-gray-700 dark:text-gray-300"><code class="text-rose-600 dark:text-rose-400">L</code> 最后（Quartz/Spring）</div>
              <div class="p-2 bg-gray-50 dark:bg-gray-700 rounded text-gray-700 dark:text-gray-300"><code class="text-rose-600 dark:text-rose-400">W</code> 最近工作日（Quartz/Spring）</div>
              <div class="p-2 bg-gray-50 dark:bg-gray-700 rounded text-gray-700 dark:text-gray-300"><code class="text-rose-600 dark:text-rose-400">#</code> 第N个星期X（Quartz/Spring）</div>
            </div>
          </div>
        </div>
      </ToolCard>
    </div>
  </NuxtLayout>
</template>
