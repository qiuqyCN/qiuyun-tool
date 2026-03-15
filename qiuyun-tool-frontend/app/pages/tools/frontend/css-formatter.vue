<script setup lang="ts">
import { ref } from 'vue'
import { Copy, Check, Palette, RefreshCw, FileCode2, FileMinus2 } from 'lucide-vue-next'

const inputCss = ref('')
const outputCss = ref('')
const mode = ref<'format' | 'minify'>('format')
const indentSize = ref(2)
const useTabs = ref(false)

const toast = ref({ show: false, message: '' })

const showToast = (message: string) => {
  toast.value.message = message
  toast.value.show = true
  setTimeout(() => toast.value.show = false, 2000)
}

const formatCss = (css: string, indentSize: number, useTabs: boolean) => {
  const indent = useTabs ? '\t' : ' '.repeat(indentSize)
  let result = ''
  let level = 0
  let inComment = false
  let inQuotes = false
  let quoteChar = ''
  
  css = css.trim()
  
  for (let i = 0; i < css.length; i++) {
    const char = css[i]
    const nextChar = css[i + 1] || ''
    
    if (!inQuotes && !inComment && char === '/' && nextChar === '*') {
      inComment = true
      result += char + nextChar
      i++
      continue
    }
    
    if (inComment && char === '*' && nextChar === '/') {
      inComment = false
      result += char + nextChar
      i++
      continue
    }
    
    if (!inComment && !inQuotes && (char === '"' || char === "'")) {
      if (quoteChar === '') {
        quoteChar = char
        inQuotes = true
      } else if (quoteChar === char) {
        quoteChar = ''
        inQuotes = false
      }
      result += char
      continue
    }
    
    if (inComment || inQuotes) {
      result += char
      continue
    }
    
    if (char === '{') {
      result += ' {\n'
      level++
      continue
    }
    
    if (char === '}') {
      level = Math.max(0, level - 1)
      result = result.trimEnd() + '\n' + indent.repeat(level) + '}\n'
      continue
    }
    
    if (char === ';') {
      result += ';\n' + indent.repeat(level)
      continue
    }
    
    if (char === ' ' && (result.endsWith(' ') || result.endsWith('\n') || result.endsWith(';') || result.endsWith('{'))) {
      continue
    }
    
    if (char === '\n' || char === '\r') {
      continue
    }
    
    result += char
  }
  
  return result.replace(/\n\s*\n/g, '\n').trim()
}

const minifyCss = (css: string) => {
  return css
    .replace(/\/\*[\s\S]*?\*\//g, '')
    .replace(/\s+/g, ' ')
    .replace(/\s*{\s*/g, '{')
    .replace(/\s*}\s*/g, '}')
    .replace(/\s*;\s*/g, ';')
    .replace(/\s*:\s*/g, ':')
    .replace(/\s*,\s*/g, ',')
    .replace(/;}/g, '}')
    .trim()
}

const process = () => {
  if (!inputCss.value.trim()) {
    outputCss.value = ''
    return
  }
  
  try {
    if (mode.value === 'format') {
      outputCss.value = formatCss(inputCss.value, indentSize.value, useTabs.value)
    } else {
      outputCss.value = minifyCss(inputCss.value)
    }
  } catch (e) {
    outputCss.value = '处理失败，请检查CSS格式'
  }
}

const copyOutput = async () => {
  if (!outputCss.value) return
  try {
    await navigator.clipboard.writeText(outputCss.value)
    showToast('已复制')
  } catch {
    showToast('复制失败')
  }
}

const reset = () => {
  inputCss.value = ''
  outputCss.value = ''
}
</script>

<template>
  <NuxtLayout name="tool" tool-code="css-formatter">
    <div class="border border-border/40 rounded-xl overflow-hidden">
      <div class="p-4 border-b bg-muted/20">
        <div class="flex flex-wrap items-center gap-4">
          <div class="flex items-center gap-2">
            <button
              @click="mode = 'format'"
              class="px-3 py-1.5 rounded text-sm transition-colors flex items-center gap-1"
              :class="mode === 'format' ? 'bg-primary text-white' : 'hover:bg-muted'"
            >
              <FileCode2 class="w-4 h-4" />
              格式化
            </button>
            <button
              @click="mode = 'minify'"
              class="px-3 py-1.5 rounded text-sm transition-colors flex items-center gap-1"
              :class="mode === 'minify' ? 'bg-primary text-white' : 'hover:bg-muted'"
            >
              <FileMinus2 class="w-4 h-4" />
              压缩
            </button>
          </div>

          <template v-if="mode === 'format'">
            <div class="flex items-center gap-2">
              <span class="text-xs text-muted-foreground">缩进：</span>
              <button
                @click="indentSize = 2"
                class="px-2 py-1 rounded text-xs transition-colors"
                :class="indentSize === 2 ? 'bg-primary text-white' : 'hover:bg-muted'"
              >
                2 空格
              </button>
              <button
                @click="indentSize = 4"
                class="px-2 py-1 rounded text-xs transition-colors"
                :class="indentSize === 4 ? 'bg-primary text-white' : 'hover:bg-muted'"
              >
                4 空格
              </button>
              <button
                @click="useTabs = !useTabs"
                class="px-2 py-1 rounded text-xs transition-colors"
                :class="useTabs ? 'bg-primary text-white' : 'hover:bg-muted'"
              >
                Tab
              </button>
            </div>
          </template>

          <div class="ml-auto flex items-center gap-2">
            <button
              @click="reset"
              class="text-xs text-muted-foreground hover:text-foreground px-3 py-1.5 rounded hover:bg-muted transition-colors flex items-center gap-1"
            >
              <RefreshCw class="w-3 h-3" />
              清空
            </button>
            <button
              v-if="outputCss"
              @click="copyOutput"
              class="text-xs text-white bg-primary hover:bg-primary/90 px-3 py-1.5 rounded transition-colors flex items-center gap-1"
            >
              <Copy class="w-3 h-3" />
              复制
            </button>
          </div>
        </div>
      </div>

      <div class="p-6">
        <div class="grid grid-cols-1 lg:grid-cols-2 gap-6">
          <div>
            <div class="flex items-center gap-2 mb-4">
              <div class="w-1 h-4 bg-primary rounded-full"></div>
              <label class="text-sm font-medium">输入 CSS</label>
            </div>
            <textarea
              v-model="inputCss"
              @input="process"
              class="w-full h-96 p-4 border border-border rounded-lg bg-background text-sm font-mono resize-none focus:outline-none focus:ring-2 focus:ring-primary/50"
              placeholder="粘贴需要处理的CSS代码..."
            />
          </div>

          <div>
            <div class="flex items-center gap-2 mb-4">
              <div class="w-1 h-4 bg-green-500 rounded-full"></div>
              <label class="text-sm font-medium">输出结果</label>
            </div>
            <textarea
              v-model="outputCss"
              readonly
              class="w-full h-96 p-4 border border-border rounded-lg bg-muted/30 text-sm font-mono resize-none"
              placeholder="处理后的CSS将显示在这里..."
            />
          </div>
        </div>

        <div class="mt-6 p-3 bg-indigo-50/50 dark:bg-indigo-900/20 rounded-lg border border-indigo-100 dark:border-indigo-800">
          <div class="text-xs text-indigo-600 dark:text-indigo-400 font-medium mb-2">使用说明：</div>
          <ul class="text-xs text-gray-600 dark:text-gray-300 space-y-1 list-disc list-inside">
            <li><strong>格式化</strong>：美化CSS代码，自动添加缩进和换行</li>
            <li><strong>压缩</strong>：去除CSS中的所有空白字符，减小文件体积</li>
            <li>支持自定义缩进大小（2空格/4空格）或使用Tab</li>
            <li>输入时自动处理，无需点击按钮</li>
          </ul>
        </div>
      </div>
    </div>

    <div
      v-if="toast.show"
      class="fixed bottom-4 right-4 bg-green-500 text-white px-4 py-2 rounded-lg shadow-lg flex items-center gap-2 z-50"
    >
      <Check class="w-4 h-4" />
      {{ toast.message }}
    </div>
  </NuxtLayout>
</template>
