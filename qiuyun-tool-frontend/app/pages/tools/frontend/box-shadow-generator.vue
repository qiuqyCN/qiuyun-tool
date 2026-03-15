<script setup lang="ts">
import { ref, computed, watch, onMounted } from 'vue'
import { Copy, Check, Layers, RefreshCw, Plus, Trash2 } from 'lucide-vue-next'

interface Shadow {
  id: number
  horizontal: number
  vertical: number
  blur: number
  spread: number
  color: string
  opacity: number
  inset: boolean
}

const shadows = ref<Shadow[]>([
  {
    id: 1,
    horizontal: 0,
    vertical: 4,
    blur: 6,
    spread: -1,
    color: '#000000',
    opacity: 0.1,
    inset: false
  },
  {
    id: 2,
    horizontal: 0,
    vertical: 2,
    blur: 4,
    spread: -2,
    color: '#000000',
    opacity: 0.06,
    inset: false
  }
])

let nextId = 3
const toast = ref({ show: false, message: '' })

const showToast = (message: string) => {
  toast.value.message = message
  toast.value.show = true
  setTimeout(() => toast.value.show = false, 2000)
}

const addShadow = () => {
  shadows.value.push({
    id: nextId++,
    horizontal: 0,
    vertical: 0,
    blur: 10,
    spread: 0,
    color: '#000000',
    opacity: 0.3,
    inset: false
  })
}

const removeShadow = (id: number) => {
  if (shadows.value.length > 1) {
    shadows.value = shadows.value.filter(s => s.id !== id)
  }
}

const reset = () => {
  shadows.value = [
    {
      id: 1,
      horizontal: 0,
      vertical: 4,
      blur: 6,
      spread: -1,
      color: '#000000',
      opacity: 0.1,
      inset: false
    },
    {
      id: 2,
      horizontal: 0,
      vertical: 2,
      blur: 4,
      spread: -2,
      color: '#000000',
      opacity: 0.06,
      inset: false
    }
  ]
  nextId = 3
}

const hexToRgba = (hex: string, opacity: number) => {
  const result = /^#?([a-f\d]{2})([a-f\d]{2})([a-f\d]{2})$/i.exec(hex)
  return result
    ? `rgba(${parseInt(result[1]!, 16)}, ${parseInt(result[2]!, 16)}, ${parseInt(result[3]!, 16)}, ${opacity})`
    : hex
}

const boxShadow = computed(() => {
  return shadows.value.map(shadow => {
    const rgba = hexToRgba(shadow.color, shadow.opacity)
    let parts: string[] = []
    if (shadow.inset) {
      parts.push('inset')
    }
    parts.push(`${shadow.horizontal}px`)
    parts.push(`${shadow.vertical}px`)
    parts.push(`${shadow.blur}px`)
    if (shadow.spread !== 0) {
      parts.push(`${shadow.spread}px`)
    }
    parts.push(rgba)
    return parts.join(' ')
  }).join(', ')
})

const generatedCode = computed(() => {
  return `box-shadow: ${boxShadow.value};`
})

const copyCode = async () => {
  try {
    await navigator.clipboard.writeText(generatedCode.value)
    showToast('已复制代码')
  } catch {
    showToast('复制失败')
  }
}

const forceUpdate = ref(0)
watch(shadows, () => {
  forceUpdate.value++
}, { deep: true })
</script>

<template>
  <NuxtLayout name="tool" tool-code="box-shadow-generator">
    <div class="border border-border/40 rounded-xl overflow-hidden">
      <div class="p-4 border-b bg-muted/20">
        <div class="flex flex-wrap items-center gap-4">
          <div class="flex items-center gap-2">
            <Layers class="w-5 h-5 text-primary" />
            <span class="text-sm font-medium">CSS阴影生成器</span>
          </div>

          <div class="ml-auto flex items-center gap-2">
            <button
              @click="addShadow"
              class="text-xs text-white bg-primary hover:bg-primary/90 px-3 py-1.5 rounded transition-colors flex items-center gap-1"
            >
              <Plus class="w-3 h-3" />
              添加阴影
            </button>
            <button
              @click="reset"
              class="text-xs text-muted-foreground hover:text-foreground px-3 py-1.5 rounded hover:bg-muted transition-colors flex items-center gap-1"
            >
              <RefreshCw class="w-3 h-3" />
              重置
            </button>
            <button
              @click="copyCode"
              class="text-xs text-white bg-primary hover:bg-primary/90 px-3 py-1.5 rounded transition-colors flex items-center gap-1"
            >
              <Copy class="w-3 h-3" />
              复制
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
              <div class="flex items-center justify-center p-12 bg-muted/30 rounded-lg min-h-[300px]">
                <div
                  class="w-48 h-48 bg-white dark:bg-gray-800 rounded-2xl flex items-center justify-center"
                  :key="forceUpdate"
                  :style="{ boxShadow: boxShadow }"
                >
                  <span class="text-4xl">🎨</span>
                </div>
              </div>
            </div>

            <div>
              <div class="flex items-center gap-2 mb-4">
                <div class="w-1 h-4 bg-primary rounded-full"></div>
                <label class="text-sm font-medium">阴影层</label>
              </div>
              <div class="space-y-4">
                <div
                  v-for="shadow in shadows"
                  :key="shadow.id"
                  class="p-4 border border-border rounded-lg bg-muted/10"
                >
                  <div class="flex items-center justify-between mb-4">
                    <span class="text-sm font-medium">阴影 #{{ shadows.indexOf(shadow) + 1 }}</span>
                    <button
                      v-if="shadows.length > 1"
                      @click="removeShadow(shadow.id)"
                      class="p-1 rounded hover:bg-muted text-muted-foreground hover:text-red-500 transition-colors"
                    >
                      <Trash2 class="w-4 h-4" />
                    </button>
                  </div>
                  <div class="grid grid-cols-2 gap-4">
                    <div>
                      <div class="flex items-center justify-between mb-1">
                        <span class="text-xs text-muted-foreground">水平偏移 (X)</span>
                        <span class="text-xs font-mono">{{ shadow.horizontal }}px</span>
                      </div>
                      <input
                        type="range"
                        v-model.number="shadow.horizontal"
                        min="-100"
                        max="100"
                        class="w-full"
                      />
                    </div>
                    <div>
                      <div class="flex items-center justify-between mb-1">
                        <span class="text-xs text-muted-foreground">垂直偏移 (Y)</span>
                        <span class="text-xs font-mono">{{ shadow.vertical }}px</span>
                      </div>
                      <input
                        type="range"
                        v-model.number="shadow.vertical"
                        min="-100"
                        max="100"
                        class="w-full"
                      />
                    </div>
                    <div>
                      <div class="flex items-center justify-between mb-1">
                        <span class="text-xs text-muted-foreground">模糊半径</span>
                        <span class="text-xs font-mono">{{ shadow.blur }}px</span>
                      </div>
                      <input
                        type="range"
                        v-model.number="shadow.blur"
                        min="0"
                        max="100"
                        class="w-full"
                      />
                    </div>
                    <div>
                      <div class="flex items-center justify-between mb-1">
                        <span class="text-xs text-muted-foreground">扩散距离</span>
                        <span class="text-xs font-mono">{{ shadow.spread }}px</span>
                      </div>
                      <input
                        type="range"
                        v-model.number="shadow.spread"
                        min="-50"
                        max="50"
                        class="w-full"
                      />
                    </div>
                    <div>
                      <div class="flex items-center justify-between mb-1">
                        <span class="text-xs text-muted-foreground">颜色</span>
                      </div>
                      <input
                        type="color"
                        v-model="shadow.color"
                        class="w-full h-10 rounded cursor-pointer border border-border"
                      />
                    </div>
                    <div>
                      <div class="flex items-center justify-between mb-1">
                        <span class="text-xs text-muted-foreground">不透明度</span>
                        <span class="text-xs font-mono">{{ shadow.opacity }}</span>
                      </div>
                      <input
                        type="range"
                        v-model.number="shadow.opacity"
                        min="0"
                        max="1"
                        step="0.01"
                        class="w-full"
                      />
                    </div>
                  </div>
                  <div class="mt-3">
                    <label class="flex items-center gap-2 text-sm">
                      <input type="checkbox" v-model="shadow.inset" class="rounded" />
                      <span class="text-muted-foreground">内阴影</span>
                    </label>
                  </div>
                </div>
              </div>
            </div>
          </div>

          <div class="space-y-6">
            <div>
              <div class="flex items-center justify-between mb-4">
                <div class="flex items-center gap-2">
                  <div class="w-1 h-4 bg-purple-500 rounded-full"></div>
                  <label class="text-sm font-medium">生成的代码</label>
                </div>
                <button
                  @click="copyCode"
                  class="text-xs text-muted-foreground hover:text-foreground px-2 py-1 rounded hover:bg-muted transition-colors"
                >
                  <Copy class="w-3 h-3" />
                </button>
              </div>
              <pre class="bg-muted p-4 rounded-lg text-sm font-mono overflow-x-auto"><code>{{ generatedCode }}</code></pre>
            </div>

            <div class="p-3 bg-indigo-50/50 dark:bg-indigo-900/20 rounded-lg border border-indigo-100 dark:border-indigo-800">
              <div class="text-xs text-indigo-600 dark:text-indigo-400 font-medium mb-2">使用说明：</div>
              <ul class="text-xs text-gray-600 dark:text-gray-300 space-y-1 list-disc list-inside">
                <li>支持多层阴影叠加，点击"添加阴影"按钮增加新层</li>
                <li>调整水平/垂直偏移、模糊、扩散距离等参数</li>
                <li>设置阴影颜色和不透明度，可选择内阴影</li>
                <li>实时预览效果，一键复制生成的CSS代码</li>
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
