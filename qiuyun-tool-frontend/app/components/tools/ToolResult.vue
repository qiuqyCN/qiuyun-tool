<template>
  <div class="space-y-4">
    <!-- 文本结果 -->
    <div v-if="type === 'text'" class="space-y-4">
      <div class="relative">
        <Textarea
          :model-value="content"
          readonly
          rows="10"
          class="font-mono text-sm bg-gray-50"
        />
        <Button
          size="sm"
          variant="outline"
          class="absolute top-2 right-2"
          @click="copyContent"
        >
          <Icon name="lucide:copy" class="w-4 h-4 mr-1" />
          复制
        </Button>
      </div>
    </div>

    <!-- JSON结果 -->
    <div v-else-if="type === 'json'" class="space-y-4">
      <div class="relative">
        <pre class="p-4 bg-gray-900 text-gray-100 rounded-lg overflow-x-auto text-sm font-mono">{{ formattedJson }}</pre>
        <Button
          size="sm"
          variant="outline"
          class="absolute top-2 right-2 bg-white"
          @click="copyContent"
        >
          <Icon name="lucide:copy" class="w-4 h-4 mr-1" />
          复制
        </Button>
      </div>
    </div>

    <!-- 文件下载结果 -->
    <div v-else-if="type === 'file'" class="space-y-4">
      <div class="flex items-center gap-4 p-4 bg-white border border-gray-200 rounded-lg">
        <div class="w-12 h-12 bg-indigo-100 rounded-lg flex items-center justify-center">
          <Icon name="lucide:file-down" class="w-6 h-6 text-indigo-600" />
        </div>
        <div class="flex-1">
          <p class="font-medium text-gray-900">{{ fileName || '处理完成的文件' }}</p>
          <p class="text-sm text-gray-500">{{ fileSize ? formatFileSize(fileSize) : '' }}</p>
        </div>
        <Button @click="downloadFile">
          <Icon name="lucide:download" class="w-4 h-4 mr-2" />
          下载
        </Button>
      </div>
    </div>

    <!-- 图片结果 -->
    <div v-else-if="type === 'image'" class="space-y-4">
      <div class="relative group">
        <img
          :src="imageUrl"
          :alt="'处理结果'"
          class="max-w-full rounded-lg border border-gray-200"
        />
        <div class="absolute inset-0 bg-black/50 opacity-0 group-hover:opacity-100 transition-opacity rounded-lg flex items-center justify-center gap-2">
          <Button variant="secondary" @click="downloadImage">
            <Icon name="lucide:download" class="w-4 h-4 mr-2" />
            下载
          </Button>
          <Button variant="secondary" @click="copyImage">
            <Icon name="lucide:copy" class="w-4 h-4 mr-2" />
            复制
          </Button>
        </div>
      </div>
    </div>

    <!-- 表格结果 -->
    <div v-else-if="type === 'table'" class="space-y-4">
      <div class="border border-gray-200 rounded-lg overflow-hidden">
        <Table>
          <TableHeader>
            <TableRow>
              <TableHead v-for="header in tableHeaders" :key="header">
                {{ header }}
              </TableHead>
            </TableRow>
          </TableHeader>
          <TableBody>
            <TableRow v-for="(row, index) in tableData" :key="index">
              <TableCell v-for="(cell, cellIndex) in row" :key="cellIndex">
                {{ cell }}
              </TableCell>
            </TableRow>
          </TableBody>
        </Table>
      </div>
      <div class="flex justify-end gap-2">
        <Button variant="outline" size="sm" @click="copyTable">
          <Icon name="lucide:copy" class="w-4 h-4 mr-2" />
          复制表格
        </Button>
        <Button variant="outline" size="sm" @click="downloadCsv">
          <Icon name="lucide:download" class="w-4 h-4 mr-2" />
          下载 CSV
        </Button>
      </div>
    </div>

    <!-- 自定义结果 -->
    <div v-else-if="type === 'custom'">
      <slot :result="result" />
    </div>

    <!-- 操作按钮 -->
    <div class="flex justify-center gap-4 pt-4 border-t border-gray-200">
      <Button variant="outline" @click="$emit('reset')">
        <Icon name="lucide:refresh-cw" class="w-4 h-4 mr-2" />
        重新处理
      </Button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'

interface Props {
  type: 'text' | 'json' | 'file' | 'image' | 'table' | 'custom'
  result: any
  content?: string
  fileUrl?: string
  fileName?: string
  fileSize?: number
  imageUrl?: string
  tableData?: any[][]
  tableHeaders?: string[]
}

const props = defineProps<Props>()
const emit = defineEmits<{
  reset: []
}>()

// 格式化JSON
const formattedJson = computed(() => {
  if (props.type === 'json' && props.result) {
    try {
      return JSON.stringify(props.result, null, 2)
    } catch {
      return String(props.result)
    }
  }
  return ''
})

// 格式化文件大小
const formatFileSize = (bytes: number): string => {
  if (bytes === 0) return '0 Bytes'
  const k = 1024
  const sizes = ['Bytes', 'KB', 'MB', 'GB']
  const i = Math.floor(Math.log(bytes) / Math.log(k))
  return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i]
}

// 复制内容
const copyContent = async () => {
  let textToCopy = ''

  switch (props.type) {
    case 'text':
      textToCopy = props.content || ''
      break
    case 'json':
      textToCopy = formattedJson.value
      break
    case 'table':
      textToCopy = convertTableToText()
      break
  }

  try {
    await navigator.clipboard.writeText(textToCopy)
    // 显示成功提示
  } catch (err) {
    // 显示失败提示
  }
}

// 复制图片
const copyImage = async () => {
  if (!props.imageUrl) return

  try {
    const response = await fetch(props.imageUrl)
    const blob = await response.blob()
    await navigator.clipboard.write([
      new ClipboardItem({ [blob.type]: blob })
    ])
    // 显示成功提示
  } catch (err) {
    // 显示失败提示
  }
}

// 复制表格
const copyTable = () => {
  copyContent()
}

// 转换表格为文本
const convertTableToText = (): string => {
  if (!props.tableHeaders || !props.tableData) return ''

  const lines: string[] = []
  lines.push(props.tableHeaders.join('\t'))
  props.tableData.forEach(row => {
    lines.push(row.join('\t'))
  })
  return lines.join('\n')
}

// 下载CSV
const downloadCsv = () => {
  if (!props.tableHeaders || !props.tableData) return

  const csvContent = [
    props.tableHeaders.join(','),
    ...props.tableData.map(row =>
      row.map(cell => `"${String(cell).replace(/"/g, '""')}"`).join(',')
    )
  ].join('\n')

  const blob = new Blob(['\ufeff' + csvContent], { type: 'text/csv;charset=utf-8;' })
  const link = document.createElement('a')
  link.href = URL.createObjectURL(blob)
  link.download = `${props.fileName || 'export'}.csv`
  link.click()
  URL.revokeObjectURL(link.href)
}

// 下载文件
const downloadFile = () => {
  if (!props.fileUrl) return

  const link = document.createElement('a')
  link.href = props.fileUrl
  link.download = props.fileName || 'download'
  document.body.appendChild(link)
  link.click()
  document.body.removeChild(link)
}

// 下载图片
const downloadImage = () => {
  if (!props.imageUrl) return

  const link = document.createElement('a')
  link.href = props.imageUrl
  link.download = props.fileName || 'image.png'
  document.body.appendChild(link)
  link.click()
  document.body.removeChild(link)
}
</script>
