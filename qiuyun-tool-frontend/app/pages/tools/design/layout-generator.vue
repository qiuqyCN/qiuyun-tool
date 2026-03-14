<script setup lang="ts">
import { ref, computed } from 'vue'
import { Layout, Copy, Check, Plus, Minus, RefreshCw, Maximize2 } from 'lucide-vue-next'

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
      border: '2px dashed #9333EA',
      borderRadius: '8px',
      backgroundColor: '#F3E8FF'
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
      border: '2px dashed #9333EA',
      borderRadius: '8px',
      backgroundColor: '#F3E8FF'
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
    '#9333EA', '#A855F7', '#7C3AED', '#8B5CF6', '#6366F1',
    '#EC4899', '#F43F5E', '#EF4444', '#F59E0B', '#10B981',
    '#14B8A6', '#06B6D4', '#3B82F6', '#6366F1', '#8B5CF6'
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
                    <label class="text-xs font-medium mb-1 block">flex-direction</label>
                    <select v-model="flexDirection" class="w-full p-2 rounded border text-sm">
                      <option value="row">row</option>
                      <option value="row-reverse">row-reverse</option>
                      <option value="column">column</option>
                      <option value="column-reverse">column-reverse</option>
                    </select>
                  </div>

                  <div>
                    <label class="text-xs font-medium mb-1 block">flex-wrap</label>
                    <select v-model="flexWrap" class="w-full p-2 rounded border text-sm">
                      <option value="nowrap">nowrap</option>
                      <option value="wrap">wrap</option>
                      <option value="wrap-reverse">wrap-reverse</option>
                    </select>
                  </div>

                  <div>
                    <label class="text-xs font-medium mb-1 block">justify-content</label>
                    <select v-model="justifyContent" class="w-full p-2 rounded border text-sm">
                      <option value="flex-start">flex-start</option>
                      <option value="flex-end">flex-end</option>
                      <option value="center">center</option>
                      <option value="space-between">space-between</option>
                      <option value="space-around">space-around</option>
                      <option value="space-evenly">space-evenly</option>
                    </select>
                  </div>

                  <div>
                    <label class="text-xs font-medium mb-1 block">align-items</label>
                    <select v-model="alignItems" class="w-full p-2 rounded border text-sm">
                      <option value="flex-start">flex-start</option>
                      <option value="flex-end">flex-end</option>
                      <option value="center">center</option>
                      <option value="stretch">stretch</option>
                      <option value="baseline">baseline</option>
                    </select>
                  </div>

                  <div>
                    <label class="text-xs font-medium mb-1 block">align-content</label>
                    <select v-model="alignContent" class="w-full p-2 rounded border text-sm">
                      <option value="flex-start">flex-start</option>
                      <option value="flex-end">flex-end</option>
                      <option value="center">center</option>
                      <option value="space-between">space-between</option>
                      <option value="space-around">space-around</option>
                      <option value="stretch">stretch</option>
                    </select>
                  </div>

                  <div>
                    <label class="text-xs font-medium mb-1 block">gap</label>
                    <input
                      v-model="gap"
                      type="text"
                      class="w-full p-2 rounded border text-sm"
                      placeholder="0px"
                    />
                  </div>
                </div>
              </template>

              <template v-else>
                <div class="space-y-4">
                  <div>
                    <label class="text-xs font-medium mb-1 block">grid-template-columns</label>
                    <input
                      v-model="gridTemplateColumns"
                      type="text"
                      class="w-full p-2 rounded border text-sm"
                      placeholder="repeat(3, 1fr)"
                    />
                  </div>

                  <div>
                    <label class="text-xs font-medium mb-1 block">grid-template-rows</label>
                    <input
                      v-model="gridTemplateRows"
                      type="text"
                      class="w-full p-2 rounded border text-sm"
                      placeholder="auto"
                    />
                  </div>

                  <div>
                    <label class="text-xs font-medium mb-1 block">grid-auto-flow</label>
                    <select v-model="gridAutoFlow" class="w-full p-2 rounded border text-sm">
                      <option value="row">row</option>
                      <option value="column">column</option>
                      <option value="dense">dense</option>
                      <option value="row-dense">row-dense</option>
                      <option value="column-dense">column-dense</option>
                    </select>
                  </div>

                  <div>
                    <label class="text-xs font-medium mb-1 block">gap</label>
                    <input
                      v-model="gridGap"
                      type="text"
                      class="w-full p-2 rounded border text-sm"
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
              <div :style="containerStyle">
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
              <pre class="bg-muted p-4 rounded-lg text-sm font-mono overflow-x-auto">
                <code>{{ generatedCode }}</code>
              </pre>
            </div>
          </div>
        </div>

        <div class="p-3 bg-blue-50/50 dark:bg-blue-900/20 rounded-lg border border-blue-100 dark:border-blue-800">
          <div class="text-xs text-blue-600 dark:text-blue-400 font-medium mb-2">布局生成器说明:</div>
          <ul class="text-xs text-gray-600 dark:text-gray-300 space-y-1 list-disc list-inside">
            <li><strong>Flexbox</strong>：一维布局，适合处理行或列的排列</li>
            <li><strong>Grid</strong>：二维布局，适合处理复杂的网格系统</li>
            <li>可以实时调整参数，预览效果</li>
            <li>生成的代码可以直接复制使用</li>
            <li>支持添加/删除布局项</li>
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
