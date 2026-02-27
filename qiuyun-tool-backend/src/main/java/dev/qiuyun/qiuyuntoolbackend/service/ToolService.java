package dev.qiuyun.qiuyuntoolbackend.service;

import dev.qiuyun.qiuyuntoolbackend.payload.response.ToolResponse;

import java.util.List;

/**
 * 工具服务接口
 */
public interface ToolService {

    /**
     * 获取所有工具
     */
    List<ToolResponse> getAllTools();

    /**
     * 根据分类code获取工具
     */
    List<ToolResponse> getToolsByCategoryCode(String categoryCode);

    /**
     * 获取热门工具
     */
    List<ToolResponse> getHotTools();

    /**
     * 获取最新工具
     */
    List<ToolResponse> getNewTools();

    /**
     * 搜索工具
     */
    List<ToolResponse> searchTools(String keyword);

    /**
     * 获取工具详情（根据code）
     */
    ToolResponse getToolByCode(String toolCode);

    /**
     * 根据ID获取工具
     */
    ToolResponse getToolById(Long id);
}
