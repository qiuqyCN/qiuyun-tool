<template>
  <div class="min-h-screen bg-background">
    <div class="max-w-4xl mx-auto px-4 sm:px-6 lg:px-8 py-12">
      <div class="text-center mb-12">
        <div class="inline-flex items-center justify-center w-16 h-16 rounded-2xl bg-primary/10 mb-6">
          <MessageSquareWarning class="w-8 h-8 text-primary" />
        </div>
        <h1 class="text-3xl sm:text-4xl font-bold mb-4">问题反馈</h1>
        <p class="text-muted-foreground">帮助我们改进，提交您的问题或建议</p>
      </div>

      <div class="grid grid-cols-1 lg:grid-cols-3 gap-8">
        <div class="lg:col-span-2">
          <Card>
            <CardHeader>
              <CardTitle>提交反馈</CardTitle>
              <CardDescription>请详细描述您遇到的问题或建议</CardDescription>
            </CardHeader>
            <CardContent>
              <form @submit.prevent="handleSubmit" class="space-y-6">
                <div class="grid grid-cols-1 sm:grid-cols-2 gap-4">
                  <div class="space-y-2">
                    <Label for="name">您的姓名</Label>
                    <Input
                      id="name"
                      v-model="formData.name"
                      placeholder="请输入姓名"
                      required
                    />
                  </div>
                  <div class="space-y-2">
                    <Label for="email">电子邮箱</Label>
                    <Input
                      id="email"
                      v-model="formData.email"
                      type="email"
                      placeholder="请输入邮箱"
                      required
                    />
                  </div>
                </div>

                <div class="space-y-2">
                  <Label for="type">反馈类型</Label>
                  <Select v-model="formData.type" required>
                    <SelectTrigger>
                      <SelectValue placeholder="请选择反馈类型" />
                    </SelectTrigger>
                    <SelectContent>
                      <SelectItem value="bug">功能故障/Bug报告</SelectItem>
                      <SelectItem value="feature">功能建议</SelectItem>
                      <SelectItem value="improvement">改进建议</SelectItem>
                      <SelectItem value="content">内容问题</SelectItem>
                      <SelectItem value="other">其他</SelectItem>
                    </SelectContent>
                  </Select>
                </div>

                <div class="space-y-2">
                  <Label for="tool">相关工具（选填）</Label>
                  <Select v-model="formData.tool">
                    <SelectTrigger>
                      <SelectValue placeholder="请选择相关工具" />
                    </SelectTrigger>
                    <SelectContent>
                      <SelectItem value="">无</SelectItem>
                      <SelectItem value="json-formatter">JSON格式化</SelectItem>
                      <SelectItem value="base64">Base64编码</SelectItem>
                      <SelectItem value="url-codec">URL编解码</SelectItem>
                      <SelectItem value="md5-encrypt">MD5加密</SelectItem>
                      <SelectItem value="timestamp">时间戳转换</SelectItem>
                      <SelectItem value="json-yaml">JSON/YAML转换</SelectItem>
                      <SelectItem value="html-escape">HTML转义</SelectItem>
                      <SelectItem value="regex">正则表达式测试</SelectItem>
                      <SelectItem value="color-converter">颜色转换器</SelectItem>
                      <SelectItem value="other">其他工具</SelectItem>
                    </SelectContent>
                  </Select>
                </div>

                <div class="space-y-2">
                  <Label for="title">标题</Label>
                  <Input
                    id="title"
                    v-model="formData.title"
                    placeholder="请简要描述问题或建议"
                    required
                  />
                </div>

                <div class="space-y-2">
                  <Label for="description">详细描述</Label>
                  <Textarea
                    id="description"
                    v-model="formData.description"
                    rows="6"
                    placeholder="请详细描述问题：
1. 问题发生的步骤
2. 期望的结果
3. 实际的结果
4. 其他相关信息"
                    required
                  />
                </div>

                <div class="space-y-2">
                  <Label>附件（选填）</Label>
                  <div class="border-2 border-dashed border-border rounded-lg p-6 text-center hover:bg-muted/50 transition-colors cursor-pointer" @click="fileInput?.click()">
                    <input
                      ref="fileInput"
                      type="file"
                      multiple
                      accept="image/*,.txt,.log,.json,.xml"
                      class="hidden"
                      @change="handleFileChange"
                    />
                    <Upload class="w-8 h-8 mx-auto text-muted-foreground mb-2" />
                    <p class="text-sm text-muted-foreground">
                      点击上传截图或文件
                    </p>
                    <p class="text-xs text-muted-foreground mt-1">
                      支持图片、文本文件，单个文件不超过 5MB
                    </p>
                  </div>
                  <div v-if="formData.files.length > 0" class="mt-3 space-y-2">
                    <div
                      v-for="(file, index) in formData.files"
                      :key="index"
                      class="flex items-center justify-between bg-muted rounded-lg px-3 py-2"
                    >
                      <div class="flex items-center gap-2">
                        <FileText class="w-4 h-4 text-primary" />
                        <span class="text-sm truncate max-w-[200px]">{{ file.name }}</span>
                        <span class="text-xs text-muted-foreground">({{ formatFileSize(file.size) }})</span>
                      </div>
                      <Button
                        type="button"
                        variant="ghost"
                        size="sm"
                        @click="removeFile(index)"
                      >
                        <X class="w-4 h-4" />
                      </Button>
                    </div>
                  </div>
                </div>

                <div class="flex items-start gap-2">
                  <Checkbox
                    id="agree"
                    v-model:checked="formData.agree"
                    required
                  />
                  <Label for="agree" class="text-sm font-normal leading-tight cursor-pointer">
                    我同意秋云工具收集和处理我提交的反馈信息，以便改进服务质量。
                  </Label>
                </div>

                <Button
                  type="submit"
                  class="w-full"
                  :disabled="isSubmitting || !formData.agree"
                >
                  <Send class="w-4 h-4 mr-2" />
                  {{ isSubmitting ? '提交中...' : '提交反馈' }}
                </Button>
              </form>
            </CardContent>
          </Card>
        </div>

        <div class="space-y-6">
          <Card>
            <CardHeader>
              <CardTitle class="text-base">反馈处理流程</CardTitle>
            </CardHeader>
            <CardContent class="space-y-4">
              <div class="flex items-start gap-3">
                <div class="w-8 h-8 rounded-full bg-primary/10 flex items-center justify-center shrink-0">
                  <span class="text-sm font-bold text-primary">1</span>
                </div>
                <div>
                  <p class="font-medium">提交反馈</p>
                  <p class="text-sm text-muted-foreground">填写表单并提交</p>
                </div>
              </div>
              <div class="flex items-start gap-3">
                <div class="w-8 h-8 rounded-full bg-primary/10 flex items-center justify-center shrink-0">
                  <span class="text-sm font-bold text-primary">2</span>
                </div>
                <div>
                  <p class="font-medium">确认接收</p>
                  <p class="text-sm text-muted-foreground">24小时内邮件确认</p>
                </div>
              </div>
              <div class="flex items-start gap-3">
                <div class="w-8 h-8 rounded-full bg-primary/10 flex items-center justify-center shrink-0">
                  <span class="text-sm font-bold text-primary">3</span>
                </div>
                <div>
                  <p class="font-medium">问题处理</p>
                  <p class="text-sm text-muted-foreground">技术团队分析处理</p>
                </div>
              </div>
              <div class="flex items-start gap-3">
                <div class="w-8 h-8 rounded-full bg-primary/10 flex items-center justify-center shrink-0">
                  <span class="text-sm font-bold text-primary">4</span>
                </div>
                <div>
                  <p class="font-medium">反馈结果</p>
                  <p class="text-sm text-muted-foreground">邮件通知处理结果</p>
                </div>
              </div>
            </CardContent>
          </Card>

          <Card>
            <CardHeader>
              <CardTitle class="text-base">其他联系方式</CardTitle>
            </CardHeader>
            <CardContent class="space-y-3">
              <div class="flex items-center gap-3">
                <Mail class="w-5 h-5 text-primary" />
                <div>
                  <p class="text-sm font-medium">邮箱</p>
                  <p class="text-sm text-muted-foreground">97208294@qq.com</p>
                </div>
              </div>
               <div class="flex items-center gap-3">
                <MessageSquare class="w-5 h-5 text-primary" />
                <div>
                  <p class="text-sm font-medium">微信</p>
                  <p class="text-sm text-muted-foreground">qiuyun-sa</p>
                </div>
              </div>
              <div class="flex items-center gap-3">
                <MessageCircle class="w-5 h-5 text-primary" />
                <div>
                  <p class="text-sm font-medium">在线客服</p>
                  <p class="text-sm text-muted-foreground">工作日 9:00-18:00</p>
                </div>
              </div>
            </CardContent>
          </Card>

          <div class="bg-linear-to-br from-primary/5 to-primary/10 rounded-xl p-6">
            <h3 class="font-semibold mb-2">常见问题</h3>
            <p class="text-sm text-muted-foreground mb-4">在提交反馈前，您可以先查看常见问题解答</p>
            <NuxtLink to="/help">
              <Button variant="outline" class="w-full">
                <HelpCircle class="w-4 h-4 mr-2" />
                查看帮助中心
              </Button>
            </NuxtLink>
          </div>
        </div>
      </div>
    </div>

    <Dialog :open="showSuccessDialog" @update:open="showSuccessDialog = false">
      <DialogContent>
        <DialogHeader>
          <DialogTitle class="flex items-center gap-2">
            <CheckCircle class="w-6 h-6 text-green-500" />
            反馈提交成功
          </DialogTitle>
          <DialogDescription>
            感谢您的问题反馈！我们已收到您的提交，将在24小时内通过邮件与您联系。
          </DialogDescription>
        </DialogHeader>
        <DialogFooter>
          <Button @click="showSuccessDialog = false">确定</Button>
        </DialogFooter>
      </DialogContent>
    </Dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import {
  MessageSquareWarning,
  Upload,
  FileText,
  X,
  Send,
  Mail,
  MessageCircle,
  MessageSquare,
  HelpCircle,
  CheckCircle
} from 'lucide-vue-next'

useHead({
  title: '问题反馈 - 秋云工具',
  meta: [
    { name: 'description', content: '提交问题反馈，帮助秋云工具改进服务质量。' }
  ]
})

const isSubmitting = ref(false)
const showSuccessDialog = ref(false)
const fileInput = ref<HTMLInputElement | null>(null)

const formData = reactive({
  name: '',
  email: '',
  type: '',
  tool: '',
  title: '',
  description: '',
  files: [] as File[],
  agree: false
})

const handleFileChange = (event: Event) => {
  const target = event.target as HTMLInputElement
  if (target.files) {
    const newFiles = Array.from(target.files)
    const totalSize = formData.files.reduce((sum, f) => sum + f.size, 0) +
                      newFiles.reduce((sum, f) => sum + f.size, 0)

    if (totalSize > 20 * 1024 * 1024) {
      alert('附件总大小不能超过 20MB')
      return
    }

    formData.files.push(...newFiles)
  }
}

const removeFile = (index: number) => {
  formData.files.splice(index, 1)
}

const formatFileSize = (bytes: number): string => {
  if (bytes === 0) return '0 Bytes'
  const k = 1024
  const sizes = ['Bytes', 'KB', 'MB', 'GB']
  const i = Math.floor(Math.log(bytes) / Math.log(k))
  return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i]
}

const handleSubmit = async () => {
  if (!formData.agree) {
    alert('请同意隐私条款')
    return
  }

  isSubmitting.value = true

  // 模拟提交
  await new Promise(resolve => setTimeout(resolve, 1500))

  isSubmitting.value = false
  showSuccessDialog.value = true

  // 重置表单
  Object.assign(formData, {
    name: '',
    email: '',
    type: '',
    tool: '',
    title: '',
    description: '',
    files: [],
    agree: false
  })
}
</script>
