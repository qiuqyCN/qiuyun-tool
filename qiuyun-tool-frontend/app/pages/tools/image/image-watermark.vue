<script setup lang="ts">
import { ref, computed } from 'vue'
import { AlertCircle, CheckCircle, Copy, Download, Check, Loader2, ImageIcon, Trash2, Upload, Type, Image as ImageIcon2, Stamp } from 'lucide-vue-next'
import { useToolExecutor } from '~/composables/useToolExecutor'
import { ToolType } from '~/types/tool'

// 水印类型枚举
enum WatermarkType {
  TEXT = 'text',
  IMAGE = 'image'
}

// 水印位置枚举
enum WatermarkPosition {
  TOP_LEFT = 'top-left',
  TOP_CENTER = 'top-center',
  TOP_RIGHT = 'top-right',
  CENTER_LEFT = 'center-left',
  CENTER = 'center',
  CENTER_RIGHT = 'center-right',
  BOTTOM_LEFT = 'bottom-left',
  BOTTOM_CENTER = 'bottom-center',
  BOTTOM_RIGHT = 'bottom-right',
  TILE = 'tile'
}

// 请求参数类型
interface ImageWatermarkParams {
  imageData: string
  watermarkType: string
  text?: string
  fontFamily?: string
  fontSize?: number
  fontColor?: string
  fontWeight?: string
  watermarkImageData?: string
  watermarkScale?: number
  position: string
  opacity: number
  rotation: number
  marginX: number
  marginY: number
  outputFormat: string
  quality: number
}

// 响应结果类型
interface ImageWatermarkResult {
  success: boolean
  originalData: string
  watermarkedData: string
  originalSize: number
  watermarkedSize: number
  originalWidth: number
  originalHeight: number
  format: string
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
const watermarkType = ref<WatermarkType>(WatermarkType.TEXT)
const watermarkFile = ref<File | null>(null)
const watermarkPreviewUrl = ref('')

// 文字水印设置
const text = ref('水印文字')
const fontFamily = ref('Microsoft YaHei')
const fontSize = ref(48)
const fontColor = ref('#000000')
const fontWeight = ref('normal')

// 图片水印设置
const watermarkScale = ref(30)

// 通用设置
const position = ref<WatermarkPosition>(WatermarkPosition.BOTTOM_RIGHT)
const opacity = ref(50)
const rotation = ref(0)
const marginX = ref(20)
const marginY = ref(20)
const outputFormat = ref('original')
const quality = ref(90)

const isDragging = ref(false)
const isDraggingWatermark = ref(false)
const watermarkedResult = ref<ImageWatermarkResult | null>(null)

// 文件大小限制（10MB）
const MAX_FILE_SIZE = 10 * 1024 * 1024

// 系统字体列表
const fontFamilies = [
  { value: 'Microsoft YaHei', label: '微软雅黑' },
  { value: 'SimHei', label: '黑体' },
  { value: 'SimSun', label: '宋体' },
  { value: 'KaiTi', label: '楷体' },
  { value: 'FangSong', label: '仿宋' },
  { value: 'Arial', label: 'Arial' },
  { value: 'Helvetica', label: 'Helvetica' },
  { value: 'Times New Roman', label: 'Times New Roman' },
  { value: 'Georgia', label: 'Georgia' },
  { value: 'Verdana', label: 'Verdana' }
]

// 位置选项
const positionOptions = [
  { value: WatermarkPosition.TOP_LEFT, label: '左上', row: 0, col: 0 },
  { value: WatermarkPosition.TOP_CENTER, label: '中上', row: 0, col: 1 },
  { value: WatermarkPosition.TOP_RIGHT, label: '右上', row: 0, col: 2 },
  { value: WatermarkPosition.CENTER_LEFT, label: '左中', row: 1, col: 0 },
  { value: WatermarkPosition.CENTER, label: '居中', row: 1, col: 1 },
  { value: WatermarkPosition.CENTER_RIGHT, label: '右中', row: 1, col: 2 },
  { value: WatermarkPosition.BOTTOM_LEFT, label: '左下', row: 2, col: 0 },
  { value: WatermarkPosition.BOTTOM_CENTER, label: '中下', row: 2, col: 1 },
  { value: WatermarkPosition.BOTTOM_RIGHT, label: '右下', row: 2, col: 2 }
]

// 使用工具执行器
const { execute, isLoading } = useToolExecutor<ImageWatermarkParams, ImageWatermarkResult>({
  toolCode: 'image-watermark',
  toolType: ToolType.INSTANT,
  onSuccess: (result) => {
    if (result.success) {
      watermarkedResult.value = result
      showToast('水印添加成功！')
    } else {
      showToast(result.errorMessage || '添加水印失败')
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

// 处理原图文件选择
const handleFileSelect = (event: Event) => {
  const target = event.target as HTMLInputElement
  const file = target.files?.[0]
  if (file) {
    processFile(file)
  }
}

// 处理原图文件
const processFile = (file: File) => {
  const error = validateFile(file)
  if (error) {
    showToast(error)
    return
  }

  selectedFile.value = file
  watermarkedResult.value = null

  // 创建预览 URL
  previewUrl.value = URL.createObjectURL(file)
}

// 原图拖拽处理
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

// 处理水印图片选择
const handleWatermarkFileSelect = (event: Event) => {
  const target = event.target as HTMLInputElement
  const file = target.files?.[0]
  if (file) {
    processWatermarkFile(file)
  }
}

// 处理水印图片
const processWatermarkFile = (file: File) => {
  const error = validateFile(file)
  if (error) {
    showToast(error)
    return
  }

  watermarkFile.value = file
  watermarkPreviewUrl.value = URL.createObjectURL(file)
}

// 水印图片拖拽处理
const handleWatermarkDragOver = (event: DragEvent) => {
  event.preventDefault()
  isDraggingWatermark.value = true
}

const handleWatermarkDragLeave = (event: DragEvent) => {
  event.preventDefault()
  isDraggingWatermark.value = false
}

const handleWatermarkDrop = (event: DragEvent) => {
  event.preventDefault()
  isDraggingWatermark.value = false
  const file = event.dataTransfer?.files[0]
  if (file) {
    processWatermarkFile(file)
  }
}

// 清空
const clearAll = () => {
  selectedFile.value = null
  previewUrl.value = ''
  watermarkedResult.value = null
  watermarkFile.value = null
  watermarkPreviewUrl.value = ''
  text.value = '水印文字'
  fontSize.value = 48
  opacity.value = 50
  rotation.value = 0
  marginX.value = 20
  marginY.value = 20
}

// 清空水印图片
const clearWatermarkImage = () => {
  watermarkFile.value = null
  watermarkPreviewUrl.value = ''
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

// 添加水印
const addWatermark = async () => {
  if (!selectedFile.value) {
    showToast('请先选择图片')
    return
  }

  if (watermarkType.value === WatermarkType.TEXT && !text.value.trim()) {
    showToast('请输入水印文字')
    return
  }

  if (watermarkType.value === WatermarkType.IMAGE && !watermarkFile.value) {
    showToast('请选择水印图片')
    return
  }

  try {
    // 读取原图为 Base64
    const base64 = await fileToBase64(selectedFile.value)
    const base64Data = base64.split(',')[1]

    const params: ImageWatermarkParams = {
      imageData: base64Data,
      watermarkType: watermarkType.value,
      position: position.value,
      opacity: opacity.value,
      rotation: rotation.value,
      marginX: marginX.value,
      marginY: marginY.value,
      outputFormat: outputFormat.value,
      quality: quality.value
    }

    // 文字水印参数
    if (watermarkType.value === WatermarkType.TEXT) {
      params.text = text.value
      params.fontFamily = fontFamily.value
      params.fontSize = fontSize.value
      params.fontColor = fontColor.value
      params.fontWeight = fontWeight.value
    }

    // 图片水印参数
    if (watermarkType.value === WatermarkType.IMAGE && watermarkFile.value) {
      const wmBase64 = await fileToBase64(watermarkFile.value)
      params.watermarkImageData = wmBase64.split(',')[1]
      params.watermarkScale = watermarkScale.value
    }

    await execute(params)
  } catch (err) {
    showToast('图片处理失败')
  }
}

// 复制带水印的图片
const copyWatermarkedImage = async () => {
  if (!watermarkedResult.value) return

  const dataUrl = `data:image/${watermarkedResult.value.format};base64,${watermarkedResult.value.watermarkedData}`

  try {
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

    if (navigator.clipboard && navigator.clipboard.writeText) {
      await navigator.clipboard.writeText(dataUrl)
      showToast('已复制图片数据（Base64格式）')
      return
    }

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

// 下载带水印的图片
const downloadWatermarkedImage = () => {
  if (!watermarkedResult.value) return

  const link = document.createElement('a')
  link.href = `data:image/${watermarkedResult.value.format};base64,${watermarkedResult.value.watermarkedData}`
  const ext = watermarkedResult.value.format === 'jpeg' ? 'jpg' : watermarkedResult.value.format
  link.download = `watermarked_${Date.now()}.${ext}`
  link.click()
}

// 水印后图片的预览 URL
const watermarkedPreviewUrl = computed(() => {
  if (!watermarkedResult.value) return ''
  return `data:image/${watermarkedResult.value.format};base64,${watermarkedResult.value.watermarkedData}`
})
</script>

<template>
  <NuxtLayout name="tool" tool-code="image-watermark">
    <!-- 工具执行区域 -->
    <div class="border border-border/40 rounded-xl overflow-hidden">
      <!-- 标题栏 -->
      <div class="border-b bg-muted/30 px-6 py-4">
        <div class="flex items-center gap-2">
          <Stamp class="w-5 h-5 text-primary" />
          <h2 class="text-lg font-semibold">图片水印</h2>
        </div>
        <p class="text-sm text-muted-foreground mt-1">
          为图片添加文字或图片水印，支持自定义位置、透明度、旋转角度
        </p>
      </div>

      <!-- 主内容区 -->
      <div class="p-6">
        <div class="grid grid-cols-1 lg:grid-cols-2 gap-6">
          <!-- 左侧：上传和设置 -->
          <div class="space-y-6">
            <!-- 原图上传区域 -->
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

            <!-- 水印设置 -->
            <div v-if="selectedFile" class="space-y-4 border rounded-lg p-4">
              <h3 class="text-sm font-medium flex items-center gap-2">
                <Stamp class="w-4 h-4" />
                水印设置
              </h3>

              <!-- 水印类型切换 -->
              <div class="flex gap-2 p-1 bg-muted rounded-lg">
                <button
                  @click="watermarkType = WatermarkType.TEXT"
                  class="flex-1 flex items-center justify-center gap-2 px-3 py-2 text-sm rounded-md transition-colors"
                  :class="watermarkType === WatermarkType.TEXT
                    ? 'bg-background text-foreground shadow-sm'
                    : 'text-muted-foreground hover:text-foreground'"
                >
                  <Type class="w-4 h-4" />
                  文字水印
                </button>
                <button
                  @click="watermarkType = WatermarkType.IMAGE"
                  class="flex-1 flex items-center justify-center gap-2 px-3 py-2 text-sm rounded-md transition-colors"
                  :class="watermarkType === WatermarkType.IMAGE
                    ? 'bg-background text-foreground shadow-sm'
                    : 'text-muted-foreground hover:text-foreground'"
                >
                  <ImageIcon2 class="w-4 h-4" />
                  图片水印
                </button>
              </div>

              <!-- 文字水印设置 -->
              <div v-if="watermarkType === WatermarkType.TEXT" class="space-y-4">
                <!-- 文字内容 -->
                <div class="space-y-2">
                  <label class="text-sm">水印文字</label>
                  <input
                    v-model="text"
                    type="text"
                    placeholder="请输入水印文字"
                    class="w-full px-3 py-2 text-sm border rounded-md bg-background"
                  />
                </div>

                <!-- 字体设置 -->
                <div class="space-y-2">
                  <label class="text-sm">字体</label>
                  <select
                    v-model="fontFamily"
                    class="w-full px-3 py-2 text-sm border rounded-md bg-background"
                  >
                    <option v-for="font in fontFamilies" :key="font.value" :value="font.value">
                      {{ font.label }}
                    </option>
                  </select>
                </div>

                <!-- 字号 -->
                <div class="space-y-2">
                  <div class="flex items-center justify-between">
                    <label class="text-sm">字号</label>
                    <span class="text-sm font-medium">{{ fontSize }}px</span>
                  </div>
                  <input
                    v-model="fontSize"
                    type="range"
                    min="12"
                    max="200"
                    class="w-full h-2 bg-muted rounded-lg appearance-none cursor-pointer accent-primary"
                  />
                </div>

                <!-- 颜色 -->
                <div class="space-y-2">
                  <label class="text-sm">颜色</label>
                  <div class="flex items-center gap-2">
                    <input
                      v-model="fontColor"
                      type="color"
                      class="w-10 h-10 rounded border cursor-pointer"
                    />
                    <input
                      v-model="fontColor"
                      type="text"
                      class="flex-1 px-3 py-2 text-sm border rounded-md bg-background"
                    />
                  </div>
                </div>

                <!-- 粗细 -->
                <div class="space-y-2">
                  <label class="text-sm">粗细</label>
                  <div class="flex gap-2">
                    <button
                      @click="fontWeight = 'normal'"
                      class="flex-1 px-3 py-2 text-sm rounded-md border transition-colors"
                      :class="fontWeight === 'normal'
                        ? 'border-primary bg-primary/10 text-primary'
                        : 'border-border hover:border-primary/50'"
                    >
                      正常
                    </button>
                    <button
                      @click="fontWeight = 'bold'"
                      class="flex-1 px-3 py-2 text-sm rounded-md border transition-colors"
                      :class="fontWeight === 'bold'
                        ? 'border-primary bg-primary/10 text-primary'
                        : 'border-border hover:border-primary/50'"
                    >
                      粗体
                    </button>
                  </div>
                </div>
              </div>

              <!-- 图片水印设置 -->
              <div v-else class="space-y-4">
                <!-- 水印图片上传 -->
                <div
                  v-if="!watermarkFile"
                  class="border-2 border-dashed rounded-xl p-6 text-center transition-colors"
                  :class="isDraggingWatermark ? 'border-primary bg-primary/5' : 'border-border hover:border-primary/50'"
                  @dragover="handleWatermarkDragOver"
                  @dragleave="handleWatermarkDragLeave"
                  @drop="handleWatermarkDrop"
                >
                  <input
                    type="file"
                    accept="image/*"
                    class="hidden"
                    id="watermark-image-input"
                    @change="handleWatermarkFileSelect"
                  />
                  <label
                    for="watermark-image-input"
                    class="cursor-pointer flex flex-col items-center gap-2"
                  >
                    <ImageIcon2 class="w-6 h-6 text-primary" />
                    <p class="text-xs text-muted-foreground">点击或拖拽上传水印图片</p>
                  </label>
                </div>

                <!-- 水印图片预览 -->
                <div v-else class="relative border rounded-lg p-3">
                  <button
                    @click="clearWatermarkImage"
                    class="absolute top-1 right-1 p-1 rounded-full bg-muted hover:bg-muted/80"
                  >
                    <Trash2 class="w-3 h-3" />
                  </button>
                  <img
                    :src="watermarkPreviewUrl"
                    alt="水印图片"
                    class="max-h-24 mx-auto object-contain"
                  />
                </div>

                <!-- 缩放比例 -->
                <div class="space-y-2">
                  <div class="flex items-center justify-between">
                    <label class="text-sm">缩放比例</label>
                    <span class="text-sm font-medium">{{ watermarkScale }}%</span>
                  </div>
                  <input
                    v-model="watermarkScale"
                    type="range"
                    min="10"
                    max="100"
                    class="w-full h-2 bg-muted rounded-lg appearance-none cursor-pointer accent-primary"
                  />
                </div>
              </div>

              <!-- 位置设置（九宫格） -->
              <div class="space-y-2">
                <label class="text-sm">位置</label>
                <div class="grid grid-cols-3 gap-1">
                  <button
                    v-for="pos in positionOptions"
                    :key="pos.value"
                    @click="position = pos.value"
                    class="aspect-square text-xs rounded-md border transition-colors flex items-center justify-center"
                    :class="position === pos.value
                      ? 'border-primary bg-primary/10 text-primary'
                      : 'border-border hover:border-primary/50'"
                  >
                    {{ pos.label }}
                  </button>
                </div>
                <!-- 平铺模式 -->
                <button
                  @click="position = WatermarkPosition.TILE"
                  class="w-full mt-2 px-3 py-2 text-sm rounded-md border transition-colors"
                  :class="position === WatermarkPosition.TILE
                    ? 'border-primary bg-primary/10 text-primary'
                    : 'border-border hover:border-primary/50'"
                >
                  平铺模式
                </button>
              </div>

              <!-- 透明度 -->
              <div class="space-y-2">
                <div class="flex items-center justify-between">
                  <label class="text-sm">透明度</label>
                  <span class="text-sm font-medium">{{ opacity }}%</span>
                </div>
                <input
                  v-model="opacity"
                  type="range"
                  min="0"
                  max="100"
                  class="w-full h-2 bg-muted rounded-lg appearance-none cursor-pointer accent-primary"
                />
              </div>

              <!-- 旋转角度 -->
              <div class="space-y-2">
                <div class="flex items-center justify-between">
                  <label class="text-sm">旋转角度</label>
                  <span class="text-sm font-medium">{{ rotation }}°</span>
                </div>
                <input
                  v-model="rotation"
                  type="range"
                  min="0"
                  max="360"
                  class="w-full h-2 bg-muted rounded-lg appearance-none cursor-pointer accent-primary"
                />
              </div>

              <!-- 边距 -->
              <div class="grid grid-cols-2 gap-4">
                <div class="space-y-2">
                  <label class="text-sm">水平边距</label>
                  <input
                    v-model="marginX"
                    type="number"
                    min="0"
                    max="500"
                    class="w-full px-3 py-2 text-sm border rounded-md bg-background"
                  />
                </div>
                <div class="space-y-2">
                  <label class="text-sm">垂直边距</label>
                  <input
                    v-model="marginY"
                    type="number"
                    min="0"
                    max="500"
                    class="w-full px-3 py-2 text-sm border rounded-md bg-background"
                  />
                </div>
              </div>

              <!-- 输出设置 -->
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

              <!-- 质量 -->
              <div v-if="outputFormat === 'jpeg' || outputFormat === 'webp'" class="space-y-2">
                <div class="flex items-center justify-between">
                  <label class="text-sm">输出质量</label>
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

              <!-- 操作按钮 -->
              <div class="flex gap-2 pt-2">
                <Button @click="addWatermark" :disabled="isLoading" class="flex-1">
                  <Loader2 v-if="isLoading" class="w-4 h-4 mr-2 animate-spin" />
                  <Stamp v-else class="w-4 h-4 mr-2" />
                  {{ isLoading ? '处理中...' : '添加水印' }}
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

            <!-- 水印结果 -->
            <div v-if="watermarkedResult" class="border rounded-lg p-4 space-y-4">
              <h3 class="text-sm font-medium flex items-center gap-2">
                <CheckCircle class="w-4 h-4 text-green-500" />
                处理结果
              </h3>

              <!-- 水印后预览 -->
              <div class="relative bg-muted/30 rounded-lg overflow-hidden flex items-center justify-center min-h-[200px]">
                <img
                  :src="watermarkedPreviewUrl"
                  alt="带水印图片"
                  class="max-w-full max-h-[300px] object-contain"
                />
              </div>

              <!-- 处理信息 -->
              <div class="grid grid-cols-2 gap-4 text-sm">
                <div class="bg-muted/30 rounded-lg p-3">
                  <p class="text-muted-foreground">原始大小</p>
                  <p class="font-medium">{{ formatFileSize(watermarkedResult.originalSize) }}</p>
                </div>
                <div class="bg-muted/30 rounded-lg p-3">
                  <p class="text-muted-foreground">处理后大小</p>
                  <p class="font-medium">{{ formatFileSize(watermarkedResult.watermarkedSize) }}</p>
                </div>
                <div class="bg-muted/30 rounded-lg p-3">
                  <p class="text-muted-foreground">图片尺寸</p>
                  <p class="font-medium">{{ watermarkedResult.originalWidth }} × {{ watermarkedResult.originalHeight }}</p>
                </div>
                <div class="bg-muted/30 rounded-lg p-3">
                  <p class="text-muted-foreground">输出格式</p>
                  <p class="font-medium">{{ watermarkedResult.format.toUpperCase() }}</p>
                </div>
              </div>

              <!-- 操作按钮 -->
              <div class="flex gap-2">
                <Button variant="outline" @click="copyWatermarkedImage" class="flex-1">
                  <Copy class="w-4 h-4 mr-2" />
                  复制
                </Button>
                <Button @click="downloadWatermarkedImage" class="flex-1">
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
