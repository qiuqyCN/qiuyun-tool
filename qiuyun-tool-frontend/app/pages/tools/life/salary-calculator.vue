<script setup lang="ts">
import { ref, computed } from 'vue'
import { Banknote, Calculator, TrendingDown, PieChart, Wallet } from 'lucide-vue-next'

// 计算类型
enum CalcType {
  SALARY = 'salary',
  TAX = 'tax',
  BONUS = 'bonus'
}

// 状态
const calcType = ref<CalcType>(CalcType.SALARY)
const grossSalary = ref(10000)
const socialInsurance = ref(10.5) // 五险一金比例
const specialDeduction = ref(2000)
const bonus = ref(50000)

// 个税起征点
const TAX_THRESHOLD = 5000

// 个税税率表
const TAX_BRACKETS = [
  { limit: 3000, rate: 0.03, deduction: 0 },
  { limit: 12000, rate: 0.10, deduction: 210 },
  { limit: 25000, rate: 0.20, deduction: 1410 },
  { limit: 35000, rate: 0.25, deduction: 2660 },
  { limit: 55000, rate: 0.30, deduction: 4410 },
  { limit: 80000, rate: 0.35, deduction: 7160 },
  { limit: Infinity, rate: 0.45, deduction: 15160 }
]

// 税后工资计算
const salaryResult = computed(() => {
  const gross = grossSalary.value
  const socialInsuranceAmount = gross * (socialInsurance.value / 100)
  const taxableIncome = Math.max(0, gross - socialInsuranceAmount - TAX_THRESHOLD - specialDeduction.value)
  
  let tax = 0
  for (const bracket of TAX_BRACKETS) {
    if (taxableIncome <= bracket.limit) {
      tax = taxableIncome * bracket.rate - bracket.deduction
      break
    }
  }
  
  const netSalary = gross - socialInsuranceAmount - tax
  
  return {
    gross,
    socialInsurance: socialInsuranceAmount,
    tax: Math.max(0, tax),
    netSalary,
    taxRate: gross > 0 ? (Math.max(0, tax) / gross * 100).toFixed(2) : '0.00'
  }
})

// 年终奖个税计算
const bonusResult = computed(() => {
  const bonusAmount = bonus.value
  const monthlyBonus = bonusAmount / 12
  
  let rate = 0
  let deduction = 0
  
  for (const bracket of TAX_BRACKETS) {
    if (monthlyBonus <= bracket.limit) {
      rate = bracket.rate
      deduction = bracket.deduction
      break
    }
  }
  
  const tax = bonusAmount * rate - deduction
  const netBonus = bonusAmount - tax
  
  return {
    gross: bonusAmount,
    tax: Math.max(0, tax),
    netBonus,
    rate: (rate * 100).toFixed(0)
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
  <NuxtLayout name="tool" toolCode="salary-calculator">
    <div class="border border-border/40 rounded-xl overflow-hidden bg-card">
      <div class="border-b bg-muted/30 px-6 py-4">
        <div class="flex items-center gap-2">
          <Banknote class="w-5 h-5 text-primary" />
          <h2 class="text-lg font-semibold">薪资计算器</h2>
        </div>
        <p class="text-sm text-muted-foreground mt-1">
          计算税后工资、个人所得税、年终奖个税
        </p>
      </div>

      <div class="p-6">
        <!-- 计算类型切换 -->
        <div class="flex border-b mb-6">
          <button
            v-for="type in [
              { value: CalcType.SALARY, label: '税后工资' },
              { value: CalcType.TAX, label: '个税计算' },
              { value: CalcType.BONUS, label: '年终奖' }
            ]"
            :key="type.value"
            @click="calcType = type.value"
            class="px-4 py-2 text-sm font-medium border-b-2 transition-colors"
            :class="calcType === type.value ? 'border-primary text-primary' : 'border-transparent'"
          >
            {{ type.label }}
          </button>
        </div>

        <div class="grid grid-cols-1 lg:grid-cols-2 gap-6">
          <!-- 左侧：输入区 -->
          <div class="space-y-4">
            <!-- 税后工资/个税计算 -->
            <template v-if="calcType !== CalcType.BONUS">
              <div>
                <label class="text-sm font-medium mb-2 block">税前工资：{{ grossSalary }} 元</label>
                <input v-model="grossSalary" type="range" min="3000" max="100000" step="500" class="w-full" />
              </div>

              <div>
                <label class="text-sm font-medium mb-2 block">五险一金比例：{{ socialInsurance }}%</label>
                <input v-model="socialInsurance" type="range" min="0" max="22" step="0.5" class="w-full" />
              </div>

              <div>
                <label class="text-sm font-medium mb-2 block">专项附加扣除：{{ specialDeduction }} 元</label>
                <input v-model="specialDeduction" type="range" min="0" max="10000" step="100" class="w-full" />
                <p class="text-xs text-muted-foreground mt-1">子女教育、房贷利息、赡养老人等</p>
              </div>
            </template>

            <!-- 年终奖计算 -->
            <template v-else>
              <div>
                <label class="text-sm font-medium mb-2 block">年终奖金额：{{ bonus }} 元</label>
                <input v-model="bonus" type="range" min="10000" max="500000" step="5000" class="w-full" />
              </div>
            </template>
          </div>

          <!-- 右侧：结果区 -->
          <div class="space-y-4">
            <!-- 税后工资结果 -->
            <template v-if="calcType === CalcType.SALARY">
              <div class="bg-muted/30 rounded-lg p-4 space-y-4">
                <div class="text-center p-4 bg-background rounded-lg">
                  <div class="text-sm text-muted-foreground mb-1">实发工资</div>
                  <div class="text-3xl font-bold text-primary">{{ formatMoney(salaryResult.netSalary) }}</div>
                </div>

                <div class="space-y-2">
                  <div class="flex justify-between p-2 bg-background rounded">
                    <span class="text-muted-foreground">税前工资</span>
                    <span>{{ formatMoney(salaryResult.gross) }}</span>
                  </div>
                  <div class="flex justify-between p-2 bg-background rounded">
                    <span class="text-muted-foreground">五险一金</span>
                    <span class="text-orange-500">-{{ formatMoney(salaryResult.socialInsurance) }}</span>
                  </div>
                  <div class="flex justify-between p-2 bg-background rounded">
                    <span class="text-muted-foreground">个人所得税</span>
                    <span class="text-red-500">-{{ formatMoney(salaryResult.tax) }}</span>
                  </div>
                  <div class="flex justify-between p-2 bg-background rounded">
                    <span class="text-muted-foreground">适用税率</span>
                    <span>{{ salaryResult.taxRate }}%</span>
                  </div>
                </div>
              </div>
            </template>

            <!-- 个税计算结果 -->
            <template v-else-if="calcType === CalcType.TAX">
              <div class="bg-muted/30 rounded-lg p-4 space-y-4">
                <div class="text-center p-4 bg-background rounded-lg">
                  <div class="text-sm text-muted-foreground mb-1">应缴个税</div>
                  <div class="text-3xl font-bold text-red-500">{{ formatMoney(salaryResult.tax) }}</div>
                </div>

                <div class="space-y-2">
                  <div class="flex justify-between p-2 bg-background rounded">
                    <span class="text-muted-foreground">应纳税所得额</span>
                    <span>{{ formatMoney(Math.max(0, grossSalary - salaryResult.socialInsurance - TAX_THRESHOLD - specialDeduction)) }}</span>
                  </div>
                  <div class="flex justify-between p-2 bg-background rounded">
                    <span class="text-muted-foreground">适用税率</span>
                    <span>{{ salaryResult.taxRate }}%</span>
                  </div>
                  <div class="flex justify-between p-2 bg-background rounded">
                    <span class="text-muted-foreground">速算扣除数</span>
                    <span>{{ formatMoney(TAX_BRACKETS.find(b => grossSalary - salaryResult.socialInsurance - TAX_THRESHOLD - specialDeduction <= b.limit)?.deduction || 0) }}</span>
                  </div>
                </div>
              </div>
            </template>

            <!-- 年终奖结果 -->
            <template v-else>
              <div class="bg-muted/30 rounded-lg p-4 space-y-4">
                <div class="text-center p-4 bg-background rounded-lg">
                  <div class="text-sm text-muted-foreground mb-1">税后年终奖</div>
                  <div class="text-3xl font-bold text-primary">{{ formatMoney(bonusResult.netBonus) }}</div>
                </div>

                <div class="space-y-2">
                  <div class="flex justify-between p-2 bg-background rounded">
                    <span class="text-muted-foreground">税前年终奖</span>
                    <span>{{ formatMoney(bonusResult.gross) }}</span>
                  </div>
                  <div class="flex justify-between p-2 bg-background rounded">
                    <span class="text-muted-foreground">适用税率</span>
                    <span>{{ bonusResult.rate }}%</span>
                  </div>
                  <div class="flex justify-between p-2 bg-background rounded">
                    <span class="text-muted-foreground">应缴个税</span>
                    <span class="text-red-500">-{{ formatMoney(bonusResult.tax) }}</span>
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
