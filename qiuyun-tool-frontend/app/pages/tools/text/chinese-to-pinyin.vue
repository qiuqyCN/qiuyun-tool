<script setup lang="ts">
import { ref, computed, watch } from 'vue'
import {
  Type,
  Copy,
  RotateCcw,
  Settings2,
  Languages,
  Sparkles,
  AlertCircle,
  CheckCircle2,
  AlignLeft,
  Hash
} from 'lucide-vue-next'
import { ToolCard } from '@/components/ui/tool-card'
import { ToolButton } from '@/components/ui/tool-button'
import { Textarea } from '@/components/ui/textarea'
import { useToolExecutor } from '~/composables/useToolExecutor'
import { ToolType } from '~/types/tool'

useHead({
  title: '汉字转拼音 - 秋云工具',
  meta: [
    { name: 'description', content: '将汉字转换为拼音，支持声调、首字母提取、多音字检测等功能' }
  ]
})

// 请求参数类型
interface PinyinParams {
  text: string
  toneType: string
  caseType: string
  separator: string
  keepNonChinese: boolean
  extractInitials: boolean
  detectPolyphone: boolean
  showDetails: boolean
}

// 响应结果类型
interface PinyinResult {
  originalText: string
  pinyin: string
  initials?: string
  stats: {
    totalChars: number
    chineseChars: number
    englishChars: number
    numbers: number
    otherChars: number
  }
  polyphones?: Array<{
    character: string
    position: number
    readings: string[]
  }>
  details?: Array<{
    character: string
    chinese: boolean
    pinyinWithTone?: string
    pinyinWithoutTone?: string
    initial?: string
    alternativeReadings?: string[]
  }>
}

// 输入状态
const inputText = ref('')
const copySuccess = ref(false)

// 选项配置
const options = ref({
  toneType: 'without_tone',
  caseType: 'lowercase',
  separator: 'space',
  keepNonChinese: true,
  extractInitials: false,
  detectPolyphone: false,
  showDetails: false
})

// 示例数据
const examples = [
  { text: '中华人民共和国', desc: '国家名称' },
  { text: '北京欢迎你', desc: '欢迎语' },
  { text: '汉字转拼音工具', desc: '工具名称' },
  { text: '你好世界', desc: '问候语' }
]

// 使用工具执行器
const { execute, isLoading, error, result } = useToolExecutor<PinyinParams, PinyinResult>({
  toolCode: 'chinese-to-pinyin',
  toolType: ToolType.INSTANT
})

// 使用示例
const useExample = (text: string) => {
  inputText.value = text
  handleConvert()
}

// 执行转换
const handleConvert = async () => {
  if (!inputText.value.trim()) return

  await execute({
    text: inputText.value,
    ...options.value
  })
}

// 复制文本
const copyText = async (text: string) => {
  try {
    await navigator.clipboard.writeText(text)
    copySuccess.value = true
    setTimeout(() => copySuccess.value = false, 2000)
  } catch {
    // 复制失败
  }
}

// 重置
const reset = () => {
  inputText.value = ''
  options.value = {
    toneType: 'without_tone',
    caseType: 'lowercase',
    separator: 'space',
    keepNonChinese: true,
    extractInitials: false,
    detectPolyphone: false,
    showDetails: false
  }
}

// 监听选项变化自动转换
watch(options, () => {
  if (inputText.value.trim() && result.value) {
    handleConvert()
  }
}, { deep: true })

// 格式化拼音显示
const formattedPinyin = computed(() => {
  if (!result.value?.pinyin) return ''
  return result.value.pinyin
})

// 格式化首字母显示
const formattedInitials = computed(() => {
  if (!result.value?.initials) return ''
  return result.value.initials.toUpperCase()
})
</script>

<template>
  <NuxtLayout name="tool" toolCode="chinese-to-pinyin">
    <div class="max-w-5xl mx-auto space-y-6">
      <!-- 输入区域 -->
      <ToolCard>
        <div class="flex items-center gap-2 mb-6">
          <Type class="w-6 h-6 text-purple-500" />
          <h2 class="text-lg font-semibold text-gray-900">汉字转拼音</h2>
        </div>

        <div class="space-y-6">
          <!-- 文本输入 -->
          <div>
            <label class="block text-sm font-medium text-gray-700 mb-2">
              输入中文文本
            </label>
            <Textarea
              v-model="inputText"
              placeholder="请输入需要转换的中文内容，如：汉字转拼音"
              class="min-h-[120px] text-base"
            />
          </div>

          <!-- 转换选项 -->
          <div class="bg-gray-50 rounded-xl p-4 space-y-4">
            <div class="flex items-center gap-2 text-sm font-medium text-gray-700">
              <Settings2 class="w-4 h-4" />
              转换选项
            </div>

            <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
              <!-- 声调类型 -->
              <div>
                <label class="block text-xs text-gray-500 mb-1">声调显示</label>
                <select
                  v-model="options.toneType"
                  class="w-full px-3 py-2 bg-white border border-gray-200 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-purple-500"
                >
                  <option value="without_tone">不带声调</option>
                  <option value="with_tone">带声调符号</option>
                  <option value="tone_number">数字声调</option>
                </select>
              </div>

              <!-- 大小写 -->
              <div>
                <label class="block text-xs text-gray-500 mb-1">大小写</label>
                <select
                  v-model="options.caseType"
                  class="w-full px-3 py-2 bg-white border border-gray-200 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-purple-500"
                >
                  <option value="lowercase">小写</option>
                  <option value="uppercase">大写</option>
                  <option value="capitalize">首字母大写</option>
                </select>
              </div>

              <!-- 分隔符 -->
              <div>
                <label class="block text-xs text-gray-500 mb-1">分隔符</label>
                <select
                  v-model="options.separator"
                  class="w-full px-3 py-2 bg-white border border-gray-200 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-purple-500"
                >
                  <option value="space">空格</option>
                  <option value="none">无</option>
                  <option value="dash">连字符 (-)</option>
                  <option value="underline">下划线 (_)</option>
                </select>
              </div>
            </div>

            <!-- 复选框选项 -->
            <div class="flex flex-wrap gap-4 pt-2">
              <label class="flex items-center gap-2 cursor-pointer">
                <input
                  type="checkbox"
                  v-model="options.keepNonChinese"
                  class="w-4 h-4 text-purple-500 rounded focus:ring-purple-500"
                />
                <span class="text-sm text-gray-700">保留非中文字符</span>
              </label>
              <label class="flex items-center gap-2 cursor-pointer">
                <input
                  type="checkbox"
                  v-model="options.extractInitials"
                  class="w-4 h-4 text-purple-500 rounded focus:ring-purple-500"
                />
                <span class="text-sm text-gray-700">提取首字母</span>
              </label>
              <label class="flex items-center gap-2 cursor-pointer">
                <input
                  type="checkbox"
                  v-model="options.detectPolyphone"
                  class="w-4 h-4 text-purple-500 rounded focus:ring-purple-500"
                />
                <span class="text-sm text-gray-700">检测多音字</span>
              </label>
              <label class="flex items-center gap-2 cursor-pointer">
                <input
                  type="checkbox"
                  v-model="options.showDetails"
                  class="w-4 h-4 text-purple-500 rounded focus:ring-purple-500"
                />
                <span class="text-sm text-gray-700">显示逐字详情</span>
              </label>
            </div>
          </div>

          <!-- 错误提示 -->
          <div v-if="error" class="bg-red-50 text-red-700 p-4 rounded-lg">
            <AlertCircle class="w-5 h-5 inline mr-2" />
            {{ error }}
          </div>

          <!-- 操作按钮 -->
          <div class="flex items-center gap-3">
            <ToolButton
              variant="primary"
              @click="handleConvert"
              :disabled="isLoading || !inputText.trim()"
            >
              <Languages class="w-4 h-4 mr-1" />
              {{ isLoading ? '转换中...' : '开始转换' }}
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

      <!-- 转换结果 -->
      <ToolCard v-if="result">
        <div class="flex items-center gap-2 mb-6">
          <Sparkles class="w-6 h-6 text-purple-500" />
          <h2 class="text-lg font-semibold text-gray-900">转换结果</h2>
        </div>

        <div class="space-y-6">
          <!-- 拼音结果 -->
          <div class="bg-linear-to-r from-purple-50 to-violet-50 rounded-xl p-6 border border-purple-100">
            <div class="flex items-center justify-between mb-3">
              <span class="text-sm text-gray-600">拼音</span>
              <ToolButton
                variant="secondary"
                size="sm"
                @click="copyText(result.pinyin)"
              >
                <Copy class="w-4 h-4 mr-1" />
                复制
              </ToolButton>
            </div>
            <div class="text-xl font-medium text-gray-900 tracking-wide leading-relaxed">
              {{ formattedPinyin }}
            </div>
          </div>

          <!-- 首字母结果 -->
          <div v-if="result.initials" class="bg-linear-to-r from-blue-50 to-indigo-50 rounded-xl p-6 border border-blue-100">
            <div class="flex items-center justify-between mb-3">
              <span class="text-sm text-gray-600">首字母</span>
              <ToolButton
                variant="secondary"
                size="sm"
                @click="copyText(result.initials)"
              >
                <Copy class="w-4 h-4 mr-1" />
                复制
              </ToolButton>
            </div>
            <div class="text-xl font-medium text-gray-900 tracking-widest">
              {{ formattedInitials }}
            </div>
          </div>

          <!-- 文本统计 -->
          <div v-if="result.stats" class="grid grid-cols-2 md:grid-cols-5 gap-4">
            <div class="bg-gray-50 rounded-lg p-4 text-center">
              <div class="text-2xl font-bold text-gray-900">{{ result.stats.totalChars }}</div>
              <div class="text-xs text-gray-500 mt-1">总字符</div>
            </div>
            <div class="bg-purple-50 rounded-lg p-4 text-center">
              <div class="text-2xl font-bold text-purple-600">{{ result.stats.chineseChars }}</div>
              <div class="text-xs text-purple-600 mt-1">中文</div>
            </div>
            <div class="bg-blue-50 rounded-lg p-4 text-center">
              <div class="text-2xl font-bold text-blue-600">{{ result.stats.englishChars }}</div>
              <div class="text-xs text-blue-600 mt-1">英文</div>
            </div>
            <div class="bg-amber-50 rounded-lg p-4 text-center">
              <div class="text-2xl font-bold text-amber-600">{{ result.stats.numbers }}</div>
              <div class="text-xs text-amber-600 mt-1">数字</div>
            </div>
            <div class="bg-gray-50 rounded-lg p-4 text-center">
              <div class="text-2xl font-bold text-gray-600">{{ result.stats.otherChars }}</div>
              <div class="text-xs text-gray-500 mt-1">其他</div>
            </div>
          </div>

          <!-- 多音字检测 -->
          <div v-if="result.polyphones && result.polyphones.length > 0" class="bg-amber-50 rounded-xl p-6 border border-amber-100">
            <div class="flex items-center gap-2 mb-4">
              <AlertCircle class="w-5 h-5 text-amber-600" />
              <h3 class="font-semibold text-amber-900">检测到多音字</h3>
            </div>
            <div class="space-y-3">
              <div
                v-for="poly in result.polyphones"
                :key="poly.position"
                class="flex items-center gap-4 p-3 bg-white rounded-lg"
              >
                <span class="text-xl font-bold text-amber-600">{{ poly.character }}</span>
                <div class="flex-1">
                  <div class="text-sm text-gray-600">可能的读音：</div>
                  <div class="flex flex-wrap gap-2 mt-1">
                    <span
                      v-for="reading in poly.readings"
                      :key="reading"
                      class="px-2 py-1 bg-amber-100 text-amber-800 rounded text-sm"
                    >
                      {{ reading }}
                    </span>
                  </div>
                </div>
              </div>
            </div>
          </div>

          <!-- 逐字详情 -->
          <div v-if="result.details && result.details.length > 0">
            <h3 class="font-semibold text-gray-900 mb-4 flex items-center gap-2">
              <AlignLeft class="w-5 h-5 text-purple-500" />
              逐字详情
            </h3>
            <div class="overflow-x-auto">
              <table class="w-full text-sm">
                <thead class="bg-gray-50">
                  <tr>
                    <th class="px-4 py-2 text-left text-gray-600 font-medium">字符</th>
                    <th class="px-4 py-2 text-left text-gray-600 font-medium">带声调</th>
                    <th class="px-4 py-2 text-left text-gray-600 font-medium">不带声调</th>
                    <th class="px-4 py-2 text-left text-gray-600 font-medium">声母</th>
                    <th class="px-4 py-2 text-left text-gray-600 font-medium">多音字</th>
                  </tr>
                </thead>
                <tbody class="divide-y divide-gray-100">
                  <tr
                    v-for="(detail, index) in result.details"
                    :key="index"
                    class="hover:bg-gray-50"
                  >
                    <td class="px-4 py-3">
                      <span
                        :class="detail.chinese
                          ? 'text-lg font-bold text-purple-600'
                          : 'text-gray-400'"
                      >
                        {{ detail.character }}
                      </span>
                    </td>
                    <td class="px-4 py-3 text-gray-700">{{ detail.pinyinWithTone || '-' }}</td>
                    <td class="px-4 py-3 text-gray-700">{{ detail.pinyinWithoutTone || '-' }}</td>
                    <td class="px-4 py-3">
                      <span v-if="detail.initial" class="px-2 py-1 bg-blue-100 text-blue-700 rounded text-xs">
                        {{ detail.initial }}
                      </span>
                      <span v-else class="text-gray-400">-</span>
                    </td>
                    <td class="px-4 py-3">
                      <div v-if="detail.alternativeReadings && detail.alternativeReadings.length > 1" class="flex flex-wrap gap-1">
                        <span
                          v-for="reading in detail.alternativeReadings.slice(1)"
                          :key="reading"
                          class="px-2 py-1 bg-amber-100 text-amber-700 rounded text-xs"
                        >
                          {{ reading }}
                        </span>
                      </div>
                      <span v-else class="text-gray-400">-</span>
                    </td>
                  </tr>
                </tbody>
              </table>
            </div>
          </div>
        </div>
      </ToolCard>

      <!-- 快速示例 -->
      <ToolCard>
        <div class="flex items-center gap-2 mb-4">
          <Hash class="w-5 h-5 text-purple-500" />
          <h3 class="font-semibold text-gray-900">快速示例</h3>
        </div>
        <div class="grid grid-cols-2 md:grid-cols-4 gap-3">
          <button
            v-for="example in examples"
            :key="example.text"
            @click="useExample(example.text)"
            class="p-3 text-left rounded-lg border border-gray-200 hover:border-purple-300 hover:bg-purple-50 transition-all"
          >
            <div class="text-sm font-medium text-gray-900">{{ example.text }}</div>
            <div class="text-xs text-gray-500">{{ example.desc }}</div>
          </button>
        </div>
      </ToolCard>

      <!-- 使用说明 -->
      <ToolCard>
        <div class="flex items-center gap-2 mb-4">
          <CheckCircle2 class="w-5 h-5 text-purple-500" />
          <h3 class="font-semibold text-gray-900">使用说明</h3>
        </div>
        <div class="space-y-3 text-sm text-gray-600">
          <p><strong>声调显示：</strong>可选择带声调符号（如：hàn zì）、不带声调（如：han zi）或数字声调（如：han4 zi4）</p>
          <p><strong>大小写转换：</strong>支持小写、大写、首字母大写等多种格式</p>
          <p><strong>分隔符：</strong>可选择空格、连字符、下划线或不分隔</p>
          <p><strong>首字母提取：</strong>快速获取每个汉字拼音的首字母，适用于缩写生成</p>
          <p><strong>多音字检测：</strong>自动识别文本中的多音字，并显示可能的读音</p>
          <p><strong>逐字详情：</strong>显示每个字符的详细拼音信息，包括带声调、不带声调、声母等</p>
        </div>
      </ToolCard>
    </div>
  </NuxtLayout>
</template>
