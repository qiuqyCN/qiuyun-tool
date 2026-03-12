<script setup lang="ts">
import { ref, computed, watch, nextTick } from 'vue'
import { 
  FileText, 
  Eye, 
  Download, 
  Copy, 
  Check, 
  BarChart3, 
  List, 
  Settings,
  Moon,
  Sun,
  Type,
  AlignLeft,
  Code,
  Image,
  Link,
  Table,
  Heading,
  Trash2,
  CheckCircle,
  Loader2,
  Maximize2,
  Minimize2
} from 'lucide-vue-next'
import { useToolExecutor } from '~/composables/useToolExecutor'
import { ToolType } from '~/types/tool'
import { generateFileName, downloadFile } from '~/utils/file'

// 主题类型
enum Theme {
  DEFAULT = 'default',
  GITHUB = 'github',
  DARK = 'dark'
}

// 操作类型
enum Action {
  PREVIEW = 'preview',
  EXPORT = 'export',
  STATS = 'stats',
  TOC = 'toc'
}

// 编辑器选项
interface EditorOptions {
  theme: Theme
  enableSyntaxHighlight: boolean
  enableFootnotes: boolean
  enableToc: boolean
  enableSuperscript: boolean
  enableTypographic: boolean
  enableYamlFrontMatter: boolean
}

// 文档统计
interface DocumentStats {
  totalChars: number
  charsNoSpaces: number
  words: number
  lines: number
  codeLines: number
  headings: number
  images: number
  links: number
  tables: number
}

// 目录项
interface TocItem {
  title: string
  anchor: string
  level: number
}

// 响应结果
interface EditorResult {
  success: boolean
  action: string
  html?: string
  css?: string
  stats?: DocumentStats
  toc?: TocItem[]
  errorMessage?: string
}

// Toast 提示状态
const toast = ref({
  show: false,
  message: '',
  type: 'success' as 'success' | 'error'
})

const showToast = (message: string, type: 'success' | 'error' = 'success') => {
  toast.value.message = message
  toast.value.type = type
  toast.value.show = true
  setTimeout(() => {
    toast.value.show = false
  }, 2000)
}

// 状态
const inputContent = ref('')
const previewHtml = ref('')
const currentAction = ref<Action>(Action.PREVIEW)
const isFullscreen = ref(false)
const activeTab = ref<'edit' | 'preview' | 'split'>('split')
const showStatsModal = ref(false)
const showTocModal = ref(false)
const showSettingsModal = ref(false)
const documentStats = ref<DocumentStats | null>(null)
const tocItems = ref<TocItem[]>([])

// 编辑器选项
const options = ref<EditorOptions>({
  theme: Theme.DEFAULT,
  enableSyntaxHighlight: true,
  enableFootnotes: false,
  enableToc: false,
  enableSuperscript: false,
  enableTypographic: false,
  enableYamlFrontMatter: false
})

// 主题标签
const themeLabels: Record<Theme, string> = {
  [Theme.DEFAULT]: '默认',
  [Theme.GITHUB]: 'GitHub',
  [Theme.DARK]: '深色'
}

// 示例内容
const sampleContent = `# Markdown 编辑器

欢迎使用 **Markdown 编辑器**！这是一个功能强大的在线编辑工具。

## 功能特性

- 📝 **实时预览** - 边写边看效果
- 🎨 **多主题支持** - 默认、GitHub、深色主题
- 📊 **字数统计** - 详细的文档统计信息
- 📑 **目录生成** - 自动生成文档目录
- 💾 **HTML 导出** - 导出为 HTML 文件

## 语法示例

### 1. 标题

# 一级标题
## 二级标题
### 三级标题

### 2. 文本样式

**粗体文本**、*斜体文本*、~~删除线~~、==高亮==

### 3. 列表

无序列表：
- 项目一
- 项目二
- 项目三

有序列表：
1. 第一步
2. 第二步
3. 第三步

任务列表：
- [x] 已完成任务
- [ ] 未完成任务

### 4. 代码

行内代码：\`console.log('Hello')\`

代码块：
\`\`\`java
public class HelloWorld {
    public static void main(String[] args) {
        System.out.println("Hello, World!");
    }
}
\`\`\`

### 5. 表格

| 功能 | 描述 | 状态 |
|------|------|------|
| 实时预览 | 边写边看 | ✅ |
| 主题切换 | 多种风格 | ✅ |
| 导出功能 | HTML 格式 | ✅ |

### 6. 引用

> 这是一段引用文字。
> 可以有多行。

### 7. 链接和图片

[访问秋云工具](https://qiuyun.com)

![示例图片](https://via.placeholder.com/400x200/3b82f6/ffffff?text=Markdown)

### 8. 分隔线

---

## 开始使用

在左侧编辑区输入 Markdown 内容，右侧即可实时预览效果！
`

// 使用工具执行器
const { execute, isLoading } = useToolExecutor<{ 
  markdown: string
  action: string
  options: EditorOptions
}, EditorResult>({
  toolCode: 'markdown-editor',
  toolType: ToolType.INSTANT,
  onSuccess: (result) => {
    if (result.success) {
      handleSuccess(result)
    } else {
      showToast(result.errorMessage || '操作失败', 'error')
    }
  },
  onError: (err) => {
    showToast(err, 'error')
  }
})

// 处理成功响应
const handleSuccess = (result: EditorResult) => {
  switch (result.action) {
    case Action.PREVIEW:
    case Action.EXPORT:
      if (result.html) {
        previewHtml.value = result.html
      }
      break
    case Action.STATS:
      if (result.stats) {
        documentStats.value = result.stats
        showStatsModal.value = true
      }
      break
    case Action.TOC:
      if (result.toc) {
        tocItems.value = result.toc
        showTocModal.value = true
      }
      break
  }
}

// 执行操作
const executeAction = async (action: Action) => {
  if (!inputContent.value.trim() && action !== Action.PREVIEW) {
    showToast('请输入 Markdown 内容', 'error')
    return
  }

  currentAction.value = action
  await execute({
    markdown: inputContent.value,
    action: action,
    options: options.value
  })
}

// 防抖预览
let previewTimeout: NodeJS.Timeout | null = null
const debouncedPreview = () => {
  if (previewTimeout) {
    clearTimeout(previewTimeout)
  }
  previewTimeout = setTimeout(() => {
    if (inputContent.value.trim()) {
      executeAction(Action.PREVIEW)
    }
  }, 500)
}

// 监听输入变化，自动预览
watch(inputContent, () => {
  debouncedPreview()
}, { immediate: true })

// 监听选项变化，重新预览
watch(options, () => {
  debouncedPreview()
}, { deep: true })

// 加载示例
const loadSample = () => {
  inputContent.value = sampleContent
  showToast('已加载示例内容')
}

// 清空内容
const clearContent = () => {
  if (inputContent.value) {
    if (confirm('确定要清空所有内容吗？')) {
      inputContent.value = ''
      previewHtml.value = ''
      showToast('内容已清空')
    }
  }
}

// 复制内容
const copyContent = async () => {
  if (!inputContent.value) return
  try {
    await navigator.clipboard.writeText(inputContent.value)
    showToast('已复制到剪贴板')
  } catch {
    showToast('复制失败', 'error')
  }
}

// 导出 HTML
const exportHtml = async () => {
  if (!inputContent.value.trim()) {
    showToast('请输入 Markdown 内容', 'error')
    return
  }

  await executeAction(Action.EXPORT)
  
  nextTick(() => {
    if (previewHtml.value) {
      const fileName = generateFileName('markdown', 'html')
      downloadFile(previewHtml.value, fileName, 'text/html')
      showToast('HTML 文件已下载')
    }
  })
}

// 插入 Markdown 语法
const insertMarkdown = (type: string) => {
  const textarea = document.querySelector('textarea') as HTMLTextAreaElement
  if (!textarea) return

  const start = textarea.selectionStart
  const end = textarea.selectionEnd
  const selectedText = inputContent.value.substring(start, end)
  let insertion = ''

  switch (type) {
    case 'bold':
      insertion = `**${selectedText || '粗体文本'}**`
      break
    case 'italic':
      insertion = `*${selectedText || '斜体文本'}*`
      break
    case 'strikethrough':
      insertion = `~~${selectedText || '删除线'}~~`
      break
    case 'heading':
      insertion = `\n## ${selectedText || '标题'}\n`
      break
    case 'code':
      insertion = `\`\`\`\n${selectedText || '代码块'}\n\`\`\``
      break
    case 'quote':
      insertion = `\n> ${selectedText || '引用文本'}\n`
      break
    case 'link':
      insertion = `[${selectedText || '链接文本'}](https://example.com)`
      break
    case 'image':
      insertion = `![${selectedText || '图片描述'}](https://example.com/image.png)`
      break
    case 'table':
      insertion = `\n| 列1 | 列2 | 列3 |\n|-----|-----|-----|\n| A | B | C |\n`
      break
    case 'list':
      insertion = `\n- 项目1\n- 项目2\n- 项目3\n`
      break
    case 'ordered-list':
      insertion = `\n1. 项目1\n2. 项目2\n3. 项目3\n`
      break
    case 'task':
      insertion = `\n- [ ] 待办任务\n- [x] 已完成任务\n`
      break
  }

  const newContent = inputContent.value.substring(0, start) + insertion + inputContent.value.substring(end)
  inputContent.value = newContent
  
  nextTick(() => {
    textarea.focus()
    const newCursorPos = start + insertion.length
    textarea.setSelectionRange(newCursorPos, newCursorPos)
  })
}

// 切换全屏
const toggleFullscreen = () => {
  isFullscreen.value = !isFullscreen.value
}

// 格式化数字
const formatNumber = (num: number) => {
  return num.toLocaleString('zh-CN')
}
</script>

<template>
  <NuxtLayout name="tool" tool-code="markdown-editor">
    <div :class="['border border-border/40 rounded-xl overflow-hidden bg-card', isFullscreen ? 'fixed inset-4 z-50' : '']">
      <!-- 标题栏 -->
      <div class="border-b bg-muted/30 px-4 py-3 flex items-center justify-between">
        <div class="flex items-center gap-2">
          <FileText class="w-5 h-5 text-primary" />
          <h2 class="text-lg font-semibold">Markdown 编辑器</h2>
        </div>
        <div class="flex items-center gap-1">
          <button
            @click="showSettingsModal = true"
            class="p-2 rounded-lg hover:bg-muted transition-colors"
            title="设置"
          >
            <Settings class="w-4 h-4" />
          </button>
          <button
            @click="toggleFullscreen"
            class="p-2 rounded-lg hover:bg-muted transition-colors"
            :title="isFullscreen ? '退出全屏' : '全屏'"
          >
            <Minimize2 v-if="isFullscreen" class="w-4 h-4" />
            <Maximize2 v-else class="w-4 h-4" />
          </button>
        </div>
      </div>

      <!-- 工具栏 -->
      <div class="border-b bg-muted/20 px-4 py-2 flex flex-wrap items-center gap-1">
        <!-- 格式工具 -->
        <div class="flex items-center gap-0.5 pr-2 border-r">
          <button @click="insertMarkdown('heading')" class="p-1.5 rounded hover:bg-muted" title="标题">
            <Heading class="w-4 h-4" />
          </button>
          <button @click="insertMarkdown('bold')" class="p-1.5 rounded hover:bg-muted font-bold text-sm" title="粗体">
            B
          </button>
          <button @click="insertMarkdown('italic')" class="p-1.5 rounded hover:bg-muted italic text-sm" title="斜体">
            I
          </button>
          <button @click="insertMarkdown('strikethrough')" class="p-1.5 rounded hover:bg-muted line-through text-sm" title="删除线">
            S
          </button>
        </div>

        <!-- 列表工具 -->
        <div class="flex items-center gap-0.5 px-2 border-r">
          <button @click="insertMarkdown('list')" class="p-1.5 rounded hover:bg-muted" title="无序列表">
            <AlignLeft class="w-4 h-4" />
          </button>
          <button @click="insertMarkdown('ordered-list')" class="p-1.5 rounded hover:bg-muted" title="有序列表">
            <List class="w-4 h-4" />
          </button>
          <button @click="insertMarkdown('task')" class="p-1.5 rounded hover:bg-muted" title="任务列表">
            <CheckCircle class="w-4 h-4" />
          </button>
        </div>

        <!-- 插入工具 -->
        <div class="flex items-center gap-0.5 px-2 border-r">
          <button @click="insertMarkdown('code')" class="p-1.5 rounded hover:bg-muted" title="代码块">
            <Code class="w-4 h-4" />
          </button>
          <button @click="insertMarkdown('quote')" class="p-1.5 rounded hover:bg-muted" title="引用">
            <Type class="w-4 h-4" />
          </button>
          <button @click="insertMarkdown('link')" class="p-1.5 rounded hover:bg-muted" title="链接">
            <Link class="w-4 h-4" />
          </button>
          <button @click="insertMarkdown('image')" class="p-1.5 rounded hover:bg-muted" title="图片">
            <Image class="w-4 h-4" />
          </button>
          <button @click="insertMarkdown('table')" class="p-1.5 rounded hover:bg-muted" title="表格">
            <Table class="w-4 h-4" />
          </button>
        </div>

        <!-- 操作工具 -->
        <div class="flex items-center gap-0.5 pl-2">
          <button @click="loadSample" class="px-2 py-1.5 text-xs rounded hover:bg-muted" title="加载示例">
            示例
          </button>
          <button @click="copyContent" class="p-1.5 rounded hover:bg-muted" title="复制">
            <Copy class="w-4 h-4" />
          </button>
          <button @click="clearContent" class="p-1.5 rounded hover:bg-muted text-red-500" title="清空">
            <Trash2 class="w-4 h-4" />
          </button>
        </div>

        <!-- 右侧操作 -->
        <div class="flex items-center gap-1 ml-auto">
          <button
            @click="executeAction(Action.STATS)"
            :disabled="isLoading || !inputContent"
            class="flex items-center gap-1 px-3 py-1.5 text-sm rounded-lg border hover:bg-muted transition-colors"
          >
            <BarChart3 class="w-4 h-4" />
            统计
          </button>
          <button
            @click="executeAction(Action.TOC)"
            :disabled="isLoading || !inputContent"
            class="flex items-center gap-1 px-3 py-1.5 text-sm rounded-lg border hover:bg-muted transition-colors"
          >
            <List class="w-4 h-4" />
            目录
          </button>
          <button
            @click="exportHtml"
            :disabled="isLoading || !inputContent"
            class="flex items-center gap-1 px-3 py-1.5 text-sm rounded-lg bg-primary text-primary-foreground hover:bg-primary/90 transition-colors"
          >
            <Download class="w-4 h-4" />
            导出
          </button>
        </div>
      </div>

      <!-- 视图切换标签 -->
      <div class="border-b bg-muted/10 px-4 py-2 flex items-center gap-2">
        <button
          @click="activeTab = 'edit'"
          class="px-3 py-1 text-sm rounded-md transition-colors"
          :class="activeTab === 'edit' ? 'bg-primary text-primary-foreground' : 'hover:bg-muted'"
        >
          编辑
        </button>
        <button
          @click="activeTab = 'split'"
          class="px-3 py-1 text-sm rounded-md transition-colors"
          :class="activeTab === 'split' ? 'bg-primary text-primary-foreground' : 'hover:bg-muted'"
        >
          分屏
        </button>
        <button
          @click="activeTab = 'preview'"
          class="px-3 py-1 text-sm rounded-md transition-colors"
          :class="activeTab === 'preview' ? 'bg-primary text-primary-foreground' : 'hover:bg-muted'"
        >
          预览
        </button>
      </div>

      <!-- 主编辑区 -->
      <div class="flex" :class="isFullscreen ? 'h-[calc(100%-140px)]' : 'min-h-[500px] max-h-[700px]'">
        <!-- 编辑区 -->
        <div
          v-show="activeTab === 'edit' || activeTab === 'split'"
          :class="[
            'flex flex-col border-r',
            activeTab === 'split' ? 'w-1/2' : 'w-full'
          ]"
        >
          <div class="flex items-center justify-between px-4 py-2 bg-muted/20 border-b">
            <span class="text-xs text-muted-foreground">Markdown</span>
            <span class="text-xs text-muted-foreground">{{ inputContent.length }} 字符</span>
          </div>
          <textarea
            v-model="inputContent"
            placeholder="在此输入 Markdown 内容..."
            class="flex-1 w-full p-4 font-mono text-sm resize-none bg-transparent focus:outline-none"
            spellcheck="false"
          />
        </div>

        <!-- 预览区 -->
        <div
          v-show="activeTab === 'preview' || activeTab === 'split'"
          :class="[
            'flex flex-col bg-background',
            activeTab === 'split' ? 'w-1/2' : 'w-full'
          ]"
        >
          <div class="flex items-center justify-between px-4 py-2 bg-muted/20 border-b">
            <span class="text-xs text-muted-foreground">预览</span>
            <div class="flex items-center gap-1">
              <button
                v-for="(label, theme) in themeLabels"
                :key="theme"
                @click="options.theme = theme"
                class="px-2 py-0.5 text-xs rounded"
                :class="options.theme === theme ? 'bg-primary text-primary-foreground' : 'hover:bg-muted'"
              >
                {{ label }}
              </button>
            </div>
          </div>
          <div class="flex-1 overflow-auto">
            <div
              v-if="previewHtml"
              class="markdown-preview"
              v-html="previewHtml"
            />
            <div v-else class="flex items-center justify-center h-full text-muted-foreground">
              <div class="text-center">
                <Eye class="w-12 h-12 mx-auto mb-4 opacity-50" />
                <p>输入 Markdown 内容以查看预览</p>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 统计弹窗 -->
    <div
      v-if="showStatsModal"
      class="fixed inset-0 bg-black/50 z-50 flex items-center justify-center"
      @click.self="showStatsModal = false"
    >
      <div class="bg-card border rounded-xl p-6 w-96 max-w-[90vw]">
        <div class="flex items-center justify-between mb-4">
          <h3 class="text-lg font-semibold flex items-center gap-2">
            <BarChart3 class="w-5 h-5 text-primary" />
            文档统计
          </h3>
          <button @click="showStatsModal = false" class="p-1 rounded hover:bg-muted">
            <span class="sr-only">关闭</span>
            <svg class="w-5 h-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12" />
            </svg>
          </button>
        </div>
        <div v-if="documentStats" class="space-y-3">
          <div class="grid grid-cols-2 gap-3">
            <div class="p-3 bg-muted/30 rounded-lg text-center">
              <div class="text-2xl font-bold text-primary">{{ formatNumber(documentStats.words) }}</div>
              <div class="text-xs text-muted-foreground">字数</div>
            </div>
            <div class="p-3 bg-muted/30 rounded-lg text-center">
              <div class="text-2xl font-bold text-primary">{{ formatNumber(documentStats.totalChars) }}</div>
              <div class="text-xs text-muted-foreground">字符数</div>
            </div>
            <div class="p-3 bg-muted/30 rounded-lg text-center">
              <div class="text-2xl font-bold text-primary">{{ formatNumber(documentStats.lines) }}</div>
              <div class="text-xs text-muted-foreground">行数</div>
            </div>
            <div class="p-3 bg-muted/30 rounded-lg text-center">
              <div class="text-2xl font-bold text-primary">{{ formatNumber(documentStats.codeLines) }}</div>
              <div class="text-xs text-muted-foreground">代码行数</div>
            </div>
          </div>
          <div class="border-t pt-3 space-y-2">
            <div class="flex justify-between text-sm">
              <span class="text-muted-foreground">标题数</span>
              <span class="font-medium">{{ documentStats.headings }}</span>
            </div>
            <div class="flex justify-between text-sm">
              <span class="text-muted-foreground">图片数</span>
              <span class="font-medium">{{ documentStats.images }}</span>
            </div>
            <div class="flex justify-between text-sm">
              <span class="text-muted-foreground">链接数</span>
              <span class="font-medium">{{ documentStats.links }}</span>
            </div>
            <div class="flex justify-between text-sm">
              <span class="text-muted-foreground">表格数</span>
              <span class="font-medium">{{ documentStats.tables }}</span>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 目录弹窗 -->
    <div
      v-if="showTocModal"
      class="fixed inset-0 bg-black/50 z-50 flex items-center justify-center"
      @click.self="showTocModal = false"
    >
      <div class="bg-card border rounded-xl p-6 w-96 max-w-[90vw] max-h-[80vh] flex flex-col">
        <div class="flex items-center justify-between mb-4">
          <h3 class="text-lg font-semibold flex items-center gap-2">
            <List class="w-5 h-5 text-primary" />
            文档目录
          </h3>
          <button @click="showTocModal = false" class="p-1 rounded hover:bg-muted">
            <span class="sr-only">关闭</span>
            <svg class="w-5 h-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12" />
            </svg>
          </button>
        </div>
        <div class="flex-1 overflow-auto">
          <div v-if="tocItems.length > 0" class="space-y-1">
            <div
              v-for="item in tocItems"
              :key="item.anchor"
              class="py-1.5 px-2 rounded hover:bg-muted cursor-pointer text-sm"
              :style="{ paddingLeft: `${(item.level - 1) * 16 + 8}px` }"
              @click="showTocModal = false"
            >
              {{ item.title }}
            </div>
          </div>
          <div v-else class="text-center text-muted-foreground py-8">
            未找到标题
          </div>
        </div>
      </div>
    </div>

    <!-- 设置弹窗 -->
    <div
      v-if="showSettingsModal"
      class="fixed inset-0 bg-black/50 z-50 flex items-center justify-center"
      @click.self="showSettingsModal = false"
    >
      <div class="bg-card border rounded-xl p-6 w-80 max-w-[90vw]">
        <div class="flex items-center justify-between mb-4">
          <h3 class="text-lg font-semibold flex items-center gap-2">
            <Settings class="w-5 h-5 text-primary" />
            编辑器设置
          </h3>
          <button @click="showSettingsModal = false" class="p-1 rounded hover:bg-muted">
            <span class="sr-only">关闭</span>
            <svg class="w-5 h-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12" />
            </svg>
          </button>
        </div>
        <div class="space-y-4">
          <div>
            <label class="text-sm font-medium mb-2 block">主题</label>
            <div class="flex gap-2">
              <button
                v-for="(label, theme) in themeLabels"
                :key="theme"
                @click="options.theme = theme"
                class="flex-1 px-3 py-2 text-sm rounded-lg border transition-colors"
                :class="options.theme === theme ? 'border-primary bg-primary/10 text-primary' : 'hover:bg-muted'"
              >
                {{ label }}
              </button>
            </div>
          </div>
          <div class="space-y-2">
            <label class="flex items-center gap-2 cursor-pointer">
              <input v-model="options.enableSyntaxHighlight" type="checkbox" class="rounded">
              <span class="text-sm">代码高亮</span>
            </label>
            <label class="flex items-center gap-2 cursor-pointer">
              <input v-model="options.enableFootnotes" type="checkbox" class="rounded">
              <span class="text-sm">脚注支持</span>
            </label>
            <label class="flex items-center gap-2 cursor-pointer">
              <input v-model="options.enableToc" type="checkbox" class="rounded">
              <span class="text-sm">自动生成目录</span>
            </label>
            <label class="flex items-center gap-2 cursor-pointer">
              <input v-model="options.enableSuperscript" type="checkbox" class="rounded">
              <span class="text-sm">上标/下标</span>
            </label>
            <label class="flex items-center gap-2 cursor-pointer">
              <input v-model="options.enableTypographic" type="checkbox" class="rounded">
              <span class="text-sm">排版美化</span>
            </label>
            <label class="flex items-center gap-2 cursor-pointer">
              <input v-model="options.enableYamlFrontMatter" type="checkbox" class="rounded">
              <span class="text-sm">YAML 前置元数据</span>
            </label>
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

<style scoped>
.markdown-preview {
  padding: 20px;
  min-height: 100%;
}

.markdown-preview :deep(.markdown-body) {
  max-width: 100%;
}
</style>
