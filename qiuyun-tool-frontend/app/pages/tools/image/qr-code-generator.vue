<script setup lang="ts">
import { ref, computed, watch } from 'vue'
import {
  QrCode,
  Download,
  Copy,
  Check,
  Upload,
  Trash2,
  Wifi,
  Mail,
  Phone,
  MessageSquare,
  Link,
  Type,
  Settings,
  RefreshCw
} from 'lucide-vue-next'
import { useToolExecutor } from '~/composables/useToolExecutor'
import { useDebounce } from '~/composables/useDebounce'
import { ToolType } from '~/types/tool'

// 内容类型
enum ContentType {
  TEXT = 'text',
  URL = 'url',
  WIFI = 'wifi',
  EMAIL = 'email',
  PHONE = 'phone',
  SMS = 'sms'
}

// 纠错级别
enum ErrorCorrection {
  L = 'L',
  M = 'M',
  Q = 'Q',
  H = 'H'
}

// WiFi 加密类型
enum WifiEncryption {
  WPA = 'WPA',
  WEP = 'WEP',
  NOPASS = 'nopass'
}

// 请求参数
interface GeneratorParams {
  content: string
  contentType: ContentType
  size: number
  errorCorrection: ErrorCorrection
  logoBase64?: string
  // WiFi 字段
  wifiSsid?: string
  wifiPassword?: string
  wifiEncryption?: WifiEncryption
  wifiHidden?: boolean
  // 邮件字段
  emailSubject?: string
  emailBody?: string
  // 短信字段
  smsBody?: string
}

// 响应结果
interface GeneratorResult {
  success: boolean
  qrCodeBase64: string
  size: number
  contentType: string
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
const contentType = ref<ContentType>(ContentType.URL)
const content = ref('')
const size = ref(300)
const errorCorrection = ref<ErrorCorrection>(ErrorCorrection.M)
const qrCodeBase64 = ref('')
const logoBase64 = ref('')

// WiFi 字段
const wifiSsid = ref('')
const wifiPassword = ref('')
const wifiEncryption = ref<WifiEncryption>(WifiEncryption.WPA)
const wifiHidden = ref(false)

// 邮件字段
const emailSubject = ref('')
const emailBody = ref('')

// 短信字段
const smsBody = ref('')

// 内容类型选项
const contentTypeOptions = [
  { value: ContentType.URL, label: '网址', icon: Link },
  { value: ContentType.TEXT, label: '文本', icon: Type },
  { value: ContentType.WIFI, label: 'WiFi', icon: Wifi },
  { value: ContentType.EMAIL, label: '邮箱', icon: Mail },
  { value: ContentType.PHONE, label: '电话', icon: Phone },
  { value: ContentType.SMS, label: '短信', icon: MessageSquare }
]

// 纠错级别选项
const errorCorrectionOptions = [
  { value: ErrorCorrection.L, label: '低 (~7%)', description: '适合干净的扫描环境' },
  { value: ErrorCorrection.M, label: '中 (~15%)', description: '推荐，平衡质量和容错' },
  { value: ErrorCorrection.Q, label: '高 (~25%)', description: '适合有一定损坏的码' },
  { value: ErrorCorrection.H, label: '最高 (~30%)', description: '适合添加Logo的码' }
]

// 获取当前内容
const getCurrentContent = () => {
  switch (contentType.value) {
    case ContentType.WIFI:
      return wifiSsid.value
    case ContentType.EMAIL:
      return content.value
    case ContentType.PHONE:
      return content.value
    case ContentType.SMS:
      return content.value
    default:
      return content.value
  }
}

// 构建请求参数
const buildParams = (): GeneratorParams => {
  const params: GeneratorParams = {
    content: getCurrentContent(),
    contentType: contentType.value,
    size: size.value,
    errorCorrection: errorCorrection.value
  }

  if (logoBase64.value) {
    params.logoBase64 = logoBase64.value
  }

  if (contentType.value === ContentType.WIFI) {
    params.wifiSsid = wifiSsid.value
    params.wifiPassword = wifiPassword.value
    params.wifiEncryption = wifiEncryption.value
    params.wifiHidden = wifiHidden.value
  }

  if (contentType.value === ContentType.EMAIL) {
    params.emailSubject = emailSubject.value
    params.emailBody = emailBody.value
  }

  if (contentType.value === ContentType.SMS) {
    params.smsBody = smsBody.value
  }

  return params
}

// 使用工具执行器
const { execute, isLoading } = useToolExecutor<GeneratorParams, GeneratorResult>({
  toolCode: 'qr-code-generator',
  toolType: ToolType.INSTANT,
  onSuccess: (result) => {
    if (result.success) {
      qrCodeBase64.value = result.qrCodeBase64
    } else {
      showToast(result.errorMessage || '生成失败', 'error')
    }
  },
  onError: (err) => {
    showToast(err, 'error')
  }
})

// 防抖生成
const { debouncedFn: debouncedGenerate } = useDebounce(
  () => {
    if (canGenerate.value) {
      execute(buildParams())
    }
  },
  { delay: 500 }
)

// 是否可以生成
const canGenerate = computed(() => {
  if (contentType.value === ContentType.WIFI) {
    return wifiSsid.value.trim() !== ''
  }
  return content.value.trim() !== ''
})

// 监听变化自动生成
watch([content, wifiSsid, wifiPassword, size, errorCorrection, logoBase64], () => {
  debouncedGenerate()
}, { deep: true })

// 切换内容类型时清空
watch(contentType, () => {
  content.value = ''
  wifiSsid.value = ''
  wifiPassword.value = ''
  emailSubject.value = ''
  emailBody.value = ''
  smsBody.value = ''
  qrCodeBase64.value = ''
})

// 手动生成
const generate = () => {
  if (!canGenerate.value) {
    showToast('请输入内容', 'error')
    return
  }
  execute(buildParams())
}

// 下载二维码
const downloadQRCode = () => {
  if (!qrCodeBase64.value) return
  const link = document.createElement('a')
  link.href = qrCodeBase64.value
  link.download = `qrcode-${Date.now()}.png`
  link.click()
  showToast('已下载')
}

// 复制二维码
const copyQRCode = async () => {
  if (!qrCodeBase64.value) return
  try {
    const response = await fetch(qrCodeBase64.value)
    const blob = await response.blob()
    await navigator.clipboard.write([
      new ClipboardItem({ 'image/png': blob })
    ])
    showToast('已复制到剪贴板')
  } catch {
    showToast('复制失败', 'error')
  }
}

// 上传Logo
const handleLogoUpload = (event: Event) => {
  const file = (event.target as HTMLInputElement).files?.[0]
  if (!file) return

  if (!file.type.startsWith('image/')) {
    showToast('请上传图片文件', 'error')
    return
  }

  const reader = new FileReader()
  reader.onload = (e) => {
    logoBase64.value = e.target?.result as string
    showToast('Logo 已添加')
  }
  reader.readAsDataURL(file)
}

// 移除Logo
const removeLogo = () => {
  logoBase64.value = ''
}

// 清空所有
const clearAll = () => {
  content.value = ''
  wifiSsid.value = ''
  wifiPassword.value = ''
  emailSubject.value = ''
  emailBody.value = ''
  smsBody.value = ''
  logoBase64.value = ''
  qrCodeBase64.value = ''
}
</script>

<template>
  <NuxtLayout name="tool" tool-code="qr-code-generator">
    <div class="border border-border/40 rounded-xl overflow-hidden bg-card">
      <!-- 标题栏 -->
      <div class="border-b bg-muted/30 px-6 py-4">
        <div class="flex items-center gap-2">
          <QrCode class="w-5 h-5 text-primary" />
          <h2 class="text-lg font-semibold">二维码生成</h2>
        </div>
        <p class="text-sm text-muted-foreground mt-1">
          生成各种类型的二维码，支持自定义样式和 Logo
        </p>
      </div>

      <div class="p-6">
        <div class="grid grid-cols-1 lg:grid-cols-2 gap-8">
          <!-- 左侧：配置区 -->
          <div class="space-y-6">
            <!-- 内容类型选择 -->
            <div>
              <label class="text-sm font-medium mb-3 block">内容类型</label>
              <div class="grid grid-cols-3 gap-2">
                <button
                  v-for="option in contentTypeOptions"
                  :key="option.value"
                  @click="contentType = option.value"
                  class="flex flex-col items-center gap-1 p-3 rounded-lg border transition-colors"
                  :class="contentType === option.value
                    ? 'border-primary bg-primary/10 text-primary'
                    : 'border-border hover:border-primary/50'"
                >
                  <component :is="option.icon" class="w-5 h-5" />
                  <span class="text-xs">{{ option.label }}</span>
                </button>
              </div>
            </div>

            <!-- 内容输入区 -->
            <div>
              <label class="text-sm font-medium mb-2 block">
                {{ contentTypeOptions.find(o => o.value === contentType)?.label }}内容
              </label>

              <!-- URL/文本/电话 -->
              <template v-if="contentType === ContentType.URL || contentType === ContentType.TEXT || contentType === ContentType.PHONE">
                <input
                  v-model="content"
                  :placeholder="contentType === ContentType.URL ? 'https://example.com' : contentType === ContentType.PHONE ? '+86 138 0000 0000' : '输入文本内容'"
                  class="w-full px-3 py-2 border rounded-lg bg-background focus:outline-none focus:ring-2 focus:ring-primary"
                />
              </template>

              <!-- WiFi -->
              <template v-if="contentType === ContentType.WIFI">
                <div class="space-y-3">
                  <input
                    v-model="wifiSsid"
                    placeholder="WiFi 名称 (SSID)"
                    class="w-full px-3 py-2 border rounded-lg bg-background focus:outline-none focus:ring-2 focus:ring-primary"
                  />
                  <input
                    v-model="wifiPassword"
                    type="password"
                    placeholder="WiFi 密码"
                    class="w-full px-3 py-2 border rounded-lg bg-background focus:outline-none focus:ring-2 focus:ring-primary"
                  />
                  <div class="flex gap-2">
                    <select
                      v-model="wifiEncryption"
                      class="px-3 py-2 border rounded-lg bg-background focus:outline-none focus:ring-2 focus:ring-primary"
                    >
                      <option :value="WifiEncryption.WPA">WPA/WPA2</option>
                      <option :value="WifiEncryption.WEP">WEP</option>
                      <option :value="WifiEncryption.NOPASS">无密码</option>
                    </select>
                    <label class="flex items-center gap-2 px-3 py-2">
                      <input v-model="wifiHidden" type="checkbox" />
                      <span class="text-sm">隐藏网络</span>
                    </label>
                  </div>
                </div>
              </template>

              <!-- 邮箱 -->
              <template v-if="contentType === ContentType.EMAIL">
                <div class="space-y-3">
                  <input
                    v-model="content"
                    type="email"
                    placeholder="email@example.com"
                    class="w-full px-3 py-2 border rounded-lg bg-background focus:outline-none focus:ring-2 focus:ring-primary"
                  />
                  <input
                    v-model="emailSubject"
                    placeholder="主题（可选）"
                    class="w-full px-3 py-2 border rounded-lg bg-background focus:outline-none focus:ring-2 focus:ring-primary"
                  />
                  <textarea
                    v-model="emailBody"
                    placeholder="正文（可选）"
                    rows="3"
                    class="w-full px-3 py-2 border rounded-lg bg-background focus:outline-none focus:ring-2 focus:ring-primary resize-none"
                  />
                </div>
              </template>

              <!-- 短信 -->
              <template v-if="contentType === ContentType.SMS">
                <div class="space-y-3">
                  <input
                    v-model="content"
                    placeholder="电话号码"
                    class="w-full px-3 py-2 border rounded-lg bg-background focus:outline-none focus:ring-2 focus:ring-primary"
                  />
                  <textarea
                    v-model="smsBody"
                    placeholder="短信内容"
                    rows="3"
                    class="w-full px-3 py-2 border rounded-lg bg-background focus:outline-none focus:ring-2 focus:ring-primary resize-none"
                  />
                </div>
              </template>
            </div>

            <!-- 尺寸设置 -->
            <div>
              <label class="text-sm font-medium mb-2 block">
                尺寸: {{ size }}px
              </label>
              <input
                v-model="size"
                type="range"
                min="100"
                max="1000"
                step="50"
                class="w-full"
              />
              <div class="flex justify-between text-xs text-muted-foreground mt-1">
                <span>100px</span>
                <span>1000px</span>
              </div>
            </div>

            <!-- 纠错级别 -->
            <div>
              <label class="text-sm font-medium mb-2 block">纠错级别</label>
              <div class="grid grid-cols-2 gap-2">
                <button
                  v-for="option in errorCorrectionOptions"
                  :key="option.value"
                  @click="errorCorrection = option.value"
                  class="p-3 rounded-lg border text-left transition-colors"
                  :class="errorCorrection === option.value
                    ? 'border-primary bg-primary/10'
                    : 'border-border hover:border-primary/50'"
                >
                  <div class="text-sm font-medium">{{ option.label }}</div>
                  <div class="text-xs text-muted-foreground">{{ option.description }}</div>
                </button>
              </div>
            </div>

            <!-- Logo 上传 -->
            <div>
              <label class="text-sm font-medium mb-2 block">Logo（可选）</label>
              <div v-if="logoBase64" class="flex items-center gap-3">
                <img :src="logoBase64" class="w-12 h-12 object-contain border rounded" />
                <button
                  @click="removeLogo"
                  class="p-2 text-red-500 hover:bg-red-50 rounded-lg"
                >
                  <Trash2 class="w-4 h-4" />
                </button>
              </div>
              <label
                v-else
                class="flex flex-col items-center gap-2 p-6 border-2 border-dashed border-border rounded-lg cursor-pointer hover:border-primary/50 transition-colors"
              >
                <Upload class="w-8 h-8 text-muted-foreground" />
                <span class="text-sm text-muted-foreground">点击上传 Logo</span>
                <input type="file" accept="image/*" class="hidden" @change="handleLogoUpload" />
              </label>
            </div>

            <!-- 操作按钮 -->
            <div class="flex gap-2">
              <button
                @click="generate"
                :disabled="isLoading || !canGenerate"
                class="flex-1 flex items-center justify-center gap-2 px-4 py-2 bg-primary text-primary-foreground rounded-lg hover:bg-primary/90 disabled:opacity-50"
              >
                <RefreshCw v-if="isLoading" class="w-4 h-4 animate-spin" />
                <QrCode v-else class="w-4 h-4" />
                {{ isLoading ? '生成中...' : '生成二维码' }}
              </button>
              <button
                @click="clearAll"
                class="px-4 py-2 border rounded-lg hover:bg-muted"
              >
                <Trash2 class="w-4 h-4" />
              </button>
            </div>
          </div>

          <!-- 右侧：预览区 -->
          <div class="flex flex-col items-center justify-center p-6 bg-muted/30 rounded-lg">
            <div
              v-if="qrCodeBase64"
              class="relative group"
            >
              <img
                :src="qrCodeBase64"
                :alt="'二维码'"
                class="max-w-full rounded-lg shadow-lg"
                :style="{ width: Math.min(size, 400) + 'px' }"
              />
              <div class="absolute inset-0 flex items-center justify-center gap-2 opacity-0 group-hover:opacity-100 transition-opacity bg-black/50 rounded-lg">
                <button
                  @click="downloadQRCode"
                  class="p-2 bg-white rounded-lg hover:bg-gray-100"
                  title="下载"
                >
                  <Download class="w-5 h-5" />
                </button>
                <button
                  @click="copyQRCode"
                  class="p-2 bg-white rounded-lg hover:bg-gray-100"
                  title="复制"
                >
                  <Copy class="w-5 h-5" />
                </button>
              </div>
            </div>
            <div v-else class="text-center text-muted-foreground">
              <QrCode class="w-24 h-24 mx-auto mb-4 opacity-30" />
              <p>输入内容生成二维码</p>
            </div>

            <!-- 提示信息 -->
            <div v-if="qrCodeBase64" class="mt-4 text-center text-sm text-muted-foreground">
              <p>尺寸: {{ size }}px | 纠错级别: {{ errorCorrection }}</p>
              <p class="mt-1">悬停图片可下载或复制</p>
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
