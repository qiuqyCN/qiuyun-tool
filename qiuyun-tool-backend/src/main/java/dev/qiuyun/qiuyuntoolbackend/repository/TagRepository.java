package dev.qiuyun.qiuyuntoolbackend.repository;

import dev.qiuyun.qiuyuntoolbackend.entity.Tag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 标签Repository
 */
@Repository
public interface TagRepository extends JpaRepository<Tag, Long> {

    /**
     * 根据名称查找标签
     */
    Optional<Tag> findByName(String name);

    /**
     * 查找热门标签
     */
    List<Tag> findByIsHotTrue();

    /**
     * 根据名称列表查找标签
     */
    List<Tag> findByNameIn(List<String> names);
}
