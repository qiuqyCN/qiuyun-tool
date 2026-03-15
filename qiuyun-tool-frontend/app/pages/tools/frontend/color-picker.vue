<script setup lang="ts">
import { ref, watch } from 'vue'
import { Copy, Check, Palette, RefreshCw, History, Trash2 } from 'lucide-vue-next'

const currentColor = ref('#6366F1')
const red = ref(99)
const green = ref(102)
const blue = ref(241)
const alpha = ref(1)
const hue = ref(239)
const saturation = ref(84)
const lightness = ref(67)

const colorHistory = ref<string[]>([])
const toast = ref({ show: false, message: '' })

const showToast = (message: string) => {
  toast.value.message = message
  toast.value.show = true
  setTimeout(() => toast.value.show = false, 2000)
}

const hexToRgb = (hex: string) => {
  const result = /^#?([a-f\d]{2})([a-f\d]{2})([a-f\d]{2})$/i.exec(hex)
  return result
    ? {
        r: parseInt(result[1]!, 16),
        g: parseInt(result[2]!, 16),
        b: parseInt(result[3]!, 16)
      }
    : null
}

const rgbToHsl = (r: number, g: number, b: number) => {
  r /= 255
  g /= 255
  b /= 255
  const max = Math.max(r, g, b)
  const min = Math.min(r, g, b)
  let h: number = 0, s: number = 0, l: number = (max + min) / 2

  if (max === min) {
    h = s = 0
  } else {
    const d = max - min
    s = l > 0.5 ? d / (2 - max - min) : d / (max + min)
    switch (max) {
      case r: h = (g - b) / d + (g < b ? 6 : 0); break
      case g: h = (b - r) / d + 2; break
      case b: h = (r - g) / d + 4; break
    }
    h /= 6
  }

  return {
    h: Math.round(h * 360),
    s: Math.round(s * 100),
    l: Math.round(l * 100)
  }
}

const hslToRgb = (h: number, s: number, l: number) => {
  s /= 100
  l /= 100
  const a = s * Math.min(l, 1 - l)
  const f = (n: number) => {
    const k = (n + h / 30) % 12
    const color = l - a * Math.max(Math.min(k - 3, 9 - k, 1), -1)
    return Math.round(255 * color)
  }
  return { r: f(0), g: f(8), b: f(4) }
}

const rgbToHex = (r: number, g: number, b: number) => {
  return '#' + [r, g, b].map(x => {
    const hex = x.toString(16)
    return hex.length === 1 ? '0' + hex : hex
  }).join('').toUpperCase()
}

const updateFromHex = () => {
  const rgb = hexToRgb(currentColor.value)
  if (rgb) {
    red.value = rgb.r
    green.value = rgb.g
    blue.value = rgb.b
    const hsl = rgbToHsl(rgb.r, rgb.g, rgb.b)
    hue.value = hsl.h
    saturation.value = hsl.s
    lightness.value = hsl.l
    addToHistory(currentColor.value)
  }
}

const updateFromRgb = () => {
  currentColor.value = rgbToHex(red.value, green.value, blue.value)
  const hsl = rgbToHsl(red.value, green.value, blue.value)
  hue.value = hsl.h
  saturation.value = hsl.s
  lightness.value = hsl.l
  addToHistory(currentColor.value)
}

const updateFromHsl = () => {
  const rgb = hslToRgb(hue.value, saturation.value, lightness.value)
  red.value = rgb.r
  green.value = rgb.g
  blue.value = rgb.b
  currentColor.value = rgbToHex(rgb.r, rgb.g, rgb.b)
  addToHistory(currentColor.value)
}

const addToHistory = (color: string) => {
  const normalizedColor = color.toUpperCase()
  const index = colorHistory.value.indexOf(normalizedColor)
  if (index > -1) {
    colorHistory.value.splice(index, 1)
  }
  colorHistory.value.unshift(normalizedColor)
  if (colorHistory.value.length > 20) {
    colorHistory.value.pop()
  }
}

const selectFromHistory = (color: string) => {
  currentColor.value = color
  updateFromHex()
}

const clearHistory = () => {
  colorHistory.value = []
}

const copyToClipboard = async (text: string) => {
  try {
    await navigator.clipboard.writeText(text)
    showToast('已复制')
  } catch {
    showToast('复制失败')
  }
}

const getHexValue = () => currentColor.value
const getRgbValue = () => `rgb(${red.value}, ${green.value}, ${blue.value})`
const getRgbaValue = () => `rgba(${red.value}, ${green.value}, ${blue.value}, ${alpha.value})`
const getHslValue = () => `hsl(${hue.value}, ${saturation.value}%, ${lightness.value}%)`
const getHslaValue = () => `hsla(${hue.value}, ${saturation.value}%, ${lightness.value}%, ${alpha.value})`
</script>

<template>
  <NuxtLayout name="tool" tool-code="color-picker">
    <div class="border border-border/40 rounded-xl overflow-hidden">
      <div class="p-4 border-b bg-muted/20">
        <div class="flex flex-wrap items-center gap-4">
          <div class="flex items-center gap-2">
            <Palette class="w-5 h-5 text-primary" />
            <span class="text-sm font-medium">颜色选择器</span>
          </div>

          <div class="ml-auto flex items-center gap-2">
            <button
              @click="colorHistory.length > 0 && clearHistory"
              :disabled="colorHistory.length === 0"
              class="text-xs text-muted-foreground hover:text-foreground px-3 py-1.5 rounded hover:bg-muted transition-colors flex items-center gap-1 disabled:opacity-50 disabled:cursor-not-allowed"
            >
              <Trash2 class="w-3 h-3" />
              清空历史
            </button>
          </div>
        </div>
      </div>

      <div class="p-6">
        <div class="grid grid-cols-1 lg:grid-cols-2 gap-6">
          <div class="space-y-6">
            <div>
              <div class="flex items-center gap-2 mb-4">
                <div class="w-1 h-4 bg-primary rounded-full"></div>
                <label class="text-sm font-medium">选择颜色</label>
              </div>
              <div class="grid grid-cols-2 gap-4">
                <div class="flex flex-col items-center gap-4">
                  <input
                    type="color"
                    v-model="currentColor"
                    @input="updateFromHex"
                    class="w-40 h-40 rounded-lg cursor-pointer border-2 border-border"
                  />
                  <div
                    class="w-full h-24 rounded-lg border-2 border-border flex items-center justify-center font-mono text-sm"
                    :style="{ backgroundColor: currentColor }"
                  >
                    <span :style="{ color: lightness > 50 ? '#000' : '#fff' }">{{ currentColor }}</span>
                  </div>
                </div>
                <div class="space-y-4">
                  <div class="p-3 border border-border rounded-lg">
                    <div class="flex items-center justify-between mb-2">
                      <span class="text-xs font-medium">HEX</span>
                      <button @click="copyToClipboard(getHexValue())" class="text-xs text-primary hover:underline">复制</button>
                    </div>
                    <div class="font-mono text-sm">{{ getHexValue() }}</div>
                  </div>
                  <div class="p-3 border border-border rounded-lg">
                    <div class="flex items-center justify-between mb-2">
                      <span class="text-xs font-medium">RGB</span>
                      <button @click="copyToClipboard(getRgbValue())" class="text-xs text-primary hover:underline">复制</button>
                    </div>
                    <div class="font-mono text-sm">{{ getRgbValue() }}</div>
                  </div>
                  <div class="p-3 border border-border rounded-lg">
                    <div class="flex items-center justify-between mb-2">
                      <span class="text-xs font-medium">RGBA</span>
                      <button @click="copyToClipboard(getRgbaValue())" class="text-xs text-primary hover:underline">复制</button>
                    </div>
                    <div class="font-mono text-sm">{{ getRgbaValue() }}</div>
                  </div>
                  <div class="p-3 border border-border rounded-lg">
                    <div class="flex items-center justify-between mb-2">
                      <span class="text-xs font-medium">HSL</span>
                      <button @click="copyToClipboard(getHslValue())" class="text-xs text-primary hover:underline">复制</button>
                    </div>
                    <div class="font-mono text-sm">{{ getHslValue() }}</div>
                  </div>
                  <div class="p-3 border border-border rounded-lg">
                    <div class="flex items-center justify-between mb-2">
                      <span class="text-xs font-medium">HSLA</span>
                      <button @click="copyToClipboard(getHslaValue())" class="text-xs text-primary hover:underline">复制</button>
                    </div>
                    <div class="font-mono text-sm">{{ getHslaValue() }}</div>
                  </div>
                </div>
              </div>
            </div>

            <div class="grid grid-cols-2 gap-4">
              <div class="space-y-4">
                <div class="flex items-center gap-2 mb-2">
                  <div class="w-1 h-4 bg-red-500 rounded-full"></div>
                  <label class="text-sm font-medium">RGB</label>
                </div>
                <div class="space-y-3">
                  <div>
                    <div class="flex items-center justify-between mb-1">
                      <span class="text-xs text-muted-foreground">R (红)</span>
                      <span class="text-xs font-mono">{{ red }}</span>
                    </div>
                    <input
                      type="range"
                      v-model.number="red"
                      @input="updateFromRgb"
                      min="0"
                      max="255"
                      class="w-full"
                    />
                  </div>
                  <div>
                    <div class="flex items-center justify-between mb-1">
                      <span class="text-xs text-muted-foreground">G (绿)</span>
                      <span class="text-xs font-mono">{{ green }}</span>
                    </div>
                    <input
                      type="range"
                      v-model.number="green"
                      @input="updateFromRgb"
                      min="0"
                      max="255"
                      class="w-full"
                    />
                  </div>
                  <div>
                    <div class="flex items-center justify-between mb-1">
                      <span class="text-xs text-muted-foreground">B (蓝)</span>
                      <span class="text-xs font-mono">{{ blue }}</span>
                    </div>
                    <input
                      type="range"
                      v-model.number="blue"
                      @input="updateFromRgb"
                      min="0"
                      max="255"
                      class="w-full"
                    />
                  </div>
                  <div>
                    <div class="flex items-center justify-between mb-1">
                      <span class="text-xs text-muted-foreground">Alpha</span>
                      <span class="text-xs font-mono">{{ alpha }}</span>
                    </div>
                    <input
                      type="range"
                      v-model.number="alpha"
                      min="0"
                      max="1"
                      step="0.01"
                      class="w-full"
                    />
                  </div>
                </div>
              </div>

              <div class="space-y-4">
                <div class="flex items-center gap-2 mb-2">
                  <div class="w-1 h-4 bg-purple-500 rounded-full"></div>
                  <label class="text-sm font-medium">HSL</label>
                </div>
                <div class="space-y-3">
                  <div>
                    <div class="flex items-center justify-between mb-1">
                      <span class="text-xs text-muted-foreground">H (色相)</span>
                      <span class="text-xs font-mono">{{ hue }}°</span>
                    </div>
                    <input
                      type="range"
                      v-model.number="hue"
                      @input="updateFromHsl"
                      min="0"
                      max="360"
                      class="w-full"
                    />
                  </div>
                  <div>
                    <div class="flex items-center justify-between mb-1">
                      <span class="text-xs text-muted-foreground">S (饱和度)</span>
                      <span class="text-xs font-mono">{{ saturation }}%</span>
                    </div>
                    <input
                      type="range"
                      v-model.number="saturation"
                      @input="updateFromHsl"
                      min="0"
                      max="100"
                      class="w-full"
                    />
                  </div>
                  <div>
                    <div class="flex items-center justify-between mb-1">
                      <span class="text-xs text-muted-foreground">L (亮度)</span>
                      <span class="text-xs font-mono">{{ lightness }}%</span>
                    </div>
                    <input
                      type="range"
                      v-model.number="lightness"
                      @input="updateFromHsl"
                      min="0"
                      max="100"
                      class="w-full"
                    />
                  </div>
                </div>
              </div>
            </div>
          </div>

          <div class="space-y-6">
            <div>
              <div class="flex items-center gap-2 mb-4">
                <div class="w-1 h-4 bg-green-500 rounded-full"></div>
                <div class="flex items-center gap-2">
                  <label class="text-sm font-medium">历史颜色</label>
                  <History class="w-4 h-4 text-muted-foreground" />
                </div>
              </div>
              <div class="border border-border rounded-lg p-4">
                <div v-if="colorHistory.length > 0" class="grid grid-cols-5 gap-2">
                  <button
                    v-for="color in colorHistory"
                    :key="color"
                    @click="selectFromHistory(color)"
                    class="w-full aspect-square rounded-lg border-2 border-border hover:border-primary transition-colors flex items-center justify-center"
                    :style="{ backgroundColor: color }"
                    :title="color"
                  >
                    <span class="text-[8px] font-mono text-white drop-shadow-md" v-if="color.length > 0">{{ color.slice(1) }}</span>
                  </button>
                </div>
                <div v-else class="text-center text-muted-foreground text-sm py-8">
                  暂无历史颜色
                </div>
              </div>
            </div>

            <div class="p-3 bg-indigo-50/50 dark:bg-indigo-900/20 rounded-lg border border-indigo-100 dark:border-indigo-800">
              <div class="text-xs text-indigo-600 dark:text-indigo-400 font-medium mb-2">使用说明：</div>
              <ul class="text-xs text-gray-600 dark:text-gray-300 space-y-1 list-disc list-inside">
                <li>使用颜色选择器选择颜色，或手动输入HEX值</li>
                <li>通过RGB或HSL滑块精确调整颜色</li>
                <li>所有颜色格式（HEX/RGB/RGBA/HSL/HSLA）实时转换</li>
                <li>点击格式名称旁的复制按钮快速复制</li>
                <li>历史颜色自动保存，点击可快速选择</li>
              </ul>
            </div>

            <div class="p-3 bg-blue-50/50 dark:bg-blue-900/20 rounded-lg border border-blue-100 dark:border-blue-800">
              <div class="text-xs text-blue-600 dark:text-blue-400 font-medium mb-2">颜色格式说明：</div>
              <div class="space-y-2 text-xs text-gray-600 dark:text-gray-300">
                <div>
                  <strong class="text-blue-600 dark:text-blue-400">HEX</strong>：十六进制颜色表示，格式为#RRGGBB，如#6366F1。其中RR、GG、BB分别表示红、绿、蓝三原色的十六进制值（00-FF），是Web开发中最常用的颜色格式。
                </div>
                <div>
                  <strong class="text-blue-600 dark:text-blue-400">RGB</strong>：红绿蓝三原色表示，格式为rgb(R, G, B)，如rgb(99, 102, 241)。每个参数的取值范围是0-255，数值越大颜色越亮。
                </div>
                <div>
                  <strong class="text-blue-600 dark:text-blue-400">RGBA</strong>：带透明度的RGB格式，格式为rgba(R, G, B, A)，如rgba(99, 102, 241, 0.8)。最后一个参数A表示透明度，取值范围0-1，0表示完全透明，1表示完全不透明。
                </div>
                <div>
                  <strong class="text-blue-600 dark:text-blue-400">HSL</strong>：色相、饱和度、亮度表示，格式为hsl(H, S%, L%)，如hsl(239, 84%, 67%)。H（色相）取值0-360°，S（饱和度）0-100%，L（亮度）0-100%。这种格式更符合人类对颜色的认知，便于调整颜色。
                </div>
                <div>
                  <strong class="text-blue-600 dark:text-blue-400">HSLA</strong>：带透明度的HSL格式，格式为hsla(H, S%, L%, A)，如hsla(239, 84%, 67%, 0.8)。最后一个参数A表示透明度，取值范围0-1。
                </div>
              </div>
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
