package dev.qiuyun.qiuyuntoolbackend.service;

import dev.qiuyun.qiuyuntoolbackend.payload.response.HomeDataResponse;

/**
 * 首页服务接口
 */
public interface HomeService {

    /**
     * 获取首页所有数据（聚合）
     */
    HomeDataResponse getHomeData();
}
