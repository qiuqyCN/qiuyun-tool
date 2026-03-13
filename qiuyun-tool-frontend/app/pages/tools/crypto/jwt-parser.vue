<script setup lang="ts">
import { ref, computed } from 'vue'
import {
  Key,
  Copy,
  Check,
  AlertCircle,
  Clock,
  Shield,
  Code,
  FileJson,
  Trash2,
  RefreshCw
} from 'lucide-vue-next'
import { useToolExecutor } from '~/composables/useToolExecutor'
import { ToolType } from '~/types/tool'

// 响应结果类型
interface JwtHeader {
  rawJson: string
  alg?: string
  typ?: string
  kid?: string
}

interface JwtPayload {
  rawJson: string
  iss?: string
  sub?: string
  aud?: string[]
  exp?: number
  expFormatted?: string
  nbf?: number
  nbfFormatted?: string
  iat?: number
  iatFormatted?: string
  jti?: string
  customClaims?: Record<string, any>
}

interface ParserResult {
  success: boolean
  rawToken: string
  header: JwtHeader
  payload: JwtPayload
  signature: string
  signatureValid: boolean
  expired?: boolean
  expiresIn?: number
  errorMessage?: string
}

// Toast 提示
const toast = ref({
  show: false,
  message: '',
  type: 'success' as 'success' | 'error'
})

const showToast = (message: string, type: 'success' | 'error' = 'success') => {
  toast.value = { show: true, message, type }
  setTimeout(() => toast.value.show = false, 2000)
}

// 状态
const token = ref('')
const activeTab = ref<'decoded' | 'header' | 'payload'>('decoded')
const result = ref<ParserResult | null>(null)

// 使用工具执行器
const { execute, isLoading } = useToolExecutor<{ token: string }, ParserResult>({
  toolCode: 'jwt-parser',
  toolType: ToolType.INSTANT,
  onSuccess: (res) => {
    if (res.success) {
      result.value = res
    } else {
      showToast(res.errorMessage || '解析失败', 'error')
    }
  },
  onError: (err) => {
    showToast(err, 'error')
  }
})

// 解析 JWT
const parseToken = () => {
  if (!token.value.trim()) {
    showToast('请输入 JWT Token', 'error')
    return
  }
  execute({ token: token.value })
}

// 清空
const clearToken = () => {
  token.value = ''
  result.value = null
}

// 复制内容
const copyContent = async (text: string) => {
  try {
    await navigator.clipboard.writeText(text)
    showToast('已复制到剪贴板')
  } catch {
    showToast('复制失败', 'error')
  }
}

// 格式化过期时间
const formatExpiresIn = (seconds: number): string => {
  if (seconds <= 0) return '已过期'
  const days = Math.floor(seconds / 86400)
  const hours = Math.floor((seconds % 86400) / 3600)
  const minutes = Math.floor((seconds % 3600) / 60)
  const secs = seconds % 60
  
  if (days > 0) return `${days}天 ${hours}小时`
  if (hours > 0) return `${hours}小时 ${minutes}分钟`
  if (minutes > 0) return `${minutes}分钟 ${secs}秒`
  return `${secs}秒`
}

// 示例 Token
const loadExample = () => {
  token.value = 'eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyLCJleHAiOjE5MTYyMzkwMjJ9.4Adcj8T9w1aFJj8x8z3x8x8x8x8x8x8x8x8x8x8x8x8'
  parseToken()
}
</script>

<template>
  <NuxtLayout name="tool" tool-code="jwt-parser">
    <div class="border border-border/40 rounded-xl overflow-hidden bg-card">
      <!-- 标题栏 -->
      <div class="border-b bg-muted/30 px-6 py-4">
        <div class="flex items-center gap-2">
          <Key class="w-5 h-5 text-primary" />
          <h2 class="text-lg font-semibold">JWT 解析器</h2>
        </div>
        <p class="text-sm text-muted-foreground mt-1">
          解析 JWT Token，查看 Header、Payload、过期时间等信息
        </p>
      </div>

      <div class="p-6">
        <div class="grid grid-cols-1 lg:grid-cols-2 gap-6">
          <!-- 左侧：输入区 -->
          <div class="space-y-4">
            <!-- Token 输入 -->
            <div>
              <div class="flex items-center justify-between mb-2">
                <label class="text-sm font-medium">JWT Token</label>
                <div class="flex items-center gap-2">
                  <button
                    @click="loadExample"
                    class="text-xs text-primary hover:underline"
                  >
                    加载示例
                  </button>
                  <button
                    v-if="token"
                    @click="clearToken"
                    class="p-1 text-muted-foreground hover:text-red-500"
                    title="清空"
                  >
                    <Trash2 class="w-4 h-4" />
                  </button>
                </div>
              </div>
              <textarea
                v-model="token"
                placeholder="请输入 JWT Token，支持 Bearer 格式&#10;例如：eyJhbGciOiJIUzI1NiIs..."
                class="w-full min-h-[120px] px-3 py-2 border rounded-lg bg-background font-mono text-sm resize-y focus:outline-none focus:ring-2 focus:ring-primary"
                rows="4"
                spellcheck="false"
              />
            </div>

            <!-- 操作按钮 -->
            <div class="flex gap-2">
              <button
                @click="parseToken"
                :disabled="isLoading || !token"
                class="flex-1 flex items-center justify-center gap-2 px-4 py-2 bg-primary text-primary-foreground rounded-lg hover:bg-primary/90 disabled:opacity-50"
              >
                <RefreshCw v-if="isLoading" class="w-4 h-4 animate-spin" />
                <Key v-else class="w-4 h-4" />
                {{ isLoading ? '解析中...' : '解析 Token' }}
              </button>
            </div>

            <!-- 说明 -->
            <div class="bg-muted/30 rounded-lg p-4 text-sm text-muted-foreground">
              <h4 class="font-medium text-foreground mb-2">支持功能</h4>
              <ul class="space-y-1">
                <li>• 解析 JWT 结构（Header.Payload.Signature）</li>
                <li>• 格式化显示 Header 和 Payload</li>
                <li>• 自动检测过期时间</li>
                <li>• 支持 Bearer Token 格式</li>
                <li>• 显示时间戳为可读日期</li>
              </ul>
            </div>
          </div>

          <!-- 右侧：结果区 -->
          <div class="space-y-4">
            <!-- 空状态 -->
            <div v-if="!result" class="flex flex-col items-center justify-center h-full min-h-[400px] text-muted-foreground">
              <Key class="w-16 h-16 mb-4 opacity-30" />
              <p>输入 JWT Token 查看解析结果</p>
            </div>

            <!-- 解析结果 -->
            <div v-else class="space-y-4">
              <!-- 过期状态提示 -->
              <div
                v-if="result.expired !== undefined"
                class="flex items-center gap-3 p-3 rounded-lg"
                :class="result.expired ? 'bg-red-50 text-red-700 border border-red-200' : 'bg-green-50 text-green-700 border border-green-200'"
              >
                <AlertCircle v-if="result.expired" class="w-5 h-5" />
                <Shield v-else class="w-5 h-5" />
                <div class="flex-1">
                  <div class="font-medium">
                    {{ result.expired ? 'Token 已过期' : 'Token 有效' }}
                  </div>
                  <div v-if="result.expiresIn !== undefined" class="text-sm opacity-80">
                    {{ result.expired ? '已过期' : '剩余有效期' }}: {{ formatExpiresIn(result.expiresIn) }}
                  </div>
                </div>
              </div>

              <!-- 标签切换 -->
              <div class="flex border-b">
                <button
                  @click="activeTab = 'decoded'"
                  class="px-4 py-2 text-sm font-medium border-b-2 transition-colors"
                  :class="activeTab === 'decoded' ? 'border-primary text-primary' : 'border-transparent hover:text-foreground'"
                >
                  解析结果
                </button>
                <button
                  @click="activeTab = 'header'"
                  class="px-4 py-2 text-sm font-medium border-b-2 transition-colors"
                  :class="activeTab === 'header' ? 'border-primary text-primary' : 'border-transparent hover:text-foreground'"
                >
                  Header
                </button>
                <button
                  @click="activeTab = 'payload'"
                  class="px-4 py-2 text-sm font-medium border-b-2 transition-colors"
                  :class="activeTab === 'payload' ? 'border-primary text-primary' : 'border-transparent hover:text-foreground'"
                >
                  Payload
                </button>
              </div>

              <!-- 解析结果视图 -->
              <div v-if="activeTab === 'decoded'" class="space-y-4">
                <!-- Header 信息 -->
                <div class="bg-muted/30 rounded-lg p-4">
                  <div class="flex items-center gap-2 mb-3">
                    <Code class="w-4 h-4 text-primary" />
                    <span class="font-medium">Header</span>
                  </div>
                  <div class="space-y-2 text-sm">
                    <div class="flex justify-between">
                      <span class="text-muted-foreground">算法 (alg)</span>
                      <span class="font-mono">{{ result.header.alg || '-' }}</span>
                    </div>
                    <div class="flex justify-between">
                      <span class="text-muted-foreground">类型 (typ)</span>
                      <span class="font-mono">{{ result.header.typ || '-' }}</span>
                    </div>
                    <div v-if="result.header.kid" class="flex justify-between">
                      <span class="text-muted-foreground">密钥 ID (kid)</span>
                      <span class="font-mono">{{ result.header.kid }}</span>
                    </div>
                  </div>
                </div>

                <!-- Payload 信息 -->
                <div class="bg-muted/30 rounded-lg p-4">
                  <div class="flex items-center gap-2 mb-3">
                    <FileJson class="w-4 h-4 text-primary" />
                    <span class="font-medium">Payload</span>
                  </div>
                  <div class="space-y-2 text-sm">
                    <div v-if="result.payload.iss" class="flex justify-between">
                      <span class="text-muted-foreground">签发者 (iss)</span>
                      <span class="font-mono">{{ result.payload.iss }}</span>
                    </div>
                    <div v-if="result.payload.sub" class="flex justify-between">
                      <span class="text-muted-foreground">主题 (sub)</span>
                      <span class="font-mono">{{ result.payload.sub }}</span>
                    </div>
                    <div v-if="result.payload.aud?.length" class="flex justify-between">
                      <span class="text-muted-foreground">受众 (aud)</span>
                      <span class="font-mono">{{ result.payload.aud.join(', ') }}</span>
                    </div>
                    <div v-if="result.payload.exp" class="flex justify-between">
                      <span class="text-muted-foreground">过期时间 (exp)</span>
                      <span class="font-mono">{{ result.payload.expFormatted }}</span>
                    </div>
                    <div v-if="result.payload.iat" class="flex justify-between">
                      <span class="text-muted-foreground">签发时间 (iat)</span>
                      <span class="font-mono">{{ result.payload.iatFormatted }}</span>
                    </div>
                    <div v-if="result.payload.nbf" class="flex justify-between">
                      <span class="text-muted-foreground">生效时间 (nbf)</span>
                      <span class="font-mono">{{ result.payload.nbfFormatted }}</span>
                    </div>
                    <div v-if="result.payload.jti" class="flex justify-between">
                      <span class="text-muted-foreground">JWT ID (jti)</span>
                      <span class="font-mono">{{ result.payload.jti }}</span>
                    </div>
                  </div>
                </div>

                <!-- 自定义声明 -->
                <div v-if="result.payload.customClaims && Object.keys(result.payload.customClaims).length > 0" class="bg-muted/30 rounded-lg p-4">
                  <div class="flex items-center gap-2 mb-3">
                    <Clock class="w-4 h-4 text-primary" />
                    <span class="font-medium">自定义声明</span>
                  </div>
                  <div class="space-y-2 text-sm">
                    <div
                      v-for="(value, key) in result.payload.customClaims"
                      :key="key"
                      class="flex gap-4"
                    >
                      <span class="text-muted-foreground shrink-0">{{ key }}</span>
                      <span class="font-mono break-all text-right flex-1">{{ typeof value === 'object' ? JSON.stringify(value) : value }}</span>
                    </div>
                  </div>
                </div>

                <!-- Signature -->
                <div class="bg-muted/30 rounded-lg p-4">
                  <div class="flex items-center gap-2 mb-3">
                    <Shield class="w-4 h-4 text-primary" />
                    <span class="font-medium">Signature</span>
                  </div>
                  <div class="text-sm text-muted-foreground">
                    <p>签名用于验证 Token 的完整性</p>
                    <p class="mt-1">签名状态: {{ result.signatureValid ? '✓ 有效' : '✗ 无效' }}</p>
                  </div>
                </div>
              </div>

              <!-- Header JSON 视图 -->
              <div v-else-if="activeTab === 'header'" class="bg-muted/30 rounded-lg p-4">
                <div class="flex items-center justify-between mb-2">
                  <span class="font-medium">Header JSON</span>
                  <button
                    @click="copyContent(result.header.rawJson)"
                    class="p-1.5 hover:bg-muted rounded"
                    title="复制"
                  >
                    <Copy class="w-4 h-4" />
                  </button>
                </div>
                <pre class="text-sm font-mono bg-background p-3 rounded border overflow-auto">{{ JSON.stringify(JSON.parse(result.header.rawJson), null, 2) }}</pre>
              </div>

              <!-- Payload JSON 视图 -->
              <div v-else-if="activeTab === 'payload'" class="bg-muted/30 rounded-lg p-4">
                <div class="flex items-center justify-between mb-2">
                  <span class="font-medium">Payload JSON</span>
                  <button
                    @click="copyContent(result.payload.rawJson)"
                    class="p-1.5 hover:bg-muted rounded"
                    title="复制"
                  >
                    <Copy class="w-4 h-4" />
                  </button>
                </div>
                <pre class="text-sm font-mono bg-background p-3 rounded border overflow-auto">{{ JSON.stringify(JSON.parse(result.payload.rawJson), null, 2) }}</pre>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- Toast 提示 -->
    <Transition
      enter-active-class="transition duration-300 ease-out"
      enter-from-class="transform translate-y-2 opacity-0"
      enter-to-class="transform translate-y-0 opacity-100"
      leave-active-class="transition duration-200 ease-in"
      leave-from-class="transform translate-y-0 opacity-100"
      leave-to-class="transform translate-y-2 opacity-0"
    >
      <div
        v-if="toast.show"
        class="fixed bottom-8 left-1/2 -translate-x-1/2 z-50 flex items-center gap-2 px-4 py-2.5 rounded-lg shadow-lg"
        :class="toast.type === 'success' ? 'bg-foreground text-background' : 'bg-red-500 text-white'"
      >
        <Check v-if="toast.type === 'success'" class="w-4 h-4" />
        <span class="text-sm font-medium">{{ toast.message }}</span>
      </div>
    </Transition>
  </NuxtLayout>
</template>
