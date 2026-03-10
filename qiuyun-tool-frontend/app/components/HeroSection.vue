<script setup lang="ts">
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Badge } from "@/components/ui/badge";
import { Search, Sparkles, Loader2 } from "lucide-vue-next";
import type { ToolResponse } from "@/types/api";

// 统计数据接口
interface Stats {
  totalTools: number;
  monthlyNewTools: number;
  totalVisits: number;
}

interface SearchResult {
  tool: ToolResponse;
  relevance: number;
}

const props = defineProps<{
  tools: ToolResponse[];
  loading?: boolean;
  stats?: Stats;
}>();

const emit = defineEmits<{
  select: [tool: ToolResponse];
}>();

const searchQuery = ref("");
const isSearching = ref(false);
const searchResults = ref<SearchResult[]>([]);
const selectedIndex = ref(-1);
const showResults = ref(false);
const searchInputRef = ref<HTMLInputElement | null>(null);
const resultsContainerRef = ref<HTMLDivElement | null>(null);

// 计算搜索相关性
const calculateRelevance = (tool: ToolResponse, query: string): number => {
  const lowerQuery = query.toLowerCase();
  const lowerName = tool.name.toLowerCase();
  const lowerDesc = tool.description.toLowerCase();
  const lowerTags = tool.tags.map((t) => t.toLowerCase());

  let score = 0;
  let hasMatch = false;

  // 名称完全匹配得分最高
  if (lowerName === lowerQuery) {
    score += 100;
    hasMatch = true;
  }
  // 名称开头匹配
  else if (lowerName.startsWith(lowerQuery)) {
    score += 80;
    hasMatch = true;
  }
  // 名称包含
  else if (lowerName.includes(lowerQuery)) {
    score += 60;
    hasMatch = true;
  }

  // 标签匹配
  if (lowerTags.some((tag) => tag === lowerQuery)) {
    score += 50;
    hasMatch = true;
  } else if (lowerTags.some((tag) => tag.includes(lowerQuery))) {
    score += 30;
    hasMatch = true;
  }

  // 描述包含
  if (lowerDesc.includes(lowerQuery)) {
    score += 20;
    hasMatch = true;
  }

  // 如果没有匹配，返回 0
  if (!hasMatch) {
    return 0;
  }

  // 访问量加成（热门工具优先）- 只在有匹配时添加
  score += Math.min((tool.visits || 0) / 10000, 10);

  return score;
};

// 执行搜索
const performSearch = () => {
  const query = searchQuery.value.trim();
  if (!query) {
    searchResults.value = [];
    showResults.value = false;
    return;
  }

  isSearching.value = true;

  // 本地搜索所有工具
  const results: SearchResult[] = props.tools
    .map((tool) => ({
      tool,
      relevance: calculateRelevance(tool, query),
    }))
    .filter((result) => result.relevance > 0)
    .sort((a, b) => b.relevance - a.relevance)
    .slice(0, 10); // 最多显示10个结果

  searchResults.value = results;
  selectedIndex.value = results.length > 0 ? 0 : -1;
  showResults.value = true;
  isSearching.value = false;
};

// 防抖搜索
let searchTimeout: ReturnType<typeof setTimeout> | null = null;
const debouncedSearch = () => {
  if (searchTimeout) {
    clearTimeout(searchTimeout);
  }
  searchTimeout = setTimeout(() => {
    performSearch();
  }, 200);
};

// 监听输入变化
watch(searchQuery, () => {
  debouncedSearch();
});

// 监听工具数据变化
watch(
  () => props.tools,
  () => {
    if (searchQuery.value.trim()) {
      performSearch();
    }
  },
  { deep: true }
);

// 键盘导航
const handleKeydown = (e: KeyboardEvent) => {
  if (!showResults.value || searchResults.value.length === 0) return;

  switch (e.key) {
    case "ArrowDown":
      e.preventDefault();
      selectedIndex.value =
        (selectedIndex.value + 1) % searchResults.value.length;
      scrollToSelected();
      break;
    case "ArrowUp":
      e.preventDefault();
      selectedIndex.value =
        selectedIndex.value <= 0
          ? searchResults.value.length - 1
          : selectedIndex.value - 1;
      scrollToSelected();
      break;
    case "Enter":
      e.preventDefault();
      if (
        selectedIndex.value >= 0 &&
        selectedIndex.value < searchResults.value.length
      ) {
        const result = searchResults.value[selectedIndex.value];
        if (result && result.tool) {
          selectTool(result.tool);
        }
      }
      break;
    case "Escape":
      showResults.value = false;
      selectedIndex.value = -1;
      break;
  }
};

// 滚动到选中项
const scrollToSelected = () => {
  nextTick(() => {
    const container = resultsContainerRef.value;
    if (!container) return;

    const selector = `[data-index="${selectedIndex.value}"]`;
    const selectedElement = container.querySelector(selector) as HTMLElement | null;
    if (selectedElement) {
      selectedElement.scrollIntoView({ block: "nearest", behavior: "smooth" });
    }
  });
};

// 选择工具
const selectTool = (tool: ToolResponse) => {
  emit("select", tool);
  showResults.value = false;
  searchQuery.value = "";
};

// 高亮匹配文本
const highlightText = (text: string, query: string): string => {
  if (!query.trim()) return text;
  const regex = new RegExp(`(${escapeRegExp(query)})`, "gi");
  return text.replace(regex, '<mark class="search-highlight">$1</mark>');
};

// 转义正则特殊字符
const escapeRegExp = (string: string): string => {
  return string.replace(/[.*+?^${}()|[\]\\]/g, "\\$&");
};

// 格式化数字（转换为 w/k 格式）
const formatNumber = (num: number): string => {
  if (num >= 10000) {
    return (num / 10000).toFixed(0) + 'w+';
  }
  if (num >= 1000) {
    return (num / 1000).toFixed(0) + 'k+';
  }
  return num + '+';
};

// 点击外部关闭结果
const handleClickOutside = (e: MouseEvent) => {
  const target = e.target as HTMLElement;
  const searchBox = document.querySelector(".search-box-container");
  if (searchBox && !searchBox.contains(target)) {
    showResults.value = false;
  }
};

onMounted(() => {
  document.addEventListener("click", handleClickOutside);
});

onUnmounted(() => {
  document.removeEventListener("click", handleClickOutside);
  if (searchTimeout) {
    clearTimeout(searchTimeout);
  }
});
</script>

<template>
  <section class="relative py-10 lg:py-16">
    <!-- Background Grid -->
    <div class="absolute inset-0 -z-10 overflow-hidden">
      <div
        class="absolute inset-0 bg-[linear-gradient(to_right,#80808012_1px,transparent_1px),linear-gradient(to_bottom,#80808012_1px,transparent_1px)] bg-size-[24px_24px]"
      ></div>
      <div
        class="absolute left-0 right-0 top-0 -z-10 m-auto h-[310px] w-[310px] rounded-full bg-sky-400 opacity-20 blur-[100px]"
      ></div>
    </div>
    <div class="container mx-auto px-4 relative">
      <div class="max-w-3xl mx-auto text-center">
        <Badge variant="secondary" class="mb-4">
          <Sparkles class="w-3 h-3 mr-1" />
          程序员必备工具箱
        </Badge>
        <h1
          class="text-4xl lg:text-6xl font-bold mb-6 text-transparent bg-clip-text bg-linear-to-r from-cyan-200 to-blue-600"
        >
          秋云工具
        </h1>
        <p class="text-lg lg:text-xl text-muted-foreground mb-8">
          高效、简洁、实用的在线工具集合<br class="hidden sm:block" />
          助力开发者提升工作效率
        </p>

        <!-- Search Box -->
        <div class="search-box-container max-w-xl mx-auto relative">
          <div class="relative">
            <Search
              class="absolute left-3 top-1/2 -translate-y-1/2 w-5 h-5 text-muted-foreground"
            />
            <Input
              ref="searchInputRef"
              v-model="searchQuery"
              type="text"
              placeholder="搜索工具名称或描述..."
              class="pl-10 pr-12 h-12 text-base"
              @keydown="handleKeydown"
              @focus="searchQuery.trim() && (showResults = true)"
            />
            <div
              v-if="isSearching || loading"
              class="absolute right-3 top-1/2 -translate-y-1/2"
            >
              <Loader2 class="w-5 h-5 animate-spin text-muted-foreground" />
            </div>
          </div>

          <!-- Search Results Dropdown -->
          <div
            v-if="showResults && searchResults.length > 0"
            class="absolute top-full left-0 right-0 mt-2 bg-background border border-border rounded-lg shadow-lg max-h-80 flex flex-col z-50"
          >
            <!-- Scrollable Results List -->
            <div ref="resultsContainerRef" class="flex-1 overflow-y-auto py-2">
              <div
                v-for="(result, index) in searchResults"
                :key="result.tool.code"
                :data-index="index"
                :class="[
                  'px-4 py-3 cursor-pointer transition-colors',
                  index === selectedIndex
                    ? 'bg-primary/10 border-l-4 border-primary'
                    : 'hover:bg-muted border-l-4 border-transparent',
                ]"
                @click="selectTool(result.tool)"
                @mouseenter="selectedIndex = index"
              >
                <div class="flex items-center gap-3">
                  <!-- Tool Icon -->
                  <div
                    class="w-10 h-10 rounded-lg bg-primary/10 flex items-center justify-center shrink-0"
                  >
                    <component
                      :is="
                        $slots[`icon-${result.tool.icon}`]
                          ? $slots[`icon-${result.tool.icon}`]
                          : 'span'
                      "
                      class="w-5 h-5 text-primary"
                    >
                      <span class="text-lg">🔧</span>
                    </component>
                  </div>

                  <!-- Tool Info -->
                  <div class="flex-1 min-w-0 text-left">
                    <div
                      class="font-medium text-foreground truncate"
                      v-html="highlightText(result.tool.name, searchQuery)"
                    />
                    <div
                      class="text-sm text-muted-foreground truncate"
                      v-html="highlightText(result.tool.description, searchQuery)"
                    />
                  </div>

                  <!-- Tags -->
                  <div class="flex gap-1 shrink-0">
                    <Badge
                      v-for="tag in result.tool.tags.slice(0, 2)"
                      :key="tag"
                      variant="secondary"
                      class="text-xs"
                    >
                      {{ tag }}
                    </Badge>
                  </div>

                  <!-- VIP Badge -->
                  <Badge
                    v-if="result.tool.isVip"
                    variant="default"
                    class="bg-linear-to-r from-yellow-400 to-orange-500 text-white shrink-0"
                  >
                    VIP
                  </Badge>
                </div>
              </div>
            </div>

            <!-- Keyboard Hints - Fixed at Bottom -->
            <div
              class="px-4 py-2 border-t border-border bg-muted/50 text-xs text-muted-foreground flex items-center justify-between shrink-0"
            >
              <div class="flex items-center gap-4">
                <span class="flex items-center gap-1">
                  <kbd
                    class="px-1.5 py-0.5 bg-background border rounded text-xs"
                    >↑</kbd
                  >
                  <kbd
                    class="px-1.5 py-0.5 bg-background border rounded text-xs"
                    >↓</kbd
                  >
                  选择
                </span>
                <span class="flex items-center gap-1">
                  <kbd
                    class="px-1.5 py-0.5 bg-background border rounded text-xs"
                    >Enter</kbd
                  >
                  进入
                </span>
                <span class="flex items-center gap-1">
                  <kbd
                    class="px-1.5 py-0.5 bg-background border rounded text-xs"
                    >Esc</kbd
                  >
                  关闭
                </span>
              </div>
              <span>{{ searchResults.length }} 个结果</span>
            </div>
          </div>

          <!-- No Results -->
          <div
            v-else-if="showResults && searchQuery.trim() && !isSearching"
            class="absolute top-full left-0 right-0 mt-2 bg-background border border-border rounded-lg shadow-lg p-8 text-center z-50"
          >
            <div class="text-muted-foreground">
              <Search class="w-12 h-12 mx-auto mb-3 opacity-50" />
              <p>未找到与 "{{ searchQuery }}" 相关的工具</p>
              <p class="text-sm mt-1">请尝试其他关键词</p>
            </div>
          </div>
        </div>

        <!-- Quick Stats -->
        <div
          class="flex items-center justify-center gap-8 mt-8 text-sm text-muted-foreground"
        >
          <div class="flex items-center gap-2">
            <span class="font-semibold text-foreground">{{ formatNumber(props.stats?.totalTools || 0) }}</span>
            <span>实用工具</span>
          </div>
           <div class="flex items-center gap-2">
            <span class="font-semibold text-foreground">{{ formatNumber(props.stats?.monthlyNewTools || 0) }}</span>
            <span>本月新增</span>
          </div>
          <div class="flex items-center gap-2">
            <span class="font-semibold text-foreground">{{ formatNumber(props.stats?.totalVisits || 0) }}</span>
            <span>总使用量</span>
          </div>
        </div>
      </div>
    </div>
  </section>
</template>

<style scoped>
/* 自定义滚动条样式 */
.overflow-y-auto::-webkit-scrollbar {
  width: 8px;
}

.overflow-y-auto::-webkit-scrollbar-track {
  background: hsl(var(--muted) / 0.5);
  border-radius: 4px;
}

.overflow-y-auto::-webkit-scrollbar-thumb {
  background: hsl(var(--muted-foreground) / 0.5);
  border-radius: 4px;
  border: 2px solid transparent;
  background-clip: content-box;
}

.overflow-y-auto::-webkit-scrollbar-thumb:hover {
  background: hsl(var(--muted-foreground) / 0.8);
  border: 2px solid transparent;
  background-clip: content-box;
}

/* Firefox 滚动条 */
.overflow-y-auto {
  scrollbar-width: thin;
  scrollbar-color: hsl(var(--muted-foreground) / 0.5) hsl(var(--muted) / 0.5);
}

/* 高亮样式 */
:deep(.search-highlight) {
  background: linear-gradient(120deg, hsl(var(--primary) / 0.3) 0%, hsl(var(--primary) / 0.2) 100%);
  color: hsl(var(--primary));
  font-weight: 600;
  padding: 1px 0;
  border-radius: 2px;
  box-decoration-break: clone;
  -webkit-box-decoration-break: clone;
}

/* 选中项中的高亮样式 */
:deep([data-index].bg-primary\/10 .search-highlight) {
  background: linear-gradient(120deg, hsl(var(--primary) / 0.5) 0%, hsl(var(--primary) / 0.3) 100%);
  color: hsl(var(--primary-foreground));
}
</style>
