<script setup lang="ts">
import { ref, onMounted, watch, nextTick, computed } from 'vue'
import { Check, Download, Palette, RefreshCw, Image, Type, Sparkles, FileCode } from 'lucide-vue-next'

type Mode = 'icon' | 'text' | 'image'
type ExportFormat = 'png' | 'ico' | 'svg'

const mode = ref<Mode>('text')
const exportFormat = ref<ExportFormat>('png')

const iconText = ref('秋云')
const iconColor = ref('#6366F1')
const bgColor = ref('#FFFFFF')
const fontSize = ref(48)
const borderRadius = ref(25)
const selectedFile = ref<File | null>(null)
const previewUrl = ref<string>('')
const previewImage = ref<HTMLImageElement | null>(null)

const canvasRefs = ref<Map<number, HTMLCanvasElement>>(new Map())

const toast = ref({ show: false, message: '' })

const showToast = (message: string) => {
  toast.value.message = message
  toast.value.show = true
  setTimeout(() => toast.value.show = false, 2000)
}

const sizes = [16, 32, 48, 64, 128, 256, 512]
const previewSizes = [16, 32, 64, 128]

const presetIcons = [
  { emoji: '🚀', name: '火箭' },
  { emoji: '⚡', name: '闪电' },
  { emoji: '🔥', name: '火焰' },
  { emoji: '💡', name: '灯泡' },
  { emoji: '🎯', name: '目标' },
  { emoji: '⭐', name: '星星' },
  { emoji: '💎', name: '钻石' },
  { emoji: '🌟', name: '闪星' },
  { emoji: '🎨', name: '调色板' },
  { emoji: '📱', name: '手机' },
  { emoji: '💻', name: '电脑' },
  { emoji: '🔧', name: '工具' },
  { emoji: '📚', name: '书籍' },
  { emoji: '🎵', name: '音乐' },
  { emoji: '📷', name: '相机' },
  { emoji: '🏠', name: '房屋' },
]

const selectedIcon = ref(presetIcons[0])

const setCanvasRef = (size: number, el: any) => {
  if (el) {
    canvasRefs.value.set(size, el)
    drawToCanvas(el, size)
  }
}

const drawRoundRect = (ctx: CanvasRenderingContext2D, x: number, y: number, w: number, h: number, r: number) => {
  if (w < 2 * r) r = w / 2
  if (h < 2 * r) r = h / 2
  ctx.beginPath()
  ctx.moveTo(x + r, y)
  ctx.arcTo(x + w, y, x + w, y + h, r)
  ctx.arcTo(x + w, y + h, x, y + h, r)
  ctx.arcTo(x, y + h, x, y, r)
  ctx.arcTo(x, y, x + w, y, r)
  ctx.closePath()
}

const drawToCanvas = (canvas: HTMLCanvasElement, size: number) => {
  const ctx = canvas.getContext('2d')
  if (!ctx) return

  ctx.clearRect(0, 0, size, size)

  if (mode.value === 'image' && previewImage.value && previewImage.value.complete) {
    ctx.drawImage(previewImage.value, 0, 0, size, size)
  } else {
    ctx.fillStyle = bgColor.value
    drawRoundRect(ctx, 0, 0, size, size, (size * borderRadius.value) / 100)
    ctx.fill()

    if (mode.value === 'icon') {
      ctx.font = `${Math.max(1, size * 0.6)}px serif`
      ctx.textAlign = 'center'
      ctx.textBaseline = 'middle'
      ctx.fillText(selectedIcon.value.emoji, size / 2, size / 2 + size * 0.02)
    } else {
      ctx.fillStyle = iconColor.value
      const calculatedFontSize = Math.max(1, (size * fontSize.value) / 100)
      ctx.font = `bold ${calculatedFontSize}px "Microsoft YaHei", "PingFang SC", "Hiragino Sans GB", system-ui, sans-serif`
      ctx.textAlign = 'center'
      ctx.textBaseline = 'middle'
      ctx.fillText(iconText.value, size / 2, size / 2 + calculatedFontSize * 0.05)
    }
  }
}

const updateAllCanvases = () => {
  canvasRefs.value.forEach((canvas, size) => {
    drawToCanvas(canvas, size)
  })
}

const generateFavicon = async (size: number): Promise<HTMLCanvasElement | null> => {
  const canvas = document.createElement('canvas')
  canvas.width = size
  canvas.height = size

  if (mode.value === 'image' && previewImage.value) {
    if (!previewImage.value.complete) {
      await new Promise<void>((resolve) => {
        if (previewImage.value) {
          previewImage.value.onload = () => resolve()
        }
      })
    }
  }

  drawToCanvas(canvas, size)
  return canvas
}

const generateSVG = (size: number = 32): string => {
  const r = (size * borderRadius.value) / 100
  
  let content = ''
  if (mode.value === 'image' && previewImage.value) {
    content = `<image href="${previewUrl.value}" width="${size}" height="${size}"/>`
  } else if (mode.value === 'icon') {
    const iconFontSize = size * 0.6
    content = `<text x="${size/2}" y="${size/2 + iconFontSize * 0.35}" text-anchor="middle" dominant-baseline="middle" font-size="${iconFontSize}">${selectedIcon.value.emoji}</text>`
  } else {
    const textFontSize = (size * fontSize.value) / 100
    content = `<text x="${size/2}" y="${size/2 + textFontSize * 0.35}" text-anchor="middle" dominant-baseline="middle" font-size="${textFontSize}" font-weight="bold" font-family="Microsoft YaHei, sans-serif" fill="${iconColor.value}">${iconText.value}</text>`
  }

  return `<svg xmlns="http://www.w3.org/2000/svg" width="${size}" height="${size}" viewBox="0 0 ${size} ${size}">
  <rect width="${size}" height="${size}" rx="${r}" fill="${bgColor.value}"/>
  ${content}
</svg>`
}

const downloadPNG = async (size: number) => {
  const canvas = await generateFavicon(size)
  if (!canvas) return

  const link = document.createElement('a')
  link.download = `favicon-${size}x${size}.png`
  link.href = canvas.toDataURL('image/png')
  link.click()
  showToast(`已下载 ${size}x${size} PNG`)
}

const downloadICO = async (size: number) => {
  const canvas = await generateFavicon(size)
  if (!canvas) return
  
  const pngBlob = await new Promise<Blob>((resolve) => {
    canvas.toBlob(resolve, 'image/png')
  })
  
  if (!pngBlob) return
  
  const headerSize = 6
  const dirEntrySize = 16
  const pngArrayBuffer = await pngBlob.arrayBuffer()
  const totalSize = headerSize + dirEntrySize + pngArrayBuffer.byteLength
  
  const buffer = new ArrayBuffer(totalSize)
  const view = new DataView(buffer)
  const uint8Array = new Uint8Array(buffer)
  
  view.setUint16(0, 0, true)
  view.setUint16(2, 1, true)
  view.setUint16(4, 1, true)
  
  let pos = 6
  view.setUint8(pos++, size === 256 ? 0 : size)
  view.setUint8(pos++, size === 256 ? 0 : size)
  view.setUint8(pos++, 0)
  view.setUint8(pos++, 0)
  view.setUint16(pos, 1, true)
  pos += 2
  view.setUint16(pos, 32, true)
  pos += 2
  view.setUint32(pos, pngArrayBuffer.byteLength, true)
  pos += 4
  view.setUint32(pos, headerSize + dirEntrySize, true)
  pos += 4
  
  const pngData = new Uint8Array(pngArrayBuffer)
  for (let i = 0; i < pngData.length; i++) {
    uint8Array[headerSize + dirEntrySize + i] = pngData[i]
  }
  
  const blob = new Blob([buffer], { type: 'image/x-icon' })
  const link = document.createElement('a')
  link.download = `favicon-${size}x${size}.ico`
  link.href = URL.createObjectURL(blob)
  link.click()
  URL.revokeObjectURL(link.href)
  showToast(`已下载 ${size}x${size} ICO`)
}

const downloadSVG = () => {
  const svgContent = generateSVG(32)
  const blob = new Blob([svgContent], { type: 'image/svg+xml' })
  const link = document.createElement('a')
  link.download = 'favicon.svg'
  link.href = URL.createObjectURL(blob)
  link.click()
  URL.revokeObjectURL(link.href)
  showToast('已下载 favicon.svg')
}

const downloadFavicon = async (size: number) => {
  switch (exportFormat.value) {
    case 'png':
      await downloadPNG(size)
      break
    case 'ico':
      await downloadICO(size)
      break
    case 'svg':
      downloadSVG()
      break
  }
}

const downloadAll = async () => {
  if (exportFormat.value === 'ico') {
    sizes.forEach((size, index) => {
      setTimeout(() => {
        downloadICO(size)
      }, index * 500)
    })
  } else if (exportFormat.value === 'svg') {
    downloadSVG()
  } else {
    sizes.forEach((size, index) => {
      setTimeout(() => {
        downloadPNG(size)
      }, index * 500)
    })
  }
}

const handleFileChange = (e: Event) => {
  const target = e.target as HTMLInputElement
  if (target.files && target.files[0]) {
    selectedFile.value = target.files[0]
    const reader = new FileReader()
    reader.onload = (e) => {
      previewUrl.value = e.target?.result as string
      const img = document.createElement('img')
      img.crossOrigin = 'anonymous'
      img.onload = () => {
        previewImage.value = img
        updateAllCanvases()
      }
      img.src = previewUrl.value
    }
    reader.readAsDataURL(target.files[0])
  }
}

const reset = () => {
  mode.value = 'text'
  iconText.value = '秋云'
  iconColor.value = '#6366F1'
  bgColor.value = '#FFFFFF'
  fontSize.value = 48
  borderRadius.value = 25
  selectedFile.value = null
  previewUrl.value = ''
  previewImage.value = null
  selectedIcon.value = presetIcons[0]
  exportFormat.value = 'png'
  nextTick(() => {
    updateAllCanvases()
  })
}

watch(
  [mode, iconText, iconColor, bgColor, fontSize, borderRadius, selectedIcon],
  () => {
    updateAllCanvases()
  }
)

onMounted(() => {
  nextTick(() => {
    updateAllCanvases()
  })
})
</script>

<template>
  <NuxtLayout name="tool" tool-code="favicon-generator">
    <div class="border border-border/40 rounded-xl overflow-hidden">
      <div class="p-4 border-b bg-muted/20">
        <div class="flex flex-wrap items-center gap-4">
          <div class="flex items-center gap-2">
            <Image class="w-5 h-5 text-primary" />
            <span class="text-sm font-medium">Favicon生成器</span>
          </div>

          <div class="flex items-center gap-2">
            <button
              @click="mode = 'text'"
              class="px-3 py-1.5 rounded text-sm transition-colors"
              :class="mode === 'text' ? 'bg-primary text-white' : 'hover:bg-muted'"
            >
              <Type class="w-4 h-4 inline mr-1" />
              文字
            </button>
            <button
              @click="mode = 'icon'"
              class="px-3 py-1.5 rounded text-sm transition-colors"
              :class="mode === 'icon' ? 'bg-primary text-white' : 'hover:bg-muted'"
            >
              <Sparkles class="w-4 h-4 inline mr-1" />
              图标
            </button>
            <button
              @click="mode = 'image'"
              class="px-3 py-1.5 rounded text-sm transition-colors"
              :class="mode === 'image' ? 'bg-primary text-white' : 'hover:bg-muted'"
            >
              <Image class="w-4 h-4 inline mr-1" />
              图片
            </button>
          </div>

          <div class="flex items-center gap-2 ml-auto">
            <select
              v-model="exportFormat"
              class="px-3 py-1.5 text-sm border rounded bg-background"
            >
              <option value="png">PNG格式</option>
              <option value="ico">ICO格式</option>
              <option value="svg">SVG格式</option>
            </select>
            <button
              @click="reset"
              class="text-xs text-muted-foreground hover:text-foreground px-3 py-1.5 rounded hover:bg-muted transition-colors flex items-center gap-1"
            >
              <RefreshCw class="w-3 h-3" />
              重置
            </button>
          </div>
        </div>
      </div>

      <div class="p-6">
        <div class="grid grid-cols-1 lg:grid-cols-3 gap-6">
          <div class="lg:col-span-2 space-y-6">
            <div>
              <div class="flex items-center gap-2 mb-4">
                <div class="w-1 h-4 bg-green-500 rounded-full"></div>
                <label class="text-sm font-medium">预览</label>
              </div>
              <div class="flex items-center justify-center gap-8 p-8 bg-muted/30 rounded-lg min-h-[200px]">
                <div
                  v-for="size in previewSizes"
                  :key="size"
                  class="flex flex-col items-center gap-2"
                >
                  <div
                    class="border-2 border-border rounded bg-white"
                    :style="{ width: size + 'px', height: size + 'px' }"
                  >
                    <canvas
                      :ref="(el) => setCanvasRef(size, el)"
                      :width="size"
                      :height="size"
                      class="w-full h-full"
                    />
                  </div>
                  <span class="text-xs text-muted-foreground">{{ size }}x{{ size }}</span>
                </div>
              </div>
            </div>

            <div v-if="mode === 'icon'" class="space-y-4">
              <div class="flex items-center gap-2 mb-2">
                <div class="w-1 h-4 bg-primary rounded-full"></div>
                <label class="text-sm font-medium">选择图标</label>
              </div>
              <div class="grid grid-cols-8 gap-2">
                <button
                  v-for="icon in presetIcons"
                  :key="icon.emoji"
                  @click="selectedIcon = icon"
                  class="p-3 text-2xl rounded-lg border transition-all hover:scale-110"
                  :class="selectedIcon.emoji === icon.emoji 
                    ? 'border-primary bg-primary/10 ring-2 ring-primary/20' 
                    : 'border-border hover:border-primary/50'"
                  :title="icon.name"
                >
                  {{ icon.emoji }}
                </button>
              </div>
              
              <div class="grid grid-cols-2 gap-4 pt-4">
                <div>
                  <div class="flex items-center justify-between mb-1">
                    <span class="text-xs text-muted-foreground">背景颜色</span>
                  </div>
                  <input
                    type="color"
                    v-model="bgColor"
                    class="w-full h-10 rounded cursor-pointer border border-border"
                  />
                </div>
                <div>
                  <div class="flex items-center justify-between mb-1">
                    <span class="text-xs text-muted-foreground">圆角</span>
                    <span class="text-xs font-mono">{{ borderRadius }}%</span>
                  </div>
                  <input
                    type="range"
                    v-model.number="borderRadius"
                    min="0"
                    max="50"
                    class="w-full mt-2"
                  />
                </div>
              </div>
            </div>

            <div v-if="mode === 'text'" class="grid grid-cols-2 gap-4">
              <div class="space-y-4">
                <div class="flex items-center gap-2 mb-2">
                  <div class="w-1 h-4 bg-primary rounded-full"></div>
                  <label class="text-sm font-medium">样式设置</label>
                </div>

                <div>
                  <div class="flex items-center justify-between mb-1">
                    <span class="text-xs text-muted-foreground">文字内容</span>
                  </div>
                  <input
                    type="text"
                    v-model="iconText"
                    maxlength="3"
                    class="w-full px-3 py-2 border border-border rounded-lg bg-background text-sm"
                  />
                </div>

                <div>
                  <div class="flex items-center justify-between mb-1">
                    <span class="text-xs text-muted-foreground">文字颜色</span>
                  </div>
                  <input
                    type="color"
                    v-model="iconColor"
                    class="w-full h-10 rounded cursor-pointer border border-border"
                  />
                </div>

                <div>
                  <div class="flex items-center justify-between mb-1">
                    <span class="text-xs text-muted-foreground">背景颜色</span>
                  </div>
                  <input
                    type="color"
                    v-model="bgColor"
                    class="w-full h-10 rounded cursor-pointer border border-border"
                  />
                </div>
              </div>

              <div class="space-y-4">
                <div class="flex items-center gap-2 mb-2">
                  <div class="w-1 h-4 bg-purple-500 rounded-full"></div>
                  <label class="text-sm font-medium">尺寸设置</label>
                </div>

                <div>
                  <div class="flex items-center justify-between mb-1">
                    <span class="text-xs text-muted-foreground">字体大小</span>
                    <span class="text-xs font-mono">{{ fontSize }}%</span>
                  </div>
                  <input
                    type="range"
                    v-model.number="fontSize"
                    min="20"
                    max="80"
                    class="w-full"
                  />
                </div>

                <div>
                  <div class="flex items-center justify-between mb-1">
                    <span class="text-xs text-muted-foreground">圆角</span>
                    <span class="text-xs font-mono">{{ borderRadius }}%</span>
                  </div>
                  <input
                    type="range"
                    v-model.number="borderRadius"
                    min="0"
                    max="50"
                    class="w-full"
                  />
                </div>
              </div>
            </div>

            <div v-if="mode === 'image'" class="space-y-4">
              <div class="p-6 border border-border rounded-lg text-center">
                <input
                  type="file"
                  @change="handleFileChange"
                  accept="image/*"
                  class="hidden"
                  id="image-upload"
                />
                <label
                  for="image-upload"
                  class="cursor-pointer inline-flex flex-col items-center gap-2 p-8 border-2 border-dashed border-border rounded-lg hover:border-primary transition-colors"
                >
                  <Image class="w-12 h-12 text-muted-foreground" />
                  <span class="text-sm text-muted-foreground">点击上传图片</span>
                  <span class="text-xs text-muted-foreground">支持 JPG、PNG、GIF、SVG 格式</span>
                </label>
                <div v-if="previewUrl" class="mt-4 flex justify-center">
                  <img :src="previewUrl" class="w-24 h-24 rounded-lg border border-border object-contain" />
                </div>
              </div>
              
              <div class="grid grid-cols-2 gap-4">
                <div>
                  <div class="flex items-center justify-between mb-1">
                    <span class="text-xs text-muted-foreground">圆角</span>
                    <span class="text-xs font-mono">{{ borderRadius }}%</span>
                  </div>
                  <input
                    type="range"
                    v-model.number="borderRadius"
                    min="0"
                    max="50"
                    class="w-full"
                  />
                </div>
              </div>
            </div>
          </div>

          <div class="space-y-6">
            <div>
              <div class="flex items-center justify-between mb-4">
                <div class="flex items-center gap-2">
                  <div class="w-1 h-4 bg-purple-500 rounded-full"></div>
                  <label class="text-sm font-medium">下载选项</label>
                </div>
              </div>
              
              <div v-if="exportFormat === 'png'" class="space-y-2">
                <button
                  v-for="size in sizes"
                  :key="size"
                  @click="downloadPNG(size)"
                  class="w-full flex items-center justify-between px-4 py-3 border border-border rounded-lg hover:bg-muted transition-colors"
                >
                  <span class="text-sm">{{ size }}x{{ size }}</span>
                  <Download class="w-4 h-4 text-muted-foreground" />
                </button>
              </div>
              
              <div v-else-if="exportFormat === 'ico'" class="space-y-2">
                <button
                  v-for="size in sizes"
                  :key="size"
                  @click="downloadICO(size)"
                  class="w-full flex items-center justify-between px-4 py-3 border border-border rounded-lg hover:bg-muted transition-colors"
                >
                  <span class="text-sm">{{ size }}x{{ size }}</span>
                  <Download class="w-4 h-4 text-muted-foreground" />
                </button>
              </div>
              
              <div v-else-if="exportFormat === 'svg'" class="space-y-2">
                <button
                  @click="downloadSVG"
                  class="w-full flex items-center justify-between px-4 py-3 border border-border rounded-lg hover:bg-muted transition-colors"
                >
                  <div class="flex items-center gap-2">
                    <FileCode class="w-4 h-4 text-muted-foreground" />
                    <span class="text-sm">favicon.svg</span>
                  </div>
                  <Download class="w-4 h-4 text-muted-foreground" />
                </button>
                <p class="text-xs text-muted-foreground px-2">
                  SVG 格式为矢量图，可无限缩放
                </p>
              </div>
            </div>

            <div class="p-3 bg-primary/5 rounded-lg border border-primary/20">
              <div class="text-xs text-primary font-medium mb-2">使用说明：</div>
              <ul class="text-xs text-muted-foreground space-y-1 list-disc list-inside">
                <li><strong>图标模式</strong>：选择预设 Emoji 图标</li>
                <li><strong>文字模式</strong>：自定义文字、颜色、背景</li>
                <li><strong>图片模式</strong>：上传图片转换为 Favicon</li>
                <li><strong>PNG</strong>：多种尺寸，适合现代浏览器</li>
                <li><strong>ICO</strong>：兼容旧浏览器，多种尺寸可选</li>
                <li><strong>SVG</strong>：矢量格式，可无限缩放</li>
              </ul>
            </div>
          </div>
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
