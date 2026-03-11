<script setup lang="ts">
import { ref, computed } from 'vue'
import { AlertCircle, CheckCircle, Copy, Download, Check, Loader2, Code2, FileCode, Braces, Database, Coffee, FileType2 } from 'lucide-vue-next'
import { useToolExecutor } from '~/composables/useToolExecutor'
import { ToolType } from '~/types/tool'

// 语言类型枚举
enum LanguageType {
  HTML = 'html',
  CSS = 'css',
  JAVASCRIPT = 'javascript',
  JAVA = 'java',
  SQL = 'sql',
  XML = 'xml'
}

// 操作类型枚举
enum OperationType {
  FORMAT = 'format',
  COMPRESS = 'compress'
}

// 请求参数类型
interface CodeBeautifyParams {
  language: string
  operation: string
  input: string
}

// 响应结果类型
interface CodeBeautifyResult {
  success: boolean
  result: string
  language: string
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
const activeLanguage = ref<LanguageType>(LanguageType.HTML)
const activeOperation = ref<OperationType>(OperationType.FORMAT)
const inputCode = ref('')
const outputCode = ref('')
const error = ref('')

// 语言标签
const languageLabels: Record<LanguageType, string> = {
  [LanguageType.HTML]: 'HTML',
  [LanguageType.CSS]: 'CSS',
  [LanguageType.JAVASCRIPT]: 'JavaScript',
  [LanguageType.JAVA]: 'Java',
  [LanguageType.SQL]: 'SQL',
  [LanguageType.XML]: 'XML'
}

// 语言图标
const languageIcons: Record<LanguageType, any> = {
  [LanguageType.HTML]: FileCode,
  [LanguageType.CSS]: Code2,
  [LanguageType.JAVASCRIPT]: Braces,
  [LanguageType.JAVA]: Coffee,
  [LanguageType.SQL]: Database,
  [LanguageType.XML]: FileType2
}

// 操作标签
const operationLabels: Record<OperationType, string> = {
  [OperationType.FORMAT]: '格式化',
  [OperationType.COMPRESS]: '压缩'
}

// 占位符
const placeholders: Record<LanguageType, string> = {
  [LanguageType.HTML]: '<!DOCTYPE html>\n<html>\n<head>\n<title>示例</title>\n</head>\n<body>\n<h1>Hello World</h1>\n</body>\n</html>',
  [LanguageType.CSS]: 'body {\nmargin: 0;\npadding: 0;\nbackground: #f5f5f5;\n}\n\nh1 {\ncolor: #333;\nfont-size: 24px;\n}',
  [LanguageType.JAVASCRIPT]: 'function greet(name) {\nconsole.log("Hello, " + name);\nreturn "Welcome!";\n}\n\ngreet("World");',
  [LanguageType.JAVA]: 'public class HelloWorld {\npublic static void main(String[] args) {\nSystem.out.println("Hello, World!");\n}\n}',
  [LanguageType.SQL]: 'SELECT u.id, u.name, COUNT(o.id) as order_count\nFROM users u\nLEFT JOIN orders o ON u.id = o.user_id\nWHERE u.status = "active"\nGROUP BY u.id\nORDER BY order_count DESC;',
  [LanguageType.XML]: '<?xml version="1.0" encoding="UTF-8"?>\n<root>\n<user>\n<name>张三</name>\n<age>25</age>\n</user>\n</root>'
}

// 当前占位符
const currentPlaceholder = computed(() => placeholders[activeLanguage.value])

// 使用工具执行器
const { execute, isLoading } = useToolExecutor<CodeBeautifyParams, CodeBeautifyResult>({
  toolCode: 'code-beautify',
  toolType: ToolType.INSTANT,
  onSuccess: (result) => {
    if (result.success) {
      outputCode.value = result.result
      error.value = ''
    } else {
      error.value = result.errorMessage || '处理失败'
      outputCode.value = ''
    }
  },
  onError: (err) => {
    error.value = err
    outputCode.value = ''
  }
})

// 执行代码处理
const processCode = async () => {
  if (!inputCode.value.trim()) {
    error.value = `请输入 ${languageLabels[activeLanguage.value]} 代码`
    return
  }

  await execute({
    language: activeLanguage.value,
    operation: activeOperation.value,
    input: inputCode.value
  })
}

// 切换语言
const switchLanguage = (lang: LanguageType) => {
  activeLanguage.value = lang
  inputCode.value = ''
  outputCode.value = ''
  error.value = ''
}

// 切换操作
const switchOperation = (op: OperationType) => {
  activeOperation.value = op
  outputCode.value = ''
  error.value = ''
}

// 清空
const clearInput = () => {
  inputCode.value = ''
  outputCode.value = ''
  error.value = ''
}

// 复制结果
const copyOutput = async () => {
  if (outputCode.value) {
    try {
      await navigator.clipboard.writeText(outputCode.value)
      showToast('复制成功！')
    } catch (err) {
      showToast('复制失败，请手动复制')
    }
  }
}

// 下载结果
const downloadOutput = () => {
  if (outputCode.value) {
    const extensions: Record<LanguageType, string> = {
      [LanguageType.HTML]: 'html',
      [LanguageType.CSS]: 'css',
      [LanguageType.JAVASCRIPT]: 'js',
      [LanguageType.JAVA]: 'java',
      [LanguageType.SQL]: 'sql',
      [LanguageType.XML]: 'xml'
    }
    const mimeTypes: Record<LanguageType, string> = {
      [LanguageType.HTML]: 'text/html',
      [LanguageType.CSS]: 'text/css',
      [LanguageType.JAVASCRIPT]: 'application/javascript',
      [LanguageType.JAVA]: 'text/plain',
      [LanguageType.SQL]: 'text/plain',
      [LanguageType.XML]: 'application/xml'
    }

    const blob = new Blob([outputCode.value], { type: mimeTypes[activeLanguage.value] })
    const url = URL.createObjectURL(blob)
    const a = document.createElement('a')
    a.href = url
    a.download = `output.${extensions[activeLanguage.value]}`
    a.click()
    URL.revokeObjectURL(url)
  }
}
</script>

<template>
  <NuxtLayout name="tool" tool-code="code-beautify">
    <!-- 工具执行区域 -->
    <div class="border border-border/40 rounded-xl overflow-hidden">
      <!-- 语言选择 Tabs -->
      <div class="border-b bg-muted/30">
        <div class="flex">
          <button
            v-for="(label, lang) in languageLabels"
            :key="lang"
            @click="switchLanguage(lang as LanguageType)"
            class="flex items-center gap-2 px-6 py-3 border-b-2 transition-colors"
            :class="activeLanguage === lang
              ? 'border-primary bg-background text-primary'
              : 'border-transparent text-muted-foreground hover:text-foreground'"
          >
            <component :is="languageIcons[lang as LanguageType]" class="w-4 h-4" />
            {{ label }}
          </button>
        </div>
      </div>

      <!-- 操作选择 -->
      <div class="border-b bg-muted/20 px-6 py-3">
        <div class="flex items-center gap-4">
          <span class="text-sm text-muted-foreground">操作：</span>
          <div class="flex gap-2">
            <button
              v-for="(label, op) in operationLabels"
              :key="op"
              @click="switchOperation(op as OperationType)"
              class="px-4 py-1.5 text-sm rounded-md transition-colors"
              :class="activeOperation === op
                ? 'bg-primary text-primary-foreground'
                : 'bg-muted hover:bg-muted/80'"
            >
              {{ label }}
            </button>
          </div>
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
                <label class="text-sm font-medium">输入 {{ languageLabels[activeLanguage] }}</label>
              </div>
              <button
                @click="clearInput"
                class="text-xs text-muted-foreground hover:text-foreground px-2 py-1 rounded hover:bg-muted transition-colors"
              >
                清空
              </button>
            </div>
            <div class="relative">
              <Textarea
                v-model="inputCode"
                :placeholder="currentPlaceholder"
                class="font-mono text-sm resize-none min-h-[400px] max-h-[600px] border-border/60 focus:border-primary"
              />
            </div>
          </div>

          <!-- 输出区域 -->
          <div class="flex flex-col gap-3">
            <div class="flex items-center justify-between">
              <div class="flex items-center gap-2">
                <div class="w-1 h-4 bg-green-500 rounded-full"></div>
                <label class="text-sm font-medium">
                  {{ operationLabels[activeOperation] }}结果
                </label>
              </div>
              <div class="flex gap-1">
                <button
                  v-if="outputCode"
                  @click="copyOutput"
                  class="text-xs text-muted-foreground hover:text-foreground px-2 py-1 rounded hover:bg-muted transition-colors flex items-center gap-1"
                >
                  <Copy class="w-3 h-3" />
                  复制
                </button>
                <button
                  v-if="outputCode"
                  @click="downloadOutput"
                  class="text-xs text-muted-foreground hover:text-foreground px-2 py-1 rounded hover:bg-muted transition-colors flex items-center gap-1"
                >
                  <Download class="w-3 h-3" />
                  下载
                </button>
              </div>
            </div>
            <Textarea
              v-model="outputCode"
              readonly
              class="font-mono text-sm resize-none bg-muted/20 min-h-[400px] max-h-[600px] border-border/60"
              placeholder="处理结果将显示在这里..."
            />
          </div>
        </div>

        <!-- 操作按钮 - 居中显示 -->
        <div class="flex justify-center mt-6">
          <div class="flex items-center gap-3 bg-muted/30 px-6 py-3 rounded-full">
            <Button @click="processCode" :disabled="isLoading" size="lg" class="rounded-full px-8">
              <Loader2 v-if="isLoading" class="w-4 h-4 mr-2 animate-spin" />
              <CheckCircle v-else class="w-4 h-4 mr-2" />
              {{ isLoading ? '处理中...' : operationLabels[activeOperation] }}
            </Button>
          </div>
        </div>

        <!-- 错误提示 -->
        <div v-if="error" class="mt-4 flex items-center gap-2 text-sm text-red-500 bg-red-50/80 p-3 rounded-lg border border-red-200">
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
