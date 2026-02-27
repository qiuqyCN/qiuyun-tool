package dev.qiuyun.qiuyuntoolbackend.service.impl;

import dev.qiuyun.qiuyuntoolbackend.entity.Category;
import dev.qiuyun.qiuyuntoolbackend.entity.Tool;
import dev.qiuyun.qiuyuntoolbackend.payload.response.CategoryResponse;
import dev.qiuyun.qiuyuntoolbackend.payload.response.HomeDataResponse;
import dev.qiuyun.qiuyuntoolbackend.payload.response.ToolResponse;
import dev.qiuyun.qiuyuntoolbackend.repository.CategoryRepository;
import dev.qiuyun.qiuyuntoolbackend.repository.ToolRepository;
import dev.qiuyun.qiuyuntoolbackend.service.HomeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
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
        List<CategoryResponse> categories = categoryRepository.findByIsActiveTrueOrderBySortOrderAsc()
                .stream()
                .map(this::convertToCategoryResponse)
                .collect(Collectors.toList());

        // 获取热门工具（8个）
        List<ToolResponse> hotTools = toolRepository.findTop8ByIsActiveTrueOrderByVisitsCountDesc()
                .stream()
                .map(this::convertToToolResponse)
                .collect(Collectors.toList());

        // 获取最新工具（8个）
        List<ToolResponse> newTools = toolRepository.findTop8ByIsActiveTrueOrderByCreatedAtDesc()
                .stream()
                .map(this::convertToToolResponse)
                .collect(Collectors.toList());

        // 获取所有工具并按分类分组
        List<HomeDataResponse.CategoryToolsResponse> categoryTools = categories.stream()
                .map(category -> {
                    List<ToolResponse> tools = toolRepository.findByCategoryCodeAndIsActiveTrue(category.getCode())
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

        return HomeDataResponse.builder()
                .categories(categories)
                .hotTools(hotTools)
                .newTools(newTools)
                .categoryTools(categoryTools)
                .build();
    }

    /**
     * 转换Category实体为响应对象
     */
    private CategoryResponse convertToCategoryResponse(Category category) {
        // 统计该分类下的工具数量
        Integer toolCount = toolRepository.countByCategoryCodeAndIsActiveTrue(category.getCode());
        
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
