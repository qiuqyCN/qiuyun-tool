package dev.qiuyun.qiuyuntoolbackend.repository;

import dev.qiuyun.qiuyuntoolbackend.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 分类Repository
 */
@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {

    /**
     * 根据code查找分类
     */
    Optional<Category> findByCode(String code);

    /**
     * 查询所有启用的分类，按排序号排序
     */
    List<Category> findByIsActiveTrueOrderBySortOrderAsc();
}
