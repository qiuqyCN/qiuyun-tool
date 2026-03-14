<script setup lang="ts">
import { ref, computed, watch, onMounted } from 'vue'
import {
  Image,
  Download,
  Copy,
  Check,
  Upload,
  Trash2,
  Settings,
  RefreshCw,
  FileCode,
  CheckCircle,
  XCircle,
  AlertTriangle,
  ZoomIn,
  ZoomOut,
  RotateCcw
} from 'lucide-vue-next'
import { useToolExecutor } from '~/composables/useToolExecutor'
import { useDebounce } from '~/composables/useDebounce'
import { useClipboard } from '~/composables/useClipboard'
import { ToolType } from '~/types/tool'

enum Operation {
  OPTIMIZE = 'optimize',
  MINIFY = 'minify',
  TO_PNG = 'toPng',
  FORMAT = 'format',
  VALIDATE = 'validate'
}

interface EditorParams {
  svgContent: string
  operation: Operation
  width?: number
  height?: number
}

interface EditorResult {
  success: boolean
  svgContent?: string
  pngBase64?: string
  operation: string
  originalSize?: number
  optimizedSize?: number
  width?: number
  height?: number
  valid?: boolean
  validationMessage?: string
  errors?: string[]
  errorMessage?: string
}

const { copy } = useClipboard()

const toast = ref({
  show: false,
  message: '',
  type: 'success' as 'success' | 'error'
})

const showToast = (message: string, type: 'success' | 'error' = 'success') => {
  toast.value = { show: true, message, type }
  setTimeout(() => toast.value.show = false, 2000)
}

const svgContent = ref('')
const operation = ref<Operation>(Operation.FORMAT)
const width = ref<number>(800)
const height = ref<number>(600)
const result = ref<EditorResult | null>(null)
const previewZoom = ref(1)
const previewRef = ref<HTMLDivElement | null>(null)

const operationOptions = [
  { value: Operation.FORMAT, label: '格式化', description: '美化SVG代码，便于阅读' },
  { value: Operation.OPTIMIZE, label: '优化', description: '移除冗余属性和空元素' },
  { value: Operation.MINIFY, label: '压缩', description: '移除空白和注释，减小体积' },
  { value: Operation.TO_PNG, label: '转PNG', description: '将SVG转换为PNG图片' },
  { value: Operation.VALIDATE, label: '验证', description: '检查SVG格式是否正确' }
]

const hasContent = computed(() => svgContent.value.trim().length > 0)

const canExecute = computed(() => hasContent.value)

const resultStats = computed(() => {
  if (!result.value) return null
  if (result.value.originalSize && result.value.optimizedSize) {
    const saved = result.value.originalSize - result.value.optimizedSize
    const percent = ((saved / result.value.originalSize) * 100).toFixed(1)
    return {
      original: result.value.originalSize,
      optimized: result.value.optimizedSize,
      saved,
      percent
    }
  }
  return null
})

const { execute, isLoading } = useToolExecutor<EditorParams, EditorResult>({
  toolCode: 'svg-editor',
  toolType: ToolType.INSTANT,
  onSuccess: (res) => {
    result.value = res
  },
  onError: (err) => {
    showToast(err, 'error')
  }
})

const { debouncedFn: debouncedValidate } = useDebounce(
  () => {
    if (hasContent.value && operation.value === Operation.VALIDATE) {
      executeOperation()
    }
  },
  { delay: 500 }
)

watch(svgContent, () => {
  if (operation.value === Operation.VALIDATE) {
    debouncedValidate()
  }
})

const executeOperation = async () => {
  if (!canExecute.value) {
    showToast('请输入SVG内容', 'error')
    return
  }
  
  const params: EditorParams = {
    svgContent: svgContent.value,
    operation: operation.value
  }
  
  if (operation.value === Operation.TO_PNG) {
    params.width = width.value
    params.height = height.value
  }
  
  await execute(params)
}

const handleFileUpload = (event: Event) => {
  const file = (event.target as HTMLInputElement).files?.[0]
  if (!file) return
  
  if (!file.name.toLowerCase().endsWith('.svg')) {
    showToast('请上传SVG文件', 'error')
    return
  }
  
  const reader = new FileReader()
  reader.onload = (e) => {
    svgContent.value = e.target?.result as string
    showToast('文件已加载')
  }
  reader.readAsText(file)
}

const downloadResult = () => {
  if (!result.value) return
  
  let content: string
  let filename: string
  let mimeType: string
  
  if (result.value.pngBase64) {
    const link = document.createElement('a')
    link.href = result.value.pngBase64
    link.download = `svg-export-${Date.now()}.png`
    link.click()
    showToast('已下载PNG')
    return
  } else if (result.value.svgContent) {
    content = result.value.svgContent
    filename = `svg-${operation.value}-${Date.now()}.svg`
    mimeType = 'image/svg+xml'
  } else {
    return
  }
  
  const blob = new Blob([content], { type: mimeType })
  const url = URL.createObjectURL(blob)
  const link = document.createElement('a')
  link.href = url
  link.download = filename
  link.click()
  URL.revokeObjectURL(url)
  showToast('已下载')
}

const copyResult = async () => {
  if (!result.value?.svgContent) return
  
  const success = await copy(result.value.svgContent)
  if (success) {
    showToast('已复制到剪贴板')
  } else {
    showToast('复制失败', 'error')
  }
}

const clearAll = () => {
  svgContent.value = ''
  result.value = null
  previewZoom.value = 1
}

const zoomIn = () => {
  previewZoom.value = Math.min(previewZoom.value * 1.2, 5)
}

const zoomOut = () => {
  previewZoom.value = Math.max(previewZoom.value / 1.2, 0.1)
}

const resetZoom = () => {
  previewZoom.value = 1
}

const sampleSvg = `<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 100 100" width="100" height="100">
  <circle cx="50" cy="50" r="40" fill="#4F46E5" stroke="#312E81" stroke-width="2"/>
  <rect x="30" y="30" width="40" height="40" fill="#818CF8" rx="5"/>
  <path d="M20 80 L50 50 L80 80" stroke="#C7D2FE" stroke-width="3" fill="none"/>
</svg>`

const loadSample = () => {
  svgContent.value = sampleSvg
  showToast('已加载示例SVG')
}

const getValidationIcon = () => {
  if (!result.value) return null
  if (result.value.valid) return CheckCircle
  if (result.value.errors && result.value.errors.length > 0) return XCircle
  return AlertTriangle
}

const getValidationColor = () => {
  if (!result.value) return ''
  if (result.value.valid) return 'text-green-500'
  if (result.value.errors && result.value.errors.length > 0) return 'text-red-500'
  return 'text-yellow-500'
}
</script>

<template>
  <NuxtLayout name="tool" tool-code="svg-editor">
    <div class="border border-border/40 rounded-xl overflow-hidden bg-card">
      <div class="border-b bg-muted/30 px-6 py-4">
        <div class="flex items-center gap-2">
          <Image class="w-5 h-5 text-primary" />
          <h2 class="text-lg font-semibold">SVG编辑器</h2>
        </div>
        <p class="text-sm text-muted-foreground mt-1">
          在线编辑、优化、格式化和转换SVG图片
        </p>
      </div>

      <div class="p-6">
        <div class="grid grid-cols-1 lg:grid-cols-2 gap-6">
          <div class="space-y-4">
            <div class="flex items-center justify-between">
              <label class="text-sm font-medium">SVG代码</label>
              <div class="flex gap-2">
                <label class="cursor-pointer">
                  <input type="file" accept=".svg" class="hidden" @change="handleFileUpload" />
                  <span class="inline-flex items-center gap-1 px-3 py-1 text-xs border rounded-lg hover:bg-muted transition-colors">
                    <Upload class="w-3 h-3" />
                    上传文件
                  </span>
                </label>
                <button
                  @click="loadSample"
                  class="inline-flex items-center gap-1 px-3 py-1 text-xs border rounded-lg hover:bg-muted transition-colors"
                >
                  <FileCode class="w-3 h-3" />
                  加载示例
                </button>
              </div>
            </div>
            
            <div class="relative">
              <textarea
                v-model="svgContent"
                placeholder="粘贴SVG代码或上传SVG文件..."
                rows="16"
                class="w-full px-4 py-3 border rounded-lg bg-background font-mono text-sm focus:outline-none focus:ring-2 focus:ring-primary resize-none"
              />
              <button
                v-if="svgContent"
                @click="svgContent = ''"
                class="absolute top-2 right-2 p-1 text-muted-foreground hover:text-foreground"
              >
                <Trash2 class="w-4 h-4" />
              </button>
            </div>

            <div>
              <label class="text-sm font-medium mb-2 block">操作类型</label>
              <div class="grid grid-cols-2 sm:grid-cols-3 gap-2">
                <button
                  v-for="opt in operationOptions"
                  :key="opt.value"
                  @click="operation = opt.value"
                  class="p-3 rounded-lg border text-left transition-colors"
                  :class="operation === opt.value
                    ? 'border-primary bg-primary/10'
                    : 'border-border hover:border-primary/50'"
                >
                  <div class="text-sm font-medium">{{ opt.label }}</div>
                  <div class="text-xs text-muted-foreground mt-0.5">{{ opt.description }}</div>
                </button>
              </div>
            </div>

            <div v-if="operation === Operation.TO_PNG" class="grid grid-cols-2 gap-4">
              <div>
                <label class="text-sm font-medium mb-2 block">宽度 (px)</label>
                <input
                  v-model.number="width"
                  type="number"
                  min="1"
                  max="4096"
                  class="w-full px-3 py-2 border rounded-lg bg-background focus:outline-none focus:ring-2 focus:ring-primary"
                />
              </div>
              <div>
                <label class="text-sm font-medium mb-2 block">高度 (px)</label>
                <input
                  v-model.number="height"
                  type="number"
                  min="1"
                  max="4096"
                  class="w-full px-3 py-2 border rounded-lg bg-background focus:outline-none focus:ring-2 focus:ring-primary"
                />
              </div>
            </div>

            <div class="flex gap-2">
              <button
                @click="executeOperation"
                :disabled="isLoading || !canExecute"
                class="flex-1 flex items-center justify-center gap-2 px-4 py-2.5 bg-primary text-primary-foreground rounded-lg hover:bg-primary/90 disabled:opacity-50"
              >
                <RefreshCw v-if="isLoading" class="w-4 h-4 animate-spin" />
                <Settings v-else class="w-4 h-4" />
                {{ isLoading ? '处理中...' : '执行操作' }}
              </button>
              <button
                @click="clearAll"
                class="px-4 py-2.5 border rounded-lg hover:bg-muted"
              >
                <Trash2 class="w-4 h-4" />
              </button>
            </div>
          </div>

          <div class="space-y-4">
            <div class="flex items-center justify-between">
              <label class="text-sm font-medium">结果预览</label>
              <div v-if="result?.svgContent || result?.pngBase64" class="flex gap-2">
                <button
                  @click="zoomIn"
                  class="p-1.5 border rounded hover:bg-muted"
                  title="放大"
                >
                  <ZoomIn class="w-4 h-4" />
                </button>
                <button
                  @click="zoomOut"
                  class="p-1.5 border rounded hover:bg-muted"
                  title="缩小"
                >
                  <ZoomOut class="w-4 h-4" />
                </button>
                <button
                  @click="resetZoom"
                  class="p-1.5 border rounded hover:bg-muted"
                  title="重置"
                >
                  <RotateCcw class="w-4 h-4" />
                </button>
              </div>
            </div>

            <div
              ref="previewRef"
              class="border rounded-lg bg-muted/30 min-h-[300px] flex items-center justify-center overflow-auto p-4"
            >
              <div v-if="result?.pngBase64" class="text-center">
                <img
                  :src="result.pngBase64"
                  alt="PNG预览"
                  class="max-w-full rounded shadow-lg"
                  :style="{ transform: `scale(${previewZoom})`, transformOrigin: 'center' }"
                />
              </div>
              <div
                v-else-if="result?.svgContent"
                class="text-center"
                v-html="result.svgContent"
                :style="{ transform: `scale(${previewZoom})`, transformOrigin: 'center' }"
              ></div>
              <div v-else-if="result?.valid !== undefined" class="text-center py-8">
                <component
                  :is="getValidationIcon()"
                  class="w-16 h-16 mx-auto mb-4"
                  :class="getValidationColor()"
                />
                <p class="text-lg font-medium" :class="getValidationColor()">
                  {{ result.validationMessage }}
                </p>
                <div v-if="result.errors?.length" class="mt-4 text-left max-w-sm mx-auto">
                  <p class="text-sm font-medium mb-2">详细信息:</p>
                  <ul class="text-sm text-muted-foreground space-y-1">
                    <li v-for="(error, idx) in result.errors" :key="idx" class="flex items-start gap-2">
                      <span class="text-red-500">•</span>
                      {{ error }}
                    </li>
                  </ul>
                </div>
              </div>
              <div v-else class="text-center text-muted-foreground py-12">
                <Image class="w-16 h-16 mx-auto mb-4 opacity-30" />
                <p>执行操作后显示结果</p>
              </div>
            </div>

            <div v-if="resultStats" class="p-3 bg-primary/5 rounded-lg border border-primary/20">
              <div class="grid grid-cols-3 gap-4 text-center text-sm">
                <div>
                  <div class="text-muted-foreground">原始大小</div>
                  <div class="font-mono font-medium">{{ resultStats.original }} 字节</div>
                </div>
                <div>
                  <div class="text-muted-foreground">优化后</div>
                  <div class="font-mono font-medium text-green-600">{{ resultStats.optimized }} 字节</div>
                </div>
                <div>
                  <div class="text-muted-foreground">节省</div>
                  <div class="font-mono font-medium text-primary">{{ resultStats.percent }}%</div>
                </div>
              </div>
            </div>

            <div v-if="result?.svgContent" class="relative">
              <label class="text-sm font-medium mb-2 block">输出代码</label>
              <textarea
                :value="result.svgContent"
                readonly
                rows="10"
                class="w-full px-4 py-3 border rounded-lg bg-muted/50 font-mono text-sm resize-none"
              />
              <div class="absolute top-8 right-2 flex gap-1">
                <button
                  @click="copyResult"
                  class="p-1.5 bg-background border rounded hover:bg-muted"
                  title="复制"
                >
                  <Copy class="w-4 h-4" />
                </button>
              </div>
            </div>

            <div v-if="result?.svgContent || result?.pngBase64" class="flex gap-2">
              <button
                @click="downloadResult"
                class="flex-1 flex items-center justify-center gap-2 px-4 py-2 border rounded-lg hover:bg-muted"
              >
                <Download class="w-4 h-4" />
                下载结果
              </button>
              <button
                v-if="result?.svgContent"
                @click="copyResult"
                class="flex-1 flex items-center justify-center gap-2 px-4 py-2 border rounded-lg hover:bg-muted"
              >
                <Copy class="w-4 h-4" />
                复制代码
              </button>
            </div>
          </div>
        </div>

        <div class="mt-6 p-4 bg-muted/30 rounded-lg">
          <h3 class="text-sm font-medium mb-3">使用说明</h3>
          <ul class="text-sm text-muted-foreground space-y-2 list-disc list-inside">
            <li><strong>格式化</strong>：美化SVG代码，添加缩进和换行，便于阅读和编辑</li>
            <li><strong>优化</strong>：移除冗余属性（如不必要的命名空间）、空元素等，保持可读性</li>
            <li><strong>压缩</strong>：移除所有空白、注释，最小化文件体积</li>
            <li><strong>转PNG</strong>：将SVG转换为PNG位图，可指定输出尺寸</li>
            <li><strong>验证</strong>：检查SVG格式是否正确，发现潜在问题</li>
          </ul>
        </div>
      </div>
    </div>

    <Transition
      enter-active-class="transition duration-300 ease-out"
      enter-from-class="transform translate-y-2 opacity-0"
      enter-to-class="transform translate-y-0 opacity-100"
      leave-active-class="transition duration-200 ease-in"
      leave-from-class="transform translate-y-0 opacity-100"
      leave-to-class="transform translate-y-2 opacity-0"
    >
      <div
        v-if="toast.show"
        class="fixed bottom-8 left-1/2 -translate-x-1/2 z-50 flex items-center gap-2 px-4 py-2.5 rounded-lg shadow-lg"
        :class="toast.type === 'success' ? 'bg-foreground text-background' : 'bg-red-500 text-white'"
      >
        <Check v-if="toast.type === 'success'" class="w-4 h-4" />
        <span class="text-sm font-medium">{{ toast.message }}</span>
      </div>
    </Transition>
  </NuxtLayout>
</template>
