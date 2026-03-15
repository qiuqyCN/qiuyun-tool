 package dev.qiuyun.qiuyuntoolbackend.config;

import dev.qiuyun.qiuyuntoolbackend.entity.Category;
import dev.qiuyun.qiuyuntoolbackend.entity.Tag;
import dev.qiuyun.qiuyuntoolbackend.entity.Tool;
import dev.qiuyun.qiuyuntoolbackend.entity.User;
import dev.qiuyun.qiuyuntoolbackend.enums.UserStatus;
import dev.qiuyun.qiuyuntoolbackend.repository.CategoryRepository;
import dev.qiuyun.qiuyuntoolbackend.repository.TagRepository;
import dev.qiuyun.qiuyuntoolbackend.repository.ToolRepository;
import dev.qiuyun.qiuyuntoolbackend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 数据初始化器
 * 应用启动时初始化分类、标签和工具数据
 * 支持增量更新：新增工具会自动添加，已有工具会更新配置
 *
 * ==================== 工具色系规范 ====================
 * 每个分类有固定的色系，保持视觉一致性：
 *
 * 开发工具 (dev) - 蓝色系
 *   主色: #2563EB (亮蓝), #3B82F6 (中蓝), #1D4ED8 (深蓝)
 *   背景: #DBEAFE (浅蓝), #EFF6FF (极浅蓝), #BFDBFE (淡蓝)
 *   图标: Braces, ArrowRightLeft, Code2, Search, Clock, Binary, Coffee, Wifi
 *   使用建议: 开发类工具使用蓝色系，传达专业、技术感
 *
 * 前端工具 (frontend) - 靛蓝色系
 *   主色: #6366F1 (靛蓝), #818CF8 (中靛蓝), #4F46E5 (深靛蓝)
 *   背景: #E0E7FF (浅靛蓝), #EEF2FF (极浅靛蓝), #C7D2FE (淡靛蓝)
 *   图标: Layout, Palette, Component, Layers
 *   使用建议: 前端开发类工具使用靛蓝色系，传达现代、创意感
 *
 * 图片工具 (image) - 绿色系
 *   主色: #16A34A (翠绿), #22C55E (亮绿), #15803D (深绿)
 *   背景: #DCFCE7 (浅绿), #F0FDF4 (极浅绿), #BBF7D0 (淡绿)
 *   图标: ImageMinus, ImagePlus, Image, QrCode, ScanLine, Stamp
 *   使用建议: 图片处理类工具使用绿色系，传达自然、清晰感
 *
 * 文档工具 (doc) - 橙色系
 *   主色: #EA580C (亮橙), #F97316 (中橙), #C2410C (深橙)
 *   背景: #FFEDD5 (浅橙), #FFF7ED (极浅橙), #FED7AA (淡橙)
 *   图标: FileText, FileEdit
 *   使用建议: 文档处理类工具使用橙色系，传达活力、创造力
 *
 * 加密工具 (crypto) - 红色系
 *   主色: #DC2626 (亮红), #EF4444 (中红), #B91C1C (深红)
 *   背景: #FEE2E2 (浅红), #FEF2F2 (极浅红), #FECACA (淡红)
 *   图标: Hash, Link, Lock, Key, Binary
 *   使用建议: 安全加密类工具使用红色系，传达警示、重要性
 *
 * 文本工具 (text) - 紫色系
 *   主色: #9333EA (亮紫), #A855F7 (中紫), #7C3AED (深紫)
 *   背景: #F3E8FF (浅紫), #FAF5FF (极浅紫), #E9D5FF (淡紫)
 *   图标: GitCompare, Text, AlignLeft, Type
 *   使用建议: 文本处理类工具使用紫色系，传达优雅、文艺感
 *
 * 数字工具 (number) - 琥珀/黄色系
 *   主色: #D97706 (琥珀), #F59E0B (亮黄), #B45309 (深琥珀)
 *   背景: #FEF3C7 (浅琥珀), #FFFBEB (极浅琥珀), #FDE68A (淡黄)
 *   图标: Binary, Dices, Calculator, Hash
 *   使用建议: 数字计算类工具使用琥珀色系，传达计算、逻辑感
 *
 * 网络工具 (network) - 青色系
 *   主色: #0891B2 (青蓝), #06B6D4 (亮青), #0E7490 (深青)
 *   背景: #CFFAFE (浅青), #ECFEFF (极浅青), #A5F3FC (淡青)
 *   图标: Globe, Wifi, Server, Network, Link
 *   使用建议: 网络诊断类工具使用青色系，传达连接、科技感
 *
 * 媒体工具 (media) - 紫红色系
 *   主色: #C026D3 (紫红), #D946EF (亮紫红), #A21CAF (深紫红)
 *   背景: #FAE8FF (浅紫红), #FDF4FF (极浅紫红), #F5D0FE (淡紫红)
 *   图标: Video, Music, Play, Film, Mic
 *   使用建议: 音视频类工具使用紫红色系，传达现代、创意感
 *
 * 设计工具 (design) - 渐变/彩虹系
 *   主色: #8B5CF6 (紫), #EC4899 (粉), #F59E0B (橙)
 *   背景: #F5F3FF (浅紫), #FDF2F8 (浅粉), #FFFBEB (浅橙)
 *   图标: Palette, PenTool, Sparkles, Frame, Image
 *   使用建议: 设计创意类工具使用渐变色系，传达创意、艺术感
 *
 * 生活工具 (life) - 粉色/玫瑰系
 *   主色: #E11D48 (玫瑰), #F43F5E (亮粉), #BE123C (深玫瑰)
 *   背景: #FFE4E6 (浅粉), #FFF1F2 (极浅粉), #FECDD3 (淡粉)
 *   图标: Heart, Star, Calendar, Clock, Home, Smile
 *   使用建议: 生活实用类工具使用粉色系，传达温馨、亲和力
 *
 * 颜色选择原则:
 *   1. 同一分类内使用相近色系，保持和谐
 *   2. 热门工具使用该分类的主色（第一个颜色）
 *   3. VIP工具可使用更深或更亮的颜色突出显示
 *   4. 背景色使用对应主色的极浅版本（Tailwind 50-100 色阶）
 *   5. 主色建议使用 Tailwind 500-700 色阶
 *   6. 避免使用过于相近的颜色，确保分类辨识度
 *
 * 使用示例:
 *   new ToolDefinition("tool-code", "工具名称", "描述",
 *       category, "IconName", "#2563EB", "#DBEAFE", false, true,
 *       buildInstructions(...), tags)
 * ====================================================
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer {

    private final CategoryRepository categoryRepository;
    private final TagRepository tagRepository;
    private final ToolRepository toolRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Async
    @EventListener(ApplicationReadyEvent.class)
    @Transactional
    public void init() {
        log.info("开始异步数据初始化/同步...");

        try {
            // 1. 初始化分类（增量更新）
            Map<String, Category> categories = initCategories();

            // 2. 初始化标签（增量更新）
            Map<String, Tag> tags = initTags();

            // 3. 初始化/同步工具（增量更新）
            initTools(categories, tags);

            // 4. 初始化用户数据（仅在用户表为空时执行）
            if (userRepository.count() == 0) {
                initUsers();
            }

            log.info("异步数据初始化/同步完成！");
        } catch (Exception e) {
            log.error("数据初始化失败: {}", e.getMessage(), e);
        }
    }

    /**
     * 初始化分类数据（支持增量更新，仅在实际数据变化时更新）
     */
    private Map<String, Category> initCategories() {
        log.info("同步分类数据...");

        // 定义所有分类（使用静态常量避免每次创建）
        final List<CategoryDefinition> CATEGORY_DEFINITIONS = Arrays.asList(
                new CategoryDefinition("dev", "开发工具", "Code", "JSON格式化、代码压缩、正则测试等开发常用工具", 1),
                new CategoryDefinition("frontend", "前端工具", "Layout", "CSS格式化、颜色选择器、布局生成器等前端开发工具", 2),
                new CategoryDefinition("image", "图片工具", "Image", "图片压缩、格式转换、Base64编码、二维码生成等图片处理工具", 3),
                new CategoryDefinition("doc", "文档转换", "FileText", "PDF转换、Word转换、Markdown编辑等文档工具", 4),
                new CategoryDefinition("crypto", "加密工具", "Lock", "MD5加密、Base64、URL编码等加密解密工具", 5),
                new CategoryDefinition("text", "文本工具", "Type", "文本对比、字数统计、大小写转换等文本处理工具", 6),
                new CategoryDefinition("number", "数字工具", "Calculator", "进制转换、单位换算、随机数生成等数字工具", 7),
                new CategoryDefinition("network", "网络工具", "Globe", "IP查询、端口扫描、DNS查询等网络诊断工具", 8),
                new CategoryDefinition("media", "视频/音频工具", "Video", "视频格式转换、音频剪辑、文字转语音等多媒体工具", 9),
                new CategoryDefinition("design", "设计工具", "Palette", "Logo生成器、流程图、海报设计等创意设计工具", 10),
                new CategoryDefinition("life", "生活工具", "Heart", "日历、天气、单位换算、日常计算等生活实用工具", 11)
        );

        // 获取现有分类
        List<Category> existingCategories = categoryRepository.findAll();
        Map<String, Category> existingMap = existingCategories.stream()
                .collect(Collectors.toMap(Category::getCode, c -> c));

        // 快速路径：如果分类数量和代码都匹配，检查是否有变化
        if (existingCategories.size() == CATEGORY_DEFINITIONS.size()) {
            boolean allMatch = CATEGORY_DEFINITIONS.stream()
                    .allMatch(def -> existingMap.containsKey(def.code));
            if (allMatch) {
                boolean anyChanged = false;
                for (CategoryDefinition def : CATEGORY_DEFINITIONS) {
                    Category existing = existingMap.get(def.code);
                    if (isCategoryChanged(existing, def)) {
                        anyChanged = true;
                        break;
                    }
                }
                if (!anyChanged) {
                    log.info("分类数据未变化，跳过同步");
                    return existingMap;
                }
            }
        }

        List<Category> toSave = new ArrayList<>();
        int newCount = 0;
        int updateCount = 0;
        int unchangedCount = 0;

        for (CategoryDefinition def : CATEGORY_DEFINITIONS) {
            Category category = existingMap.get(def.code);
            if (category == null) {
                // 新增分类
                category = Category.builder()
                        .code(def.code)
                        .name(def.name)
                        .icon(def.icon)
                        .description(def.description)
                        .sortOrder(def.sortOrder)
                        .isActive(true)
                        .build();
                log.info("新增分类: {}", def.name);
                newCount++;
                toSave.add(category);
            } else if (isCategoryChanged(category, def)) {
                // 分类配置发生变化，才进行更新
                category.setName(def.name);
                category.setIcon(def.icon);
                category.setDescription(def.description);
                category.setSortOrder(def.sortOrder);
                log.info("更新分类: {}", def.name);
                updateCount++;
                toSave.add(category);
            } else {
                // 分类未发生变化
                unchangedCount++;
                log.debug("分类未变化，跳过: {}", def.name);
            }
        }

        // 只在有需要保存的数据时才执行保存操作
        List<Category> saved;
        if (!toSave.isEmpty()) {
            saved = categoryRepository.saveAll(toSave);
        } else {
            saved = existingCategories;
        }

        log.info("分类同步完成: 新增 {} 个, 更新 {} 个, 未变化 {} 个", newCount, updateCount, unchangedCount);
        return saved.stream().collect(Collectors.toMap(Category::getCode, c -> c));
    }

    /**
     * 比较分类定义与现有分类是否发生变化
     */
    private boolean isCategoryChanged(Category existingCategory, CategoryDefinition def) {
        if (!Objects.equals(existingCategory.getName(), def.name)) return true;
        if (!Objects.equals(existingCategory.getIcon(), def.icon)) return true;
        if (!Objects.equals(existingCategory.getDescription(), def.description)) return true;
        if (existingCategory.getSortOrder() != def.sortOrder) return true;
        return false;
    }

    /**
     * 初始化标签数据（支持增量更新，仅在实际数据变化时更新）
     */
    private Map<String, Tag> initTags() {
        log.info("同步标签数据...");

        // 定义所有标签（使用静态常量避免每次创建）
        final List<TagDefinition> TAG_DEFINITIONS = Arrays.asList(
                new TagDefinition("热门", "热门工具", true),
                new TagDefinition("常用", "常用工具", true),
                new TagDefinition("VIP", "VIP专属工具", false),
                new TagDefinition("开发", "开发相关", false),
                new TagDefinition("文档", "文档处理", false),
                new TagDefinition("图片", "图片处理", false),
                new TagDefinition("媒体", "音视频处理", false),
                new TagDefinition("生活", "生活实用", false),
                new TagDefinition("加密", "加密解密", false),
                new TagDefinition("文本", "文本处理", false),
                new TagDefinition("转换", "格式转换", false),
                new TagDefinition("生成", "内容生成", false),
                new TagDefinition("解析", "内容解析", false),
                new TagDefinition("计算", "数学计算", false),
                new TagDefinition("测试", "测试调试", false),
                new TagDefinition("网络", "网络相关", false),
                new TagDefinition("前端", "前端开发", false),
                new TagDefinition("后端", "后端开发", false),
                new TagDefinition("安全", "安全相关", false),
                new TagDefinition("效率", "效率工具", false),
                new TagDefinition("设计", "设计相关", false),
                new TagDefinition("财务", "财务计算", false),
                new TagDefinition("健康", "健康相关", false),
                new TagDefinition("时间", "日期时间", false)
        );

        // 获取现有标签
        List<Tag> existingTags = tagRepository.findAll();
        Map<String, Tag> existingMap = existingTags.stream()
                .collect(Collectors.toMap(Tag::getName, t -> t));

        // 快速路径：如果标签数量和名称都匹配，直接返回
        if (existingTags.size() == TAG_DEFINITIONS.size()) {
            boolean allMatch = TAG_DEFINITIONS.stream()
                    .allMatch(def -> existingMap.containsKey(def.name));
            if (allMatch) {
                // 检查是否有变化
                boolean anyChanged = false;
                for (TagDefinition def : TAG_DEFINITIONS) {
                    Tag existing = existingMap.get(def.name);
                    if (!Objects.equals(existing.getDescription(), def.description) 
                            || existing.getIsHot() != def.isHot) {
                        anyChanged = true;
                        break;
                    }
                }
                if (!anyChanged) {
                    log.info("标签数据未变化，跳过同步");
                    return existingMap;
                }
            }
        }

        List<Tag> toSave = new ArrayList<>();
        int newCount = 0;
        int updateCount = 0;
        int unchangedCount = 0;

        for (TagDefinition def : TAG_DEFINITIONS) {
            Tag tag = existingMap.get(def.name);
            if (tag == null) {
                // 新增标签
                tag = Tag.builder()
                        .name(def.name)
                        .description(def.description)
                        .isHot(def.isHot)
                        .build();
                log.info("新增标签: {}", def.name);
                newCount++;
                toSave.add(tag);
            } else if (isTagChanged(tag, def)) {
                // 标签配置发生变化，才进行更新
                tag.setDescription(def.description);
                tag.setIsHot(def.isHot);
                log.info("更新标签: {}", def.name);
                updateCount++;
                toSave.add(tag);
            } else {
                // 标签未发生变化
                unchangedCount++;
                log.debug("标签未变化，跳过: {}", def.name);
            }
        }

        // 只在有需要保存的数据时才执行保存操作
        List<Tag> saved;
        if (!toSave.isEmpty()) {
            saved = tagRepository.saveAll(toSave);
        } else {
            saved = existingTags;
        }

        log.info("标签同步完成: 新增 {} 个, 更新 {} 个, 未变化 {} 个", newCount, updateCount, unchangedCount);
        return saved.stream().collect(Collectors.toMap(Tag::getName, t -> t));
    }

    /**
     * 比较标签定义与现有标签是否发生变化
     */
    private boolean isTagChanged(Tag existingTag, TagDefinition def) {
        if (!Objects.equals(existingTag.getDescription(), def.description)) return true;
        if (existingTag.getIsHot() != def.isHot) return true;
        return false;
    }

    /**
     * 初始化/同步工具数据（支持增量更新，仅在实际数据变化时更新）
     */
    private void initTools(Map<String, Category> categories, Map<String, Tag> tags) {
        log.info("同步工具数据...");

        // 获取现有工具（只获取必要的字段用于比较）
        List<Tool> existingTools = toolRepository.findAll();
        Map<String, Tool> existingMap = existingTools.stream()
                .collect(Collectors.toMap(Tool::getCode, t -> t));

        // 定义所有工具
        List<ToolDefinition> toolDefs = buildToolDefinitions(categories, tags);

        List<Tool> toSave = new ArrayList<>();
        int newCount = 0;
        int updateCount = 0;
        int unchangedCount = 0;

        // 预计算工具定义的标签名称集合，避免重复计算
        Map<String, Set<String>> defTagNamesCache = new HashMap<>();
        for (ToolDefinition def : toolDefs) {
            Set<String> tagNames = def.tags != null
                    ? def.tags.stream().map(Tag::getName).collect(Collectors.toSet())
                    : Collections.emptySet();
            defTagNamesCache.put(def.code, tagNames);
        }

        for (ToolDefinition def : toolDefs) {
            Tool tool = existingMap.get(def.code);
            if (tool == null) {
                // 新增工具
                tool = Tool.builder()
                        .code(def.code)
                        .name(def.name)
                        .description(def.description)
                        .category(def.category)
                        .icon(def.icon)
                        .iconColor(def.iconColor)
                        .iconBgColor(def.iconBgColor)
                        .isVip(def.isVip)
                        .isActive(def.isActive)
                        .visitsCount(0L)
                        .viewCount(0L)
                        .usageCount(0L)
                        .rating(BigDecimal.valueOf(0.0))
                        .reviewCount(0)
                        .favoriteCount(0)
                        .instructions(def.instructions)
                        .tags(def.tags)
                        .build();
                log.info("新增工具: {}", def.name);
                newCount++;
                toSave.add(tool);
            } else if (isToolChangedFast(tool, def, defTagNamesCache.get(def.code))) {
                // 工具配置发生变化，才进行更新
                tool.setName(def.name);
                tool.setDescription(def.description);
                tool.setCategory(def.category);
                tool.setIcon(def.icon);
                tool.setIconColor(def.iconColor);
                tool.setIconBgColor(def.iconBgColor);
                tool.setIsVip(def.isVip);
                tool.setIsActive(def.isActive);
                tool.setInstructions(def.instructions);
                tool.setTags(def.tags);
                log.info("更新工具: {}", def.name);
                updateCount++;
                toSave.add(tool);
            } else {
                // 工具未发生变化
                unchangedCount++;
                log.debug("工具未变化，跳过: {}", def.name);
            }
        }

        // 只在有需要保存的数据时才执行保存操作
        if (!toSave.isEmpty()) {
            toolRepository.saveAll(toSave);
        }

        log.info("工具同步完成: 新增 {} 个, 更新 {} 个, 未变化 {} 个", newCount, updateCount, unchangedCount);
    }

    /**
     * 比较工具定义与现有工具是否发生变化（快速版本，使用预计算的标签集合）
     */
    private boolean isToolChangedFast(Tool existingTool, ToolDefinition def, Set<String> defTagNames) {
        // 比较基本字段（按变化概率排序，先检查最可能变化的）
        if (!Objects.equals(existingTool.getName(), def.name)) return true;
        if (!Objects.equals(existingTool.getDescription(), def.description)) return true;
        if (!Objects.equals(existingTool.getInstructions(), def.instructions)) return true;
        if (!Objects.equals(existingTool.getIcon(), def.icon)) return true;
        if (!Objects.equals(existingTool.getIconColor(), def.iconColor)) return true;
        if (!Objects.equals(existingTool.getIconBgColor(), def.iconBgColor)) return true;
        if (existingTool.getIsVip() != def.isVip) return true;
        if (existingTool.getIsActive() != def.isActive) return true;

        // 比较分类
        if (existingTool.getCategory() == null || def.category == null) {
            if (existingTool.getCategory() != def.category) return true;
        } else if (!Objects.equals(existingTool.getCategory().getId(), def.category.getId())) {
            return true;
        }

        // 比较标签（使用预计算的defTagNames，避免重复创建集合）
        Set<Tag> existingTags = existingTool.getTags();
        if (existingTags == null || existingTags.isEmpty()) {
            return !defTagNames.isEmpty();
        }
        if (existingTags.size() != defTagNames.size()) return true;
        
        // 快速比较：检查现有标签是否都在定义中
        for (Tag tag : existingTags) {
            if (!defTagNames.contains(tag.getName())) {
                return true;
            }
        }

        return false;
    }

    /**
     * 构建工具定义列表
     */
    private List<ToolDefinition> buildToolDefinitions(Map<String, Category> categories, Map<String, Tag> tags) {
        List<ToolDefinition> defs = new ArrayList<>();

        Category devCategory = categories.get("dev");
        Category frontendCategory = categories.get("frontend");
        Category imageCategory = categories.get("image");
        Category docCategory = categories.get("doc");
        Category cryptoCategory = categories.get("crypto");
        Category textCategory = categories.get("text");
        Category numberCategory = categories.get("number");
        Category networkCategory = categories.get("network");
        Category mediaCategory = categories.get("media");
        Category designCategory = categories.get("design");
        Category lifeCategory = categories.get("life");

        Tag hotTag = tags.get("热门");
        Tag commonTag = tags.get("常用");
        Tag vipTag = tags.get("VIP");
        Tag devTag = tags.get("开发");
        Tag imageTag = tags.get("图片");
        Tag docTag = tags.get("文档");
        Tag mediaTag = tags.get("媒体");
        Tag lifeTag = tags.get("生活");

        // ========== 开发工具 (蓝色系) ==========
        defs.add(new ToolDefinition("json-formatter", "JSON格式化", "JSON数据的格式化、压缩、转义等操作",
                devCategory, "Braces", "#2563EB", "#DBEAFE", false, true,
                buildInstructions("格式化", "将压缩的 JSON 数据转换为易读的格式，自动添加缩进和换行",
                        "压缩", "去除 JSON 中的空白字符，减小数据体积",
                        "转义", "将 JSON 字符串转义，适用于在代码中使用",
                        "去转义", "将转义后的 JSON 字符串还原为正常格式"),
                new HashSet<>(Arrays.asList(hotTag, commonTag))));

        defs.add(new ToolDefinition("yaml-json-converter", "YAML/JSON互转", "YAML与JSON格式互相转换",
                devCategory, "ArrowRightLeft", "#3B82F6", "#EFF6FF", false, true,
                buildInstructions("选择转换方向", "点击顶部标签切换 YAML→JSON 或 JSON→YAML",
                        "YAML转JSON", "将YAML格式的数据转换为JSON格式，便于程序解析",
                        "JSON转YAML", "将JSON格式的数据转换为YAML格式，便于配置文件编写",
                        "切换方向", "点击切换按钮可快速交换输入输出内容并反向转换"),
                new HashSet<>(Arrays.asList(devTag))));

        defs.add(new ToolDefinition("code-beautify", "代码美化", "HTML/CSS/JavaScript/Java/SQL/XML代码格式化",
                devCategory, "Code2", "#1D4ED8", "#BFDBFE", true, true,
                buildInstructions("HTML格式化", "自动缩进和换行，使HTML结构清晰易读",
                        "CSS格式化", "美化CSS样式代码，规范属性格式",
                        "JavaScript格式化", "格式化JS代码，提高可读性",
                        "代码压缩", "支持代码压缩功能，减小文件体积"),
                new HashSet<>(Arrays.asList(vipTag))));

        defs.add(new ToolDefinition("regex-tester", "正则测试", "在线正则表达式测试工具",
                devCategory, "Search", "#2563EB", "#DBEAFE", false, true,
                buildInstructions("输入正则表达式", "在正则输入框中填写要测试的正则表达式",
                        "输入测试文本", "在文本框中输入需要匹配的测试内容",
                        "实时匹配", "系统会实时显示匹配结果和高亮匹配内容"),
                new HashSet<>()));

        defs.add(new ToolDefinition("timestamp-converter", "时间戳转换", "Unix时间戳与日期时间互转",
                devCategory, "Clock", "#3B82F6", "#EFF6FF", false, true,
                buildInstructions("时间戳转日期", "输入Unix时间戳（秒或毫秒），转换为可读的日期时间",
                        "日期转时间戳", "选择或输入日期时间，转换为Unix时间戳",
                        "多种格式", "支持多种日期格式输出"),
                new HashSet<>(Arrays.asList(hotTag))));

        // WebSocket 测试
        defs.add(new ToolDefinition("websocket-tester", "WebSocket测试", "WebSocket连接测试工具，支持实时消息收发和心跳保活",
                devCategory, "Wifi", "#8B5CF6", "#F5F3FF", false, true,
                buildInstructions("输入URL", "输入WebSocket地址（ws://或wss://开头）",
                        "建立连接", "点击连接按钮建立WebSocket连接",
                        "收发消息", "在发送区输入消息，实时查看服务器返回的消息",
                        "高级设置", "支持自动重连、心跳保活等功能"),
                new HashSet<>(Arrays.asList(devTag))));



        defs.add(new ToolDefinition("yaml-properties-converter", "YAML/Properties互转", "YAML格式与Java Properties配置文件双向转换",
                devCategory, "FileCog", "#2563EB", "#DBEAFE", false, true,
                buildInstructions("选择转换方向", "点击顶部标签切换 Properties→YAML 或 YAML→Properties",
                        "输入内容", "在输入框中粘贴需要转换的配置内容",
                        "嵌套结构", "支持嵌套结构转换（点号分隔键 ↔ YAML层级）"),
                new HashSet<>(Arrays.asList(devTag))));

        defs.add(new ToolDefinition("json-java-converter", "JSON/Java互转", "JSON与Java POJO类互相转换",
                devCategory, "Coffee", "#3B82F6", "#EFF6FF", false, true,
                buildInstructions("JSON → Java", "输入JSON数据，生成对应的Java POJO类",
                        "Java → JSON", "输入Java对象的JSON表示，格式化输出",
                        "设置选项", "可设置包名、根类名、是否使用Lombok"),
                new HashSet<>(Arrays.asList(devTag))));

        // HTTP 请求测试
        defs.add(new ToolDefinition("http-request", "HTTP 请求测试", "在线 HTTP 请求测试工具，支持各种方法和自定义请求头",
                devCategory, "Globe", "#2563EB", "#DBEAFE", false, true,
                buildInstructions("选择方法", "选择 HTTP 方法：GET、POST、PUT、DELETE 等",
                        "输入 URL", "填写请求的完整地址",
                        "配置参数", "添加查询参数、请求头、请求体",
                        "发送请求", "点击发送按钮查看响应结果"),
                new HashSet<>(Arrays.asList(hotTag, commonTag, devTag))));

        // User-Agent 解析
        defs.add(new ToolDefinition("user-agent-parser", "User-Agent 解析", "解析 User-Agent 字符串，获取浏览器、操作系统、设备等信息",
                devCategory, "Smartphone", "#3B82F6", "#EFF6FF", false, true,
                buildInstructions("输入 User-Agent", "粘贴需要解析的 User-Agent 字符串",
                        "自动解析", "系统自动识别浏览器、操作系统、设备等信息",
                        "查看详情", "查看完整的解析结果"),
                new HashSet<>(Arrays.asList(devTag))));

        // ========== 图片工具 (绿色系) ==========
        defs.add(new ToolDefinition("image-compress", "图片压缩", "在线图片压缩，支持JPG/PNG/GIF",
                imageCategory, "ImageMinus", "#16A34A", "#DCFCE7", false, true,
                buildInstructions("上传图片", "点击上传或拖拽图片文件到指定区域",
                        "选择压缩质量", "调整压缩比例，平衡画质和文件大小",
                        "预览对比", "查看压缩前后的画质对比"),
                new HashSet<>(Arrays.asList(hotTag, commonTag))));

        defs.add(new ToolDefinition("image-convert", "图片格式转换", "图片格式互相转换",
                imageCategory, "ImagePlus", "#22C55E", "#F0FDF4", true, true,
                buildInstructions("上传图片", "选择需要转换格式的图片文件",
                        "选择目标格式", "选择要转换成的图片格式",
                        "开始转换", "点击转换按钮，等待处理完成"),
                new HashSet<>(Arrays.asList(vipTag, imageTag))));

        defs.add(new ToolDefinition("image-to-base64", "图片转Base64", "图片转换为Base64编码",
                imageCategory, "FileImage", "#15803D", "#BBF7D0", false, true,
                buildInstructions("上传图片", "选择要转换的图片文件",
                        "自动转换", "系统自动将图片转换为Base64编码",
                        "复制结果", "一键复制Base64字符串"),
                new HashSet<>(Arrays.asList(imageTag))));

        defs.add(new ToolDefinition("qr-code-generator", "二维码生成", "生成各种类型的二维码，支持自定义样式和Logo",
                imageCategory, "QrCode", "#16A34A", "#DCFCE7", false, true,
                buildInstructions("选择内容类型", "支持文本、URL、WiFi、邮箱、电话、短信",
                        "输入内容", "根据类型填写相应信息",
                        "自定义样式", "调整尺寸、纠错级别，可添加Logo"),
                new HashSet<>(Arrays.asList(hotTag, imageTag))));

        defs.add(new ToolDefinition("qr-code-parser", "二维码解析", "上传二维码图片，识别其中的内容信息",
                imageCategory, "ScanLine", "#22C55E", "#F0FDF4", false, true,
                buildInstructions("上传图片", "点击或拖拽二维码图片到上传区域",
                        "开始解析", "系统自动识别二维码内容",
                        "查看结果", "显示内容类型和结构化数据"),
                new HashSet<>(Arrays.asList(imageTag))));

        // 图片水印
        defs.add(new ToolDefinition("image-watermark", "图片水印", "为图片添加文字或图片水印，支持自定义位置、透明度、旋转角度",
                imageCategory, "Stamp", "#16A34A", "#DCFCE7", false, true,
                buildInstructions("上传图片", "点击或拖拽上传需要添加水印的图片",
                        "选择水印类型", "选择文字水印或图片水印",
                        "配置水印", "设置文字内容、字体、颜色或上传水印图片",
                        "调整位置", "选择九宫格位置或平铺模式，设置透明度、旋转角度",
                        "添加水印", "点击按钮生成带水印的图片"),
                new HashSet<>(Arrays.asList(imageTag))));

        // SVG编辑器
        defs.add(new ToolDefinition("svg-editor", "SVG编辑器", "在线SVG编辑、优化、格式化和转换PNG工具",
                imageCategory, "PenTool", "#22C55E", "#F0FDF4", false, true,
                buildInstructions("输入SVG", "粘贴SVG代码或上传SVG文件",
                        "选择操作", "格式化、优化、压缩、转PNG、验证",
                        "转PNG设置", "转换为PNG时可设置输出尺寸",
                        "下载结果", "下载处理后的SVG或PNG文件"),
                new HashSet<>(Arrays.asList(hotTag, imageTag))));

        // ========== 文档工具 (橙色系) ==========
        defs.add(new ToolDefinition("markdown-editor", "Markdown编辑器", "在线Markdown编辑和预览",
                docCategory, "FileEdit", "#F97316", "#FFF7ED", false, true,
                buildInstructions("编辑Markdown", "在左侧编辑器中输入Markdown语法",
                        "实时预览", "右侧实时显示渲染后的效果",
                        "工具栏", "使用工具栏快速插入常用格式"),
                new HashSet<>(Arrays.asList(docTag))));

        defs.add(new ToolDefinition("markdown-converter", "Markdown格式转换", "Markdown转HTML、PDF、Word等格式",
                docCategory, "FileText", "#C2410C", "#FED7AA", false, true,
                buildInstructions("输入 Markdown", "在输入框中粘贴 Markdown 格式的文档内容",
                        "选择格式", "点击格式按钮选择目标格式（HTML、PDF、Word）",
                        "执行转换", "点击转换按钮进行格式转换"),
                new HashSet<>(Arrays.asList(docTag))));

        // ========== 加密工具 (红色系) ==========
        defs.add(new ToolDefinition("md5-encrypt", "MD5加密", "MD5加密工具，支持32位/16位",
                cryptoCategory, "Hash", "#DC2626", "#FEE2E2", false, true,
                buildInstructions("输入文本", "在输入框中填写需要加密的文本",
                        "选择位数", "选择32位或16位MD5加密",
                        "获取结果", "系统自动生成MD5加密字符串"),
                new HashSet<>(Arrays.asList(hotTag, commonTag))));

        defs.add(new ToolDefinition("url-encode", "URL编解码", "URL编码和解码工具",
                cryptoCategory, "Link", "#EF4444", "#FEF2F2", false, true,
                buildInstructions("URL编码", "将特殊字符转换为URL安全格式",
                        "URL解码", "将编码后的URL还原为原始字符串",
                        "自动识别", "自动识别编码/解码操作"),
                new HashSet<>()));

        defs.add(new ToolDefinition("base64-codec", "Base64编解码", "Base64编码和解码工具",
                cryptoCategory, "Binary", "#DC2626", "#FEE2E2", false, true,
                buildInstructions("Base64编码", "将普通文本转换为Base64编码格式",
                        "Base64解码", "将Base64编码还原为原始文本",
                        "URL安全", "支持URL安全的Base64编码"),
                new HashSet<>(Arrays.asList(commonTag))));

        defs.add(new ToolDefinition("jwt-parser", "JWT解析器", "解析 JWT Token，查看 Header、Payload、过期时间等信息",
                cryptoCategory, "Key", "#B91C1C", "#FECACA", false, true,
                buildInstructions("输入 Token", "粘贴 JWT Token，支持 Bearer 格式",
                        "解析结构", "自动解析 Header、Payload、Signature 三部分",
                        "查看过期", "自动检测并显示 Token 过期时间"),
                new HashSet<>(Arrays.asList(devTag))));

        defs.add(new ToolDefinition("rsa-key-generator", "RSA密钥生成器", "生成 RSA 公私钥对，支持多种密钥长度和格式",
                cryptoCategory, "KeyRound", "#991B1B", "#FEE2E2", false, true,
                buildInstructions("选择密钥长度", "1024/2048/3072/4096 bit，推荐 2048",
                        "选择格式", "PKCS#1 或 PKCS#8 格式",
                        "生成密钥", "点击生成按钮获取公私钥对"),
                new HashSet<>(Arrays.asList(devTag))));

        defs.add(new ToolDefinition("hmac-generator", "HMAC生成器", "生成 HMAC 消息认证码，支持多种哈希算法",
                cryptoCategory, "Fingerprint", "#DC2626", "#FEE2E2", false, true,
                buildInstructions("输入消息", "输入要计算 HMAC 的消息内容",
                        "输入密钥", "输入 Secret Key",
                        "选择算法", "支持 HMAC-SHA1/SHA256/SHA384/SHA512/MD5"),
                new HashSet<>(Arrays.asList(devTag))));

        // ========== 文本工具 (紫色系) ==========
        defs.add(new ToolDefinition("text-compare", "文本对比", "文本差异对比工具",
                textCategory, "GitCompare", "#9333EA", "#F3E8FF", false, true,
                buildInstructions("输入原文本", "在左侧输入框中填写原始文本",
                        "输入对比文本", "在右侧输入框中填写对比文本",
                        "查看差异", "系统自动高亮显示差异部分"),
                new HashSet<>()));

        defs.add(new ToolDefinition("word-count", "字数统计", "统计文本字数、字符数、行数",
                textCategory, "Text", "#A855F7", "#FAF5FF", false, true,
                buildInstructions("输入文本", "在文本框中输入或粘贴需要统计的内容",
                        "实时统计", "系统自动显示字数、字符数、行数",
                        "详细数据", "查看中文字数、英文单词数、标点符号等"),
                new HashSet<>(Arrays.asList(commonTag))));

        // 汉字转拼音
        defs.add(new ToolDefinition("chinese-to-pinyin", "汉字转拼音", "将汉字转换为拼音，支持声调、首字母提取、多音字检测",
                textCategory, "Type", "#9333EA", "#F3E8FF", false, true,
                buildInstructions("输入中文", "在文本框中输入需要转换的中文内容",
                        "选择选项", "设置声调显示、大小写、分隔符等选项",
                        "开始转换", "点击转换按钮获取拼音结果",
                        "高级功能", "可选提取首字母、检测多音字、查看逐字详情"),
                new HashSet<>(Arrays.asList(hotTag, commonTag))));

        // 重复行删除
        defs.add(new ToolDefinition("remove-duplicate-lines", "重复行删除", "删除文本中的重复行，支持多种选项",
                textCategory, "Filter", "#7C3AED", "#E9D5FF", false, true,
                buildInstructions("输入文本", "在左侧输入框中输入或粘贴要处理的文本内容",
                        "选择选项", "可选择忽略大小写、忽略首尾空白、保留首次出现、结果排序等",
                        "删除重复", "点击删除重复行按钮执行去重操作",
                        "查看统计", "显示原行数、结果行数、删除重复数等统计信息"),
                new HashSet<>(Arrays.asList(commonTag))));

        // ========== 数字工具 (琥珀/黄色系) ==========
        // 科学计算器
        defs.add(new ToolDefinition("scientific-calculator", "科学计算器", "支持三角函数、对数、指数等科学运算的计算器",
                numberCategory, "Calculator", "#D97706", "#FEF3C7", false, true,
                buildInstructions("基本运算", "支持加、减、乘、除四则运算",
                        "科学运算", "支持三角函数、对数、指数、幂运算、阶乘等",
                        "角度切换", "可切换弧度(RAD)和角度(DEG)模式",
                        "内存操作", "使用M+/M-/MR/MC进行内存存储"),
                new HashSet<>(Arrays.asList(hotTag))));

        defs.add(new ToolDefinition("radix-converter", "进制转换", "二进制、八进制、十进制、十六进制互转",
                numberCategory, "Binary", "#D97706", "#FEF3C7", false, true,
                buildInstructions("输入数值", "在输入框中填写要转换的数字",
                        "选择进制", "选择输入数字的当前进制",
                        "查看结果", "自动显示其他进制的转换结果"),
                new HashSet<>()));

        defs.add(new ToolDefinition("random-number", "随机数生成", "生成随机数、随机密码",
                numberCategory, "Dices", "#F59E0B", "#FFFBEB", false, true,
                buildInstructions("设置范围", "输入最小值和最大值",
                        "生成数量", "选择要生成的随机数个数",
                        "点击生成", "获取随机数结果"),
                new HashSet<>(Arrays.asList(hotTag))));

        // ========== 数字转中文大写 (数字工具) ==========
        defs.add(new ToolDefinition("number-to-chinese", "数字转中文大写", "将阿拉伯数字转换为中文大写金额，支持财务票据、合同等场景",
                numberCategory, "Banknote", "#D97706", "#FEF3C7", false, true,
                buildInstructions("选择模式", "金额格式（元角分）或普通数字",
                        "输入数字", "填写需要转换的阿拉伯数字",
                        "查看结果", "自动转换为中文大写",
                        "一键复制", "点击复制按钮复制转换结果"),
                new HashSet<>(Arrays.asList(hotTag, commonTag))));

        // ========== 媒体工具 (青色系) ==========
        defs.add(new ToolDefinition("video-convert", "视频格式转换", "视频格式互相转换，支持MP4/AVI/MOV等",
                mediaCategory, "Video", "#0891B2", "#CFFAFE", false, true,
                buildInstructions("上传视频", "选择需要转换的视频文件",
                        "选择格式", "选择目标视频格式",
                        "开始转换", "点击转换按钮，等待处理完成"),
                new HashSet<>(Arrays.asList(mediaTag))));

        defs.add(new ToolDefinition("audio-convert", "音频格式转换", "音频格式互相转换，支持MP3/WAV/FLAC等",
                mediaCategory, "Music", "#06B6D4", "#ECFEFF", false, true,
                buildInstructions("上传音频", "选择需要转换的音频文件",
                        "选择格式", "选择目标音频格式",
                        "开始转换", "点击转换按钮，等待处理完成"),
                new HashSet<>(Arrays.asList(mediaTag))));

        // ========== 生活工具 (粉色/玫瑰系) ==========
        defs.add(new ToolDefinition("mortgage-calculator", "房贷计算器", "计算商业贷款、公积金贷款、组合贷款及提前还款的月供和利息",
                lifeCategory, "Home", "#E11D48", "#FFE4E6", false, true,
                buildInstructions("选择计算模式", "普通房贷计算或提前还款计算",
                        "输入贷款信息", "填写贷款金额、年限、利率",
                        "设置提前还款", "可选，输入提前还款金额和方式",
                        "查看结果", "获取月供、总利息、还款计划表、节省利息"),
                new HashSet<>(Arrays.asList(hotTag, lifeTag))));

        defs.add(new ToolDefinition("salary-calculator", "薪资计算器", "计算税后工资、个人所得税、年终奖个税",
                lifeCategory, "Receipt", "#BE123C", "#FECDD3", false, true,
                buildInstructions("选择计算类型", "税后工资、个人所得税、年终奖",
                        "输入收入信息", "填写税前工资、城市、五险一金",
                        "填写扣除项", "专项附加扣除、其他扣除",
                        "查看明细", "获取个税、实发工资、税率等详细信息"),
                new HashSet<>(Arrays.asList(hotTag, lifeTag))));

        defs.add(new ToolDefinition("unit-converter", "综合单位换算", "一站式长度、重量、面积、体积、温度等单位换算",
                lifeCategory, "ArrowRightLeft", "#F43F5E", "#FFF1F2", false, true,
                buildInstructions("选择换算类型", "长度、重量、面积、体积、温度等",
                        "输入数值", "填写要换算的数值",
                        "选择单位", "选择原始单位和目标单位",
                        "查看结果", "自动显示换算结果"),
                new HashSet<>(Arrays.asList(commonTag, lifeTag))));

        defs.add(new ToolDefinition("date-calculator", "日期计算器", "计算日期间隔、日期加减、工作日计算",
                lifeCategory, "CalendarDays", "#BE123C", "#FECDD3", false, true,
                buildInstructions("选择计算方式", "日期间隔、日期加减、工作日计算",
                        "输入日期", "选择或输入相关日期",
                        "设置参数", "是否包含节假日、周末等",
                        "查看结果", "获取计算结果和详细天数"),
                new HashSet<>(Arrays.asList(lifeTag))));

        defs.add(new ToolDefinition("bmi-calculator", "BMI计算器", "计算身体质量指数并提供健康建议",
                lifeCategory, "Scale", "#E11D48", "#FFE4E6", false, true,
                buildInstructions("输入身高", "填写身高（厘米）",
                        "输入体重", "填写体重（公斤）",
                        "计算BMI", "自动计算BMI指数",
                        "查看建议", "获取体重状态和健康建议"),
                new HashSet<>(Arrays.asList(lifeTag))));

        defs.add(new ToolDefinition("age-calculator", "年龄计算器", "精确计算年龄，精确到天数",
                lifeCategory, "Cake", "#F43F5E", "#FFF1F2", false, true,
                buildInstructions("输入出生日期", "选择出生年月日",
                        "选择目标日期", "默认今天，可自定义",
                        "查看结果", "获取周岁、虚岁、存活天数等"),
                new HashSet<>(Arrays.asList(lifeTag))));

        defs.add(new ToolDefinition("countdown-timer", "倒计时工具", "设置重要日期倒计时，支持正计时和倒计时",
                lifeCategory, "Timer", "#BE123C", "#FECDD3", false, true,
                buildInstructions("设置目标日期", "选择重要日期如考试、生日、纪念日",
                        "添加标题", "为倒计时添加描述标题",
                        "保存倒计时", "可保存多个倒计时",
                        "查看剩余", "实时显示剩余天数、小时、分钟"),
                new HashSet<>(Arrays.asList(lifeTag))));

        // ========== Cron 工具 (开发工具) ==========
        defs.add(new ToolDefinition("cron-generator", "Cron 生成与解析", "支持 Linux、Quartz、Spring、AWS 等多种格式的 Cron 表达式生成、解析和转换",
                devCategory, "AlarmClock", "#2563EB", "#DBEAFE", false, true,
                buildInstructions("选择格式", "选择 Linux/Quartz/Spring/AWS 等 Cron 格式",
                        "生成表达式", "使用生成器可视化配置时间规则",
                        "解析表达式", "输入 Cron 表达式查看人类可读描述",
                        "执行预览", "查看未来执行时间列表"),
                new HashSet<>(Arrays.asList(devTag, commonTag))));

        // ========== 设备信息工具 (开发工具) ==========
        defs.add(new ToolDefinition("device-info", "设备信息检测", "获取浏览器、操作系统、屏幕、硬件、网络等详细的设备信息",
                devCategory, "Monitor", "#3B82F6", "#EFF6FF", false, true,
                buildInstructions("自动检测", "页面加载后自动获取所有设备信息",
                        "查看信息", "浏览操作系统、浏览器、屏幕、硬件、电池、网络等详细信息",
                        "功能检测", "查看设备支持的 Web 功能（WebGL、存储、定位等）",
                        "复制报告", "一键复制完整的设备信息报告"),
                new HashSet<>(Arrays.asList(devTag))));

        // ========== 前端工具 (靛蓝色系) ==========
        defs.add(new ToolDefinition("css-formatter", "CSS格式化", "CSS代码美化、压缩、格式化工具",
                frontendCategory, "Paintbrush", "#6366F1", "#E0E7FF", false, true,
                buildInstructions("输入CSS", "在输入框中粘贴需要格式化的CSS代码",
                        "选择操作", "选择格式化（美化）或压缩（去除空白）",
                        "查看结果", "系统自动处理并显示结果"),
                new HashSet<>()));

        defs.add(new ToolDefinition("layout-generator", "Flex/Grid布局生成器", "可视化生成CSS Flexbox和Grid布局",
                frontendCategory, "Layout", "#818CF8", "#C7D2FE", false, true,
                buildInstructions("选择布局类型", "选择 Flexbox 或 Grid 布局",
                        "调整参数", "实时调整布局参数",
                        "预览效果", "即时查看布局效果",
                        "复制代码", "一键复制生成的CSS代码"),
                new HashSet<>(Arrays.asList(commonTag))));

        defs.add(new ToolDefinition("color-picker", "颜色选择器", "多功能颜色选择器，支持HEX/RGB/HSL互转",
                frontendCategory, "Palette", "#6366F1", "#E0E7FF", false, true,
                buildInstructions("选择颜色", "使用颜色选择器或手动输入",
                        "格式转换", "支持HEX、RGB、RGBA、HSL、HSLA格式",
                        "精确调整", "通过滑块精确调整RGB和HSL参数",
                        "历史记录", "自动保存历史颜色，快速选择"),
                new HashSet<>(Arrays.asList(commonTag))));

        defs.add(new ToolDefinition("box-shadow-generator", "CSS阴影生成器", "可视化生成CSS box-shadow效果",
                frontendCategory, "Layers", "#4F46E5", "#C7D2FE", false, true,
                buildInstructions("添加阴影层", "支持多层阴影叠加效果",
                        "调整参数", "设置水平/垂直偏移、模糊、扩散、颜色",
                        "内阴影", "支持内阴影(inset)效果",
                        "一键复制", "实时预览并复制CSS代码"),
                new HashSet<>(Arrays.asList(commonTag))));

        defs.add(new ToolDefinition("gradient-generator", "渐变生成器", "生成线性渐变和径向渐变背景",
                frontendCategory, "Palette", "#818CF8", "#EEF2FF", false, true,
                buildInstructions("渐变类型", "选择线性渐变或径向渐变",
                        "线性角度", "线性渐变支持0-360度角度调整",
                        "添加色标", "支持多个颜色停止点，可调整位置",
                        "实时预览", "即时查看渐变效果，一键复制代码"),
                new HashSet<>(Arrays.asList(commonTag))));

        defs.add(new ToolDefinition("favicon-generator", "Favicon生成器", "快速生成网站图标，支持多种尺寸",
                frontendCategory, "Image", "#6366F1", "#E0E7FF", false, true,
                buildInstructions("图标模式", "使用文字和颜色快速生成简单图标",
                        "文字模式", "自定义文字内容、颜色、背景、圆角",
                        "图片模式", "上传图片直接转换为Favicon",
                        "多尺寸下载", "支持16x16到512x512多种尺寸PNG格式"),
                new HashSet<>(Arrays.asList(commonTag))));

        defs.add(new ToolDefinition("animation-generator", "CSS动画生成器", "可视化生成CSS动画效果，支持多种预设和参数调整",
                frontendCategory, "Play", "#4F46E5", "#C7D2FE", false, true,
                buildInstructions("选择动画", "从20+种预设动画中选择",
                        "调整参数", "设置持续时间、延迟、迭代次数等",
                        "实时预览", "即时查看动画效果，支持暂停/播放",
                        "一键复制", "复制完整的CSS代码和keyframes"),
                new HashSet<>(Arrays.asList(commonTag))));

        // ========== 网络工具 (青色系) ==========
        defs.add(new ToolDefinition("ip-query", "IP地址查询", "查询IP地址的地理位置、运营商等信息",
                networkCategory, "MapPin", "#0891B2", "#CFFAFE", false, true,
                buildInstructions("输入IP", "填写要查询的IP地址（留空查询本机IP）",
                        "点击查询", "系统自动获取IP的地理位置信息",
                        "查看结果", "显示国家、地区、运营商等详细信息"),
                new HashSet<>()));

        // ========== 设计工具 (紫色系) ==========
        defs.add(new ToolDefinition("color-palette", "配色方案生成", "生成和谐的配色方案，支持多种配色模式",
                designCategory, "SwatchBook", "#9333EA", "#F3E8FF", false, true,
                buildInstructions("选择主色", "选择或输入一个基础颜色",
                        "选择模式", "选择配色模式：类比、互补、三角等",
                        "生成方案", "系统自动生成和谐的配色组合"),
                new HashSet<>()));

        return defs;
    }

    /**
     * 构建使用说明HTML
     */
    private String buildInstructions(String... steps) {
        StringBuilder sb = new StringBuilder("<ol class=\"list-decimal list-inside space-y-2\">\n");
        for (int i = 0; i < steps.length; i += 2) {
            String title = steps[i];
            String desc = i + 1 < steps.length ? steps[i + 1] : "";
            sb.append(String.format("    <li><strong>%s</strong>：%s</li>\n", title, desc));
        }
        sb.append("  </ol>");
        return sb.toString();
    }

    /**
     * 初始化用户数据（仅在首次启动时执行）
     */
    private void initUsers() {
        log.info("初始化用户数据...");

        // 创建普通用户
        User normalUser = User.builder()
                .username("user")
                .email("user@example.com")
                .password(passwordEncoder.encode("123456"))
                .nickname("普通用户")
                .avatar("https://api.dicebear.com/9.x/dylan/svg?seed=user")
                .isVip(false)
                .status(UserStatus.ENABLED)
                .build();
        normalUser.addRole("USER");

        // 创建VIP用户
        User vipUser = User.builder()
                .username("vip")
                .email("vip@example.com")
                .password(passwordEncoder.encode("123456"))
                .nickname("VIP用户")
                .avatar("https://api.dicebear.com/9.x/dylan/svg?seed=vip")
                .isVip(true)
                .status(UserStatus.ENABLED)
                .build();
        vipUser.addRole("USER");
        vipUser.addRole("VIP");

        // 创建管理员用户
        User adminUser = User.builder()
                .username("admin")
                .email("admin@example.com")
                .password(passwordEncoder.encode("123456"))
                .nickname("管理员")
                .avatar("https://api.dicebear.com/9.x/dylan/svg?seed=admin")
                .isVip(true)
                .status(UserStatus.ENABLED)
                .build();
        adminUser.addRole("USER");
        adminUser.addRole("ADMIN");

        userRepository.saveAll(Arrays.asList(normalUser, vipUser, adminUser));

        log.info("用户数据初始化完成，共创建 {} 个用户", 3);
        log.info("普通用户: user/123456");
        log.info("VIP用户: vip/123456");
        log.info("管理员: admin/123456");
    }

    // ==================== 定义类 ====================

    private record CategoryDefinition(String code, String name, String icon, String description, int sortOrder) {}

    private record TagDefinition(String name, String description, boolean isHot) {}

    private record ToolDefinition(
            String code,
            String name,
            String description,
            Category category,
            String icon,
            String iconColor,
            String iconBgColor,
            boolean isVip,
            boolean isActive,
            String instructions,
            Set<Tag> tags
    ) {}
}
