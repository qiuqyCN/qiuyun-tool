package dev.qiuyun.qiuyuntoolbackend.service;

import dev.qiuyun.qiuyuntoolbackend.payload.response.TagResponse;
import dev.qiuyun.qiuyuntoolbackend.payload.response.ToolResponse;

import java.util.List;

/**
 * 标签服务接口
 */
public interface TagService {

    /**
     * 获取所有标签
     */
    List<TagResponse> getAllTags();

    /**
     * 获取热门标签
     */
    List<TagResponse> getHotTags();

    /**
     * 根据名称获取标签
     */
    TagResponse getTagByName(String name);

    /**
     * 根据ID获取标签
     */
    TagResponse getTagById(Long id);

    /**
     * 根据标签名称获取工具列表
     */
    List<ToolResponse> getToolsByTagName(String tagName);
}
