<script setup lang="ts">
import { ref } from 'vue'
import { AlertCircle, CheckCircle, Copy, Download, Check, Loader2, ArrowRightLeft, FileJson, Coffee } from 'lucide-vue-next'
import { useToolExecutor } from '~/composables/useToolExecutor'
import { ToolType } from '~/types/tool'

// 操作类型枚举
enum ConvertOperation {
  JSON_TO_JAVA = 'json-to-java',
  JAVA_TO_JSON = 'java-to-json'
}

// 请求参数类型
interface ConvertParams {
  operation: string
  input: string
  packageName?: string
  useLombok?: boolean
  rootClassName?: string
}

// 响应结果类型
interface ConvertResult {
  success: boolean
  result: string
  operation: string
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
const activeTab = ref<ConvertOperation>(ConvertOperation.JSON_TO_JAVA)
const inputContent = ref('')
const outputContent = ref('')
const error = ref('')
const packageName = ref('com.example')
const useLombok = ref(true)
const rootClassName = ref('Root')

// 操作标签
const operationLabels: Record<ConvertOperation, string> = {
  [ConvertOperation.JSON_TO_JAVA]: 'JSON → Java',
  [ConvertOperation.JAVA_TO_JSON]: 'Java → JSON'
}

// 占位符
const placeholders: Record<ConvertOperation, string> = {
  [ConvertOperation.JSON_TO_JAVA]: `{
  "name": "张三",
  "age": 25,
  "email": "zhangsan@example.com",
  "address": {
    "city": "北京",
    "zipcode": "100000"
  },
  "hobbies": ["读书", "游泳"]
}`,
  [ConvertOperation.JAVA_TO_JSON]: `// 输入 Java 对象的 JSON 表示
{
  "name": "张三",
  "age": 25,
  "email": "zhangsan@example.com"
}`
}

// 输入输出标签
const inputLabels: Record<ConvertOperation, string> = {
  [ConvertOperation.JSON_TO_JAVA]: '输入 JSON',
  [ConvertOperation.JAVA_TO_JSON]: '输入 Java 对象'
}

const outputLabels: Record<ConvertOperation, string> = {
  [ConvertOperation.JSON_TO_JAVA]: '输出 Java 类',
  [ConvertOperation.JAVA_TO_JSON]: '输出 JSON'
}

// 使用工具执行器
const { execute, isLoading } = useToolExecutor<ConvertParams, ConvertResult>({
  toolCode: 'json-java-converter',
  toolType: ToolType.INSTANT,
  onSuccess: (result) => {
    if (result.success) {
      outputContent.value = result.result
      error.value = ''
    } else {
      error.value = result.errorMessage || '转换失败'
      outputContent.value = ''
    }
  },
  onError: (err) => {
    error.value = err
    outputContent.value = ''
  }
})

// 执行转换
const processConvert = async (operation: ConvertOperation) => {
  if (!inputContent.value.trim()) {
    error.value = activeTab.value === ConvertOperation.JSON_TO_JAVA ? '请输入 JSON 数据' : '请输入 Java 对象'
    return
  }

  const params: ConvertParams = {
    operation,
    input: inputContent.value
  }

  // JSON to Java 时添加额外参数
  if (operation === ConvertOperation.JSON_TO_JAVA) {
    params.packageName = packageName.value
    params.useLombok = useLombok.value
    params.rootClassName = rootClassName.value
  }

  await execute(params)
}

// 切换转换方向
const switchDirection = () => {
  activeTab.value = activeTab.value === ConvertOperation.JSON_TO_JAVA
    ? ConvertOperation.JAVA_TO_JSON
    : ConvertOperation.JSON_TO_JAVA
  // 交换输入输出
  const temp = inputContent.value
  inputContent.value = outputContent.value
  outputContent.value = temp
  error.value = ''
}

// 清空
const clearInput = () => {
  inputContent.value = ''
  outputContent.value = ''
  error.value = ''
}

// 复制结果
const copyOutput = async () => {
  if (outputContent.value) {
    try {
      await navigator.clipboard.writeText(outputContent.value)
      showToast('复制成功！')
    } catch (err) {
      showToast('复制失败，请手动复制')
    }
  }
}

// 下载结果
const downloadOutput = () => {
  if (outputContent.value) {
    const isJava = activeTab.value === ConvertOperation.JSON_TO_JAVA
    const blob = new Blob([outputContent.value], { type: isJava ? 'text/java' : 'application/json' })
    const url = URL.createObjectURL(blob)
    const a = document.createElement('a')
    a.href = url
    a.download = isJava ? `${rootClassName.value || 'Root'}.java` : 'output.json'
    a.click()
    URL.revokeObjectURL(url)
  }
}

// 处理
const handleProcess = () => {
  processConvert(activeTab.value)
}
</script>

<template>
  <NuxtLayout name="tool" tool-code="json-java-converter">
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
            <FileJson v-if="op === ConvertOperation.JSON_TO_JAVA" class="w-4 h-4" />
            <Coffee v-else class="w-4 h-4" />
            {{ label }}
          </button>
        </div>
      </div>

      <!-- 编辑区域 -->
      <div class="p-6">
        <div class="grid grid-cols-1 lg:grid-cols-2 gap-6">
          <!-- 左侧：输入和设置 -->
          <div class="space-y-4">
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
                    @click="clearInput"
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
            </div>

            <!-- JSON to Java 设置 -->
            <div v-if="activeTab === ConvertOperation.JSON_TO_JAVA" class="border rounded-lg p-4 space-y-4">
              <h3 class="text-sm font-medium">生成设置</h3>
              
              <!-- 包名 -->
              <div class="space-y-2">
                <label class="text-sm">包名</label>
                <input
                  v-model="packageName"
                  type="text"
                  placeholder="com.example"
                  class="w-full px-3 py-2 text-sm border rounded-md bg-background"
                />
              </div>

              <!-- 根类名 -->
              <div class="space-y-2">
                <label class="text-sm">根类名</label>
                <input
                  v-model="rootClassName"
                  type="text"
                  placeholder="Root"
                  class="w-full px-3 py-2 text-sm border rounded-md bg-background"
                />
              </div>

              <!-- Lombok 选项 -->
              <div class="flex items-center gap-2">
                <input
                  v-model="useLombok"
                  type="checkbox"
                  id="use-lombok"
                  class="w-4 h-4 rounded border-gray-300"
                />
                <label for="use-lombok" class="text-sm cursor-pointer">
                  使用 Lombok（@Data, @NoArgsConstructor, @AllArgsConstructor）
                </label>
              </div>
            </div>
          </div>

          <!-- 右侧：输出区域 -->
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

        <!-- 错误提示 -->
        <div v-if="error" class="mt-4 flex items-center gap-2 text-sm text-red-500 bg-red-50/80 p-3 rounded-lg border border-red-200">
          <AlertCircle class="w-4 h-4 shrink-0" />
          {{ error }}
        </div>

        <!-- 使用说明 -->
        <div class="mt-6 border rounded-lg p-4 space-y-2">
          <h3 class="text-sm font-medium">使用说明</h3>
          <ul class="text-sm text-muted-foreground space-y-1 list-disc list-inside">
            <li><strong>JSON → Java</strong>：根据 JSON 结构生成 Java POJO 类</li>
            <li><strong>Java → JSON</strong>：将 Java 对象的 JSON 表示格式化</li>
            <li>支持嵌套对象（自动生成内部类）</li>
            <li>支持数组/List类型</li>
            <li>自动推断字段类型（String/Integer/Long/Double/Boolean）</li>
            <li>可选 Lombok 注解简化代码</li>
            <li>生成 @JsonProperty 注解保持字段映射</li>
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
