<script setup lang="ts">
import { ref, computed } from 'vue'
import { Palette, Copy, Check, RefreshCw, Shuffle } from 'lucide-vue-next'

type ColorMode = 'analogous' | 'complementary' | 'triadic' | 'split-complementary' | 'monochromatic' | 'tetradic'

interface ColorPalette {
  primary: string
  colors: string[]
  labels: string[]
}

const primaryColor = ref('#9333EA')
const colorMode = ref<ColorMode>('analogous')
const currentPalette = ref<ColorPalette | null>(null)
const toast = ref({ show: false, message: '' })

const showToast = (message: string) => {
  toast.value.message = message
  toast.value.show = true
  setTimeout(() => toast.value.show = false, 2000)
}

const hexToHsl = (hex: string): { h: number, s: number, l: number } => {
  let r = parseInt(hex.slice(1, 3), 16) / 255
  let g = parseInt(hex.slice(3, 5), 16) / 255
  let b = parseInt(hex.slice(5, 7), 16) / 255

  const max = Math.max(r, g, b)
  const min = Math.min(r, g, b)
  let h = 0, s = 0
  const l = (max + min) / 2

  if (max !== min) {
    const d = max - min
    s = l > 0.5 ? d / (2 - max - min) : d / (max + min)
    switch (max) {
      case r: h = (g - b) / d + (g < b ? 6 : 0); break
      case g: h = (b - r) / d + 2; break
      case b: h = (r - g) / d + 4; break
    }
    h /= 6
  }

  return { h: h * 360, s: s * 100, l: l * 100 }
}

const hslToHex = (h: number, s: number, l: number): string => {
  h /= 360
  s /= 100
  l /= 100

  let r, g, b

  if (s === 0) {
    r = g = b = l
  } else {
    const hue2rgb = (p: number, q: number, t: number) => {
      if (t < 0) t += 1
      if (t > 1) t -= 1
      if (t < 1/6) return p + (q - p) * 6 * t
      if (t < 1/2) return q
      if (t < 2/3) return p + (q - p) * (2/3 - t) * 6
      return p
    }

    const q = l < 0.5 ? l * (1 + s) : l + s - l * s
    const p = 2 * l - q
    r = hue2rgb(p, q, h + 1/3)
    g = hue2rgb(p, q, h)
    b = hue2rgb(p, q, h - 1/3)
  }

  const toHex = (x: number) => {
    const hex = Math.round(x * 255).toString(16)
    return hex.length === 1 ? '0' + hex : hex
  }

  return '#' + toHex(r) + toHex(g) + toHex(b)
}

const generatePalette = () => {
  const hsl = hexToHsl(primaryColor.value)
  let colors: string[] = []
  let labels: string[] = []

  switch (colorMode.value) {
    case 'analogous':
      colors = [
        hslToHex((hsl.h - 30 + 360) % 360, hsl.s, hsl.l),
        primaryColor.value,
        hslToHex((hsl.h + 30) % 360, hsl.s, hsl.l)
      ]
      labels = ['次要色', '主色', '次要色']
      break

    case 'complementary':
      colors = [
        primaryColor.value,
        hslToHex((hsl.h + 180) % 360, hsl.s, hsl.l)
      ]
      labels = ['主色', '互补色']
      break

    case 'triadic':
      colors = [
        primaryColor.value,
        hslToHex((hsl.h + 120) % 360, hsl.s, hsl.l),
        hslToHex((hsl.h + 240) % 360, hsl.s, hsl.l)
      ]
      labels = ['主色', '三角色1', '三角色2']
      break

    case 'split-complementary':
      colors = [
        primaryColor.value,
        hslToHex((hsl.h + 150) % 360, hsl.s, hsl.l),
        hslToHex((hsl.h + 210) % 360, hsl.s, hsl.l)
      ]
      labels = ['主色', '分裂互补1', '分裂互补2']
      break

    case 'monochromatic':
      colors = [
        hslToHex(hsl.h, hsl.s, Math.max(hsl.l - 30, 10)),
        hslToHex(hsl.h, hsl.s, Math.max(hsl.l - 15, 20)),
        primaryColor.value,
        hslToHex(hsl.h, hsl.s, Math.min(hsl.l + 15, 80)),
        hslToHex(hsl.h, hsl.s, Math.min(hsl.l + 30, 90))
      ]
      labels = ['深色1', '深色2', '主色', '浅色1', '浅色2']
      break

    case 'tetradic':
      colors = [
        primaryColor.value,
        hslToHex((hsl.h + 60) % 360, hsl.s, hsl.l),
        hslToHex((hsl.h + 180) % 360, hsl.s, hsl.l),
        hslToHex((hsl.h + 240) % 360, hsl.s, hsl.l)
      ]
      labels = ['主色', '四方1', '四方2', '四方3']
      break
  }

  currentPalette.value = {
    primary: primaryColor.value,
    colors,
    labels
  }
}

const copyColor = async (color: string) => {
  try {
    await navigator.clipboard.writeText(color)
    showToast('已复制: ' + color)
  } catch {
    showToast('复制失败')
  }
}

const randomColor = () => {
  const letters = '0123456789ABCDEF'
  let color = '#'
  for (let i = 0; i < 6; i++) {
    color += letters[Math.floor(Math.random() * 16)]
  }
  primaryColor.value = color
  generatePalette()
}

const colorModes = [
  { value: 'analogous', label: '类比色 (Analogous)', desc: '相邻的颜色' },
  { value: 'complementary', label: '互补色 (Complementary)', desc: '对比色' },
  { value: 'triadic', label: '三角色 (Triadic)', desc: '等边三角形分布' },
  { value: 'split-complementary', label: '分裂互补 (Split)', desc: '互补色两侧' },
  { value: 'monochromatic', label: '单色系 (Mono)', desc: '同色不同深浅' },
  { value: 'tetradic', label: '四方色 (Tetradic)', desc: '矩形分布' }
]

const getContrastColor = (hex: string) => {
  const r = parseInt(hex.slice(1, 3), 16)
  const g = parseInt(hex.slice(3, 5), 16)
  const b = parseInt(hex.slice(5, 7), 16)
  const luminance = (0.299 * r + 0.587 * g + 0.114 * b) / 255
  return luminance > 0.5 ? '#000000' : '#FFFFFF'
}

generatePalette()
</script>

<template>
  <NuxtLayout name="tool" tool-code="color-palette">
    <div class="border border-border/40 rounded-xl overflow-hidden">
      <div class="p-4 border-b bg-muted/20">
        <div class="flex flex-wrap items-center gap-4">
          <div class="flex items-center gap-3">
            <div class="flex items-center gap-2">
              <label class="text-sm">主色:</label>
              <input
                v-model="primaryColor"
                type="color"
                @input="generatePalette"
                class="w-10 h-10 rounded cursor-pointer border-0 p-0"
              />
              <span class="font-mono text-sm">{{ primaryColor }}</span>
            </div>
            <button
              @click="randomColor"
              class="text-xs text-muted-foreground hover:text-foreground px-3 py-1.5 rounded hover:bg-muted transition-colors flex items-center gap-1"
            >
              <Shuffle class="w-3 h-3" />
              随机
            </button>
          </div>

          <div class="ml-auto flex items-center gap-2">
            <button
              @click="generatePalette"
              class="text-xs text-white bg-primary hover:bg-primary/90 px-3 py-1.5 rounded transition-colors flex items-center gap-1"
            >
              <RefreshCw class="w-3 h-3" />
              重新生成
            </button>
          </div>
        </div>
      </div>

      <div class="p-6 space-y-6">
        <div>
          <div class="flex items-center gap-2 mb-4">
            <div class="w-1 h-4 bg-primary rounded-full"></div>
            <label class="text-sm font-medium">配色模式</label>
          </div>
          <div class="grid grid-cols-2 md:grid-cols-3 gap-3">
            <button
              v-for="mode in colorModes"
              :key="mode.value"
              @click="colorMode = mode.value as ColorMode; generatePalette()"
              class="p-3 rounded-lg border transition-all text-left"
              :class="colorMode === mode.value ? 'border-primary bg-primary/5' : 'hover:bg-muted/50'"
            >
              <div class="text-sm font-medium">{{ mode.label }}</div>
              <div class="text-xs text-muted-foreground">{{ mode.desc }}</div>
            </button>
          </div>
        </div>

        <div v-if="currentPalette" class="space-y-6">
          <div>
            <div class="flex items-center gap-2 mb-4">
              <div class="w-1 h-4 bg-purple-500 rounded-full"></div>
              <label class="text-sm font-medium">配色方案</label>
            </div>
            <div class="grid grid-cols-2 md:grid-cols-3 lg:grid-cols-5 gap-4">
              <div
                v-for="(color, index) in currentPalette.colors"
                :key="index"
                class="group relative"
              >
                <div
                  class="h-32 rounded-lg shadow-md transition-transform group-hover:scale-105 cursor-pointer"
                  :style="{ backgroundColor: color }"
                  @click="copyColor(color)"
                >
                  <div
                    class="absolute bottom-0 left-0 right-0 p-3 opacity-0 group-hover:opacity-100 transition-opacity"
                    :style="{ color: getContrastColor(color) }"
                  >
                    <div class="font-mono text-sm">{{ color }}</div>
                    <div class="text-xs opacity-80">{{ currentPalette.labels[index] }}</div>
                  </div>
                </div>
                <div class="mt-2 text-center">
                  <div class="font-mono text-xs">{{ color }}</div>
                  <div class="text-xs text-muted-foreground">{{ currentPalette.labels[index] }}</div>
                  <button
                    @click="copyColor(color)"
                    class="mt-1 text-xs text-primary hover:text-primary/80 flex items-center gap-1 mx-auto"
                  >
                    <Copy class="w-3 h-3" />
                    复制
                  </button>
                </div>
              </div>
            </div>
          </div>

          <div>
            <div class="flex items-center gap-2 mb-4">
              <div class="w-1 h-4 bg-green-500 rounded-full"></div>
              <label class="text-sm font-medium">颜色预览</label>
            </div>
            <div class="space-y-4">
              <div class="p-6 rounded-lg border" :style="{ backgroundColor: currentPalette.colors[0] }">
                <h3 class="text-xl font-bold" :style="{ color: getContrastColor(currentPalette.colors[0]) }">主标题</h3>
                <p class="mt-2" :style="{ color: getContrastColor(currentPalette.colors[0]) + '99' }">
                  这是一段示例文本，用于展示配色效果。
                </p>
              </div>
              <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
                <div
                  v-for="(color, index) in currentPalette.colors"
                  :key="index"
                  class="p-4 rounded-lg border"
                  :style="{ backgroundColor: color }"
                >
                  <div class="font-medium" :style="{ color: getContrastColor(color) }">
                    {{ currentPalette.labels[index] }}
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>

        <div class="p-3 bg-blue-50/50 dark:bg-blue-900/20 rounded-lg border border-blue-100 dark:border-blue-800">
          <div class="text-xs text-blue-600 dark:text-blue-400 font-medium mb-2">配色模式说明:</div>
          <ul class="text-xs text-gray-600 dark:text-gray-300 space-y-1 list-disc list-inside">
            <li><strong>类比色</strong>：色轮上相邻的颜色，和谐自然</li>
            <li><strong>互补色</strong>：色轮上相对的颜色，对比强烈</li>
            <li><strong>三角色</strong>：色轮上均匀分布的三个颜色</li>
            <li><strong>分裂互补</strong>：互补色两侧的颜色，更柔和</li>
            <li><strong>单色系</strong>：同一颜色的不同深浅，简洁统一</li>
            <li><strong>四方色</strong>：色轮上矩形分布的四个颜色</li>
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
