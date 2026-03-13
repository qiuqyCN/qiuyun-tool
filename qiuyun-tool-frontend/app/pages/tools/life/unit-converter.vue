<script setup lang="ts">
import { ref, computed, watch } from 'vue'
import { ArrowRightLeft, Ruler, Weight, Thermometer, Gauge, Clock, Database } from 'lucide-vue-next'
import { ToolInput } from '@/components/ui/tool-input'
import { ToolButton } from '@/components/ui/tool-button'
import { ToolCard } from '@/components/ui/tool-card'

useHead({
  title: '综合单位换算 - 秋云工具',
  meta: [
    { name: 'description', content: '支持长度、重量、温度、压力、面积、体积、时间、数据存储等多种单位换算' }
  ]
})

enum Category {
  LENGTH = 'length',
  WEIGHT = 'weight',
  TEMPERATURE = 'temperature',
  PRESSURE = 'pressure',
  AREA = 'area',
  VOLUME = 'volume',
  TIME = 'time',
  DATA = 'data'
}

interface Unit {
  name: string
  symbol: string
  toBase: number
  fromBase?: (val: number) => number
}

const categories: Record<Category, { name: string; icon: any; units: Record<string, Unit> }> = {
  [Category.LENGTH]: {
    name: '长度',
    icon: Ruler,
    units: {
      m: { name: '米', symbol: 'm', toBase: 1 },
      km: { name: '千米', symbol: 'km', toBase: 1000 },
      cm: { name: '厘米', symbol: 'cm', toBase: 0.01 },
      mm: { name: '毫米', symbol: 'mm', toBase: 0.001 },
      um: { name: '微米', symbol: 'μm', toBase: 0.000001 },
      nm: { name: '纳米', symbol: 'nm', toBase: 0.000000001 },
      inch: { name: '英寸', symbol: 'in', toBase: 0.0254 },
      ft: { name: '英尺', symbol: 'ft', toBase: 0.3048 },
      yd: { name: '码', symbol: 'yd', toBase: 0.9144 },
      mi: { name: '英里', symbol: 'mi', toBase: 1609.344 },
      nmi: { name: '海里', symbol: 'nmi', toBase: 1852 },
      li: { name: '里', symbol: '里', toBase: 500 },
      zhang: { name: '丈', symbol: '丈', toBase: 3.333333 },
      chi: { name: '尺', symbol: '尺', toBase: 0.333333 },
      cun: { name: '寸', symbol: '寸', toBase: 0.033333 }
    }
  },
  [Category.WEIGHT]: {
    name: '重量',
    icon: Weight,
    units: {
      kg: { name: '千克', symbol: 'kg', toBase: 1 },
      g: { name: '克', symbol: 'g', toBase: 0.001 },
      mg: { name: '毫克', symbol: 'mg', toBase: 0.000001 },
      t: { name: '吨', symbol: 't', toBase: 1000 },
      lb: { name: '磅', symbol: 'lb', toBase: 0.453592 },
      oz: { name: '盎司', symbol: 'oz', toBase: 0.0283495 },
      jin: { name: '斤', symbol: '斤', toBase: 0.5 },
      liang: { name: '两', symbol: '两', toBase: 0.05 },
      dan: { name: '担', symbol: '担', toBase: 50 }
    }
  },
  [Category.TEMPERATURE]: {
    name: '温度',
    icon: Thermometer,
    units: {
      c: {
        name: '摄氏度',
        symbol: '°C',
        toBase: 1,
        fromBase: (val) => val
      },
      f: {
        name: '华氏度',
        symbol: '°F',
        toBase: 1,
        fromBase: (val) => (val * 9 / 5) + 32
      },
      k: {
        name: '开尔文',
        symbol: 'K',
        toBase: 1,
        fromBase: (val) => val + 273.15
      },
      r: {
        name: '兰氏度',
        symbol: '°R',
        toBase: 1,
        fromBase: (val) => (val + 273.15) * 9 / 5
      }
    }
  },
  [Category.PRESSURE]: {
    name: '压力',
    icon: Gauge,
    units: {
      pa: { name: '帕斯卡', symbol: 'Pa', toBase: 1 },
      kpa: { name: '千帕', symbol: 'kPa', toBase: 1000 },
      mpa: { name: '兆帕', symbol: 'MPa', toBase: 1000000 },
      bar: { name: '巴', symbol: 'bar', toBase: 100000 },
      mbar: { name: '毫巴', symbol: 'mbar', toBase: 100 },
      atm: { name: '标准大气压', symbol: 'atm', toBase: 101325 },
      mmhg: { name: '毫米汞柱', symbol: 'mmHg', toBase: 133.322 },
      psi: { name: '磅力/平方英寸', symbol: 'psi', toBase: 6894.76 },
      torr: { name: '托', symbol: 'Torr', toBase: 133.322 }
    }
  },
  [Category.AREA]: {
    name: '面积',
    icon: Ruler,
    units: {
      m2: { name: '平方米', symbol: 'm²', toBase: 1 },
      km2: { name: '平方千米', symbol: 'km²', toBase: 1000000 },
      cm2: { name: '平方厘米', symbol: 'cm²', toBase: 0.0001 },
      mm2: { name: '平方毫米', symbol: 'mm²', toBase: 0.000001 },
      ha: { name: '公顷', symbol: 'ha', toBase: 10000 },
      mu: { name: '亩', symbol: '亩', toBase: 666.667 },
      qing: { name: '顷', symbol: '顷', toBase: 66666.7 },
      acre: { name: '英亩', symbol: 'acre', toBase: 4046.86 },
      ft2: { name: '平方英尺', symbol: 'ft²', toBase: 0.092903 },
      in2: { name: '平方英寸', symbol: 'in²', toBase: 0.00064516 }
    }
  },
  [Category.VOLUME]: {
    name: '体积',
    icon: Database,
    units: {
      m3: { name: '立方米', symbol: 'm³', toBase: 1 },
      l: { name: '升', symbol: 'L', toBase: 0.001 },
      ml: { name: '毫升', symbol: 'mL', toBase: 0.000001 },
      cm3: { name: '立方厘米', symbol: 'cm³', toBase: 0.000001 },
      dm3: { name: '立方分米', symbol: 'dm³', toBase: 0.001 },
      gal_us: { name: '美制加仑', symbol: 'gal(US)', toBase: 0.00378541 },
      gal_uk: { name: '英制加仑', symbol: 'gal(UK)', toBase: 0.00454609 },
      oz_us: { name: '美制液盎司', symbol: 'fl oz(US)', toBase: 0.0000295735 },
      pt_us: { name: '美制品脱', symbol: 'pt(US)', toBase: 0.000473176 },
      qt_us: { name: '美制夸脱', symbol: 'qt(US)', toBase: 0.000946353 }
    }
  },
  [Category.TIME]: {
    name: '时间',
    icon: Clock,
    units: {
      s: { name: '秒', symbol: 's', toBase: 1 },
      min: { name: '分钟', symbol: 'min', toBase: 60 },
      h: { name: '小时', symbol: 'h', toBase: 3600 },
      d: { name: '天', symbol: 'd', toBase: 86400 },
      w: { name: '周', symbol: 'w', toBase: 604800 },
      mo: { name: '月(30天)', symbol: 'mo', toBase: 2592000 },
      y: { name: '年(365天)', symbol: 'y', toBase: 31536000 },
      ms: { name: '毫秒', symbol: 'ms', toBase: 0.001 },
      us: { name: '微秒', symbol: 'μs', toBase: 0.000001 },
      ns: { name: '纳米', symbol: 'ns', toBase: 0.000000001 }
    }
  },
  [Category.DATA]: {
    name: '数据存储',
    icon: Database,
    units: {
      b: { name: '字节', symbol: 'B', toBase: 1 },
      kb: { name: '千字节', symbol: 'KB', toBase: 1024 },
      mb: { name: '兆字节', symbol: 'MB', toBase: 1048576 },
      gb: { name: '吉字节', symbol: 'GB', toBase: 1073741824 },
      tb: { name: '太字节', symbol: 'TB', toBase: 1099511627776 },
      pb: { name: '拍字节', symbol: 'PB', toBase: 1125899906842624 },
      bit: { name: '比特', symbol: 'bit', toBase: 0.125 },
      kbit: { name: '千比特', symbol: 'Kbit', toBase: 128 },
      mbit: { name: '兆比特', symbol: 'Mbit', toBase: 131072 },
      gbit: { name: '吉比特', symbol: 'Gbit', toBase: 134217728 }
    }
  }
}

const currentCategory = ref<Category>(Category.LENGTH)
const inputValue = ref<number>(1)
const fromUnit = ref<string>('')
const toUnit = ref<string>('')

const currentUnits = computed(() => {
  return categories[currentCategory.value].units
})

const unitKeys = computed(() => Object.keys(currentUnits.value))

watch(currentCategory, (newCategory) => {
  const keys = Object.keys(categories[newCategory].units)
  fromUnit.value = keys[0] || ''
  toUnit.value = keys[1] || keys[0] || ''
}, { immediate: true })

const convertTemperature = (value: number, from: string, to: string): number => {
  let celsius: number

  switch (from) {
    case 'c': celsius = value; break
    case 'f': celsius = (value - 32) * 5 / 9; break
    case 'k': celsius = value - 273.15; break
    case 'r': celsius = (value - 491.67) * 5 / 9; break
    default: celsius = value
  }

  switch (to) {
    case 'c': return celsius
    case 'f': return celsius * 9 / 5 + 32
    case 'k': return celsius + 273.15
    case 'r': return (celsius + 273.15) * 9 / 5
    default: return celsius
  }
}

const result = computed(() => {
  if (!inputValue.value || isNaN(inputValue.value)) return 0

  const fromUnitKey = fromUnit.value
  const toUnitKey = toUnit.value
  const from = fromUnitKey ? currentUnits.value[fromUnitKey] : null
  const to = toUnitKey ? currentUnits.value[toUnitKey] : null

  if (!from || !to) return 0

  if (currentCategory.value === Category.TEMPERATURE) {
    return convertTemperature(inputValue.value, fromUnitKey, toUnitKey)
  }

  const baseValue = inputValue.value * from.toBase
  return baseValue / to.toBase
})

const formattedResult = computed(() => {
  if (result.value === 0) return '0'

  const absValue = Math.abs(result.value)
  if (absValue >= 1000000000 || (absValue < 0.001 && absValue > 0)) {
    return result.value.toExponential(6)
  }
  return result.value.toLocaleString('zh-CN', { maximumFractionDigits: 10 })
})

const swapUnits = () => {
  const temp = fromUnit.value
  fromUnit.value = toUnit.value
  toUnit.value = temp
}

const quickConversions = computed(() => {
  const conversions: { unit: string; value: number; name: string; symbol: string }[] = []
  const units = currentUnits.value

  Object.entries(units).forEach(([key, unit]) => {
    if (key !== fromUnit.value) {
      let convertedValue: number
      if (currentCategory.value === Category.TEMPERATURE) {
        convertedValue = convertTemperature(inputValue.value, fromUnit.value, key)
      } else {
        const from = units[fromUnit.value]
        if (!from) return
        const baseValue = inputValue.value * from.toBase
        convertedValue = baseValue / unit.toBase
      }

      conversions.push({
        unit: key,
        value: convertedValue,
        name: unit.name,
        symbol: unit.symbol
      })
    }
  })

  return conversions
})

const formatQuickValue = (value: number): string => {
  const absValue = Math.abs(value)
  if (absValue >= 1000000 || (absValue < 0.001 && absValue > 0)) {
    return value.toExponential(4)
  }
  return value.toLocaleString('zh-CN', { maximumFractionDigits: 6 })
}
</script>

<template>
  <NuxtLayout name="tool" toolCode="unit-converter">
    <div class="max-w-4xl mx-auto space-y-6">
      <!-- 分类选择 -->
      <ToolCard>
        <h2 class="text-lg font-semibold text-gray-900 mb-4">选择换算类型</h2>
        <div class="grid grid-cols-4 sm:grid-cols-8 gap-2">
          <button
            v-for="(cat, key) in categories"
            :key="key"
            @click="currentCategory = key"
            class="flex flex-col items-center justify-center p-3 rounded-xl transition-all duration-200"
            :class="currentCategory === key
              ? 'bg-linear-to-br from-rose-500 to-pink-600 text-white shadow-lg shadow-rose-200'
              : 'bg-gray-50 text-gray-600 hover:bg-rose-50 hover:text-rose-600'"
          >
            <component :is="cat.icon" class="w-5 h-5 mb-1" />
            <span class="text-xs font-medium">{{ cat.name }}</span>
          </button>
        </div>
      </ToolCard>

      <!-- 换算器 -->
      <ToolCard>
        <div class="flex items-center gap-2 mb-6">
          <component :is="categories[currentCategory].icon" class="w-6 h-6 text-rose-500" />
          <h2 class="text-lg font-semibold text-gray-900">
            {{ categories[currentCategory].name }}换算
          </h2>
        </div>

        <div class="grid md:grid-cols-[1fr,auto,1fr] gap-4 items-end">
          <!-- 输入 -->
          <div class="space-y-2">
            <label class="text-sm font-medium text-gray-700">输入值</label>
            <ToolInput
              v-model.number="inputValue"
              type="number"
              placeholder="请输入数值"
            />
            <select
              v-model="fromUnit"
              class="w-full px-4 py-2.5 bg-white border border-gray-200 rounded-xl text-gray-700 focus:outline-none focus:ring-2 focus:ring-rose-500/20 focus:border-rose-500 transition-all"
            >
              <option
                v-for="(unit, key) in currentUnits"
                :key="key"
                :value="key"
              >
                {{ unit.name }} ({{ unit.symbol }})
              </option>
            </select>
          </div>

          <!-- 交换按钮 -->
          <div class="flex justify-center md:pb-8">
            <button
              @click="swapUnits"
              class="p-3 rounded-xl bg-rose-50 text-rose-600 hover:bg-rose-100 transition-colors"
            >
              <ArrowRightLeft class="w-5 h-5" />
            </button>
          </div>

          <!-- 输出 -->
          <div class="space-y-2">
            <label class="text-sm font-medium text-gray-700">换算结果</label>
            <div class="px-4 py-2.5 bg-rose-50 border border-rose-100 rounded-xl text-rose-700 font-mono text-lg min-h-[46px] flex items-center">
              {{ formattedResult }}
            </div>
            <select
              v-model="toUnit"
              class="w-full px-4 py-2.5 bg-white border border-gray-200 rounded-xl text-gray-700 focus:outline-none focus:ring-2 focus:ring-rose-500/20 focus:border-rose-500 transition-all"
            >
              <option
                v-for="(unit, key) in currentUnits"
                :key="key"
                :value="key"
              >
                {{ unit.name }} ({{ unit.symbol }})
              </option>
            </select>
          </div>
        </div>

        <!-- 结果展示 -->
        <div class="mt-6 p-4 bg-linear-to-r from-rose-50 to-pink-50 rounded-xl">
          <div class="text-center">
            <div class="text-sm text-gray-500 mb-1">换算结果</div>
            <div class="text-3xl font-bold text-gray-900">
              {{ inputValue }} {{ currentUnits[fromUnit]?.symbol }}
              <span class="text-rose-500 mx-2">=</span>
              {{ formattedResult }} {{ currentUnits[toUnit]?.symbol }}
            </div>
          </div>
        </div>
      </ToolCard>

      <!-- 快速换算表 -->
      <ToolCard>
        <h3 class="text-base font-semibold text-gray-900 mb-4">快速换算参考</h3>
        <div class="grid grid-cols-2 sm:grid-cols-3 md:grid-cols-4 gap-3">
          <div
            v-for="conv in quickConversions"
            :key="conv.unit"
            class="p-3 bg-gray-50 rounded-lg"
          >
            <div class="text-xs text-gray-500 mb-1">{{ conv.name }}</div>
            <div class="font-mono text-sm text-gray-900 truncate">
              {{ formatQuickValue(conv.value) }} {{ conv.symbol }}
            </div>
          </div>
        </div>
      </ToolCard>
    </div>
  </NuxtLayout>
</template>
