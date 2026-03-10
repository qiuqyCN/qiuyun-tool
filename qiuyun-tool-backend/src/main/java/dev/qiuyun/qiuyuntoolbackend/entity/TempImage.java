package dev.qiuyun.qiuyuntoolbackend.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * 临时图片表
 * 用于存储用户上传但未关联到评论的图片
 */
@Entity
@Table(name = "temp_images", indexes = {
        // 唯一索引：根据图片URL查询/更新
        // 覆盖场景：findByImageUrl / unlinkImage - WHERE image_url=?
        @Index(name = "idx_image_url", columnList = "image_url", unique = true),
        // 复合索引：定时清理未关联的旧图片
        // 覆盖场景：findUnlinkedImagesBefore / deleteUnlinkedImagesBefore - WHERE is_linked=false AND created_at<?
        @Index(name = "idx_cleanup", columnList = "is_linked, created_at")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TempImage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 图片URL
     */
    @Column(name = "image_url", nullable = false, length = 500)
    private String imageUrl;

    /**
     * 上传用户ID
     */
    @Column(name = "user_id", nullable = false)
    private Long userId;

    /**
     * 是否已关联到评论
     */
    @Column(name = "is_linked", nullable = false)
    @Builder.Default
    private Boolean isLinked = false;

    /**
     * 创建时间
     */
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * 关联时间
     */
    @Column(name = "linked_at")
    private LocalDateTime linkedAt;
}
