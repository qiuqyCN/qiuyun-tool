<script setup lang="ts">
import { ref, computed, onUnmounted } from 'vue'
import { Wifi, WifiOff, Send, Trash2, Copy, Check, RefreshCw, Settings, Activity, Clock } from 'lucide-vue-next'
import { useToast } from '~/composables/useToast'
import { useClipboard } from '~/composables/useClipboard'

// 连接状态枚举
enum ConnectionState {
  DISCONNECTED = 'disconnected',
  CONNECTING = 'connecting',
  CONNECTED = 'connected',
  ERROR = 'error'
}

// 消息类型枚举
enum MessageType {
  SENT = 'sent',
  RECEIVED = 'received',
  SYSTEM = 'system',
  ERROR = 'error'
}

// 消息接口
interface Message {
  id: number
  type: MessageType
  content: string
  timestamp: Date
}

// Toast
const { toast, showSuccess, showError, showInfo } = useToast()
const { copy } = useClipboard()

// 连接相关状态
const wsUrl = ref('wss://echo.websocket.org')
const connectionState = ref<ConnectionState>(ConnectionState.DISCONNECTED)
const socket = ref<WebSocket | null>(null)

// 消息相关状态
const messages = ref<Message[]>([])
const inputMessage = ref('')
const messageIdCounter = ref(0)

// 设置相关状态
const autoReconnect = ref(false)
const heartbeatEnabled = ref(false)
const heartbeatInterval = ref(30)
const heartbeatTimer = ref<ReturnType<typeof setInterval> | null>(null)

// 连接统计
const sentCount = ref(0)
const receivedCount = ref(0)
const connectionTime = ref<Date | null>(null)

// 连接状态显示
const connectionStateText = computed(() => {
  switch (connectionState.value) {
    case ConnectionState.CONNECTED:
      return '已连接'
    case ConnectionState.CONNECTING:
      return '连接中...'
    case ConnectionState.ERROR:
      return '连接错误'
    default:
      return '未连接'
  }
})

const connectionStateColor = computed(() => {
  switch (connectionState.value) {
    case ConnectionState.CONNECTED:
      return 'text-green-500'
    case ConnectionState.CONNECTING:
      return 'text-yellow-500'
    case ConnectionState.ERROR:
      return 'text-red-500'
    default:
      return 'text-gray-400'
  }
})

// 连接时长
const connectionDuration = computed(() => {
  if (!connectionTime.value) return ''
  const diff = Math.floor((Date.now() - connectionTime.value.getTime()) / 1000)
  const hours = Math.floor(diff / 3600)
  const minutes = Math.floor((diff % 3600) / 60)
  const seconds = diff % 60
  if (hours > 0) {
    return `${hours}:${String(minutes).padStart(2, '0')}:${String(seconds).padStart(2, '0')}`
  }
  return `${minutes}:${String(seconds).padStart(2, '0')}`
})

// 添加消息
const addMessage = (type: MessageType, content: string) => {
  messages.value.push({
    id: ++messageIdCounter.value,
    type,
    content,
    timestamp: new Date()
  })
  // 自动滚动到底部
  setTimeout(() => {
    const container = document.querySelector('.message-list')
    if (container) {
      container.scrollTop = container.scrollHeight
    }
  }, 10)
}

// 连接 WebSocket
const connect = () => {
  if (!wsUrl.value.trim()) {
    showError('请输入 WebSocket URL')
    return
  }

  if (!wsUrl.value.startsWith('ws://') && !wsUrl.value.startsWith('wss://')) {
    showError('URL 必须以 ws:// 或 wss:// 开头')
    return
  }

  if (socket.value) {
    socket.value.close()
  }

  connectionState.value = ConnectionState.CONNECTING
  addMessage(MessageType.SYSTEM, `正在连接到 ${wsUrl.value}...`)

  try {
    socket.value = new WebSocket(wsUrl.value)

    socket.value.onopen = () => {
      connectionState.value = ConnectionState.CONNECTED
      connectionTime.value = new Date()
      addMessage(MessageType.SYSTEM, '连接成功')
      showSuccess('WebSocket 连接成功')

      // 启动心跳
      if (heartbeatEnabled.value) {
        startHeartbeat()
      }
    }

    socket.value.onmessage = (event) => {
      receivedCount.value++
      addMessage(MessageType.RECEIVED, event.data)
    }

    socket.value.onerror = (error) => {
      connectionState.value = ConnectionState.ERROR
      addMessage(MessageType.ERROR, '连接发生错误')
      showError('WebSocket 连接错误')
    }

    socket.value.onclose = (event) => {
      const wasConnected = connectionState.value === ConnectionState.CONNECTED
      connectionState.value = ConnectionState.DISCONNECTED
      connectionTime.value = null
      stopHeartbeat()
      addMessage(MessageType.SYSTEM, `连接已关闭 (Code: ${event.code}, Reason: ${event.reason || 'N/A'})`)

      // 自动重连
      if (autoReconnect.value && wasConnected) {
        addMessage(MessageType.SYSTEM, '5秒后自动重连...')
        setTimeout(() => {
          if (autoReconnect.value && connectionState.value === ConnectionState.DISCONNECTED) {
            connect()
          }
        }, 5000)
      }
    }
  } catch (error: any) {
    connectionState.value = ConnectionState.ERROR
    addMessage(MessageType.ERROR, `连接失败: ${error.message}`)
    showError('连接失败')
  }
}

// 断开连接
const disconnect = () => {
  if (socket.value) {
    socket.value.close()
    socket.value = null
  }
  connectionState.value = ConnectionState.DISCONNECTED
  stopHeartbeat()
}

// 发送消息
const sendMessage = () => {
  if (!inputMessage.value.trim()) {
    return
  }

  if (!socket.value || connectionState.value !== ConnectionState.CONNECTED) {
    showError('请先建立连接')
    return
  }

  try {
    socket.value.send(inputMessage.value)
    sentCount.value++
    addMessage(MessageType.SENT, inputMessage.value)
    inputMessage.value = ''
  } catch (error: any) {
    addMessage(MessageType.ERROR, `发送失败: ${error.message}`)
    showError('发送失败')
  }
}

// 发送心跳
const sendHeartbeat = () => {
  if (socket.value && connectionState.value === ConnectionState.CONNECTED) {
    socket.value.send(JSON.stringify({ type: 'ping', timestamp: Date.now() }))
    sentCount.value++
    addMessage(MessageType.SENT, '💓 Heartbeat Ping')
  }
}

// 启动心跳
const startHeartbeat = () => {
  stopHeartbeat()
  if (heartbeatEnabled.value && heartbeatInterval.value > 0) {
    heartbeatTimer.value = setInterval(sendHeartbeat, heartbeatInterval.value * 1000)
    addMessage(MessageType.SYSTEM, `心跳已启动，间隔 ${heartbeatInterval.value} 秒`)
  }
}

// 停止心跳
const stopHeartbeat = () => {
  if (heartbeatTimer.value) {
    clearInterval(heartbeatTimer.value)
    heartbeatTimer.value = null
  }
}

// 清空消息
const clearMessages = () => {
  messages.value = []
  sentCount.value = 0
  receivedCount.value = 0
}

// 复制消息
const copyMessage = async (content: string) => {
  const success = await copy(content)
  if (success) {
    showSuccess('已复制消息内容')
  } else {
    showError('复制失败')
  }
}

// 格式化时间
const formatTime = (date: Date) => {
  return date.toLocaleTimeString('zh-CN', { hour12: false })
}

// 消息类型样式
const getMessageStyle = (type: MessageType) => {
  switch (type) {
    case MessageType.SENT:
      return 'bg-blue-500/10 border-blue-500/30 text-blue-600 dark:text-blue-400'
    case MessageType.RECEIVED:
      return 'bg-green-500/10 border-green-500/30 text-green-600 dark:text-green-400'
    case MessageType.ERROR:
      return 'bg-red-500/10 border-red-500/30 text-red-600 dark:text-red-400'
    default:
      return 'bg-muted/50 border-border text-muted-foreground'
  }
}

// 消息类型标签
const getMessageLabel = (type: MessageType) => {
  switch (type) {
    case MessageType.SENT:
      return '发送'
    case MessageType.RECEIVED:
      return '接收'
    case MessageType.ERROR:
      return '错误'
    default:
      return '系统'
  }
}

// 按回车发送
const handleKeydown = (event: KeyboardEvent) => {
  if (event.key === 'Enter' && !event.shiftKey) {
    event.preventDefault()
    sendMessage()
  }
}

// 组件卸载时关闭连接
onUnmounted(() => {
  if (socket.value) {
    socket.value.close()
  }
  stopHeartbeat()
})
</script>

<template>
  <NuxtLayout name="tool" tool-code="websocket-tester">
    <div class="border border-border/40 rounded-xl overflow-hidden">
      <!-- 标题栏 -->
      <div class="border-b bg-muted/30 px-6 py-4">
        <div class="flex items-center gap-2">
          <Wifi class="w-5 h-5 text-primary" />
          <h2 class="text-lg font-semibold">WebSocket 测试</h2>
        </div>
        <p class="text-sm text-muted-foreground mt-1">
          WebSocket 连接测试工具，支持实时消息收发和心跳保活
        </p>
      </div>

      <div class="p-6 space-y-4">
        <!-- 连接设置 -->
        <div class="border rounded-lg p-4 space-y-4">
          <h3 class="text-sm font-medium flex items-center gap-2">
            <Settings class="w-4 h-4" />
            连接设置
          </h3>

          <div class="flex gap-2">
            <input
              v-model="wsUrl"
              type="text"
              placeholder="ws://example.com/socket 或 wss://example.com/socket"
              class="flex-1 px-3 py-2 text-sm border rounded-md bg-background font-mono"
              :disabled="connectionState === ConnectionState.CONNECTED"
              @keyup.enter="connectionState !== ConnectionState.CONNECTED && connect()"
            />
            <Button
              v-if="connectionState !== ConnectionState.CONNECTED"
              @click="connect"
              :disabled="connectionState === ConnectionState.CONNECTING"
            >
              <RefreshCw v-if="connectionState === ConnectionState.CONNECTING" class="w-4 h-4 mr-2 animate-spin" />
              <Wifi v-else class="w-4 h-4 mr-2" />
              {{ connectionState === ConnectionState.CONNECTING ? '连接中' : '连接' }}
            </Button>
            <Button v-else variant="destructive" @click="disconnect">
              <WifiOff class="w-4 h-4 mr-2" />
              断开
            </Button>
          </div>

          <!-- 连接状态 -->
          <div class="flex items-center gap-4 text-sm">
            <div class="flex items-center gap-2">
              <span class="text-muted-foreground">状态:</span>
              <span :class="connectionStateColor" class="font-medium flex items-center gap-1">
                <span class="w-2 h-2 rounded-full" :class="{
                  'bg-green-500': connectionState === ConnectionState.CONNECTED,
                  'bg-yellow-500 animate-pulse': connectionState === ConnectionState.CONNECTING,
                  'bg-red-500': connectionState === ConnectionState.ERROR,
                  'bg-gray-400': connectionState === ConnectionState.DISCONNECTED
                }"></span>
                {{ connectionStateText }}
              </span>
            </div>
            <div v-if="connectionState === ConnectionState.CONNECTED" class="flex items-center gap-2 text-muted-foreground">
              <Clock class="w-4 h-4" />
              <span>时长: {{ connectionDuration }}</span>
            </div>
          </div>

          <!-- 高级设置 -->
          <div class="flex flex-wrap items-center gap-4 pt-2 border-t">
            <label class="flex items-center gap-2 text-sm cursor-pointer">
              <input
                v-model="autoReconnect"
                type="checkbox"
                class="rounded border-gray-300"
              />
              <span>自动重连</span>
            </label>
            <label class="flex items-center gap-2 text-sm cursor-pointer">
              <input
                v-model="heartbeatEnabled"
                type="checkbox"
                class="rounded border-gray-300"
                @change="heartbeatEnabled ? startHeartbeat() : stopHeartbeat()"
              />
              <span>心跳保活</span>
            </label>
            <div v-if="heartbeatEnabled" class="flex items-center gap-2 text-sm">
              <span class="text-muted-foreground">间隔:</span>
              <input
                v-model="heartbeatInterval"
                type="number"
                min="5"
                max="300"
                class="w-16 px-2 py-1 border rounded text-center"
              />
              <span class="text-muted-foreground">秒</span>
            </div>
          </div>
        </div>

        <!-- 主内容区 -->
        <div class="grid grid-cols-1 lg:grid-cols-3 gap-4">
          <!-- 发送区域 -->
          <div class="lg:col-span-1 flex flex-col gap-2">
            <div class="flex items-center justify-between">
              <label class="text-sm font-medium">发送消息</label>
              <span class="text-xs text-muted-foreground">按 Enter 发送</span>
            </div>
            <Textarea
              v-model="inputMessage"
              placeholder="输入要发送的消息内容..."
              rows="8"
              class="font-mono text-sm resize-none"
              :disabled="connectionState !== ConnectionState.CONNECTED"
              @keydown="handleKeydown"
            />
            <div class="flex gap-2">
              <Button
                @click="sendMessage"
                :disabled="connectionState !== ConnectionState.CONNECTED || !inputMessage.trim()"
                class="flex-1"
              >
                <Send class="w-4 h-4 mr-2" />
                发送
              </Button>
              <Button variant="outline" @click="inputMessage = ''">
                <Trash2 class="w-4 h-4" />
              </Button>
            </div>
          </div>

          <!-- 消息历史 -->
          <div class="lg:col-span-2 flex flex-col gap-2">
            <div class="flex items-center justify-between">
              <label class="text-sm font-medium flex items-center gap-2">
                <Activity class="w-4 h-4" />
                消息历史
              </label>
              <div class="flex items-center gap-4 text-xs text-muted-foreground">
                <span>发送: {{ sentCount }}</span>
                <span>接收: {{ receivedCount }}</span>
                <button
                  @click="clearMessages"
                  class="hover:text-foreground transition-colors flex items-center gap-1"
                >
                  <Trash2 class="w-3 h-3" />
                  清空
                </button>
              </div>
            </div>
            <div class="message-list border rounded-lg p-3 h-[320px] overflow-y-auto bg-muted/10 space-y-2">
              <div v-if="messages.length === 0" class="h-full flex items-center justify-center text-muted-foreground text-sm">
                暂无消息记录
              </div>
              <div
                v-for="msg in messages"
                :key="msg.id"
                class="rounded-lg border p-2 text-sm"
                :class="getMessageStyle(msg.type)"
              >
                <div class="flex items-center justify-between mb-1">
                  <span class="text-xs font-medium">{{ getMessageLabel(msg.type) }}</span>
                  <div class="flex items-center gap-2">
                    <span class="text-xs opacity-70">{{ formatTime(msg.timestamp) }}</span>
                    <button
                      @click="copyMessage(msg.content)"
                      class="opacity-50 hover:opacity-100 transition-opacity"
                    >
                      <Copy class="w-3 h-3" />
                    </button>
                  </div>
                </div>
                <pre class="whitespace-pre-wrap break-all font-mono text-xs">{{ msg.content }}</pre>
              </div>
            </div>
          </div>
        </div>

        <!-- 使用说明 -->
        <div class="p-3 bg-primary/5 rounded-lg border border-primary/20">
          <div class="text-xs text-primary font-medium mb-2">使用说明:</div>
          <ul class="text-xs text-muted-foreground space-y-1 list-disc list-inside">
            <li>输入 WebSocket URL（以 ws:// 或 wss:// 开头），点击"连接"按钮建立连接</li>
            <li>连接成功后，在发送区域输入消息，按 Enter 或点击"发送"按钮发送消息</li>
            <li>服务器返回的消息会实时显示在消息历史区域</li>
            <li>开启"心跳保活"可定期发送心跳包，防止连接断开</li>
            <li>测试地址: <code class="bg-muted px-1 rounded">wss://echo.websocket.org</code> (会原样返回发送的消息)</li>
          </ul>
        </div>
      </div>
    </div>

    <!-- Toast 提示 -->
    <div
      v-if="toast.show"
      class="fixed bottom-4 right-4 px-4 py-2 rounded-lg shadow-lg flex items-center gap-2 z-50"
      :class="{
        'bg-green-500 text-white': toast.type === 'success',
        'bg-red-500 text-white': toast.type === 'error',
        'bg-blue-500 text-white': toast.type === 'info'
      }"
    >
      <Check v-if="toast.type === 'success'" class="w-4 h-4" />
      <span class="text-sm">{{ toast.message }}</span>
    </div>
  </NuxtLayout>
</template>
