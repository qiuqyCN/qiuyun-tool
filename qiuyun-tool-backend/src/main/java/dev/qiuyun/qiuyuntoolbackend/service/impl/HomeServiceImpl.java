package dev.qiuyun.qiuyuntoolbackend.service.impl;

import dev.qiuyun.qiuyuntoolbackend.entity.Category;
import dev.qiuyun.qiuyuntoolbackend.entity.Tool;
import dev.qiuyun.qiuyuntoolbackend.payload.response.CategoryResponse;
import dev.qiuyun.qiuyuntoolbackend.payload.response.HomeDataResponse;
import dev.qiuyun.qiuyuntoolbackend.payload.response.StatsResponse;
import dev.qiuyun.qiuyuntoolbackend.payload.response.ToolResponse;
import dev.qiuyun.qiuyuntoolbackend.repository.CategoryRepository;
import dev.qiuyun.qiuyuntoolbackend.repository.ToolRepository;
import dev.qiuyun.qiuyuntoolbackend.service.HomeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 首页服务实现类
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class HomeServiceImpl implements HomeService {

    private final CategoryRepository categoryRepository;
    private final ToolRepository toolRepository;

    @Override
    public HomeDataResponse getHomeData() {
        // 获取所有分类
        List<Category> categoryEntities = categoryRepository.findByIsActiveTrueOrderBySortOrderAsc();

        // 一次性获取所有启用的工具
        List<Tool> allTools = toolRepository.findByIsActiveTrue();

        // 按分类分组工具（在内存中处理，减少数据库查询）
        Map<String, List<Tool>> toolsByCategory = allTools.stream()
                .filter(tool -> tool.getCategory() != null)
                .collect(Collectors.groupingBy(tool -> tool.getCategory().getCode()));

        // 转换分类响应（使用内存统计工具数量）
        List<CategoryResponse> categories = categoryEntities.stream()
                .map(category -> convertToCategoryResponse(category, toolsByCategory))
                .collect(Collectors.toList());

        // 获取热门工具（8个）
        List<ToolResponse> hotTools = allTools.stream()
                .sorted((t1, t2) -> t2.getVisitsCount().compareTo(t1.getVisitsCount()))
                .limit(8)
                .map(this::convertToToolResponse)
                .collect(Collectors.toList());

        // 获取最新工具（8个）
        List<ToolResponse> newTools = allTools.stream()
                .sorted((t1, t2) -> t2.getCreatedAt().compareTo(t1.getCreatedAt()))
                .limit(8)
                .map(this::convertToToolResponse)
                .collect(Collectors.toList());

        // 构建分类工具列表（从内存分组中获取）
        List<HomeDataResponse.CategoryToolsResponse> categoryTools = categories.stream()
                .map(category -> {
                    List<ToolResponse> tools = toolsByCategory.getOrDefault(category.getCode(), List.of())
                            .stream()
                            .map(this::convertToToolResponse)
                            .collect(Collectors.toList());
                    return HomeDataResponse.CategoryToolsResponse.builder()
                            .categoryCode(category.getCode())
                            .categoryName(category.getName())
                            .tools(tools)
                            .build();
                })
                .collect(Collectors.toList());

        // 构建统计数据（从已加载的工具列表计算）
        StatsResponse stats = buildStatsResponse(allTools);

        return HomeDataResponse.builder()
                .categories(categories)
                .hotTools(hotTools)
                .newTools(newTools)
                .categoryTools(categoryTools)
                .stats(stats)
                .build();
    }

    /**
     * 构建统计数据（从已加载的工具列表计算，避免重复查询数据库）
     */
    private StatsResponse buildStatsResponse(List<Tool> allTools) {
        // 统计工具总数
        Integer totalTools = allTools.size();

        // 统计月访问量（所有工具访问量之和）
        Long monthlyVisits = allTools.stream()
                .mapToLong(Tool::getVisitsCount)
                .sum();

        // 日活跃用户（模拟数据：月访问量的 1/30，最低 1000）
        Integer dailyActiveUsers = (int) Math.max(monthlyVisits / 30, 1000);

        return StatsResponse.builder()
                .totalTools(totalTools)
                .dailyActiveUsers(dailyActiveUsers)
                .monthlyVisits(monthlyVisits)
                .build();
    }

    /**
     * 转换Category实体为响应对象（使用内存统计工具数量）
     */
    private CategoryResponse convertToCategoryResponse(Category category, Map<String, List<Tool>> toolsByCategory) {
        // 从内存分组中统计该分类下的工具数量
        Integer toolCount = toolsByCategory.getOrDefault(category.getCode(), List.of()).size();

        return CategoryResponse.builder()
                .id(category.getId())
                .code(category.getCode())
                .name(category.getName())
                .icon(category.getIcon())
                .description(category.getDescription())
                .toolCount(toolCount)
                .build();
    }

    /**
     * 转换Tool实体为响应对象
     */
    private ToolResponse convertToToolResponse(Tool tool) {
        return ToolResponse.builder()
                .id(tool.getId())
                .code(tool.getCode())
                .name(tool.getName())
                .description(tool.getDescription())
                .category(tool.getCategory() != null ? tool.getCategory().getCode() : null)
                .icon(tool.getIcon())
                .isVip(tool.getIsVip())
                .visits(tool.getVisitsCount())
                .rating(tool.getRating())
                .reviewCount(tool.getReviewCount())
                .tags(tool.getTags().stream()
                        .map(tag -> tag.getName())
                        .collect(Collectors.toList()))
                .build();
    }
}
