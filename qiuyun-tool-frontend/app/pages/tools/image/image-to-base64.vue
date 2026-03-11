<script setup lang="ts">
import { ref, computed } from 'vue'
import { CheckCircle, Copy, Download, Check, Upload, ImageIcon, Trash2, FileCode, Loader2 } from 'lucide-vue-next'
import { useToolExecutor } from '~/composables/useToolExecutor'
import { ToolType } from '~/types/tool'

// 请求参数类型
interface ImageToBase64Params {
  imageData: string
  includePrefix: boolean
}

// 响应结果类型
interface ImageToBase64Result {
  success: boolean
  base64Data: string
  originalSize: number
  base64Length: number
  width: number
  height: number
  format: string
  hasPrefix: boolean
  errorMessage?: string
}

// Toast 提示状态
const toast = ref({
  show: false,
  message: ''
})

// 显示提示
const showToast = (message: string) => {
  toast.value.message = message
  toast.value.show = true
  setTimeout(() => {
    toast.value.show = false
  }, 2000)
}

// 状态
const selectedFile = ref<File | null>(null)
const previewUrl = ref('')
const base64Result = ref('')
const includePrefix = ref(true)
const isDragging = ref(false)
const imageInfo = ref<{ width: number; height: number; format: string; size: number } | null>(null)

// 文件大小限制（10MB）
const MAX_FILE_SIZE = 10 * 1024 * 1024

// 使用工具执行器
const { execute, isLoading } = useToolExecutor<ImageToBase64Params, ImageToBase64Result>({
  toolCode: 'image-to-base64',
  toolType: ToolType.INSTANT,
  onSuccess: (result) => {
    if (result.success) {
      base64Result.value = result.base64Data
      imageInfo.value = {
        width: result.width,
        height: result.height,
        format: result.format,
        size: result.originalSize
      }
      showToast('转换成功！')
    } else {
      showToast(result.errorMessage || '转换失败')
    }
  },
  onError: (err) => {
    showToast(err)
  }
})

// 格式化文件大小
const formatFileSize = (bytes: number): string => {
  if (bytes === 0) return '0 B'
  const k = 1024
  const sizes = ['B', 'KB', 'MB', 'GB']
  const i = Math.floor(Math.log(bytes) / Math.log(k))
  return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i]
}

// 获取文件格式
const getFileFormat = (file: File): string => {
  return file.type.split('/')[1] || 'unknown'
}

// 验证文件
const validateFile = (file: File): string | null => {
  if (file.size > MAX_FILE_SIZE) {
    return '文件大小超过 10MB 限制'
  }
  if (!file.type.startsWith('image/')) {
    return '仅支持图片文件'
  }
  return null
}

// 处理文件选择
const handleFileSelect = (event: Event) => {
  const target = event.target as HTMLInputElement
  const file = target.files?.[0]
  if (file) {
    processFile(file)
  }
}

// 处理文件
const processFile = async (file: File) => {
  const error = validateFile(file)
  if (error) {
    showToast(error)
    return
  }

  selectedFile.value = file
  base64Result.value = ''
  imageInfo.value = null

  // 创建预览 URL
  previewUrl.value = URL.createObjectURL(file)

  // 转换为 Base64 并发送到后端
  await convertAndSend(file)
}

// 转换为 Base64 并发送到后端
const convertAndSend = async (file: File) => {
  try {
    const base64 = await fileToBase64(file)
    // 移除 data:image 前缀，只保留纯 Base64 数据
    const base64Data = base64.split(',')[1]

    await execute({
      imageData: base64Data,
      includePrefix: includePrefix.value
    })
  } catch (err) {
    showToast('图片处理失败')
  }
}

// 文件转 Base64
const fileToBase64 = (file: File): Promise<string> => {
  return new Promise((resolve, reject) => {
    const reader = new FileReader()
    reader.onload = () => resolve(reader.result as string)
    reader.onerror = reject
    reader.readAsDataURL(file)
  })
}

// 拖拽处理
const handleDragOver = (event: DragEvent) => {
  event.preventDefault()
  isDragging.value = true
}

const handleDragLeave = (event: DragEvent) => {
  event.preventDefault()
  isDragging.value = false
}

const handleDrop = (event: DragEvent) => {
  event.preventDefault()
  isDragging.value = false
  const file = event.dataTransfer?.files[0]
  if (file) {
    processFile(file)
  }
}

// 清空
const clearAll = () => {
  selectedFile.value = null
  previewUrl.value = ''
  base64Result.value = ''
  imageInfo.value = null
  includePrefix.value = true
}

// 切换前缀选项时重新转换
const togglePrefix = async () => {
  if (selectedFile.value) {
    await convertAndSend(selectedFile.value)
  }
}

// 复制 Base64
const copyBase64 = async () => {
  if (!base64Result.value) return

  try {
    await navigator.clipboard.writeText(base64Result.value)
    showToast('已复制到剪贴板！')
  } catch (err) {
    // 降级方案
    const textArea = document.createElement('textarea')
    textArea.value = base64Result.value
    textArea.style.position = 'fixed'
    textArea.style.left = '-999999px'
    textArea.style.top = '-999999px'
    document.body.appendChild(textArea)
    textArea.focus()
    textArea.select()

    try {
      const successful = document.execCommand('copy')
      if (successful) {
        showToast('已复制到剪贴板！')
      } else {
        showToast('复制失败')
      }
    } catch (e) {
      showToast('复制失败')
    }

    document.body.removeChild(textArea)
  }
}

// 下载 Base64 文本
const downloadBase64 = () => {
  if (!base64Result.value) return

  const blob = new Blob([base64Result.value], { type: 'text/plain' })
  const url = URL.createObjectURL(blob)
  const link = document.createElement('a')
  link.href = url
  link.download = `${selectedFile.value?.name.split('.')[0] || 'image'}.txt`
  link.click()
  URL.revokeObjectURL(url)
}

// 下载为 HTML img 标签
const downloadAsHtml = () => {
  if (!base64Result.value) return

  const html = `<img src="${base64Result.value}" alt="${selectedFile.value?.name || 'image'}" />`
  const blob = new Blob([html], { type: 'text/html' })
  const url = URL.createObjectURL(blob)
  const link = document.createElement('a')
  link.href = url
  link.download = `${selectedFile.value?.name.split('.')[0] || 'image'}.html`
  link.click()
  URL.revokeObjectURL(url)
}

// Base64 结果长度
const base64Length = computed(() => {
  return base64Result.value.length
})

// Base64 结果大小（估算）
const base64Size = computed(() => {
  // Base64 编码后大小约为原文件的 4/3
  return Math.ceil(base64Result.value.length * 0.75)
})
</script>

<template>
  <NuxtLayout name="tool" tool-code="image-to-base64">
    <!-- 工具执行区域 -->
    <div class="border border-border/40 rounded-xl overflow-hidden">
      <!-- 标题栏 -->
      <div class="border-b bg-muted/30 px-6 py-4">
        <div class="flex items-center gap-2">
          <FileCode class="w-5 h-5 text-primary" />
          <h2 class="text-lg font-semibold">图片转 Base64</h2>
        </div>
        <p class="text-sm text-muted-foreground mt-1">
          将图片转换为 Base64 编码字符串，支持复制和下载
        </p>
      </div>

      <!-- 主内容区 -->
      <div class="p-6">
        <div class="grid grid-cols-1 lg:grid-cols-2 gap-6">
          <!-- 左侧：上传和预览 -->
          <div class="space-y-6">
            <!-- 上传区域 -->
            <div
              class="border-2 border-dashed rounded-xl p-8 text-center transition-colors"
              :class="isDragging ? 'border-primary bg-primary/5' : 'border-border hover:border-primary/50'"
              @dragover="handleDragOver"
              @dragleave="handleDragLeave"
              @drop="handleDrop"
            >
              <input
                type="file"
                accept="image/*"
                class="hidden"
                id="image-input"
                @change="handleFileSelect"
              />
              <label
                for="image-input"
                class="cursor-pointer flex flex-col items-center gap-3"
              >
                <div class="w-16 h-16 rounded-full bg-primary/10 flex items-center justify-center">
                  <Upload class="w-8 h-8 text-primary" />
                </div>
                <div>
                  <p class="text-sm font-medium">点击或拖拽上传图片</p>
                  <p class="text-xs text-muted-foreground mt-1">支持任意图片格式，最大 10MB</p>
                </div>
              </label>
            </div>

            <!-- 图片预览 -->
            <div v-if="previewUrl" class="border rounded-lg p-4">
              <div class="flex items-center justify-between mb-3">
                <h3 class="text-sm font-medium">图片预览</h3>
                <button
                  @click="clearAll"
                  class="text-xs text-muted-foreground hover:text-foreground px-2 py-1 rounded hover:bg-muted transition-colors flex items-center gap-1"
                >
                  <Trash2 class="w-3 h-3" />
                  清空
                </button>
              </div>
              <div class="relative bg-muted/30 rounded-lg overflow-hidden flex items-center justify-center min-h-[200px]">
                <img
                  :src="previewUrl"
                  alt="图片预览"
                  class="max-w-full max-h-[300px] object-contain"
                />
              </div>
            </div>

            <!-- 图片信息 -->
            <div v-if="imageInfo" class="border rounded-lg p-4 space-y-2">
              <h3 class="text-sm font-medium">图片信息</h3>
              <div class="text-sm text-muted-foreground space-y-1">
                <p>文件名：{{ selectedFile?.name }}</p>
                <p>大小：{{ formatFileSize(imageInfo.size) }}</p>
                <p>格式：{{ imageInfo.format.toUpperCase() }}</p>
                <p>尺寸：{{ imageInfo.width }} × {{ imageInfo.height }}</p>
              </div>
            </div>

            <!-- 空状态 -->
            <div v-if="!previewUrl" class="border rounded-lg p-8 text-center text-muted-foreground">
              <ImageIcon class="w-12 h-12 mx-auto mb-3 opacity-50" />
              <p>上传图片后将显示预览</p>
            </div>
          </div>

          <!-- 右侧：Base64 结果 -->
          <div class="space-y-6">
            <!-- 设置选项 -->
            <div v-if="selectedFile" class="border rounded-lg p-4 space-y-4">
              <h3 class="text-sm font-medium flex items-center gap-2">
                <FileCode class="w-4 h-4" />
                转换设置
              </h3>

              <!-- 包含前缀选项 -->
              <div class="flex items-center gap-2">
                <input
                  v-model="includePrefix"
                  type="checkbox"
                  id="include-prefix"
                  class="w-4 h-4 rounded border-gray-300"
                  @change="togglePrefix"
                />
                <label for="include-prefix" class="text-sm cursor-pointer">
                  包含 data:image 前缀（可直接用于 img 标签 src 属性）
                </label>
              </div>
            </div>

            <!-- 加载状态 -->
            <div v-if="isLoading" class="border rounded-lg p-8 text-center">
              <Loader2 class="w-8 h-8 mx-auto mb-3 animate-spin text-primary" />
              <p class="text-sm text-muted-foreground">正在转换...</p>
            </div>

            <!-- Base64 结果 -->
            <div v-else-if="base64Result" class="border rounded-lg p-4 space-y-4">
              <div class="flex items-center justify-between">
                <h3 class="text-sm font-medium flex items-center gap-2">
                  <CheckCircle class="w-4 h-4 text-green-500" />
                  Base64 编码结果
                </h3>
                <div class="text-xs text-muted-foreground">
                  长度：{{ base64Length.toLocaleString() }} 字符
                </div>
              </div>

              <!-- 结果文本框 -->
              <textarea
                v-model="base64Result"
                readonly
                class="w-full h-[300px] font-mono text-xs resize-none bg-muted/20 border-border/60 rounded-md p-3 break-all"
              />

              <!-- 统计信息 -->
              <div class="grid grid-cols-2 gap-4 text-sm">
                <div class="bg-muted/30 rounded-lg p-3">
                  <p class="text-muted-foreground">原始大小</p>
                  <p class="font-medium">{{ formatFileSize(imageInfo?.size || 0) }}</p>
                </div>
                <div class="bg-muted/30 rounded-lg p-3">
                  <p class="text-muted-foreground">Base64 大小（估算）</p>
                  <p class="font-medium text-orange-500">
                    {{ formatFileSize(base64Size) }}
                  </p>
                </div>
              </div>

              <!-- 操作按钮 -->
              <div class="flex flex-wrap gap-2">
                <Button @click="copyBase64" class="flex-1">
                  <Copy class="w-4 h-4 mr-2" />
                  复制
                </Button>
                <Button variant="outline" @click="downloadBase64" class="flex-1">
                  <Download class="w-4 h-4 mr-2" />
                  下载 TXT
                </Button>
                <Button variant="outline" @click="downloadAsHtml" class="flex-1">
                  <FileCode class="w-4 h-4 mr-2" />
                  下载 HTML
                </Button>
              </div>
            </div>

            <!-- 空状态 -->
            <div v-else class="border rounded-lg p-8 text-center text-muted-foreground">
              <FileCode class="w-12 h-12 mx-auto mb-3 opacity-50" />
              <p>上传图片后将显示 Base64 编码结果</p>
            </div>

            <!-- 使用说明 -->
            <div class="border rounded-lg p-4 space-y-2">
              <h3 class="text-sm font-medium">使用说明</h3>
              <ul class="text-sm text-muted-foreground space-y-1 list-disc list-inside">
                <li>Base64 编码后的图片体积会比原图大约 33%</li>
                <li>包含 data:image 前缀的结果可直接用于 HTML img 标签</li>
                <li>不包含前缀的结果可用于需要纯 Base64 数据的场景</li>
                <li>建议仅对小图（&lt; 100KB）使用 Base64 编码</li>
              </ul>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- Toast 提示 -->
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
        class="fixed bottom-8 left-1/2 -translate-x-1/2 z-50 flex items-center gap-2 px-4 py-2.5 bg-foreground text-background rounded-lg shadow-lg"
      >
        <Check class="w-4 h-4 text-green-400" />
        <span class="text-sm font-medium">{{ toast.message }}</span>
      </div>
    </Transition>
  </NuxtLayout>
</template>
