package dev.qiuyun.qiuyuntoolbackend.controller;

import dev.qiuyun.qiuyuntoolbackend.payload.response.ApiResponse;
import dev.qiuyun.qiuyuntoolbackend.security.UserDetailsImpl;
import dev.qiuyun.qiuyuntoolbackend.service.FileStorageService;
import dev.qiuyun.qiuyuntoolbackend.service.TempImageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
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
    public ApiResponse<Map<String, String>> uploadImage(
            @RequestPart("file") MultipartFile file,
            @AuthenticationPrincipal UserDetailsImpl user) {

        if (user == null) {
            return ApiResponse.error(401, "请先登录");
        }

        // 验证文件类型
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            return ApiResponse.error(400, "只能上传图片文件");
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
