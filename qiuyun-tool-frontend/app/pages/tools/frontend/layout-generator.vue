<script setup lang="ts">
import { ref, computed } from 'vue'
import { Layout, Copy, Check, Plus, Minus, RefreshCw, HelpCircle } from 'lucide-vue-next'
import { Select, SelectTrigger, SelectValue, SelectContent, SelectItem } from '~/components/ui/select'
import { Input } from '~/components/ui/input'

type LayoutType = 'flex' | 'grid'
type FlexDirection = 'row' | 'row-reverse' | 'column' | 'column-reverse'
type FlexWrap = 'nowrap' | 'wrap' | 'wrap-reverse'
type JustifyContent = 'flex-start' | 'flex-end' | 'center' | 'space-between' | 'space-around' | 'space-evenly'
type AlignItems = 'flex-start' | 'flex-end' | 'center' | 'stretch' | 'baseline'
type AlignContent = 'flex-start' | 'flex-end' | 'center' | 'space-between' | 'space-around' | 'stretch'
type GridAutoFlow = 'row' | 'column' | 'dense' | 'row-dense' | 'column-dense'

interface LayoutItem {
  id: number
  flex: string
  order: number
  gridColumn: string
  gridRow: string
}

interface Option {
  value: string
  label: string
  description?: string
}

const layoutType = ref<LayoutType>('flex')
const itemCount = ref(6)

const flexDirection = ref<FlexDirection>('row')
const flexWrap = ref<FlexWrap>('nowrap')
const justifyContent = ref<JustifyContent>('flex-start')
const alignItems = ref<AlignItems>('stretch')
const alignContent = ref<AlignContent>('stretch')
const gap = ref('0px')

const gridTemplateColumns = ref('repeat(3, 1fr)')
const gridTemplateRows = ref('auto')
const gridAutoFlow = ref<GridAutoFlow>('row')
const gridGap = ref('0px')

const layoutItems = ref<LayoutItem[]>([])

const toast = ref({ show: false, message: '' })

const showToast = (message: string) => {
  toast.value.message = message
  toast.value.show = true
  setTimeout(() => toast.value.show = false, 2000)
}

const initItems = () => {
  const items: LayoutItem[] = []
  for (let i = 0; i < itemCount.value; i++) {
    items.push({
      id: i,
      flex: '1',
      order: 0,
      gridColumn: 'auto',
      gridRow: 'auto'
    })
  }
  layoutItems.value = items
}

initItems()

const containerStyle = computed(() => {
  if (layoutType.value === 'flex') {
    return {
      display: 'flex',
      flexDirection: flexDirection.value,
      flexWrap: flexWrap.value,
      justifyContent: justifyContent.value,
      alignItems: alignItems.value,
      alignContent: alignContent.value,
      gap: gap.value,
      width: '100%',
      minHeight: '300px',
      padding: '10px',
      border: '2px dashed #6366F1',
      borderRadius: '8px'
    }
  } else {
    return {
      display: 'grid',
      gridTemplateColumns: gridTemplateColumns.value,
      gridTemplateRows: gridTemplateRows.value,
      gridAutoFlow: gridAutoFlow.value,
      gap: gridGap.value,
      width: '100%',
      minHeight: '300px',
      padding: '10px',
      border: '2px dashed #6366F1',
      borderRadius: '8px'
    }
  }
})

const getItemStyle = (item: LayoutItem) => {
  if (layoutType.value === 'flex') {
    return {
      flex: item.flex,
      order: item.order,
      backgroundColor: getRandomColor(item.id),
      color: '#fff',
      padding: '20px',
      borderRadius: '4px',
      display: 'flex',
      alignItems: 'center',
      justifyContent: 'center',
      fontWeight: 'bold',
      minHeight: '50px'
    }
  } else {
    return {
      gridColumn: item.gridColumn,
      gridRow: item.gridRow,
      backgroundColor: getRandomColor(item.id),
      color: '#fff',
      padding: '20px',
      borderRadius: '4px',
      display: 'flex',
      alignItems: 'center',
      justifyContent: 'center',
      fontWeight: 'bold',
      minHeight: '50px'
    }
  }
}

const getRandomColor = (index: number) => {
  const colors = [
    '#6366F1', '#8B5CF6', '#A855F7', '#D946EF', '#EC4899',
    '#F43F5E', '#EF4444', '#F97316', '#F59E0B', '#EAB308',
    '#84CC16', '#22C55E', '#10B981', '#14B8A6', '#06B6D4'
  ]
  return colors[index % colors.length]
}

const generatedCode = computed(() => {
  if (layoutType.value === 'flex') {
    return `.container {
  display: flex;
  flex-direction: ${flexDirection.value};
  flex-wrap: ${flexWrap.value};
  justify-content: ${justifyContent.value};
  align-items: ${alignItems.value};
  align-content: ${alignContent.value};
  gap: ${gap.value};
}

.item {
  flex: 1;
}`
  } else {
    return `.container {
  display: grid;
  grid-template-columns: ${gridTemplateColumns.value};
  grid-template-rows: ${gridTemplateRows.value};
  grid-auto-flow: ${gridAutoFlow.value};
  gap: ${gridGap.value};
}`
  }
})

const copyCode = async () => {
  try {
    await navigator.clipboard.writeText(generatedCode.value)
    showToast('已复制代码')
  } catch {
    showToast('复制失败')
  }
}

const addItem = () => {
  itemCount.value++
  layoutItems.value.push({
    id: itemCount.value - 1,
    flex: '1',
    order: 0,
    gridColumn: 'auto',
    gridRow: 'auto'
  })
}

const removeItem = () => {
  if (itemCount.value > 1) {
    itemCount.value--
    layoutItems.value.pop()
  }
}

const resetLayout = () => {
  layoutType.value = 'flex'
  flexDirection.value = 'row'
  flexWrap.value = 'nowrap'
  justifyContent.value = 'flex-start'
  alignItems.value = 'stretch'
  alignContent.value = 'stretch'
  gap.value = '0px'
  gridTemplateColumns.value = 'repeat(3, 1fr)'
  gridTemplateRows.value = 'auto'
  gridAutoFlow.value = 'row'
  gridGap.value = '0px'
  itemCount.value = 6
  initItems()
}

const flexDirectionOptions: Option[] = [
  { value: 'row', label: 'row', description: '从左到右排列' },
  { value: 'row-reverse', label: 'row-reverse', description: '从右到左排列' },
  { value: 'column', label: 'column', description: '从上到下排列' },
  { value: 'column-reverse', label: 'column-reverse', description: '从下到上排列' }
]

const flexWrapOptions: Option[] = [
  { value: 'nowrap', label: 'nowrap', description: '不换行，单行显示' },
  { value: 'wrap', label: 'wrap', description: '换行，多行显示' },
  { value: 'wrap-reverse', label: 'wrap-reverse', description: '换行，反向多行' }
]

const justifyContentOptions: Option[] = [
  { value: 'flex-start', label: 'flex-start', description: '左对齐' },
  { value: 'flex-end', label: 'flex-end', description: '右对齐' },
  { value: 'center', label: 'center', description: '居中对齐' },
  { value: 'space-between', label: 'space-between', description: '两端对齐，间距相等' },
  { value: 'space-around', label: 'space-around', description: '两侧间距相等' },
  { value: 'space-evenly', label: 'space-evenly', description: '所有间距相等' }
]

const alignItemsOptions: Option[] = [
  { value: 'flex-start', label: 'flex-start', description: '顶部对齐' },
  { value: 'flex-end', label: 'flex-end', description: '底部对齐' },
  { value: 'center', label: 'center', description: '垂直居中' },
  { value: 'stretch', label: 'stretch', description: '拉伸填满' },
  { value: 'baseline', label: 'baseline', description: '基线对齐' }
]

const alignContentOptions: Option[] = [
  { value: 'flex-start', label: 'flex-start', description: '顶部对齐' },
  { value: 'flex-end', label: 'flex-end', description: '底部对齐' },
  { value: 'center', label: 'center', description: '居中对齐' },
  { value: 'space-between', label: 'space-between', description: '两端对齐' },
  { value: 'space-around', label: 'space-around', description: '两侧间距相等' },
  { value: 'stretch', label: 'stretch', description: '拉伸填满' }
]

const gridAutoFlowOptions: Option[] = [
  { value: 'row', label: 'row', description: '先行后列' },
  { value: 'column', label: 'column', description: '先列后行' },
  { value: 'dense', label: 'dense', description: '密集填充' },
  { value: 'row-dense', label: 'row-dense', description: '先行密集填充' },
  { value: 'column-dense', label: 'column-dense', description: '先列密集填充' }
]
</script>

<template>
  <NuxtLayout name="tool" tool-code="layout-generator">
    <div class="border border-border/40 rounded-xl overflow-hidden">
      <div class="p-4 border-b bg-muted/20">
        <div class="flex flex-wrap items-center gap-4">
          <div class="flex items-center gap-2">
            <button
              @click="layoutType = 'flex'"
              class="px-3 py-1.5 rounded text-sm transition-colors"
              :class="layoutType === 'flex' ? 'bg-primary text-white' : 'hover:bg-muted'"
            >
              Flexbox
            </button>
            <button
              @click="layoutType = 'grid'"
              class="px-3 py-1.5 rounded text-sm transition-colors"
              :class="layoutType === 'grid' ? 'bg-primary text-white' : 'hover:bg-muted'"
            >
              Grid
            </button>
          </div>

          <div class="flex items-center gap-2">
            <button
              @click="removeItem"
              :disabled="itemCount <= 1"
              class="p-1.5 rounded hover:bg-muted disabled:opacity-50 disabled:cursor-not-allowed"
            >
              <Minus class="w-4 h-4" />
            </button>
            <span class="text-sm font-medium">{{ itemCount }} 项</span>
            <button
              @click="addItem"
              class="p-1.5 rounded hover:bg-muted"
            >
              <Plus class="w-4 h-4" />
            </button>
          </div>

          <div class="ml-auto flex items-center gap-2">
            <button
              @click="resetLayout"
              class="text-xs text-muted-foreground hover:text-foreground px-3 py-1.5 rounded hover:bg-muted transition-colors flex items-center gap-1"
            >
              <RefreshCw class="w-3 h-3" />
              重置
            </button>
            <button
              @click="copyCode"
              class="text-xs text-white bg-primary hover:bg-primary/90 px-3 py-1.5 rounded transition-colors flex items-center gap-1"
            >
              <Copy class="w-3 h-3" />
              复制代码
            </button>
          </div>
        </div>
      </div>

      <div class="p-6 space-y-6">
        <div class="grid grid-cols-1 lg:grid-cols-3 gap-6">
          <div class="lg:col-span-1 space-y-4">
            <div>
              <div class="flex items-center gap-2 mb-4">
                <div class="w-1 h-4 bg-primary rounded-full"></div>
                <label class="text-sm font-medium">布局配置</label>
              </div>

              <template v-if="layoutType === 'flex'">
                <div class="space-y-4">
                  <div>
                    <div class="flex items-center justify-between mb-1">
                      <label class="text-xs font-medium">
                        <span class="text-foreground">flex-direction</span>
                        <span class="text-muted-foreground ml-1">（主轴方向）</span>
                      </label>
                      <div class="group relative">
                        <HelpCircle class="w-3 h-3 text-muted-foreground cursor-help" />
                        <div class="absolute bottom-full left-1/2 -translate-x-1/2 mb-2 px-3 py-2 bg-foreground text-background text-xs rounded opacity-0 group-hover:opacity-100 transition-opacity z-50 w-64">
                          <div class="font-semibold mb-1">定义主轴方向</div>
                          <div>确定flex容器内项目的排列方向，是Flex布局的核心属性</div>
                        </div>
                      </div>
                    </div>
                    <Select v-model="flexDirection">
                      <SelectTrigger>
                        <SelectValue />
                      </SelectTrigger>
                      <SelectContent>
                        <SelectItem v-for="opt in flexDirectionOptions" :key="opt.value" :value="opt.value">
                          <div class="flex flex-col">
                            <span>{{ opt.label }}</span>
                            <span class="text-xs text-muted-foreground">{{ opt.description }}</span>
                          </div>
                        </SelectItem>
                      </SelectContent>
                    </Select>
                  </div>

                  <div>
                    <div class="flex items-center justify-between mb-1">
                      <label class="text-xs font-medium">
                        <span class="text-foreground">flex-wrap</span>
                        <span class="text-muted-foreground ml-1">（换行方式）</span>
                      </label>
                      <div class="group relative">
                        <HelpCircle class="w-3 h-3 text-muted-foreground cursor-help" />
                        <div class="absolute bottom-full left-1/2 -translate-x-1/2 mb-2 px-3 py-2 bg-foreground text-background text-xs rounded opacity-0 group-hover:opacity-100 transition-opacity z-50 w-64">
                          <div class="font-semibold mb-1">定义是否换行</div>
                          <div>控制flex容器内的项目在一行放不下时如何处理，可以实现多行布局</div>
                        </div>
                      </div>
                    </div>
                    <Select v-model="flexWrap">
                      <SelectTrigger>
                        <SelectValue />
                      </SelectTrigger>
                      <SelectContent>
                        <SelectItem v-for="opt in flexWrapOptions" :key="opt.value" :value="opt.value">
                          <div class="flex flex-col">
                            <span>{{ opt.label }}</span>
                            <span class="text-xs text-muted-foreground">{{ opt.description }}</span>
                          </div>
                        </SelectItem>
                      </SelectContent>
                    </Select>
                  </div>

                  <div>
                    <div class="flex items-center justify-between mb-1">
                      <label class="text-xs font-medium">
                        <span class="text-foreground">justify-content</span>
                        <span class="text-muted-foreground ml-1">（主轴对齐）</span>
                      </label>
                      <div class="group relative">
                        <HelpCircle class="w-3 h-3 text-muted-foreground cursor-help" />
                        <div class="absolute bottom-full left-1/2 -translate-x-1/2 mb-2 px-3 py-2 bg-foreground text-background text-xs rounded opacity-0 group-hover:opacity-100 transition-opacity z-50 w-64">
                          <div class="font-semibold mb-1">主轴对齐方式</div>
                          <div>控制项目在主轴方向上的对齐和分布方式，可实现居中、两端对齐等效果</div>
                        </div>
                      </div>
                    </div>
                    <Select v-model="justifyContent">
                      <SelectTrigger>
                        <SelectValue />
                      </SelectTrigger>
                      <SelectContent>
                        <SelectItem v-for="opt in justifyContentOptions" :key="opt.value" :value="opt.value">
                          <div class="flex flex-col">
                            <span>{{ opt.label }}</span>
                            <span class="text-xs text-muted-foreground">{{ opt.description }}</span>
                          </div>
                        </SelectItem>
                      </SelectContent>
                    </Select>
                  </div>

                  <div>
                    <div class="flex items-center justify-between mb-1">
                      <label class="text-xs font-medium">
                        <span class="text-foreground">align-items</span>
                        <span class="text-muted-foreground ml-1">（交叉轴对齐）</span>
                      </label>
                      <div class="group relative">
                        <HelpCircle class="w-3 h-3 text-muted-foreground cursor-help" />
                        <div class="absolute bottom-full left-1/2 -translate-x-1/2 mb-2 px-3 py-2 bg-foreground text-background text-xs rounded opacity-0 group-hover:opacity-100 transition-opacity z-50 w-64">
                          <div class="font-semibold mb-1">交叉轴对齐方式</div>
                          <div>控制项目在交叉轴方向上的对齐方式，适用于单行布局的垂直对齐</div>
                        </div>
                      </div>
                    </div>
                    <Select v-model="alignItems">
                      <SelectTrigger>
                        <SelectValue />
                      </SelectTrigger>
                      <SelectContent>
                        <SelectItem v-for="opt in alignItemsOptions" :key="opt.value" :value="opt.value">
                          <div class="flex flex-col">
                            <span>{{ opt.label }}</span>
                            <span class="text-xs text-muted-foreground">{{ opt.description }}</span>
                          </div>
                        </SelectItem>
                      </SelectContent>
                    </Select>
                  </div>

                  <div>
                    <div class="flex items-center justify-between mb-1">
                      <label class="text-xs font-medium">
                        <span class="text-foreground">align-content</span>
                        <span class="text-muted-foreground ml-1">（多行对齐）</span>
                      </label>
                      <div class="group relative">
                        <HelpCircle class="w-3 h-3 text-muted-foreground cursor-help" />
                        <div class="absolute bottom-full left-1/2 -translate-x-1/2 mb-2 px-3 py-2 bg-foreground text-background text-xs rounded opacity-0 group-hover:opacity-100 transition-opacity z-50 w-64">
                          <div class="font-semibold mb-1">多行对齐方式</div>
                          <div>控制多行项目在交叉轴方向上的对齐和分布，仅在flex-wrap: wrap时有效</div>
                        </div>
                      </div>
                    </div>
                    <Select v-model="alignContent">
                      <SelectTrigger>
                        <SelectValue />
                      </SelectTrigger>
                      <SelectContent>
                        <SelectItem v-for="opt in alignContentOptions" :key="opt.value" :value="opt.value">
                          <div class="flex flex-col">
                            <span>{{ opt.label }}</span>
                            <span class="text-xs text-muted-foreground">{{ opt.description }}</span>
                          </div>
                        </SelectItem>
                      </SelectContent>
                    </Select>
                  </div>

                  <div>
                    <div class="flex items-center justify-between mb-1">
                      <label class="text-xs font-medium">
                        <span class="text-foreground">gap</span>
                        <span class="text-muted-foreground ml-1">（间距）</span>
                      </label>
                      <div class="group relative">
                        <HelpCircle class="w-3 h-3 text-muted-foreground cursor-help" />
                        <div class="absolute bottom-full left-1/2 -translate-x-1/2 mb-2 px-3 py-2 bg-foreground text-background text-xs rounded opacity-0 group-hover:opacity-100 transition-opacity z-50 w-64">
                          <div class="font-semibold mb-1">元素间距</div>
                          <div>设置项目之间的间距，支持px、em、rem等单位，如10px、1rem</div>
                        </div>
                      </div>
                    </div>
                    <Input
                      v-model="gap"
                      type="text"
                      placeholder="0px"
                    />
                  </div>
                </div>
              </template>

              <template v-else>
                <div class="space-y-4">
                  <div>
                    <div class="flex items-center justify-between mb-1">
                      <label class="text-xs font-medium">
                        <span class="text-foreground">grid-template-columns</span>
                        <span class="text-muted-foreground ml-1">（列模板）</span>
                      </label>
                      <div class="group relative">
                        <HelpCircle class="w-3 h-3 text-muted-foreground cursor-help" />
                        <div class="absolute bottom-full left-1/2 -translate-x-1/2 mb-2 px-3 py-2 bg-foreground text-background text-xs rounded opacity-0 group-hover:opacity-100 transition-opacity z-50 w-64">
                          <div class="font-semibold mb-1">列模板定义</div>
                          <div>定义网格的列数和每列宽度，常用repeat()函数，如repeat(3, 1fr)表示3列等宽</div>
                        </div>
                      </div>
                    </div>
                    <Input
                      v-model="gridTemplateColumns"
                      type="text"
                      placeholder="repeat(3, 1fr)"
                    />
                  </div>

                  <div>
                    <div class="flex items-center justify-between mb-1">
                      <label class="text-xs font-medium">
                        <span class="text-foreground">grid-template-rows</span>
                        <span class="text-muted-foreground ml-1">（行模板）</span>
                      </label>
                      <div class="group relative">
                        <HelpCircle class="w-3 h-3 text-muted-foreground cursor-help" />
                        <div class="absolute bottom-full left-1/2 -translate-x-1/2 mb-2 px-3 py-2 bg-foreground text-background text-xs rounded opacity-0 group-hover:opacity-100 transition-opacity z-50 w-64">
                          <div class="font-semibold mb-1">行模板定义</div>
                          <div>定义网格的行数和每行高度，auto表示自动适应内容，fr表示分数单位</div>
                        </div>
                      </div>
                    </div>
                    <Input
                      v-model="gridTemplateRows"
                      type="text"
                      placeholder="auto"
                    />
                  </div>

                  <div>
                    <div class="flex items-center justify-between mb-1">
                      <label class="text-xs font-medium">
                        <span class="text-foreground">grid-auto-flow</span>
                        <span class="text-muted-foreground ml-1">（自动布局）</span>
                      </label>
                      <div class="group relative">
                        <HelpCircle class="w-3 h-3 text-muted-foreground cursor-help" />
                        <div class="absolute bottom-full left-1/2 -translate-x-1/2 mb-2 px-3 py-2 bg-foreground text-background text-xs rounded opacity-0 group-hover:opacity-100 transition-opacity z-50 w-64">
                          <div class="font-semibold mb-1">自动布局方向</div>
                          <div>控制自动放置项目的顺序和方向，dense可尝试填充网格中的空白</div>
                        </div>
                      </div>
                    </div>
                    <Select v-model="gridAutoFlow">
                      <SelectTrigger>
                        <SelectValue />
                      </SelectTrigger>
                      <SelectContent>
                        <SelectItem v-for="opt in gridAutoFlowOptions" :key="opt.value" :value="opt.value">
                          <div class="flex flex-col">
                            <span>{{ opt.label }}</span>
                            <span class="text-xs text-muted-foreground">{{ opt.description }}</span>
                          </div>
                        </SelectItem>
                      </SelectContent>
                    </Select>
                  </div>

                  <div>
                    <div class="flex items-center justify-between mb-1">
                      <label class="text-xs font-medium">
                        <span class="text-foreground">gap</span>
                        <span class="text-muted-foreground ml-1">（间距）</span>
                      </label>
                      <div class="group relative">
                        <HelpCircle class="w-3 h-3 text-muted-foreground cursor-help" />
                        <div class="absolute bottom-full left-1/2 -translate-x-1/2 mb-2 px-3 py-2 bg-foreground text-background text-xs rounded opacity-0 group-hover:opacity-100 transition-opacity z-50 w-64">
                          <div class="font-semibold mb-1">网格间距</div>
                          <div>设置网格行和列之间的间距，支持px、em、rem等单位</div>
                        </div>
                      </div>
                    </div>
                    <Input
                      v-model="gridGap"
                      type="text"
                      placeholder="0px"
                    />
                  </div>
                </div>
              </template>
            </div>
          </div>

          <div class="lg:col-span-2 space-y-4">
            <div>
              <div class="flex items-center gap-2 mb-4">
                <div class="w-1 h-4 bg-green-500 rounded-full"></div>
                <label class="text-sm font-medium">预览</label>
              </div>
              <div :style="containerStyle" class="bg-indigo-100 dark:bg-indigo-900/30">
                <div
                  v-for="item in layoutItems"
                  :key="item.id"
                  :style="getItemStyle(item)"
                >
                  {{ item.id + 1 }}
                </div>
              </div>
            </div>

            <div>
              <div class="flex items-center justify-between mb-4">
                <div class="flex items-center gap-2">
                  <div class="w-1 h-4 bg-purple-500 rounded-full"></div>
                  <label class="text-sm font-medium">生成的代码</label>
                </div>
                <button
                  @click="copyCode"
                  class="text-xs text-muted-foreground hover:text-foreground px-2 py-1 rounded hover:bg-muted transition-colors"
                >
                  <Copy class="w-3 h-3" />
                </button>
              </div>
              <pre class="bg-muted p-4 rounded-lg text-sm font-mono overflow-x-auto"><code>{{ generatedCode }}</code></pre>
            </div>
          </div>
        </div>

        <div class="p-3 bg-indigo-50/50 dark:bg-indigo-900/20 rounded-lg border border-indigo-100 dark:border-indigo-800">
          <div class="text-xs text-indigo-600 dark:text-indigo-400 font-medium mb-2">布局生成器说明:</div>
          <ul class="text-xs text-gray-600 dark:text-gray-300 space-y-1 list-disc list-inside">
            <li><strong>Flexbox</strong>：一维布局，适合处理行或列的排列</li>
            <li><strong>Grid</strong>：二维布局，适合处理复杂的网格系统</li>
            <li>鼠标悬停问号图标可查看参数详细说明</li>
            <li>下拉选项中包含每个值的功能描述</li>
            <li>可以实时调整参数，预览效果</li>
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
