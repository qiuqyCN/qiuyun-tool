package dev.qiuyun.qiuyuntoolbackend.controller;

import dev.qiuyun.qiuyuntoolbackend.exception.ResourceNotFoundException;
import dev.qiuyun.qiuyuntoolbackend.payload.request.ToolSearchRequest;
import dev.qiuyun.qiuyuntoolbackend.payload.response.ApiResponse;
import dev.qiuyun.qiuyuntoolbackend.payload.response.CategoryResponse;
import dev.qiuyun.qiuyuntoolbackend.payload.response.HomeDataResponse;
import dev.qiuyun.qiuyuntoolbackend.payload.response.TagResponse;
import dev.qiuyun.qiuyuntoolbackend.payload.response.ToolResponse;
import dev.qiuyun.qiuyuntoolbackend.service.CategoryService;
import dev.qiuyun.qiuyuntoolbackend.service.TagService;
import dev.qiuyun.qiuyuntoolbackend.service.ToolService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 首页Controller
 */
@RestController
@RequestMapping("/api/home")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
@Validated
public class HomeController {

    private final CategoryService categoryService;
    private final ToolService toolService;
    private final TagService tagService;

    /**
     * 获取首页数据（分类、热门工具、最新工具、热门标签）
     */
    @GetMapping
    public ApiResponse<HomeDataResponse> getHomeData() {
        HomeDataResponse data = HomeDataResponse.builder()
                .categories(categoryService.getAllCategories())
                .hotTools(toolService.getHotTools())
                .newTools(toolService.getNewTools())
                .hotTags(tagService.getHotTags())
                .build();
        return ApiResponse.success(data);
    }

    /**
     * 获取所有分类
     */
    @GetMapping("/categories")
    public ApiResponse<List<CategoryResponse>> getCategories() {
        return ApiResponse.success(categoryService.getAllCategories());
    }

    /**
     * 根据code获取分类
     */
    @GetMapping("/categories/{code}")
    public ApiResponse<CategoryResponse> getCategoryByCode(
            @PathVariable
            @NotBlank(message = "分类编码不能为空")
            @Pattern(regexp = "^[a-z0-9_-]+$", message = "分类编码格式不正确")
            String code) {
        CategoryResponse category = categoryService.getCategoryByCode(code);
        if (category == null) {
            throw new ResourceNotFoundException("分类", code);
        }
        return ApiResponse.success(category);
    }

    /**
     * 获取热门工具
     */
    @GetMapping("/tools/hot")
    public ApiResponse<List<ToolResponse>> getHotTools() {
        return ApiResponse.success(toolService.getHotTools());
    }

    /**
     * 获取最新工具
     */
    @GetMapping("/tools/new")
    public ApiResponse<List<ToolResponse>> getNewTools() {
        return ApiResponse.success(toolService.getNewTools());
    }

    /**
     * 获取所有工具
     */
    @GetMapping("/tools")
    public ApiResponse<List<ToolResponse>> getAllTools() {
        return ApiResponse.success(toolService.getAllTools());
    }

    /**
     * 根据分类code获取工具
     */
    @GetMapping("/tools/category/{categoryCode}")
    public ApiResponse<List<ToolResponse>> getToolsByCategory(
            @PathVariable
            @NotBlank(message = "分类编码不能为空")
            @Pattern(regexp = "^[a-z0-9_-]+$", message = "分类编码格式不正确")
            String categoryCode) {
        return ApiResponse.success(toolService.getToolsByCategoryCode(categoryCode));
    }

    /**
     * 搜索工具（带参数校验）
     */
    @GetMapping("/tools/search")
    public ApiResponse<List<ToolResponse>> searchTools(
            @RequestParam
            @NotBlank(message = "搜索关键词不能为空")
            @Size(max = 100, message = "搜索关键词长度不能超过100个字符")
            String keyword) {
        return ApiResponse.success(toolService.searchTools(keyword));
    }

    /**
     * 高级搜索工具（使用请求对象）
     */
    @GetMapping("/tools/search/advanced")
    public ApiResponse<List<ToolResponse>> searchToolsAdvanced(@Valid ToolSearchRequest request) {
        return ApiResponse.success(toolService.searchTools(request.getKeyword()));
    }

    /**
     * 获取工具详情（根据code）
     */
    @GetMapping("/tools/{toolCode}")
    public ApiResponse<ToolResponse> getToolByCode(
            @PathVariable
            @NotBlank(message = "工具编码不能为空")
            @Pattern(regexp = "^[a-z0-9_-]+$", message = "工具编码格式不正确")
            String toolCode) {
        ToolResponse tool = toolService.getToolByCode(toolCode);
        if (tool == null) {
            throw new ResourceNotFoundException("工具", toolCode);
        }
        return ApiResponse.success(tool);
    }

    /**
     * 获取所有标签
     */
    @GetMapping("/tags")
    public ApiResponse<List<TagResponse>> getAllTags() {
        return ApiResponse.success(tagService.getAllTags());
    }

    /**
     * 获取热门标签
     */
    @GetMapping("/tags/hot")
    public ApiResponse<List<TagResponse>> getHotTags() {
        return ApiResponse.success(tagService.getHotTags());
    }

    /**
     * 根据标签名称获取工具
     */
    @GetMapping("/tags/{tagName}/tools")
    public ApiResponse<List<ToolResponse>> getToolsByTagName(
            @PathVariable
            @NotBlank(message = "标签名称不能为空")
            @Size(min = 1, max = 50, message = "标签名称长度必须在1-50个字符之间")
            String tagName) {
        return ApiResponse.success(tagService.getToolsByTagName(tagName));
    }
}
