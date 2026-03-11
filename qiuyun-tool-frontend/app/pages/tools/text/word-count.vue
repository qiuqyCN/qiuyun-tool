<script setup lang="ts">
import { ref, computed, watch } from 'vue'
import { Type, Copy, Check, AlertCircle, Trash2, BarChart3, Clock, Hash } from 'lucide-vue-next'
import { useToolExecutor } from '~/composables/useToolExecutor'
import { ToolType } from '~/types/tool'

// 请求参数
interface WordCountParams {
  text: string
  includeSpaces: boolean
  showWordFrequency: boolean
}

// 基础统计
interface BasicStats {
  totalChars: number
  charsWithoutSpaces: number
  lines: number
  paragraphs: number
  bytes: number
}

// 字符统计
interface CharStats {
  spaces: number
  newlines: number
  punctuation: number
  specialChars: number
}

// 中文统计
interface ChineseStats {
  chineseChars: number
  chinesePunctuation: number
}

// 英文统计
interface EnglishStats {
  englishWords: number
  englishLetters: number
}

// 数字统计
interface NumberStats {
  digits: number
  numberGroups: number
}

// 阅读时间
interface ReadingTime {
  seconds: number
  minutes: number
  hours: number
  formattedTime: string
}

// 词频
interface WordFrequency {
  word: string
  count: number
}

// 响应结果
interface WordCountResult {
  success: boolean
  basicStats: BasicStats
  charStats: CharStats
  chineseStats: ChineseStats
  englishStats: EnglishStats
  numberStats: NumberStats
  readingTime: ReadingTime
  wordFrequencies?: WordFrequency[]
  errorMessage?: string
}

// 状态
const text = ref('')
const includeSpaces = ref(true)
const showWordFrequency = ref(false)
const result = ref<WordCountResult | null>(null)
const error = ref('')

// Toast 提示
const toast = ref({ show: false, message: '' })
const showToast = (message: string) => {
  toast.value.message = message
  toast.value.show = true
  setTimeout(() => toast.value.show = false, 2000)
}

// 使用工具执行器
const { execute, isLoading } = useToolExecutor<WordCountParams, WordCountResult>({
  toolCode: 'word-count',
  toolType: ToolType.INSTANT,
  onSuccess: (res) => {
    if (res.success) {
      result.value = res
      error.value = ''
    } else {
      error.value = res.errorMessage || '统计失败'
      result.value = null
    }
  },
  onError: (err) => {
    error.value = err
    result.value = null
  }
})

// 执行统计
const countWords = async () => {
  await execute({
    text: text.value,
    includeSpaces: includeSpaces.value,
    showWordFrequency: showWordFrequency.value
  })
}

// 复制文本
const copyText = async () => {
  try {
    await navigator.clipboard.writeText(text.value)
    showToast('已复制')
  } catch {
    showToast('复制失败')
  }
}

// 清空
const clearAll = () => {
  text.value = ''
  result.value = null
  error.value = ''
}

// 监听输入变化，自动统计
watch([text, includeSpaces, showWordFrequency], () => {
  countWords()
}, { deep: true })

// 统计卡片数据
const statCards = computed(() => {
  if (!result.value) return []
  
  return [
    {
      title: '总字符数',
      value: result.value.basicStats.totalChars,
      icon: Type,
      color: 'text-blue-600 bg-blue-50'
    },
    {
      title: '不含空格',
      value: result.value.basicStats.charsWithoutSpaces,
      icon: Hash,
      color: 'text-green-600 bg-green-50'
    },
    {
      title: '中文字符',
      value: result.value.chineseStats.chineseChars,
      icon: Type,
      color: 'text-red-600 bg-red-50'
    },
    {
      title: '英文单词',
      value: result.value.englishStats.englishWords,
      icon: Type,
      color: 'text-purple-600 bg-purple-50'
    },
    {
      title: '行数',
      value: result.value.basicStats.lines,
      icon: BarChart3,
      color: 'text-orange-600 bg-orange-50'
    },
    {
      title: '段落数',
      value: result.value.basicStats.paragraphs,
      icon: BarChart3,
      color: 'text-pink-600 bg-pink-50'
    }
  ]
})
</script>

<template>
  <NuxtLayout name="tool" tool-code="word-count">
    <div class="border border-border/40 rounded-xl overflow-hidden">
      <!-- 配置选项 -->
      <div class="p-4 border-b bg-muted/20">
        <div class="flex flex-wrap items-center gap-4">
          <label class="flex items-center gap-2 text-sm cursor-pointer">
            <input
              v-model="showWordFrequency"
              type="checkbox"
              class="rounded border-gray-300"
            />
            <span>显示词频统计</span>
          </label>

          <div class="ml-auto">
            <button
              @click="clearAll"
              class="text-xs text-muted-foreground hover:text-foreground px-2 py-1 rounded hover:bg-muted transition-colors flex items-center gap-1"
            >
              <Trash2 class="w-3 h-3" />
              清空
            </button>
          </div>
        </div>
      </div>

      <div class="p-6 space-y-4">
        <!-- 输入区域 -->
        <div>
          <div class="flex items-center justify-between mb-2">
            <div class="flex items-center gap-2">
              <div class="w-1 h-4 bg-primary rounded-full"></div>
              <label class="text-sm font-medium">输入文本</label>
            </div>
            <button
              v-if="text"
              @click="copyText"
              class="text-xs text-muted-foreground hover:text-foreground px-2 py-1 rounded hover:bg-muted transition-colors"
            >
              <Copy class="w-3 h-3" />
            </button>
          </div>
          <Textarea
            v-model="text"
            placeholder="输入或粘贴要统计的文本内容..."
            rows="8"
            class="font-mono text-sm resize-none"
          />
        </div>

        <!-- 错误提示 -->
        <div
          v-if="error"
          class="flex items-center gap-2 text-sm text-red-500 bg-red-50/80 p-3 rounded-lg border border-red-200"
        >
          <AlertCircle class="w-4 h-4 shrink-0" />
          {{ error }}
        </div>

        <!-- 统计结果卡片 -->
        <div v-if="result" class="grid grid-cols-2 md:grid-cols-3 gap-4">
          <div
            v-for="card in statCards"
            :key="card.title"
            class="p-4 rounded-lg border"
            :class="card.color"
          >
            <div class="flex items-center gap-2 mb-1">
              <component :is="card.icon" class="w-4 h-4" />
              <span class="text-xs opacity-80">{{ card.title }}</span>
            </div>
            <div class="text-2xl font-bold">{{ card.value }}</div>
          </div>
        </div>

        <!-- 详细统计 -->
        <div v-if="result" class="grid grid-cols-1 md:grid-cols-2 gap-4">
          <!-- 字符统计 -->
          <div class="p-4 bg-muted/20 rounded-lg">
            <h4 class="text-sm font-medium mb-3 flex items-center gap-2">
              <Hash class="w-4 h-4" />
              字符统计
            </h4>
            <div class="space-y-2 text-sm">
              <div class="flex justify-between">
                <span class="text-muted-foreground">空格</span>
                <span>{{ result.charStats.spaces }}</span>
              </div>
              <div class="flex justify-between">
                <span class="text-muted-foreground">换行</span>
                <span>{{ result.charStats.newlines }}</span>
              </div>
              <div class="flex justify-between">
                <span class="text-muted-foreground">标点符号</span>
                <span>{{ result.charStats.punctuation }}</span>
              </div>
              <div class="flex justify-between">
                <span class="text-muted-foreground">特殊字符</span>
                <span>{{ result.charStats.specialChars }}</span>
              </div>
              <div class="flex justify-between">
                <span class="text-muted-foreground">字节数</span>
                <span>{{ result.basicStats.bytes }} bytes</span>
              </div>
            </div>
          </div>

          <!-- 语言统计 -->
          <div class="p-4 bg-muted/20 rounded-lg">
            <h4 class="text-sm font-medium mb-3 flex items-center gap-2">
              <Type class="w-4 h-4" />
              语言统计
            </h4>
            <div class="space-y-2 text-sm">
              <div class="flex justify-between">
                <span class="text-muted-foreground">中文字符</span>
                <span>{{ result.chineseStats.chineseChars }}</span>
              </div>
              <div class="flex justify-between">
                <span class="text-muted-foreground">中文标点</span>
                <span>{{ result.chineseStats.chinesePunctuation }}</span>
              </div>
              <div class="flex justify-between">
                <span class="text-muted-foreground">英文单词</span>
                <span>{{ result.englishStats.englishWords }}</span>
              </div>
              <div class="flex justify-between">
                <span class="text-muted-foreground">英文字母</span>
                <span>{{ result.englishStats.englishLetters }}</span>
              </div>
              <div class="flex justify-between">
                <span class="text-muted-foreground">数字个数</span>
                <span>{{ result.numberStats.digits }}</span>
              </div>
            </div>
          </div>
        </div>

        <!-- 阅读时间 -->
        <div v-if="result?.readingTime" class="p-4 bg-blue-50/50 rounded-lg border border-blue-100">
          <div class="flex items-center gap-2 mb-2">
            <Clock class="w-4 h-4 text-blue-600" />
            <span class="text-sm font-medium text-blue-600">预计阅读时间</span>
          </div>
          <div class="text-2xl font-bold text-blue-700">
            {{ result.readingTime.formattedTime }}
          </div>
          <div class="text-xs text-muted-foreground mt-1">
            基于 {{ result.chineseStats.chineseChars }} 中文字符 + {{ result.englishStats.englishWords }} 英文单词计算
          </div>
        </div>

        <!-- 词频统计 -->
        <div v-if="result?.wordFrequencies?.length" class="space-y-3">
          <div class="flex items-center gap-2">
            <div class="w-1 h-4 bg-purple-500 rounded-full"></div>
            <label class="text-sm font-medium">词频统计 (Top 10)</label>
          </div>
          <div class="grid grid-cols-2 md:grid-cols-5 gap-2">
            <div
              v-for="item in result.wordFrequencies"
              :key="item.word"
              class="p-2 bg-muted/20 rounded border text-center"
            >
              <div class="font-mono text-sm">{{ item.word }}</div>
              <div class="text-xs text-muted-foreground">{{ item.count }} 次</div>
            </div>
          </div>
        </div>

        <!-- 说明 -->
        <div class="p-3 bg-blue-50/50 rounded-lg border border-blue-100">
          <div class="text-xs text-blue-600 font-medium mb-2">字数统计说明:</div>
          <ul class="text-xs text-muted-foreground space-y-1 list-disc list-inside">
            <li>总字符数包含所有字符（含空格、标点等）</li>
            <li>中文字符统计 CJK 统一表意文字范围</li>
            <li>英文单词按连续字母识别</li>
            <li>阅读时间按中文 400 字/分钟、英文 200 词/分钟估算</li>
            <li>词频统计包含中文字符和英文单词</li>
          </ul>
        </div>
      </div>
    </div>

    <!-- Toast 提示 -->
    <div
      v-if="toast.show"
      class="fixed bottom-4 right-4 bg-green-500 text-white px-4 py-2 rounded-lg shadow-lg flex items-center gap-2 z-50"
    >
      <Check class="w-4 h-4" />
      {{ toast.message }}
    </div>
  </NuxtLayout>
</template>
