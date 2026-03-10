package dev.qiuyun.qiuyuntoolbackend.repository;

import dev.qiuyun.qiuyuntoolbackend.entity.TempImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 临时图片数据访问层
 */
@Repository
public interface TempImageRepository extends JpaRepository<TempImage, Long> {

    /**
     * 根据图片URL查询
     */
    Optional<TempImage> findByImageUrl(String imageUrl);

    /**
     * 查询指定时间之前未关联的图片
     */
    @Query("SELECT t FROM TempImage t WHERE t.isLinked = false AND t.createdAt < :beforeTime")
    List<TempImage> findUnlinkedImagesBefore(@Param("beforeTime") LocalDateTime beforeTime);

    /**
     * 标记图片为已关联
     */
    @Modifying
    @Query("UPDATE TempImage t SET t.isLinked = true, t.linkedAt = CURRENT_TIMESTAMP WHERE t.imageUrl IN :imageUrls")
    int markImagesAsLinked(@Param("imageUrls") List<String> imageUrls);

    /**
     * 移除图片的关联关系（编辑时删除图片调用）
     */
    @Modifying
    @Query("UPDATE TempImage t SET t.isLinked = false, t.linkedAt = null WHERE t.imageUrl = :imageUrl")
    int unlinkImage(@Param("imageUrl") String imageUrl);

    /**
     * 删除指定时间之前未关联的图片记录
     */
    @Modifying
    @Query("DELETE FROM TempImage t WHERE t.isLinked = false AND t.createdAt < :beforeTime")
    int deleteUnlinkedImagesBefore(@Param("beforeTime") LocalDateTime beforeTime);
}
