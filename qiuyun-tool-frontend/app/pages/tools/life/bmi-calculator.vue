<script setup lang="ts">
import { ref, computed } from 'vue'
import { Scale, Ruler, Info, Heart, AlertCircle } from 'lucide-vue-next'
import ToolInput from '@/components/ui/ToolInput.vue'
import ToolCard from '@/components/ui/ToolCard.vue'

useHead({
  title: 'BMI计算器 - 秋云工具',
  meta: [
    { name: 'description', content: '计算身体质量指数(BMI)，评估体重健康状况，提供标准体重参考' }
  ]
})

const height = ref<number | undefined>(undefined)
const weight = ref<number | undefined>(undefined)
const gender = ref<'male' | 'female'>('male')

const bmiResult = computed(() => {
  if (!height.value || !weight.value || height.value <= 0 || weight.value <= 0) return null

  const heightInMeters = height.value / 100
  const bmi = weight.value / (heightInMeters * heightInMeters)

  let category = ''
  let color = ''
  let advice = ''

  if (bmi < 18.5) {
    category = '偏瘦'
    color = '#3B82F6'
    advice = '建议适当增加营养摄入，进行适量运动增强体质。'
  } else if (bmi < 24) {
    category = '正常'
    color = '#10B981'
    advice = '您的体重在健康范围内，请继续保持良好的生活习惯。'
  } else if (bmi < 28) {
    category = '偏胖'
    color = '#F59E0B'
    advice = '建议控制饮食，增加运动量，预防体重进一步增加。'
  } else {
    category = '肥胖'
    color = '#EF4444'
    advice = '建议咨询医生或营养师，制定科学的减重计划。'
  }

  // 标准体重计算 (BMI 22)
  const standardWeight = 22 * heightInMeters * heightInMeters
  const weightDiff = weight.value - standardWeight

  // 理想体重范围 (BMI 18.5-24)
  const minWeight = 18.5 * heightInMeters * heightInMeters
  const maxWeight = 24 * heightInMeters * heightInMeters

  return {
    bmi: Math.round(bmi * 10) / 10,
    category,
    color,
    advice,
    standardWeight: Math.round(standardWeight * 10) / 10,
    weightDiff: Math.round(weightDiff * 10) / 10,
    minWeight: Math.round(minWeight * 10) / 10,
    maxWeight: Math.round(maxWeight * 10) / 10
  }
})

const bmiCategories = [
  { min: 0, max: 18.5, label: '偏瘦', color: '#3B82F6', desc: 'BMI < 18.5' },
  { min: 18.5, max: 24, label: '正常', color: '#10B981', desc: '18.5 ≤ BMI < 24' },
  { min: 24, max: 28, label: '偏胖', color: '#F59E0B', desc: '24 ≤ BMI < 28' },
  { min: 28, max: 100, label: '肥胖', color: '#EF4444', desc: 'BMI ≥ 28' }
]

const getBmiPosition = (bmi: number): number => {
  // Map BMI 15-35 to 0-100%
  const minBMI = 15
  const maxBMI = 35
  const percentage = ((bmi - minBMI) / (maxBMI - minBMI)) * 100
  return Math.max(0, Math.min(100, percentage))
}
</script>

<template>
  <NuxtLayout name="tool" toolCode="bmi-calculator">
    <div class="max-w-4xl mx-auto space-y-6">
      <!-- 输入区域 -->
      <ToolCard>
        <div class="flex items-center gap-2 mb-6">
          <Scale class="w-6 h-6 text-rose-500" />
          <h2 class="text-lg font-semibold text-gray-900">BMI 计算器</h2>
        </div>

        <div class="grid md:grid-cols-2 gap-6">
          <div class="space-y-4">
            <!-- 性别选择 -->
            <div>
              <label class="block text-sm font-medium text-gray-700 mb-2">性别</label>
              <div class="grid grid-cols-2 gap-3">
                <button
                  @click="gender = 'male'"
                  class="flex items-center justify-center gap-2 p-3 rounded-xl border-2 transition-all"
                  :class="gender === 'male'
                    ? 'border-rose-500 bg-rose-50 text-rose-700'
                    : 'border-gray-200 text-gray-600 hover:border-rose-200'"
                >
                  <span class="font-medium">男</span>
                </button>
                <button
                  @click="gender = 'female'"
                  class="flex items-center justify-center gap-2 p-3 rounded-xl border-2 transition-all"
                  :class="gender === 'female'
                    ? 'border-rose-500 bg-rose-50 text-rose-700'
                    : 'border-gray-200 text-gray-600 hover:border-rose-200'"
                >
                  <span class="font-medium">女</span>
                </button>
              </div>
            </div>

            <!-- 身高 -->
            <div>
              <label class="block text-sm font-medium text-gray-700 mb-2">
                <span class="flex items-center gap-1">
                  <Ruler class="w-4 h-4" />
                  身高 (cm)
                </span>
              </label>
              <ToolInput
                v-model="height"
                type="number"
                placeholder="请输入身高"
                min="1"
                max="300"
              />
            </div>

            <!-- 体重 -->
            <div>
              <label class="block text-sm font-medium text-gray-700 mb-2">
                <span class="flex items-center gap-1">
                  <Scale class="w-4 h-4" />
                  体重 (kg)
                </span>
              </label>
              <ToolInput
                v-model="weight"
                type="number"
                placeholder="请输入体重"
                min="1"
                max="500"
              />
            </div>
          </div>

          <!-- 结果展示 -->
          <div v-if="bmiResult" class="p-6 bg-linear-to-r from-rose-50 to-pink-50 rounded-xl">
            <div class="text-center mb-4">
              <div class="text-sm text-gray-500 mb-1">您的 BMI 指数</div>
              <div class="text-5xl font-bold" :style="{ color: bmiResult.color }">
                {{ bmiResult.bmi }}
              </div>
              <div
                class="inline-block px-4 py-1 rounded-full text-white text-sm font-medium mt-2"
                :style="{ backgroundColor: bmiResult.color }"
              >
                {{ bmiResult.category }}
              </div>
            </div>

            <!-- BMI 指示器 -->
            <div class="mt-4">
              <div class="h-3 bg-gray-200 rounded-full overflow-hidden flex">
                <div class="h-full" style="width: 18.75%; background-color: #3B82F6;"></div>
                <div class="h-full" style="width: 27.5%; background-color: #10B981;"></div>
                <div class="h-full" style="width: 25%; background-color: #F59E0B;"></div>
                <div class="h-full" style="width: 28.75%; background-color: #EF4444;"></div>
              </div>
              <div class="relative h-4 mt-1">
                <div
                  class="absolute w-0 h-0 border-l-[6px] border-l-transparent border-r-[6px] border-r-transparent border-b-[8px] border-b-gray-700 transform -translate-x-1/2"
                  :style="{ left: getBmiPosition(bmiResult.bmi) + '%' }"
                ></div>
              </div>
              <div class="flex justify-between text-xs text-gray-500 mt-1">
                <span>15</span>
                <span>18.5</span>
                <span>24</span>
                <span>28</span>
                <span>35</span>
              </div>
            </div>
          </div>

          <div v-else class="p-6 bg-gray-50 rounded-xl flex items-center justify-center">
            <div class="text-center text-gray-400">
              <Scale class="w-12 h-12 mx-auto mb-2" />
              <p>请输入身高和体重</p>
            </div>
          </div>
        </div>
      </ToolCard>

      <!-- 详细分析 -->
      <ToolCard v-if="bmiResult">
        <h3 class="text-base font-semibold text-gray-900 mb-4 flex items-center gap-2">
          <Info class="w-5 h-5 text-rose-500" />
          详细分析
        </h3>

        <div class="grid md:grid-cols-2 gap-4">
          <div class="p-4 bg-gray-50 rounded-xl">
            <div class="text-sm text-gray-500 mb-1">标准体重</div>
            <div class="text-2xl font-bold text-gray-900">{{ bmiResult.standardWeight }} kg</div>
            <div class="text-sm text-gray-500 mt-1">
              健康范围: {{ bmiResult.minWeight }} - {{ bmiResult.maxWeight }} kg
            </div>
          </div>

          <div class="p-4 bg-gray-50 rounded-xl">
            <div class="text-sm text-gray-500 mb-1">与标准体重相差</div>
            <div
              class="text-2xl font-bold"
              :class="bmiResult.weightDiff > 0 ? 'text-orange-500' : bmiResult.weightDiff < 0 ? 'text-blue-500' : 'text-green-500'"
            >
              {{ bmiResult.weightDiff > 0 ? '+' : '' }}{{ bmiResult.weightDiff }} kg
            </div>
            <div class="text-sm text-gray-500 mt-1">
              {{ bmiResult.weightDiff > 0 ? '超出标准体重' : bmiResult.weightDiff < 0 ? '低于标准体重' : '正好达到标准体重' }}
            </div>
          </div>
        </div>

        <div class="mt-4 p-4 rounded-xl" :style="{ backgroundColor: bmiResult.color + '15' }">
          <div class="flex items-start gap-3">
            <Heart class="w-5 h-5 mt-0.5 flex-shrink-0" :style="{ color: bmiResult.color }" />
            <div>
              <div class="font-medium text-gray-900 mb-1">健康建议</div>
              <div class="text-sm text-gray-600">{{ bmiResult.advice }}</div>
            </div>
          </div>
        </div>
      </ToolCard>

      <!-- BMI 分类说明 -->
      <ToolCard>
        <h3 class="text-base font-semibold text-gray-900 mb-4">BMI 分类标准 (中国标准)</h3>
        <div class="space-y-3">
          <div
            v-for="cat in bmiCategories"
            :key="cat.label"
            class="flex items-center justify-between p-3 rounded-lg"
            :class="bmiResult?.category === cat.label ? 'bg-gray-100' : 'bg-gray-50'"
          >
            <div class="flex items-center gap-3">
              <div
                class="w-4 h-4 rounded-full"
                :style="{ backgroundColor: cat.color }"
              ></div>
              <span class="font-medium text-gray-900">{{ cat.label }}</span>
            </div>
            <span class="text-sm text-gray-500">{{ cat.desc }}</span>
          </div>
        </div>

        <div class="mt-4 p-4 bg-blue-50 rounded-xl flex items-start gap-3">
          <AlertCircle class="w-5 h-5 text-blue-500 mt-0.5 flex-shrink-0" />
          <div class="text-sm text-blue-700">
            <p class="font-medium mb-1">说明</p>
            <p>BMI (身体质量指数) 是衡量人体胖瘦程度的常用指标。计算公式为：BMI = 体重(kg) ÷ 身高²(m²)。</p>
            <p class="mt-1">本工具采用中国成人BMI标准。BMI仅作为参考，实际健康状况还需结合体脂率、腰围等其他指标综合评估。</p>
          </div>
        </div>
      </ToolCard>
    </div>
  </NuxtLayout>
</template>
