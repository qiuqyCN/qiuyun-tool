<script setup lang="ts">
import { ref, computed } from 'vue'
import {
  Network,
  Scan,
  RotateCcw,
  Globe,
  Wifi,
  CheckCircle2,
  XCircle,
  Server,
  Activity,
  Shield,
  Info
} from 'lucide-vue-next'
import { ToolCard } from '@/components/ui/tool-card'
import { ToolButton } from '@/components/ui/tool-button'
import { Input } from '@/components/ui/input'
import { useToolExecutor } from '~/composables/useToolExecutor'
import { ToolType } from '~/types/tool'

useHead({
  title: '端口扫描器 - 秋云工具',
  meta: [
    { name: 'description', content: '扫描目标主机的开放端口，支持TCP和UDP协议' }
  ]
})

interface PortScanParams {
  host: string
  port?: number
  startPort?: number
  endPort?: number
  protocol?: string
  timeout?: number
  concurrency?: number
}

interface PortInfo {
  port: number
  service: string
  open: boolean
}

interface PortScanResult {
  host: string
  protocol: string
  scannedPorts: number
  openPorts: number
  ports: PortInfo[]
}

const host = ref('')
const scanMode = ref<'single' | 'range' | 'common'>('common')
const singlePort = ref<number | undefined>(undefined)
const startPort = ref<number | undefined>(1)
const endPort = ref<number | undefined>(1000)
const protocol = ref('TCP')
const timeout = ref(500)
const concurrency = ref(20)

const commonPorts = [
  { port: 21, service: 'FTP' },
  { port: 22, service: 'SSH' },
  { port: 23, service: 'Telnet' },
  { port: 25, service: 'SMTP' },
  { port: 53, service: 'DNS' },
  { port: 80, service: 'HTTP' },
  { port: 110, service: 'POP3' },
  { port: 143, service: 'IMAP' },
  { port: 443, service: 'HTTPS' },
  { port: 3306, service: 'MySQL' },
  { port: 3389, service: 'RDP' },
  { port: 5432, service: 'PostgreSQL' },
  { port: 6379, service: 'Redis' },
  { port: 8080, service: 'HTTP-Proxy' }
]

const { execute, isLoading, error, result } = useToolExecutor<PortScanParams, PortScanResult>({
  toolCode: 'port-scan',
  toolType: ToolType.INSTANT
})

const scanPorts = async () => {
  const params: PortScanParams = {
    host: host.value.trim(),
    protocol: protocol.value,
    timeout: timeout.value,
    concurrency: concurrency.value
  }

  if (scanMode.value === 'single' && singlePort.value) {
    params.port = singlePort.value
  } else if (scanMode.value === 'range') {
    params.startPort = startPort.value
    params.endPort = endPort.value
  }

  await execute(params)
}

const reset = () => {
  host.value = ''
  singlePort.value = undefined
  startPort.value = 1
  endPort.value = 1000
  protocol.value = 'TCP'
  timeout.value = 200
  concurrency.value = 50
  scanMode.value = 'common'
}

const getServiceIcon = (service: string) => {
  if (service.includes('HTTP')) return Globe
  if (service.includes('SSH') || service.includes('Telnet')) return Shield
  if (service.includes('SQL') || service.includes('Redis')) return Server
  return Activity
}
</script>

<template>
  <NuxtLayout name="tool" toolCode="port-scan">
    <div class="max-w-5xl mx-auto space-y-6">
      <ToolCard>
        <div class="flex items-center gap-2 mb-6">
          <Network class="w-6 h-6 text-primary" />
          <h2 class="text-lg font-semibold text-foreground">端口扫描器</h2>
        </div>

        <div class="space-y-4">
          <div>
            <label class="block text-sm font-medium text-muted-foreground mb-2">
              目标主机
            </label>
            <Input
              v-model="host"
              placeholder="输入主机地址或IP，如: example.com 或 192.168.1.1"
              class="text-sm"
            />
          </div>

          <div>
            <label class="block text-sm font-medium text-muted-foreground mb-2">
              扫描模式
            </label>
            <div class="flex gap-2 flex-wrap">
              <button
                v-for="mode in [
                  { key: 'common', label: '常用端口' },
                  { key: 'single', label: '单个端口' },
                  { key: 'range', label: '端口范围' }
                ]"
                :key="mode.key"
                @click="scanMode = mode.key as any"
                :class="[
                  'px-4 py-2 rounded-lg text-sm font-medium transition-all',
                  scanMode === mode.key
                    ? 'bg-primary text-primary-foreground'
                    : 'bg-muted text-muted-foreground hover:bg-muted/80'
                ]"
              >
                {{ mode.label }}
              </button>
            </div>
          </div>

          <div v-if="scanMode === 'single'">
            <label class="block text-sm font-medium text-muted-foreground mb-2">
              端口号
            </label>
            <Input
              v-model.number="singlePort"
              type="number"
              placeholder="输入端口号 (1-65535)"
              min="1"
              max="65535"
              class="text-sm"
            />
          </div>

          <div v-if="scanMode === 'range'" class="grid grid-cols-2 gap-4">
            <div>
              <label class="block text-sm font-medium text-muted-foreground mb-2">
                起始端口
              </label>
              <Input
                v-model.number="startPort"
                type="number"
                placeholder="1"
                min="1"
                max="65535"
                class="text-sm"
              />
            </div>
            <div>
              <label class="block text-sm font-medium text-muted-foreground mb-2">
                结束端口
              </label>
              <Input
                v-model.number="endPort"
                type="number"
                placeholder="65535"
                min="1"
                max="65535"
                class="text-sm"
              />
            </div>
          </div>

          <div class="grid grid-cols-1 md:grid-cols-3 gap-4">
            <div>
              <label class="block text-sm font-medium text-muted-foreground mb-2">
                协议
              </label>
              <select
                v-model="protocol"
                class="w-full h-10 px-3 rounded-md border border-input bg-background text-sm"
              >
                <option value="TCP">TCP</option>
                <option value="UDP">UDP</option>
              </select>
            </div>
            <div>
              <label class="block text-sm font-medium text-muted-foreground mb-2">
                超时时间 (ms)
              </label>
              <Input
                v-model.number="timeout"
                type="number"
                min="100"
                max="10000"
                class="text-sm"
              />
            </div>
            <div>
              <label class="block text-sm font-medium text-muted-foreground mb-2">
                并发数
              </label>
              <Input
                v-model.number="concurrency"
                type="number"
                min="1"
                max="100"
                class="text-sm"
              />
            </div>
          </div>

          <div v-if="error" class="bg-red-50 dark:bg-red-950/30 text-red-700 dark:text-red-400 p-4 rounded-lg">
            <XCircle class="w-5 h-5 inline mr-2" />
            {{ error }}
          </div>

          <div class="flex items-center gap-3 flex-wrap">
            <ToolButton
              variant="primary"
              @click="scanPorts"
              :disabled="isLoading || !host"
            >
              <Scan class="w-4 h-4 mr-1" />
              {{ isLoading ? '扫描中...' : '开始扫描' }}
            </ToolButton>
            <ToolButton variant="secondary" @click="reset">
              <RotateCcw class="w-4 h-4 mr-1" />
              重置
            </ToolButton>
          </div>
        </div>
      </ToolCard>

      <ToolCard v-if="result">
        <div class="flex items-center gap-2 mb-6">
          <CheckCircle2 class="w-6 h-6 text-green-600 dark:text-green-400" />
          <h2 class="text-lg font-semibold text-foreground">扫描结果</h2>
        </div>

        <div class="grid grid-cols-2 md:grid-cols-4 gap-4 mb-6">
          <div class="bg-muted rounded-lg p-4 text-center">
            <div class="text-2xl font-bold text-foreground">{{ result.scannedPorts }}</div>
            <div class="text-sm text-muted-foreground">扫描端口数</div>
          </div>
          <div class="bg-green-50 dark:bg-green-950/30 rounded-lg p-4 text-center">
            <div class="text-2xl font-bold text-green-600 dark:text-green-400">{{ result.openPorts }}</div>
            <div class="text-sm text-muted-foreground">开放端口</div>
          </div>
          <div class="bg-muted rounded-lg p-4 text-center">
            <div class="text-2xl font-bold text-foreground">{{ result.protocol }}</div>
            <div class="text-sm text-muted-foreground">协议</div>
          </div>
          <div class="bg-muted rounded-lg p-4 text-center">
            <div class="text-2xl font-bold text-foreground">{{ result.host }}</div>
            <div class="text-sm text-muted-foreground">目标主机</div>
          </div>
        </div>

        <div v-if="result.ports.length > 0">
          <h3 class="font-semibold text-foreground mb-4">开放端口列表</h3>
          <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-3">
            <div
              v-for="port in result.ports"
              :key="port.port"
              class="flex items-center gap-3 p-4 bg-green-50 dark:bg-green-950/30 rounded-lg border border-green-200 dark:border-green-800"
            >
              <component
                :is="getServiceIcon(port.service)"
                class="w-5 h-5 text-green-600 dark:text-green-400"
              />
              <div class="flex-1">
                <div class="font-semibold text-foreground">{{ port.port }}</div>
                <div class="text-sm text-muted-foreground">{{ port.service }}</div>
              </div>
              <CheckCircle2 class="w-5 h-5 text-green-600 dark:text-green-400" />
            </div>
          </div>
        </div>

        <div v-else class="text-center py-8 text-muted-foreground">
          <Shield class="w-12 h-12 mx-auto mb-4 opacity-50" />
          <p>未发现开放端口</p>
        </div>
      </ToolCard>

      <ToolCard>
        <div class="flex items-center gap-2 mb-4">
          <Info class="w-5 h-5 text-primary" />
          <h3 class="font-semibold text-foreground">常用端口参考</h3>
        </div>
        <div class="grid grid-cols-2 md:grid-cols-4 lg:grid-cols-7 gap-3">
          <div
            v-for="item in commonPorts"
            :key="item.port"
            class="p-3 bg-muted rounded-lg text-center"
          >
            <div class="font-semibold text-foreground">{{ item.port }}</div>
            <div class="text-xs text-muted-foreground">{{ item.service }}</div>
          </div>
        </div>
      </ToolCard>

      <ToolCard>
        <div class="flex items-center gap-2 mb-4">
          <CheckCircle2 class="w-5 h-5 text-primary" />
          <h3 class="font-semibold text-foreground">使用说明</h3>
        </div>
        <div class="space-y-3 text-sm text-muted-foreground">
          <p><strong>扫描模式：</strong>支持扫描常用端口、单个端口或自定义端口范围</p>
          <p><strong>协议选择：</strong>TCP扫描更准确，UDP扫描可能受防火墙影响</p>
          <p><strong>超时设置：</strong>网络环境差时可适当增加超时时间</p>
          <p><strong>并发控制：</strong>扫描大量端口时可调整并发数避免被目标主机限制</p>
          <p><strong>安全提示：</strong>请仅扫描您有权限的目标主机</p>
        </div>
      </ToolCard>
    </div>
  </NuxtLayout>
</template>
