<script setup lang="ts">
import { ref, computed } from 'vue'
import {
  Globe,
  Send,
  Copy,
  RotateCcw,
  Plus,
  Trash2,
  Clock,
  AlertCircle,
  CheckCircle2,
  Code2,
  FileJson,
  Globe2,
  Settings,
  History,
  X,
  Zap
} from 'lucide-vue-next'
import { ToolCard } from '@/components/ui/tool-card'
import { ToolButton } from '@/components/ui/tool-button'
import { Input } from '@/components/ui/input'
import { Textarea } from '@/components/ui/textarea'
import { useToolExecutor } from '~/composables/useToolExecutor'
import { ToolType } from '~/types/tool'

useHead({
  title: 'HTTP 请求测试 - 秋云工具',
  meta: [
    { name: 'description', content: '在线 HTTP 请求测试工具，支持 GET、POST、PUT、DELETE 等各种 HTTP 方法' }
  ]
})

// HTTP 方法列表
const httpMethods = [
  { value: 'GET', label: 'GET', color: 'text-green-600', bgColor: 'bg-green-50' },
  { value: 'POST', label: 'POST', color: 'text-blue-600', bgColor: 'bg-blue-50' },
  { value: 'PUT', label: 'PUT', color: 'text-amber-600', bgColor: 'bg-amber-50' },
  { value: 'DELETE', label: 'DELETE', color: 'text-red-600', bgColor: 'bg-red-50' },
  { value: 'PATCH', label: 'PATCH', color: 'text-purple-600', bgColor: 'bg-purple-50' },
  { value: 'HEAD', label: 'HEAD', color: 'text-gray-600', bgColor: 'bg-gray-50' },
  { value: 'OPTIONS', label: 'OPTIONS', color: 'text-teal-600', bgColor: 'bg-teal-50' }
]

// 常用请求头
const commonHeaders = [
  { key: 'Content-Type', value: 'application/json' },
  { key: 'Accept', value: 'application/json' },
  { key: 'Authorization', value: 'Bearer ' },
  { key: 'User-Agent', value: 'Mozilla/5.0' },
  { key: 'Accept-Language', value: 'zh-CN,zh;q=0.9' },
  { key: 'Cache-Control', value: 'no-cache' }
]

// 请求参数类型
interface HttpRequestParams {
  method: string
  url: string
  headers: Array<{ key: string; value: string }>
  params: Array<{ key: string; value: string }>
  body: string | null
  timeout: number
}

// 响应结果类型
interface HttpRequestResult {
  statusCode: number
  statusText: string
  headers: Record<string, string[]>
  body: string
  bodySize: number
  bodyTruncated: boolean
  bodyHash: string
  contentType: string
  duration: number
  request: {
    method: string
    url: string
    finalUrl: string
    headers: Record<string, string>
  }
  analysis: {
    statusCategory: string
    statusDescription: string
    performance: string
    cacheable: boolean
    compressed: boolean
  }
}

// 请求状态
const method = ref('GET')
const url = ref('')
const activeTab = ref('params') // params, headers, body
const showSettings = ref(false)

// 请求配置
const headers = ref<Array<{ key: string; value: string; enabled: boolean }>>([
  { key: '', value: '', enabled: true }
])
const params = ref<Array<{ key: string; value: string; enabled: boolean }>>([
  { key: '', value: '', enabled: true }
])
const body = ref('')
const timeout = ref(30000)

// 响应结果
const response = ref<HttpRequestResult | null>(null)
const responseTab = ref('body') // body, headers, analysis

// 历史记录
const history = ref<Array<{ method: string; url: string; timestamp: number }>>([])

// 示例 URL
const examples = [
  { url: 'https://api.github.com/users/github', desc: 'GitHub API' },
  { url: 'https://httpbin.org/get', desc: 'HTTP Bin' },
  { url: 'https://jsonplaceholder.typicode.com/posts/1', desc: 'JSON Placeholder' },
  { url: 'https://api.ipify.org?format=json', desc: '获取 IP' }
]

// 使用工具执行器
const { execute, isLoading, error } = useToolExecutor<HttpRequestParams, HttpRequestResult>({
  toolCode: 'http-request',
  toolType: ToolType.INSTANT,
  onSuccess: (result) => {
    response.value = result
    addToHistory()
  }
})

// 使用示例
const useExample = (exampleUrl: string) => {
  url.value = exampleUrl
  method.value = 'GET'
}

// 添加请求头
const addHeader = () => {
  headers.value.push({ key: '', value: '', enabled: true })
}

// 删除请求头
const removeHeader = (index: number) => {
  headers.value.splice(index, 1)
  if (headers.value.length === 0) {
    addHeader()
  }
}

// 添加常用请求头
const addCommonHeader = (header: { key: string; value: string }) => {
  const existingIndex = headers.value.findIndex(h => h.key === header.key && h.value === '')
  if (existingIndex >= 0) {
    headers.value[existingIndex] = { ...header, enabled: true }
  } else {
    headers.value.push({ ...header, enabled: true })
  }
}

// 添加查询参数
const addParam = () => {
  params.value.push({ key: '', value: '', enabled: true })
}

// 删除查询参数
const removeParam = (index: number) => {
  params.value.splice(index, 1)
  if (params.value.length === 0) {
    addParam()
  }
}

// 格式化 JSON
const formatBody = () => {
  try {
    const parsed = JSON.parse(body.value)
    body.value = JSON.stringify(parsed, null, 2)
  } catch {
    // 格式化失败，不做处理
  }
}

// 发送请求
const sendRequest = async () => {
  if (!url.value.trim()) return

  const requestHeaders = headers.value
    .filter(h => h.enabled && h.key.trim())
    .map(h => ({ key: h.key.trim(), value: h.value }))

  const requestParams = params.value
    .filter(p => p.enabled && p.key.trim())
    .map(p => ({ key: p.key.trim(), value: p.value }))

  await execute({
    method: method.value,
    url: url.value.trim(),
    headers: requestHeaders,
    params: requestParams,
    body: body.value || null,
    timeout: timeout.value
  })
}

// 添加到历史记录
const addToHistory = () => {
  const entry = {
    method: method.value,
    url: url.value,
    timestamp: Date.now()
  }
  history.value = [entry, ...history.value.slice(0, 9)]
}

// 从历史记录加载
const loadFromHistory = (entry: any) => {
  method.value = entry.method
  url.value = entry.url
}

// 清空历史
const clearHistory = () => {
  history.value = []
}

// 复制响应体
const copyResponse = () => {
  if (response.value?.body) {
    navigator.clipboard.writeText(response.value.body)
  }
}

// 重置
const reset = () => {
  method.value = 'GET'
  url.value = ''
  headers.value = [{ key: '', value: '', enabled: true }]
  params.value = [{ key: '', value: '', enabled: true }]
  body.value = ''
  timeout.value = 30000
  response.value = null
}

// 获取状态码颜色
const getStatusColor = (code: number) => {
  if (code >= 200 && code < 300) return 'text-green-600 bg-green-50'
  if (code >= 300 && code < 400) return 'text-amber-600 bg-amber-50'
  if (code >= 400 && code < 500) return 'text-orange-600 bg-orange-50'
  if (code >= 500) return 'text-red-600 bg-red-50'
  return 'text-gray-600 bg-gray-50'
}

// 格式化 JSON 响应
const formattedResponse = computed(() => {
  if (!response.value?.body) return ''
  try {
    const parsed = JSON.parse(response.value.body)
    return JSON.stringify(parsed, null, 2)
  } catch {
    return response.value.body
  }
})

// 计算完整 URL
const fullUrl = computed(() => {
  if (!url.value) return ''
  const enabledParams = params.value.filter(p => p.enabled && p.key.trim())
  if (enabledParams.length === 0) return url.value

  const queryString = enabledParams
    .map(p => `${encodeURIComponent(p.key.trim())}=${encodeURIComponent(p.value)}`)
    .join('&')

  const separator = url.value.includes('?') ? '&' : '?'
  return `${url.value}${separator}${queryString}`
})
</script>

<template>
  <NuxtLayout name="tool" toolCode="http-request">
    <div class="max-w-6xl mx-auto space-y-6">
      <!-- 请求配置区域 -->
      <ToolCard>
        <div class="flex items-center gap-2 mb-6">
          <Globe class="w-6 h-6 text-blue-500" />
          <h2 class="text-lg font-semibold text-gray-900">HTTP 请求测试</h2>
        </div>

        <div class="space-y-6">
          <!-- URL 和方法 -->
          <div class="flex flex-col sm:flex-row gap-3">
            <select
              v-model="method"
              class="px-4 py-2.5 bg-white border border-gray-200 rounded-lg text-sm font-medium focus:outline-none focus:ring-2 focus:ring-blue-500 shrink-0"
            >
              <option
                v-for="m in httpMethods"
                :key="m.value"
                :value="m.value"
              >
                {{ m.label }}
              </option>
            </select>
            <div class="flex-1 flex gap-2">
              <Input
                v-model="url"
                placeholder="输入请求 URL，如：https://api.example.com/users"
                class="flex-1"
                @keyup.enter="sendRequest"
              />
              <ToolButton
                variant="primary"
                @click="sendRequest"
                :disabled="isLoading || !url.trim()"
                class="shrink-0 px-4"
              >
                <Send class="w-4 h-4 mr-1.5" />
                {{ isLoading ? '发送中...' : '发送' }}
              </ToolButton>
            </div>
          </div>

          <!-- 完整 URL 预览 -->
          <div v-if="fullUrl && fullUrl !== url" class="text-sm text-gray-500 bg-gray-50 p-3 rounded-lg break-all">
            <span class="text-gray-400">完整 URL：</span>{{ fullUrl }}
          </div>

          <!-- 选项卡 -->
          <div class="border-b border-gray-200">
            <div class="flex gap-1">
              <button
                v-for="tab in ['params', 'headers', 'body']"
                :key="tab"
                @click="activeTab = tab"
                :class="[
                  'px-4 py-2 text-sm font-medium border-b-2 transition-colors',
                  activeTab === tab
                    ? 'border-blue-500 text-blue-600'
                    : 'border-transparent text-gray-500 hover:text-gray-700'
                ]"
              >
                {{ tab === 'params' ? '查询参数' : tab === 'headers' ? '请求头' : '请求体' }}
                <span
                  v-if="tab === 'params' && params.filter(p => p.enabled && p.key).length > 0"
                  class="ml-1 px-1.5 py-0.5 bg-blue-100 text-blue-600 rounded text-xs"
                >
                  {{ params.filter(p => p.enabled && p.key).length }}
                </span>
                <span
                  v-if="tab === 'headers' && headers.filter(h => h.enabled && h.key).length > 0"
                  class="ml-1 px-1.5 py-0.5 bg-blue-100 text-blue-600 rounded text-xs"
                >
                  {{ headers.filter(h => h.enabled && h.key).length }}
                </span>
              </button>
              <button
                @click="showSettings = !showSettings"
                :class="[
                  'px-4 py-2 text-sm font-medium border-b-2 transition-colors ml-auto',
                  showSettings
                    ? 'border-blue-500 text-blue-600'
                    : 'border-transparent text-gray-500 hover:text-gray-700'
                ]"
              >
                <Settings class="w-4 h-4" />
              </button>
            </div>
          </div>

          <!-- 查询参数 -->
          <div v-if="activeTab === 'params'" class="space-y-3">
            <div
              v-for="(param, index) in params"
              :key="index"
              class="flex items-center gap-2"
            >
              <input
                type="checkbox"
                v-model="param.enabled"
                class="w-4 h-4 text-blue-500 rounded focus:ring-blue-500"
              />
              <Input
                v-model="param.key"
                placeholder="参数名"
                class="flex-1"
                :disabled="!param.enabled"
              />
              <Input
                v-model="param.value"
                placeholder="参数值"
                class="flex-1"
                :disabled="!param.enabled"
              />
              <button
                @click="removeParam(index)"
                class="p-2 text-gray-400 hover:text-red-500 transition-colors"
              >
                <Trash2 class="w-4 h-4" />
              </button>
            </div>
            <ToolButton variant="secondary" size="sm" @click="addParam">
              <Plus class="w-4 h-4 mr-1" />
              添加参数
            </ToolButton>
          </div>

          <!-- 请求头 -->
          <div v-if="activeTab === 'headers'" class="space-y-3">
            <!-- 常用请求头快捷添加 -->
            <div class="flex flex-wrap gap-2 mb-4">
              <span class="text-xs text-gray-500 py-1">常用：</span>
              <button
                v-for="header in commonHeaders"
                :key="header.key"
                @click="addCommonHeader(header)"
                class="px-2 py-1 text-xs bg-gray-100 hover:bg-blue-100 text-gray-600 hover:text-blue-600 rounded transition-colors"
              >
                {{ header.key }}
              </button>
            </div>

            <div
              v-for="(header, index) in headers"
              :key="index"
              class="flex items-center gap-2"
            >
              <input
                type="checkbox"
                v-model="header.enabled"
                class="w-4 h-4 text-blue-500 rounded focus:ring-blue-500"
              />
              <Input
                v-model="header.key"
                placeholder="Header 名称"
                class="flex-1"
                :disabled="!header.enabled"
              />
              <Input
                v-model="header.value"
                placeholder="Header 值"
                class="flex-1"
                :disabled="!header.enabled"
              />
              <button
                @click="removeHeader(index)"
                class="p-2 text-gray-400 hover:text-red-500 transition-colors"
              >
                <Trash2 class="w-4 h-4" />
              </button>
            </div>
            <ToolButton variant="secondary" size="sm" @click="addHeader">
              <Plus class="w-4 h-4 mr-1" />
              添加请求头
            </ToolButton>
          </div>

          <!-- 请求体 -->
          <div v-if="activeTab === 'body'" class="space-y-3">
            <div class="flex items-center justify-between">
              <span class="text-sm text-gray-600">请求体内容</span>
              <ToolButton variant="secondary" size="sm" @click="formatBody">
                <Code2 class="w-4 h-4 mr-1" />
                格式化 JSON
              </ToolButton>
            </div>
            <Textarea
              v-model="body"
              placeholder="输入请求体内容（JSON、XML、Form Data 等）"
              class="min-h-[200px] font-mono text-sm"
            />
          </div>

          <!-- 设置 -->
          <div v-if="showSettings" class="bg-gray-50 rounded-lg p-4 space-y-4">
            <h3 class="font-medium text-gray-900">高级设置</h3>
            <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
              <div>
                <label class="block text-sm text-gray-600 mb-1">超时时间（毫秒）</label>
                <Input
                  v-model.number="timeout"
                  type="number"
                  min="1000"
                  max="120000"
                  step="1000"
                />
              </div>
            </div>
          </div>

          <!-- 错误提示 -->
          <div v-if="error" class="bg-red-50 text-red-700 p-4 rounded-lg">
            <AlertCircle class="w-5 h-5 inline mr-2" />
            {{ error }}
          </div>
        </div>
      </ToolCard>

      <!-- 响应结果 -->
      <ToolCard v-if="response">
        <div class="flex items-center justify-between mb-6">
          <div class="flex items-center gap-2">
            <Zap class="w-6 h-6 text-amber-500" />
            <h2 class="text-lg font-semibold text-gray-900">响应结果</h2>
          </div>
          <div class="flex items-center gap-3">
            <span
              :class="[
                'px-3 py-1 rounded-full text-sm font-medium',
                getStatusColor(response.statusCode)
              ]"
            >
              {{ response.statusCode }}
            </span>
            <span class="flex items-center gap-1 text-sm text-gray-500">
              <Clock class="w-4 h-4" />
              {{ response.duration }}ms
            </span>
          </div>
        </div>

        <!-- 响应选项卡 -->
        <div class="border-b border-gray-200 mb-4">
          <div class="flex gap-1">
            <button
              v-for="tab in ['body', 'headers', 'analysis']"
              :key="tab"
              @click="responseTab = tab"
              :class="[
                'px-4 py-2 text-sm font-medium border-b-2 transition-colors',
                responseTab === tab
                  ? 'border-amber-500 text-amber-600'
                  : 'border-transparent text-gray-500 hover:text-gray-700'
              ]"
            >
              {{ tab === 'body' ? '响应体' : tab === 'headers' ? '响应头' : '分析' }}
            </button>
            <ToolButton
              v-if="responseTab === 'body'"
              variant="secondary"
              size="sm"
              class="ml-auto"
              @click="copyResponse"
            >
              <Copy class="w-4 h-4 mr-1" />
              复制
            </ToolButton>
          </div>
        </div>

        <!-- 响应体 -->
        <div v-if="responseTab === 'body'" class="space-y-4">
          <div class="flex items-center gap-4 text-sm text-gray-600">
            <span>大小：{{ (response.bodySize / 1024).toFixed(2) }} KB</span>
            <span>类型：{{ response.contentType }}</span>
            <span v-if="response.bodyHash">MD5：{{ response.bodyHash.substring(0, 16) }}...</span>
          </div>
          <div v-if="response.bodyTruncated" class="bg-amber-50 text-amber-700 p-3 rounded-lg text-sm">
            <AlertCircle class="w-4 h-4 inline mr-1" />
            响应体过大，已截断显示
          </div>
          <pre class="bg-gray-900 text-gray-100 p-4 rounded-lg overflow-x-auto text-sm font-mono">{{ formattedResponse || response.body || '(无响应体)' }}</pre>
        </div>

        <!-- 响应头 -->
        <div v-if="responseTab === 'headers'" class="space-y-2">
          <div
            v-for="(values, key) in response.headers"
            :key="key"
            class="flex items-start gap-4 py-2 border-b border-gray-100 last:border-0"
          >
            <span class="text-sm font-medium text-gray-700 min-w-[200px]">{{ key }}</span>
            <span class="text-sm text-gray-600">{{ values.join(', ') }}</span>
          </div>
        </div>

        <!-- 分析 -->
        <div v-if="responseTab === 'analysis'" class="space-y-4">
          <div v-if="response.analysis" class="grid grid-cols-1 md:grid-cols-2 gap-4">
            <div class="bg-gray-50 rounded-lg p-4">
              <div class="text-sm text-gray-500 mb-1">状态分类</div>
              <div class="text-lg font-medium text-gray-900">{{ response.analysis.statusCategory }}</div>
              <div class="text-sm text-gray-600">{{ response.analysis.statusDescription }}</div>
            </div>
            <div class="bg-gray-50 rounded-lg p-4">
              <div class="text-sm text-gray-500 mb-1">性能评估</div>
              <div class="text-lg font-medium" :class="{
                'text-green-600': response.analysis.performance === '极快' || response.analysis.performance === '快',
                'text-amber-600': response.analysis.performance === '正常',
                'text-orange-600': response.analysis.performance === '较慢',
                'text-red-600': response.analysis.performance === '慢'
              }">{{ response.analysis.performance }}</div>
              <div class="text-sm text-gray-600">耗时 {{ response.duration }}ms</div>
            </div>
            <div class="bg-gray-50 rounded-lg p-4">
              <div class="text-sm text-gray-500 mb-1">缓存</div>
              <div class="text-lg font-medium" :class="response.analysis.cacheable ? 'text-green-600' : 'text-gray-600'">
                {{ response.analysis.cacheable ? '可缓存' : '不可缓存' }}
              </div>
            </div>
            <div class="bg-gray-50 rounded-lg p-4">
              <div class="text-sm text-gray-500 mb-1">压缩</div>
              <div class="text-lg font-medium" :class="response.analysis.compressed ? 'text-green-600' : 'text-gray-600'">
                {{ response.analysis.compressed ? '已压缩' : '未压缩' }}
              </div>
            </div>
          </div>

          <!-- 请求信息 -->
          <div v-if="response.request" class="border-t pt-4">
            <h3 class="font-medium text-gray-900 mb-3">请求信息</h3>
            <div class="bg-gray-50 rounded-lg p-4 space-y-2 text-sm">
              <div><span class="text-gray-500">方法：</span>{{ response.request.method }}</div>
              <div><span class="text-gray-500">URL：</span>{{ response.request.url }}</div>
              <div v-if="response.request.finalUrl !== response.request.url">
                <span class="text-gray-500">最终 URL：</span>{{ response.request.finalUrl }}
              </div>
            </div>
          </div>
        </div>
      </ToolCard>

      <!-- 快速示例 -->
      <ToolCard>
        <div class="flex items-center gap-2 mb-4">
          <Globe2 class="w-5 h-5 text-blue-500" />
          <h3 class="font-semibold text-gray-900">快速示例</h3>
        </div>
        <div class="grid grid-cols-1 md:grid-cols-2 gap-3">
          <button
            v-for="example in examples"
            :key="example.url"
            @click="useExample(example.url)"
            class="flex items-center gap-3 p-3 text-left rounded-lg border border-gray-200 hover:border-blue-300 hover:bg-blue-50 transition-all"
          >
            <FileJson class="w-5 h-5 text-blue-500" />
            <div>
              <div class="text-sm font-medium text-gray-900">{{ example.desc }}</div>
              <div class="text-xs text-gray-500 truncate">{{ example.url }}</div>
            </div>
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
            class="text-sm text-red-500 hover:text-red-600"
          >
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
            <span
              :class="[
                'px-2 py-0.5 rounded text-xs font-medium',
                httpMethods.find(m => m.value === entry.method)?.bgColor,
                httpMethods.find(m => m.value === entry.method)?.color
              ]"
            >
              {{ entry.method }}
            </span>
            <span class="flex-1 text-sm text-gray-700 truncate">{{ entry.url }}</span>
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
          <p><strong>支持方法：</strong>GET、POST、PUT、DELETE、PATCH、HEAD、OPTIONS 等所有标准 HTTP 方法</p>
          <p><strong>查询参数：</strong>在 Params 标签页添加 URL 查询参数，自动编码并拼接到 URL</p>
          <p><strong>请求头：</strong>在 Headers 标签页添加自定义请求头，支持常用请求头快捷添加</p>
          <p><strong>请求体：</strong>在 Body 标签页输入请求体内容，支持 JSON 格式化</p>
          <p><strong>响应分析：</strong>自动分析响应状态、性能、缓存和压缩情况</p>
          <p><strong>安全提示：</strong>请勿在生产环境发送包含敏感信息的请求</p>
        </div>
      </ToolCard>
    </div>
  </NuxtLayout>
</template>
