<script setup lang="ts">
import { ref, computed } from 'vue'
import {
  Globe,
  Search,
  RotateCcw,
  CheckCircle2,
  XCircle,
  Server,
  Calendar,
  User,
  Building,
  Mail,
  Phone,
  Info,
  Copy,
  FileText
} from 'lucide-vue-next'
import { ToolCard } from '@/components/ui/tool-card'
import { ToolButton } from '@/components/ui/tool-button'
import { Input } from '@/components/ui/input'
import { useToolExecutor } from '~/composables/useToolExecutor'
import { ToolType } from '~/types/tool'

useHead({
  title: 'Whois查询 - 秋云工具',
  meta: [
    { name: 'description', content: '查询域名或IP地址的Whois注册信息' }
  ]
})

interface WhoisQueryParams {
  query: string
  whoisServer?: string
}

interface WhoisQueryResult {
  query: string
  whoisServer: string
  rawResult: string
  parsedResult: Record<string, string>
}

const query = ref('')
const customServer = ref('')
const showRaw = ref(false)

const examples = [
  { name: 'Google', value: 'google.com' },
  { name: 'GitHub', value: 'github.com' },
  { name: '百度', value: 'baidu.com' },
  { name: 'Cloudflare', value: 'cloudflare.com' }
]

const { execute, isLoading, error, result } = useToolExecutor<WhoisQueryParams, WhoisQueryResult>({
  toolCode: 'whois-query',
  toolType: ToolType.INSTANT
})

const queryWhois = async () => {
  await execute({
    query: query.value.trim(),
    whoisServer: customServer.value.trim() || undefined
  })
}

const useExample = (value: string) => {
  query.value = value
  queryWhois()
}

const reset = () => {
  query.value = ''
  customServer.value = ''
  showRaw.value = false
}

const copyRawResult = () => {
  if (result.value?.rawResult) {
    navigator.clipboard.writeText(result.value.rawResult)
  }
}

const getImportantFields = (parsed: Record<string, string>) => {
  const importantKeys = [
    'Domain Name',
    'Registrar',
    'Registrar URL',
    'Creation Date',
    'Updated Date',
    'Registry Expiry Date',
    'Domain Status',
    'Name Server',
    'DNSSEC'
  ]
  
  const fields: Record<string, string> = {}
  for (const key of importantKeys) {
    for (const [k, v] of Object.entries(parsed)) {
      if (k.toLowerCase().includes(key.toLowerCase())) {
        fields[k] = v
        break
      }
    }
  }
  return fields
}

const getContactFields = (parsed: Record<string, string>) => {
  const contactKeys = [
    'Registrant',
    'Admin',
    'Tech'
  ]
  
  const fields: Record<string, string> = {}
  for (const key of contactKeys) {
    for (const [k, v] of Object.entries(parsed)) {
      if (k.toLowerCase().includes(key.toLowerCase()) && 
          (k.toLowerCase().includes('name') || k.toLowerCase().includes('email') || 
           k.toLowerCase().includes('phone') || k.toLowerCase().includes('organization'))) {
        fields[k] = v
      }
    }
  }
  return fields
}
</script>

<template>
  <NuxtLayout name="tool" toolCode="whois-query">
    <div class="max-w-5xl mx-auto space-y-6">
      <ToolCard>
        <div class="flex items-center gap-2 mb-6">
          <Globe class="w-6 h-6 text-primary" />
          <h2 class="text-lg font-semibold text-foreground">Whois查询</h2>
        </div>

        <div class="space-y-4">
          <div>
            <label class="block text-sm font-medium text-muted-foreground mb-2">
              域名或IP地址
            </label>
            <Input
              v-model="query"
              placeholder="输入域名或IP地址，如: example.com 或 8.8.8.8"
              class="text-sm"
            />
          </div>

          <div>
            <label class="block text-sm font-medium text-muted-foreground mb-2">
              Whois服务器 (可选)
            </label>
            <Input
              v-model="customServer"
              placeholder="输入自定义Whois服务器，留空自动选择"
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
              @click="queryWhois"
              :disabled="isLoading || !query"
            >
              <Search class="w-4 h-4 mr-1" />
              {{ isLoading ? '查询中...' : '查询Whois' }}
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
            <Server class="w-4 h-4 text-muted-foreground" />
            <span class="text-sm text-muted-foreground">{{ result.whoisServer }}</span>
          </div>
        </div>

        <div class="space-y-6">
          <!-- 域名信息 -->
          <div class="bg-gradient-to-br from-blue-50 to-indigo-50 dark:from-blue-950/30 dark:to-indigo-950/30 rounded-xl p-5 border border-blue-100 dark:border-blue-800">
            <div class="flex items-center gap-2 mb-4">
              <Globe class="w-5 h-5 text-blue-600 dark:text-blue-400" />
              <h3 class="font-semibold text-foreground">域名信息</h3>
            </div>
            <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
              <div v-for="(value, key) in getImportantFields(result.parsedResult)" :key="key" class="flex flex-col">
                <span class="text-xs text-muted-foreground">{{ key }}</span>
                <span class="text-sm font-medium text-foreground break-all">{{ value }}</span>
              </div>
            </div>
          </div>

          <!-- 联系人信息 -->
          <div v-if="Object.keys(getContactFields(result.parsedResult)).length > 0" 
               class="bg-gradient-to-br from-purple-50 to-violet-50 dark:from-purple-950/30 dark:to-violet-950/30 rounded-xl p-5 border border-purple-100 dark:border-purple-800">
            <div class="flex items-center gap-2 mb-4">
              <User class="w-5 h-5 text-purple-600 dark:text-purple-400" />
              <h3 class="font-semibold text-foreground">联系人信息</h3>
            </div>
            <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
              <div v-for="(value, key) in getContactFields(result.parsedResult)" :key="key" class="flex flex-col">
                <span class="text-xs text-muted-foreground">{{ key }}</span>
                <span class="text-sm font-medium text-foreground break-all">{{ value }}</span>
              </div>
            </div>
          </div>

          <!-- 所有字段 -->
          <div>
            <div class="flex items-center justify-between mb-4">
              <h3 class="font-semibold text-foreground">完整信息</h3>
              <div class="flex items-center gap-2">
                <button
                  @click="showRaw = !showRaw"
                  class="text-sm text-primary hover:text-primary/80"
                >
                  {{ showRaw ? '显示解析结果' : '显示原始结果' }}
                </button>
                <ToolButton v-if="showRaw" variant="secondary" size="sm" @click="copyRawResult">
                  <Copy class="w-4 h-4 mr-1" />
                  复制
                </ToolButton>
              </div>
            </div>

            <div v-if="!showRaw" class="bg-muted rounded-lg p-4 space-y-2 max-h-96 overflow-y-auto">
              <div v-for="(value, key) in result.parsedResult" :key="key" class="flex justify-between py-1 border-b border-border last:border-0">
                <span class="text-sm text-muted-foreground shrink-0 mr-4">{{ key }}</span>
                <span class="text-sm text-foreground text-right break-all">{{ value }}</span>
              </div>
            </div>

            <div v-else class="bg-muted rounded-lg p-4">
              <pre class="text-sm text-foreground whitespace-pre-wrap break-all max-h-96 overflow-y-auto">{{ result.rawResult }}</pre>
            </div>
          </div>
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
            :key="example.value"
            @click="useExample(example.value)"
            class="p-3 text-left rounded-lg border border-border hover:border-primary hover:bg-primary/5 transition-all"
          >
            <div class="text-sm font-medium text-foreground">{{ example.name }}</div>
            <div class="text-xs text-muted-foreground mt-1">{{ example.value }}</div>
          </button>
        </div>
      </ToolCard>

      <ToolCard>
        <div class="flex items-center gap-2 mb-4">
          <Info class="w-5 h-5 text-primary" />
          <h3 class="font-semibold text-foreground">Whois信息说明</h3>
        </div>
        <div class="grid grid-cols-1 md:grid-cols-2 gap-4 text-sm">
          <div class="flex items-start gap-2">
            <Globe class="w-4 h-4 text-muted-foreground mt-0.5" />
            <div>
              <span class="font-medium text-foreground">域名信息</span>
              <p class="text-muted-foreground">域名名称、注册商、注册日期、过期日期等</p>
            </div>
          </div>
          <div class="flex items-start gap-2">
            <Server class="w-4 h-4 text-muted-foreground mt-0.5" />
            <div>
              <span class="font-medium text-foreground">DNS服务器</span>
              <p class="text-muted-foreground">域名使用的名称服务器列表</p>
            </div>
          </div>
          <div class="flex items-start gap-2">
            <User class="w-4 h-4 text-muted-foreground mt-0.5" />
            <div>
              <span class="font-medium text-foreground">注册人信息</span>
              <p class="text-muted-foreground">域名所有者的联系信息（部分隐私保护）</p>
            </div>
          </div>
          <div class="flex items-start gap-2">
            <FileText class="w-4 h-4 text-muted-foreground mt-0.5" />
            <div>
              <span class="font-medium text-foreground">域名状态</span>
              <p class="text-muted-foreground">域名的当前状态和锁定情况</p>
            </div>
          </div>
        </div>
      </ToolCard>

      <ToolCard>
        <div class="flex items-center gap-2 mb-4">
          <CheckCircle2 class="w-5 h-5 text-primary" />
          <h3 class="font-semibold text-foreground">使用说明</h3>
        </div>
        <div class="space-y-3 text-sm text-muted-foreground">
          <p><strong>查询内容：</strong>支持查询域名（如 example.com）或IP地址（如 8.8.8.8）的Whois信息</p>
          <p><strong>自动选择：</strong>系统会根据域名后缀自动选择合适的Whois服务器</p>
          <p><strong>隐私保护：</strong>部分域名启用了隐私保护，注册人信息可能显示为隐私服务</p>
          <p><strong>Referral：</strong>支持Whois Referral自动跳转，获取完整信息</p>
        </div>
      </ToolCard>
    </div>
  </NuxtLayout>
</template>
