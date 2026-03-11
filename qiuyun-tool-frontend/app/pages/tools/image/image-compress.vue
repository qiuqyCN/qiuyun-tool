<script setup lang="ts">
import { ref, computed } from 'vue'
import { AlertCircle, CheckCircle, Copy, Download, Check, Loader2, ImageIcon, Trash2, Upload, FileImage } from 'lucide-vue-next'
import { useToolExecutor } from '~/composables/useToolExecutor'
import { ToolType } from '~/types/tool'

// 图片格式枚举
enum ImageFormat {
  JPEG = 'jpeg',
  PNG = 'png',
  WEBP = 'webp'
}

// 请求参数类型
interface ImageCompressParams {
  imageData: string
  format: string
  quality: number
  maxWidth?: number
  maxHeight?: number
  outputFormat: string
}

// 响应结果类型
interface ImageCompressResult {
  success: boolean
  originalData: string
  compressedData: string
  originalSize: number
  compressedSize: number
  originalWidth: number
  originalHeight: number
  compressedWidth: number
  compressedHeight: number
  format: string
  quality: number
  compressionRatio: number
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
const quality = ref(80)
const maxWidth = ref<number | null>(null)
const maxHeight = ref<number | null>(null)
const outputFormat = ref('original')
const compressedResult = ref<ImageCompressResult | null>(null)
const isDragging = ref(false)

// 文件大小限制（10MB）
const MAX_FILE_SIZE = 10 * 1024 * 1024

// 使用工具执行器
const { execute, isLoading } = useToolExecutor<ImageCompressParams, ImageCompressResult>({
  toolCode: 'image-compress',
  toolType: ToolType.INSTANT,
  onSuccess: (result) => {
    if (result.success) {
      compressedResult.value = result
      showToast('压缩成功！')
    } else {
      showToast(result.errorMessage || '压缩失败')
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
  const ext = file.name.split('.').pop()?.toLowerCase() || ''
  if (ext === 'jpg') return 'jpeg'
  return ext
}

// 验证文件
const validateFile = (file: File): string | null => {
  if (file.size > MAX_FILE_SIZE) {
    return '文件大小超过 10MB 限制'
  }
  const validTypes = ['image/jpeg', 'image/jpg', 'image/png', 'image/webp']
  if (!validTypes.includes(file.type)) {
    return '仅支持 JPG、PNG、WebP 格式的图片'
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
const processFile = (file: File) => {
  const error = validateFile(file)
  if (error) {
    showToast(error)
    return
  }

  selectedFile.value = file
  compressedResult.value = null

  // 创建预览 URL
  previewUrl.value = URL.createObjectURL(file)
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
  compressedResult.value = null
  quality.value = 80
  maxWidth.value = null
  maxHeight.value = null
  outputFormat.value = 'original'
}

// 压缩图片
const compressImage = async () => {
  if (!selectedFile.value) {
    showToast('请先选择图片')
    return
  }

  try {
    // 读取文件为 Base64
    const base64 = await fileToBase64(selectedFile.value)
    // 移除 data:image 前缀
    const base64Data = base64.split(',')[1]

    await execute({
      imageData: base64Data,
      format: getFileFormat(selectedFile.value),
      quality: quality.value,
      maxWidth: maxWidth.value || undefined,
      maxHeight: maxHeight.value || undefined,
      outputFormat: outputFormat.value
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

// 复制压缩后的图片
const copyCompressedImage = async () => {
  if (!compressedResult.value) return

  const dataUrl = `data:image/${compressedResult.value.format};base64,${compressedResult.value.compressedData}`

  try {
    // 方案1：尝试使用 ClipboardItem 复制图片（仅支持 HTTPS 或 localhost）
    if (typeof ClipboardItem !== 'undefined' && navigator.clipboard && navigator.clipboard.write) {
      try {
        const response = await fetch(dataUrl)
        const blob = await response.blob()
        await navigator.clipboard.write([
          new ClipboardItem({ [blob.type]: blob })
        ])
        showToast('已复制到剪贴板！')
        return
      } catch (clipboardErr) {
        console.log('ClipboardItem 失败，尝试降级方案:', clipboardErr)
      }
    }

    // 方案2：尝试复制 Base64 文本
    if (navigator.clipboard && navigator.clipboard.writeText) {
      await navigator.clipboard.writeText(dataUrl)
      showToast('已复制图片数据（Base64格式）')
      return
    }

    // 方案3：使用传统的 execCommand 复制
    const textArea = document.createElement('textarea')
    textArea.value = dataUrl
    textArea.style.position = 'fixed'
    textArea.style.left = '-999999px'
    textArea.style.top = '-999999px'
    document.body.appendChild(textArea)
    textArea.focus()
    textArea.select()

    try {
      const successful = document.execCommand('copy')
      if (successful) {
        showToast('已复制图片数据（Base64格式）')
      } else {
        showToast('复制失败，请尝试下载')
      }
    } catch (err) {
      showToast('复制失败，请尝试下载')
    }

    document.body.removeChild(textArea)
  } catch (err) {
    console.error('复制失败:', err)
    showToast('复制失败，请尝试下载')
  }
}

// 下载压缩后的图片
const downloadCompressedImage = () => {
  if (!compressedResult.value) return

  const link = document.createElement('a')
  link.href = `data:image/${compressedResult.value.format};base64,${compressedResult.value.compressedData}`
  link.download = `compressed_${Date.now()}.${compressedResult.value.format}`
  link.click()
}

// 压缩后图片的预览 URL
const compressedPreviewUrl = computed(() => {
  if (!compressedResult.value) return ''
  return `data:image/${compressedResult.value.format};base64,${compressedResult.value.compressedData}`
})
</script>

<template>
  <NuxtLayout name="tool" tool-code="image-compress">
    <!-- 工具执行区域 -->
    <div class="border border-border/40 rounded-xl overflow-hidden">
      <!-- 标题栏 -->
      <div class="border-b bg-muted/30 px-6 py-4">
        <div class="flex items-center gap-2">
          <ImageIcon class="w-5 h-5 text-primary" />
          <h2 class="text-lg font-semibold">图片压缩</h2>
        </div>
        <p class="text-sm text-muted-foreground mt-1">
          支持 JPG、PNG、WebP 格式，最大支持 10MB
        </p>
      </div>

      <!-- 主内容区 -->
      <div class="p-6">
        <div class="grid grid-cols-1 lg:grid-cols-2 gap-6">
          <!-- 左侧：上传和设置 -->
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
                accept="image/jpeg,image/jpg,image/png,image/webp"
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
                  <p class="text-xs text-muted-foreground mt-1">支持 JPG、PNG、WebP，最大 10MB</p>
                </div>
              </label>
            </div>

            <!-- 压缩设置 -->
            <div v-if="selectedFile" class="space-y-4 border rounded-lg p-4">
              <h3 class="text-sm font-medium flex items-center gap-2">
                <FileImage class="w-4 h-4" />
                压缩设置
              </h3>

              <!-- 质量设置 -->
              <div class="space-y-2">
                <div class="flex items-center justify-between">
                  <label class="text-sm">压缩质量</label>
                  <span class="text-sm font-medium">{{ quality }}%</span>
                </div>
                <input
                  v-model="quality"
                  type="range"
                  min="1"
                  max="100"
                  class="w-full h-2 bg-muted rounded-lg appearance-none cursor-pointer accent-primary"
                />
                <div class="flex justify-between text-xs text-muted-foreground">
                  <span>低质量（小体积）</span>
                  <span>高质量（大体积）</span>
                </div>
              </div>

              <!-- 尺寸限制 -->
              <div class="grid grid-cols-2 gap-4">
                <div class="space-y-2">
                  <label class="text-sm">最大宽度（可选）</label>
                  <input
                    v-model="maxWidth"
                    type="number"
                    placeholder="不限制"
                    class="w-full px-3 py-2 text-sm border rounded-md bg-background"
                  />
                </div>
                <div class="space-y-2">
                  <label class="text-sm">最大高度（可选）</label>
                  <input
                    v-model="maxHeight"
                    type="number"
                    placeholder="不限制"
                    class="w-full px-3 py-2 text-sm border rounded-md bg-background"
                  />
                </div>
              </div>

              <!-- 输出格式 -->
              <div class="space-y-2">
                <label class="text-sm">输出格式</label>
                <select
                  v-model="outputFormat"
                  class="w-full px-3 py-2 text-sm border rounded-md bg-background"
                >
                  <option value="original">保持原格式</option>
                  <option value="jpeg">JPEG</option>
                  <option value="png">PNG</option>
                  <option value="webp">WebP</option>
                </select>
              </div>

              <!-- 操作按钮 -->
              <div class="flex gap-2 pt-2">
                <Button @click="compressImage" :disabled="isLoading" class="flex-1">
                  <Loader2 v-if="isLoading" class="w-4 h-4 mr-2 animate-spin" />
                  <CheckCircle v-else class="w-4 h-4 mr-2" />
                  {{ isLoading ? '压缩中...' : '开始压缩' }}
                </Button>
                <Button variant="outline" @click="clearAll">
                  <Trash2 class="w-4 h-4" />
                </Button>
              </div>
            </div>

            <!-- 原始图片信息 -->
            <div v-if="selectedFile" class="border rounded-lg p-4 space-y-2">
              <h3 class="text-sm font-medium">原始图片信息</h3>
              <div class="text-sm text-muted-foreground space-y-1">
                <p>文件名：{{ selectedFile.name }}</p>
                <p>大小：{{ formatFileSize(selectedFile.size) }}</p>
                <p>格式：{{ getFileFormat(selectedFile).toUpperCase() }}</p>
              </div>
            </div>
          </div>

          <!-- 右侧：预览和结果 -->
          <div class="space-y-6">
            <!-- 原始图片预览 -->
            <div v-if="previewUrl" class="border rounded-lg p-4">
              <h3 class="text-sm font-medium mb-3">原始图片预览</h3>
              <div class="relative bg-muted/30 rounded-lg overflow-hidden flex items-center justify-center min-h-[200px]">
                <img
                  :src="previewUrl"
                  alt="原始图片"
                  class="max-w-full max-h-[300px] object-contain"
                />
              </div>
            </div>

            <!-- 压缩结果 -->
            <div v-if="compressedResult" class="border rounded-lg p-4 space-y-4">
              <h3 class="text-sm font-medium flex items-center gap-2">
                <CheckCircle class="w-4 h-4 text-green-500" />
                压缩结果
              </h3>

              <!-- 压缩后预览 -->
              <div class="relative bg-muted/30 rounded-lg overflow-hidden flex items-center justify-center min-h-[200px]">
                <img
                  :src="compressedPreviewUrl"
                  alt="压缩后图片"
                  class="max-w-full max-h-[300px] object-contain"
                />
              </div>

              <!-- 压缩信息 -->
              <div class="grid grid-cols-2 gap-4 text-sm">
                <div class="bg-muted/30 rounded-lg p-3">
                  <p class="text-muted-foreground">原始大小</p>
                  <p class="font-medium">{{ formatFileSize(compressedResult.originalSize) }}</p>
                </div>
                <div class="bg-muted/30 rounded-lg p-3">
                  <p class="text-muted-foreground">压缩后大小</p>
                  <p class="font-medium text-green-600">
                    {{ formatFileSize(compressedResult.compressedSize) }}
                  </p>
                </div>
                <div class="bg-muted/30 rounded-lg p-3">
                  <p class="text-muted-foreground">压缩比例</p>
                  <p class="font-medium text-green-600">
                    {{ compressedResult.compressionRatio }}%
                  </p>
                </div>
                <div class="bg-muted/30 rounded-lg p-3">
                  <p class="text-muted-foreground">输出格式</p>
                  <p class="font-medium">{{ compressedResult.format.toUpperCase() }}</p>
                </div>
              </div>

              <!-- 尺寸信息 -->
              <div class="text-sm text-muted-foreground">
                <p>
                  尺寸：{{ compressedResult.originalWidth }} × {{ compressedResult.originalHeight }}
                  <span v-if="compressedResult.compressedWidth !== compressedResult.originalWidth || compressedResult.compressedHeight !== compressedResult.originalHeight">
                    → {{ compressedResult.compressedWidth }} × {{ compressedResult.compressedHeight }}
                  </span>
                </p>
              </div>

              <!-- 操作按钮 -->
              <div class="flex gap-2">
                <Button variant="outline" @click="copyCompressedImage" class="flex-1">
                  <Copy class="w-4 h-4 mr-2" />
                  复制
                </Button>
                <Button @click="downloadCompressedImage" class="flex-1">
                  <Download class="w-4 h-4 mr-2" />
                  下载
                </Button>
              </div>
            </div>

            <!-- 空状态 -->
            <div v-if="!previewUrl" class="border rounded-lg p-8 text-center text-muted-foreground">
              <ImageIcon class="w-12 h-12 mx-auto mb-3 opacity-50" />
              <p>上传图片后将显示预览</p>
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
