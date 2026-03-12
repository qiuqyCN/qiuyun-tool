<script setup lang="ts">
import { ref } from 'vue'
import { CheckCircle, Copy, Download, Check, FileCode, ArrowRightLeft, Loader2, FileJson, FileType } from 'lucide-vue-next'
import { useToolExecutor } from '~/composables/useToolExecutor'
import { ToolType } from '~/types/tool'
import { generateFileName, downloadFile } from '~/utils/file'

// 操作类型枚举
enum ConvertOperation {
  YAML_TO_PROPERTIES = 'yaml-to-properties',
  PROPERTIES_TO_YAML = 'properties-to-yaml'
}

// 请求参数类型
interface ConverterParams {
  operation: string
  input: string
  sortKeys: boolean
}

// 响应结果类型
interface ConverterResult {
  success: boolean
  input: string
  output: string
  operation: string
  itemCount: number
  lineCount: number
  errorMessage?: string
}

// Toast 提示状态
const toast = ref({
  show: false,
  message: ''
})

// 显示提示
const showToast = (message: string) => {
  toast.value.message = message
  toast.value.show = true
  setTimeout(() => {
    toast.value.show = false
  }, 2000)
}

// 状态
const activeTab = ref<ConvertOperation>(ConvertOperation.YAML_TO_PROPERTIES)
const inputContent = ref('')
const outputContent = ref('')
const sortKeys = ref(false)

// 操作标签
const operationLabels: Record<ConvertOperation, string> = {
  [ConvertOperation.YAML_TO_PROPERTIES]: 'YAML → Properties',
  [ConvertOperation.PROPERTIES_TO_YAML]: 'Properties → YAML'
}

// 占位符
const placeholders: Record<ConvertOperation, string> = {
  [ConvertOperation.YAML_TO_PROPERTIES]: `# 示例 YAML 内容
# 数据库配置
database:
  host: localhost
  port: 3306
  username: root
  password: secret

# 应用配置
app:
  name: My Application
  version: 1.0.0
  debug: true

# 列表配置
servers:
  - server1.example.com
  - server2.example.com`,
  [ConvertOperation.PROPERTIES_TO_YAML]: `# 示例 Properties 内容
# 数据库配置
database.host=localhost
database.port=3306
database.username=root
database.password=secret

# 应用配置
app.name=My Application
app.version=1.0.0
app.debug=true

# 列表配置
servers[0]=server1.example.com
servers[1]=server2.example.com`
}

// 输入输出标签
const inputLabels: Record<ConvertOperation, string> = {
  [ConvertOperation.YAML_TO_PROPERTIES]: '输入 YAML',
  [ConvertOperation.PROPERTIES_TO_YAML]: '输入 Properties'
}

const outputLabels: Record<ConvertOperation, string> = {
  [ConvertOperation.YAML_TO_PROPERTIES]: '输出 Properties',
  [ConvertOperation.PROPERTIES_TO_YAML]: '输出 YAML'
}

// 使用工具执行器
const { execute, isLoading } = useToolExecutor<ConverterParams, ConverterResult>({
  toolCode: 'yaml-properties-converter',
  toolType: ToolType.INSTANT,
  onSuccess: (result) => {
    if (result.success) {
      outputContent.value = result.output
      showToast('转换成功！')
    } else {
      showToast(result.errorMessage || '转换失败')
    }
  },
  onError: (err) => {
    showToast(err)
  }
})

// 执行转换
const convert = async (operation: ConvertOperation) => {
  if (!inputContent.value.trim()) {
    showToast('请输入内容')
    return
  }

  await execute({
    operation,
    input: inputContent.value,
    sortKeys: sortKeys.value
  })
}

// 切换转换方向
const switchDirection = () => {
  activeTab.value = activeTab.value === ConvertOperation.YAML_TO_PROPERTIES
    ? ConvertOperation.PROPERTIES_TO_YAML
    : ConvertOperation.YAML_TO_PROPERTIES
  // 交换输入输出
  const temp = inputContent.value
  inputContent.value = outputContent.value
  outputContent.value = temp
}

// 清空
const clearAll = () => {
  inputContent.value = ''
  outputContent.value = ''
  sortKeys.value = false
}

// 复制结果
const copyOutput = async () => {
  if (!outputContent.value) return

  try {
    await navigator.clipboard.writeText(outputContent.value)
    showToast('已复制到剪贴板！')
  } catch (err) {
    // 降级方案
    const textArea = document.createElement('textarea')
    textArea.value = outputContent.value
    textArea.style.position = 'fixed'
    textArea.style.left = '-999999px'
    textArea.style.top = '-999999px'
    document.body.appendChild(textArea)
    textArea.focus()
    textArea.select()

    try {
      const successful = document.execCommand('copy')
      if (successful) {
        showToast('已复制到剪贴板！')
      } else {
        showToast('复制失败')
      }
    } catch (e) {
      showToast('复制失败')
    }

    document.body.removeChild(textArea)
  }
}

// 下载结果
const downloadOutput = () => {
  if (!outputContent.value) return

  const isYaml = activeTab.value === ConvertOperation.PROPERTIES_TO_YAML
  const extension = isYaml ? 'yaml' : 'properties'
  const fileName = generateFileName('yaml-properties', extension)
  downloadFile(outputContent.value, fileName, isYaml ? 'text/yaml' : 'text/plain')
}

// 处理
const handleProcess = () => {
  convert(activeTab.value)
}
</script>

<template>
  <NuxtLayout name="tool" tool-code="yaml-properties-converter">
    <!-- 工具执行区域 -->
    <div class="border border-border/40 rounded-xl overflow-hidden">
      <!-- 工具 Tabs -->
      <div class="border-b bg-muted/30">
        <div class="flex">
          <button
            v-for="(label, op) in operationLabels"
            :key="op"
            @click="activeTab = op as ConvertOperation"
            class="flex items-center gap-2 px-6 py-3 border-b-2 transition-colors"
            :class="activeTab === op
              ? 'border-primary bg-background text-primary'
              : 'border-transparent text-muted-foreground hover:text-foreground'"
          >
            <FileType v-if="op === ConvertOperation.YAML_TO_PROPERTIES" class="w-4 h-4" />
            <FileJson v-else class="w-4 h-4" />
            {{ label }}
          </button>
        </div>
      </div>

      <!-- 编辑区域 -->
      <div class="p-6">
        <div class="grid grid-cols-1 lg:grid-cols-2 gap-6">
          <!-- 输入区域 -->
          <div class="flex flex-col gap-3">
            <div class="flex items-center justify-between">
              <div class="flex items-center gap-2">
                <div class="w-1 h-4 bg-primary rounded-full"></div>
                <label class="text-sm font-medium">{{ inputLabels[activeTab] }}</label>
              </div>
              <div class="flex items-center gap-2">
                <Button @click="handleProcess" :disabled="isLoading" size="sm" class="rounded-full px-4">
                  <Loader2 v-if="isLoading" class="w-3 h-3 mr-1 animate-spin" />
                  <CheckCircle v-else class="w-3 h-3 mr-1" />
                  {{ isLoading ? '转换中...' : '执行' }}
                </Button>
                <button
                  @click="switchDirection"
                  class="text-xs text-muted-foreground hover:text-foreground px-2 py-1 rounded hover:bg-muted transition-colors flex items-center gap-1"
                >
                  <ArrowRightLeft class="w-3 h-3" />
                  切换
                </button>
                <button
                  @click="clearAll"
                  class="text-xs text-muted-foreground hover:text-foreground px-2 py-1 rounded hover:bg-muted transition-colors"
                >
                  清空
                </button>
              </div>
            </div>
            <div class="relative">
              <Textarea
                v-model="inputContent"
                :placeholder="placeholders[activeTab]"
                class="font-mono text-sm resize-none min-h-[400px] max-h-[600px] border-border/60 focus:border-primary"
              />
            </div>
            <!-- 设置选项 -->
            <div class="flex items-center gap-2">
              <input
                v-model="sortKeys"
                type="checkbox"
                id="sort-keys"
                class="w-4 h-4 rounded border-gray-300"
              />
              <label for="sort-keys" class="text-sm cursor-pointer">
                对键进行排序（按字母顺序）
              </label>
            </div>
          </div>

          <!-- 输出区域 -->
          <div class="flex flex-col gap-3">
            <div class="flex items-center justify-between">
              <div class="flex items-center gap-2">
                <div class="w-1 h-4 bg-green-500 rounded-full"></div>
                <label class="text-sm font-medium">{{ outputLabels[activeTab] }}</label>
              </div>
              <div class="flex gap-1">
                <button
                  v-if="outputContent"
                  @click="copyOutput"
                  class="text-xs text-muted-foreground hover:text-foreground px-2 py-1 rounded hover:bg-muted transition-colors flex items-center gap-1"
                >
                  <Copy class="w-3 h-3" />
                  复制
                </button>
                <button
                  v-if="outputContent"
                  @click="downloadOutput"
                  class="text-xs text-muted-foreground hover:text-foreground px-2 py-1 rounded hover:bg-muted transition-colors flex items-center gap-1"
                >
                  <Download class="w-3 h-3" />
                  下载
                </button>
              </div>
            </div>
            <Textarea
              v-model="outputContent"
              readonly
              class="font-mono text-sm resize-none bg-muted/20 min-h-[400px] max-h-[600px] border-border/60"
              placeholder="转换结果将显示在这里..."
            />
          </div>
        </div>

        <!-- 使用说明 -->
        <div class="mt-6 border rounded-lg p-4 space-y-2">
          <h3 class="text-sm font-medium">使用说明</h3>
          <ul class="text-sm text-muted-foreground space-y-1 list-disc list-inside">
            <li>支持标准的 YAML 格式与 Java Properties 格式（key=value）双向转换</li>
            <li>嵌套结构自动转换（YAML层级 ↔ 点号分隔键）</li>
            <li>支持数组索引格式转换（YAML列表 ↔ Properties servers[0]）</li>
            <li>自动识别数值和布尔值类型</li>
            <li>使用 # 开头的行会被识别为注释</li>
          </ul>
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
        class="fixed bottom-8 left-1/2 -translate-x-1/2 z-50 flex items-center gap-2 px-4 py-2.5 bg-foreground text-background rounded-lg shadow-lg"
      >
        <Check class="w-4 h-4 text-green-400" />
        <span class="text-sm font-medium">{{ toast.message }}</span>
      </div>
    </Transition>
  </NuxtLayout>
</template>
