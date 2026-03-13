<script setup lang="ts">
import { ref } from 'vue'
import {
  Key,
  Copy,
  Check,
  RefreshCw,
  Download,
  Shield,
  Fingerprint
} from 'lucide-vue-next'
import { useToolExecutor } from '~/composables/useToolExecutor'
import { ToolType } from '~/types/tool'

// 响应结果类型
interface GeneratorResult {
  success: boolean
  keySize: number
  format: string
  encoding: string
  publicKey: string
  privateKey: string
  fingerprint: string
  passwordProtected: boolean
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
const keySize = ref(2048)
const format = ref('PKCS8')
const encoding = ref('PEM')
const password = ref('')
const showPassword = ref(false)
const result = ref<GeneratorResult | null>(null)
const activeKeyTab = ref<'public' | 'private'>('public')

// 选项
const keySizes = [
  { value: 1024, label: '1024 bit', warning: '不推荐，安全性较低' },
  { value: 2048, label: '2048 bit', warning: '推荐，平衡安全与性能' },
  { value: 3072, label: '3072 bit', warning: '高安全性' },
  { value: 4096, label: '4096 bit', warning: '最高安全性，生成较慢' }
]

const formats = [
  { value: 'PKCS8', label: 'PKCS#8', desc: '现代标准格式' },
  { value: 'PKCS1', label: 'PKCS#1', desc: '传统 RSA 格式' }
]

const encodings = [
  { value: 'PEM', label: 'PEM', desc: 'Base64 编码，带头部' },
  { value: 'DER', label: 'DER', desc: '二进制编码' }
]

// 使用工具执行器
const { execute, isLoading } = useToolExecutor<{
  keySize: number
  format: string
  encoding: string
  password?: string
}, GeneratorResult>({
  toolCode: 'rsa-key-generator',
  toolType: ToolType.INSTANT,
  onSuccess: (res) => {
    if (res.success) {
      result.value = res
      showToast('密钥生成成功')
    } else {
      showToast(res.errorMessage || '生成失败', 'error')
    }
  },
  onError: (err) => {
    showToast(err, 'error')
  }
})

// 生成密钥
const generateKeys = () => {
  execute({
    keySize: keySize.value,
    format: format.value,
    encoding: encoding.value,
    password: password.value || undefined
  })
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

// 下载密钥
const downloadKey = (type: 'public' | 'private') => {
  if (!result.value) return
  
  const content = type === 'public' ? result.value.publicKey : result.value.privateKey
  const filename = type === 'public' 
    ? `rsa_public_${result.value.keySize}.pem`
    : `rsa_private_${result.value.keySize}.pem`
  
  const blob = new Blob([content], { type: 'text/plain' })
  const url = URL.createObjectURL(blob)
  const link = document.createElement('a')
  link.href = url
  link.download = filename
  link.click()
  URL.revokeObjectURL(url)
  
  showToast(`${type === 'public' ? '公钥' : '私钥'}已下载`)
}
</script>

<template>
  <NuxtLayout name="tool" tool-code="rsa-key-generator">
    <div class="border border-border/40 rounded-xl overflow-hidden bg-card">
      <!-- 标题栏 -->
      <div class="border-b bg-muted/30 px-6 py-4">
        <div class="flex items-center gap-2">
          <Key class="w-5 h-5 text-primary" />
          <h2 class="text-lg font-semibold">RSA 密钥生成器</h2>
        </div>
        <p class="text-sm text-muted-foreground mt-1">
          生成 RSA 公私钥对，支持多种密钥长度和格式
        </p>
      </div>

      <div class="p-6">
        <div class="grid grid-cols-1 lg:grid-cols-3 gap-6">
          <!-- 左侧：配置区 -->
          <div class="space-y-6">
            <!-- 密钥长度 -->
            <div>
              <label class="text-sm font-medium mb-3 block">密钥长度</label>
              <div class="space-y-2">
                <label
                  v-for="size in keySizes"
                  :key="size.value"
                  class="flex items-start gap-3 p-3 rounded-lg border cursor-pointer transition-colors"
                  :class="keySize === size.value ? 'border-primary bg-primary/5' : 'border-border hover:border-primary/50'"
                >
                  <input
                    v-model="keySize"
                    type="radio"
                    :value="size.value"
                    class="mt-1"
                  />
                  <div class="flex-1">
                    <div class="font-medium">{{ size.label }}</div>
                    <div class="text-xs text-muted-foreground">{{ size.warning }}</div>
                  </div>
                </label>
              </div>
            </div>

            <!-- 密钥格式 -->
            <div>
              <label class="text-sm font-medium mb-3 block">密钥格式</label>
              <div class="grid grid-cols-2 gap-2">
                <button
                  v-for="fmt in formats"
                  :key="fmt.value"
                  @click="format = fmt.value"
                  class="p-3 rounded-lg border text-left transition-colors"
                  :class="format === fmt.value ? 'border-primary bg-primary/5' : 'border-border hover:border-primary/50'"
                >
                  <div class="font-medium">{{ fmt.label }}</div>
                  <div class="text-xs text-muted-foreground">{{ fmt.desc }}</div>
                </button>
              </div>
            </div>

            <!-- 编码格式 -->
            <div>
              <label class="text-sm font-medium mb-3 block">编码格式</label>
              <div class="grid grid-cols-2 gap-2">
                <button
                  v-for="enc in encodings"
                  :key="enc.value"
                  @click="encoding = enc.value"
                  class="p-3 rounded-lg border text-left transition-colors"
                  :class="encoding === enc.value ? 'border-primary bg-primary/5' : 'border-border hover:border-primary/50'"
                >
                  <div class="font-medium">{{ enc.label }}</div>
                  <div class="text-xs text-muted-foreground">{{ enc.desc }}</div>
                </button>
              </div>
            </div>

            <!-- 密码保护 -->
            <div>
              <label class="text-sm font-medium mb-2 block">私钥密码保护（可选）</label>
              <div class="relative">
                <input
                  v-model="password"
                  :type="showPassword ? 'text' : 'password'"
                  placeholder="输入密码保护私钥"
                  class="w-full px-3 py-2 pr-10 border rounded-lg bg-background focus:outline-none focus:ring-2 focus:ring-primary"
                />
                <button
                  @click="showPassword = !showPassword"
                  class="absolute right-3 top-1/2 -translate-y-1/2 text-muted-foreground hover:text-foreground"
                >
                  {{ showPassword ? '🙈' : '👁️' }}
                </button>
              </div>
              <p class="text-xs text-muted-foreground mt-1">设置密码后私钥将被加密</p>
            </div>

            <!-- 生成按钮 -->
            <button
              @click="generateKeys"
              :disabled="isLoading"
              class="w-full flex items-center justify-center gap-2 px-4 py-2 bg-primary text-primary-foreground rounded-lg hover:bg-primary/90 disabled:opacity-50"
            >
              <RefreshCw v-if="isLoading" class="w-4 h-4 animate-spin" />
              <Key v-else class="w-4 h-4" />
              {{ isLoading ? '生成中...' : '生成密钥对' }}
            </button>

            <!-- 说明 -->
            <div class="bg-muted/30 rounded-lg p-4 text-sm text-muted-foreground">
              <h4 class="font-medium text-foreground mb-2">使用说明</h4>
              <ul class="space-y-1">
                <li>• 2048 bit 是目前的推荐标准</li>
                <li>• PKCS#8 是现代标准，推荐优先使用</li>
                <li>• PEM 格式便于复制和存储</li>
                <li>• 请妥善保管私钥，不要泄露给他人</li>
              </ul>
            </div>
          </div>

          <!-- 右侧：结果区 -->
          <div class="lg:col-span-2 space-y-4">
            <!-- 空状态 -->
            <div v-if="!result" class="flex flex-col items-center justify-center h-full min-h-[400px] text-muted-foreground">
              <Key class="w-16 h-16 mb-4 opacity-30" />
              <p>点击左侧按钮生成 RSA 密钥对</p>
            </div>

            <!-- 密钥信息 -->
            <div v-else class="space-y-4">
              <!-- 指纹信息 -->
              <div class="flex items-center gap-4 p-4 bg-muted/30 rounded-lg">
                <Fingerprint class="w-5 h-5 text-primary" />
                <div class="flex-1">
                  <div class="text-sm text-muted-foreground">密钥指纹 (SHA-256)</div>
                  <div class="font-mono text-sm break-all">{{ result.fingerprint }}</div>
                </div>
                <button
                  @click="copyContent(result.fingerprint)"
                  class="p-2 hover:bg-muted rounded-lg"
                  title="复制"
                >
                  <Copy class="w-4 h-4" />
                </button>
              </div>

              <!-- 密钥标签 -->
              <div class="flex border-b">
                <button
                  @click="activeKeyTab = 'public'"
                  class="px-4 py-2 text-sm font-medium border-b-2 transition-colors flex items-center gap-2"
                  :class="activeKeyTab === 'public' ? 'border-primary text-primary' : 'border-transparent hover:text-foreground'"
                >
                  <Shield class="w-4 h-4" />
                  公钥
                </button>
                <button
                  @click="activeKeyTab = 'private'"
                  class="px-4 py-2 text-sm font-medium border-b-2 transition-colors flex items-center gap-2"
                  :class="activeKeyTab === 'private' ? 'border-primary text-primary' : 'border-transparent hover:text-foreground'"
                >
                  <Key class="w-4 h-4" />
                  私钥
                  <span v-if="result.passwordProtected" class="text-xs bg-amber-100 text-amber-700 px-1.5 py-0.5 rounded">已加密</span>
                </button>
              </div>

              <!-- 公钥显示 -->
              <div v-if="activeKeyTab === 'public'" class="space-y-3">
                <div class="flex items-center justify-between">
                  <span class="text-sm font-medium">公钥 (Public Key)</span>
                  <div class="flex gap-2">
                    <button
                      @click="copyContent(result.publicKey)"
                      class="flex items-center gap-1 px-3 py-1.5 text-sm border rounded-lg hover:bg-muted"
                    >
                      <Copy class="w-4 h-4" />
                      复制
                    </button>
                    <button
                      @click="downloadKey('public')"
                      class="flex items-center gap-1 px-3 py-1.5 text-sm bg-primary text-primary-foreground rounded-lg hover:bg-primary/90"
                    >
                      <Download class="w-4 h-4" />
                      下载
                    </button>
                  </div>
                </div>
                <pre class="p-4 bg-muted/30 rounded-lg font-mono text-sm overflow-auto max-h-[400px]">{{ result.publicKey }}</pre>
              </div>

              <!-- 私钥显示 -->
              <div v-else class="space-y-3">
                <div class="flex items-center justify-between">
                  <span class="text-sm font-medium flex items-center gap-2">
                    私钥 (Private Key)
                    <span class="text-xs text-red-500">⚠️ 请妥善保管，不要泄露</span>
                  </span>
                  <div class="flex gap-2">
                    <button
                      @click="copyContent(result.privateKey)"
                      class="flex items-center gap-1 px-3 py-1.5 text-sm border rounded-lg hover:bg-muted"
                    >
                      <Copy class="w-4 h-4" />
                      复制
                    </button>
                    <button
                      @click="downloadKey('private')"
                      class="flex items-center gap-1 px-3 py-1.5 text-sm bg-primary text-primary-foreground rounded-lg hover:bg-primary/90"
                    >
                      <Download class="w-4 h-4" />
                      下载
                    </button>
                  </div>
                </div>
                <pre class="p-4 bg-red-50 border border-red-200 rounded-lg font-mono text-sm overflow-auto max-h-[400px]">{{ result.privateKey }}</pre>
              </div>

              <!-- 密钥信息摘要 -->
              <div class="grid grid-cols-4 gap-4 p-4 bg-muted/30 rounded-lg text-sm">
                <div>
                  <span class="text-muted-foreground">密钥长度</span>
                  <div class="font-medium">{{ result.keySize }} bit</div>
                </div>
                <div>
                  <span class="text-muted-foreground">格式</span>
                  <div class="font-medium">{{ result.format }}</div>
                </div>
                <div>
                  <span class="text-muted-foreground">编码</span>
                  <div class="font-medium">{{ result.encoding }}</div>
                </div>
                <div>
                  <span class="text-muted-foreground">密码保护</span>
                  <div class="font-medium">{{ result.passwordProtected ? '是' : '否' }}</div>
                </div>
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
