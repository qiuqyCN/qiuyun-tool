<script setup lang="ts">
import { ref, computed } from 'vue'
import { Calendar, Clock, Sparkles, Gift, Star, Moon, Sun } from 'lucide-vue-next'
import { ToolCard } from '@/components/ui/tool-card'

useHead({
  title: '年龄计算器 - 秋云工具',
  meta: [
    { name: 'description', content: '精确计算周岁年龄、农历生日转换、生肖星座、下一个生日等信息' }
  ]
})

enum DateType {
  SOLAR = 'solar',
  LUNAR = 'lunar'
}

const birthDateType = ref<DateType>(DateType.SOLAR)
const birthDate = ref<string>('')
const calcDate = ref<string>('')

const now = new Date()
const today = now.toISOString().split('T')[0] as string

birthDate.value = today
calcDate.value = today

// 农历数据表
const lunarInfo = [
  0x04bd8, 0x04ae0, 0x0a570, 0x054d5, 0x0d260, 0x0d950, 0x16554, 0x056a0, 0x09ad0, 0x055d2,
  0x04ae0, 0x0a5b6, 0x0a4d0, 0x0d250, 0x1d255, 0x0b540, 0x0d6a0, 0x0ada2, 0x095b0, 0x14977,
  0x04970, 0x0a4b0, 0x0b4b5, 0x06a50, 0x06d40, 0x1ab54, 0x02b60, 0x09570, 0x052f2, 0x04970,
  0x06566, 0x0d4a0, 0x0ea50, 0x06e95, 0x05ad0, 0x02b60, 0x186e3, 0x092e0, 0x1c8d7, 0x0c950,
  0x0d4a0, 0x1d8a6, 0x0b550, 0x056a0, 0x1a5b4, 0x025d0, 0x092d0, 0x0d2b2, 0x0a950, 0x0b557,
  0x06ca0, 0x0b550, 0x15355, 0x04da0, 0x0a5d0, 0x14573, 0x052d0, 0x0a9a8, 0x0e950, 0x06aa0,
  0x0aea6, 0x0ab50, 0x04b60, 0x0aae4, 0x0a570, 0x05260, 0x0f263, 0x0d950, 0x05b57, 0x056a0,
  0x096d0, 0x04dd5, 0x04ad0, 0x0a4d0, 0x0d4d4, 0x0d250, 0x0d558, 0x0b540, 0x0b5a0, 0x195a6,
  0x095b0, 0x049b0, 0x0a974, 0x0a4b0, 0x0b27a, 0x06a50, 0x06d40, 0x0af46, 0x0ab60, 0x09570,
  0x04af5, 0x04970, 0x064b0, 0x074a3, 0x0ea50, 0x06b58, 0x055c0, 0x0ab60, 0x096d5, 0x092e0,
  0x0c960, 0x0d954, 0x0d4a0, 0x0da50, 0x07552, 0x056a0, 0x0abb7, 0x025d0, 0x092d0, 0x0cab5,
  0x0a950, 0x0b4a0, 0x0baa4, 0x0ad50, 0x055d9, 0x04ba0, 0x0a5b0, 0x15176, 0x052b0, 0x0a930,
  0x07954, 0x06aa0, 0x0ad50, 0x05b52, 0x04b60, 0x0a6e6, 0x0a4e0, 0x0d260, 0x0ea65, 0x0d530,
  0x05aa0, 0x076a3, 0x096d0, 0x04bd7, 0x04ad0, 0x0a4d0, 0x1d0b6, 0x0d250, 0x0d520, 0x0dd45,
  0x0b5a0, 0x056d0, 0x055b2, 0x049b0, 0x0a577, 0x0a4b0, 0x0aa50, 0x1b255, 0x06d20, 0x0ada0
]

const Gan = ['甲', '乙', '丙', '丁', '戊', '己', '庚', '辛', '壬', '癸']
const Zhi = ['子', '丑', '寅', '卯', '辰', '巳', '午', '未', '申', '酉', '戌', '亥']
const Animals = ['鼠', '牛', '虎', '兔', '龙', '蛇', '马', '羊', '猴', '鸡', '狗', '猪']
const lunarMonths = ['正', '二', '三', '四', '五', '六', '七', '八', '九', '十', '冬', '腊']
const lunarDays = ['初一', '初二', '初三', '初四', '初五', '初六', '初七', '初八', '初九', '初十',
  '十一', '十二', '十三', '十四', '十五', '十六', '十七', '十八', '十九', '二十',
  '廿一', '廿二', '廿三', '廿四', '廿五', '廿六', '廿七', '廿八', '廿九', '三十']

// 农历转公历
function lunarToSolar(lunarYear: number, lunarMonth: number, lunarDay: number, isLeap: boolean = false): Date | null {
  const baseDate = new Date(1900, 0, 31)
  let offset = 0

  for (let i = 1900; i < lunarYear; i++) {
    offset += lYearDays(i)
  }

  let leapMonth = leapMonthOfYear(lunarYear)
  for (let i = 1; i < lunarMonth; i++) {
    if (i === leapMonth && isLeap) break
    offset += monthDays(lunarYear, i)
  }

  if (isLeap && lunarMonth === leapMonth) {
    offset += monthDays(lunarYear, lunarMonth)
  }

  offset += lunarDay - 1

  const result = new Date(baseDate.getTime() + offset * 86400000)
  return result
}

// 公历转农历
function solarToLunar(date: Date): { year: number; month: number; day: number; isLeap: boolean; yearGanZhi: string; animal: string; monthName: string; dayName: string } | null {
  const baseDate = new Date(1900, 0, 31)
  const offset = Math.floor((date.getTime() - baseDate.getTime()) / 86400000)

  let year = 1900
  let days = offset
  let temp = 0

  for (year = 1900; year < 2050 && days > 0; year++) {
    temp = lYearDays(year)
    days -= temp
  }

  if (days < 0) {
    days += temp
    year--
  }

  const lunarYear = year
  const leap = leapMonthOfYear(lunarYear)
  let isLeap = false
  let month = 1

  for (month = 1; month < 13 && days > 0; month++) {
    if (leap > 0 && month === leap + 1 && !isLeap) {
      month--
      isLeap = true
      temp = leapDays(lunarYear)
    } else {
      temp = monthDays(lunarYear, month)
    }

    if (isLeap && month === leap + 1) isLeap = false
    days -= temp
  }

  if (days === 0 && leap > 0 && month === leap + 1) {
    if (isLeap) {
      isLeap = false
    } else {
      isLeap = true
      month--
    }
  }

  if (days < 0) {
    days += temp
    month--
  }

  const lunarMonth = month
  const lunarDay = days + 1

  const gan = Gan[(lunarYear - 4) % 10]
  const zhi = Zhi[(lunarYear - 4) % 12]
  const yearGanZhi = (gan || '') + (zhi || '')
  const animal = Animals[(lunarYear - 4) % 12] || ''
  const monthName = (isLeap ? '闰' : '') + (lunarMonths[lunarMonth - 1] || '') + '月'
  const dayName = lunarDays[lunarDay - 1] || ''

  return {
    year: lunarYear,
    month: lunarMonth,
    day: lunarDay,
    isLeap,
    yearGanZhi,
    animal,
    monthName,
    dayName
  }
}

function lYearDays(y: number): number {
  let sum = 348
  const info = lunarInfo[y - 1900]
  if (info === undefined) return sum
  for (let i = 0x8000; i > 0x8; i >>= 1) {
    sum += (info & i) ? 1 : 0
  }
  return sum + leapDays(y)
}

function leapDays(y: number): number {
  if (leapMonthOfYear(y)) {
    const info = lunarInfo[y - 1900]
    if (info === undefined) return 30
    return (info & 0x10000) ? 30 : 29
  }
  return 0
}

function leapMonthOfYear(y: number): number {
  const info = lunarInfo[y - 1900]
  if (info === undefined) return 0
  return info & 0xf
}

function monthDays(y: number, m: number): number {
  const info = lunarInfo[y - 1900]
  if (info === undefined) return 30
  return (info & (0x10000 >> m)) ? 30 : 29
}

// 农历输入
const lunarYear = ref<number>(2000)
const lunarMonth = ref<number>(1)
const lunarDay = ref<number>(1)
const isLeapMonth = ref<boolean>(false)

// 获取实际出生日期（公历）
const actualBirthDate = computed(() => {
  if (birthDateType.value === DateType.SOLAR) {
    return birthDate.value ? new Date(birthDate.value) : null
  } else {
    // 农历转公历
    return lunarToSolar(lunarYear.value, lunarMonth.value, lunarDay.value, isLeapMonth.value)
  }
})

// 农历显示
const lunarDisplay = computed(() => {
  const date = actualBirthDate.value
  if (!date) return null
  return solarToLunar(date)
})

const ageResult = computed(() => {
  const birth = actualBirthDate.value
  if (!birth || !calcDate.value) return null

  const calc = new Date(calcDate.value)

  if (isNaN(birth.getTime()) || isNaN(calc.getTime())) return null

  // 计算年月日
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

  // 计算总天数、小时、分钟
  const diffTime = calc.getTime() - birth.getTime()
  const totalDays = Math.floor(diffTime / (1000 * 60 * 60 * 24))
  const totalHours = Math.floor(diffTime / (1000 * 60 * 60))
  const totalMinutes = Math.floor(diffTime / (1000 * 60))
  const totalWeeks = Math.floor(totalDays / 7)

  // 生肖（使用农历年）
  const lunar = solarToLunar(birth)
  const zodiac = lunar ? lunar.animal : Animals[(birth.getFullYear() - 4) % 12]

  // 星座
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

  // 下一个公历生日
  const nextSolarBirthday = new Date(calc.getFullYear(), birth.getMonth(), birth.getDate())
  if (nextSolarBirthday <= calc) {
    nextSolarBirthday.setFullYear(nextSolarBirthday.getFullYear() + 1)
  }
  const daysToNextSolarBirthday = Math.floor((nextSolarBirthday.getTime() - calc.getTime()) / (1000 * 60 * 60 * 24))

  // 下一个农历生日
  const currentLunar = solarToLunar(calc)
  let nextLunarBirthday = null
  let daysToNextLunarBirthday = 0

  if (currentLunar && lunar) {
    // 查找下一个农历生日
    for (let y = currentLunar.year; y <= currentLunar.year + 1; y++) {
      const leapMonth = leapMonthOfYear(y)
      let targetMonth = lunar.month
      let targetIsLeap = lunar.isLeap

      // 处理闰月情况
      if (lunar.isLeap && leapMonth !== lunar.month) {
        // 原生日是闰月，但今年没有该闰月，则使用普通月
        targetIsLeap = false
      }

      const solarDate = lunarToSolar(y, targetMonth, lunar.day, targetIsLeap)
      if (solarDate && solarDate > calc) {
        nextLunarBirthday = solarDate
        daysToNextLunarBirthday = Math.floor((solarDate.getTime() - calc.getTime()) / (1000 * 60 * 60 * 24))
        break
      }
    }
  }

  // 生命进度 (假设寿命80岁)
  const lifeProgress = Math.min(100, Math.round((years / 80) * 100))

  return {
    years,
    months,
    days,
    totalDays,
    totalHours,
    totalMinutes,
    totalWeeks,
    zodiac,
    constellation: constellation?.name || '',
    nextSolarBirthday: nextSolarBirthday.toLocaleDateString('zh-CN'),
    daysToNextSolarBirthday,
    nextLunarBirthday: nextLunarBirthday?.toLocaleDateString('zh-CN') || '',
    daysToNextLunarBirthday,
    lifeProgress,
    lunarYearGanZhi: lunar?.yearGanZhi || '',
    lunarDisplay: lunar
  }
})

// 生成年份选项（1900-2050）
const yearOptions = computed(() => {
  const years = []
  for (let i = 1900; i <= 2050; i++) {
    years.push(i)
  }
  return years
})

// 月份选项
const monthOptions = computed(() => {
  return [1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12]
})

// 日期选项
const dayOptions = computed(() => {
  const days = []
  const maxDay = monthDays(lunarYear.value, lunarMonth.value)
  for (let i = 1; i <= maxDay; i++) {
    days.push(i)
  }
  return days
})

// 当前年份是否有闰月
const currentYearLeapMonth = computed(() => {
  return leapMonthOfYear(lunarYear.value)
})
</script>

<template>
  <NuxtLayout name="tool" toolCode="age-calculator">
    <div class="max-w-4xl mx-auto space-y-6">
      <!-- 输入区域 -->
      <ToolCard>
        <div class="flex items-center gap-2 mb-6">
          <Calendar class="w-6 h-6 text-rose-500" />
          <h2 class="text-lg font-semibold text-gray-900">年龄计算器</h2>
        </div>

        <!-- 日期类型选择 -->
        <div class="mb-6">
          <label class="block text-sm font-medium text-gray-700 mb-2">出生日期类型</label>
          <div class="grid grid-cols-2 gap-3">
            <button
              @click="birthDateType = DateType.SOLAR"
              class="flex items-center justify-center gap-2 p-3 rounded-xl border-2 transition-all"
              :class="birthDateType === DateType.SOLAR
                ? 'border-rose-500 bg-rose-50 text-rose-700'
                : 'border-gray-200 text-gray-600 hover:border-rose-200'"
            >
              <Sun class="w-5 h-5" />
              <span class="font-medium">公历（阳历）</span>
            </button>
            <button
              @click="birthDateType = DateType.LUNAR"
              class="flex items-center justify-center gap-2 p-3 rounded-xl border-2 transition-all"
              :class="birthDateType === DateType.LUNAR
                ? 'border-rose-500 bg-rose-50 text-rose-700'
                : 'border-gray-200 text-gray-600 hover:border-rose-200'"
            >
              <Moon class="w-5 h-5" />
              <span class="font-medium">农历（阴历）</span>
            </button>
          </div>
        </div>

        <div class="grid md:grid-cols-2 gap-6">
          <!-- 公历输入 -->
          <div v-if="birthDateType === DateType.SOLAR" class="space-y-4">
            <div>
              <label class="block text-sm font-medium text-gray-700 mb-2">出生日期（公历）</label>
              <input
                v-model="birthDate"
                type="date"
                class="w-full px-4 py-2.5 bg-white border border-gray-200 rounded-xl text-gray-700 focus:outline-none focus:ring-2 focus:ring-rose-500/20 focus:border-rose-500 transition-all"
              />
            </div>
          </div>

          <!-- 农历输入 -->
          <div v-else class="space-y-4">
            <div>
              <label class="block text-sm font-medium text-gray-700 mb-2">出生日期（农历）</label>
              <div class="grid grid-cols-3 gap-2">
                <select
                  v-model="lunarYear"
                  class="px-3 py-2.5 bg-white border border-gray-200 rounded-xl text-gray-700 focus:outline-none focus:ring-2 focus:ring-rose-500/20 focus:border-rose-500 transition-all"
                >
                  <option v-for="year in yearOptions" :key="year" :value="year">{{ year }}年</option>
                </select>
                <select
                  v-model="lunarMonth"
                  class="px-3 py-2.5 bg-white border border-gray-200 rounded-xl text-gray-700 focus:outline-none focus:ring-2 focus:ring-rose-500/20 focus:border-rose-500 transition-all"
                >
                  <option v-for="month in monthOptions" :key="month" :value="month">{{ lunarMonths[month - 1] }}月</option>
                </select>
                <select
                  v-model="lunarDay"
                  class="px-3 py-2.5 bg-white border border-gray-200 rounded-xl text-gray-700 focus:outline-none focus:ring-2 focus:ring-rose-500/20 focus:border-rose-500 transition-all"
                >
                  <option v-for="day in dayOptions" :key="day" :value="day">{{ lunarDays[day - 1] }}</option>
                </select>
              </div>
              <!-- 闰月选择 -->
              <div v-if="currentYearLeapMonth === lunarMonth" class="mt-2">
                <label class="flex items-center gap-2">
                  <input
                    v-model="isLeapMonth"
                    type="checkbox"
                    class="w-4 h-4 text-rose-500 border-gray-300 rounded focus:ring-rose-500"
                  />
                  <span class="text-sm text-gray-600">闰月</span>
                </label>
              </div>
            </div>
          </div>

          <div class="space-y-4">
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
        </div>

        <!-- 农历显示 -->
        <div v-if="ageResult?.lunarDisplay" class="mt-4 p-4 bg-linear-to-r from-amber-50 to-yellow-50 rounded-xl">
          <div class="flex items-center gap-2 text-amber-700">
            <Moon class="w-5 h-5" />
            <span class="font-medium">农历信息</span>
          </div>
          <div class="mt-2 text-amber-800">
            <span class="text-lg font-bold">{{ ageResult.lunarDisplay.yearGanZhi }}年</span>
            <span class="mx-2">·</span>
            <span>{{ ageResult.lunarDisplay.monthName }}</span>
            <span>{{ ageResult.lunarDisplay.dayName }}</span>
            <span class="mx-2">·</span>
            <span>属{{ ageResult.lunarDisplay.animal }}</span>
          </div>
        </div>
      </ToolCard>

      <!-- 主要结果 -->
      <ToolCard v-if="ageResult">
        <div class="text-center mb-6">
          <div class="text-sm text-gray-500 mb-2">您的年龄</div>
          <div class="text-5xl font-bold text-rose-600">
            {{ ageResult.years }}
            <span class="text-2xl">岁</span>
          </div>
          <div class="text-gray-600 mt-2">
            {{ ageResult.months }} 个月 {{ ageResult.days }} 天
          </div>
        </div>

        <div class="grid grid-cols-2 md:grid-cols-4 gap-4">
          <div class="p-4 bg-gray-50 rounded-xl text-center">
            <div class="text-xs text-gray-500 mb-1">总天数</div>
            <div class="text-xl font-bold text-gray-900">{{ ageResult.totalDays.toLocaleString() }}</div>
          </div>
          <div class="p-4 bg-gray-50 rounded-xl text-center">
            <div class="text-xs text-gray-500 mb-1">总周数</div>
            <div class="text-xl font-bold text-gray-900">{{ ageResult.totalWeeks.toLocaleString() }}</div>
          </div>
          <div class="p-4 bg-gray-50 rounded-xl text-center">
            <div class="text-xs text-gray-500 mb-1">总小时</div>
            <div class="text-xl font-bold text-gray-900">{{ ageResult.totalHours.toLocaleString() }}</div>
          </div>
          <div class="p-4 bg-gray-50 rounded-xl text-center">
            <div class="text-xs text-gray-500 mb-1">总分钟</div>
            <div class="text-xl font-bold text-gray-900">{{ ageResult.totalMinutes.toLocaleString() }}</div>
          </div>
        </div>

        <!-- 生命进度条 -->
        <div class="mt-6 p-4 bg-linear-to-r from-rose-50 to-pink-50 rounded-xl">
          <div class="flex items-center justify-between mb-2">
            <span class="text-sm font-medium text-gray-700 flex items-center gap-1">
              <Clock class="w-4 h-4" />
              人生进度 (假设80岁)
            </span>
            <span class="text-sm font-bold text-rose-600">{{ ageResult.lifeProgress }}%</span>
          </div>
          <div class="h-3 bg-white rounded-full overflow-hidden">
            <div
              class="h-full bg-linear-to-r from-rose-400 to-pink-500 rounded-full transition-all duration-500"
              :style="{ width: ageResult.lifeProgress + '%' }"
            ></div>
          </div>
        </div>
      </ToolCard>

      <!-- 生肖星座 -->
      <ToolCard v-if="ageResult">
        <h3 class="text-base font-semibold text-gray-900 mb-4 flex items-center gap-2">
          <Sparkles class="w-5 h-5 text-rose-500" />
          生肖与星座
        </h3>
        <div class="grid md:grid-cols-2 gap-4">
          <div class="p-4 bg-linear-to-br from-red-50 to-orange-50 rounded-xl text-center">
            <div class="text-sm text-gray-500 mb-2">生肖</div>
            <div class="text-4xl font-bold text-red-600">{{ ageResult.zodiac }}</div>
            <div v-if="ageResult.lunarYearGanZhi" class="text-sm text-gray-500 mt-1">{{ ageResult.lunarYearGanZhi }}年</div>
          </div>
          <div class="p-4 bg-linear-to-br from-purple-50 to-indigo-50 rounded-xl text-center">
            <div class="text-sm text-gray-500 mb-2">星座</div>
            <div class="text-2xl font-bold text-purple-600">{{ ageResult.constellation }}</div>
          </div>
        </div>
      </ToolCard>

      <!-- 下一个生日 -->
      <ToolCard v-if="ageResult">
        <h3 class="text-base font-semibold text-gray-900 mb-4 flex items-center gap-2">
          <Gift class="w-5 h-5 text-rose-500" />
          下一个生日
        </h3>
        <div class="grid md:grid-cols-2 gap-4">
          <!-- 公历生日 -->
          <div class="p-6 bg-linear-to-r from-rose-50 to-pink-50 rounded-xl">
            <div class="flex items-center gap-2 mb-3">
              <Sun class="w-5 h-5 text-rose-500" />
              <span class="font-medium text-gray-700">公历生日</span>
            </div>
            <div class="text-2xl font-bold text-gray-900">{{ ageResult.nextSolarBirthday }}</div>
            <div class="text-rose-600 mt-1">距离还有 {{ ageResult.daysToNextSolarBirthday }} 天</div>
          </div>
          <!-- 农历生日 -->
          <div class="p-6 bg-linear-to-r from-amber-50 to-yellow-50 rounded-xl">
            <div class="flex items-center gap-2 mb-3">
              <Moon class="w-5 h-5 text-amber-600" />
              <span class="font-medium text-gray-700">农历生日</span>
            </div>
            <div class="text-2xl font-bold text-gray-900">{{ ageResult.nextLunarBirthday || '无闰月' }}</div>
            <div v-if="ageResult.nextLunarBirthday" class="text-amber-600 mt-1">
              距离还有 {{ ageResult.daysToNextLunarBirthday }} 天
            </div>
          </div>
        </div>
      </ToolCard>
    </div>
  </NuxtLayout>
</template>
