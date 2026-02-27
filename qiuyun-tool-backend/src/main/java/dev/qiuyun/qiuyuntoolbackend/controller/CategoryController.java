package dev.qiuyun.qiuyuntoolbackend.controller;

import dev.qiuyun.qiuyuntoolbackend.payload.request.CategoryToolsRequest;
import dev.qiuyun.qiuyuntoolbackend.payload.response.ApiResponse;
import dev.qiuyun.qiuyuntoolbackend.payload.response.CategoryToolsResponse;
import dev.qiuyun.qiuyuntoolbackend.service.CategoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 分类Controller
 */
@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    /**
     * 获取分类下的工具列表
     * @param categoryCode 分类代码，"all"表示全部工具
     * @param request 排序参数
     */
    @GetMapping("/{categoryCode}/tools")
    public ApiResponse<CategoryToolsResponse> getCategoryTools(
            @PathVariable String categoryCode,
            @Valid CategoryToolsRequest request) {
        return ApiResponse.success(categoryService.getCategoryTools(categoryCode, request));
    }
}
