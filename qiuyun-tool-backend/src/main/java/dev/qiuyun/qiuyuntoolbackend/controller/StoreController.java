package dev.qiuyun.qiuyuntoolbackend.controller;

import dev.qiuyun.qiuyuntoolbackend.payload.response.ApiResponse;
import dev.qiuyun.qiuyuntoolbackend.payload.response.CategoryResponse;
import dev.qiuyun.qiuyuntoolbackend.payload.response.ToolResponse;
import dev.qiuyun.qiuyuntoolbackend.service.StoreService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 商店/存储 Controller
 * 提供前端 Pinia Store 所需的基础数据接口
 */
@RestController
@RequestMapping("/api/store")
@RequiredArgsConstructor
public class StoreController {

    private final StoreService storeService;

    /**
     * 获取所有分类列表
     * 用于前端 Pinia Store 初始化分类数据
     */
    @GetMapping("/categories")
    public ApiResponse<List<CategoryResponse>> getCategories() {
        return ApiResponse.success(storeService.getAllCategories());
    }

    /**
     * 获取所有工具列表
     * 用于前端 Pinia Store 初始化工具数据
     */
    @GetMapping("/tools")
    public ApiResponse<List<ToolResponse>> getTools() {
        return ApiResponse.success(storeService.getAllTools());
    }
}
