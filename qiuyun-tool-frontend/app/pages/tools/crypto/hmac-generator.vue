<script setup lang="ts">
import { ref, computed } from 'vue'
import {
  Hash,
  Copy,
  Check,
  RefreshCw,
  Key,
  FileText,
  Shield
} from 'lucide-vue-next'
import { useToolExecutor } from '~/composables/useToolExecutor'
import { ToolType } from '~/types/tool'

// 响应结果类型
interface GeneratorResult {
  success: boolean
  algorithm: string
  outputFormat: string
  hmac: string
  messageLength: number
  secretLength: number
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
const message = ref('')
const secret = ref('')
const algorithm = ref('HmacSHA256')
const outputFormat = ref('hex')
const showSecret = ref(false)
const result = ref<GeneratorResult | null>(null)

// 算法选项
const algorithms = [
  { value: 'HmacSHA256', label: 'HMAC-SHA256', desc: '推荐，安全性高', recommended: true },
  { value: 'HmacSHA1', label: 'HMAC-SHA1', desc: '兼容性较好', recommended: false },
  { value: 'HmacSHA384', label: 'HMAC-SHA384', desc: '高安全性', recommended: false },
  { value: 'HmacSHA512', label: 'HMAC-SHA512', desc: '最高安全性', recommended: false },
  { value: 'HmacMD5', label: 'HMAC-MD5', desc: '不推荐，已不安全', deprecated: true }
]

const outputFormats = [
  { value: 'hex', label: '十六进制', desc: '小写十六进制字符串' },
  { value: 'base64', label: 'Base64', desc: 'Base64 编码' }
]

// 使用工具执行器
const { execute, isLoading } = useToolExecutor<{
  message: string
  secret: string
  algorithm: string
  outputFormat: string
}, GeneratorResult>({
  toolCode: 'hmac-generator',
  toolType: ToolType.INSTANT,
  onSuccess: (res) => {
    if (res.success) {
      result.value = res
      showToast('HMAC 生成成功')
    } else {
      showToast(res.errorMessage || '生成失败', 'error')
    }
  },
  onError: (err) => {
    showToast(err, 'error')
  }
})

// 生成 HMAC
const generateHmac = () => {
  if (!message.value.trim()) {
    showToast('请输入消息内容', 'error')
    return
  }
  if (!secret.value) {
    showToast('请输入密钥', 'error')
    return
  }
  
  execute({
    message: message.value,
    secret: secret.value,
    algorithm: algorithm.value,
    outputFormat: outputFormat.value
  })
}

// 复制结果
const copyResult = async () => {
  if (!result.value) return
  try {
    await navigator.clipboard.writeText(result.value.hmac)
    showToast('已复制到剪贴板')
  } catch {
    showToast('复制失败', 'error')
  }
}

// 清空
const clearAll = () => {
  message.value = ''
  secret.value = ''
  result.value = null
}

// 计算 HMAC 长度
const hmacLength = computed(() => {
  if (!result.value) return 0
  return result.value.hmac.length
})
</script>

<template>
  <NuxtLayout name="tool" tool-code="hmac-generator">
    <div class="border border-border/40 rounded-xl overflow-hidden bg-card">
      <!-- 标题栏 -->
      <div class="border-b bg-muted/30 px-6 py-4">
        <div class="flex items-center gap-2">
          <Hash class="w-5 h-5 text-primary" />
          <h2 class="text-lg font-semibold">HMAC 生成器</h2>
        </div>
        <p class="text-sm text-muted-foreground mt-1">
          生成 HMAC 消息认证码，支持多种哈希算法
        </p>
      </div>

      <div class="p-6">
        <div class="grid grid-cols-1 lg:grid-cols-2 gap-6">
          <!-- 左侧：输入区 -->
          <div class="space-y-4">
            <!-- 消息内容 -->
            <div>
              <label class="text-sm font-medium mb-2 flex items-center gap-2">
                <FileText class="w-4 h-4" />
                消息内容
              </label>
              <textarea
                v-model="message"
                placeholder="输入要计算 HMAC 的消息内容..."
                class="w-full min-h-[100px] px-3 py-2 border rounded-lg bg-background font-mono text-sm resize-y focus:outline-none focus:ring-2 focus:ring-primary"
                rows="4"
                spellcheck="false"
              />
              <div class="text-xs text-muted-foreground mt-1 text-right">
                {{ message.length }} 字符
              </div>
            </div>

            <!-- 密钥 -->
            <div>
              <label class="text-sm font-medium mb-2 flex items-center gap-2">
                <Key class="w-4 h-4" />
                密钥 (Secret Key)
              </label>
              <div class="relative">
                <input
                  v-model="secret"
                  :type="showSecret ? 'text' : 'password'"
                  placeholder="输入密钥..."
                  class="w-full px-3 py-2 pr-10 border rounded-lg bg-background font-mono text-sm focus:outline-none focus:ring-2 focus:ring-primary"
                />
                <button
                  @click="showSecret = !showSecret"
                  class="absolute right-3 top-1/2 -translate-y-1/2 text-muted-foreground hover:text-foreground"
                >
                  {{ showSecret ? '🙈' : '👁️' }}
                </button>
              </div>
              <div class="text-xs text-muted-foreground mt-1 text-right">
                {{ secret.length }} 字符
              </div>
            </div>

            <!-- 算法选择 -->
            <div>
              <label class="text-sm font-medium mb-2 block">哈希算法</label>
              <div class="space-y-2">
                <button
                  v-for="algo in algorithms"
                  :key="algo.value"
                  @click="algorithm = algo.value"
                  class="w-full p-3 rounded-lg border text-left transition-colors flex items-center justify-between"
                  :class="[
                    algorithm === algo.value ? 'border-primary bg-primary/5' : 'border-border hover:border-primary/50',
                    algo.deprecated ? 'opacity-60' : ''
                  ]"
                >
                  <div>
                    <div class="font-medium flex items-center gap-2">
                      {{ algo.label }}
                      <span v-if="algo.recommended" class="text-xs bg-green-100 text-green-700 px-1.5 py-0.5 rounded">推荐</span>
                      <span v-if="algo.deprecated" class="text-xs bg-red-100 text-red-700 px-1.5 py-0.5 rounded">不推荐</span>
                    </div>
                    <div class="text-xs text-muted-foreground">{{ algo.desc }}</div>
                  </div>
                </button>
              </div>
            </div>

            <!-- 输出格式 -->
            <div>
              <label class="text-sm font-medium mb-2 block">输出格式</label>
              <div class="grid grid-cols-2 gap-2">
                <button
                  v-for="fmt in outputFormats"
                  :key="fmt.value"
                  @click="outputFormat = fmt.value"
                  class="p-3 rounded-lg border text-left transition-colors"
                  :class="outputFormat === fmt.value ? 'border-primary bg-primary/5' : 'border-border hover:border-primary/50'"
                >
                  <div class="font-medium">{{ fmt.label }}</div>
                  <div class="text-xs text-muted-foreground">{{ fmt.desc }}</div>
                </button>
              </div>
            </div>

            <!-- 操作按钮 -->
            <div class="flex gap-2">
              <button
                @click="generateHmac"
                :disabled="isLoading"
                class="flex-1 flex items-center justify-center gap-2 px-4 py-2 bg-primary text-primary-foreground rounded-lg hover:bg-primary/90 disabled:opacity-50"
              >
                <RefreshCw v-if="isLoading" class="w-4 h-4 animate-spin" />
                <Hash v-else class="w-4 h-4" />
                {{ isLoading ? '计算中...' : '生成 HMAC' }}
              </button>
              <button
                @click="clearAll"
                class="px-4 py-2 border rounded-lg hover:bg-muted"
              >
                清空
              </button>
            </div>
          </div>

          <!-- 右侧：结果区 -->
          <div class="space-y-4">
            <!-- 空状态 -->
            <div v-if="!result" class="flex flex-col items-center justify-center h-full min-h-[400px] text-muted-foreground">
              <Hash class="w-16 h-16 mb-4 opacity-30" />
              <p>输入消息和密钥生成 HMAC</p>
            </div>

            <!-- 结果 -->
            <div v-else class="space-y-4">
              <!-- HMAC 结果 -->
              <div class="bg-muted/30 rounded-lg p-4">
                <div class="flex items-center justify-between mb-3">
                  <span class="font-medium flex items-center gap-2">
                    <Shield class="w-4 h-4 text-primary" />
                    HMAC 结果
                  </span>
                  <button
                    @click="copyResult"
                    class="flex items-center gap-1 px-3 py-1.5 text-sm border rounded-lg hover:bg-muted"
                  >
                    <Copy class="w-4 h-4" />
                    复制
                  </button>
                </div>
                <div class="p-4 bg-background rounded-lg border">
                  <code class="font-mono text-sm break-all">{{ result.hmac }}</code>
                </div>
                <div class="text-xs text-muted-foreground mt-2">
                  长度: {{ hmacLength }} 字符
                </div>
              </div>

              <!-- 计算信息 -->
              <div class="grid grid-cols-2 gap-4">
                <div class="bg-muted/30 rounded-lg p-4">
                  <div class="text-sm text-muted-foreground mb-1">算法</div>
                  <div class="font-medium">{{ result.algorithm.replace('Hmac', 'HMAC-') }}</div>
                </div>
                <div class="bg-muted/30 rounded-lg p-4">
                  <div class="text-sm text-muted-foreground mb-1">输出格式</div>
                  <div class="font-medium">{{ result.outputFormat === 'hex' ? '十六进制' : 'Base64' }}</div>
                </div>
                <div class="bg-muted/30 rounded-lg p-4">
                  <div class="text-sm text-muted-foreground mb-1">消息长度</div>
                  <div class="font-medium">{{ result.messageLength }} 字符</div>
                </div>
                <div class="bg-muted/30 rounded-lg p-4">
                  <div class="text-sm text-muted-foreground mb-1">密钥长度</div>
                  <div class="font-medium">{{ result.secretLength }} 字符</div>
                </div>
              </div>

              <!-- 说明 -->
              <div class="bg-muted/30 rounded-lg p-4 text-sm text-muted-foreground">
                <h4 class="font-medium text-foreground mb-2">什么是 HMAC？</h4>
                <p class="mb-2">
                  HMAC（Hash-based Message Authentication Code）是一种基于哈希函数的消息认证码算法，
                  用于验证消息的完整性和真实性。
                </p>
                <p>
                  它结合了哈希函数和密钥，确保只有拥有正确密钥的人才能生成有效的认证码。
                </p>
              </div>

              <!-- 使用场景 -->
              <div class="bg-muted/30 rounded-lg p-4 text-sm text-muted-foreground">
                <h4 class="font-medium text-foreground mb-2">常见使用场景</h4>
                <ul class="space-y-1">
                  <li>• API 请求签名验证</li>
                  <li>• Webhook 回调验证</li>
                  <li>• 数据完整性校验</li>
                  <li>• JWT 签名（HS256/HS512）</li>
                </ul>
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
