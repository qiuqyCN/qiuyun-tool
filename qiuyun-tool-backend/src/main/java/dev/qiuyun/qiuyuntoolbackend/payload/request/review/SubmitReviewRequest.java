package dev.qiuyun.qiuyuntoolbackend.payload.request.review;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

/**
 * 提交评论请求
 */
@Data
public class SubmitReviewRequest {

    @NotNull(message = "工具ID不能为空")
    private Long toolId;

    @Min(value = 1, message = "评分最小为1")
    @Max(value = 5, message = "评分最大为5")
    private Integer rating;  // 主评论时必填，回复时可为空

    @Size(max = 500, message = "评论内容最多500字")
    private String content;

    private Long parentId;  // 回复时使用

    private List<String> imageUrls;  // 图片URL列表
}
