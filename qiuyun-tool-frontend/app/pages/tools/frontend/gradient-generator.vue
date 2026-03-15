<script setup lang="ts">
import { ref, computed } from 'vue'
import { Copy, Check, Palette, RefreshCw, Plus, Trash2 } from 'lucide-vue-next'

interface ColorStop {
  id: number
  color: string
  position: number
}

const gradientType = ref<'linear' | 'radial'>('linear')
const angle = ref(180)
const colorStops = ref<ColorStop[]>([
  { id: 1, color: '#6366F1', position: 0 },
  { id: 2, color: '#EC4899', position: 100 }
])
let nextId = 3

const toast = ref({ show: false, message: '' })

const showToast = (message: string) => {
  toast.value.message = message
  toast.value.show = true
  setTimeout(() => toast.value.show = false, 2000)
}

const addColorStop = () => {
  colorStops.value.push({
    id: nextId++,
    color: '#FFFFFF',
    position: 50
  })
  sortColorStops()
}

const removeColorStop = (id: number) => {
  if (colorStops.value.length > 2) {
    colorStops.value = colorStops.value.filter(s => s.id !== id)
  }
}

const sortColorStops = () => {
  colorStops.value.sort((a, b) => a.position - b.position)
}

const generatedCode = computed(() => {
  const stops = colorStops.value.map(s => `${s.color} ${s.position}%`).join(', ')
  if (gradientType.value === 'linear') {
    return `background: linear-gradient(${angle.value}deg, ${stops});`
  } else {
    return `background: radial-gradient(circle, ${stops});`
  }
})

const backgroundStyle = computed(() => {
  const stops = colorStops.value.map(s => `${s.color} ${s.position}%`).join(', ')
  if (gradientType.value === 'linear') {
    return { background: `linear-gradient(${angle.value}deg, ${stops})` }
  } else {
    return { background: `radial-gradient(circle, ${stops})` }
  }
})

const copyCode = async () => {
  try {
    await navigator.clipboard.writeText(generatedCode.value)
    showToast('已复制代码')
  } catch {
    showToast('复制失败')
  }
}

const reset = () => {
  gradientType.value = 'linear'
  angle.value = 180
  colorStops.value = [
    { id: 1, color: '#6366F1', position: 0 },
    { id: 2, color: '#EC4899', position: 100 }
  ]
  nextId = 3
}
</script>

<template>
  <NuxtLayout name="tool" tool-code="gradient-generator">
    <div class="border border-border/40 rounded-xl overflow-hidden">
      <div class="p-4 border-b bg-muted/20">
        <div class="flex flex-wrap items-center gap-4">
          <div class="flex items-center gap-2">
            <Palette class="w-5 h-5 text-primary" />
            <span class="text-sm font-medium">渐变生成器</span>
          </div>

          <div class="flex items-center gap-2">
            <button
              @click="gradientType = 'linear'"
              class="px-3 py-1.5 rounded text-sm transition-colors"
              :class="gradientType === 'linear' ? 'bg-primary text-white' : 'hover:bg-muted'"
            >
              线性渐变
            </button>
            <button
              @click="gradientType = 'radial'"
              class="px-3 py-1.5 rounded text-sm transition-colors"
              :class="gradientType === 'radial' ? 'bg-primary text-white' : 'hover:bg-muted'"
            >
              径向渐变
            </button>
          </div>

          <div class="ml-auto flex items-center gap-2">
            <button
              @click="addColorStop"
              class="text-xs text-white bg-primary hover:bg-primary/90 px-3 py-1.5 rounded transition-colors flex items-center gap-1"
            >
              <Plus class="w-3 h-3" />
              添加色标
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
              <div
                class="w-full h-64 rounded-xl flex items-center justify-center"
                :style="backgroundStyle"
              >
                <div class="bg-white/80 dark:bg-black/50 backdrop-blur-sm px-6 py-3 rounded-lg">
                  <span class="text-2xl font-bold bg-linear-to-r from-indigo-600 to-pink-600 bg-clip-text text-transparent">渐变效果</span>
                </div>
              </div>
            </div>

            <div v-if="gradientType === 'linear'" class="p-4 border border-border rounded-lg">
              <div class="flex items-center justify-between mb-2">
                <span class="text-xs font-medium">角度</span>
                <span class="text-xs font-mono">{{ angle }}°</span>
              </div>
              <input
                type="range"
                v-model.number="angle"
                min="0"
                max="360"
                class="w-full"
              />
            </div>

            <div>
              <div class="flex items-center gap-2 mb-4">
                <div class="w-1 h-4 bg-primary rounded-full"></div>
                <label class="text-sm font-medium">色标</label>
              </div>
              <div class="space-y-4">
                <div
                  v-for="stop in colorStops"
                  :key="stop.id"
                  class="p-4 border border-border rounded-lg bg-muted/10"
                >
                  <div class="flex items-center justify-between mb-3">
                    <span class="text-sm font-medium">色标 #{{ colorStops.indexOf(stop) + 1 }}</span>
                    <button
                      v-if="colorStops.length > 2"
                      @click="removeColorStop(stop.id)"
                      class="p-1 rounded hover:bg-muted text-muted-foreground hover:text-red-500 transition-colors"
                    >
                      <Trash2 class="w-4 h-4" />
                    </button>
                  </div>
                  <div class="grid grid-cols-2 gap-4">
                    <div>
                      <div class="flex items-center justify-between mb-1">
                        <span class="text-xs text-muted-foreground">颜色</span>
                      </div>
                      <input
                        type="color"
                        v-model="stop.color"
                        class="w-full h-10 rounded cursor-pointer border border-border"
                      />
                    </div>
                    <div>
                      <div class="flex items-center justify-between mb-1">
                        <span class="text-xs text-muted-foreground">位置</span>
                        <span class="text-xs font-mono">{{ stop.position }}%</span>
                      </div>
                      <input
                        type="range"
                        v-model.number="stop.position"
                        @input="sortColorStops"
                        min="0"
                        max="100"
                        class="w-full"
                      />
                    </div>
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
                <li>选择线性渐变或径向渐变类型</li>
                <li>线性渐变支持调整角度（0-360度）</li>
                <li>添加多个色标，调整颜色和位置</li>
                <li>实时预览效果，一键复制CSS代码</li>
              </ul>
            </div>

            <div class="p-3 bg-blue-50/50 dark:bg-blue-900/20 rounded-lg border border-blue-100 dark:border-blue-800">
              <div class="text-xs text-blue-600 dark:text-blue-400 font-medium mb-2">CSS参数说明：</div>
              <div class="space-y-2 text-xs text-gray-600 dark:text-gray-300">
                <div>
                  <strong class="text-blue-600 dark:text-blue-400">linear-gradient(angle, color-stop1, color-stop2, ...)</strong>
                  <div class="ml-2 mt-1">
                    <div><strong>angle</strong>：渐变方向角度，0deg表示从下到上，90deg表示从左到右，180deg表示从上到下，默认为180deg</div>
                    <div><strong>color-stop</strong>：色标，格式为"color position"，如"#6366F1 0%"表示在0%位置开始使用靛蓝色</div>
                  </div>
                </div>
                <div>
                  <strong class="text-blue-600 dark:text-blue-400">radial-gradient(shape, color-stop1, color-stop2, ...)</strong>
                  <div class="ml-2 mt-1">
                    <div><strong>shape</strong>：渐变形状，circle表示圆形，ellipse表示椭圆（默认）</div>
                    <div><strong>color-stop</strong>：色标，格式为"color position"，从中心向外渐变</div>
                  </div>
                </div>
                <div>
                  <strong class="text-blue-600 dark:text-blue-400">色标位置</strong>：0%表示渐变起点，100%表示渐变终点，中间的值表示颜色过渡的位置
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
