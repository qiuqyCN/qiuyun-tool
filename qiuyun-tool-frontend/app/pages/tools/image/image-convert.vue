<script setup lang="ts">
import { ref, computed } from 'vue'
import { AlertCircle, CheckCircle, Copy, Download, Check, Loader2, ImageIcon, Trash2, Upload, ArrowRightLeft } from 'lucide-vue-next'
import { useToolExecutor } from '~/composables/useToolExecutor'
import { ToolType } from '~/types/tool'

// 图片格式枚举
enum ImageFormat {
  JPEG = 'jpeg',
  PNG = 'png',
  WEBP = 'webp',
  BMP = 'bmp',
  GIF = 'gif'
}

// 请求参数类型
interface ImageConvertParams {
  imageData: string
  sourceFormat: string
  targetFormat: string
  quality?: number
  keepTransparency: boolean
}

// 响应结果类型
interface ImageConvertResult {
  success: boolean
  originalData: string
  convertedData: string
  originalSize: number
  convertedSize: number
  originalWidth: number
  originalHeight: number
  sourceFormat: string
  targetFormat: string
  hasTransparency: boolean
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
const targetFormat = ref<ImageFormat>(ImageFormat.PNG)
const quality = ref(90)
const keepTransparency = ref(true)
const convertedResult = ref<ImageConvertResult | null>(null)
const isDragging = ref(false)

// 文件大小限制（10MB）
const MAX_FILE_SIZE = 10 * 1024 * 1024

// 支持的格式
const supportedFormats = [
  { value: ImageFormat.JPEG, label: 'JPEG', ext: 'jpg' },
  { value: ImageFormat.PNG, label: 'PNG', ext: 'png' },
  { value: ImageFormat.WEBP, label: 'WebP', ext: 'webp' },
  { value: ImageFormat.BMP, label: 'BMP', ext: 'bmp' },
  { value: ImageFormat.GIF, label: 'GIF', ext: 'gif' }
]

// 支持透明通道的格式
const transparencyFormats = ['png', 'webp', 'gif']

// 使用工具执行器
const { execute, isLoading } = useToolExecutor<ImageConvertParams, ImageConvertResult>({
  toolCode: 'image-convert',
  toolType: ToolType.INSTANT,
  onSuccess: (result) => {
    if (result.success) {
      convertedResult.value = result
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
  const ext = file.name.split('.').pop()?.toLowerCase() || ''
  if (ext === 'jpg') return 'jpeg'
  return ext
}

// 验证文件
const validateFile = (file: File): string | null => {
  if (file.size > MAX_FILE_SIZE) {
    return '文件大小超过 10MB 限制'
  }
  const validTypes = ['image/jpeg', 'image/jpg', 'image/png', 'image/webp', 'image/bmp', 'image/gif']
  if (!validTypes.includes(file.type)) {
    return '仅支持 JPG、PNG、WebP、BMP、GIF 格式的图片'
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
  convertedResult.value = null

  // 创建预览 URL
  previewUrl.value = URL.createObjectURL(file)

  // 自动设置目标格式（如果不是当前格式）
  const currentFormat = getFileFormat(file)
  if (currentFormat === targetFormat.value) {
    // 如果相同，切换到另一个常用格式
    targetFormat.value = currentFormat === 'png' ? ImageFormat.JPEG : ImageFormat.PNG
  }
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
  convertedResult.value = null
  targetFormat.value = ImageFormat.PNG
  quality.value = 90
  keepTransparency.value = true
}

// 转换图片
const convertImage = async () => {
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
      sourceFormat: getFileFormat(selectedFile.value),
      targetFormat: targetFormat.value,
      quality: targetFormat.value === ImageFormat.JPEG ? quality.value : undefined,
      keepTransparency: keepTransparency.value
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

// 复制转换后的图片
const copyConvertedImage = async () => {
  if (!convertedResult.value) return

  const dataUrl = `data:image/${convertedResult.value.targetFormat};base64,${convertedResult.value.convertedData}`

  try {
    // 方案1：尝试使用 ClipboardItem
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

    // 方案3：使用传统的 execCommand
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

// 下载转换后的图片
const downloadConvertedImage = () => {
  if (!convertedResult.value) return

  const link = document.createElement('a')
  link.href = `data:image/${convertedResult.value.targetFormat};base64,${convertedResult.value.convertedData}`
  link.download = `converted_${Date.now()}.${convertedResult.value.targetFormat === 'jpeg' ? 'jpg' : convertedResult.value.targetFormat}`
  link.click()
}

// 目标格式是否支持透明通道
const targetSupportsTransparency = computed(() => {
  return transparencyFormats.includes(targetFormat.value)
})

// 转换后图片的预览 URL
const convertedPreviewUrl = computed(() => {
  if (!convertedResult.value) return ''
  return `data:image/${convertedResult.value.targetFormat};base64,${convertedResult.value.convertedData}`
})
</script>

<template>
  <NuxtLayout name="tool" tool-code="image-convert">
    <!-- 工具执行区域 -->
    <div class="border border-border/40 rounded-xl overflow-hidden">
      <!-- 标题栏 -->
      <div class="border-b bg-muted/30 px-6 py-4">
        <div class="flex items-center gap-2">
          <ArrowRightLeft class="w-5 h-5 text-primary" />
          <h2 class="text-lg font-semibold">图片格式转换</h2>
        </div>
        <p class="text-sm text-muted-foreground mt-1">
          支持 JPG、PNG、WebP、BMP、GIF 格式互转，支持透明通道处理
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
                accept="image/jpeg,image/jpg,image/png,image/webp,image/bmp,image/gif"
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
                  <p class="text-xs text-muted-foreground mt-1">支持 JPG、PNG、WebP、BMP、GIF，最大 10MB</p>
                </div>
              </label>
            </div>

            <!-- 转换设置 -->
            <div v-if="selectedFile" class="space-y-4 border rounded-lg p-4">
              <h3 class="text-sm font-medium flex items-center gap-2">
                <ImageIcon class="w-4 h-4" />
                转换设置
              </h3>

              <!-- 目标格式 -->
              <div class="space-y-2">
                <label class="text-sm">目标格式</label>
                <div class="grid grid-cols-5 gap-2">
                  <button
                    v-for="format in supportedFormats"
                    :key="format.value"
                    @click="targetFormat = format.value"
                    class="px-3 py-2 text-sm rounded-md border transition-colors"
                    :class="targetFormat === format.value
                      ? 'border-primary bg-primary/10 text-primary'
                      : 'border-border hover:border-primary/50'"
                  >
                    {{ format.label }}
                  </button>
                </div>
              </div>

              <!-- JPEG 质量设置 -->
              <div v-if="targetFormat === ImageFormat.JPEG" class="space-y-2">
                <div class="flex items-center justify-between">
                  <label class="text-sm">JPEG 质量</label>
                  <span class="text-sm font-medium">{{ quality }}%</span>
                </div>
                <input
                  v-model="quality"
                  type="range"
                  min="1"
                  max="100"
                  class="w-full h-2 bg-muted rounded-lg appearance-none cursor-pointer accent-primary"
                />
              </div>

              <!-- 透明通道设置 -->
              <div v-if="targetSupportsTransparency" class="flex items-center gap-2">
                <input
                  v-model="keepTransparency"
                  type="checkbox"
                  id="keep-transparency"
                  class="w-4 h-4 rounded border-gray-300"
                />
                <label for="keep-transparency" class="text-sm cursor-pointer">
                  保留透明通道（仅对 PNG、WebP、GIF 有效）
                </label>
              </div>

              <!-- 操作按钮 -->
              <div class="flex gap-2 pt-2">
                <Button @click="convertImage" :disabled="isLoading" class="flex-1">
                  <Loader2 v-if="isLoading" class="w-4 h-4 mr-2 animate-spin" />
                  <ArrowRightLeft v-else class="w-4 h-4 mr-2" />
                  {{ isLoading ? '转换中...' : '开始转换' }}
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

            <!-- 转换结果 -->
            <div v-if="convertedResult" class="border rounded-lg p-4 space-y-4">
              <h3 class="text-sm font-medium flex items-center gap-2">
                <CheckCircle class="w-4 h-4 text-green-500" />
                转换结果
              </h3>

              <!-- 转换后预览 -->
              <div class="relative bg-muted/30 rounded-lg overflow-hidden flex items-center justify-center min-h-[200px]">
                <img
                  :src="convertedPreviewUrl"
                  alt="转换后图片"
                  class="max-w-full max-h-[300px] object-contain"
                />
              </div>

              <!-- 转换信息 -->
              <div class="grid grid-cols-2 gap-4 text-sm">
                <div class="bg-muted/30 rounded-lg p-3">
                  <p class="text-muted-foreground">原始大小</p>
                  <p class="font-medium">{{ formatFileSize(convertedResult.originalSize) }}</p>
                </div>
                <div class="bg-muted/30 rounded-lg p-3">
                  <p class="text-muted-foreground">转换后大小</p>
                  <p class="font-medium" :class="convertedResult.convertedSize > convertedResult.originalSize ? 'text-orange-500' : 'text-green-600'">
                    {{ formatFileSize(convertedResult.convertedSize) }}
                  </p>
                </div>
                <div class="bg-muted/30 rounded-lg p-3">
                  <p class="text-muted-foreground">源格式</p>
                  <p class="font-medium">{{ convertedResult.sourceFormat.toUpperCase() }}</p>
                </div>
                <div class="bg-muted/30 rounded-lg p-3">
                  <p class="text-muted-foreground">目标格式</p>
                  <p class="font-medium text-primary">{{ convertedResult.targetFormat.toUpperCase() }}</p>
                </div>
              </div>

              <!-- 透明通道信息 -->
              <div v-if="convertedResult.hasTransparency" class="text-sm text-muted-foreground">
                <p class="flex items-center gap-1">
                  <CheckCircle class="w-3 h-3 text-green-500" />
                  已保留透明通道
                </p>
              </div>

              <!-- 操作按钮 -->
              <div class="flex gap-2">
                <Button variant="outline" @click="copyConvertedImage" class="flex-1">
                  <Copy class="w-4 h-4 mr-2" />
                  复制
                </Button>
                <Button @click="downloadConvertedImage" class="flex-1">
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
