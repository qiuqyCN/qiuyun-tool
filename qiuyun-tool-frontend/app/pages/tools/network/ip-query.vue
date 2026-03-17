<script setup lang="ts">
import { ref, computed } from 'vue'
import {
  MapPin,
  Copy,
  RotateCcw,
  Globe,
  Wifi,
  Zap,
  History,
  Trash2,
  CheckCircle2,
  Database
} from 'lucide-vue-next'
import { ToolCard } from '@/components/ui/tool-card'
import { ToolButton } from '@/components/ui/tool-button'
import { Input } from '@/components/ui/input'
import { useToolExecutor } from '~/composables/useToolExecutor'
import { ToolType } from '~/types/tool'

useHead({
  title: 'IP地址查询 - 秋云工具',
  meta: [
    { name: 'description', content: '查询IP地址的地理位置信息，包括国家、省份、城市和运营商' }
  ]
})

interface IpQueryParams {
  ip: string
}

interface IpQueryResult {
  ip: string
  country: string
  region: string
  city: string
  isp: string
  regionId: string
  cityId: string
  countryId: string
  source: string
  queryTime: number
}

const ip = ref('')
const history = ref<Array<{ ip: string; timestamp: number }>>([])

const examples = [
  { name: '北京联通', ip: '202.60.126.220' },
  { name: '上海电信', ip: '202.96.128.86' },
  { name: '广州移动', ip: '211.136.192.6' },
  { name: '深圳联通', ip: '210.21.4.130' }
]

const { execute, isLoading, error, result } = useToolExecutor<IpQueryParams, IpQueryResult>({
  toolCode: 'ip-query',
  toolType: ToolType.INSTANT,
  onSuccess: () => {
    addToHistory()
  }
})

const useExample = (exampleIp: string) => {
  ip.value = exampleIp
  queryIp()
}

const queryMyIp = () => {
  ip.value = ''
  queryIp()
}

const queryIp = async () => {
  await execute({
    ip: ip.value.trim()
  })
}

const addToHistory = () => {
  const entry = {
    ip: result.value?.ip || ip.value,
    timestamp: Date.now()
  }
  history.value = history.value.filter(h => h.ip !== entry.ip)
  history.value = [entry, ...history.value.slice(0, 9)]
}

const loadFromHistory = (entry: any) => {
  ip.value = entry.ip
  queryIp()
}

const clearHistory = () => {
  history.value = []
}

const copyResult = () => {
  if (result.value) {
    const text = JSON.stringify(result.value, null, 2)
    navigator.clipboard.writeText(text)
  }
}

const reset = () => {
  ip.value = ''
}

const getSourceLabel = (source: string) => {
  const labels: Record<string, string> = {
    local: '本地缓存',
    redis: 'Redis缓存',
    api: 'API查询'
  }
  return labels[source] || source
}

const getSourceColor = (source: string) => {
  const colors: Record<string, string> = {
    local: 'text-green-600 dark:text-green-400',
    redis: 'text-blue-600 dark:text-blue-400',
    api: 'text-orange-600 dark:text-orange-400'
  }
  return colors[source] || 'text-muted-foreground'
}
</script>

<template>
  <NuxtLayout name="tool" toolCode="ip-query">
    <div class="max-w-5xl mx-auto space-y-6">
      <ToolCard>
        <div class="flex items-center gap-2 mb-6">
          <MapPin class="w-6 h-6 text-primary" />
          <h2 class="text-lg font-semibold text-foreground">IP地址查询</h2>
        </div>

        <div class="space-y-4">
          <div>
            <label class="block text-sm font-medium text-muted-foreground mb-2">
              IP地址
            </label>
            <Input
              v-model="ip"
              placeholder="输入IP地址，留空则查询当前访问IP"
              class="text-sm"
            />
          </div>

          <div v-if="error" class="bg-red-50 dark:bg-red-950/30 text-red-700 dark:text-red-400 p-4 rounded-lg">
            <Globe class="w-5 h-5 inline mr-2" />
            {{ error }}
          </div>

          <div class="flex items-center gap-3 flex-wrap">
            <ToolButton
              variant="primary"
              @click="queryIp"
              :disabled="isLoading"
            >
              <Zap class="w-4 h-4 mr-1" />
              {{ isLoading ? '查询中...' : '开始查询' }}
            </ToolButton>
            <ToolButton variant="secondary" @click="queryMyIp">
              <Globe class="w-4 h-4 mr-1" />
              查询当前IP
            </ToolButton>
            <ToolButton variant="secondary" @click="reset">
              <RotateCcw class="w-4 h-4 mr-1" />
              重置
            </ToolButton>
          </div>
        </div>
      </ToolCard>

      <ToolCard v-if="result">
        <div class="flex items-center justify-between mb-6">
          <div class="flex items-center gap-2">
            <CheckCircle2 class="w-6 h-6 text-green-600 dark:text-green-400" />
            <h2 class="text-lg font-semibold text-foreground">查询结果</h2>
          </div>
          <ToolButton variant="secondary" size="sm" @click="copyResult">
            <Copy class="w-4 h-4 mr-1" />
            复制结果
          </ToolButton>
        </div>

        <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
          <div class="bg-gradient-to-br from-blue-50 to-indigo-50 dark:from-blue-950/30 dark:to-indigo-950/30 rounded-xl p-5 border border-blue-100 dark:border-blue-800">
            <div class="flex items-center gap-2 mb-4">
              <Globe class="w-5 h-5 text-blue-600 dark:text-blue-400" />
              <h3 class="font-semibold text-foreground">基本信息</h3>
            </div>
            <div class="space-y-2">
              <div class="flex justify-between">
                <span class="text-sm text-muted-foreground">IP地址</span>
                <span class="text-sm font-medium text-foreground">{{ result.ip }}</span>
              </div>
              <div class="flex justify-between">
                <span class="text-sm text-muted-foreground">国家</span>
                <span class="text-sm font-medium text-foreground">{{ result.country || '-' }}</span>
              </div>
              <div class="flex justify-between">
                <span class="text-sm text-muted-foreground">省份</span>
                <span class="text-sm font-medium text-foreground">{{ result.region || '-' }}</span>
              </div>
              <div class="flex justify-between">
                <span class="text-sm text-muted-foreground">城市</span>
                <span class="text-sm font-medium text-foreground">{{ result.city || '-' }}</span>
              </div>
            </div>
          </div>

          <div class="bg-gradient-to-br from-purple-50 to-violet-50 dark:from-purple-950/30 dark:to-violet-950/30 rounded-xl p-5 border border-purple-100 dark:border-purple-800">
            <div class="flex items-center gap-2 mb-4">
              <Wifi class="w-5 h-5 text-purple-600 dark:text-purple-400" />
              <h3 class="font-semibold text-foreground">网络信息</h3>
            </div>
            <div class="space-y-2">
              <div class="flex justify-between">
                <span class="text-sm text-muted-foreground">运营商</span>
                <span class="text-sm font-medium text-foreground">{{ result.isp || '-' }}</span>
              </div>
              <div class="flex justify-between">
                <span class="text-sm text-muted-foreground">国家代码</span>
                <span class="text-sm font-medium text-foreground">{{ result.countryId || '-' }}</span>
              </div>
              <div class="flex justify-between">
                <span class="text-sm text-muted-foreground">省份代码</span>
                <span class="text-sm font-medium text-foreground">{{ result.regionId || '-' }}</span>
              </div>
              <div class="flex justify-between">
                <span class="text-sm text-muted-foreground">城市代码</span>
                <span class="text-sm font-medium text-foreground">{{ result.cityId || '-' }}</span>
              </div>
            </div>
          </div>
        </div>

        <div class="mt-6 bg-muted rounded-lg p-4">
          <div class="flex items-center gap-2 mb-2">
            <Database class="w-4 h-4 text-muted-foreground" />
            <span class="text-sm font-medium text-muted-foreground">数据来源</span>
          </div>
          <div class="flex items-center justify-between text-sm">
            <span :class="getSourceColor(result.source)" class="font-medium">
              {{ getSourceLabel(result.source) }}
            </span>
            <span v-if="result.queryTime" class="text-muted-foreground">
              查询时间: {{ new Date(result.queryTime).toLocaleString() }}
            </span>
          </div>
        </div>
      </ToolCard>

      <ToolCard>
        <div class="flex items-center gap-2 mb-4">
          <Zap class="w-5 h-5 text-primary" />
          <h3 class="font-semibold text-foreground">常用示例</h3>
        </div>
        <div class="grid grid-cols-1 md:grid-cols-2 gap-3">
          <button
            v-for="example in examples"
            :key="example.ip"
            @click="useExample(example.ip)"
            class="p-3 text-left rounded-lg border border-border hover:border-primary hover:bg-primary/5 transition-all"
          >
            <div class="text-sm font-medium text-foreground">{{ example.name }}</div>
            <div class="text-xs text-muted-foreground mt-1">{{ example.ip }}</div>
          </button>
        </div>
      </ToolCard>

      <ToolCard v-if="history.length > 0">
        <div class="flex items-center justify-between mb-4">
          <div class="flex items-center gap-2">
            <History class="w-5 h-5 text-primary" />
            <h3 class="font-semibold text-foreground">历史记录</h3>
          </div>
          <button
            @click="clearHistory"
            class="flex items-center gap-1 text-sm text-red-600 dark:text-red-400 hover:text-red-700 dark:hover:text-red-300"
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
            class="w-full flex items-center gap-3 p-3 text-left rounded-lg hover:bg-muted transition-colors"
          >
            <MapPin class="w-4 h-4 text-muted-foreground" />
            <span class="flex-1 text-sm text-foreground">{{ entry.ip }}</span>
            <span class="text-xs text-muted-foreground">
              {{ new Date(entry.timestamp).toLocaleTimeString() }}
            </span>
          </button>
        </div>
      </ToolCard>

      <ToolCard>
        <div class="flex items-center gap-2 mb-4">
          <CheckCircle2 class="w-5 h-5 text-primary" />
          <h3 class="font-semibold text-foreground">使用说明</h3>
        </div>
        <div class="space-y-3 text-sm text-muted-foreground">
          <p><strong>查询方式：</strong>输入IP地址查询，或留空查询当前访问IP</p>
          <p><strong>多级缓存：</strong>使用本地缓存(24小时)和Redis缓存(7天)提升查询速度</p>
          <p><strong>限流保护：</strong>API请求采用队列和限流机制，避免触发淘宝IP API限流</p>
          <p><strong>数据来源：</strong>使用淘宝IP API，数据准确可靠</p>
        </div>
      </ToolCard>
    </div>
  </NuxtLayout>
</template>
