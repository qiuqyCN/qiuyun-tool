<script setup lang="ts">
import { ref, computed } from 'vue'
import { Calendar, Clock, CalendarDays, CalendarClock, ChevronDown } from 'lucide-vue-next'
import ToolInput from '@/components/ui/ToolInput.vue'
import ToolCard from '@/components/ui/ToolCard.vue'

useHead({
  title: '日期计算器 - 秋云工具',
  meta: [
    { name: 'description', content: '计算日期差、推算未来/过去日期、工作日计算、年龄计算等功能' }
  ]
})

enum CalcMode {
  DIFF = 'diff',
  ADD = 'add',
  SUB = 'sub',
  WORKDAY = 'workday',
  AGE = 'age'
}

const currentMode = ref<CalcMode>(CalcMode.DIFF)

// 日期差计算
const startDate = ref<string>('')
const endDate = ref<string>('')

// 日期加减
const baseDate = ref<string>('')
const daysToAdd = ref<number>(0)
const monthsToAdd = ref<number>(0)
const yearsToAdd = ref<number>(0)

// 工作日计算
const workStartDate = ref<string>('')
const workDays = ref<number>(0)
const includeEndDay = ref<boolean>(true)

// 年龄计算
const birthDate = ref<string>('')
const calcDate = ref<string>('')

const today = new Date().toISOString().split('T')[0] as string

// 初始化日期
startDate.value = today
endDate.value = today
baseDate.value = today
workStartDate.value = today
birthDate.value = today
calcDate.value = today

const dateDiffResult = computed(() => {
  if (!startDate.value || !endDate.value) return null

  const start = new Date(startDate.value)
  const end = new Date(endDate.value)

  if (isNaN(start.getTime()) || isNaN(end.getTime())) return null

  const diffTime = end.getTime() - start.getTime()
  const diffDays = Math.floor(diffTime / (1000 * 60 * 60 * 24))
  const diffWeeks = Math.floor(diffDays / 7)
  const diffMonths = (end.getFullYear() - start.getFullYear()) * 12 + (end.getMonth() - start.getMonth())

  let preciseMonths = diffMonths
  const dayDiff = end.getDate() - start.getDate()
  if (dayDiff < 0) {
    preciseMonths--
  }

  const diffYears = Math.floor(preciseMonths / 12)
  const remainingMonths = preciseMonths % 12

  return {
    days: diffDays,
    weeks: diffWeeks,
    months: preciseMonths,
    years: diffYears,
    remainingMonths: remainingMonths,
    isFuture: diffDays > 0
  }
})

const addDateResult = computed(() => {
  if (!baseDate.value) return null

  const date = new Date(baseDate.value)
  if (isNaN(date.getTime())) return null

  const result = new Date(date)
  result.setFullYear(result.getFullYear() + yearsToAdd.value)
  result.setMonth(result.getMonth() + monthsToAdd.value)
  result.setDate(result.getDate() + daysToAdd.value)

  return {
    result: result.toISOString().split('T')[0],
    weekday: result.toLocaleDateString('zh-CN', { weekday: 'long' }),
    formatted: result.toLocaleDateString('zh-CN', { year: 'numeric', month: 'long', day: 'numeric' })
  }
})

const isWorkday = (date: Date): boolean => {
  const day = date.getDay()
  return day !== 0 && day !== 6
}

const workdayResult = computed(() => {
  if (!workStartDate.value || workDays.value < 0) return null

  const start = new Date(workStartDate.value)
  if (isNaN(start.getTime())) return null

  let current = new Date(start)
  let workdayCount = 0
  const targetDays = includeEndDay.value ? workDays.value - 1 : workDays.value

  while (workdayCount < targetDays) {
    current.setDate(current.getDate() + 1)
    if (isWorkday(current)) {
      workdayCount++
    }
  }

  const totalDays = Math.floor((current.getTime() - start.getTime()) / (1000 * 60 * 60 * 24))
  let weekendDays = 0
  const tempDate = new Date(start)
  while (tempDate <= current) {
    if (!isWorkday(tempDate)) weekendDays++
    tempDate.setDate(tempDate.getDate() + 1)
  }

  return {
    endDate: current.toISOString().split('T')[0],
    formatted: current.toLocaleDateString('zh-CN', { year: 'numeric', month: 'long', day: 'numeric' }),
    weekday: current.toLocaleDateString('zh-CN', { weekday: 'long' }),
    totalDays: totalDays + 1,
    weekendDays: weekendDays
  }
})

const ageResult = computed(() => {
  if (!birthDate.value || !calcDate.value) return null

  const birth = new Date(birthDate.value)
  const calc = new Date(calcDate.value)

  if (isNaN(birth.getTime()) || isNaN(calc.getTime())) return null

  let years = calc.getFullYear() - birth.getFullYear()
  let months = calc.getMonth() - birth.getMonth()
  let days = calc.getDate() - birth.getDate()

  if (days < 0) {
    months--
    const prevMonth = new Date(calc.getFullYear(), calc.getMonth(), 0)
    days += prevMonth.getDate()
  }
  if (months < 0) {
    years--
    months += 12
  }

  const totalDays = Math.floor((calc.getTime() - birth.getTime()) / (1000 * 60 * 60 * 24))

  const nextBirthday = new Date(calc.getFullYear(), birth.getMonth(), birth.getDate())
  if (nextBirthday < calc) {
    nextBirthday.setFullYear(nextBirthday.getFullYear() + 1)
  }
  const daysToNextBirthday = Math.floor((nextBirthday.getTime() - calc.getTime()) / (1000 * 60 * 60 * 24))

  const zodiacAnimals = ['猴', '鸡', '狗', '猪', '鼠', '牛', '虎', '兔', '龙', '蛇', '马', '羊']
  const zodiac = zodiacAnimals[birth.getFullYear() % 12]

  const constellations = [
    { name: '摩羯座', start: [1, 1], end: [1, 19] },
    { name: '水瓶座', start: [1, 20], end: [2, 18] },
    { name: '双鱼座', start: [2, 19], end: [3, 20] },
    { name: '白羊座', start: [3, 21], end: [4, 19] },
    { name: '金牛座', start: [4, 20], end: [5, 20] },
    { name: '双子座', start: [5, 21], end: [6, 21] },
    { name: '巨蟹座', start: [6, 22], end: [7, 22] },
    { name: '狮子座', start: [7, 23], end: [8, 22] },
    { name: '处女座', start: [8, 23], end: [9, 22] },
    { name: '天秤座', start: [9, 23], end: [10, 23] },
    { name: '天蝎座', start: [10, 24], end: [11, 22] },
    { name: '射手座', start: [11, 23], end: [12, 21] },
    { name: '摩羯座', start: [12, 22], end: [12, 31] }
  ]

  const month = birth.getMonth() + 1
  const day = birth.getDate()
  const constellation = constellations.find(c => {
    const startM = c.start[0] ?? 0
    const startD = c.start[1] ?? 0
    const endM = c.end[0] ?? 0
    const endD = c.end[1] ?? 0
    if (startM === endM) {
      return month === startM && day >= startD && day <= endD
    }
    return (month === startM && day >= startD) || (month === endM && day <= endD)
  })

  return {
    years,
    months,
    days,
    totalDays,
    daysToNextBirthday,
    zodiac,
    constellation: constellation?.name || ''
  }
})

const modes = [
  { key: CalcMode.DIFF, label: '日期差', icon: Calendar },
  { key: CalcMode.ADD, label: '日期推算', icon: CalendarDays },
  { key: CalcMode.WORKDAY, label: '工作日', icon: CalendarClock },
  { key: CalcMode.AGE, label: '年龄计算', icon: Clock }
]
</script>

<template>
  <NuxtLayout name="tool" toolCode="date-calculator">
    <div class="max-w-4xl mx-auto space-y-6">
      <!-- 模式选择 -->
      <ToolCard>
        <h2 class="text-lg font-semibold text-gray-900 mb-4">选择计算类型</h2>
        <div class="grid grid-cols-2 sm:grid-cols-4 gap-3">
          <button
            v-for="mode in modes"
            :key="mode.key"
            @click="currentMode = mode.key"
            class="flex items-center justify-center gap-2 p-3 rounded-xl transition-all duration-200"
            :class="currentMode === mode.key
              ? 'bg-linear-to-br from-rose-500 to-pink-600 text-white shadow-lg shadow-rose-200'
              : 'bg-gray-50 text-gray-600 hover:bg-rose-50 hover:text-rose-600'"
          >
            <component :is="mode.icon" class="w-5 h-5" />
            <span class="font-medium">{{ mode.label }}</span>
          </button>
        </div>
      </ToolCard>

      <!-- 日期差计算 -->
      <ToolCard v-if="currentMode === CalcMode.DIFF">
        <div class="flex items-center gap-2 mb-6">
          <Calendar class="w-6 h-6 text-rose-500" />
          <h2 class="text-lg font-semibold text-gray-900">日期差计算</h2>
        </div>

        <div class="grid md:grid-cols-2 gap-6">
          <div class="space-y-4">
            <div>
              <label class="block text-sm font-medium text-gray-700 mb-2">开始日期</label>
              <input
                v-model="startDate"
                type="date"
                class="w-full px-4 py-2.5 bg-white border border-gray-200 rounded-xl text-gray-700 focus:outline-none focus:ring-2 focus:ring-rose-500/20 focus:border-rose-500 transition-all"
              />
            </div>
            <div>
              <label class="block text-sm font-medium text-gray-700 mb-2">结束日期</label>
              <input
                v-model="endDate"
                type="date"
                class="w-full px-4 py-2.5 bg-white border border-gray-200 rounded-xl text-gray-700 focus:outline-none focus:ring-2 focus:ring-rose-500/20 focus:border-rose-500 transition-all"
              />
            </div>
          </div>

          <div v-if="dateDiffResult" class="p-4 bg-gradient-to-r from-rose-50 to-pink-50 rounded-xl">
            <div class="text-center mb-4">
              <div class="text-sm text-gray-500">相差天数</div>
              <div class="text-4xl font-bold text-rose-600">
                {{ Math.abs(dateDiffResult.days) }}
                <span class="text-lg">天</span>
              </div>
              <div class="text-sm text-gray-500 mt-1">
                {{ dateDiffResult.isFuture ? '结束日期在开始日期之后' : '结束日期在开始日期之前' }}
              </div>
            </div>
            <div class="grid grid-cols-2 gap-3 text-center">
              <div class="p-2 bg-white rounded-lg">
                <div class="text-xs text-gray-500">周数</div>
                <div class="font-semibold text-gray-900">{{ Math.abs(dateDiffResult.weeks) }} 周</div>
              </div>
              <div class="p-2 bg-white rounded-lg">
                <div class="text-xs text-gray-500">月数</div>
                <div class="font-semibold text-gray-900">{{ Math.abs(dateDiffResult.months) }} 个月</div>
              </div>
              <div class="p-2 bg-white rounded-lg col-span-2">
                <div class="text-xs text-gray-500">年数月数</div>
                <div class="font-semibold text-gray-900">
                  {{ Math.abs(dateDiffResult.years) }} 年 {{ Math.abs(dateDiffResult.remainingMonths) }} 个月
                </div>
              </div>
            </div>
          </div>
        </div>
      </ToolCard>

      <!-- 日期推算 -->
      <ToolCard v-if="currentMode === CalcMode.ADD">
        <div class="flex items-center gap-2 mb-6">
          <CalendarDays class="w-6 h-6 text-rose-500" />
          <h2 class="text-lg font-semibold text-gray-900">日期推算</h2>
        </div>

        <div class="grid md:grid-cols-2 gap-6">
          <div class="space-y-4">
            <div>
              <label class="block text-sm font-medium text-gray-700 mb-2">基准日期</label>
              <input
                v-model="baseDate"
                type="date"
                class="w-full px-4 py-2.5 bg-white border border-gray-200 rounded-xl text-gray-700 focus:outline-none focus:ring-2 focus:ring-rose-500/20 focus:border-rose-500 transition-all"
              />
            </div>
            <div class="grid grid-cols-3 gap-3">
              <div>
                <label class="block text-xs font-medium text-gray-600 mb-1">年</label>
                <ToolInput v-model="yearsToAdd" type="number" placeholder="0" />
              </div>
              <div>
                <label class="block text-xs font-medium text-gray-600 mb-1">月</label>
                <ToolInput v-model="monthsToAdd" type="number" placeholder="0" />
              </div>
              <div>
                <label class="block text-xs font-medium text-gray-600 mb-1">日</label>
                <ToolInput v-model="daysToAdd" type="number" placeholder="0" />
              </div>
            </div>
            <p class="text-xs text-gray-500">正值表示未来日期，负值表示过去日期</p>
          </div>

          <div v-if="addDateResult" class="p-4 bg-gradient-to-r from-rose-50 to-pink-50 rounded-xl">
            <div class="text-center">
              <div class="text-sm text-gray-500 mb-2">推算结果</div>
              <div class="text-2xl font-bold text-gray-900">{{ addDateResult.formatted }}</div>
              <div class="text-rose-600 font-medium mt-1">{{ addDateResult.weekday }}</div>
              <div class="mt-3 p-2 bg-white rounded-lg">
                <div class="text-xs text-gray-500">标准格式</div>
                <div class="font-mono text-gray-900">{{ addDateResult.result }}</div>
              </div>
            </div>
          </div>
        </div>
      </ToolCard>

      <!-- 工作日计算 -->
      <ToolCard v-if="currentMode === CalcMode.WORKDAY">
        <div class="flex items-center gap-2 mb-6">
          <CalendarClock class="w-6 h-6 text-rose-500" />
          <h2 class="text-lg font-semibold text-gray-900">工作日计算</h2>
        </div>

        <div class="grid md:grid-cols-2 gap-6">
          <div class="space-y-4">
            <div>
              <label class="block text-sm font-medium text-gray-700 mb-2">开始日期</label>
              <input
                v-model="workStartDate"
                type="date"
                class="w-full px-4 py-2.5 bg-white border border-gray-200 rounded-xl text-gray-700 focus:outline-none focus:ring-2 focus:ring-rose-500/20 focus:border-rose-500 transition-all"
              />
            </div>
            <div>
              <label class="block text-sm font-medium text-gray-700 mb-2">工作日数量</label>
              <ToolInput v-model="workDays" type="number" placeholder="请输入工作日数" min="1" />
            </div>
            <div class="flex items-center gap-2">
              <input
                v-model="includeEndDay"
                type="checkbox"
                id="includeEnd"
                class="w-4 h-4 text-rose-500 border-gray-300 rounded focus:ring-rose-500"
              />
              <label for="includeEnd" class="text-sm text-gray-600">包含结束当天</label>
            </div>
          </div>

          <div v-if="workdayResult" class="p-4 bg-gradient-to-r from-rose-50 to-pink-50 rounded-xl">
            <div class="text-center mb-4">
              <div class="text-sm text-gray-500">结束日期</div>
              <div class="text-2xl font-bold text-gray-900">{{ workdayResult.formatted }}</div>
              <div class="text-rose-600 font-medium mt-1">{{ workdayResult.weekday }}</div>
            </div>
            <div class="grid grid-cols-2 gap-3">
              <div class="p-2 bg-white rounded-lg text-center">
                <div class="text-xs text-gray-500">总天数</div>
                <div class="font-semibold text-gray-900">{{ workdayResult.totalDays }} 天</div>
              </div>
              <div class="p-2 bg-white rounded-lg text-center">
                <div class="text-xs text-gray-500">周末天数</div>
                <div class="font-semibold text-gray-900">{{ workdayResult.weekendDays }} 天</div>
              </div>
            </div>
          </div>
        </div>
      </ToolCard>

      <!-- 年龄计算 -->
      <ToolCard v-if="currentMode === CalcMode.AGE">
        <div class="flex items-center gap-2 mb-6">
          <Clock class="w-6 h-6 text-rose-500" />
          <h2 class="text-lg font-semibold text-gray-900">年龄计算</h2>
        </div>

        <div class="grid md:grid-cols-2 gap-6">
          <div class="space-y-4">
            <div>
              <label class="block text-sm font-medium text-gray-700 mb-2">出生日期</label>
              <input
                v-model="birthDate"
                type="date"
                class="w-full px-4 py-2.5 bg-white border border-gray-200 rounded-xl text-gray-700 focus:outline-none focus:ring-2 focus:ring-rose-500/20 focus:border-rose-500 transition-all"
              />
            </div>
            <div>
              <label class="block text-sm font-medium text-gray-700 mb-2">计算日期</label>
              <input
                v-model="calcDate"
                type="date"
                class="w-full px-4 py-2.5 bg-white border border-gray-200 rounded-xl text-gray-700 focus:outline-none focus:ring-2 focus:ring-rose-500/20 focus:border-rose-500 transition-all"
              />
              <p class="text-xs text-gray-500 mt-1">默认为今天，可修改计算特定日期的年龄</p>
            </div>
          </div>

          <div v-if="ageResult" class="p-4 bg-gradient-to-r from-rose-50 to-pink-50 rounded-xl">
            <div class="text-center mb-4">
              <div class="text-sm text-gray-500">年龄</div>
              <div class="text-4xl font-bold text-rose-600">
                {{ ageResult.years }}
                <span class="text-xl">岁</span>
              </div>
              <div class="text-gray-600 mt-1">
                {{ ageResult.months }} 个月 {{ ageResult.days }} 天
              </div>
            </div>
            <div class="space-y-2">
              <div class="p-2 bg-white rounded-lg flex justify-between">
                <span class="text-sm text-gray-500">总天数</span>
                <span class="font-medium text-gray-900">{{ ageResult.totalDays.toLocaleString() }} 天</span>
              </div>
              <div class="p-2 bg-white rounded-lg flex justify-between">
                <span class="text-sm text-gray-500">距离下次生日</span>
                <span class="font-medium text-rose-600">{{ ageResult.daysToNextBirthday }} 天</span>
              </div>
              <div class="grid grid-cols-2 gap-2">
                <div class="p-2 bg-white rounded-lg text-center">
                  <div class="text-xs text-gray-500">生肖</div>
                  <div class="font-semibold text-gray-900">{{ ageResult.zodiac }}</div>
                </div>
                <div class="p-2 bg-white rounded-lg text-center">
                  <div class="text-xs text-gray-500">星座</div>
                  <div class="font-semibold text-gray-900">{{ ageResult.constellation }}</div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </ToolCard>
    </div>
  </NuxtLayout>
</template>
