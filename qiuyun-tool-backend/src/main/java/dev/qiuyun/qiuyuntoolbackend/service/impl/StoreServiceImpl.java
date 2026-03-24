package dev.qiuyun.qiuyuntoolbackend.service.impl;

import dev.qiuyun.qiuyuntoolbackend.payload.response.CategoryResponse;
import dev.qiuyun.qiuyuntoolbackend.payload.response.ToolResponse;
import dev.qiuyun.qiuyuntoolbackend.repository.CategoryRepository;
import dev.qiuyun.qiuyuntoolbackend.repository.ToolRepository;
import dev.qiuyun.qiuyuntoolbackend.service.StoreService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 商店/存储 Service 实现类
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StoreServiceImpl implements StoreService {

    private final CategoryRepository categoryRepository;
    private final ToolRepository toolRepository;

    @Override
    public List<CategoryResponse> getAllCategories() {
        // 获取所有启用的分类，toolCount 由前端自行计算
        return categoryRepository.findByIsActiveTrueOrderBySortOrderAsc()
                .stream()
                .map(CategoryResponse::from)
                .collect(Collectors.toList());
    }

    @Override
    public List<ToolResponse> getAllTools() {
        // 使用 JOIN FETCH 一次性加载标签和分类，避免 N+1 查询问题
        return toolRepository.findByIsActiveTrueWithTags()
                .stream()
                .map(ToolResponse::from)
                .collect(Collectors.toList());
    }
}
