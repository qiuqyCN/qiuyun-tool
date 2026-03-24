package dev.qiuyun.qiuyuntoolbackend.controller;

import dev.qiuyun.qiuyuntoolbackend.payload.response.ApiResponse;
import dev.qiuyun.qiuyuntoolbackend.security.UserDetailsImpl;
import dev.qiuyun.qiuyuntoolbackend.service.FileStorageService;
import dev.qiuyun.qiuyuntoolbackend.service.TempImageService;
import dev.qiuyun.qiuyuntoolbackend.util.FileTypeValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

/**
 * 文件上传控制器
 */
@RestController
@RequestMapping("/api/upload")
@RequiredArgsConstructor
public class UploadController {

    private final FileStorageService fileStorageService;
    private final TempImageService tempImageService;

    /**
     * 上传评论图片（临时上传）
     *
     * @param file 图片文件
     * @param user 当前用户
     */
    @PostMapping(value = "/image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<Map<String, String>> uploadImage(
            @RequestPart("file") MultipartFile file,
            @AuthenticationPrincipal UserDetailsImpl user) {

        // 验证文件类型（基于文件头魔数）
        if (!FileTypeValidator.isValidImage(file)) {
            return ApiResponse.error(400, "只能上传有效的图片文件（JPEG、PNG、GIF、WebP、BMP）");
        }

        // 验证文件类型是否与声明的一致
        String contentType = file.getContentType();
        if (contentType == null || !FileTypeValidator.isContentTypeMatch(file, contentType)) {
            return ApiResponse.error(400, "文件类型与声明的不一致");
        }

        // 验证文件大小（最大5MB）
        if (file.getSize() > 5 * 1024 * 1024) {
            return ApiResponse.error(400, "图片大小不能超过5MB");
        }

        // 存储图片
        String fileName = fileStorageService.storeImage(file, user.getId());
        String fileUrl = fileStorageService.getImageUrl(fileName);

        // 保存临时图片记录（未关联状态）
        tempImageService.saveTempImage(fileUrl, user.getId());

        return ApiResponse.success(Map.of("url", fileUrl));
    }
}
