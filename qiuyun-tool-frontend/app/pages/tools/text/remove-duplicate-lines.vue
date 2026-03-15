<script setup lang="ts">
import { ref, watch } from 'vue'
import { Copy, Check, AlertCircle, Trash2, AlignLeft, Filter } from 'lucide-vue-next'

interface DuplicateLineInfo {
  line: string
  count: number
}

interface RemoveDuplicateLinesResult {
  success: boolean
  resultText: string
  originalLines: number
  resultLines: number
  removedDuplicates: number
  duplicateLineInfos?: DuplicateLineInfo[]
}

const text = ref('')
const ignoreCase = ref(false)
const ignoreWhitespace = ref(false)
const keepFirst = ref(true)
const sortLines = ref(false)
const showStats = ref(true)
const result = ref<RemoveDuplicateLinesResult | null>(null)
const error = ref('')

const toast = ref({ show: false, message: '' })
const showToast = (message: string) => {
  toast.value.message = message
  toast.value.show = true
  setTimeout(() => toast.value.show = false, 2000)
}

const removeDuplicates = () => {
  if (!text.value) {
    result.value = null
    error.value = ''
    return
  }

  const lines = text.value.split(/\r?\n/)
  const resultLines: string[] = []
  const seen = new Map<string, number>()
  const lineCounts = new Map<string, number>()
  let duplicateCount = 0

  for (const line of lines) {
    let processedLine = line

    if (ignoreWhitespace.value) {
      processedLine = processedLine.trim()
    }

    if (processedLine === '') {
      resultLines.push(line)
      continue
    }

    const key = ignoreCase.value ? processedLine.toLowerCase() : processedLine

    if (seen.has(key)) {
      duplicateCount++
      lineCounts.set(key, (lineCounts.get(key) || 1) + 1)
      if (!keepFirst.value) {
        resultLines.push(line)
      }
    } else {
      seen.set(key, resultLines.length)
      lineCounts.set(key, 1)
      if (keepFirst.value) {
        resultLines.push(line)
      }
    }
  }

  if (!keepFirst.value) {
    const uniqueLines = new Map<string, string>()
    for (let i = lines.length - 1; i >= 0; i--) {
      const line = lines[i]!
      let processedLine = line
      if (ignoreWhitespace.value) {
        processedLine = processedLine.trim()
      }
      if (processedLine === '') {
        uniqueLines.set(`empty-${i}`, line)
        continue
      }
      const key = ignoreCase.value ? processedLine.toLowerCase() : processedLine
      if (!uniqueLines.has(key)) {
        uniqueLines.set(key, line)
      }
    }
    resultLines.length = 0
    for (const line of uniqueLines.values()) {
      resultLines.unshift(line)
    }
  }

  if (sortLines.value) {
    const nonEmptyLines: string[] = []
    const emptyLinesResult: string[] = []

    for (const line of resultLines) {
      if (line.trim() === '') {
        emptyLinesResult.push(line)
      } else {
        nonEmptyLines.push(line)
      }
    }

    if (ignoreCase.value) {
      nonEmptyLines.sort((a, b) => a.localeCompare(b, undefined, { sensitivity: 'base' }))
    } else {
      nonEmptyLines.sort()
    }

    resultLines.length = 0
    resultLines.push(...nonEmptyLines, ...emptyLinesResult)
  }

  const resultText = resultLines.join('\n')

  const duplicateLineInfos: DuplicateLineInfo[] = []
  for (const [key, count] of lineCounts.entries()) {
    if (count > 1) {
      duplicateLineInfos.push({ line: key, count })
    }
  }
  duplicateLineInfos.sort((a, b) => b.count - a.count)

  result.value = {
    success: true,
    resultText,
    originalLines: lines.length,
    resultLines: resultLines.length,
    removedDuplicates: duplicateCount,
    duplicateLineInfos: showStats.value ? duplicateLineInfos : undefined
  }
  error.value = ''
}

const copyText = async (textToCopy: string) => {
  try {
    await navigator.clipboard.writeText(textToCopy)
    showToast('已复制')
  } catch {
    showToast('复制失败')
  }
}

const clearAll = () => {
  text.value = ''
  result.value = null
  error.value = ''
}

watch([text, ignoreCase, ignoreWhitespace, keepFirst, sortLines, showStats], () => {
  removeDuplicates()
}, { deep: true })
</script>

<template>
  <NuxtLayout name="tool" tool-code="remove-duplicate-lines">
    <div class="border border-border/40 rounded-xl overflow-hidden">
      <div class="p-4 border-b bg-muted/20">
        <div class="flex flex-wrap items-center gap-4">
          <label class="flex items-center gap-2 text-sm cursor-pointer">
            <input
              v-model="ignoreCase"
              type="checkbox"
              class="rounded border-gray-300"
            />
            <span>忽略大小写</span>
          </label>
          <label class="flex items-center gap-2 text-sm cursor-pointer">
            <input
              v-model="ignoreWhitespace"
              type="checkbox"
              class="rounded border-gray-300"
            />
            <span>忽略首尾空白</span>
          </label>
          <label class="flex items-center gap-2 text-sm cursor-pointer">
            <input
              v-model="keepFirst"
              type="checkbox"
              class="rounded border-gray-300"
            />
            <span>保留首次出现</span>
          </label>
          <label class="flex items-center gap-2 text-sm cursor-pointer">
            <input
              v-model="sortLines"
              type="checkbox"
              class="rounded border-gray-300"
            />
            <span>结果排序</span>
          </label>
          <label class="flex items-center gap-2 text-sm cursor-pointer">
            <input
              v-model="showStats"
              type="checkbox"
              class="rounded border-gray-300"
            />
            <span>显示统计信息</span>
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
        <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
          <div>
            <div class="flex items-center justify-between mb-2">
              <div class="flex items-center gap-2">
                <div class="w-1 h-4 bg-primary rounded-full"></div>
                <label class="text-sm font-medium">输入文本</label>
              </div>
            </div>
            <Textarea
              v-model="text"
              placeholder="输入或粘贴要处理的文本内容..."
              rows="12"
              class="font-mono text-sm resize-none"
            />
          </div>

          <div>
            <div class="flex items-center justify-between mb-2">
              <div class="flex items-center gap-2">
                <div class="w-1 h-4 bg-green-500 rounded-full"></div>
                <label class="text-sm font-medium">处理结果</label>
              </div>
              <button
                v-if="result?.resultText"
                @click="copyText(result.resultText)"
                class="text-xs text-muted-foreground hover:text-foreground px-2 py-1 rounded hover:bg-muted transition-colors"
              >
                <Copy class="w-3 h-3" />
              </button>
            </div>
            <Textarea
              :value="result?.resultText || ''"
              placeholder="处理结果将显示在这里..."
              rows="12"
              class="font-mono text-sm resize-none"
              readonly
            />
          </div>
        </div>

        <div
          v-if="error"
          class="flex items-center gap-2 text-sm text-red-500 bg-red-50/80 p-3 rounded-lg border border-red-200"
        >
          <AlertCircle class="w-4 h-4 shrink-0" />
          {{ error }}
        </div>

        <div v-if="result && showStats" class="grid grid-cols-2 md:grid-cols-4 gap-4">
          <div class="p-4 rounded-lg border text-blue-600 bg-blue-50">
            <div class="flex items-center gap-2 mb-1">
              <AlignLeft class="w-4 h-4" />
              <span class="text-xs opacity-80">原行数</span>
            </div>
            <div class="text-2xl font-bold">{{ result.originalLines }}</div>
          </div>
          <div class="p-4 rounded-lg border text-green-600 bg-green-50">
            <div class="flex items-center gap-2 mb-1">
              <AlignLeft class="w-4 h-4" />
              <span class="text-xs opacity-80">结果行数</span>
            </div>
            <div class="text-2xl font-bold">{{ result.resultLines }}</div>
          </div>
          <div class="p-4 rounded-lg border text-orange-600 bg-orange-50">
            <div class="flex items-center gap-2 mb-1">
              <Filter class="w-4 h-4" />
              <span class="text-xs opacity-80">删除重复</span>
            </div>
            <div class="text-2xl font-bold">{{ result.removedDuplicates }}</div>
          </div>
          <div class="p-4 rounded-lg border text-purple-600 bg-purple-50">
            <div class="flex items-center gap-2 mb-1">
              <Check class="w-4 h-4" />
              <span class="text-xs opacity-80">去重率</span>
            </div>
            <div class="text-2xl font-bold">
              {{ result.originalLines > 0 ? Math.round((result.removedDuplicates / result.originalLines) * 100) : 0 }}%
            </div>
          </div>
        </div>

        <div v-if="result?.duplicateLineInfos?.length && showStats" class="space-y-3">
          <div class="flex items-center gap-2">
            <div class="w-1 h-4 bg-purple-500 rounded-full"></div>
            <label class="text-sm font-medium">重复行详情 (Top 10)</label>
          </div>
          <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-2">
            <div
              v-for="item in result.duplicateLineInfos.slice(0, 10)"
              :key="item.line"
              class="p-2 bg-muted/20 rounded border text-center"
            >
              <div class="font-mono text-sm truncate" :title="item.line">
                {{ item.line.length > 20 ? item.line.substring(0, 20) + '...' : item.line || '(空行)' }}
              </div>
              <div class="text-xs text-muted-foreground">{{ item.count }} 次</div>
            </div>
          </div>
        </div>

        <div class="p-3 bg-blue-50/50 dark:bg-blue-900/20 rounded-lg border border-blue-100 dark:border-blue-800">
          <div class="text-xs text-blue-600 dark:text-blue-400 font-medium mb-2">重复行删除说明:</div>
          <ul class="text-xs text-gray-600 dark:text-gray-300 space-y-1 list-disc list-inside">
            <li>默认保留首次出现的行，可选择保留最后出现的行</li>
            <li>忽略大小写时，"Hello" 和 "hello" 视为重复</li>
            <li>忽略首尾空白时，"  test  " 和 "test" 视为重复</li>
            <li>空行也会被去重处理</li>
            <li>结果排序按字母顺序排列</li>
          </ul>
        </div>
      </div>
    </div>

    <div
      v-if="toast.show"
      class="fixed bottom-4 right-4 bg-green-500 text-white px-4 py-2 rounded-lg shadow-lg flex items-center gap-2 z-50"
    >
      <Check class="w-4 h-4" />
      {{ toast.message }}
    </div>
  </NuxtLayout>
</template>
