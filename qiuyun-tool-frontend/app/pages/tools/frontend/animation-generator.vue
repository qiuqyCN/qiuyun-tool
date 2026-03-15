<script setup lang="ts">
import { ref, computed } from 'vue'
import { Copy, Check, RefreshCw, Play, Pause } from 'lucide-vue-next'

interface AnimationType {
  name: string
  keyframes: string
  displayName: string
}

const animationTypes: AnimationType[] = [
  {
    name: 'fadeIn',
    displayName: '淡入',
    keyframes: `@keyframes fadeIn {
  from { opacity: 0; }
  to { opacity: 1; }
}`
  },
  {
    name: 'fadeOut',
    displayName: '淡出',
    keyframes: `@keyframes fadeOut {
  from { opacity: 1; }
  to { opacity: 0; }
}`
  },
  {
    name: 'slideInLeft',
    displayName: '左滑入',
    keyframes: `@keyframes slideInLeft {
  from {
    opacity: 0;
    transform: translateX(-100%);
  }
  to {
    opacity: 1;
    transform: translateX(0);
  }
}`
  },
  {
    name: 'slideInRight',
    displayName: '右滑入',
    keyframes: `@keyframes slideInRight {
  from {
    opacity: 0;
    transform: translateX(100%);
  }
  to {
    opacity: 1;
    transform: translateX(0);
  }
}`
  },
  {
    name: 'slideInTop',
    displayName: '上滑入',
    keyframes: `@keyframes slideInTop {
  from {
    opacity: 0;
    transform: translateY(-100%);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}`
  },
  {
    name: 'slideInBottom',
    displayName: '下滑入',
    keyframes: `@keyframes slideInBottom {
  from {
    opacity: 0;
    transform: translateY(100%);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}`
  },
  {
    name: 'bounce',
    displayName: '弹跳',
    keyframes: `@keyframes bounce {
  0%, 20%, 50%, 80%, 100% {
    transform: translateY(0);
  }
  40% {
    transform: translateY(-30px);
  }
  60% {
    transform: translateY(-15px);
  }
}`
  },
  {
    name: 'pulse',
    displayName: '脉冲',
    keyframes: `@keyframes pulse {
  0%, 100% {
    transform: scale(1);
  }
  50% {
    transform: scale(1.05);
  }
}`
  },
  {
    name: 'shake',
    displayName: '摇晃',
    keyframes: `@keyframes shake {
  0%, 100% { transform: translateX(0); }
  10%, 30%, 50%, 70%, 90% { transform: translateX(-10px); }
  20%, 40%, 60%, 80% { transform: translateX(10px); }
}`
  },
  {
    name: 'rotate',
    displayName: '旋转',
    keyframes: `@keyframes rotate {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}`
  },
  {
    name: 'flipX',
    displayName: 'X轴翻转',
    keyframes: `@keyframes flipX {
  from { transform: perspective(400px) rotateY(0deg); }
  to { transform: perspective(400px) rotateY(360deg); }
}`
  },
  {
    name: 'flipY',
    displayName: 'Y轴翻转',
    keyframes: `@keyframes flipY {
  from { transform: perspective(400px) rotateX(0deg); }
  to { transform: perspective(400px) rotateX(360deg); }
}`
  },
  {
    name: 'zoomIn',
    displayName: '放大',
    keyframes: `@keyframes zoomIn {
  from {
    opacity: 0;
    transform: scale(0.3);
  }
  to {
    opacity: 1;
    transform: scale(1);
  }
}`
  },
  {
    name: 'zoomOut',
    displayName: '缩小',
    keyframes: `@keyframes zoomOut {
  from {
    opacity: 1;
    transform: scale(1);
  }
  to {
    opacity: 0;
    transform: scale(0.3);
  }
}`
  },
  {
    name: 'flash',
    displayName: '闪烁',
    keyframes: `@keyframes flash {
  0%, 50%, 100% { opacity: 1; }
  25%, 75% { opacity: 0; }
}`
  },
  {
    name: 'rubberBand',
    displayName: '橡皮筋',
    keyframes: `@keyframes rubberBand {
  0%, 100% { transform: scale3d(1, 1, 1); }
  30% { transform: scale3d(1.25, 0.75, 1); }
  40% { transform: scale3d(0.75, 1.25, 1); }
  50% { transform: scale3d(1.15, 0.85, 1); }
  65% { transform: scale3d(0.95, 1.05, 1); }
  75% { transform: scale3d(1.05, 0.95, 1); }
}`
  },
  {
    name: 'swing',
    displayName: '摇摆',
    keyframes: `@keyframes swing {
  20%, 100% { transform: rotate(0deg); }
  40% { transform: rotate(15deg); }
  60% { transform: rotate(-10deg); }
  80% { transform: rotate(5deg); }
}`
  },
  {
    name: 'tada',
    displayName: '欢呼',
    keyframes: `@keyframes tada {
  0%, 100% { transform: scale3d(1, 1, 1); }
  10%, 20% { transform: scale3d(0.9, 0.9, 0.9) rotate3d(0, 0, 1, -3deg); }
  30%, 50%, 70%, 90% { transform: scale3d(1.1, 1.1, 1.1) rotate3d(0, 0, 1, 3deg); }
  40%, 60%, 80% { transform: scale3d(1.1, 1.1, 1.1) rotate3d(0, 0, 1, -3deg); }
}`
  },
  {
    name: 'jello',
    displayName: '果冻',
    keyframes: `@keyframes jello {
  0%, 100% { transform: none; }
  11.1%, 33.3% { transform: skewX(-12.5deg) skewY(-12.5deg); }
  22.2%, 44.4% { transform: skewX(6.25deg) skewY(6.25deg); }
  55.5% { transform: skewX(-3.125deg) skewY(-3.125deg); }
  66.6% { transform: skewX(1.5625deg) skewY(1.5625deg); }
  77.7% { transform: skewX(-0.78125deg) skewY(-0.78125deg); }
}`
  },
  {
    name: 'wobble',
    displayName: '摇晃',
    keyframes: `@keyframes wobble {
  0%, 100% { transform: translateX(0) rotate(0deg); }
  15% { transform: translateX(-25%) rotate(-5deg); }
  30% { transform: translateX(20%) rotate(3deg); }
  45% { transform: translateX(-15%) rotate(-3deg); }
  60% { transform: translateX(10%) rotate(2deg); }
  75% { transform: translateX(-5%) rotate(-1deg); }
}`
  }
]

const easingOptions = [
  { value: 'ease', label: 'ease' },
  { value: 'ease-in', label: 'ease-in' },
  { value: 'ease-out', label: 'ease-out' },
  { value: 'ease-in-out', label: 'ease-in-out' },
  { value: 'linear', label: 'linear' },
  { value: 'cubic-bezier(0.68, -0.55, 0.265, 1.55)', label: '弹性' },
  { value: 'cubic-bezier(0.175, 0.885, 0.32, 1.275)', label: '回弹' }
]

const selectedAnimation = ref(animationTypes[0])
const duration = ref(1)
const delay = ref(0)
const easing = ref('ease')
const iterations = ref(1)
const direction = ref<'normal' | 'reverse' | 'alternate' | 'alternate-reverse'>('normal')
const fillMode = ref<'none' | 'forwards' | 'backwards' | 'both'>('both')
const isPlaying = ref(true)
const forceUpdate = ref(0)

const toast = ref({ show: false, message: '' })

const showToast = (message: string) => {
  toast.value.message = message
  toast.value.show = true
  setTimeout(() => toast.value.show = false, 2000)
}

const animationStyle = computed(() => {
  if (!isPlaying.value) {
    return {}
  }
  return {
    animationName: selectedAnimation.value.name,
    animationDuration: `${duration.value}s`,
    animationDelay: `${delay.value}s`,
    animationTimingFunction: easing.value,
    animationIterationCount: iterations.value === -1 ? 'infinite' : iterations.value,
    animationDirection: direction.value,
    animationFillMode: fillMode.value
  }
})

const generatedCode = computed(() => {
  const animation = selectedAnimation.value
  const style = `.animated-element {
  animation-name: ${animation.name};
  animation-duration: ${duration.value}s;
  animation-delay: ${delay.value}s;
  animation-timing-function: ${easing.value};
  animation-iteration-count: ${iterations.value === -1 ? 'infinite' : iterations.value};
  animation-direction: ${direction.value};
  animation-fill-mode: ${fillMode.value};
}`
  return `${animation.keyframes}\n\n${style}`
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
  selectedAnimation.value = animationTypes[0]
  duration.value = 1
  delay.value = 0
  easing.value = 'ease'
  iterations.value = 1
  direction.value = 'normal'
  fillMode.value = 'both'
  isPlaying.value = true
  forceUpdate.value++
}

const togglePlay = () => {
  isPlaying.value = !isPlaying.value
  if (isPlaying.value) {
    forceUpdate.value++
  }
}
</script>

<template>
  <NuxtLayout name="tool" tool-code="animation-generator">
    <div class="border border-border/40 rounded-xl overflow-hidden">
      <div class="p-4 border-b bg-muted/20">
        <div class="flex flex-wrap items-center gap-4">
          <div class="flex items-center gap-2">
            <Play class="w-5 h-5 text-primary" />
            <span class="text-sm font-medium">CSS动画生成器</span>
          </div>

          <div class="ml-auto flex items-center gap-2">
            <button
              @click="togglePlay"
              class="text-xs text-muted-foreground hover:text-foreground px-3 py-1.5 rounded hover:bg-muted transition-colors flex items-center gap-1"
            >
              <component :is="isPlaying ? Pause : Play" class="w-3 h-3" />
              {{ isPlaying ? '暂停' : '播放' }}
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
              <div class="w-full h-80 rounded-xl flex items-center justify-center bg-muted/30 overflow-hidden">
                <div
                  :key="forceUpdate"
                  class="w-24 h-24 bg-primary rounded-xl flex items-center justify-center"
                  :style="animationStyle"
                >
                  <span class="text-white text-2xl font-bold">动</span>
                </div>
              </div>
            </div>

            <div>
              <div class="flex items-center gap-2 mb-4">
                <div class="w-1 h-4 bg-primary rounded-full"></div>
                <label class="text-sm font-medium">选择动画</label>
              </div>
              <div class="grid grid-cols-4 sm:grid-cols-5 gap-2">
                <button
                  v-for="anim in animationTypes"
                  :key="anim.name"
                  @click="selectedAnimation = anim"
                  class="px-3 py-2 text-xs rounded-lg transition-colors text-center"
                  :class="selectedAnimation.name === anim.name ? 'bg-primary text-white' : 'bg-muted hover:bg-muted/80'"
                >
                  {{ anim.displayName }}
                </button>
              </div>
            </div>

            <div class="grid grid-cols-1 sm:grid-cols-2 gap-4">
              <div class="p-4 border border-border rounded-lg bg-muted/10">
                <div class="flex items-center gap-2 mb-4">
                  <div class="w-1 h-4 bg-purple-500 rounded-full"></div>
                  <label class="text-sm font-medium">时间设置</label>
                </div>
                
                <div class="space-y-4">
                  <div>
                    <div class="flex items-center justify-between mb-1">
                      <span class="text-xs text-muted-foreground">持续时间</span>
                      <span class="text-xs font-mono">{{ duration }}s</span>
                    </div>
                    <input
                      type="range"
                      v-model.number="duration"
                      min="0.1"
                      max="10"
                      step="0.1"
                      class="w-full"
                    />
                  </div>

                  <div>
                    <div class="flex items-center justify-between mb-1">
                      <span class="text-xs text-muted-foreground">延迟</span>
                      <span class="text-xs font-mono">{{ delay }}s</span>
                    </div>
                    <input
                      type="range"
                      v-model.number="delay"
                      min="0"
                      max="5"
                      step="0.1"
                      class="w-full"
                    />
                  </div>

                  <div>
                    <div class="flex items-center justify-between mb-1">
                      <span class="text-xs text-muted-foreground">迭代次数</span>
                      <span class="text-xs font-mono">{{ iterations === -1 ? '无限' : iterations }}</span>
                    </div>
                    <input
                      type="range"
                      v-model.number="iterations"
                      min="-1"
                      max="20"
                      class="w-full"
                    />
                    <div class="flex justify-between text-xs text-muted-foreground mt-1">
                      <span>-1</span>
                      <span>无限</span>
                      <span>20</span>
                    </div>
                  </div>
                </div>
              </div>

              <div class="p-4 border border-border rounded-lg bg-muted/10">
                <div class="flex items-center gap-2 mb-4">
                  <div class="w-1 h-4 bg-blue-500 rounded-full"></div>
                  <label class="text-sm font-medium">动画属性</label>
                </div>
                
                <div class="space-y-4">
                  <div>
                    <div class="flex items-center justify-between mb-1">
                      <span class="text-xs text-muted-foreground">缓动函数</span>
                    </div>
                    <select
                      v-model="easing"
                      class="w-full px-3 py-2 border border-border rounded-lg bg-background text-sm"
                    >
                      <option
                        v-for="opt in easingOptions"
                        :key="opt.value"
                        :value="opt.value"
                      >
                        {{ opt.label }}
                      </option>
                    </select>
                  </div>

                  <div>
                    <div class="flex items-center justify-between mb-1">
                      <span class="text-xs text-muted-foreground">播放方向</span>
                    </div>
                    <select
                      v-model="direction"
                      class="w-full px-3 py-2 border border-border rounded-lg bg-background text-sm"
                    >
                      <option value="normal">正常</option>
                      <option value="reverse">反向</option>
                      <option value="alternate">交替</option>
                      <option value="alternate-reverse">反向交替</option>
                    </select>
                  </div>

                  <div>
                    <div class="flex items-center justify-between mb-1">
                      <span class="text-xs text-muted-foreground">填充模式</span>
                    </div>
                    <select
                      v-model="fillMode"
                      class="w-full px-3 py-2 border border-border rounded-lg bg-background text-sm"
                    >
                      <option value="none">无</option>
                      <option value="forwards">保持结束状态</option>
                      <option value="backwards">保持开始状态</option>
                      <option value="both">两者都保持</option>
                    </select>
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
              <pre class="bg-muted p-4 rounded-lg text-xs font-mono overflow-x-auto max-h-96"><code>{{ generatedCode }}</code></pre>
            </div>

            <div class="p-3 bg-indigo-50/50 dark:bg-indigo-900/20 rounded-lg border border-indigo-100 dark:border-indigo-800">
              <div class="text-xs text-indigo-600 dark:text-indigo-400 font-medium mb-2">使用说明：</div>
              <ul class="text-xs text-gray-600 dark:text-gray-300 space-y-1 list-disc list-inside">
                <li>从预设动画列表中选择喜欢的效果</li>
                <li>调整持续时间、延迟、迭代次数等参数</li>
                <li>选择合适的缓动函数和播放方向</li>
                <li>实时预览效果，一键复制完整CSS代码</li>
              </ul>
            </div>

            <div class="p-3 bg-blue-50/50 dark:bg-blue-900/20 rounded-lg border border-blue-100 dark:border-blue-800">
              <div class="text-xs text-blue-600 dark:text-blue-400 font-medium mb-2">CSS动画属性说明：</div>
              <div class="space-y-2 text-xs text-gray-600 dark:text-gray-300">
                <div>
                  <strong class="text-blue-600 dark:text-blue-400">animation-duration</strong>：动画完成一个周期所需时间，单位秒(s)或毫秒(ms)
                </div>
                <div>
                  <strong class="text-blue-600 dark:text-blue-400">animation-delay</strong>：动画何时开始，正值延迟开始，负值立即开始但已播放一段时间
                </div>
                <div>
                  <strong class="text-blue-600 dark:text-blue-400">animation-timing-function</strong>：动画节奏，ease（默认慢快慢）、linear（匀速）、ease-in（慢入）、ease-out（慢出）等
                </div>
                <div>
                  <strong class="text-blue-600 dark:text-blue-400">animation-iteration-count</strong>：动画播放次数，infinite表示无限循环
                </div>
                <div>
                  <strong class="text-blue-600 dark:text-blue-400">animation-direction</strong>：播放方向，normal（正常）、reverse（反向）、alternate（交替）
                </div>
                <div>
                  <strong class="text-blue-600 dark:text-blue-400">animation-fill-mode</strong>：动画执行前后的样式，forwards（保持结束状态）、backwards（保持开始状态）
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
