package dev.qiuyun.qiuyuntoolbackend.repository;

import dev.qiuyun.qiuyuntoolbackend.entity.UserFavorite;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * 用户收藏Repository
 */
@Repository
public interface UserFavoriteRepository extends JpaRepository<UserFavorite, Long> {

    /**
     * 根据用户ID和工具ID查询收藏记录
     */
    Optional<UserFavorite> findByUserIdAndToolId(Long userId, Long toolId);

    /**
     * 检查用户是否已收藏该工具
     */
    boolean existsByUserIdAndToolId(Long userId, Long toolId);

    /**
     * 删除用户的收藏记录
     */
    void deleteByUserIdAndToolId(Long userId, Long toolId);

    /**
     * 统计工具的收藏数
     */
    long countByToolId(Long toolId);

    /**
     * 查询用户的收藏列表
     */
    Page<UserFavorite> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);

    /**
     * 统计用户的收藏数量
     */
    long countByUserId(Long userId);

    /**
     * 批量查询用户是否收藏了多个工具
     */
    @Query("SELECT uf.toolId FROM UserFavorite uf WHERE uf.userId = :userId AND uf.toolId IN :toolIds")
    java.util.List<Long> findToolIdsByUserIdAndToolIds(@Param("userId") Long userId, @Param("toolIds") java.util.List<Long> toolIds);
}
