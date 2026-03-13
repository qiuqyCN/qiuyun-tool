<script setup lang="ts">
import { ref } from 'vue'
import {
  QrCode,
  Upload,
  Copy,
  Check,
  Link,
  Wifi,
  Mail,
  Phone,
  MessageSquare,
  Type,
  User,
  MapPin,
  ExternalLink,
  Trash2,
  ScanLine
} from 'lucide-vue-next'
import { useToolExecutor } from '~/composables/useToolExecutor'
import { ToolType } from '~/types/tool'

// 内容类型
interface ParserResult {
  success: boolean
  rawContent: string
  contentType: string
  contentTypeLabel: string
  structuredData: Record<string, any>
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
const imageBase64 = ref('')
const isDragging = ref(false)
const result = ref<ParserResult | null>(null)

// 使用工具执行器
const { execute, isLoading } = useToolExecutor<{ imageBase64: string }, ParserResult>({
  toolCode: 'qr-code-parser',
  toolType: ToolType.INSTANT,
  onSuccess: (res) => {
    if (res.success) {
      result.value = res
      showToast('解析成功')
    } else {
      showToast(res.errorMessage || '解析失败', 'error')
    }
  },
  onError: (err) => {
    showToast(err, 'error')
  }
})

// 处理文件选择
const handleFileSelect = (event: Event) => {
  const file = (event.target as HTMLInputElement).files?.[0]
  if (file) processFile(file)
}

// 处理拖放
const handleDrop = (event: DragEvent) => {
  isDragging.value = false
  const file = event.dataTransfer?.files[0]
  if (file) processFile(file)
}

// 处理文件
const processFile = (file: File) => {
  if (!file.type.startsWith('image/')) {
    showToast('请上传图片文件', 'error')
    return
  }

  const reader = new FileReader()
  reader.onload = (e) => {
    imageBase64.value = e.target?.result as string
    parseQRCode()
  }
  reader.readAsDataURL(file)
}

// 解析二维码
const parseQRCode = () => {
  if (!imageBase64.value) {
    showToast('请先上传图片', 'error')
    return
  }
  execute({ imageBase64: imageBase64.value })
}

// 清空
const clearAll = () => {
  imageBase64.value = ''
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

// 获取内容类型图标
const getContentTypeIcon = (type: string) => {
  const icons: Record<string, any> = {
    url: Link,
    wifi: Wifi,
    email: Mail,
    phone: Phone,
    sms: MessageSquare,
    vcard: User,
    geo: MapPin,
    text: Type
  }
  return icons[type] || Type
}

// 打开链接
const openUrl = (url: string) => {
  window.open(url, '_blank')
}

// 格式化 WiFi 信息
const formatWifiInfo = (data: Record<string, any>) => {
  const parts = []
  if (data.ssid) parts.push(`网络: ${data.ssid}`)
  if (data.encryption && data.encryption !== 'nopass') parts.push(`加密: ${data.encryption}`)
  if (data.password) parts.push(`密码: ${data.password}`)
  return parts.join(' | ')
}
</script>

<template>
  <NuxtLayout name="tool" tool-code="qr-code-parser">
    <div class="border border-border/40 rounded-xl overflow-hidden bg-card">
      <!-- 标题栏 -->
      <div class="border-b bg-muted/30 px-6 py-4">
        <div class="flex items-center gap-2">
          <ScanLine class="w-5 h-5 text-primary" />
          <h2 class="text-lg font-semibold">二维码解析</h2>
        </div>
        <p class="text-sm text-muted-foreground mt-1">
          上传二维码图片，自动识别其中的内容信息
        </p>
      </div>

      <div class="p-6">
        <div class="grid grid-cols-1 lg:grid-cols-2 gap-8">
          <!-- 左侧：上传区 -->
          <div class="space-y-6">
            <!-- 上传区域 -->
            <div
              @dragover.prevent
              @dragenter.prevent="isDragging = true"
              @dragleave.prevent="isDragging = false"
              @drop.prevent="handleDrop"
              :class="[
                'relative border-2 border-dashed rounded-lg p-8 text-center transition-colors',
                isDragging ? 'border-primary bg-primary/5' : 'border-border',
                imageBase64 ? 'bg-muted/30' : ''
              ]"
            >
              <input
                type="file"
                accept="image/*"
                class="absolute inset-0 w-full h-full opacity-0 cursor-pointer"
                @change="handleFileSelect"
              />
              
              <div v-if="imageBase64" class="relative">
                <img
                  :src="imageBase64"
                  alt="上传的二维码"
                  class="max-h-64 mx-auto rounded-lg shadow"
                />
                <button
                  @click.stop="clearAll"
                  class="absolute -top-2 -right-2 p-1.5 bg-red-500 text-white rounded-full hover:bg-red-600 shadow"
                >
                  <Trash2 class="w-4 h-4" />
                </button>
              </div>
              
              <div v-else class="py-8">
                <Upload class="w-12 h-12 mx-auto mb-4 text-muted-foreground" />
                <p class="text-muted-foreground mb-2">点击或拖拽图片到此处</p>
                <p class="text-xs text-muted-foreground">
                  支持 PNG、JPG、JPEG、GIF、BMP、WEBP 格式
                </p>
              </div>
            </div>

            <!-- 操作按钮 -->
            <div class="flex gap-2">
              <button
                @click="parseQRCode"
                :disabled="isLoading || !imageBase64"
                class="flex-1 flex items-center justify-center gap-2 px-4 py-2 bg-primary text-primary-foreground rounded-lg hover:bg-primary/90 disabled:opacity-50"
              >
                <ScanLine v-if="!isLoading" class="w-4 h-4" />
                <span v-else class="w-4 h-4 border-2 border-white/30 border-t-white rounded-full animate-spin" />
                {{ isLoading ? '解析中...' : '开始解析' }}
              </button>
              <button
                @click="clearAll"
                :disabled="!imageBase64"
                class="px-4 py-2 border rounded-lg hover:bg-muted disabled:opacity-50"
              >
                <Trash2 class="w-4 h-4" />
              </button>
            </div>

            <!-- 使用说明 -->
            <div class="bg-muted/30 rounded-lg p-4">
              <h3 class="text-sm font-medium mb-2">支持的内容类型</h3>
              <div class="grid grid-cols-2 gap-2 text-xs text-muted-foreground">
                <div class="flex items-center gap-1.5">
                  <Link class="w-3.5 h-3.5" />
                  <span>网址链接</span>
                </div>
                <div class="flex items-center gap-1.5">
                  <Wifi class="w-3.5 h-3.5" />
                  <span>WiFi 连接</span>
                </div>
                <div class="flex items-center gap-1.5">
                  <Mail class="w-3.5 h-3.5" />
                  <span>电子邮箱</span>
                </div>
                <div class="flex items-center gap-1.5">
                  <Phone class="w-3.5 h-3.5" />
                  <span>电话号码</span>
                </div>
                <div class="flex items-center gap-1.5">
                  <MessageSquare class="w-3.5 h-3.5" />
                  <span>短信</span>
                </div>
                <div class="flex items-center gap-1.5">
                  <User class="w-3.5 h-3.5" />
                  <span>联系人卡片</span>
                </div>
                <div class="flex items-center gap-1.5">
                  <MapPin class="w-3.5 h-3.5" />
                  <span>地理位置</span>
                </div>
                <div class="flex items-center gap-1.5">
                  <Type class="w-3.5 h-3.5" />
                  <span>纯文本</span>
                </div>
              </div>
            </div>
          </div>

          <!-- 右侧：结果区 -->
          <div class="space-y-6">
            <!-- 解析结果 -->
            <div v-if="result" class="space-y-4">
              <!-- 内容类型标签 -->
              <div class="flex items-center gap-3">
                <div class="flex items-center gap-2 px-3 py-1.5 bg-primary/10 text-primary rounded-full">
                  <component :is="getContentTypeIcon(result.contentType)" class="w-4 h-4" />
                  <span class="text-sm font-medium">{{ result.contentTypeLabel }}</span>
                </div>
              </div>

              <!-- 原始内容 -->
              <div class="bg-muted/30 rounded-lg p-4">
                <div class="flex items-center justify-between mb-2">
                  <span class="text-sm font-medium">原始内容</span>
                  <button
                    @click="copyContent(result.rawContent)"
                    class="p-1.5 hover:bg-muted rounded"
                    title="复制"
                  >
                    <Copy class="w-4 h-4" />
                  </button>
                </div>
                <div class="text-sm break-all font-mono bg-background p-3 rounded border">
                  {{ result.rawContent }}
                </div>
              </div>

              <!-- 结构化数据 -->
              <div v-if="Object.keys(result.structuredData).length > 0" class="space-y-3">
                <h3 class="text-sm font-medium">解析详情</h3>

                <!-- URL -->
                <template v-if="result.contentType === 'url'">
                  <div class="flex items-center gap-2 p-3 bg-muted/30 rounded-lg">
                    <Link class="w-5 h-5 text-primary" />
                    <span class="flex-1 text-sm truncate">{{ result.structuredData.displayUrl || result.structuredData.url }}</span>
                    <button
                      @click="openUrl(result.structuredData.url)"
                      class="p-1.5 hover:bg-muted rounded"
                      title="打开链接"
                    >
                      <ExternalLink class="w-4 h-4" />
                    </button>
                  </div>
                </template>

                <!-- WiFi -->
                <template v-if="result.contentType === 'wifi'">
                  <div class="p-3 bg-muted/30 rounded-lg space-y-2">
                    <div class="flex items-center gap-2">
                      <Wifi class="w-5 h-5 text-primary" />
                      <span class="font-medium">{{ result.structuredData.ssid }}</span>
                    </div>
                    <div class="text-sm text-muted-foreground space-y-1">
                      <p v-if="result.structuredData.encryption && result.structuredData.encryption !== 'nopass'">
                        加密方式: {{ result.structuredData.encryption }}
                      </p>
                      <p v-if="result.structuredData.password">
                        密码: {{ result.structuredData.password }}
                      </p>
                      <p v-if="result.structuredData.hidden">隐藏网络</p>
                    </div>
                  </div>
                </template>

                <!-- 邮箱 -->
                <template v-if="result.contentType === 'email'">
                  <div class="p-3 bg-muted/30 rounded-lg space-y-2">
                    <div class="flex items-center gap-2">
                      <Mail class="w-5 h-5 text-primary" />
                      <span class="font-medium">{{ result.structuredData.email }}</span>
                    </div>
                    <div v-if="result.structuredData.subject" class="text-sm">
                      <span class="text-muted-foreground">主题:</span> {{ result.structuredData.subject }}
                    </div>
                    <div v-if="result.structuredData.body" class="text-sm">
                      <span class="text-muted-foreground">正文:</span> {{ result.structuredData.body }}
                    </div>
                  </div>
                </template>

                <!-- 电话 -->
                <template v-if="result.contentType === 'phone'">
                  <div class="flex items-center gap-2 p-3 bg-muted/30 rounded-lg">
                    <Phone class="w-5 h-5 text-primary" />
                    <span class="flex-1 font-medium">{{ result.structuredData.phone }}</span>
                    <a
                      :href="'tel:' + result.structuredData.phone"
                      class="px-3 py-1 text-sm bg-primary text-primary-foreground rounded hover:bg-primary/90"
                    >
                      拨打
                    </a>
                  </div>
                </template>

                <!-- 短信 -->
                <template v-if="result.contentType === 'sms'">
                  <div class="p-3 bg-muted/30 rounded-lg space-y-2">
                    <div class="flex items-center gap-2">
                      <MessageSquare class="w-5 h-5 text-primary" />
                      <span class="font-medium">{{ result.structuredData.phone }}</span>
                    </div>
                    <div v-if="result.structuredData.message" class="text-sm">
                      <span class="text-muted-foreground">内容:</span> {{ result.structuredData.message }}
                    </div>
                  </div>
                </template>

                <!-- 联系人 -->
                <template v-if="result.contentType === 'vcard'">
                  <div class="p-3 bg-muted/30 rounded-lg space-y-2">
                    <div class="flex items-center gap-2">
                      <User class="w-5 h-5 text-primary" />
                      <span class="font-medium">{{ result.structuredData.fullName || '未知联系人' }}</span>
                    </div>
                    <div class="text-sm text-muted-foreground space-y-1">
                      <p v-if="result.structuredData.phone">电话: {{ result.structuredData.phone }}</p>
                      <p v-if="result.structuredData.email">邮箱: {{ result.structuredData.email }}</p>
                      <p v-if="result.structuredData.organization">公司: {{ result.structuredData.organization }}</p>
                      <p v-if="result.structuredData.title">职位: {{ result.structuredData.title }}</p>
                    </div>
                  </div>
                </template>

                <!-- 地理位置 -->
                <template v-if="result.contentType === 'geo'">
                  <div class="p-3 bg-muted/30 rounded-lg space-y-2">
                    <div class="flex items-center gap-2">
                      <MapPin class="w-5 h-5 text-primary" />
                      <span class="font-medium">地理位置</span>
                    </div>
                    <div class="text-sm text-muted-foreground">
                      <p>纬度: {{ result.structuredData.latitude }}</p>
                      <p>经度: {{ result.structuredData.longitude }}</p>
                    </div>
                    <a
                      :href="`https://maps.google.com/?q=${result.structuredData.latitude},${result.structuredData.longitude}`"
                      target="_blank"
                      class="inline-flex items-center gap-1 text-sm text-primary hover:underline"
                    >
                      在地图中查看 <ExternalLink class="w-3 h-3" />
                    </a>
                  </div>
                </template>

                <!-- 纯文本 -->
                <template v-if="result.contentType === 'text'">
                  <div class="p-3 bg-muted/30 rounded-lg">
                    <p class="text-sm whitespace-pre-wrap">{{ result.structuredData.text }}</p>
                  </div>
                </template>
              </div>
            </div>

            <!-- 空状态 -->
            <div v-else class="flex flex-col items-center justify-center h-full min-h-[300px] text-muted-foreground">
              <QrCode class="w-16 h-16 mb-4 opacity-30" />
              <p>上传二维码图片查看解析结果</p>
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
