package dev.qiuyun.qiuyuntoolbackend.service.impl;

import dev.qiuyun.qiuyuntoolbackend.entity.Category;
import dev.qiuyun.qiuyuntoolbackend.entity.Tool;
import dev.qiuyun.qiuyuntoolbackend.enums.PriceMode;
import dev.qiuyun.qiuyuntoolbackend.enums.ToolSortType;
import dev.qiuyun.qiuyuntoolbackend.exception.ResourceNotFoundException;
import dev.qiuyun.qiuyuntoolbackend.payload.request.CategoryToolsRequest;
import dev.qiuyun.qiuyuntoolbackend.payload.response.CategoryResponse;
import dev.qiuyun.qiuyuntoolbackend.payload.response.CategoryToolsResponse;
import dev.qiuyun.qiuyuntoolbackend.payload.response.ToolResponse;
import dev.qiuyun.qiuyuntoolbackend.repository.CategoryRepository;
import dev.qiuyun.qiuyuntoolbackend.repository.ToolRepository;
import dev.qiuyun.qiuyuntoolbackend.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 分类服务实现类
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;
    private final ToolRepository toolRepository;

    @Override
    public List<CategoryResponse> getAllCategories() {
        return categoryRepository.findByIsActiveTrueOrderBySortOrderAsc()
                .stream()
                .map(CategoryResponse::from)
                .collect(Collectors.toList());
    }

    @Override
    public CategoryResponse getCategoryByCode(String code) {
        return categoryRepository.findByCode(code)
                .map(CategoryResponse::from)
                .orElse(null);
    }

    @Override
    public CategoryResponse getCategoryById(Long id) {
        return categoryRepository.findById(id)
                .map(CategoryResponse::from)
                .orElse(null);
    }

    @Override
    public CategoryToolsResponse getCategoryTools(String categoryCode, CategoryToolsRequest request) {
        // 获取分类信息
        Category category = null;
        if (!"all".equals(categoryCode)) {
            category = categoryRepository.findByCode(categoryCode)
                    .orElseThrow(() -> new ResourceNotFoundException("分类不存在: " + categoryCode));
        }

        // 获取工具列表
        List<Tool> tools;
        if ("all".equals(categoryCode)) {
            tools = toolRepository.findByIsActiveTrue();
        } else {
            tools = toolRepository.findByCategoryCodeAndIsActiveTrue(categoryCode);
        }

        // 价格筛选
        tools = filterByPriceMode(tools, request.getPrice());

        // 排序
        tools = sortTools(tools, request.getSort());

        // 构建响应
        return CategoryToolsResponse.builder()
                .categoryCode(categoryCode)
                .categoryName(category != null ? category.getName() : "全部工具")
                .categoryDescription(category != null ? category.getDescription() : "浏览所有可用工具")
                .tools(tools.stream()
                        .map(ToolResponse::simpleFrom)
                        .collect(Collectors.toList()))
                .build();
    }

    /**
     * 根据价格模式筛选工具
     */
    private List<Tool> filterByPriceMode(List<Tool> tools, PriceMode priceMode) {
        if (priceMode == null || priceMode == PriceMode.ALL) {
            return tools;
        }

        return tools.stream()
                .filter(tool -> {
                    if (priceMode == PriceMode.FREE) {
                        return !tool.getIsVip();
                    } else if (priceMode == PriceMode.VIP) {
                        return tool.getIsVip();
                    }
                    return true;
                })
                .collect(Collectors.toList());
    }

    /**
     * 对工具列表进行排序
     */
    private List<Tool> sortTools(List<Tool> tools, ToolSortType sortType) {
        if (sortType == null) {
            sortType = ToolSortType.VISITS;
        }

        Comparator<Tool> comparator;

        switch (sortType) {
            case RATING:
                comparator = Comparator.comparing(Tool::getRating, Comparator.reverseOrder());
                break;
            case NEWEST:
                comparator = Comparator.comparing(Tool::getCreatedAt, Comparator.reverseOrder());
                break;
            case NAME:
                comparator = Comparator.comparing(Tool::getName);
                break;
            case VISITS:
            default:
                comparator = Comparator.comparing(Tool::getVisitsCount, Comparator.reverseOrder());
                break;
        }

        return tools.stream()
                .sorted(comparator)
                .collect(Collectors.toList());
    }
}
