<script setup lang="ts">
import { ref, computed } from 'vue'
import { 
  Banknote, 
  Copy, 
  RotateCcw, 
  History,
  CheckCircle2,
  Calculator
} from 'lucide-vue-next'
import { ToolCard } from '@/components/ui/tool-card'
import { ToolButton } from '@/components/ui/tool-button'
import { ToolInput } from '@/components/ui/tool-input'

useHead({
  title: '数字转中文大写 - 秋云工具',
  meta: [
    { name: 'description', content: '将阿拉伯数字转换为中文大写金额，支持财务票据、合同金额等场景' }
  ]
})

// 输入状态
const inputNumber = ref<string>('')
const showRMB = ref<boolean>(true)
const copySuccess = ref<boolean>(false)
const history = ref<string[]>([])

// 中文数字映射
const chineseDigits = ['零', '壹', '贰', '叁', '肆', '伍', '陆', '柒', '捌', '玖']
const chineseUnits = ['', '拾', '佰', '仟']
const chineseBigUnits = ['', '万', '亿', '万亿']

// 金额单位
const rmbUnits = ['元', '角', '分']

// 转换数字为中文大写
const numberToChinese = (numStr: string, isRMB: boolean = false): string => {
  // 去除前后空格
  numStr = numStr.trim()
  if (!numStr || numStr === '') return ''
  
  // 处理负数
  const isNegative = numStr.startsWith('-')
  if (isNegative) {
    numStr = numStr.substring(1)
  }
  
  // 验证数字格式
  if (!/^\d+(\.\d+)?$/.test(numStr)) return ''
  
  // 处理零
  if (parseFloat(numStr) === 0) {
    return isRMB ? '零元整' : '零'
  }
  
  if (isRMB) {
    // 金额格式转换
    const result = convertToRMB(numStr)
    return (isNegative ? '负' : '') + result
  } else {
    // 普通数字转换
    const parts = numStr.split('.')
    const integerPart = parseInt(parts[0], 10) || 0
    const result = convertToChinese(integerPart)
    
    // 处理小数部分
    if (parts[1]) {
      return (isNegative ? '负' : '') + result + '点' + convertDecimalStr(parts[1])
    }
    return (isNegative ? '负' : '') + result
  }
}

// 转换为人民币大写
const convertToRMB = (numStr: string): string => {
  // 分割整数和小数部分
  const parts = numStr.split('.')
  const integerPart = parts[0] || '0'
  const decimalPart = (parts[1] || '').padEnd(2, '0').slice(0, 2)
  
  const yuan = parseInt(integerPart, 10) || 0
  const jiao = parseInt(decimalPart[0] || '0', 10)
  const fen = parseInt(decimalPart[1] || '0', 10)
  
  let result = ''
  
  // 转换元部分
  if (yuan > 0) {
    result += convertToChinese(yuan) + '元'
  } else if (jiao === 0 && fen === 0) {
    return '零元整'
  } else {
    result += '零'
  }
  
  // 转换角部分
  if (jiao > 0) {
    result += chineseDigits[jiao] + '角'
  }
  
  // 转换分部分
  if (fen > 0) {
    result += chineseDigits[fen] + '分'
  }
  
  // 如果没有角和分，添加"整"
  if (jiao === 0 && fen === 0) {
    result += '整'
  }
  
  return result
}

// 转换整数部分
const convertToChinese = (num: number): string => {
  if (num === 0) return '零'
  
  let result = ''
  let unitIndex = 0
  
  while (num > 0) {
    const segment = num % 10000
    if (segment > 0) {
      const segmentStr = convertSegment(segment)
      result = segmentStr + chineseBigUnits[unitIndex] + result
    }
    num = Math.floor(num / 10000)
    unitIndex++
  }
  
  // 清理连续的零
  result = result.replace(/零+/g, '零')
  result = result.replace(/零$/, '')
  
  return result || '零'
}

// 转换四位以内的数字
const convertSegment = (num: number): string => {
  let result = ''
  let zeroFlag = false
  
  for (let i = 0; i < 4; i++) {
    const digit = num % 10
    if (digit === 0) {
      if (!zeroFlag && result !== '') {
        zeroFlag = true
      }
    } else {
      if (zeroFlag) {
        result = '零' + result
        zeroFlag = false
      }
      result = chineseDigits[digit] + chineseUnits[i] + result
    }
    num = Math.floor(num / 10)
  }
  
  return result
}

// 转换小数部分（字符串版本，避免精度问题）
const convertDecimalStr = (decimalStr: string): string => {
  let result = ''
  for (let i = 0; i < decimalStr.length && i < 10; i++) {
    const digit = parseInt(decimalStr[i], 10)
    if (!isNaN(digit)) {
      result += chineseDigits[digit]
    }
  }
  return result
}

// 计算结果
const result = computed(() => {
  if (!inputNumber.value || inputNumber.value.trim() === '') return ''
  return numberToChinese(inputNumber.value, showRMB.value)
})

// 阿拉伯数字格式化显示
const formattedNumber = computed(() => {
  if (!inputNumber.value) return ''
  const num = parseFloat(inputNumber.value)
  if (isNaN(num)) return inputNumber.value
  
  if (showRMB.value) {
    return '¥' + num.toLocaleString('zh-CN', {
      minimumFractionDigits: 2,
      maximumFractionDigits: 2
    })
  }
  return num.toLocaleString('zh-CN')
})

// 复制结果
const copyResult = async () => {
  if (!result.value) return
  
  try {
    await navigator.clipboard.writeText(result.value)
    copySuccess.value = true
    addToHistory(result.value)
    setTimeout(() => copySuccess.value = false, 2000)
  } catch {
    // 复制失败
  }
}

// 添加到历史记录
const addToHistory = (text: string) => {
  if (!text) return
  history.value = [text, ...history.value.filter(h => h !== text)].slice(0, 5)
}

// 重置
const reset = () => {
  inputNumber.value = ''
  copySuccess.value = false
}

// 使用历史记录
const useHistory = (text: string) => {
  // 从历史记录中提取数字（简单处理）
  const match = text.match(/[零壹贰叁肆伍陆柒捌玖]+/g)
  if (match) {
    // 这里简化处理，实际应该反向转换
    inputNumber.value = ''
  }
}

// 示例数据
const examples = [
  { num: '12345.67', desc: '发票金额' },
  { num: '1000000', desc: '合同金额' },
  { num: '0.88', desc: '零头金额' },
  { num: '999999999.99', desc: '大额金额' }
]

// 使用示例
const useExample = (num: string) => {
  inputNumber.value = num
}
</script>

<template>
  <NuxtLayout name="tool" toolCode="number-to-chinese">
    <div class="max-w-4xl mx-auto space-y-6">
      <!-- 输入区域 -->
      <ToolCard>
        <div class="flex items-center gap-2 mb-6">
          <Banknote class="w-6 h-6 text-rose-500" />
          <h2 class="text-lg font-semibold text-gray-900">数字转中文大写</h2>
        </div>

        <div class="space-y-6">
          <!-- 模式切换 -->
          <div class="flex items-center gap-4">
            <label class="flex items-center gap-2 cursor-pointer">
              <input 
                type="radio" 
                v-model="showRMB" 
                :value="true"
                class="w-4 h-4 text-rose-500 focus:ring-rose-500"
              />
              <span class="text-sm text-gray-700">金额格式（元角分）</span>
            </label>
            <label class="flex items-center gap-2 cursor-pointer">
              <input 
                type="radio" 
                v-model="showRMB" 
                :value="false"
                class="w-4 h-4 text-rose-500 focus:ring-rose-500"
              />
              <span class="text-sm text-gray-700">普通数字</span>
            </label>
          </div>

          <!-- 数字输入 -->
          <div>
            <label class="block text-sm font-medium text-gray-700 mb-2">
              输入数字
            </label>
            <ToolInput
              v-model="inputNumber"
              type="text"
              :placeholder="showRMB ? '请输入金额，如：12345.67' : '请输入数字，如：12345'"
              class="text-lg"
            />
          </div>

          <!-- 结果显示 -->
          <div v-if="result" class="bg-gradient-to-r from-rose-50 to-pink-50 rounded-xl p-6 border border-rose-100">
            <div class="flex items-center justify-between mb-3">
              <span class="text-sm text-gray-600">转换结果</span>
              <ToolButton 
                variant="secondary" 
                size="sm"
                @click="copyResult"
              >
                <Copy class="w-4 h-4 mr-1" />
                {{ copySuccess ? '已复制' : '复制' }}
              </ToolButton>
            </div>
            
            <!-- 原始数字 -->
            <div class="text-sm text-gray-500 mb-2">
              {{ formattedNumber }}
            </div>
            
            <!-- 中文大写 -->
            <div class="text-2xl font-bold text-gray-900 tracking-wider leading-relaxed">
              {{ result }}
            </div>
          </div>

          <!-- 操作按钮 -->
          <div class="flex items-center gap-3">
            <ToolButton 
              variant="primary" 
              @click="copyResult"
              :disabled="!result"
            >
              <Copy class="w-4 h-4 mr-1" />
              复制结果
            </ToolButton>
            <ToolButton 
              variant="secondary" 
              @click="reset"
            >
              <RotateCcw class="w-4 h-4 mr-1" />
              重置
            </ToolButton>
          </div>
        </div>
      </ToolCard>

      <!-- 快速示例 -->
      <ToolCard>
        <div class="flex items-center gap-2 mb-4">
          <Calculator class="w-5 h-5 text-rose-500" />
          <h3 class="font-semibold text-gray-900">快速示例</h3>
        </div>
        <div class="grid grid-cols-2 md:grid-cols-4 gap-3">
          <button
            v-for="example in examples"
            :key="example.num"
            @click="useExample(example.num)"
            class="p-3 text-left rounded-lg border border-gray-200 hover:border-rose-300 hover:bg-rose-50 transition-all"
          >
            <div class="text-sm font-medium text-gray-900">{{ example.num }}</div>
            <div class="text-xs text-gray-500">{{ example.desc }}</div>
          </button>
        </div>
      </ToolCard>

      <!-- 使用说明 -->
      <ToolCard>
        <div class="flex items-center gap-2 mb-4">
          <CheckCircle2 class="w-5 h-5 text-rose-500" />
          <h3 class="font-semibold text-gray-900">使用说明</h3>
        </div>
        <div class="space-y-3 text-sm text-gray-600">
          <p><strong>金额格式：</strong>自动添加"元角分"单位，如 123.45 → 壹佰贰拾叁元肆角伍分</p>
          <p><strong>普通数字：</strong>纯数字大写转换，如 12345 → 壹万贰仟叁佰肆拾伍</p>
          <p><strong>小数处理：</strong>金额格式最多支持到分（2位小数），普通数字支持更多小数位</p>
          <p><strong>负数支持：</strong>自动添加"负"字前缀</p>
          <p><strong>零的处理：</strong>自动优化连续的零，如 1001 → 壹仟零壹</p>
        </div>
      </ToolCard>

      <!-- 历史记录 -->
      <ToolCard v-if="history.length > 0">
        <div class="flex items-center gap-2 mb-4">
          <History class="w-5 h-5 text-rose-500" />
          <h3 class="font-semibold text-gray-900">历史记录</h3>
        </div>
        <div class="space-y-2">
          <div
            v-for="(item, index) in history"
            :key="index"
            class="flex items-center justify-between p-3 bg-gray-50 rounded-lg"
          >
            <span class="text-sm text-gray-700">{{ item }}</span>
            <ToolButton 
              variant="secondary" 
              size="sm"
              @click="navigator.clipboard.writeText(item)"
            >
              <Copy class="w-3 h-3" />
            </ToolButton>
          </div>
        </div>
      </ToolCard>
    </div>
  </NuxtLayout>
</template>
