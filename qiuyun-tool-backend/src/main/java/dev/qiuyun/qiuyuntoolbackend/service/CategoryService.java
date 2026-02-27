package dev.qiuyun.qiuyuntoolbackend.service;

import dev.qiuyun.qiuyuntoolbackend.payload.request.CategoryToolsRequest;
import dev.qiuyun.qiuyuntoolbackend.payload.response.CategoryResponse;
import dev.qiuyun.qiuyuntoolbackend.payload.response.CategoryToolsResponse;

import java.util.List;

/**
 * 分类服务接口
 */
public interface CategoryService {

    /**
     * 获取所有分类
     */
    List<CategoryResponse> getAllCategories();

    /**
     * 根据code获取分类
     */
    CategoryResponse getCategoryByCode(String code);

    /**
     * 根据ID获取分类
     */
    CategoryResponse getCategoryById(Long id);

    /**
     * 获取分类下的工具列表
     */
    CategoryToolsResponse getCategoryTools(String categoryCode, CategoryToolsRequest request);
}
