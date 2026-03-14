<script setup lang="ts">
import { ref, computed } from 'vue'
import { Calculator, Delete, History, Trash2, Copy, Check } from 'lucide-vue-next'
import { useToast } from '~/composables/useToast'
import { useClipboard } from '~/composables/useClipboard'

// Toast
const { toast, showSuccess, showError } = useToast()
const { copy } = useClipboard()

// 计算器状态
const display = ref('0')
const expression = ref('')
const history = ref<Array<{ expr: string; result: string }>>([])
const showHistory = ref(false)
const isRadians = ref(true)
const memory = ref(0)

// 是否显示历史记录
const hasHistory = computed(() => history.value.length > 0)

// 格式化数字显示
const formatNumber = (num: number): string => {
  if (isNaN(num)) return 'Error'
  if (!isFinite(num)) return 'Infinity'
  if (Math.abs(num) < 1e-10 && num !== 0) return '0'
  if (Math.abs(num) >= 1e10 || (Math.abs(num) < 0.0001 && num !== 0)) {
    return num.toExponential(6)
  }
  const str = num.toString()
  if (str.length > 12) {
    return num.toPrecision(10)
  }
  return str
}

// 计算表达式
const calculate = (expr: string): number => {
  try {
    let processedExpr = expr
      .replace(/×/g, '*')
      .replace(/÷/g, '/')
      .replace(/π/g, Math.PI.toString())
      .replace(/e(?![x])/g, Math.E.toString())
      .replace(/(\d+)!/g, (_, n) => factorial(parseInt(n)).toString())
      .replace(/sin\(([^)]+)\)/g, (_, p) => (isRadians.value ? Math.sin(eval(p)) : Math.sin((eval(p) * Math.PI) / 180)).toString())
      .replace(/cos\(([^)]+)\)/g, (_, p) => (isRadians.value ? Math.cos(eval(p)) : Math.cos((eval(p) * Math.PI) / 180)).toString())
      .replace(/tan\(([^)]+)\)/g, (_, p) => (isRadians.value ? Math.tan(eval(p)) : Math.tan((eval(p) * Math.PI) / 180)).toString())
      .replace(/asin\(([^)]+)\)/g, (_, p) => (isRadians.value ? Math.asin(eval(p)) : (Math.asin(eval(p)) * 180) / Math.PI).toString())
      .replace(/acos\(([^)]+)\)/g, (_, p) => (isRadians.value ? Math.acos(eval(p)) : (Math.acos(eval(p)) * 180) / Math.PI).toString())
      .replace(/atan\(([^)]+)\)/g, (_, p) => (isRadians.value ? Math.atan(eval(p)) : (Math.atan(eval(p)) * 180) / Math.PI).toString())
      .replace(/log\(([^)]+)\)/g, (_, p) => Math.log10(eval(p)).toString())
      .replace(/ln\(([^)]+)\)/g, (_, p) => Math.log(eval(p)).toString())
      .replace(/sqrt\(([^)]+)\)/g, (_, p) => Math.sqrt(eval(p)).toString())
      .replace(/abs\(([^)]+)\)/g, (_, p) => Math.abs(eval(p)).toString())
      .replace(/exp\(([^)]+)\)/g, (_, p) => Math.exp(eval(p)).toString())
      .replace(/\^/g, '**')

    return eval(processedExpr)
  } catch {
    return NaN
  }
}

// 阶乘
const factorial = (n: number): number => {
  if (n < 0) return NaN
  if (n === 0 || n === 1) return 1
  let result = 1
  for (let i = 2; i <= n; i++) {
    result *= i
  }
  return result
}

// 输入数字
const inputNumber = (num: string) => {
  if (display.value === '0' || display.value === 'Error') {
    display.value = num
  } else {
    display.value += num
  }
}

// 输入运算符
const inputOperator = (op: string) => {
  const lastChar = display.value.slice(-1)
  if (['+', '-', '×', '÷', '^'].includes(lastChar)) {
    display.value = display.value.slice(0, -1) + op
  } else {
    display.value += op
  }
}

// 输入函数
const inputFunction = (func: string) => {
  display.value += func + '('
}

// 输入常量
const inputConstant = (constant: string) => {
  if (display.value === '0') {
    display.value = constant
  } else {
    display.value += constant
  }
}

// 输入小数点
const inputDecimal = () => {
  const parts = display.value.split(/[\+\-\×\÷\^]/)
  const lastPart = parts[parts.length - 1] || ''
  if (!lastPart.includes('.')) {
    display.value += '.'
  }
}

// 输入括号
const inputParenthesis = (paren: string) => {
  display.value += paren
}

// 输入百分比
const inputPercent = () => {
  try {
    const value = parseFloat(display.value)
    if (!isNaN(value)) {
      display.value = (value / 100).toString()
    }
  } catch {
    display.value = 'Error'
  }
}

// 计算阶乘
const inputFactorial = () => {
  try {
    const value = parseInt(display.value)
    if (!isNaN(value) && value >= 0 && value <= 170) {
      display.value = factorial(value).toString()
    } else {
      display.value = 'Error'
    }
  } catch {
    display.value = 'Error'
  }
}

// 平方
const square = () => {
  try {
    const value = parseFloat(display.value)
    if (!isNaN(value)) {
      display.value = formatNumber(value * value)
    }
  } catch {
    display.value = 'Error'
  }
}

// 平方根
const squareRoot = () => {
  try {
    const value = parseFloat(display.value)
    if (!isNaN(value) && value >= 0) {
      display.value = formatNumber(Math.sqrt(value))
    } else {
      display.value = 'Error'
    }
  } catch {
    display.value = 'Error'
  }
}

// 倒数
const reciprocal = () => {
  try {
    const value = parseFloat(display.value)
    if (!isNaN(value) && value !== 0) {
      display.value = formatNumber(1 / value)
    } else {
      display.value = 'Error'
    }
  } catch {
    display.value = 'Error'
  }
}

// 正负号切换
const toggleSign = () => {
  try {
    const value = parseFloat(display.value)
    if (!isNaN(value)) {
      display.value = (-value).toString()
    }
  } catch {
    display.value = 'Error'
  }
}

// 等于（计算结果）
const equals = () => {
  try {
    const result = calculate(display.value)
    if (!isNaN(result) && isFinite(result)) {
      const formattedResult = formatNumber(result)
      history.value.unshift({
        expr: display.value,
        result: formattedResult
      })
      if (history.value.length > 20) {
        history.value.pop()
      }
      expression.value = display.value + ' ='
      display.value = formattedResult
    } else {
      display.value = 'Error'
    }
  } catch {
    display.value = 'Error'
  }
}

// 清除
const clear = () => {
  display.value = '0'
  expression.value = ''
}

// 退格
const backspace = () => {
  if (display.value.length > 1) {
    display.value = display.value.slice(0, -1)
  } else {
    display.value = '0'
  }
}

// 清除历史
const clearHistory = () => {
  history.value = []
}

// 使用历史记录
const useHistory = (item: { expr: string; result: string }) => {
  display.value = item.result
  showHistory.value = false
}

// 复制结果
const copyResult = async () => {
  if (display.value && display.value !== 'Error') {
    const success = await copy(display.value)
    if (success) {
      showSuccess('已复制结果')
    } else {
      showError('复制失败')
    }
  }
}

// 内存操作
const memoryAdd = () => {
  const value = parseFloat(display.value)
  if (!isNaN(value)) {
    memory.value += value
    showSuccess('已添加到内存')
  }
}

const memorySubtract = () => {
  const value = parseFloat(display.value)
  if (!isNaN(value)) {
    memory.value -= value
    showSuccess('已从内存减去')
  }
}

const memoryRecall = () => {
  display.value = memory.value.toString()
}

const memoryClear = () => {
  memory.value = 0
  showSuccess('内存已清除')
}
</script>

<template>
  <NuxtLayout name="tool" tool-code="scientific-calculator">
    <div class="border border-border/40 rounded-xl overflow-hidden">
      <!-- 标题栏 -->
      <div class="border-b bg-muted/30 px-6 py-4">
        <div class="flex items-center gap-2">
          <Calculator class="w-5 h-5 text-primary" />
          <h2 class="text-lg font-semibold">科学计算器</h2>
        </div>
        <p class="text-sm text-muted-foreground mt-1">
          支持三角函数、对数、指数等科学运算
        </p>
      </div>

      <div class="p-4">
        <div class="max-w-lg mx-auto space-y-4">
          <!-- 显示区域 -->
          <div class="bg-muted/50 rounded-lg p-4 space-y-2">
            <div class="text-xs text-muted-foreground h-4 text-right">
              {{ expression }}
            </div>
            <div class="text-right text-3xl font-mono tracking-wider overflow-x-auto">
              {{ display }}
            </div>
            <div class="flex items-center justify-between text-xs text-muted-foreground">
              <span v-if="memory !== 0" class="text-primary">M: {{ memory }}</span>
              <span v-else></span>
              <button @click="copyResult" class="hover:text-primary transition-colors flex items-center gap-1">
                <Copy class="w-3 h-3" />
                复制结果
              </button>
            </div>
          </div>

          <!-- 角度/弧度切换 -->
          <div class="flex gap-2">
            <button
              @click="isRadians = true"
              class="flex-1 py-1.5 text-xs rounded-md border transition-colors"
              :class="isRadians ? 'border-primary bg-primary/10 text-primary' : 'border-border hover:border-primary/50'"
            >
              弧度 (RAD)
            </button>
            <button
              @click="isRadians = false"
              class="flex-1 py-1.5 text-xs rounded-md border transition-colors"
              :class="!isRadians ? 'border-primary bg-primary/10 text-primary' : 'border-border hover:border-primary/50'"
            >
              角度 (DEG)
            </button>
            <button
              @click="showHistory = !showHistory"
              class="px-3 py-1.5 text-xs rounded-md border transition-colors"
              :class="showHistory ? 'border-primary bg-primary/10 text-primary' : 'border-border hover:border-primary/50'"
            >
              <History class="w-4 h-4" />
            </button>
          </div>

          <!-- 历史记录 -->
          <div v-if="showHistory" class="border rounded-lg p-3 max-h-48 overflow-y-auto">
            <div class="flex items-center justify-between mb-2">
              <span class="text-xs font-medium">历史记录</span>
              <button v-if="hasHistory" @click="clearHistory" class="text-xs text-muted-foreground hover:text-foreground">
                <Trash2 class="w-3 h-3" />
              </button>
            </div>
            <div v-if="!hasHistory" class="text-xs text-muted-foreground text-center py-4">
              暂无历史记录
            </div>
            <div v-else class="space-y-1">
              <div
                v-for="(item, index) in history"
                :key="index"
                @click="useHistory(item)"
                class="flex justify-between items-center text-xs p-2 rounded hover:bg-muted/50 cursor-pointer"
              >
                <span class="text-muted-foreground">{{ item.expr }} =</span>
                <span class="font-mono">{{ item.result }}</span>
              </div>
            </div>
          </div>

          <!-- 科学函数按钮 -->
          <div class="grid grid-cols-5 gap-1.5">
            <button @click="inputFunction('sin')" class="calc-btn sci">sin</button>
            <button @click="inputFunction('cos')" class="calc-btn sci">cos</button>
            <button @click="inputFunction('tan')" class="calc-btn sci">tan</button>
            <button @click="inputFunction('log')" class="calc-btn sci">log</button>
            <button @click="inputFunction('ln')" class="calc-btn sci">ln</button>

            <button @click="inputFunction('asin')" class="calc-btn sci">sin⁻¹</button>
            <button @click="inputFunction('acos')" class="calc-btn sci">cos⁻¹</button>
            <button @click="inputFunction('atan')" class="calc-btn sci">tan⁻¹</button>
            <button @click="inputFunction('exp')" class="calc-btn sci">eˣ</button>
            <button @click="inputOperator('^')" class="calc-btn sci">xʸ</button>

            <button @click="inputFunction('sqrt')" class="calc-btn sci">√</button>
            <button @click="square" class="calc-btn sci">x²</button>
            <button @click="inputFactorial" class="calc-btn sci">n!</button>
            <button @click="inputConstant('π')" class="calc-btn sci">π</button>
            <button @click="inputConstant('e')" class="calc-btn sci">e</button>

            <button @click="inputFunction('abs')" class="calc-btn sci">|x|</button>
            <button @click="reciprocal" class="calc-btn sci">1/x</button>
            <button @click="inputParenthesis('(')" class="calc-btn sci">(</button>
            <button @click="inputParenthesis(')')" class="calc-btn sci">)</button>
            <button @click="inputPercent" class="calc-btn sci">%</button>
          </div>

          <!-- 内存操作按钮 -->
          <div class="grid grid-cols-4 gap-1.5">
            <button @click="memoryClear" class="calc-btn mem">MC</button>
            <button @click="memoryRecall" class="calc-btn mem">MR</button>
            <button @click="memoryAdd" class="calc-btn mem">M+</button>
            <button @click="memorySubtract" class="calc-btn mem">M-</button>
          </div>

          <!-- 基础按钮 -->
          <div class="grid grid-cols-4 gap-1.5">
            <button @click="clear" class="calc-btn clear">AC</button>
            <button @click="backspace" class="calc-btn clear">
              <Delete class="w-4 h-4 mx-auto" />
            </button>
            <button @click="toggleSign" class="calc-btn op">±</button>
            <button @click="inputOperator('÷')" class="calc-btn op">÷</button>

            <button @click="inputNumber('7')" class="calc-btn num">7</button>
            <button @click="inputNumber('8')" class="calc-btn num">8</button>
            <button @click="inputNumber('9')" class="calc-btn num">9</button>
            <button @click="inputOperator('×')" class="calc-btn op">×</button>

            <button @click="inputNumber('4')" class="calc-btn num">4</button>
            <button @click="inputNumber('5')" class="calc-btn num">5</button>
            <button @click="inputNumber('6')" class="calc-btn num">6</button>
            <button @click="inputOperator('-')" class="calc-btn op">−</button>

            <button @click="inputNumber('1')" class="calc-btn num">1</button>
            <button @click="inputNumber('2')" class="calc-btn num">2</button>
            <button @click="inputNumber('3')" class="calc-btn num">3</button>
            <button @click="inputOperator('+')" class="calc-btn op">+</button>

            <button @click="inputNumber('0')" class="calc-btn num col-span-2">0</button>
            <button @click="inputDecimal" class="calc-btn num">.</button>
            <button @click="equals" class="calc-btn eq">=</button>
          </div>

          <!-- 使用说明 -->
          <div class="p-3 bg-primary/5 rounded-lg border border-primary/20">
            <div class="text-xs text-primary font-medium mb-2">使用说明:</div>
            <ul class="text-xs text-muted-foreground space-y-1 list-disc list-inside">
              <li>支持基本运算：加、减、乘、除</li>
              <li>支持科学运算：三角函数、对数、指数、幂运算、阶乘等</li>
              <li>切换弧度/角度模式进行三角函数计算</li>
              <li>使用 M+/M-/MR/MC 进行内存操作</li>
              <li>点击历史记录可快速使用之前的计算结果</li>
            </ul>
          </div>
        </div>
      </div>
    </div>

    <!-- Toast 提示 -->
    <div
      v-if="toast.show"
      class="fixed bottom-4 right-4 px-4 py-2 rounded-lg shadow-lg flex items-center gap-2 z-50"
      :class="{
        'bg-green-500 text-white': toast.type === 'success',
        'bg-red-500 text-white': toast.type === 'error'
      }"
    >
      <Check v-if="toast.type === 'success'" class="w-4 h-4" />
      <span class="text-sm">{{ toast.message }}</span>
    </div>
  </NuxtLayout>
</template>

<style scoped>
.calc-btn {
  padding: 0.75rem;
  font-size: 0.875rem;
  font-weight: 500;
  border-radius: 0.5rem;
  transition: all 150ms;
}

.calc-btn:active {
  transform: scale(0.95);
}

.calc-btn.num {
  background-color: color-mix(in oklab, var(--muted) 50%, transparent);
  color: var(--foreground);
}

.calc-btn.num:hover {
  background-color: var(--muted);
}

.calc-btn.op {
  background-color: color-mix(in oklab, var(--primary) 10%, transparent);
  color: var(--primary);
}

.calc-btn.op:hover {
  background-color: color-mix(in oklab, var(--primary) 20%, transparent);
}

.calc-btn.sci {
  background-color: color-mix(in oklab, var(--muted) 30%, transparent);
  color: var(--muted-foreground);
  font-size: 0.75rem;
  padding: 0.5rem;
}

.calc-btn.sci:hover {
  background-color: color-mix(in oklab, var(--muted) 50%, transparent);
  color: var(--foreground);
}

.calc-btn.clear {
  background-color: color-mix(in oklab, oklch(0.628 0.257 25.335) 10%, transparent);
  color: oklch(0.628 0.257 25.335);
}

.calc-btn.clear:hover {
  background-color: color-mix(in oklab, oklch(0.628 0.257 25.335) 20%, transparent);
}

.calc-btn.eq {
  background-color: var(--primary);
  color: var(--primary-foreground);
}

.calc-btn.eq:hover {
  background-color: color-mix(in oklab, var(--primary) 90%, transparent);
}

.calc-btn.mem {
  background-color: color-mix(in oklab, oklch(0.646 0.222 239.4) 10%, transparent);
  color: oklch(0.646 0.222 239.4);
  font-size: 0.75rem;
  padding: 0.5rem;
}

.calc-btn.mem:hover {
  background-color: color-mix(in oklab, oklch(0.646 0.222 239.4) 20%, transparent);
}
</style>
