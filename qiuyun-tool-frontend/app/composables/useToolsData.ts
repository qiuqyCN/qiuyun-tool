import type { ApiResponse, CategoryResponse, ToolResponse } from '~/types/api'

export interface ToolsData {
  categories: CategoryResponse[]
  tools: ToolResponse[]
}

/**
 * 获取工具和分类数据（SSR 安全）
 * 用于 SEO 优化，每页独立获取数据
 */
export async function useToolsData() {
  const config = useRuntimeConfig()

  const [{ data: categories }, { data: tools }] = await Promise.all([
    useFetch<ApiResponse<CategoryResponse[]>>('/api/store/categories', {
      baseURL: config.public.apiBaseUrl,
      server: true,
      default: () => ({ code: 200, message: 'success', data: [] })
    }),
    useFetch<ApiResponse<ToolResponse[]>>('/api/store/tools', {
      baseURL: config.public.apiBaseUrl,
      server: true,
      default: () => ({ code: 200, message: 'success', data: [] })
    })
  ])

  return {
    categories: computed(() => (categories.value as ApiResponse<CategoryResponse[]>)?.data || []),
    tools: computed(() => (tools.value as ApiResponse<ToolResponse[]>)?.data || [])
  }
}

/**
 * 工具筛选计算函数
 */
export function useToolFilters(tools: Ref<ToolResponse[]>) {
  // 热门工具（按访问量排序，前8个）
  const hotTools = computed(() =>
    [...tools.value]
      .sort((a, b) => (b.visits || 0) - (a.visits || 0))
      .slice(0, 8)
  )

  // 最新工具（按ID倒序，前8个）
  const newTools = computed(() =>
    [...tools.value]
      .sort((a, b) => b.id - a.id)
      .slice(0, 8)
  )

  // 按分类分组的工具
  const toolsByCategory = computed(() => {
    const grouped: Record<string, ToolResponse[]> = {}
    tools.value.forEach((tool: ToolResponse) => {
      const category = tool.category || 'other'
      if (!grouped[category]) {
        grouped[category] = []
      }
      grouped[category].push(tool)
    })
    return grouped
  })

  // 获取分类工具列表
  const categoryTools = (categories: CategoryResponse[]) => {
    return categories.map((cat: CategoryResponse) => ({
      categoryCode: cat.code,
      categoryName: cat.name,
      tools: toolsByCategory.value[cat.code] || []
    }))
  }

  // 根据code获取工具
  const getToolByCode = (code: string) => {
    return tools.value.find((tool: ToolResponse) => tool.code === code)
  }

  // 根据分类code获取工具列表
  const getToolsByCategoryCode = (categoryCode: string) => {
    return tools.value.filter((tool: ToolResponse) => tool.category === categoryCode)
  }

  // 获取相关工具（同分类的其他工具，排除当前工具）
  const getRelatedTools = (code: string, limit = 5) => {
    const tool = getToolByCode(code)
    if (!tool) return []
    return tools.value
      .filter((t: ToolResponse) => t.category === tool.category && t.code !== code)
      .slice(0, limit)
  }

  // 获取工具总数
  const totalTools = computed(() => tools.value.length)

  // 获取总访问量
  const totalVisits = computed(() =>
    tools.value.reduce((sum, tool) => sum + (tool.visits || 0), 0)
  )

  // 获取本月新增工具数
  const monthlyNewTools = computed(() => {
    const now = new Date()
    const monthStart = new Date(now.getFullYear(), now.getMonth(), 1)
    return tools.value.filter((tool: ToolResponse) => {
      if (!tool.createdAt) return false
      const createdAt = new Date(tool.createdAt)
      return createdAt >= monthStart
    }).length
  })

  return {
    hotTools,
    newTools,
    toolsByCategory,
    categoryTools,
    getToolByCode,
    getToolsByCategoryCode,
    getRelatedTools,
    totalTools,
    totalVisits,
    monthlyNewTools
  }
}
