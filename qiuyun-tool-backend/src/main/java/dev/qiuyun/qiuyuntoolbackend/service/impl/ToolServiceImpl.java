package dev.qiuyun.qiuyuntoolbackend.service.impl;

import dev.qiuyun.qiuyuntoolbackend.payload.response.ToolResponse;
import dev.qiuyun.qiuyuntoolbackend.entity.Category;
import dev.qiuyun.qiuyuntoolbackend.entity.Tag;
import dev.qiuyun.qiuyuntoolbackend.entity.Tool;
import dev.qiuyun.qiuyuntoolbackend.repository.CategoryRepository;
import dev.qiuyun.qiuyuntoolbackend.repository.ToolRepository;
import dev.qiuyun.qiuyuntoolbackend.service.ToolService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 工具服务实现类
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ToolServiceImpl implements ToolService {

    private final ToolRepository toolRepository;
    private final CategoryRepository categoryRepository;

    @Override
    public List<ToolResponse> getAllTools() {
        return toolRepository.findByIsActiveTrue()
                .stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<ToolResponse> getToolsByCategoryCode(String categoryCode) {
        Category category = categoryRepository.findByCode(categoryCode).orElse(null);
        if (category == null) {
            return Collections.emptyList();
        }
        return toolRepository.findByCategoryIdAndIsActiveTrue(category.getId())
                .stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<ToolResponse> getHotTools() {
        return toolRepository.findTop8ByIsActiveTrueOrderByVisitsCountDesc()
                .stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<ToolResponse> getNewTools() {
        return toolRepository.findTop8ByIsActiveTrueOrderByCreatedAtDesc()
                .stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<ToolResponse> searchTools(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return Collections.emptyList();
        }
        return toolRepository.searchByKeyword(keyword.trim())
                .stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public ToolResponse getToolByCode(String toolCode) {
        return toolRepository.findByCode(toolCode)
                .map(this::convertToResponse)
                .orElse(null);
    }

    @Override
    public ToolResponse getToolById(Long id) {
        return toolRepository.findById(id)
                .map(this::convertToResponse)
                .orElse(null);
    }

    /**
     * 转换Tool实体为响应对象
     */
    private ToolResponse convertToResponse(Tool tool) {
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
                        .map(Tag::getName)
                        .collect(Collectors.toList()))
                .build();
    }
}
