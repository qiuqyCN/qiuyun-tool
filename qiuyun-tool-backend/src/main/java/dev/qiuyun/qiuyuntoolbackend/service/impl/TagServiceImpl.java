package dev.qiuyun.qiuyuntoolbackend.service.impl;

import dev.qiuyun.qiuyuntoolbackend.payload.response.TagResponse;
import dev.qiuyun.qiuyuntoolbackend.payload.response.ToolResponse;
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
                .map(TagResponse::from)
                .collect(Collectors.toList());
    }

    @Override
    public List<TagResponse> getHotTags() {
        // 标签不再使用 isHot 字段，热门状态由工具的 isHot 字段控制
        // 返回空列表或根据工具热度计算热门标签
        return List.of();
    }

    @Override
    public TagResponse getTagByName(String name) {
        return tagRepository.findByName(name)
                .map(TagResponse::from)
                .orElse(null);
    }

    @Override
    public TagResponse getTagById(Long id) {
        return tagRepository.findById(id)
                .map(TagResponse::from)
                .orElse(null);
    }

    @Override
    public List<ToolResponse> getToolsByTagName(String tagName) {
        return toolRepository.findByTagName(tagName)
                .stream()
                .map(ToolResponse::simpleFrom)
                .collect(Collectors.toList());
    }
}
