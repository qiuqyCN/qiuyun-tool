package dev.qiuyun.qiuyuntoolbackend.repository;

import dev.qiuyun.qiuyuntoolbackend.entity.Tool;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 工具Repository
 */
@Repository
public interface ToolRepository extends JpaRepository<Tool, Long> {

    /**
     * 根据code查找工具
     */
    Optional<Tool> findByCode(String code);

    /**
     * 根据分类ID查询启用的工具
     */
    List<Tool> findByCategoryIdAndIsActiveTrue(Long categoryId);

    /**
     * 根据分类ID查询启用的工具（分页）
     */
    Page<Tool> findByCategoryIdAndIsActiveTrue(Long categoryId, Pageable pageable);

    /**
     * 查询所有启用的工具
     */
    List<Tool> findByIsActiveTrue();

    /**
     * 查询所有启用的工具（分页）
     */
    Page<Tool> findByIsActiveTrue(Pageable pageable);

    /**
     * 根据标签ID查询工具
     */
    @Query("SELECT t FROM Tool t JOIN t.tags tag WHERE t.isActive = true AND tag.id = :tagId ORDER BY t.visitsCount DESC")
    List<Tool> findByTagId(@Param("tagId") Long tagId);

    /**
     * 根据标签名称查询工具
     */
    @Query("SELECT t FROM Tool t JOIN t.tags tag WHERE t.isActive = true AND tag.name = :tagName ORDER BY t.visitsCount DESC")
    List<Tool> findByTagName(@Param("tagName") String tagName);

    /**
     * 搜索工具（根据名称或描述）
     */
    @Query("SELECT t FROM Tool t WHERE t.isActive = true AND " +
           "(LOWER(t.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(t.description) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
           "ORDER BY t.visitsCount DESC")
    List<Tool> searchByKeyword(@Param("keyword") String keyword);

    /**
     * 搜索工具（分页）
     */
    @Query("SELECT t FROM Tool t WHERE t.isActive = true AND " +
           "(LOWER(t.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(t.description) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
           "ORDER BY t.visitsCount DESC")
    Page<Tool> searchByKeyword(@Param("keyword") String keyword, Pageable pageable);

    /**
     * 查询热门工具（按访问量排序）
     */
    List<Tool> findTop8ByIsActiveTrueOrderByVisitsCountDesc();

    /**
     * 查询最新工具（按创建时间排序）
     */
    List<Tool> findTop8ByIsActiveTrueOrderByCreatedAtDesc();

    /**
     * 根据分类code查询启用的工具
     */
    @Query("SELECT t FROM Tool t WHERE t.isActive = true AND t.category.code = :categoryCode ORDER BY t.visitsCount DESC")
    List<Tool> findByCategoryCodeAndIsActiveTrue(@Param("categoryCode") String categoryCode);

    /**
     * 统计分类下的工具数量
     */
    @Query("SELECT COUNT(t) FROM Tool t WHERE t.isActive = true AND t.category.code = :categoryCode")
    Integer countByCategoryCodeAndIsActiveTrue(@Param("categoryCode") String categoryCode);
}
