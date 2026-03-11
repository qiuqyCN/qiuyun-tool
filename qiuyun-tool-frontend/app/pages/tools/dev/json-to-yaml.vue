<script setup lang="ts">
import { ref } from 'vue'
import { AlertCircle, CheckCircle, Copy, Download, Check, Loader2, ArrowRightLeft } from 'lucide-vue-next'
import { useToolExecutor } from '~/composables/useToolExecutor'
import { ToolType } from '~/types/tool'

// 操作类型枚举
enum ConvertOperation {
  JSON_TO_YAML = 'json-to-yaml',
  YAML_TO_JSON = 'yaml-to-json'
}

// 请求参数类型
interface ConvertParams {
  operation: string
  input: string
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
const activeTab = ref<ConvertOperation>(ConvertOperation.JSON_TO_YAML)
const inputContent = ref('')
const outputContent = ref('')
const error = ref('')

// 使用工具执行器
const { execute, isLoading } = useToolExecutor<ConvertParams, ConvertResult>({
  toolCode: 'json-to-yaml',
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
    error.value = activeTab.value === ConvertOperation.JSON_TO_YAML ? '请输入 JSON 数据' : '请输入 YAML 数据'
    return
  }

  await execute({
    operation,
    input: inputContent.value
  })
}

// JSON转YAML
const convertJsonToYaml = () => processConvert(ConvertOperation.JSON_TO_YAML)

// YAML转JSON
const convertYamlToJson = () => processConvert(ConvertOperation.YAML_TO_JSON)

// 切换转换方向
const switchDirection = () => {
  activeTab.value = activeTab.value === ConvertOperation.JSON_TO_YAML
    ? ConvertOperation.YAML_TO_JSON
    : ConvertOperation.JSON_TO_YAML
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
    const isYaml = activeTab.value === ConvertOperation.JSON_TO_YAML
    const blob = new Blob([outputContent.value], { type: isYaml ? 'text/yaml' : 'application/json' })
    const url = URL.createObjectURL(blob)
    const a = document.createElement('a')
    a.href = url
    a.download = isYaml ? 'output.yaml' : 'output.json'
    a.click()
    URL.revokeObjectURL(url)
  }
}

// 处理
const handleProcess = () => {
  if (activeTab.value === ConvertOperation.JSON_TO_YAML) {
    convertJsonToYaml()
  } else {
    convertYamlToJson()
  }
}

// 输入框占位符
const inputPlaceholder = computed(() => {
  return activeTab.value === ConvertOperation.JSON_TO_YAML
    ? '请输入要转换的 JSON 数据...\n例如：\n{\n  "name": "张三",\n  "age": 25\n}'
    : '请输入要转换的 YAML 数据...\n例如：\nname: 张三\nage: 25'
})

// 输入框标签
const inputLabel = computed(() => {
  return activeTab.value === ConvertOperation.JSON_TO_YAML ? '输入 JSON' : '输入 YAML'
})

// 输出框标签
const outputLabel = computed(() => {
  return activeTab.value === ConvertOperation.JSON_TO_YAML ? '输出 YAML' : '输出 JSON'
})
</script>

<template>
  <NuxtLayout name="tool" tool-code="json-to-yaml">
    <!-- 工具执行区域 -->
    <div class="border border-border/40 rounded-xl overflow-hidden">
      <!-- 工具 Tabs -->
      <Tabs v-model="activeTab" class="w-full">
        <TabsList class="w-full justify-start rounded-none border-b bg-muted/30 p-0">
          <TabsTrigger
            :value="ConvertOperation.JSON_TO_YAML"
            class="rounded-none border-b-2 border-transparent data-[state=active]:border-primary data-[state=active]:bg-background px-6 py-3"
          >
            JSON → YAML
          </TabsTrigger>
          <TabsTrigger
            :value="ConvertOperation.YAML_TO_JSON"
            class="rounded-none border-b-2 border-transparent data-[state=active]:border-primary data-[state=active]:bg-background px-6 py-3"
          >
            YAML → JSON
          </TabsTrigger>
        </TabsList>

        <!-- JSON转YAML -->
        <TabsContent :value="ConvertOperation.JSON_TO_YAML" class="m-0 p-6">
          <div class="space-y-4">
            <div class="grid grid-cols-1 lg:grid-cols-2 gap-4">
              <!-- 输入区域 -->
              <div>
                <label class="text-sm font-medium mb-2 block">{{ inputLabel }}</label>
                <Textarea
                  v-model="inputContent"
                  :placeholder="inputPlaceholder"
                  rows="12"
                  class="font-mono text-sm"
                />
              </div>
              <!-- 输出区域 -->
              <div>
                <label class="text-sm font-medium mb-2 block">{{ outputLabel }}</label>
                <Textarea
                  v-model="outputContent"
                  readonly
                  rows="12"
                  class="font-mono text-sm bg-muted/30"
                  placeholder="转换结果将显示在这里..."
                />
              </div>
            </div>
            <div class="flex items-center gap-2 flex-wrap">
              <Button @click="convertJsonToYaml" :disabled="isLoading">
                <Loader2 v-if="isLoading" class="w-4 h-4 mr-2 animate-spin" />
                <CheckCircle v-else class="w-4 h-4 mr-2" />
                {{ isLoading ? '转换中...' : '开始转换' }}
              </Button>
              <Button variant="outline" @click="switchDirection">
                <ArrowRightLeft class="w-4 h-4 mr-2" />
                切换方向
              </Button>
              <Button variant="outline" @click="clearInput" :disabled="isLoading">清空</Button>
              <Button variant="outline" @click="copyOutput" :disabled="!outputContent || isLoading">
                <Copy class="w-4 h-4 mr-2" />
                复制结果
              </Button>
              <Button variant="outline" @click="downloadOutput" :disabled="!outputContent || isLoading">
                <Download class="w-4 h-4 mr-2" />
                下载
              </Button>
            </div>
            <div v-if="error" class="flex items-center gap-2 text-sm text-red-500">
              <AlertCircle class="w-4 h-4" />
              {{ error }}
            </div>
          </div>
        </TabsContent>

        <!-- YAML转JSON -->
        <TabsContent :value="ConvertOperation.YAML_TO_JSON" class="m-0 p-6">
          <div class="space-y-4">
            <div class="grid grid-cols-1 lg:grid-cols-2 gap-4">
              <!-- 输入区域 -->
              <div>
                <label class="text-sm font-medium mb-2 block">{{ inputLabel }}</label>
                <Textarea
                  v-model="inputContent"
                  :placeholder="inputPlaceholder"
                  rows="12"
                  class="font-mono text-sm"
                />
              </div>
              <!-- 输出区域 -->
              <div>
                <label class="text-sm font-medium mb-2 block">{{ outputLabel }}</label>
                <Textarea
                  v-model="outputContent"
                  readonly
                  rows="12"
                  class="font-mono text-sm bg-muted/30"
                  placeholder="转换结果将显示在这里..."
                />
              </div>
            </div>
            <div class="flex items-center gap-2 flex-wrap">
              <Button @click="convertYamlToJson" :disabled="isLoading">
                <Loader2 v-if="isLoading" class="w-4 h-4 mr-2 animate-spin" />
                <CheckCircle v-else class="w-4 h-4 mr-2" />
                {{ isLoading ? '转换中...' : '开始转换' }}
              </Button>
              <Button variant="outline" @click="switchDirection">
                <ArrowRightLeft class="w-4 h-4 mr-2" />
                切换方向
              </Button>
              <Button variant="outline" @click="clearInput" :disabled="isLoading">清空</Button>
              <Button variant="outline" @click="copyOutput" :disabled="!outputContent || isLoading">
                <Copy class="w-4 h-4 mr-2" />
                复制结果
              </Button>
              <Button variant="outline" @click="downloadOutput" :disabled="!outputContent || isLoading">
                <Download class="w-4 h-4 mr-2" />
                下载
              </Button>
            </div>
            <div v-if="error" class="flex items-center gap-2 text-sm text-red-500">
              <AlertCircle class="w-4 h-4" />
              {{ error }}
            </div>
          </div>
        </TabsContent>
      </Tabs>
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
