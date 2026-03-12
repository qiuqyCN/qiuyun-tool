<script setup lang="ts">
import { ref, computed } from 'vue'
import { AlertCircle, CheckCircle, Copy, Download, Check, Loader2, FileText, FileCode, FileType, Eye, Settings } from 'lucide-vue-next'
import { useToolExecutor } from '~/composables/useToolExecutor'
import { ToolType } from '~/types/tool'

// 目标格式枚举
enum TargetFormat {
  HTML = 'html',
  PDF = 'pdf',
  WORD = 'word'
}

// 可选插件类型
interface OptionalPlugin {
  key: string
  label: string
}

// 请求参数类型
interface ConvertParams {
  markdown: string
  targetFormat: string
  enabledPlugins?: string[]
}

// 响应结果类型
interface ConvertResult {
  success: boolean
  result: string
  targetFormat: string
  contentType: string
  fileExtension: string
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
const inputContent = ref('')
const outputContent = ref('')
const targetFormat = ref<TargetFormat>(TargetFormat.HTML)
const showPreview = ref(false)
const enabledPlugins = ref<string[]>([])

// 可选插件列表
const optionalPlugins: OptionalPlugin[] = [
  { key: 'footnotes', label: '脚注支持' },
  { key: 'toc', label: '自动生成目录' },
  { key: 'superscript', label: '上标/下标' },
  { key: 'typographic', label: '排版美化' },
  { key: 'yamlFrontMatter', label: 'YAML前置元数据' }
]

// 格式标签
const formatLabels: Record<TargetFormat, string> = {
  [TargetFormat.HTML]: 'HTML',
  [TargetFormat.PDF]: 'PDF',
  [TargetFormat.WORD]: 'Word (DOCX)'
}

// 格式图标
const formatIcons: Record<TargetFormat, string> = {
  [TargetFormat.HTML]: 'FileCode',
  [TargetFormat.PDF]: 'FileText',
  [TargetFormat.WORD]: 'FileType'
}

// 占位符
const placeholder = `# Markdown 示例

## 标题

这是一段**加粗**的文字和*斜体*文字。

## 列表

- 列表项 1
- 列表项 2
- 列表项 3

## 代码

\`\`\`java
public class Hello {
    public static void main(String[] args) {
        System.out.println("Hello World!");
    }
}
\`\`\`

## 表格

| 姓名 | 年龄 | 城市 |
|------|------|------|
| 张三 | 25   | 北京 |
| 李四 | 30   | 上海 |

## 任务列表

- [x] 已完成任务
- [ ] 未完成任务

## 引用

> 这是一段引用文字。

## 链接

[秋云工具](https://qiuyun.com)`

// 使用工具执行器
const { execute, isLoading } = useToolExecutor<ConvertParams, ConvertResult>({
  toolCode: 'markdown-converter',
  toolType: ToolType.INSTANT,
  onSuccess: (result) => {
    if (result.success) {
      outputContent.value = result.result
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
const convert = async () => {
  if (!inputContent.value.trim()) {
    showToast('请输入 Markdown 内容')
    return
  }

  await execute({
    markdown: inputContent.value,
    targetFormat: targetFormat.value,
    enabledPlugins: enabledPlugins.value.length > 0 ? enabledPlugins.value : undefined
  })
}

// 清空
const clearAll = () => {
  inputContent.value = ''
  outputContent.value = ''
  enabledPlugins.value = []
}

// 切换插件启用状态
const togglePlugin = (pluginKey: string) => {
  const index = enabledPlugins.value.indexOf(pluginKey)
  if (index > -1) {
    enabledPlugins.value.splice(index, 1)
  } else {
    enabledPlugins.value.push(pluginKey)
  }
}

// 切换格式
const switchFormat = (format: TargetFormat) => {
  if (targetFormat.value !== format) {
    targetFormat.value = format
    // 清空输出内容，避免格式不匹配
    outputContent.value = ''
  }
}

// 复制结果
const copyOutput = async () => {
  if (!outputContent.value) return

  try {
    await navigator.clipboard.writeText(outputContent.value)
    showToast('已复制到剪贴板！')
  } catch (err) {
    showToast('复制失败')
  }
}

// 下载结果
const downloadOutput = () => {
  if (!outputContent.value) return

  let mimeType: string
  let fileName: string

  switch (targetFormat.value) {
    case TargetFormat.HTML:
      mimeType = 'text/html'
      fileName = 'output.html'
      break
    case TargetFormat.PDF:
      mimeType = 'application/pdf'
      fileName = 'output.pdf'
      break
    case TargetFormat.WORD:
      mimeType = 'application/vnd.openxmlformats-officedocument.wordprocessingml.document'
      fileName = 'output.docx'
      break
    default:
      mimeType = 'text/plain'
      fileName = 'output.txt'
  }

  // PDF 和 Word 是 Base64 编码
  if (targetFormat.value === TargetFormat.PDF || targetFormat.value === TargetFormat.WORD) {
    const byteCharacters = atob(outputContent.value)
    const byteNumbers = new Array(byteCharacters.length)
    for (let i = 0; i < byteCharacters.length; i++) {
      byteNumbers[i] = byteCharacters.charCodeAt(i)
    }
    const byteArray = new Uint8Array(byteNumbers)
    const blob = new Blob([byteArray], { type: mimeType })
    const url = URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.href = url
    link.download = fileName
    link.click()
    URL.revokeObjectURL(url)
  } else {
    // HTML 是文本
    const blob = new Blob([outputContent.value], { type: mimeType })
    const url = URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.href = url
    link.download = fileName
    link.click()
    URL.revokeObjectURL(url)
  }
}

// 预览 HTML
const previewHtml = () => {
  if (outputContent.value && targetFormat.value === TargetFormat.HTML) {
    const newWindow = window.open('', '_blank')
    if (newWindow) {
      newWindow.document.write(outputContent.value)
      newWindow.document.close()
    }
  }
}

// 是否是二进制格式（PDF/Word）
const isBinaryFormat = computed(() => {
  return targetFormat.value === TargetFormat.PDF || targetFormat.value === TargetFormat.WORD
})
</script>

<template>
  <NuxtLayout name="tool" tool-code="markdown-converter">
    <!-- 工具执行区域 -->
    <div class="border border-border/40 rounded-xl overflow-hidden">
      <!-- 标题栏 -->
      <div class="border-b bg-muted/30 px-6 py-4">
        <div class="flex items-center gap-2">
          <FileText class="w-5 h-5 text-primary" />
          <h2 class="text-lg font-semibold">Markdown格式转换</h2>
        </div>
        <p class="text-sm text-muted-foreground mt-1">
          将 Markdown 文档转换为 HTML、PDF 或 Word 格式
        </p>
      </div>

      <!-- 主内容区 -->
      <div class="p-6">
        <!-- 格式选择 -->
        <div class="flex gap-2 mb-4">
          <button
            v-for="(label, format) in formatLabels"
            :key="format"
            @click="switchFormat(format as TargetFormat)"
            class="flex items-center gap-2 px-4 py-2 rounded-lg border transition-colors"
            :class="targetFormat === format
              ? 'border-primary bg-primary/10 text-primary'
              : 'border-border hover:border-primary/50'"
          >
            <FileCode v-if="format === TargetFormat.HTML" class="w-4 h-4" />
            <FileText v-else-if="format === TargetFormat.PDF" class="w-4 h-4" />
            <FileType v-else class="w-4 h-4" />
            {{ label }}
          </button>
        </div>

        <!-- 可选插件 -->
        <div class="mb-6 p-4 border rounded-lg bg-muted/20">
          <div class="flex items-center gap-2 mb-3">
            <Settings class="w-4 h-4 text-muted-foreground" />
            <span class="text-sm font-medium">可选插件（点击启用）</span>
          </div>
          <div class="flex flex-wrap gap-2">
            <button
              v-for="plugin in optionalPlugins"
              :key="plugin.key"
              @click="togglePlugin(plugin.key)"
              class="px-3 py-1.5 text-xs rounded-full border transition-colors"
              :class="enabledPlugins.includes(plugin.key)
                ? 'border-primary bg-primary/10 text-primary'
                : 'border-border hover:border-primary/50 text-muted-foreground'"
            >
              <Check v-if="enabledPlugins.includes(plugin.key)" class="w-3 h-3 inline mr-1" />
              {{ plugin.label }}
            </button>
          </div>
        </div>

        <div class="grid grid-cols-1 lg:grid-cols-2 gap-6">
          <!-- 输入区域 -->
          <div class="flex flex-col gap-3">
            <div class="flex items-center justify-between">
              <div class="flex items-center gap-2">
                <div class="w-1 h-4 bg-primary rounded-full"></div>
                <label class="text-sm font-medium">输入 Markdown</label>
              </div>
              <div class="flex items-center gap-2">
                <Button @click="convert" :disabled="isLoading" size="sm" class="rounded-full px-4">
                  <Loader2 v-if="isLoading" class="w-3 h-3 mr-1 animate-spin" />
                  <CheckCircle v-else class="w-3 h-3 mr-1" />
                  {{ isLoading ? '转换中...' : '转换' }}
                </Button>
                <button
                  @click="clearAll"
                  class="text-xs text-muted-foreground hover:text-foreground px-2 py-1 rounded hover:bg-muted transition-colors"
                >
                  清空
                </button>
              </div>
            </div>
            <Textarea
              v-model="inputContent"
              :placeholder="placeholder"
              class="font-mono text-sm resize-none min-h-[400px] max-h-[600px] border-border/60 focus:border-primary"
            />
          </div>

          <!-- 输出区域 -->
          <div class="flex flex-col gap-3">
            <div class="flex items-center justify-between">
              <div class="flex items-center gap-2">
                <div class="w-1 h-4 bg-green-500 rounded-full"></div>
                <label class="text-sm font-medium">输出 {{ formatLabels[targetFormat] }}</label>
              </div>
              <div class="flex gap-1">
                <button
                  v-if="outputContent && !isBinaryFormat"
                  @click="copyOutput"
                  class="text-xs text-muted-foreground hover:text-foreground px-2 py-1 rounded hover:bg-muted transition-colors flex items-center gap-1"
                >
                  <Copy class="w-3 h-3" />
                  复制
                </button>
                <button
                  v-if="outputContent && targetFormat === TargetFormat.HTML"
                  @click="previewHtml"
                  class="text-xs text-muted-foreground hover:text-foreground px-2 py-1 rounded hover:bg-muted transition-colors flex items-center gap-1"
                >
                  <Eye class="w-3 h-3" />
                  预览
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
            <!-- HTML 格式显示文本框 -->
            <Textarea
              v-if="!isBinaryFormat"
              v-model="outputContent"
              readonly
              class="font-mono text-sm resize-none bg-muted/20 min-h-[400px] max-h-[600px] border-border/60"
              placeholder="转换结果将显示在这里..."
            />
            <!-- PDF/Word 格式显示下载卡片 -->
            <div
              v-else-if="outputContent"
              class="flex flex-col items-center justify-center min-h-[400px] max-h-[600px] bg-muted/20 border border-border/60 rounded-md p-8"
            >
              <div class="w-16 h-16 bg-primary/10 rounded-full flex items-center justify-center mb-4">
                <FileText v-if="targetFormat === TargetFormat.PDF" class="w-8 h-8 text-primary" />
                <FileType v-else class="w-8 h-8 text-primary" />
              </div>
              <h3 class="text-lg font-medium mb-2">{{ formatLabels[targetFormat] }} 文件已生成</h3>
              <p class="text-sm text-muted-foreground mb-6 text-center">
                文件已准备就绪，点击下方按钮下载
              </p>
              <Button @click="downloadOutput" class="rounded-full px-6">
                <Download class="w-4 h-4 mr-2" />
                下载 {{ formatLabels[targetFormat] }}
              </Button>
            </div>
            <div
              v-else
              class="flex items-center justify-center min-h-[400px] max-h-[600px] bg-muted/20 border border-border/60 rounded-md"
            >
              <p class="text-muted-foreground">转换结果将显示在这里...</p>
            </div>
          </div>
        </div>

        <!-- 使用说明 -->
        <div class="mt-6 border rounded-lg p-4 space-y-2">
          <h3 class="text-sm font-medium">使用说明</h3>
          <ul class="text-sm text-muted-foreground space-y-1 list-disc list-inside">
            <li>支持标准 Markdown 语法（标题、列表、代码块、表格等）</li>
            <li>支持 GitHub Flavored Markdown 扩展（任务列表、删除线、表情符号等）</li>
            <li>HTML 格式可直接预览和复制</li>
            <li>PDF 和 Word 格式需要下载后查看</li>
            <li>转换后的文档保留原始 Markdown 的格式和样式</li>
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
