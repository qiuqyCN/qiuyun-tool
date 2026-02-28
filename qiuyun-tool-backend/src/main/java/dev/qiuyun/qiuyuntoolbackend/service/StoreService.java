package dev.qiuyun.qiuyuntoolbackend.service;

import dev.qiuyun.qiuyuntoolbackend.payload.response.CategoryResponse;
import dev.qiuyun.qiuyuntoolbackend.payload.response.ToolResponse;

import java.util.List;

/**
 * 商店/存储 Service
 * 提供前端 Pinia Store 所需的基础数据
 */
public interface StoreService {

    /**
     * 获取所有分类列表
     */
    List<CategoryResponse> getAllCategories();

    /**
     * 获取所有工具列表
     */
    List<ToolResponse> getAllTools();
}
