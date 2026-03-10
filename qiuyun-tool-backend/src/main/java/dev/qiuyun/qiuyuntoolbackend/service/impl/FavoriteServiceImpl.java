package dev.qiuyun.qiuyuntoolbackend.service.impl;

import dev.qiuyun.qiuyuntoolbackend.entity.Tool;
import dev.qiuyun.qiuyuntoolbackend.entity.ToolAccessLog;
import dev.qiuyun.qiuyuntoolbackend.entity.UserFavorite;
import dev.qiuyun.qiuyuntoolbackend.exception.ResourceNotFoundException;
import dev.qiuyun.qiuyuntoolbackend.repository.ToolRepository;
import dev.qiuyun.qiuyuntoolbackend.repository.UserFavoriteRepository;
import dev.qiuyun.qiuyuntoolbackend.service.FavoriteService;
import dev.qiuyun.qiuyuntoolbackend.service.RankingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 收藏服务实现
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class FavoriteServiceImpl implements FavoriteService {

    private final UserFavoriteRepository userFavoriteRepository;
    private final ToolRepository toolRepository;
    private final RankingService rankingService;

    @Override
    @Transactional
    public boolean addFavorite(Long userId, Long toolId) {
        // 检查工具是否存在
        Tool tool = toolRepository.findById(toolId)
                .orElseThrow(() -> new ResourceNotFoundException("工具不存在: " + toolId));

        // 检查是否已收藏
        if (userFavoriteRepository.existsByUserIdAndToolId(userId, toolId)) {
            log.info("用户 {} 已收藏工具 {}，无需重复收藏", userId, toolId);
            return true;
        }

        // 创建收藏记录
        UserFavorite favorite = UserFavorite.builder()
                .userId(userId)
                .toolId(toolId)
                .build();

        userFavoriteRepository.save(favorite);

        // 更新工具的收藏数
        tool.setFavoriteCount((int) userFavoriteRepository.countByToolId(toolId));
        toolRepository.save(tool);

        // 记录到访问日志（用于排行榜）
        rankingService.recordAccess(toolId, userId, ToolAccessLog.ActionType.FAVORITE, null);

        log.info("用户 {} 收藏工具 {} 成功", userId, toolId);
        return true;
    }

    @Override
    @Transactional
    public boolean removeFavorite(Long userId, Long toolId) {
        // 检查是否已收藏
        if (!userFavoriteRepository.existsByUserIdAndToolId(userId, toolId)) {
            log.info("用户 {} 未收藏工具 {}，无需取消", userId, toolId);
            return true;
        }

        // 删除收藏记录
        userFavoriteRepository.deleteByUserIdAndToolId(userId, toolId);

        // 更新工具的收藏数
        Tool tool = toolRepository.findById(toolId)
                .orElseThrow(() -> new ResourceNotFoundException("工具不存在: " + toolId));
        tool.setFavoriteCount((int) userFavoriteRepository.countByToolId(toolId));
        toolRepository.save(tool);

        log.info("用户 {} 取消收藏工具 {} 成功", userId, toolId);
        return true;
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isFavorite(Long userId, Long toolId) {
        return userFavoriteRepository.existsByUserIdAndToolId(userId, toolId);
    }

    @Override
    @Transactional(readOnly = true)
    public long getFavoriteCount(Long toolId) {
        return userFavoriteRepository.countByToolId(toolId);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Tool> getUserFavorites(Long userId, Pageable pageable) {
        Page<UserFavorite> favoritePage = userFavoriteRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable);

        List<Long> toolIds = favoritePage.getContent().stream()
                .map(UserFavorite::getToolId)
                .collect(Collectors.toList());

        List<Tool> tools = toolRepository.findAllById(toolIds);

        // 按照收藏时间排序
        Map<Long, Tool> toolMap = tools.stream()
                .collect(Collectors.toMap(Tool::getId, t -> t));

        List<Tool> sortedTools = toolIds.stream()
                .map(toolMap::get)
                .filter(tool -> tool != null)
                .collect(Collectors.toList());

        return new PageImpl<>(sortedTools, pageable, favoritePage.getTotalElements());
    }

    @Override
    @Transactional(readOnly = true)
    public long getUserFavoriteCount(Long userId) {
        return userFavoriteRepository.countByUserId(userId);
    }

    @Override
    @Transactional(readOnly = true)
    public Map<Long, Boolean> batchCheckFavorite(Long userId, List<Long> toolIds) {
        if (userId == null || toolIds == null || toolIds.isEmpty()) {
            return new HashMap<>();
        }

        List<Long> favoriteToolIds = userFavoriteRepository.findToolIdsByUserIdAndToolIds(userId, toolIds);

        Map<Long, Boolean> result = new HashMap<>();
        for (Long toolId : toolIds) {
            result.put(toolId, favoriteToolIds.contains(toolId));
        }

        return result;
    }

    @Override
    @Transactional
    public boolean toggleFavorite(Long userId, Long toolId) {
        if (isFavorite(userId, toolId)) {
            removeFavorite(userId, toolId);
            return false;
        } else {
            addFavorite(userId, toolId);
            return true;
        }
    }
}
