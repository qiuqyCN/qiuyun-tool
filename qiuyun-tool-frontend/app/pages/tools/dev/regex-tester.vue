<script setup lang="ts">
import { ref, computed, watch } from 'vue'
import { AlertCircle, CheckCircle, Copy, Search, Trash2, Check, Loader2, ChevronDown } from 'lucide-vue-next'
import { useToolExecutor } from '~/composables/useToolExecutor'
import { ToolType } from '~/types/tool'

// 标志位选项
interface FlagOption {
  value: string
  label: string
  checked: boolean
}

// 匹配结果
interface MatchGroup {
  index: number
  value: string
  start: number
  end: number
}

interface MatchResult {
  fullMatch: string
  start: number
  end: number
  groups: MatchGroup[]
}

// 响应结果
interface RegexTesterResult {
  success: boolean
  isMatch: boolean
  matchCount: number
  matches: MatchResult[]
  highlightedText: string
  errorMessage?: string
}

// 请求参数
interface RegexTesterParams {
  pattern: string
  text: string
  flags: string[]
  findAll: boolean
}

// 预设正则
const presets: Record<string, string> = {
  email: '^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$',
  phone: '^1[3-9]\\d{9}$',
  url: '^https?://[\\w\\-._~:/?#\\[\\]@!$&\'()*+,;=]+$',
  ip: '^(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)$',
  idCard: '^[1-9]\\d{5}(?:18|19|20)\\d{2}(?:0[1-9]|1[0-2])(?:0[1-9]|[12]\\d|3[01])\\d{3}[\\dXx]$',
  chinese: '^[\\u4e00-\\u9fa5]+$',
  number: '^-?\\d+\\.?\\d*$',
  date: '^\\d{4}-(?:0[1-9]|1[0-2])-(?:0[1-9]|[12]\\d|3[01])$',
  htmlTag: '<[^>]+>',
  whitespace: '\\s+'
}

const presetLabels: Record<string, string> = {
  email: '邮箱地址',
  phone: '手机号码',
  url: 'URL链接',
  ip: 'IP地址',
  idCard: '身份证号',
  chinese: '中文字符',
  number: '数字',
  date: '日期格式',
  htmlTag: 'HTML标签',
  whitespace: '空白字符'
}

// 状态
const pattern = ref('')
const text = ref('')
const findAll = ref(true)
const flags = ref<FlagOption[]>([
  { value: 'i', label: '忽略大小写', checked: false },
  { value: 'm', label: '多行模式', checked: false },
  { value: 's', label: '单行模式', checked: false },
  { value: 'x', label: '忽略空白', checked: false }
])
const selectedPreset = ref('')
const showPresets = ref(false)

// 结果
const matchResult = ref<RegexTesterResult | null>(null)
const error = ref('')

// Toast 提示
const toast = ref({ show: false, message: '' })
const showToast = (message: string) => {
  toast.value.message = message
  toast.value.show = true
  setTimeout(() => toast.value.show = false, 2000)
}

// 使用工具执行器
const { execute, isLoading } = useToolExecutor<RegexTesterParams, RegexTesterResult>({
  toolCode: 'regex-tester',
  toolType: ToolType.INSTANT,
  onSuccess: (result) => {
    if (result.success) {
      matchResult.value = result
      error.value = ''
    } else {
      error.value = result.errorMessage || '测试失败'
      matchResult.value = null
    }
  },
  onError: (err) => {
    error.value = err
    matchResult.value = null
  }
})

// 执行测试
const testRegex = async () => {
  if (!pattern.value.trim()) {
    error.value = '请输入正则表达式'
    return
  }

  const activeFlags = flags.value.filter(f => f.checked).map(f => f.value)
  
  await execute({
    pattern: pattern.value,
    text: text.value,
    flags: activeFlags,
    findAll: findAll.value
  })
}

// 选择预设
const selectPreset = (key: string) => {
  pattern.value = presets[key]
  selectedPreset.value = key
  showPresets.value = false
  testRegex()
}

// 清空
const clearAll = () => {
  pattern.value = ''
  text.value = ''
  matchResult.value = null
  error.value = ''
  selectedPreset.value = ''
}

// 复制正则
const copyPattern = async () => {
  if (pattern.value) {
    try {
      await navigator.clipboard.writeText(pattern.value)
      showToast('已复制正则表达式')
    } catch {
      showToast('复制失败')
    }
  }
}

// 监听输入变化，自动测试
watch([pattern, text, flags, findAll], () => {
  if (pattern.value && text.value) {
    testRegex()
  }
}, { deep: true })

// 匹配状态文本
const matchStatusText = computed(() => {
  if (!matchResult.value) return ''
  if (matchResult.value.isMatch) {
    return `匹配成功，共 ${matchResult.value.matchCount} 个结果`
  }
  return '未匹配到结果'
})

// 匹配状态颜色
const matchStatusClass = computed(() => {
  if (!matchResult.value) return ''
  return matchResult.value.isMatch ? 'text-green-600' : 'text-gray-500'
})

// 获取标志位描述
const getFlagDescription = (flag: string): string => {
  const descriptions: Record<string, string> = {
    'i': '忽略大小写 (Ignore Case)',
    'm': '多行模式 (Multiline)',
    's': '单行模式 (Dotall)',
    'x': '忽略空白 (Extended)'
  }
  return descriptions[flag] || ''
}
</script>

<template>
  <NuxtLayout name="tool" tool-code="regex-tester">
    <div class="border border-border/40 rounded-xl overflow-hidden">
      <!-- 正则输入区域 -->
      <div class="p-6 border-b bg-muted/20">
        <div class="flex items-center gap-2 mb-3">
          <Search class="w-4 h-4 text-muted-foreground" />
          <label class="text-sm font-medium">正则表达式</label>
        </div>
        
        <div class="flex gap-3">
          <div class="flex-1 relative">
            <input
              v-model="pattern"
              type="text"
              placeholder="输入正则表达式，例如: ^[a-zA-Z0-9]+$"
              class="w-full px-4 py-2.5 border rounded-lg font-mono text-sm focus:outline-none focus:ring-2 focus:ring-primary/20 focus:border-primary"
            />
          </div>
          
          <!-- 预设选择 -->
          <div class="relative">
            <button
              @click="showPresets = !showPresets"
              class="px-4 py-2.5 border rounded-lg text-sm hover:bg-muted flex items-center gap-2"
            >
              常用预设
              <ChevronDown class="w-4 h-4" />
            </button>
            <div
              v-if="showPresets"
              class="absolute top-full right-0 mt-1 w-48 bg-white border rounded-lg shadow-lg z-10"
            >
              <button
                v-for="(value, key) in presets"
                :key="key"
                @click="selectPreset(key)"
                class="w-full px-4 py-2 text-left text-sm hover:bg-muted first:rounded-t-lg last:rounded-b-lg"
              >
                {{ presetLabels[key] }}
              </button>
            </div>
          </div>
          
          <button
            @click="copyPattern"
            :disabled="!pattern"
            class="px-4 py-2.5 border rounded-lg text-sm hover:bg-muted disabled:opacity-50"
          >
            <Copy class="w-4 h-4" />
          </button>
        </div>
        
        <!-- 标志位选项 -->
        <div class="flex items-center gap-4 mt-3">
          <span class="text-xs text-muted-foreground">标志位：</span>
          <label
            v-for="flag in flags"
            :key="flag.value"
            class="flex items-center gap-1.5 text-sm cursor-pointer"
            :title="getFlagDescription(flag.value)"
          >
            <input
              v-model="flag.checked"
              type="checkbox"
              class="rounded border-gray-300"
            />
            <span class="font-mono text-xs bg-muted px-1.5 py-0.5 rounded">{{ flag.value }}</span>
            <span class="text-muted-foreground">{{ flag.label }}</span>
          </label>
          
          <div class="ml-auto flex items-center gap-2">
            <label class="flex items-center gap-1.5 text-sm cursor-pointer">
              <input v-model="findAll" type="checkbox" class="rounded border-gray-300" />
              <span>查找所有</span>
            </label>
          </div>
        </div>
        
        <!-- 标志位说明 -->
        <div class="mt-3 p-3 bg-blue-50/50 rounded-lg border border-blue-100">
          <div class="text-xs text-blue-600 font-medium mb-2">标志位说明：</div>
          <div class="grid grid-cols-2 gap-2 text-xs text-muted-foreground">
            <div class="flex items-start gap-2">
              <span class="font-mono bg-blue-100 text-blue-700 px-1.5 py-0.5 rounded">i</span>
              <span>忽略大小写，<code class="bg-muted px-1 rounded">/hello/i</code> 匹配 "Hello"</span>
            </div>
            <div class="flex items-start gap-2">
              <span class="font-mono bg-blue-100 text-blue-700 px-1.5 py-0.5 rounded">m</span>
              <span>多行模式，<code class="bg-muted px-1 rounded">^$</code> 匹配每行开头结尾</span>
            </div>
            <div class="flex items-start gap-2">
              <span class="font-mono bg-blue-100 text-blue-700 px-1.5 py-0.5 rounded">s</span>
              <span>单行模式，<code class="bg-muted px-1 rounded">.</code> 可匹配换行符</span>
            </div>
            <div class="flex items-start gap-2">
              <span class="font-mono bg-blue-100 text-blue-700 px-1.5 py-0.5 rounded">x</span>
              <span>忽略空白，可在正则中添加注释和换行</span>
            </div>
          </div>
        </div>
      </div>
      
      <!-- 测试文本区域 -->
      <div class="p-6 border-b">
        <div class="flex items-center justify-between mb-3">
          <div class="flex items-center gap-2">
            <div class="w-1 h-4 bg-primary rounded-full"></div>
            <label class="text-sm font-medium">测试文本</label>
          </div>
          <button
            @click="clearAll"
            class="text-xs text-muted-foreground hover:text-foreground px-2 py-1 rounded hover:bg-muted transition-colors flex items-center gap-1"
          >
            <Trash2 class="w-3 h-3" />
            清空
          </button>
        </div>
        
        <Textarea
          v-model="text"
          placeholder="输入要测试的文本内容..."
          rows="6"
          class="font-mono text-sm resize-none"
        />
      </div>
      
      <!-- 匹配结果区域 -->
      <div class="p-6">
        <div class="flex items-center justify-between mb-3">
          <div class="flex items-center gap-2">
            <div class="w-1 h-4 bg-green-500 rounded-full"></div>
            <label class="text-sm font-medium">匹配结果</label>
          </div>
          <span v-if="matchResult" :class="['text-sm font-medium', matchStatusClass]">
            {{ matchStatusText }}
          </span>
        </div>
        
        <!-- 高亮显示 -->
        <div
          class="min-h-[120px] max-h-[200px] overflow-auto p-4 bg-muted/20 rounded-lg border font-mono text-sm whitespace-pre-wrap"
          v-html="matchResult?.highlightedText || '<span class=\'text-muted-foreground\'>输入正则表达式和测试文本后，结果将显示在这里...</span>'"
        ></div>
        
        <!-- 匹配详情 -->
        <div v-if="matchResult?.matches?.length" class="mt-4 space-y-2">
          <div
            v-for="(match, index) in matchResult.matches"
            :key="index"
            class="p-3 bg-muted/30 rounded-lg border"
          >
            <div class="flex items-center gap-2 mb-2">
              <span class="text-xs font-medium bg-primary/10 text-primary px-2 py-0.5 rounded">
                匹配 {{ index + 1 }}
              </span>
              <span class="text-xs text-muted-foreground">
                位置: {{ match.start }} - {{ match.end }}
              </span>
            </div>
            <div class="font-mono text-sm bg-white p-2 rounded border">
              {{ match.fullMatch }}
            </div>
            
            <!-- 分组信息 -->
            <div v-if="match.groups.length > 1" class="mt-2 space-y-1">
              <div class="text-xs text-muted-foreground">分组:</div>
              <div
                v-for="group in match.groups.slice(1)"
                :key="group.index"
                class="flex items-center gap-2 text-sm"
              >
                <span class="text-xs bg-muted px-1.5 py-0.5 rounded">${{ group.index }}</span>
                <span class="font-mono">{{ group.value || '(空)' }}</span>
              </div>
            </div>
          </div>
        </div>
        
        <!-- 错误提示 -->
        <div
          v-if="error"
          class="mt-4 flex items-center gap-2 text-sm text-red-500 bg-red-50/80 p-3 rounded-lg border border-red-200"
        >
          <AlertCircle class="w-4 h-4 shrink-0" />
          {{ error }}
        </div>
      </div>
    </div>
    
    <!-- Toast 提示 -->
    <div
      v-if="toast.show"
      class="fixed bottom-4 right-4 bg-green-500 text-white px-4 py-2 rounded-lg shadow-lg flex items-center gap-2 z-50"
    >
      <Check class="w-4 h-4" />
      {{ toast.message }}
    </div>
  </NuxtLayout>
</template>
