package dev.qiuyun.qiuyuntoolbackend.service.impl;

import dev.qiuyun.qiuyuntoolbackend.payload.response.TagResponse;
import dev.qiuyun.qiuyuntoolbackend.payload.response.ToolResponse;
import dev.qiuyun.qiuyuntoolbackend.entity.Tag;
import dev.qiuyun.qiuyuntoolbackend.entity.Tool;
import dev.qiuyun.qiuyuntoolbackend.repository.TagRepository;
import dev.qiuyun.qiuyuntoolbackend.repository.ToolRepository;
import dev.qiuyun.qiuyuntoolbackend.service.TagService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 标签服务实现类
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TagServiceImpl implements TagService {

    private final TagRepository tagRepository;
    private final ToolRepository toolRepository;

    @Override
    public List<TagResponse> getAllTags() {
        return tagRepository.findAll()
                .stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<TagResponse> getHotTags() {
        return tagRepository.findByIsHotTrue()
                .stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public TagResponse getTagByName(String name) {
        return tagRepository.findByName(name)
                .map(this::convertToResponse)
                .orElse(null);
    }

    @Override
    public TagResponse getTagById(Long id) {
        return tagRepository.findById(id)
                .map(this::convertToResponse)
                .orElse(null);
    }

    @Override
    public List<ToolResponse> getToolsByTagName(String tagName) {
        return toolRepository.findByTagName(tagName)
                .stream()
                .map(this::convertToToolResponse)
                .collect(Collectors.toList());
    }

    /**
     * 转换Tag实体为响应对象
     */
    private TagResponse convertToResponse(Tag tag) {
        return TagResponse.builder()
                .id(tag.getId())
                .name(tag.getName())
                .description(tag.getDescription())
                .isHot(tag.getIsHot())
                .build();
    }

    /**
     * 转换Tool实体为响应对象
     */
    private ToolResponse convertToToolResponse(Tool tool) {
        return ToolResponse.builder()
                .id(tool.getId())
                .code(tool.getCode())
                .name(tool.getName())
                .description(tool.getDescription())
                .category(tool.getCategory() != null ? tool.getCategory().getCode() : null)
                .icon(tool.getIcon())
                .isVip(tool.getIsVip())
                .visits(tool.getVisitsCount())
                .rating(tool.getRating())
                .reviewCount(tool.getReviewCount())
                .tags(tool.getTags().stream()
                        .map(Tag::getName)
                        .collect(Collectors.toList()))
                .build();
    }
}
