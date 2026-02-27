package dev.qiuyun.qiuyuntoolbackend.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import dev.qiuyun.qiuyuntoolbackend.payload.response.ApiResponse;
import dev.qiuyun.qiuyuntoolbackend.payload.response.HomeDataResponse;
import dev.qiuyun.qiuyuntoolbackend.service.HomeService;
import lombok.RequiredArgsConstructor;

/**
 * 首页Controller
 */
@RestController
@RequestMapping("/api/home")
@RequiredArgsConstructor
public class HomeController {

    private final HomeService homeService;

    /**
     * 获取首页所有数据（聚合）
     * 包含：分类列表、热门工具、最新工具、按分类分组的工具列表
     */
    @GetMapping
    public ApiResponse<HomeDataResponse> getHomeData() {
        return ApiResponse.success(homeService.getHomeData());
    }
}
