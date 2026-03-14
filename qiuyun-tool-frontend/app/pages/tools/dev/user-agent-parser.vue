<script setup lang="ts">
import { ref, computed } from 'vue'
import {
  Smartphone,
  Copy,
  RotateCcw,
  Globe,
  Monitor,
  Cpu,
  Layers,
  CheckCircle2,
  Zap,
  History,
  Trash2
} from 'lucide-vue-next'
import { ToolCard } from '@/components/ui/tool-card'
import { ToolButton } from '@/components/ui/tool-button'
import { Textarea } from '@/components/ui/textarea'
import { useToolExecutor } from '~/composables/useToolExecutor'
import { ToolType } from '~/types/tool'

useHead({
  title: 'User-Agent 解析 - 秋云工具',
  meta: [
    { name: 'description', content: '解析 User-Agent 字符串，获取浏览器、操作系统、设备等信息' }
  ]
})

// 请求参数类型
interface UAParserParams {
  userAgent: string
}

// 响应结果类型
interface UAParserResult {
  original: string
  browser: string
  browserVersion: string
  os: string
  osVersion: string
  deviceType: string
  brand: string
  model: string
  device: string
  engine: string
  engineVersion: string
}

// 输入状态
const userAgent = ref('')
const history = ref<Array<{ ua: string; timestamp: number }>>([])

// 常用 User-Agent 示例
const examples = [
  {
    name: 'Chrome on Windows',
    ua: 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36'
  },
  {
    name: 'Safari on macOS',
    ua: 'Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36'
  },
  {
    name: 'iPhone Safari',
    ua: 'Mozilla/5.0 (iPhone; CPU iPhone OS 17_0 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/17.0 Mobile/15E148 Safari/604.1'
  },
  {
    name: 'Android Chrome',
    ua: 'Mozilla/5.0 (Linux; Android 14; SM-S918B) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Mobile Safari/537.36'
  },
  {
    name: 'Firefox',
    ua: 'Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:121.0) Gecko/20100101 Firefox/121.0'
  },
  {
    name: 'Edge',
    ua: 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36 Edg/120.0.0.0'
  }
]

// 使用工具执行器
const { execute, isLoading, error, result } = useToolExecutor<UAParserParams, UAParserResult>({
  toolCode: 'user-agent-parser',
  toolType: ToolType.INSTANT,
  onSuccess: () => {
    addToHistory()
  }
})

// 使用示例
const useExample = (exampleUa: string) => {
  userAgent.value = exampleUa
  parseUserAgent()
}

// 获取当前浏览器 UA
const getCurrentUA = () => {
  userAgent.value = navigator.userAgent
  parseUserAgent()
}

// 解析 User-Agent
const parseUserAgent = async () => {
  if (!userAgent.value.trim()) return

  await execute({
    userAgent: userAgent.value
  })
}

// 添加到历史记录
const addToHistory = () => {
  const entry = {
    ua: userAgent.value,
    timestamp: Date.now()
  }
  // 避免重复
  history.value = history.value.filter(h => h.ua !== entry.ua)
  history.value = [entry, ...history.value.slice(0, 9)]
}

// 从历史记录加载
const loadFromHistory = (entry: any) => {
  userAgent.value = entry.ua
  parseUserAgent()
}

// 清空历史
const clearHistory = () => {
  history.value = []
}

// 复制结果
const copyResult = () => {
  if (result.value) {
    const text = JSON.stringify(result.value, null, 2)
    navigator.clipboard.writeText(text)
  }
}

// 重置
const reset = () => {
  userAgent.value = ''
}

// 获取浏览器图标颜色
const getBrowserColor = (browser: string) => {
  const colors: Record<string, string> = {
    'Chrome': 'text-blue-500',
    'Firefox': 'text-orange-500',
    'Safari': 'text-blue-400',
    'Edge': 'text-teal-500',
    'Opera': 'text-red-500',
    'WeChat': 'text-green-500',
    'QQ Browser': 'text-blue-600'
  }
  return colors[browser] || 'text-gray-500'
}

// 获取设备类型图标
const getDeviceIcon = (deviceType: string) => {
  switch (deviceType) {
    case 'Mobile':
      return Smartphone
    case 'Tablet':
      return Layers
    case 'TV':
      return Monitor
    default:
      return Monitor
  }
}
</script>

<template>
  <NuxtLayout name="tool" toolCode="user-agent-parser">
    <div class="max-w-5xl mx-auto space-y-6">
      <!-- 输入区域 -->
      <ToolCard>
        <div class="flex items-center gap-2 mb-6">
          <Smartphone class="w-6 h-6 text-blue-500" />
          <h2 class="text-lg font-semibold text-gray-900">User-Agent 解析</h2>
        </div>

        <div class="space-y-4">
          <!-- User-Agent 输入 -->
          <div>
            <label class="block text-sm font-medium text-gray-700 mb-2">
              User-Agent 字符串
            </label>
            <Textarea
              v-model="userAgent"
              placeholder="粘贴 User-Agent 字符串，例如：Mozilla/5.0 (Windows NT 10.0; Win64; x64)..."
              class="min-h-[120px] text-sm font-mono"
            />
          </div>

          <!-- 错误提示 -->
          <div v-if="error" class="bg-red-50 text-red-700 p-4 rounded-lg">
            <AlertCircle class="w-5 h-5 inline mr-2" />
            {{ error }}
          </div>

          <!-- 操作按钮 -->
          <div class="flex items-center gap-3 flex-wrap">
            <ToolButton
              variant="primary"
              @click="parseUserAgent"
              :disabled="isLoading || !userAgent.trim()"
            >
              <Zap class="w-4 h-4 mr-1" />
              {{ isLoading ? '解析中...' : '开始解析' }}
            </ToolButton>
            <ToolButton variant="secondary" @click="getCurrentUA">
              <Globe class="w-4 h-4 mr-1" />
              获取当前浏览器
            </ToolButton>
            <ToolButton variant="secondary" @click="reset">
              <RotateCcw class="w-4 h-4 mr-1" />
              重置
            </ToolButton>
          </div>
        </div>
      </ToolCard>

      <!-- 解析结果 -->
      <ToolCard v-if="result">
        <div class="flex items-center justify-between mb-6">
          <div class="flex items-center gap-2">
            <CheckCircle2 class="w-6 h-6 text-green-500" />
            <h2 class="text-lg font-semibold text-gray-900">解析结果</h2>
          </div>
          <ToolButton variant="secondary" size="sm" @click="copyResult">
            <Copy class="w-4 h-4 mr-1" />
            复制结果
          </ToolButton>
        </div>

        <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
          <!-- 浏览器信息 -->
          <div class="bg-linear-to-br from-blue-50 to-indigo-50 rounded-xl p-5 border border-blue-100">
            <div class="flex items-center gap-2 mb-4">
              <Globe class="w-5 h-5 text-blue-500" />
              <h3 class="font-semibold text-gray-900">浏览器</h3>
            </div>
            <div class="space-y-2">
              <div class="flex justify-between">
                <span class="text-sm text-gray-500">名称</span>
                <span class="text-sm font-medium" :class="getBrowserColor(result.browser)">
                  {{ result.browser || 'Unknown' }}
                </span>
              </div>
              <div class="flex justify-between">
                <span class="text-sm text-gray-500">版本</span>
                <span class="text-sm font-medium text-gray-900">{{ result.browserVersion || '-' }}</span>
              </div>
            </div>
          </div>

          <!-- 操作系统信息 -->
          <div class="bg-linear-to-br from-purple-50 to-violet-50 rounded-xl p-5 border border-purple-100">
            <div class="flex items-center gap-2 mb-4">
              <Layers class="w-5 h-5 text-purple-500" />
              <h3 class="font-semibold text-gray-900">操作系统</h3>
            </div>
            <div class="space-y-2">
              <div class="flex justify-between">
                <span class="text-sm text-gray-500">名称</span>
                <span class="text-sm font-medium text-gray-900">{{ result.os || 'Unknown' }}</span>
              </div>
              <div class="flex justify-between">
                <span class="text-sm text-gray-500">版本</span>
                <span class="text-sm font-medium text-gray-900">{{ result.osVersion || '-' }}</span>
              </div>
            </div>
          </div>

          <!-- 设备信息 -->
          <div class="bg-linear-to-br from-amber-50 to-orange-50 rounded-xl p-5 border border-amber-100">
            <div class="flex items-center gap-2 mb-4">
              <component :is="getDeviceIcon(result.deviceType)" class="w-5 h-5 text-amber-500" />
              <h3 class="font-semibold text-gray-900">设备</h3>
            </div>
            <div class="space-y-2">
              <div class="flex justify-between">
                <span class="text-sm text-gray-500">类型</span>
                <span class="text-sm font-medium text-gray-900">{{ result.deviceType || 'Unknown' }}</span>
              </div>
              <div class="flex justify-between">
                <span class="text-sm text-gray-500">品牌</span>
                <span class="text-sm font-medium text-gray-900">{{ result.brand || '-' }}</span>
              </div>
              <div v-if="result.model" class="flex justify-between">
                <span class="text-sm text-gray-500">型号</span>
                <span class="text-sm font-medium text-gray-900">{{ result.model }}</span>
              </div>
              <div v-if="result.device && result.device !== result.model" class="flex justify-between">
                <span class="text-sm text-gray-500">设备名</span>
                <span class="text-sm font-medium text-gray-900">{{ result.device }}</span>
              </div>
            </div>
          </div>

          <!-- 引擎信息 -->
          <div class="bg-linear-to-br from-teal-50 to-cyan-50 rounded-xl p-5 border border-teal-100">
            <div class="flex items-center gap-2 mb-4">
              <Cpu class="w-5 h-5 text-teal-500" />
              <h3 class="font-semibold text-gray-900">渲染引擎</h3>
            </div>
            <div class="space-y-2">
              <div class="flex justify-between">
                <span class="text-sm text-gray-500">名称</span>
                <span class="text-sm font-medium text-gray-900">{{ result.engine || 'Unknown' }}</span>
              </div>
              <div class="flex justify-between">
                <span class="text-sm text-gray-500">版本</span>
                <span class="text-sm font-medium text-gray-900">{{ result.engineVersion || '-' }}</span>
              </div>
            </div>
          </div>
        </div>

        <!-- 原始 User-Agent -->
        <div class="mt-6">
          <h3 class="font-medium text-gray-900 mb-2">原始 User-Agent</h3>
          <div class="bg-gray-50 rounded-lg p-4 text-sm font-mono text-gray-600 break-all">
            {{ result.original }}
          </div>
        </div>
      </ToolCard>

      <!-- 常用示例 -->
      <ToolCard>
        <div class="flex items-center gap-2 mb-4">
          <Zap class="w-5 h-5 text-blue-500" />
          <h3 class="font-semibold text-gray-900">常用示例</h3>
        </div>
        <div class="grid grid-cols-1 md:grid-cols-2 gap-3">
          <button
            v-for="example in examples"
            :key="example.name"
            @click="useExample(example.ua)"
            class="p-3 text-left rounded-lg border border-gray-200 hover:border-blue-300 hover:bg-blue-50 transition-all"
          >
            <div class="text-sm font-medium text-gray-900">{{ example.name }}</div>
            <div class="text-xs text-gray-500 truncate mt-1">{{ example.ua.substring(0, 60) }}...</div>
          </button>
        </div>
      </ToolCard>

      <!-- 历史记录 -->
      <ToolCard v-if="history.length > 0">
        <div class="flex items-center justify-between mb-4">
          <div class="flex items-center gap-2">
            <History class="w-5 h-5 text-blue-500" />
            <h3 class="font-semibold text-gray-900">历史记录</h3>
          </div>
          <button
            @click="clearHistory"
            class="flex items-center gap-1 text-sm text-red-500 hover:text-red-600"
          >
            <Trash2 class="w-4 h-4" />
            清空
          </button>
        </div>
        <div class="space-y-2">
          <button
            v-for="(entry, index) in history"
            :key="index"
            @click="loadFromHistory(entry)"
            class="w-full flex items-center gap-3 p-3 text-left rounded-lg hover:bg-gray-50 transition-colors"
          >
            <Smartphone class="w-4 h-4 text-gray-400" />
            <span class="flex-1 text-sm text-gray-700 truncate">{{ entry.ua.substring(0, 80) }}...</span>
            <span class="text-xs text-gray-400">
              {{ new Date(entry.timestamp).toLocaleTimeString() }}
            </span>
          </button>
        </div>
      </ToolCard>

      <!-- 使用说明 -->
      <ToolCard>
        <div class="flex items-center gap-2 mb-4">
          <CheckCircle2 class="w-5 h-5 text-blue-500" />
          <h3 class="font-semibold text-gray-900">使用说明</h3>
        </div>
        <div class="space-y-3 text-sm text-gray-600">
          <p><strong>User-Agent 是什么：</strong>User-Agent 是 HTTP 请求头的一部分，用于标识客户端的软件类型、操作系统、软件版本等信息</p>
          <p><strong>解析内容：</strong>支持解析浏览器名称和版本、操作系统、设备类型、品牌和型号、渲染引擎等信息</p>
          <p><strong>获取当前浏览器：</strong>点击"获取当前浏览器"按钮可自动获取您当前使用的浏览器的 User-Agent</p>
          <p><strong>支持的浏览器：</strong>Chrome、Firefox、Safari、Edge、Opera、微信、QQ浏览器等主流浏览器</p>
          <p><strong>支持的设备：</strong>iPhone、iPad、Android 设备、Windows、macOS、Linux 等</p>
        </div>
      </ToolCard>
    </div>
  </NuxtLayout>
</template>
