<script setup lang="ts">
import { ref, computed, onUnmounted } from 'vue'
import { Timer, Play, Pause, RotateCcw, Bell, Plus, Trash2, Clock } from 'lucide-vue-next'
import ToolInput from '@/components/ui/ToolInput.vue'
import ToolCard from '@/components/ui/ToolCard.vue'

useHead({
  title: '倒计时工具 - 秋云工具',
  meta: [
    { name: 'description', content: '多功能倒计时器，支持自定义时间、预设时间、多个倒计时管理' }
  ]
})

interface CountdownItem {
  id: number
  name: string
  totalTime: number // 总时长（毫秒）
  remainingTime: number // 剩余时间（毫秒）
  isRunning: boolean
  isExpired: boolean
  lastUpdateTime: number // 上次更新时间
}

enum TimerMode {
  COUNTDOWN = 'countdown',
  STOPWATCH = 'stopwatch'
}

const currentMode = ref<TimerMode>(TimerMode.COUNTDOWN)

// 倒计时相关
const hours = ref<number>(0)
const minutes = ref<number>(0)
const seconds = ref<number>(0)
const timerName = ref<string>('')
const activeCountdowns = ref<CountdownItem[]>([])

// 秒表相关
const stopwatchTime = ref<number>(0)
const isStopwatchRunning = ref<boolean>(false)
const stopwatchLaps = ref<number[]>([])

let intervalId: ReturnType<typeof setInterval> | null = null
let stopwatchIntervalId: ReturnType<typeof setInterval> | null = null

// 预设时间
const presets = [
  { name: '1分钟', totalSeconds: 60 },
  { name: '3分钟', totalSeconds: 180 },
  { name: '5分钟', totalSeconds: 300 },
  { name: '10分钟', totalSeconds: 600 },
  { name: '15分钟', totalSeconds: 900 },
  { name: '25分钟', totalSeconds: 1500 },
  { name: '30分钟', totalSeconds: 1800 },
  { name: '45分钟', totalSeconds: 2700 },
  { name: '1小时', totalSeconds: 3600 }
]

const applyPreset = (totalSeconds: number) => {
  hours.value = Math.floor(totalSeconds / 3600)
  minutes.value = Math.floor((totalSeconds % 3600) / 60)
  seconds.value = totalSeconds % 60
}

const addCountdown = () => {
  const totalSeconds = hours.value * 3600 + minutes.value * 60 + seconds.value
  if (totalSeconds <= 0) return

  const totalTime = totalSeconds * 1000

  const newCountdown: CountdownItem = {
    id: Date.now(),
    name: timerName.value || `倒计时 ${activeCountdowns.value.length + 1}`,
    totalTime,
    remainingTime: totalTime,
    isRunning: true,
    isExpired: false,
    lastUpdateTime: Date.now()
  }

  activeCountdowns.value.push(newCountdown)

  // 清空输入
  timerName.value = ''
  hours.value = 0
  minutes.value = 0
  seconds.value = 0

  if (!intervalId) {
    startGlobalTimer()
  }
}

const startGlobalTimer = () => {
  intervalId = setInterval(() => {
    const now = Date.now()
    activeCountdowns.value.forEach(countdown => {
      if (countdown.isRunning && !countdown.isExpired) {
        // 计算经过的时间
        const elapsed = now - countdown.lastUpdateTime
        countdown.remainingTime = Math.max(0, countdown.remainingTime - elapsed)
        countdown.lastUpdateTime = now

        // 检查是否到期
        if (countdown.remainingTime <= 0) {
          countdown.isExpired = true
          countdown.isRunning = false
          countdown.remainingTime = 0
          // 可以在这里添加提示音或通知
        }
      }
    })
  }, 100)
}

const toggleCountdown = (id: number) => {
  const countdown = activeCountdowns.value.find(c => c.id === id)
  if (countdown && !countdown.isExpired) {
    if (countdown.isRunning) {
      // 暂停
      countdown.isRunning = false
    } else {
      // 恢复
      countdown.isRunning = true
      countdown.lastUpdateTime = Date.now()
    }
  }
}

const removeCountdown = (id: number) => {
  activeCountdowns.value = activeCountdowns.value.filter(c => c.id !== id)
  if (activeCountdowns.value.length === 0 && intervalId) {
    clearInterval(intervalId)
    intervalId = null
  }
}

const getRemainingTime = (countdown: CountdownItem): { hours: number; minutes: number; seconds: number; percentage: number } => {
  const totalMs = countdown.remainingTime

  const h = Math.floor(totalMs / 3600000)
  const m = Math.floor((totalMs % 3600000) / 60000)
  const s = Math.floor((totalMs % 60000) / 1000)

  // 计算进度百分比
  const percentage = countdown.totalTime > 0
    ? Math.max(0, Math.min(100, (countdown.remainingTime / countdown.totalTime) * 100))
    : 0

  return { hours: h, minutes: m, seconds: s, percentage }
}

const formatTime = (h: number, m: number, s: number): string => {
  return `${String(h).padStart(2, '0')}:${String(m).padStart(2, '0')}:${String(s).padStart(2, '0')}`
}

// 秒表功能
const startStopwatch = () => {
  if (!isStopwatchRunning.value) {
    isStopwatchRunning.value = true
    const startTime = Date.now() - stopwatchTime.value
    stopwatchIntervalId = setInterval(() => {
      stopwatchTime.value = Date.now() - startTime
    }, 10)
  }
}

const pauseStopwatch = () => {
  if (isStopwatchRunning.value) {
    isStopwatchRunning.value = false
    if (stopwatchIntervalId) {
      clearInterval(stopwatchIntervalId)
      stopwatchIntervalId = null
    }
  }
}

const resetStopwatch = () => {
  pauseStopwatch()
  stopwatchTime.value = 0
  stopwatchLaps.value = []
}

const recordLap = () => {
  if (isStopwatchRunning.value) {
    stopwatchLaps.value.unshift(stopwatchTime.value)
  }
}

const formatStopwatchTime = (ms: number): string => {
  const minutes = Math.floor(ms / 60000)
  const seconds = Math.floor((ms % 60000) / 1000)
  const centiseconds = Math.floor((ms % 1000) / 10)
  return `${String(minutes).padStart(2, '0')}:${String(seconds).padStart(2, '0')}.${String(centiseconds).padStart(2, '0')}`
}

onUnmounted(() => {
  if (intervalId) clearInterval(intervalId)
  if (stopwatchIntervalId) clearInterval(stopwatchIntervalId)
})
</script>

<template>
  <NuxtLayout name="tool" toolCode="countdown-timer">
    <div class="max-w-4xl mx-auto space-y-6">
      <!-- 模式选择 -->
      <ToolCard>
        <h2 class="text-lg font-semibold text-gray-900 mb-4">选择模式</h2>
        <div class="grid grid-cols-2 gap-3">
          <button
            @click="currentMode = TimerMode.COUNTDOWN"
            class="flex items-center justify-center gap-2 p-4 rounded-xl transition-all duration-200"
            :class="currentMode === TimerMode.COUNTDOWN
              ? 'bg-linear-to-br from-rose-500 to-pink-600 text-white shadow-lg shadow-rose-200'
              : 'bg-gray-50 text-gray-600 hover:bg-rose-50 hover:text-rose-600'"
          >
            <Timer class="w-5 h-5" />
            <span class="font-medium">倒计时</span>
          </button>
          <button
            @click="currentMode = TimerMode.STOPWATCH"
            class="flex items-center justify-center gap-2 p-4 rounded-xl transition-all duration-200"
            :class="currentMode === TimerMode.STOPWATCH
              ? 'bg-linear-to-br from-rose-500 to-pink-600 text-white shadow-lg shadow-rose-200'
              : 'bg-gray-50 text-gray-600 hover:bg-rose-50 hover:text-rose-600'"
          >
            <Clock class="w-5 h-5" />
            <span class="font-medium">秒表</span>
          </button>
        </div>
      </ToolCard>

      <!-- 倒计时模式 -->
      <template v-if="currentMode === TimerMode.COUNTDOWN">
        <!-- 添加倒计时 -->
        <ToolCard>
          <div class="flex items-center gap-2 mb-6">
            <Timer class="w-6 h-6 text-rose-500" />
            <h2 class="text-lg font-semibold text-gray-900">新建倒计时</h2>
          </div>

          <!-- 预设时间 -->
          <div class="mb-6">
            <label class="block text-sm font-medium text-gray-700 mb-2">快速选择</label>
            <div class="flex flex-wrap gap-2">
              <button
                v-for="preset in presets"
                :key="preset.name"
                @click="applyPreset(preset.totalSeconds)"
                class="px-3 py-1.5 text-sm bg-gray-100 text-gray-700 rounded-lg hover:bg-rose-100 hover:text-rose-700 transition-colors"
              >
                {{ preset.name }}
              </button>
            </div>
          </div>

          <!-- 自定义时间 -->
          <div class="grid grid-cols-3 gap-4 mb-4">
            <div>
              <label class="block text-xs font-medium text-gray-600 mb-1">小时</label>
              <ToolInput v-model="hours" type="number" placeholder="0" min="0" max="99" />
            </div>
            <div>
              <label class="block text-xs font-medium text-gray-600 mb-1">分钟</label>
              <ToolInput v-model="minutes" type="number" placeholder="0" min="0" max="59" />
            </div>
            <div>
              <label class="block text-xs font-medium text-gray-600 mb-1">秒</label>
              <ToolInput v-model="seconds" type="number" placeholder="0" min="0" max="59" />
            </div>
          </div>

          <!-- 名称输入 -->
          <div class="mb-4">
            <label class="block text-xs font-medium text-gray-600 mb-1">倒计时名称 (可选)</label>
            <ToolInput v-model="timerName" type="text" placeholder="例如：煮鸡蛋、休息时间..." />
          </div>

          <!-- 添加按钮 -->
          <button
            @click="addCountdown"
            :disabled="hours === 0 && minutes === 0 && seconds === 0"
            class="w-full py-3 bg-linear-to-r from-rose-500 to-pink-600 text-white rounded-xl font-medium hover:from-rose-600 hover:to-pink-700 disabled:opacity-50 disabled:cursor-not-allowed transition-all flex items-center justify-center gap-2"
          >
            <Plus class="w-5 h-5" />
            开始倒计时
          </button>
        </ToolCard>

        <!-- 活动倒计时列表 -->
        <ToolCard v-if="activeCountdowns.length > 0">
          <h3 class="text-base font-semibold text-gray-900 mb-4 flex items-center gap-2">
            <Bell class="w-5 h-5 text-rose-500" />
            进行中的倒计时
          </h3>

          <div class="space-y-4">
            <div
              v-for="countdown in activeCountdowns"
              :key="countdown.id"
              class="p-4 bg-gray-50 rounded-xl"
              :class="{ 'bg-red-50': countdown.isExpired }"
            >
              <div class="flex items-center justify-between mb-3">
                <span class="font-medium text-gray-900">{{ countdown.name }}</span>
                <div class="flex items-center gap-2">
                  <button
                    v-if="!countdown.isExpired"
                    @click="toggleCountdown(countdown.id)"
                    class="p-2 rounded-lg bg-white text-gray-600 hover:text-rose-600 transition-colors"
                  >
                    <Play v-if="!countdown.isRunning" class="w-4 h-4" />
                    <Pause v-else class="w-4 h-4" />
                  </button>
                  <button
                    @click="removeCountdown(countdown.id)"
                    class="p-2 rounded-lg bg-white text-gray-600 hover:text-red-600 transition-colors"
                  >
                    <Trash2 class="w-4 h-4" />
                  </button>
                </div>
              </div>

              <div class="text-center py-4">
                <div
                  class="text-4xl font-mono font-bold"
                  :class="countdown.isExpired ? 'text-red-600' : 'text-gray-900'"
                >
                  <template v-if="!countdown.isExpired">
                    {{ formatTime(
                      getRemainingTime(countdown).hours,
                      getRemainingTime(countdown).minutes,
                      getRemainingTime(countdown).seconds
                    ) }}
                  </template>
                  <template v-else>
                    时间到!
                  </template>
                </div>
              </div>

              <!-- 进度条 -->
              <div v-if="!countdown.isExpired" class="h-2 bg-gray-200 rounded-full overflow-hidden">
                <div
                  class="h-full bg-linear-to-r from-rose-400 to-pink-500 rounded-full transition-all"
                  :style="{ width: getRemainingTime(countdown).percentage + '%' }"
                ></div>
              </div>
            </div>
          </div>
        </ToolCard>
      </template>

      <!-- 秒表模式 -->
      <template v-else>
        <ToolCard>
          <div class="flex items-center gap-2 mb-6">
            <Clock class="w-6 h-6 text-rose-500" />
            <h2 class="text-lg font-semibold text-gray-900">秒表</h2>
          </div>

          <!-- 时间显示 -->
          <div class="text-center py-8">
            <div class="text-6xl font-mono font-bold text-gray-900">
              {{ formatStopwatchTime(stopwatchTime) }}
            </div>
          </div>

          <!-- 控制按钮 -->
          <div class="flex justify-center gap-4 mb-6">
            <button
              v-if="!isStopwatchRunning"
              @click="startStopwatch"
              class="px-6 py-3 bg-linear-to-r from-rose-500 to-pink-600 text-white rounded-xl font-medium hover:from-rose-600 hover:to-pink-700 transition-all flex items-center gap-2"
            >
              <Play class="w-5 h-5" />
              开始
            </button>
            <button
              v-else
              @click="pauseStopwatch"
              class="px-6 py-3 bg-linear-to-r from-amber-500 to-orange-600 text-white rounded-xl font-medium hover:from-amber-600 hover:to-orange-700 transition-all flex items-center gap-2"
            >
              <Pause class="w-5 h-5" />
              暂停
            </button>
            <button
              @click="recordLap"
              :disabled="!isStopwatchRunning"
              class="px-6 py-3 bg-gray-100 text-gray-700 rounded-xl font-medium hover:bg-gray-200 disabled:opacity-50 disabled:cursor-not-allowed transition-all"
            >
              计次
            </button>
            <button
              @click="resetStopwatch"
              class="px-6 py-3 bg-gray-100 text-gray-700 rounded-xl font-medium hover:bg-gray-200 transition-all flex items-center gap-2"
            >
              <RotateCcw class="w-5 h-5" />
              重置
            </button>
          </div>

          <!-- 计次记录 -->
          <div v-if="stopwatchLaps.length > 0" class="border-t pt-4">
            <h3 class="text-sm font-medium text-gray-700 mb-3">计次记录</h3>
            <div class="space-y-2 max-h-48 overflow-y-auto">
              <div
                v-for="(lap, index) in stopwatchLaps"
                :key="index"
                class="flex items-center justify-between p-3 bg-gray-50 rounded-lg"
              >
                <span class="text-sm text-gray-500">计次 {{ stopwatchLaps.length - index }}</span>
                <span class="font-mono font-medium text-gray-900">{{ formatStopwatchTime(lap) }}</span>
              </div>
            </div>
          </div>
        </ToolCard>
      </template>
    </div>
  </NuxtLayout>
</template>
