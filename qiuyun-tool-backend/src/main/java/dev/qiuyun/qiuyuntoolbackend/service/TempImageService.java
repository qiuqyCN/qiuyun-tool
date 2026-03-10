package dev.qiuyun.qiuyuntoolbackend.service;

import dev.qiuyun.qiuyuntoolbackend.entity.TempImage;
import dev.qiuyun.qiuyuntoolbackend.repository.TempImageRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 临时图片服务
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class TempImageService {

    private final TempImageRepository tempImageRepository;
    private final FileStorageService fileStorageService;

    /**
     * 保存临时图片记录
     */
    @Transactional
    public void saveTempImage(String imageUrl, Long userId) {
        // 如果已存在则更新，否则创建新记录
        TempImage tempImage = tempImageRepository.findByImageUrl(imageUrl)
                .orElse(TempImage.builder()
                        .imageUrl(imageUrl)
                        .userId(userId)
                        .isLinked(false)
                        .build());
        
        // 重置为未关联状态（防止重复上传同一张图片）
        tempImage.setIsLinked(false);
        tempImage.setLinkedAt(null);
        
        tempImageRepository.save(tempImage);
        log.debug("保存临时图片记录: {}", imageUrl);
    }

    /**
     * 标记图片为已关联（评论提交时调用）
     */
    @Transactional
    public void markImagesAsLinked(List<String> imageUrls) {
        if (imageUrls == null || imageUrls.isEmpty()) {
            return;
        }
        int count = tempImageRepository.markImagesAsLinked(imageUrls);
        log.debug("标记 {} 张图片为已关联", count);
    }

    /**
     * 移除图片关联关系（编辑时删除图片调用）
     */
    @Transactional
    public void unlinkImage(String imageUrl) {
        if (imageUrl == null || imageUrl.isEmpty()) {
            return;
        }
        int count = tempImageRepository.unlinkImage(imageUrl);
        if (count > 0) {
            log.debug("移除图片关联关系: {}", imageUrl);
        }
    }

    /**
     * 定时清理过期的未关联图片
     * 每30分钟执行一次，清理2小时前上传的未关联图片
     */
    @Scheduled(fixedRate = 30 * 60 * 1000) // 30分钟
    @Transactional
    public void cleanupUnlinkedImages() {
        LocalDateTime beforeTime = LocalDateTime.now().minusHours(2);
        List<TempImage> unlinkedImages = tempImageRepository.findUnlinkedImagesBefore(beforeTime);

        if (unlinkedImages.isEmpty()) {
            return;
        }

        log.info("发现 {} 张过期的未关联图片，准备清理", unlinkedImages.size());

        for (TempImage image : unlinkedImages) {
            try {
                // 删除物理文件
                fileStorageService.deleteImage(image.getImageUrl());
                log.debug("删除过期未关联图片: {}", image.getImageUrl());
            } catch (Exception e) {
                log.warn("删除图片文件失败: {}, 原因: {}", image.getImageUrl(), e.getMessage());
            }
        }

        // 删除数据库记录
        int deletedCount = tempImageRepository.deleteUnlinkedImagesBefore(beforeTime);
        log.info("已清理 {} 张过期的未关联图片", deletedCount);
    }
}
