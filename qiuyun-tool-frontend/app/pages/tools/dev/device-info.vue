<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { 
  Monitor, 
  Cpu, 
  Wifi, 
  Battery, 
  Globe, 
  Smartphone,
  CheckCircle2,
  XCircle,
  Copy,
  RefreshCw
} from 'lucide-vue-next'
import { ToolCard } from '@/components/ui/tool-card'
import { ToolButton } from '@/components/ui/tool-button'

useHead({
  title: '设备信息检测 - 秋云工具',
  meta: [
    { name: 'description', content: '获取浏览器、操作系统、屏幕、硬件、网络等设备信息' }
  ]
})

// 设备信息状态
const deviceInfo = ref({
  // 基础信息
  userAgent: '',
  platform: '',
  language: '',
  languages: [] as string[],
  timezone: '',
  cookieEnabled: false,
  
  // 浏览器信息
  browserName: '',
  browserVersion: '',
  
  // 操作系统
  osName: '',
  osVersion: '',
  
  // 屏幕信息
  screenWidth: 0,
  screenHeight: 0,
  screenAvailWidth: 0,
  screenAvailHeight: 0,
  screenColorDepth: 0,
  screenPixelRatio: 0,
  
  // 窗口信息
  windowWidth: 0,
  windowHeight: 0,
  
  // 硬件信息
  hardwareConcurrency: 0,
  deviceMemory: 0,
  maxTouchPoints: 0,
  
  // 电池信息
  batteryLevel: null as number | null,
  batteryCharging: null as boolean | null,
  batteryChargingTime: null as number | null,
  batteryDischargingTime: null as number | null,
  
  // 网络信息
  connectionType: '',
  connectionEffectiveType: '',
  connectionDownlink: 0,
  connectionRtt: 0,
  online: false,
  
  // 功能支持
  webglSupported: false,
  webgl2Supported: false,
  canvasSupported: false,
  localStorageSupported: false,
  sessionStorageSupported: false,
  indexedDbSupported: false,
  geolocationSupported: false,
  notificationsSupported: false,
  serviceWorkerSupported: false,
  webShareSupported: false,
  bluetoothSupported: false,
  usbSupported: false,
  
  // 性能信息
  devicePixelRatio: 0,
  performanceMemory: null as any,
})

const loading = ref(false)
const copySuccess = ref(false)

// 计算属性：友好的平台名称
const platformDisplayName = computed(() => {
  const platform = deviceInfo.value.platform
  const ua = deviceInfo.value.userAgent
  
  if (!platform) return '未知'
  
  // 尝试从 User Agent 判断系统架构
  const isWin64 = ua.includes('Win64') || ua.includes('x64') || ua.includes('WOW64')
  const isMacIntel = platform.includes('MacIntel')
  const isMacARM = ua.includes('arm64') || ua.includes('ARM64')
  
  // 平台名称映射
  const platformMap: Record<string, string> = {
    'Win32': isWin64 ? 'Windows (64位)' : 'Windows',
    'Win64': 'Windows (64位)',
    'Windows': 'Windows',
    'MacIntel': isMacARM ? 'macOS (Apple Silicon)' : 'macOS (Intel)',
    'MacPPC': 'macOS (PowerPC)',
    'Mac68K': 'macOS (68K)',
    'Linux x86_64': 'Linux (64位)',
    'Linux i686': 'Linux (32位)',
    'Linux aarch64': 'Linux (ARM64)',
    'Linux armv7l': 'Linux (ARM)',
    'iPhone': 'iPhone',
    'iPod': 'iPod',
    'iPad': 'iPad',
    'Android': 'Android',
    'FreeBSD': 'FreeBSD',
    'OpenBSD': 'OpenBSD',
    'SunOS': 'SunOS'
  }
  
  return platformMap[platform] || platform
})

// 解析浏览器信息
const parseBrowser = (ua: string) => {
  let browserName = '未知浏览器'
  let browserVersion = ''
  
  if (ua.includes('Edg/')) {
    browserName = 'Microsoft Edge'
    browserVersion = ua.match(/Edg\/([\d.]+)/)?.[1] || ''
  } else if (ua.includes('Chrome/') && !ua.includes('Edg/')) {
    browserName = 'Google Chrome'
    browserVersion = ua.match(/Chrome\/([\d.]+)/)?.[1] || ''
  } else if (ua.includes('Safari/') && !ua.includes('Chrome/')) {
    browserName = 'Safari'
    browserVersion = ua.match(/Version\/([\d.]+)/)?.[1] || ''
  } else if (ua.includes('Firefox/')) {
    browserName = 'Firefox'
    browserVersion = ua.match(/Firefox\/([\d.]+)/)?.[1] || ''
  } else if (ua.includes('Opera/') || ua.includes('OPR/')) {
    browserName = 'Opera'
    browserVersion = ua.match(/(?:Opera|OPR)\/([\d.]+)/)?.[1] || ''
  }
  
  return { browserName, browserVersion }
}

// 解析操作系统
const parseOS = (ua: string, platform: string) => {
  let osName = '未知系统'
  let osVersion = ''
  
  if (ua.includes('Windows NT 10.0')) {
    osName = 'Windows'
    osVersion = '10/11'
  } else if (ua.includes('Windows NT 6.3')) {
    osName = 'Windows'
    osVersion = '8.1'
  } else if (ua.includes('Windows NT 6.2')) {
    osName = 'Windows'
    osVersion = '8'
  } else if (ua.includes('Windows NT 6.1')) {
    osName = 'Windows'
    osVersion = '7'
  } else if (ua.includes('Mac OS X') || ua.includes('macOS')) {
    osName = 'macOS'
    const match = ua.match(/Mac OS X ([\d_]+)/)
    osVersion = match && match[1] ? match[1].replace(/_/g, '.') : ''
  } else if (ua.includes('iPhone') || ua.includes('iPad')) {
    osName = 'iOS'
    const match = ua.match(/OS ([\d_]+)/)
    osVersion = match && match[1] ? match[1].replace(/_/g, '.') : ''
  } else if (ua.includes('Android')) {
    osName = 'Android'
    const match = ua.match(/Android ([\d.]+)/)
    osVersion = match && match[1] ? match[1] : ''
  } else if (ua.includes('Linux')) {
    osName = 'Linux'
  }
  
  return { osName, osVersion }
}

// 检测功能支持
const checkFeatureSupport = () => {
  const canvas = document.createElement('canvas')
  
  return {
    webglSupported: !!canvas.getContext('webgl'),
    webgl2Supported: !!canvas.getContext('webgl2'),
    canvasSupported: !!canvas.getContext('2d'),
    localStorageSupported: (() => {
      try {
        localStorage.setItem('test', 'test')
        localStorage.removeItem('test')
        return true
      } catch {
        return false
      }
    })(),
    sessionStorageSupported: (() => {
      try {
        sessionStorage.setItem('test', 'test')
        sessionStorage.removeItem('test')
        return true
      } catch {
        return false
      }
    })(),
    indexedDbSupported: 'indexedDB' in window,
    geolocationSupported: 'geolocation' in navigator,
    notificationsSupported: 'Notification' in window,
    serviceWorkerSupported: 'serviceWorker' in navigator,
    webShareSupported: 'share' in navigator,
    bluetoothSupported: 'bluetooth' in navigator,
    usbSupported: 'usb' in navigator,
  }
}

// 获取电池信息
const getBatteryInfo = async () => {
  try {
    if ('getBattery' in navigator) {
      const battery = await (navigator as any).getBattery()
      return {
        batteryLevel: Math.round(battery.level * 100),
        batteryCharging: battery.charging,
        batteryChargingTime: battery.chargingTime,
        batteryDischargingTime: battery.dischargingTime,
      }
    }
  } catch {
    // 不支持 Battery API
  }
  return {
    batteryLevel: null,
    batteryCharging: null,
    batteryChargingTime: null,
    batteryDischargingTime: null,
  }
}

// 获取网络信息
const getNetworkInfo = () => {
  const connection = (navigator as any).connection || 
                     (navigator as any).mozConnection || 
                     (navigator as any).webkitConnection
  
  if (connection) {
    return {
      connectionType: connection.type || '',
      connectionEffectiveType: connection.effectiveType || '',
      connectionDownlink: connection.downlink || 0,
      connectionRtt: connection.rtt || 0,
    }
  }
  
  return {
    connectionType: '',
    connectionEffectiveType: '',
    connectionDownlink: 0,
    connectionRtt: 0,
  }
}

// 刷新设备信息
const refreshDeviceInfo = async () => {
  loading.value = true
  
  const ua = navigator.userAgent
  const { browserName, browserVersion } = parseBrowser(ua)
  const { osName, osVersion } = parseOS(ua, navigator.platform)
  const features = checkFeatureSupport()
  const batteryInfo = await getBatteryInfo()
  const networkInfo = getNetworkInfo()
  
  deviceInfo.value = {
    userAgent: ua,
    platform: navigator.platform,
    language: navigator.language,
    languages: Array.from(navigator.languages || []),
    timezone: Intl.DateTimeFormat().resolvedOptions().timeZone,
    cookieEnabled: navigator.cookieEnabled,
    
    browserName,
    browserVersion,
    osName,
    osVersion,
    
    screenWidth: screen.width,
    screenHeight: screen.height,
    screenAvailWidth: screen.availWidth,
    screenAvailHeight: screen.availHeight,
    screenColorDepth: screen.colorDepth,
    screenPixelRatio: window.devicePixelRatio,
    
    windowWidth: window.innerWidth,
    windowHeight: window.innerHeight,
    
    hardwareConcurrency: navigator.hardwareConcurrency || 0,
    deviceMemory: (navigator as any).deviceMemory || 0,
    maxTouchPoints: navigator.maxTouchPoints || 0,
    
    ...batteryInfo,
    ...networkInfo,
    online: navigator.onLine,
    
    ...features,
    
    devicePixelRatio: window.devicePixelRatio,
    performanceMemory: (performance as any).memory || null,
  }
  
  loading.value = false
}

// 复制设备信息
const copyDeviceInfo = async () => {
  const infoText = `
设备信息报告
================

【基础信息】
操作系统: ${deviceInfo.value.osName} ${deviceInfo.value.osVersion}
浏览器: ${deviceInfo.value.browserName} ${deviceInfo.value.browserVersion}
语言: ${deviceInfo.value.language}
时区: ${deviceInfo.value.timezone}

【屏幕信息】
屏幕分辨率: ${deviceInfo.value.screenWidth} x ${deviceInfo.value.screenHeight}
可用分辨率: ${deviceInfo.value.screenAvailWidth} x ${deviceInfo.value.screenAvailHeight}
像素比: ${deviceInfo.value.screenPixelRatio}
色深: ${deviceInfo.value.screenColorDepth} bit

【硬件信息】
CPU核心数: ${deviceInfo.value.hardwareConcurrency}
设备内存: ${deviceInfo.value.deviceMemory || '未知'} GB
触摸点数: ${deviceInfo.value.maxTouchPoints}

【网络信息】
在线状态: ${deviceInfo.value.online ? '在线' : '离线'}
网络类型: ${deviceInfo.value.connectionEffectiveType || '未知'}
下行速度: ${deviceInfo.value.connectionDownlink || '未知'} Mbps

【电池信息】
电量: ${deviceInfo.value.batteryLevel !== null ? deviceInfo.value.batteryLevel + '%' : '不支持'}
充电状态: ${deviceInfo.value.batteryCharging !== null ? (deviceInfo.value.batteryCharging ? '充电中' : '未充电') : '不支持'}

User Agent:
${deviceInfo.value.userAgent}
  `.trim()
  
  try {
    await navigator.clipboard.writeText(infoText)
    copySuccess.value = true
    setTimeout(() => copySuccess.value = false, 2000)
  } catch {
    // 复制失败
  }
}

// 信息卡片组件
const InfoItem = ({ label, value, icon }: { label: string, value: string | number, icon?: any }) => {
  return h('div', { class: 'flex items-center justify-between py-2 border-b border-gray-100 last:border-0' }, [
    h('div', { class: 'flex items-center gap-2' }, [
      icon && h(icon, { class: 'w-4 h-4 text-gray-400' }),
      h('span', { class: 'text-sm text-gray-600' }, label)
    ]),
    h('span', { class: 'text-sm font-medium text-gray-900' }, value)
  ])
}

onMounted(() => {
  refreshDeviceInfo()
  
  // 监听窗口大小变化
  window.addEventListener('resize', () => {
    deviceInfo.value.windowWidth = window.innerWidth
    deviceInfo.value.windowHeight = window.innerHeight
  })
  
  // 监听在线状态变化
  window.addEventListener('online', () => deviceInfo.value.online = true)
  window.addEventListener('offline', () => deviceInfo.value.online = false)
})
</script>

<template>
  <NuxtLayout name="tool" toolCode="device-info">
    <div class="max-w-5xl mx-auto space-y-6">
      <!-- 操作栏 -->
      <div class="flex flex-wrap items-center justify-between gap-4">
        <div class="flex items-center gap-2">
          <ToolButton 
            variant="primary" 
            size="sm"
            @click="refreshDeviceInfo"
            :disabled="loading"
          >
            <RefreshCw class="w-4 h-4 mr-1" :class="{ 'animate-spin': loading }" />
            刷新信息
          </ToolButton>
          <ToolButton 
            variant="secondary" 
            size="sm"
            @click="copyDeviceInfo"
          >
            <Copy class="w-4 h-4 mr-1" />
            {{ copySuccess ? '已复制' : '复制信息' }}
          </ToolButton>
        </div>
        <div class="text-sm text-gray-500">
          窗口尺寸: {{ deviceInfo.windowWidth }} x {{ deviceInfo.windowHeight }}
        </div>
      </div>

      <!-- 信息卡片网格 -->
      <div class="grid md:grid-cols-2 gap-6">
        <!-- 基础信息 -->
        <ToolCard>
          <div class="flex items-center gap-2 mb-4">
            <Globe class="w-5 h-5 text-rose-500" />
            <h3 class="font-semibold text-gray-900">基础信息</h3>
          </div>
          <div class="space-y-1">
            <div class="flex justify-between py-2 border-b border-gray-100">
              <span class="text-sm text-gray-600">操作系统</span>
              <span class="text-sm font-medium text-gray-900">{{ deviceInfo.osName }} {{ deviceInfo.osVersion }}</span>
            </div>
            <div class="flex justify-between py-2 border-b border-gray-100">
              <span class="text-sm text-gray-600">浏览器</span>
              <span class="text-sm font-medium text-gray-900">{{ deviceInfo.browserName }} {{ deviceInfo.browserVersion }}</span>
            </div>
            <div class="flex justify-between py-2 border-b border-gray-100">
              <span class="text-sm text-gray-600">语言</span>
              <span class="text-sm font-medium text-gray-900">{{ deviceInfo.language }}</span>
            </div>
            <div class="flex justify-between py-2 border-b border-gray-100">
              <span class="text-sm text-gray-600">时区</span>
              <span class="text-sm font-medium text-gray-900">{{ deviceInfo.timezone }}</span>
            </div>
            <div class="flex justify-between py-2">
              <span class="text-sm text-gray-600">Cookie</span>
              <span class="text-sm font-medium" :class="deviceInfo.cookieEnabled ? 'text-green-600' : 'text-red-600'">
                {{ deviceInfo.cookieEnabled ? '已启用' : '已禁用' }}
              </span>
            </div>
          </div>
        </ToolCard>

        <!-- 屏幕信息 -->
        <ToolCard>
          <div class="flex items-center gap-2 mb-4">
            <Monitor class="w-5 h-5 text-rose-500" />
            <h3 class="font-semibold text-gray-900">屏幕信息</h3>
          </div>
          <div class="space-y-1">
            <div class="flex justify-between py-2 border-b border-gray-100">
              <span class="text-sm text-gray-600">屏幕分辨率</span>
              <span class="text-sm font-medium text-gray-900">{{ deviceInfo.screenWidth }} × {{ deviceInfo.screenHeight }}</span>
            </div>
            <div class="flex justify-between py-2 border-b border-gray-100">
              <span class="text-sm text-gray-600">可用分辨率</span>
              <span class="text-sm font-medium text-gray-900">{{ deviceInfo.screenAvailWidth }} × {{ deviceInfo.screenAvailHeight }}</span>
            </div>
            <div class="flex justify-between py-2 border-b border-gray-100">
              <span class="text-sm text-gray-600">像素比 (DPR)</span>
              <span class="text-sm font-medium text-gray-900">{{ deviceInfo.screenPixelRatio }}x</span>
            </div>
            <div class="flex justify-between py-2 border-b border-gray-100">
              <span class="text-sm text-gray-600">色深</span>
              <span class="text-sm font-medium text-gray-900">{{ deviceInfo.screenColorDepth }} bit</span>
            </div>
            <div class="flex justify-between py-2">
              <span class="text-sm text-gray-600">触摸支持</span>
              <span class="text-sm font-medium text-gray-900">
                {{ deviceInfo.maxTouchPoints > 0 ? `支持 (${deviceInfo.maxTouchPoints}点)` : '不支持' }}
              </span>
            </div>
          </div>
        </ToolCard>

        <!-- 硬件信息 -->
        <ToolCard>
          <div class="flex items-center gap-2 mb-4">
            <Cpu class="w-5 h-5 text-rose-500" />
            <h3 class="font-semibold text-gray-900">硬件信息</h3>
          </div>
          <div class="space-y-1">
            <div class="flex justify-between py-2 border-b border-gray-100">
              <span class="text-sm text-gray-600">CPU 核心数</span>
              <span class="text-sm font-medium text-gray-900">{{ deviceInfo.hardwareConcurrency }} 核</span>
            </div>
            <div class="flex justify-between py-2 border-b border-gray-100">
              <span class="text-sm text-gray-600">设备内存</span>
              <span class="text-sm font-medium text-gray-900">
                {{ deviceInfo.deviceMemory ? deviceInfo.deviceMemory + ' GB' : '未知' }}
              </span>
            </div>
            <div class="flex justify-between py-2 border-b border-gray-100">
              <span class="text-sm text-gray-600">硬件并发</span>
              <span class="text-sm font-medium text-gray-900">{{ deviceInfo.hardwareConcurrency }} 线程</span>
            </div>
            <div class="flex justify-between py-2">
              <span class="text-sm text-gray-600">平台</span>
              <span class="text-sm font-medium text-gray-900">{{ platformDisplayName }}</span>
            </div>
          </div>
        </ToolCard>

        <!-- 电池信息 -->
        <ToolCard>
          <div class="flex items-center gap-2 mb-4">
            <Battery class="w-5 h-5 text-rose-500" />
            <h3 class="font-semibold text-gray-900">电池信息</h3>
          </div>
          <div class="space-y-1">
            <div class="flex justify-between py-2 border-b border-gray-100">
              <span class="text-sm text-gray-600">电量</span>
              <span class="text-sm font-medium text-gray-900">
                {{ deviceInfo.batteryLevel !== null ? deviceInfo.batteryLevel + '%' : '不支持' }}
              </span>
            </div>
            <div class="flex justify-between py-2 border-b border-gray-100">
              <span class="text-sm text-gray-600">充电状态</span>
              <span class="text-sm font-medium" :class="deviceInfo.batteryCharging ? 'text-green-600' : 'text-gray-900'">
                {{ deviceInfo.batteryCharging !== null ? (deviceInfo.batteryCharging ? '充电中' : '未充电') : '不支持' }}
              </span>
            </div>
            <div class="flex justify-between py-2 border-b border-gray-100">
              <span class="text-sm text-gray-600">充满时间</span>
              <span class="text-sm font-medium text-gray-900">
                {{ deviceInfo.batteryChargingTime && deviceInfo.batteryChargingTime !== Infinity ? Math.round(deviceInfo.batteryChargingTime / 60) + ' 分钟' : '未知' }}
              </span>
            </div>
            <div class="flex justify-between py-2">
              <span class="text-sm text-gray-600">剩余时间</span>
              <span class="text-sm font-medium text-gray-900">
                {{ deviceInfo.batteryDischargingTime && deviceInfo.batteryDischargingTime !== Infinity ? Math.round(deviceInfo.batteryDischargingTime / 60) + ' 分钟' : '未知' }}
              </span>
            </div>
          </div>
        </ToolCard>

        <!-- 网络信息 -->
        <ToolCard>
          <div class="flex items-center gap-2 mb-4">
            <Wifi class="w-5 h-5 text-rose-500" />
            <h3 class="font-semibold text-gray-900">网络信息</h3>
          </div>
          <div class="space-y-1">
            <div class="flex justify-between py-2 border-b border-gray-100">
              <span class="text-sm text-gray-600">在线状态</span>
              <span class="text-sm font-medium" :class="deviceInfo.online ? 'text-green-600' : 'text-red-600'">
                {{ deviceInfo.online ? '在线' : '离线' }}
              </span>
            </div>
            <div class="flex justify-between py-2 border-b border-gray-100">
              <span class="text-sm text-gray-600">网络类型</span>
              <span class="text-sm font-medium text-gray-900">{{ deviceInfo.connectionEffectiveType || '未知' }}</span>
            </div>
            <div class="flex justify-between py-2 border-b border-gray-100">
              <span class="text-sm text-gray-600">下行速度</span>
              <span class="text-sm font-medium text-gray-900">
                {{ deviceInfo.connectionDownlink ? deviceInfo.connectionDownlink + ' Mbps' : '未知' }}
              </span>
            </div>
            <div class="flex justify-between py-2">
              <span class="text-sm text-gray-600">往返延迟 (RTT)</span>
              <span class="text-sm font-medium text-gray-900">
                {{ deviceInfo.connectionRtt ? deviceInfo.connectionRtt + ' ms' : '未知' }}
              </span>
            </div>
          </div>
        </ToolCard>

        <!-- 功能支持检测 -->
        <ToolCard>
          <div class="flex items-center gap-2 mb-4">
            <Smartphone class="w-5 h-5 text-rose-500" />
            <h3 class="font-semibold text-gray-900">功能支持检测</h3>
          </div>
          <div class="grid grid-cols-2 gap-2">
            <div 
              v-for="(supported, name) in {
                'WebGL': deviceInfo.webglSupported,
                'WebGL2': deviceInfo.webgl2Supported,
                'Canvas': deviceInfo.canvasSupported,
                '本地存储': deviceInfo.localStorageSupported,
                '会话存储': deviceInfo.sessionStorageSupported,
                'IndexedDB': deviceInfo.indexedDbSupported,
                '地理位置': deviceInfo.geolocationSupported,
                '通知': deviceInfo.notificationsSupported,
                'Service Worker': deviceInfo.serviceWorkerSupported,
                'Web Share': deviceInfo.webShareSupported,
                '蓝牙': deviceInfo.bluetoothSupported,
                'USB': deviceInfo.usbSupported,
              }" 
              :key="name"
              class="flex items-center gap-2 py-1"
            >
              <component 
                :is="supported ? CheckCircle2 : XCircle" 
                class="w-4 h-4"
                :class="supported ? 'text-green-500' : 'text-gray-300'"
              />
              <span class="text-sm text-gray-700">{{ name }}</span>
            </div>
          </div>
        </ToolCard>
      </div>

      <!-- User Agent -->
      <ToolCard>
        <div class="flex items-center gap-2 mb-4">
          <Globe class="w-5 h-5 text-rose-500" />
          <h3 class="font-semibold text-gray-900">User Agent</h3>
        </div>
        <div class="bg-gray-50 rounded-lg p-4">
          <code class="text-xs text-gray-700 break-all font-mono">
            {{ deviceInfo.userAgent }}
          </code>
        </div>
      </ToolCard>
    </div>
  </NuxtLayout>
</template>
