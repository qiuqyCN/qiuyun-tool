<template>
  <div class="bg-white rounded-xl shadow-sm border border-gray-200 overflow-hidden">
    <!-- 工具头部 -->
    <div class="px-6 py-4 border-b border-gray-200 bg-gray-50">
      <div class="flex items-center justify-between">
        <div class="flex items-center gap-3">
          <div
            class="w-12 h-12 rounded-xl flex items-center justify-center"
            :style="{ backgroundColor: tool?.iconBgColor || '#EEF2FF' }"
          >
            <Icon :name="tool?.icon || 'lucide:tool'" class="w-6 h-6" :style="{ color: tool?.iconColor || '#4F46E5' }" />
          </div>
          <div>
            <h2 class="text-xl font-bold text-gray-900">{{ tool?.name }}</h2>
            <p class="text-sm text-gray-500">{{ tool?.description }}</p>
          </div>
        </div>
        <Badge v-if="tool?.priceMode === 'vip'" variant="secondary" class="bg-linear-to-r from-amber-400 to-orange-500 text-white border-0">
          <Icon name="lucide:crown" class="w-3 h-3 mr-1" />
          VIP
        </Badge>
        <Badge v-else variant="secondary" class="bg-green-100 text-green-700 border-0">
          <Icon name="lucide:check-circle" class="w-3 h-3 mr-1" />
          免费
        </Badge>
      </div>
    </div>

    <!-- 参数输入区域 -->
    <div class="p-6">
      <slot name="params" :params="params" :updateParams="updateParams" />

      <!-- 文件上传区域（如果需要） -->
      <div v-if="tool?.needFile" class="mt-6">
        <Label class="text-base font-medium">上传文件</Label>
        <div
          class="mt-2 border-2 border-dashed border-gray-300 rounded-xl p-8 text-center hover:border-indigo-500 transition-colors cursor-pointer"
          :class="{ 'border-indigo-500 bg-indigo-50': isDragging }"
          @dragover.prevent="isDragging = true"
          @dragleave.prevent="isDragging = false"
          @drop.prevent="handleDrop"
          @click="triggerFileInput"
        >
          <input
            ref="fileInput"
            type="file"
            class="hidden"
            :accept="tool.acceptFileTypes"
            @change="handleFileSelect"
          />
          <div v-if="!file">
            <Icon name="lucide:upload-cloud" class="w-12 h-12 text-gray-400 mx-auto mb-3" />
            <p class="text-gray-600 font-medium">点击或拖拽文件到此处</p>
            <p class="text-sm text-gray-400 mt-1">
              支持 {{ tool.acceptFileTypes || '所有' }} 格式，最大 {{ formatFileSize(tool.maxFileSize || 10 * 1024 * 1024) }}
            </p>
          </div>
          <div v-else class="flex items-center justify-center gap-3">
            <Icon name="lucide:file" class="w-8 h-8 text-indigo-600" />
            <div class="text-left">
              <p class="font-medium text-gray-900">{{ file.name }}</p>
              <p class="text-sm text-gray-500">{{ formatFileSize(file.size) }}</p>
            </div>
            <button
              @click.stop="clearFile"
              class="p-1 hover:bg-gray-200 rounded-full transition-colors"
            >
              <Icon name="lucide:x" class="w-5 h-5 text-gray-500" />
            </button>
          </div>
        </div>
        <p v-if="fileError" class="mt-2 text-sm text-red-600">{{ fileError }}</p>

        <!-- 上传进度 -->
        <div v-if="isUploading" class="mt-4">
          <div class="flex items-center justify-between text-sm mb-2">
            <span class="text-gray-600">上传中...</span>
            <span class="text-gray-900 font-medium">{{ uploadProgress }}%</span>
          </div>
          <div class="h-2 bg-gray-200 rounded-full overflow-hidden">
            <div
              class="h-full bg-indigo-600 transition-all duration-300"
              :style="{ width: `${uploadProgress}%` }"
            />
          </div>
        </div>
      </div>

      <!-- 执行按钮 -->
      <div class="mt-6 flex items-center gap-4">
        <Button
          size="lg"
          class="flex-1"
          :disabled="!canExecute || isLoading"
          @click="execute"
        >
          <Icon v-if="isLoading" name="lucide:loader-2" class="w-5 h-5 mr-2 animate-spin" />
          <Icon v-else name="lucide:play" class="w-5 h-5 mr-2" />
          {{ isLoading ? '处理中...' : '开始处理' }}
        </Button>
        <Button
          v-if="isLoading && tool?.type !== 'instant'"
          variant="outline"
          size="lg"
          @click="cancel"
        >
          <Icon name="lucide:square" class="w-5 h-5 mr-2" />
          取消
        </Button>
      </div>
    </div>

    <!-- 进度显示 -->
    <div v-if="showProgress && progress" class="px-6 pb-6">
      <div class="bg-gray-50 rounded-lg p-4">
        <div class="flex items-center justify-between mb-2">
          <span class="text-sm font-medium text-gray-700">处理进度</span>
          <span class="text-sm text-gray-900 font-bold">{{ progress.percent }}%</span>
        </div>
        <div class="h-2 bg-gray-200 rounded-full overflow-hidden">
          <div
            class="h-full bg-linear-to-r from-indigo-500 to-purple-500 transition-all duration-300"
            :style="{ width: `${progress.percent}%` }"
          />
        </div>
        <p class="mt-2 text-sm text-gray-600">{{ progress.message }}</p>
      </div>
    </div>

    <!-- 结果显示区域 -->
    <div v-if="result || error" class="border-t border-gray-200">
      <div v-if="error" class="p-6 bg-red-50">
        <div class="flex items-start gap-3">
          <Icon name="lucide:alert-circle" class="w-5 h-5 text-red-600 mt-0.5" />
          <div>
            <h3 class="font-medium text-red-900">处理失败</h3>
            <p class="text-sm text-red-700 mt-1">{{ error }}</p>
          </div>
        </div>
      </div>
      <div v-else-if="result" class="p-6 bg-green-50">
        <div class="flex items-start gap-3">
          <Icon name="lucide:check-circle" class="w-5 h-5 text-green-600 mt-0.5" />
          <div class="flex-1">
            <h3 class="font-medium text-green-900">处理完成</h3>
            <slot name="result" :result="result" />
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { TaskStatus } from '~/types/tool'
import type { Tool, ToolType } from '~/types/tool'

interface Props {
  tool: Tool | null
  toolType: ToolType
}

const props = defineProps<Props>()
const emit = defineEmits<{
  execute: [params: any, file?: File]
  cancel: []
}>()

// 状态
const params = ref<Record<string, any>>({})
const file = ref<File | null>(null)
const fileError = ref('')
const isDragging = ref(false)
const isUploading = ref(false)
const uploadProgress = ref(0)
const fileInput = ref<HTMLInputElement | null>(null)

// 执行状态
const isLoading = ref(false)
const status = ref<TaskStatus>(TaskStatus.PENDING)
const progress = ref<{ percent: number; message: string } | null>(null)
const result = ref<any>(null)
const error = ref('')

// 计算属性
const canExecute = computed(() => {
  if (props.tool?.needFile && !file.value) return false
  return true
})

const showProgress = computed(() => {
  return props.toolType !== 'instant' && status.value === 'processing'
})

// 方法
const updateParams = (newParams: Record<string, any>) => {
  params.value = { ...params.value, ...newParams }
}

const triggerFileInput = () => {
  fileInput.value?.click()
}

const handleFileSelect = (event: Event) => {
  const target = event.target as HTMLInputElement
  if (target.files && target.files[0]) {
    validateAndSetFile(target.files[0])
  }
}

const handleDrop = (event: DragEvent) => {
  isDragging.value = false
  if (event.dataTransfer?.files[0]) {
    validateAndSetFile(event.dataTransfer.files[0])
  }
}

const validateAndSetFile = (selectedFile: File) => {
  fileError.value = ''

  // 检查文件大小
  const maxSize = props.tool?.maxFileSize || 10 * 1024 * 1024
  if (selectedFile.size > maxSize) {
    fileError.value = `文件大小不能超过 ${formatFileSize(maxSize)}`
    return
  }

  // 检查文件类型
  if (props.tool?.acceptFileTypes) {
    const acceptTypes = props.tool.acceptFileTypes.split(',').map(t => t.trim())
    const isAllowed = acceptTypes.some(type => {
      if (type.includes('*')) {
        return selectedFile.type.startsWith(type.replace('/*', ''))
      }
      return selectedFile.type === type
    })
    if (!isAllowed) {
      fileError.value = `不支持的文件类型`
      return
    }
  }

  file.value = selectedFile
}

const clearFile = () => {
  file.value = null
  fileError.value = ''
  if (fileInput.value) {
    fileInput.value.value = ''
  }
}

const formatFileSize = (bytes: number): string => {
  if (bytes === 0) return '0 Bytes'
  const k = 1024
  const sizes = ['Bytes', 'KB', 'MB', 'GB']
  const i = Math.floor(Math.log(bytes) / Math.log(k))
  return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i]
}

const execute = () => {
  if (!canExecute.value) return
  emit('execute', params.value, file.value || undefined)
}

const cancel = () => {
  emit('cancel')
}

// 暴露给父组件的方法
const setLoading = (value: boolean) => {
  isLoading.value = value
}

const setStatus = (newStatus: TaskStatus) => {
  status.value = newStatus
}

const setProgress = (newProgress: { percent: number; message: string } | null) => {
  progress.value = newProgress
}

const setResult = (newResult: any) => {
  result.value = newResult
  error.value = ''
}

const setError = (newError: string) => {
  error.value = newError
  result.value = null
}

const reset = () => {
  isLoading.value = false
  status.value = TaskStatus.PENDING
  progress.value = null
  result.value = null
  error.value = ''
}

defineExpose({
  setLoading,
  setStatus,
  setProgress,
  setResult,
  setError,
  reset
})
</script>
