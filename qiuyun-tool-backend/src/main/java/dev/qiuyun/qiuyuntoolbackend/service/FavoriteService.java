package dev.qiuyun.qiuyuntoolbackend.service;

import dev.qiuyun.qiuyuntoolbackend.entity.Tool;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Map;

/**
 * 收藏服务接口
 */
public interface FavoriteService {

    /**
     * 收藏工具
     *
     * @param userId 用户ID
     * @param toolId 工具ID
     * @return 是否成功
     */
    boolean addFavorite(Long userId, Long toolId);

    /**
     * 取消收藏
     *
     * @param userId 用户ID
     * @param toolId 工具ID
     * @return 是否成功
     */
    boolean removeFavorite(Long userId, Long toolId);

    /**
     * 检查用户是否已收藏该工具
     *
     * @param userId 用户ID
     * @param toolId 工具ID
     * @return 是否已收藏
     */
    boolean isFavorite(Long userId, Long toolId);

    /**
     * 获取工具的收藏数
     *
     * @param toolId 工具ID
     * @return 收藏数
     */
    long getFavoriteCount(Long toolId);

    /**
     * 获取用户的收藏列表
     *
     * @param userId 用户ID
     * @param pageable 分页参数
     * @return 收藏的工具列表
     */
    Page<Tool> getUserFavorites(Long userId, Pageable pageable);

    /**
     * 获取用户的收藏数量
     *
     * @param userId 用户ID
     * @return 收藏数量
     */
    long getUserFavoriteCount(Long userId);

    /**
     * 批量检查用户是否收藏了多个工具
     *
     * @param userId 用户ID
     * @param toolIds 工具ID列表
     * @return 工具ID到收藏状态的映射
     */
    Map<Long, Boolean> batchCheckFavorite(Long userId, List<Long> toolIds);

    /**
     * 切换收藏状态（收藏/取消收藏）
     *
     * @param userId 用户ID
     * @param toolId 工具ID
     * @return 切换后的收藏状态（true=已收藏，false=未收藏）
     */
    boolean toggleFavorite(Long userId, Long toolId);
}
