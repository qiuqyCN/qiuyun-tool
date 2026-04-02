<template>
  <div class="bg-gray-50 rounded-lg p-4 mt-4">
    <div class="flex items-center justify-between mb-3">
      <h4 class="text-sm font-medium text-gray-700">处理日志</h4>
      <Badge v-if="logs.length > 0" variant="secondary" class="text-xs">
        {{ logs.length }} 条
      </Badge>
    </div>
    <div class="max-h-60 overflow-y-auto space-y-2 bg-white rounded-md p-3 border border-gray-200">
      <div
        v-for="(log, index) in logs"
        :key="index"
        class="flex items-start gap-2 text-sm animate-in fade-in slide-in-from-left-2 duration-300"
        :style="{ animationDelay: `${index * 50}ms` }"
      >
        <Icon
          :name="getLogIcon(log.type)"
          class="w-4 h-4 mt-0.5 flex-shrink-0"
          :class="getLogColor(log.type)"
        />
        <div class="flex-1 min-w-0">
          <span :class="getLogColor(log.type)">{{ log.message }}</span>
        </div>
        <span class="text-gray-400 text-xs flex-shrink-0">{{ formatTime(log.time) }}</span>
      </div>
      <div v-if="logs.length === 0" class="text-center text-gray-400 text-sm py-4">
        等待开始...
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import type { ProcessLogEntry } from '~/types/tool'

interface Props {
  logs: ProcessLogEntry[]
}

const props = defineProps<Props>()

const getLogIcon = (type: string): string => {
  const icons: Record<string, string> = {
    INFO: 'lucide:info',
    WARN: 'lucide:alert-triangle',
    ERROR: 'lucide:x-circle',
    SUCCESS: 'lucide:check-circle'
  }
  return icons[type] || icons.INFO
}

const getLogColor = (type: string): string => {
  const colors: Record<string, string> = {
    INFO: 'text-blue-600',
    WARN: 'text-yellow-600',
    ERROR: 'text-red-600',
    SUCCESS: 'text-green-600'
  }
  return colors[type] || colors.INFO
}

const formatTime = (time: string): string => {
  if (!time) return ''
  const date = new Date(time)
  return date.toLocaleTimeString('zh-CN', {
    hour: '2-digit',
    minute: '2-digit',
    second: '2-digit'
  })
}
</script>
