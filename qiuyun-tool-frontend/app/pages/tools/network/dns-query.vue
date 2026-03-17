<script setup lang="ts">
import { ref } from 'vue'
import {
  Server,
  Search,
  RotateCcw,
  Globe,
  CheckCircle2,
  XCircle,
  Clock,
  AlertCircle,
  Info
} from 'lucide-vue-next'
import { ToolCard } from '@/components/ui/tool-card'
import { ToolButton } from '@/components/ui/tool-button'
import { Input } from '@/components/ui/input'
import { useToolExecutor } from '~/composables/useToolExecutor'
import { ToolType } from '~/types/tool'

useHead({
  title: 'DNS查询 - 秋云工具',
  meta: [
    { name: 'description', content: '查询域名的DNS记录，支持A/AAAA/MX/NS/SOA/TXT等多种记录类型' }
  ]
})

interface DnsQueryParams {
  domain: string
  type?: string
  dnsServer?: string
}

interface DnsRecord {
  name: string
  type: string
  ttl: number
  value: string
  priority?: number
  weight?: number
  port?: number
  details?: Record<string, any>
}

interface DnsQueryResult {
  domain: string
  type: string
  records: DnsRecord[]
}

const domain = ref('')
const recordType = ref('A')
const dnsServer = ref('')

const recordTypes = [
  { value: 'A', label: 'A (IPv4地址)', description: '将域名解析为IPv4地址' },
  { value: 'AAAA', label: 'AAAA (IPv6地址)', description: '将域名解析为IPv6地址' },
  { value: 'CNAME', label: 'CNAME (别名)', description: '域名的别名记录' },
  { value: 'MX', label: 'MX (邮件交换)', description: '邮件服务器记录' },
  { value: 'NS', label: 'NS (名称服务器)', description: 'DNS服务器记录' },
  { value: 'SOA', label: 'SOA (授权起始)', description: '区域授权信息' },
  { value: 'TXT', label: 'TXT (文本记录)', description: '文本信息记录' },
  { value: 'PTR', label: 'PTR (反向解析)', description: 'IP反解析为域名' },
  { value: 'SRV', label: 'SRV (服务定位)', description: '服务定位记录' }
]

const examples = [
  { domain: 'google.com', type: 'A' },
  { domain: 'baidu.com', type: 'A' },
  { domain: 'github.com', type: 'A' },
  { domain: 'cloudflare.com', type: 'AAAA' }
]

const { execute, isLoading, error, result } = useToolExecutor<DnsQueryParams, DnsQueryResult>({
  toolCode: 'dns-query',
  toolType: ToolType.INSTANT
})

const queryDns = async () => {
  await execute({
    domain: domain.value.trim(),
    type: recordType.value,
    dnsServer: dnsServer.value.trim() || undefined
  })
}

const useExample = (example: { domain: string; type: string }) => {
  domain.value = example.domain
  recordType.value = example.type
  queryDns()
}

const reset = () => {
  domain.value = ''
  recordType.value = 'A'
  dnsServer.value = ''
}

const getRecordTypeColor = (type: string) => {
  const colors: Record<string, string> = {
    A: 'bg-blue-100 text-blue-700 dark:bg-blue-900/30 dark:text-blue-400',
    AAAA: 'bg-indigo-100 text-indigo-700 dark:bg-indigo-900/30 dark:text-indigo-400',
    CNAME: 'bg-purple-100 text-purple-700 dark:bg-purple-900/30 dark:text-purple-400',
    MX: 'bg-orange-100 text-orange-700 dark:bg-orange-900/30 dark:text-orange-400',
    NS: 'bg-green-100 text-green-700 dark:bg-green-900/30 dark:text-green-400',
    SOA: 'bg-red-100 text-red-700 dark:bg-red-900/30 dark:text-red-400',
    TXT: 'bg-gray-100 text-gray-700 dark:bg-gray-800 dark:text-gray-400',
    PTR: 'bg-cyan-100 text-cyan-700 dark:bg-cyan-900/30 dark:text-cyan-400',
    SRV: 'bg-pink-100 text-pink-700 dark:bg-pink-900/30 dark:text-pink-400'
  }
  return colors[type] || 'bg-muted text-muted-foreground'
}
</script>

<template>
  <NuxtLayout name="tool" toolCode="dns-query">
    <div class="max-w-5xl mx-auto space-y-6">
      <ToolCard>
        <div class="flex items-center gap-2 mb-6">
          <Server class="w-6 h-6 text-primary" />
          <h2 class="text-lg font-semibold text-foreground">DNS查询</h2>
        </div>

        <div class="space-y-4">
          <div>
            <label class="block text-sm font-medium text-muted-foreground mb-2">
              域名
            </label>
            <Input
              v-model="domain"
              placeholder="输入要查询的域名，如: example.com"
              class="text-sm"
            />
          </div>

          <div>
            <label class="block text-sm font-medium text-muted-foreground mb-2">
              记录类型
            </label>
            <div class="grid grid-cols-2 md:grid-cols-3 lg:grid-cols-5 gap-2">
              <button
                v-for="type in recordTypes"
                :key="type.value"
                @click="recordType = type.value"
                :class="[
                  'px-3 py-2 rounded-lg text-sm font-medium transition-all text-left',
                  recordType === type.value
                    ? 'bg-primary text-primary-foreground'
                    : 'bg-muted text-muted-foreground hover:bg-muted/80'
                ]"
                :title="type.description"
              >
                {{ type.label.split(' ')[0] }}
              </button>
            </div>
            <p class="text-xs text-muted-foreground mt-2">
              {{ recordTypes.find(t => t.value === recordType)?.description }}
            </p>
          </div>

          <div>
            <label class="block text-sm font-medium text-muted-foreground mb-2">
              DNS服务器 (可选)
            </label>
            <Input
              v-model="dnsServer"
              placeholder="输入自定义DNS服务器，如: 8.8.8.8 或 114.114.114.114"
              class="text-sm"
            />
          </div>

          <div v-if="error" class="bg-red-50 dark:bg-red-950/30 text-red-700 dark:text-red-400 p-4 rounded-lg">
            <XCircle class="w-5 h-5 inline mr-2" />
            {{ error }}
          </div>

          <div class="flex items-center gap-3 flex-wrap">
            <ToolButton
              variant="primary"
              @click="queryDns"
              :disabled="isLoading || !domain"
            >
              <Search class="w-4 h-4 mr-1" />
              {{ isLoading ? '查询中...' : '查询DNS' }}
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
          <div class="flex items-center gap-2">
            <Globe class="w-4 h-4 text-muted-foreground" />
            <span class="text-sm text-muted-foreground">{{ result.domain }}</span>
            <span class="px-2 py-1 rounded text-xs font-medium" :class="getRecordTypeColor(result.type)">
              {{ result.type }}
            </span>
          </div>
        </div>

        <div v-if="result.records.length > 0" class="space-y-3">
          <div
            v-for="(record, index) in result.records"
            :key="index"
            class="p-4 bg-muted rounded-lg border border-border"
          >
            <div class="flex items-start gap-3">
              <span class="px-2 py-1 rounded text-xs font-medium shrink-0" :class="getRecordTypeColor(record.type)">
                {{ record.type }}
              </span>
              <div class="flex-1 min-w-0">
                <div class="font-medium text-foreground break-all">{{ record.value }}</div>
                <div class="flex items-center gap-4 mt-2 text-sm text-muted-foreground">
                  <span class="flex items-center gap-1">
                    <Clock class="w-4 h-4" />
                    TTL: {{ record.ttl }}s
                  </span>
                  <span v-if="record.priority !== undefined">
                    优先级: {{ record.priority }}
                  </span>
                  <span v-if="record.weight !== undefined">
                    权重: {{ record.weight }}
                  </span>
                  <span v-if="record.port !== undefined">
                    端口: {{ record.port }}
                  </span>
                </div>
                <div v-if="record.details" class="mt-3 p-3 bg-background rounded text-sm">
                  <div v-for="(value, key) in record.details" :key="key" class="flex justify-between py-1">
                    <span class="text-muted-foreground">{{ key }}:</span>
                    <span class="text-foreground">{{ value }}</span>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>

        <div v-else class="text-center py-8 text-muted-foreground">
          <AlertCircle class="w-12 h-12 mx-auto mb-4 opacity-50" />
          <p>未找到 {{ result.type }} 类型的DNS记录</p>
        </div>
      </ToolCard>

      <ToolCard>
        <div class="flex items-center gap-2 mb-4">
          <Globe class="w-5 h-5 text-primary" />
          <h3 class="font-semibold text-foreground">常用示例</h3>
        </div>
        <div class="grid grid-cols-1 md:grid-cols-2 gap-3">
          <button
            v-for="example in examples"
            :key="example.domain"
            @click="useExample(example)"
            class="p-3 text-left rounded-lg border border-border hover:border-primary hover:bg-primary/5 transition-all"
          >
            <div class="flex items-center justify-between">
              <span class="text-sm font-medium text-foreground">{{ example.domain }}</span>
              <span class="px-2 py-1 rounded text-xs" :class="getRecordTypeColor(example.type)">
                {{ example.type }}
              </span>
            </div>
          </button>
        </div>
      </ToolCard>

      <ToolCard>
        <div class="flex items-center gap-2 mb-4">
          <Info class="w-5 h-5 text-primary" />
          <h3 class="font-semibold text-foreground">记录类型说明</h3>
        </div>
        <div class="grid grid-cols-1 md:grid-cols-2 gap-4 text-sm">
          <div v-for="type in recordTypes" :key="type.value" class="flex items-start gap-2">
            <span class="px-2 py-0.5 rounded text-xs font-medium shrink-0" :class="getRecordTypeColor(type.value)">
              {{ type.value }}
            </span>
            <span class="text-muted-foreground">{{ type.description }}</span>
          </div>
        </div>
      </ToolCard>

      <ToolCard>
        <div class="flex items-center gap-2 mb-4">
          <CheckCircle2 class="w-5 h-5 text-primary" />
          <h3 class="font-semibold text-foreground">使用说明</h3>
        </div>
        <div class="space-y-3 text-sm text-muted-foreground">
          <p><strong>域名解析：</strong>输入域名查询对应的DNS记录，支持多种记录类型</p>
          <p><strong>记录类型：</strong>A记录用于IPv4，AAAA记录用于IPv6，MX记录用于邮件服务器</p>
          <p><strong>自定义DNS：</strong>可指定特定的DNS服务器进行查询，留空使用系统默认</p>
          <p><strong>反向解析：</strong>使用PTR记录类型可将IP地址反解析为域名</p>
        </div>
      </ToolCard>
    </div>
  </NuxtLayout>
</template>
