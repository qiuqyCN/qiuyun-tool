<script setup lang="ts">
import { ref, computed } from 'vue'
import { Home, Calculator, TrendingDown, Calendar, DollarSign, Percent, ArrowRight } from 'lucide-vue-next'

// 计算模式
enum CalcMode {
  NORMAL = 'normal',
  PREPAYMENT = 'prepayment'
}

// 贷款类型
enum LoanType {
  COMMERCIAL = 'commercial',
  FUND = 'fund',
  COMBINATION = 'combination'
}

// 还款方式
enum RepaymentMethod {
  EQUAL_INTEREST = 'equalInterest',
  EQUAL_PRINCIPAL = 'equalPrincipal'
}

// 提前还款方式
enum PrepaymentType {
  REDUCE_TERM = 'reduceTerm',
  REDUCE_PAYMENT = 'reducePayment'
}

// 状态
const calcMode = ref<CalcMode>(CalcMode.NORMAL)
const loanType = ref<LoanType>(LoanType.COMMERCIAL)
const repaymentMethod = ref<RepaymentMethod>(RepaymentMethod.EQUAL_INTEREST)
const loanAmount = ref(100)
const loanYears = ref(30)
const commercialRate = ref(3.8)
const fundRate = ref(3.1)
const fundAmount = ref(50)

// 提前还款参数
const prepaymentAmount = ref(20)
const prepaymentMonth = ref(12)
const prepaymentType = ref<PrepaymentType>(PrepaymentType.REDUCE_TERM)

const currentLPR = 3.85

// 普通房贷计算
const normalResult = computed(() => {
  const months = loanYears.value * 12
  const principal = loanAmount.value * 10000
  const monthlyRate = commercialRate.value / 100 / 12

  let monthlyPayment = 0
  let totalInterest = 0
  let firstMonthPayment = 0
  let monthlyDecrease = 0

  if (repaymentMethod.value === RepaymentMethod.EQUAL_INTEREST) {
    monthlyPayment = principal * monthlyRate * Math.pow(1 + monthlyRate, months) / 
                     (Math.pow(1 + monthlyRate, months) - 1)
    totalInterest = monthlyPayment * months - principal
  } else {
    const monthlyPrincipal = principal / months
    firstMonthPayment = monthlyPrincipal + principal * monthlyRate
    monthlyDecrease = monthlyPrincipal * monthlyRate
    totalInterest = (months + 1) * principal * monthlyRate / 2
  }

  return { monthlyPayment, totalInterest, firstMonthPayment, monthlyDecrease }
})

// 提前还款计算
const prepaymentResult = computed(() => {
  const months = loanYears.value * 12
  const principal = loanAmount.value * 10000
  const monthlyRate = commercialRate.value / 100 / 12
  const prepayAmount = prepaymentAmount.value * 10000
  const prepayMonth = prepaymentMonth.value

  // 计算提前还款前的剩余本金
  let remainingPrincipal = principal
  if (repaymentMethod.value === RepaymentMethod.EQUAL_INTEREST) {
    const monthlyPayment = principal * monthlyRate * Math.pow(1 + monthlyRate, months) / 
                          (Math.pow(1 + monthlyRate, months) - 1)
    for (let i = 0; i < prepayMonth; i++) {
      const interest = remainingPrincipal * monthlyRate
      const principalPaid = monthlyPayment - interest
      remainingPrincipal -= principalPaid
    }
  } else {
    const monthlyPrincipal = principal / months
    remainingPrincipal = principal - monthlyPrincipal * prepayMonth
  }

  // 提前还款后的新本金
  const newPrincipal = remainingPrincipal - prepayAmount

  // 原方案总利息
  const originalTotalInterest = normalResult.value.totalInterest

  // 已还利息
  let paidInterest = 0
  if (repaymentMethod.value === RepaymentMethod.EQUAL_INTEREST) {
    const monthlyPayment = principal * monthlyRate * Math.pow(1 + monthlyRate, months) / 
                          (Math.pow(1 + monthlyRate, months) - 1)
    let tempPrincipal = principal
    for (let i = 0; i < prepayMonth; i++) {
      paidInterest += tempPrincipal * monthlyRate
      const principalPaid = monthlyPayment - tempPrincipal * monthlyRate
      tempPrincipal -= principalPaid
    }
  } else {
    const monthlyPrincipal = principal / months
    let tempPrincipal = principal
    for (let i = 0; i < prepayMonth; i++) {
      paidInterest += tempPrincipal * monthlyRate
      tempPrincipal -= monthlyPrincipal
    }
  }

  // 新方案
  const remainingMonths = months - prepayMonth
  let newMonthlyPayment = 0
  let newTotalInterest = 0

  if (prepaymentType.value === PrepaymentType.REDUCE_TERM) {
    // 缩短年限 - 保持月供不变，计算新的还款月数
    const originalMonthlyPayment = repaymentMethod.value === RepaymentMethod.EQUAL_INTEREST
      ? principal * monthlyRate * Math.pow(1 + monthlyRate, months) / (Math.pow(1 + monthlyRate, months) - 1)
      : principal / months + newPrincipal * monthlyRate

    // 简化的缩短年限计算
    let tempPrincipal = newPrincipal
    let newMonths = 0
    if (repaymentMethod.value === RepaymentMethod.EQUAL_INTEREST) {
      // 等额本息缩短年限
      newMonths = Math.ceil(
        Math.log(originalMonthlyPayment / (originalMonthlyPayment - newPrincipal * monthlyRate)) / 
        Math.log(1 + monthlyRate)
      )
      newMonthlyPayment = originalMonthlyPayment
      newTotalInterest = newMonthlyPayment * newMonths - newPrincipal
    } else {
      // 等额本金缩短年限
      const monthlyPrincipal = newPrincipal / remainingMonths
      newMonthlyPayment = monthlyPrincipal + newPrincipal * monthlyRate
      while (tempPrincipal > 0 && newMonths < remainingMonths) {
        newTotalInterest += tempPrincipal * monthlyRate
        tempPrincipal -= monthlyPrincipal
        newMonths++
      }
    }

    return {
      originalTotalInterest,
      paidInterest,
      newTotalInterest,
      savedInterest: originalTotalInterest - paidInterest - newTotalInterest,
      newMonthlyPayment,
      newMonths,
      monthsReduced: remainingMonths - newMonths
    }
  } else {
    // 减少月供 - 保持年限不变，计算新月供
    if (repaymentMethod.value === RepaymentMethod.EQUAL_INTEREST) {
      newMonthlyPayment = newPrincipal * monthlyRate * Math.pow(1 + monthlyRate, remainingMonths) / 
                         (Math.pow(1 + monthlyRate, remainingMonths) - 1)
      newTotalInterest = newMonthlyPayment * remainingMonths - newPrincipal
    } else {
      const monthlyPrincipal = newPrincipal / remainingMonths
      newMonthlyPayment = monthlyPrincipal + newPrincipal * monthlyRate
      let tempPrincipal = newPrincipal
      for (let i = 0; i < remainingMonths; i++) {
        newTotalInterest += tempPrincipal * monthlyRate
        tempPrincipal -= monthlyPrincipal
      }
    }

    const originalMonthlyPayment = repaymentMethod.value === RepaymentMethod.EQUAL_INTEREST
      ? normalResult.value.monthlyPayment
      : normalResult.value.firstMonthPayment

    return {
      originalTotalInterest,
      paidInterest,
      newTotalInterest,
      savedInterest: originalTotalInterest - paidInterest - newTotalInterest,
      newMonthlyPayment,
      originalMonthlyPayment,
      monthlyReduced: originalMonthlyPayment - newMonthlyPayment,
      newMonths: remainingMonths
    }
  }
})

const formatMoney = (amount: number) => {
  return new Intl.NumberFormat('zh-CN', {
    style: 'currency',
    currency: 'CNY',
    minimumFractionDigits: 2
  }).format(amount)
}
</script>

<template>
  <NuxtLayout name="tool" toolCode="mortgage-calculator">
    <div class="border border-border/40 rounded-xl overflow-hidden bg-card">
      <div class="border-b bg-muted/30 px-6 py-4">
        <div class="flex items-center gap-2">
          <Home class="w-5 h-5 text-primary" />
          <h2 class="text-lg font-semibold">房贷计算器</h2>
        </div>
        <p class="text-sm text-muted-foreground mt-1">
          计算商业贷款、公积金贷款、组合贷款及提前还款的月供和利息
        </p>
      </div>

      <div class="p-6">
        <!-- 计算模式切换 -->
        <div class="flex border-b mb-6">
          <button
            @click="calcMode = CalcMode.NORMAL"
            class="px-4 py-2 text-sm font-medium border-b-2 transition-colors"
            :class="calcMode === CalcMode.NORMAL ? 'border-primary text-primary' : 'border-transparent'"
          >
            普通房贷
          </button>
          <button
            @click="calcMode = CalcMode.PREPAYMENT"
            class="px-4 py-2 text-sm font-medium border-b-2 transition-colors"
            :class="calcMode === CalcMode.PREPAYMENT ? 'border-primary text-primary' : 'border-transparent'"
          >
            提前还款
          </button>
        </div>

        <div class="grid grid-cols-1 lg:grid-cols-2 gap-6">
          <!-- 左侧：输入区 -->
          <div class="space-y-4">
            <!-- 贷款类型 -->
            <div>
              <label class="text-sm font-medium mb-2 block">贷款类型</label>
              <div class="grid grid-cols-3 gap-2">
                <button
                  v-for="type in [
                    { value: LoanType.COMMERCIAL, label: '商业贷款' },
                    { value: LoanType.FUND, label: '公积金贷款' },
                    { value: LoanType.COMBINATION, label: '组合贷款' }
                  ]"
                  :key="type.value"
                  @click="loanType = type.value"
                  class="p-2 text-sm rounded-lg border transition-colors"
                  :class="loanType === type.value ? 'border-primary bg-primary/10 text-primary' : 'border-border'"
                >
                  {{ type.label }}
                </button>
              </div>
            </div>

            <!-- 还款方式 -->
            <div>
              <label class="text-sm font-medium mb-2 block">还款方式</label>
              <div class="grid grid-cols-2 gap-2">
                <button
                  v-for="method in [
                    { value: RepaymentMethod.EQUAL_INTEREST, label: '等额本息' },
                    { value: RepaymentMethod.EQUAL_PRINCIPAL, label: '等额本金' }
                  ]"
                  :key="method.value"
                  @click="repaymentMethod = method.value"
                  class="p-2 text-sm rounded-lg border transition-colors"
                  :class="repaymentMethod === method.value ? 'border-primary bg-primary/10' : 'border-border'"
                >
                  {{ method.label }}
                </button>
              </div>
            </div>

            <!-- 贷款金额 -->
            <div>
              <label class="text-sm font-medium mb-2 block">贷款金额：{{ loanAmount }} 万元</label>
              <input v-model="loanAmount" type="range" min="10" max="1000" step="5" class="w-full" />
            </div>

            <!-- 贷款年限 -->
            <div>
              <label class="text-sm font-medium mb-2 block">贷款年限：{{ loanYears }} 年</label>
              <input v-model="loanYears" type="range" min="5" max="30" step="1" class="w-full" />
            </div>

            <!-- 利率 -->
            <div>
              <label class="text-sm font-medium mb-2 block">年利率：{{ commercialRate }}%</label>
              <input v-model="commercialRate" type="range" min="2" max="6" step="0.05" class="w-full" />
              <div class="flex gap-2 mt-2">
                <button @click="commercialRate = currentLPR - 0.45" class="px-2 py-1 text-xs border rounded">LPR-45BP</button>
                <button @click="commercialRate = currentLPR" class="px-2 py-1 text-xs border rounded">当前LPR</button>
              </div>
            </div>

            <!-- 提前还款参数 -->
            <template v-if="calcMode === CalcMode.PREPAYMENT">
              <div class="border-t pt-4 space-y-4">
                <h3 class="font-medium text-sm">提前还款设置</h3>
                
                <div>
                  <label class="text-sm font-medium mb-2 block">提前还款金额：{{ prepaymentAmount }} 万元</label>
                  <input v-model="prepaymentAmount" type="range" min="5" :max="loanAmount * 0.5" step="5" class="w-full" />
                </div>

                <div>
                  <label class="text-sm font-medium mb-2 block">提前还款时间：第 {{ prepaymentMonth }} 个月</label>
                  <input v-model="prepaymentMonth" type="range" min="6" :max="loanYears * 12 - 12" step="6" class="w-full" />
                </div>

                <div>
                  <label class="text-sm font-medium mb-2 block">还款方式</label>
                  <div class="grid grid-cols-2 gap-2">
                    <button
                      @click="prepaymentType = PrepaymentType.REDUCE_TERM"
                      class="p-2 text-sm rounded-lg border transition-colors"
                      :class="prepaymentType === PrepaymentType.REDUCE_TERM ? 'border-primary bg-primary/10' : 'border-border'"
                    >
                      缩短年限
                    </button>
                    <button
                      @click="prepaymentType = PrepaymentType.REDUCE_PAYMENT"
                      class="p-2 text-sm rounded-lg border transition-colors"
                      :class="prepaymentType === PrepaymentType.REDUCE_PAYMENT ? 'border-primary bg-primary/10' : 'border-border'"
                    >
                      减少月供
                    </button>
                  </div>
                </div>
              </div>
            </template>
          </div>

          <!-- 右侧：结果区 -->
          <div class="space-y-4">
            <!-- 普通房贷结果 -->
            <template v-if="calcMode === CalcMode.NORMAL">
              <div class="bg-muted/30 rounded-lg p-4 space-y-4">
                <div class="text-center p-4 bg-background rounded-lg">
                  <div class="text-sm text-muted-foreground mb-1">
                    {{ repaymentMethod === RepaymentMethod.EQUAL_INTEREST ? '每月还款' : '首月还款' }}
                  </div>
                  <div class="text-3xl font-bold text-primary">
                    {{ formatMoney(repaymentMethod === RepaymentMethod.EQUAL_INTEREST ? 
                       normalResult.monthlyPayment : normalResult.firstMonthPayment) }}
                  </div>
                  <div v-if="repaymentMethod === RepaymentMethod.EQUAL_PRINCIPAL" class="text-sm text-muted-foreground mt-1">
                    每月递减 {{ formatMoney(normalResult.monthlyDecrease) }}
                  </div>
                </div>

                <div class="grid grid-cols-2 gap-4">
                  <div class="p-3 bg-background rounded-lg text-center">
                    <div class="text-xs text-muted-foreground">贷款总额</div>
                    <div class="font-semibold">{{ formatMoney(loanAmount * 10000) }}</div>
                  </div>
                  <div class="p-3 bg-background rounded-lg text-center">
                    <div class="text-xs text-muted-foreground">支付利息</div>
                    <div class="font-semibold text-red-500">{{ formatMoney(normalResult.totalInterest) }}</div>
                  </div>
                </div>
              </div>
            </template>

            <!-- 提前还款结果 -->
            <template v-else>
              <div class="bg-muted/30 rounded-lg p-4 space-y-4">
                <div class="text-center p-4 bg-green-50 border border-green-200 rounded-lg">
                  <div class="text-sm text-green-700 mb-1">预计节省利息</div>
                  <div class="text-3xl font-bold text-green-600">
                    {{ formatMoney(prepaymentResult.savedInterest) }}
                  </div>
                </div>

                <div class="grid grid-cols-2 gap-4">
                  <div class="p-3 bg-background rounded-lg text-center">
                    <div class="text-xs text-muted-foreground">原总利息</div>
                    <div class="font-semibold">{{ formatMoney(prepaymentResult.originalTotalInterest) }}</div>
                  </div>
                  <div class="p-3 bg-background rounded-lg text-center">
                    <div class="text-xs text-muted-foreground">提前还款后利息</div>
                    <div class="font-semibold text-green-600">{{ formatMoney(prepaymentResult.paidInterest + prepaymentResult.newTotalInterest) }}</div>
                  </div>
                </div>

                <div class="p-4 bg-background rounded-lg">
                  <h4 class="font-medium mb-3">还款对比</h4>
                  <div class="space-y-2 text-sm">
                    <div class="flex justify-between">
                      <span class="text-muted-foreground">原月供</span>
                      <span>{{ formatMoney(prepaymentResult.originalMonthlyPayment || normalResult.monthlyPayment) }}</span>
                    </div>
                    <div class="flex justify-between">
                      <span class="text-muted-foreground">新月供</span>
                      <span class="text-green-600">{{ formatMoney(prepaymentResult.newMonthlyPayment) }}</span>
                    </div>
                    <template v-if="prepaymentType === PrepaymentType.REDUCE_TERM">
                      <div class="flex justify-between">
                        <span class="text-muted-foreground">原剩余期限</span>
                        <span>{{ loanYears * 12 - prepaymentMonth }} 个月</span>
                      </div>
                      <div class="flex justify-between">
                        <span class="text-muted-foreground">新期限</span>
                        <span class="text-green-600">{{ prepaymentResult.newMonths }} 个月</span>
                      </div>
                      <div class="flex justify-between">
                        <span class="text-muted-foreground">缩短</span>
                        <span class="text-green-600">{{ prepaymentResult.monthsReduced }} 个月</span>
                      </div>
                    </template>
                    <template v-else>
                      <div class="flex justify-between">
                        <span class="text-muted-foreground">月供减少</span>
                        <span class="text-green-600">{{ formatMoney(prepaymentResult.monthlyReduced || 0) }}</span>
                      </div>
                    </template>
                  </div>
                </div>
              </div>
            </template>
          </div>
        </div>
      </div>
    </div>
  </NuxtLayout>
</template>
