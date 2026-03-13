<script setup lang="ts">
import { ref, computed, watch } from 'vue'
import { GitCompare, Copy, Check, AlertCircle, Trash2, FileText, Type } from 'lucide-vue-next'
import { useToolExecutor } from '~/composables/useToolExecutor'
import { ToolType } from '~/types/tool'

// 对比模式
enum CompareMode {
  LINE = 'line',
  CHAR = 'char'
}

// 请求参数
interface TextCompareParams {
  oldText: string
  newText: string
  mode: string
}

// 字符差异
interface CharDiff {
  type: string
  oldChar?: string
  newChar?: string
  oldIndex?: number
  newIndex?: number
}

// 差异行
interface DiffLine {
  type: string
  oldLineNum?: number
  newLineNum?: number
  oldContent?: string
  newContent?: string
  charDiffs?: CharDiff[]
}

// 文本统计
interface TextStats {
  length: number
  lines: number
  words: number
  chars: number
}

// 差异统计
interface DiffStats {
  added: number
  removed: number
  modified: number
  unchanged: number
}

// 响应结果
interface TextCompareResult {
  success: boolean
  mode: string
  oldStats: TextStats
  newStats: TextStats
  diffStats: DiffStats
  diffLines: DiffLine[]
  errorMessage?: string
}

// 状态
const oldText = ref('')
const newText = ref('')
const mode = ref<CompareMode>(CompareMode.LINE)
const result = ref<TextCompareResult | null>(null)
const error = ref('')

// Toast 提示
const toast = ref({ show: false, message: '' })
const showToast = (message: string) => {
  toast.value.message = message
  toast.value.show = true
  setTimeout(() => toast.value.show = false, 2000)
}

// 使用工具执行器
const { execute, isLoading } = useToolExecutor<TextCompareParams, TextCompareResult>({
  toolCode: 'text-compare',
  toolType: ToolType.INSTANT,
  onSuccess: (res) => {
    if (res.success) {
      result.value = res
      error.value = ''
    } else {
      error.value = res.errorMessage || '对比失败'
      result.value = null
    }
  },
  onError: (err) => {
    error.value = err
    result.value = null
  }
})

// 执行对比
const compare = async () => {
  await execute({
    oldText: oldText.value,
    newText: newText.value,
    mode: mode.value
  })
}

// 清空
const clearAll = () => {
  oldText.value = ''
  newText.value = ''
  result.value = null
  error.value = ''
}

// 复制文本
const copyText = async (text: string) => {
  try {
    await navigator.clipboard.writeText(text)
    showToast('已复制')
  } catch {
    showToast('复制失败')
  }
}

// 监听输入变化，自动对比
watch([oldText, newText, mode], () => {
  compare()
}, { deep: true })

// 差异类型样式
const getDiffTypeClass = (type: string): string => {
  switch (type) {
    case 'added':
      return 'bg-green-50 border-green-200'
    case 'removed':
      return 'bg-red-50 border-red-200'
    case 'modified':
      return 'bg-yellow-50 border-yellow-200'
    default:
      return 'bg-white border-gray-100'
  }
}

const getDiffTypeLabel = (type: string): string => {
  switch (type) {
    case 'added':
      return '新增'
    case 'removed':
      return '删除'
    case 'modified':
      return '修改'
    default:
      return ''
  }
}

const getDiffTypeColor = (type: string): string => {
  switch (type) {
    case 'added':
      return 'text-green-600 bg-green-100'
    case 'removed':
      return 'text-red-600 bg-red-100'
    case 'modified':
      return 'text-yellow-600 bg-yellow-100'
    default:
      return 'text-gray-500 bg-gray-100'
  }
}
</script>

<template>
  <NuxtLayout name="tool" tool-code="text-compare">
    <div class="border border-border/40 rounded-xl overflow-hidden">
      <!-- 配置选项 -->
      <div class="p-4 border-b bg-muted/20">
        <div class="flex flex-wrap items-center gap-4">
          <!-- 对比模式 -->
          <div class="flex items-center gap-2">
            <span class="text-sm text-muted-foreground">对比模式:</span>
            <div class="flex gap-1">
              <button
                @click="mode = CompareMode.LINE"
                class="px-3 py-1.5 text-sm rounded transition-colors"
                :class="mode === CompareMode.LINE ? 'bg-primary text-primary-foreground' : 'bg-muted hover:bg-muted/80'"
              >
                行对比
              </button>
              <button
                @click="mode = CompareMode.CHAR"
                class="px-3 py-1.5 text-sm rounded transition-colors"
                :class="mode === CompareMode.CHAR ? 'bg-primary text-primary-foreground' : 'bg-muted hover:bg-muted/80'"
              >
                字符对比
              </button>
            </div>
          </div>

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
        <div class="grid grid-cols-1 lg:grid-cols-2 gap-4">
          <!-- 旧文本 -->
          <div class="flex flex-col gap-2">
            <div class="flex items-center justify-between">
              <div class="flex items-center gap-2">
                <div class="w-1 h-4 bg-red-400 rounded-full"></div>
                <label class="text-sm font-medium">原文本</label>
              </div>
              <button
                v-if="oldText"
                @click="copyText(oldText)"
                class="text-xs text-muted-foreground hover:text-foreground px-2 py-1 rounded hover:bg-muted transition-colors"
              >
                <Copy class="w-3 h-3" />
              </button>
            </div>
            <Textarea
              v-model="oldText"
              placeholder="输入原始文本..."
              rows="8"
              class="font-mono text-sm resize-none"
            />
          </div>

          <!-- 新文本 -->
          <div class="flex flex-col gap-2">
            <div class="flex items-center justify-between">
              <div class="flex items-center gap-2">
                <div class="w-1 h-4 bg-green-400 rounded-full"></div>
                <label class="text-sm font-medium">新文本</label>
              </div>
              <button
                v-if="newText"
                @click="copyText(newText)"
                class="text-xs text-muted-foreground hover:text-foreground px-2 py-1 rounded hover:bg-muted transition-colors"
              >
                <Copy class="w-3 h-3" />
              </button>
            </div>
            <Textarea
              v-model="newText"
              placeholder="输入新文本..."
              rows="8"
              class="font-mono text-sm resize-none"
            />
          </div>
        </div>

        <!-- 错误提示 -->
        <div
          v-if="error"
          class="flex items-center gap-2 text-sm text-red-500 bg-red-50/80 p-3 rounded-lg border border-red-200"
        >
          <AlertCircle class="w-4 h-4 shrink-0" />
          {{ error }}
        </div>

        <!-- 统计信息 -->
        <div v-if="result" class="grid grid-cols-2 lg:grid-cols-4 gap-4">
          <div class="p-3 bg-muted/20 rounded-lg">
            <div class="text-xs text-muted-foreground mb-1">原文本字符</div>
            <div class="text-lg font-semibold">{{ result.oldStats.length }}</div>
          </div>
          <div class="p-3 bg-muted/20 rounded-lg">
            <div class="text-xs text-muted-foreground mb-1">新文本字符</div>
            <div class="text-lg font-semibold">{{ result.newStats.length }}</div>
          </div>
          <div class="p-3 bg-muted/20 rounded-lg">
            <div class="text-xs text-muted-foreground mb-1">新增/删除</div>
            <div class="text-lg font-semibold">
              <span class="text-green-600">+{{ result.diffStats.added }}</span>
              <span class="text-gray-400 mx-1">/</span>
              <span class="text-red-600">-{{ result.diffStats.removed }}</span>
            </div>
          </div>
          <div class="p-3 bg-muted/20 rounded-lg">
            <div class="text-xs text-muted-foreground mb-1">修改/未变</div>
            <div class="text-lg font-semibold">
              <span class="text-yellow-600">~{{ result.diffStats.modified }}</span>
              <span class="text-gray-400 mx-1">/</span>
              <span class="text-gray-600">={{ result.diffStats.unchanged }}</span>
            </div>
          </div>
        </div>

        <!-- 对比结果 -->
        <div v-if="result?.diffLines?.length" class="space-y-2">
          <div class="flex items-center gap-2">
            <div class="w-1 h-4 bg-primary rounded-full"></div>
            <label class="text-sm font-medium">对比结果</label>
          </div>

          <div class="border rounded-lg overflow-hidden">
            <div
              v-for="(line, index) in result.diffLines"
              :key="index"
              class="flex items-start gap-2 p-2 border-b last:border-b-0 text-sm"
              :class="getDiffTypeClass(line.type)"
            >
              <!-- 行号 -->
              <div class="flex gap-1 text-xs text-muted-foreground w-20 shrink-0">
                <span class="w-8 text-right">{{ line.oldLineNum || '' }}</span>
                <span class="text-gray-300">|</span>
                <span class="w-8">{{ line.newLineNum || '' }}</span>
              </div>

              <!-- 类型标签 -->
              <span
                v-if="line.type !== 'unchanged'"
                class="text-xs px-1.5 py-0.5 rounded shrink-0"
                :class="getDiffTypeColor(line.type)"
              >
                {{ getDiffTypeLabel(line.type) }}
              </span>

              <!-- 内容 -->
              <div class="flex-1 font-mono break-all">
                <!-- 新增或删除 -->
                <div v-if="line.type === 'added'" class="text-green-700">
                  + {{ line.newContent }}
                </div>
                <div v-else-if="line.type === 'removed'" class="text-red-700">
                  - {{ line.oldContent }}
                </div>
                <!-- 修改 -->
                <div v-else-if="line.type === 'modified'" class="space-y-1">
                  <div class="text-red-700 line-through">- {{ line.oldContent }}</div>
                  <div class="text-green-700">+ {{ line.newContent }}</div>
                </div>
                <!-- 未变更 -->
                <div v-else class="text-gray-600">
                  {{ line.oldContent }}
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- 说明 -->
        <div class="p-3 bg-blue-50/50 dark:bg-blue-900/20 rounded-lg border border-blue-100 dark:border-blue-800">
          <div class="text-xs text-blue-600 dark:text-blue-400 font-medium mb-2">文本对比说明:</div>
          <ul class="text-xs text-gray-600 dark:text-gray-300 space-y-1 list-disc list-inside">
            <li><span class="text-green-600 dark:text-green-400 font-medium">绿色</span> 表示新增的内容</li>
            <li><span class="text-red-600 dark:text-red-400 font-medium">红色</span> 表示删除的内容</li>
            <li><span class="text-yellow-600 dark:text-yellow-400 font-medium">黄色</span> 表示修改的内容</li>
            <li>行对比模式适合查看代码或文档的差异</li>
            <li>字符对比模式适合查看短文本的详细差异</li>
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
