import { defineStore } from 'pinia'
import type { CategoryResponse, ToolResponse } from '~/composables/useApi'

export interface ToolState {
  categories: CategoryResponse[]
  tools: ToolResponse[]
  loading: boolean
  error: string | null
  initialized: boolean
}

export const useToolStore = defineStore('tool', {
  state: (): ToolState => ({
    categories: [],
    tools: [],
    loading: false,
    error: null,
    initialized: false
  }),

  getters: {
    // 获取工具总数
    totalTools: (state): number => state.tools.length,

    // 获取总访问量
    totalVisits: (state): number =>
      state.tools.reduce((sum, tool) => sum + (tool.visits || 0), 0),

    // 获取本月新增工具数
    monthlyNewTools: (state): number => {
      const now = new Date()
      const monthStart = new Date(now.getFullYear(), now.getMonth(), 1)
      return state.tools.filter(tool => {
        if (!tool.createdAt) return false
        const createdAt = new Date(tool.createdAt)
        return createdAt >= monthStart
      }).length
    },

    // 获取热门工具（按访问量排序，前8个）
    hotTools: (state): ToolResponse[] =>
      [...state.tools]
        .sort((a, b) => (b.visits || 0) - (a.visits || 0))
        .slice(0, 8),

    // 获取最新工具（假设按ID倒序，前8个）
    newTools: (state): ToolResponse[] =>
      [...state.tools]
        .sort((a, b) => b.id - a.id)
        .slice(0, 8),

    // 按分类分组的工具
    toolsByCategory: (state): Record<string, ToolResponse[]> => {
      const grouped: Record<string, ToolResponse[]> = {}
      state.tools.forEach(tool => {
        const category = tool.category || 'other'
        if (!grouped[category]) {
          grouped[category] = []
        }
        grouped[category].push(tool)
      })
      return grouped
    },

    // 获取分类工具列表（用于首页展示）
    categoryTools: (state): { categoryCode: string; categoryName: string; tools: ToolResponse[] }[] => {
      const grouped = {} as Record<string, ToolResponse[]>
      state.tools.forEach(tool => {
        const categoryCode = tool.category || 'other'
        if (!grouped[categoryCode]) {
          grouped[categoryCode] = []
        }
        grouped[categoryCode].push(tool)
      })

      return state.categories.map(cat => ({
        categoryCode: cat.code,
        categoryName: cat.name,
        tools: grouped[cat.code] || []
      }))
    },

    // 根据code获取工具
    getToolByCode: (state) => (code: string): ToolResponse | undefined =>
      state.tools.find(tool => tool.code === code),

    // 根据分类code获取工具列表
    getToolsByCategoryCode: (state) => (categoryCode: string): ToolResponse[] =>
      state.tools.filter(tool => tool.category === categoryCode)
  },

  actions: {
    // 初始化Store（获取分类和工具数据）
    async initialize() {
      // 如果已经初始化过，直接返回
      if (this.initialized && this.tools.length > 0) {
        return
      }

      this.loading = true
      this.error = null

      try {
        const config = useRuntimeConfig()
        const baseUrl = config.public.apiBaseUrl as string

        // 并行获取分类和工具数据
        const [categoriesRes, toolsRes] = await Promise.all([
          fetch(`${baseUrl}/store/categories`),
          fetch(`${baseUrl}/store/tools`)
        ])

        if (!categoriesRes.ok || !toolsRes.ok) {
          throw new Error('获取数据失败')
        }

        const categoriesData = await categoriesRes.json()
        const toolsData = await toolsRes.json()

        if (categoriesData.code === 200) {
          this.categories = categoriesData.data || []
        }

        if (toolsData.code === 200) {
          this.tools = toolsData.data || []
        }

        this.initialized = true
      } catch (err) {
        this.error = err instanceof Error ? err.message : '初始化失败'
        console.error('Store初始化失败:', err)
      } finally {
        this.loading = false
      }
    },

    // 设置分类数据（用于SSR时从服务端获取的数据）
    setCategories(categories: CategoryResponse[]) {
      this.categories = categories
    },

    // 设置工具数据（用于SSR时从服务端获取的数据）
    setTools(tools: ToolResponse[]) {
      this.tools = tools
    },

    // 标记已初始化
    setInitialized(value: boolean) {
      this.initialized = value
    }
  }
})
