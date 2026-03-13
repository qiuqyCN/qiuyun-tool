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
import org.springframework.boot.CommandLineRunner;
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
 *   图标: Braces, ArrowRightLeft, Code2, Search, Clock, Binary, Coffee
 *   使用建议: 开发类工具使用蓝色系，传达专业、技术感
 *
 * 图片工具 (image) - 绿色系
 *   主色: #16A34A (翠绿), #22C55E (亮绿), #15803D (深绿)
 *   背景: #DCFCE7 (浅绿), #F0FDF4 (极浅绿), #BBF7D0 (淡绿)
 *   图标: ImageMinus, ImagePlus, Image, QrCode, ScanLine
 *   使用建议: 图片处理类工具使用绿色系，传达自然、清晰感
 *
 * 文档工具 (document) - 橙色系
 *   主色: #EA580C (亮橙), #F97316 (中橙), #C2410C (深橙)
 *   背景: #FFEDD5 (浅橙), #FFF7ED (极浅橙), #FED7AA (淡橙)
 *   图标: FileText, FileEdit
 *   使用建议: 文档处理类工具使用橙色系，传达活力、创造力
 *
 * 加密工具 (crypto) - 红色系
 *   主色: #DC2626 (亮红), #EF4444 (中红), #B91C1C (深红)
 *   背景: #FEE2E2 (浅红), #FEF2F2 (极浅红), #FECACA (淡红)
 *   图标: Hash, Link
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
 * 媒体工具 (media) - 青色系
 *   主色: #0891B2 (青蓝), #06B6D4 (亮青), #0E7490 (深青)
 *   背景: #CFFAFE (浅青), #ECFEFF (极浅青), #A5F3FC (淡青)
 *   图标: Video, Music, Play, Film, Mic
 *   使用建议: 音视频类工具使用青色系，传达现代、科技感
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
public class DataInitializer implements CommandLineRunner {

    private final CategoryRepository categoryRepository;
    private final TagRepository tagRepository;
    private final ToolRepository toolRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(String... args) {
        log.info("开始数据初始化/同步...");

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

        log.info("数据初始化/同步完成！");
    }

    /**
     * 初始化分类数据（支持增量更新，仅在实际数据变化时更新）
     */
    private Map<String, Category> initCategories() {
        log.info("同步分类数据...");

        // 定义所有分类
        List<CategoryDefinition> categoryDefs = Arrays.asList(
                new CategoryDefinition("dev", "开发工具", "Code", "JSON格式化、代码压缩、正则测试等开发常用工具", 1),
                new CategoryDefinition("document", "文档转换", "FileText", "PDF转换、Word转换、Markdown编辑等文档工具", 3),
                new CategoryDefinition("image", "图片工具", "Image", "图片压缩、格式转换、Base64编码、二维码生成等图片处理工具", 2),
                new CategoryDefinition("media", "媒体工具", "Video", "视频转换、音频处理、格式转换等媒体工具", 7),
                new CategoryDefinition("crypto", "加密工具", "Lock", "MD5加密、Base64、URL编码等加密解密工具", 4),
                new CategoryDefinition("text", "文本工具", "Type", "文本对比、字数统计、大小写转换等文本处理工具", 5),
                new CategoryDefinition("number", "数字工具", "Calculator", "进制转换、单位换算、随机数生成等数字工具", 6),
                new CategoryDefinition("life", "生活工具", "Heart", "日历、天气、单位换算、日常计算等生活实用工具", 8)
        );

        // 获取现有分类
        List<Category> existingCategories = categoryRepository.findAll();
        Map<String, Category> existingMap = existingCategories.stream()
                .collect(Collectors.toMap(Category::getCode, c -> c));

        List<Category> toSave = new ArrayList<>();
        int newCount = 0;
        int updateCount = 0;
        int unchangedCount = 0;

        for (CategoryDefinition def : categoryDefs) {
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

        // 定义所有标签
        List<TagDefinition> tagDefs = Arrays.asList(
                new TagDefinition("热门", "热门工具", true),
                new TagDefinition("常用", "常用工具", true),
                new TagDefinition("VIP", "VIP专属工具", false),
                new TagDefinition("开发", "开发相关", false),
                new TagDefinition("文档", "文档处理", false),
                new TagDefinition("图片", "图片处理", false),
                new TagDefinition("媒体", "音视频处理", false),
                new TagDefinition("生活", "生活实用", false)
        );

        // 获取现有标签
        List<Tag> existingTags = tagRepository.findAll();
        Map<String, Tag> existingMap = existingTags.stream()
                .collect(Collectors.toMap(Tag::getName, t -> t));

        List<Tag> toSave = new ArrayList<>();
        int newCount = 0;
        int updateCount = 0;
        int unchangedCount = 0;

        for (TagDefinition def : tagDefs) {
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

        // 获取现有工具
        List<Tool> existingTools = toolRepository.findAll();
        Map<String, Tool> existingMap = existingTools.stream()
                .collect(Collectors.toMap(Tool::getCode, t -> t));

        // 定义所有工具
        List<ToolDefinition> toolDefs = buildToolDefinitions(categories, tags);

        List<Tool> toSave = new ArrayList<>();
        int newCount = 0;
        int updateCount = 0;
        int unchangedCount = 0;

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
            } else if (isToolChanged(tool, def)) {
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
     * 比较工具定义与现有工具是否发生变化
     */
    private boolean isToolChanged(Tool existingTool, ToolDefinition def) {
        // 比较基本字段
        if (!Objects.equals(existingTool.getName(), def.name)) return true;
        if (!Objects.equals(existingTool.getDescription(), def.description)) return true;
        if (!Objects.equals(existingTool.getIcon(), def.icon)) return true;
        if (!Objects.equals(existingTool.getIconColor(), def.iconColor)) return true;
        if (!Objects.equals(existingTool.getIconBgColor(), def.iconBgColor)) return true;
        if (existingTool.getIsVip() != def.isVip) return true;
        if (existingTool.getIsActive() != def.isActive) return true;
        if (!Objects.equals(existingTool.getInstructions(), def.instructions)) return true;

        // 比较分类
        if (existingTool.getCategory() == null || def.category == null) {
            if (existingTool.getCategory() != def.category) return true;
        } else if (!Objects.equals(existingTool.getCategory().getId(), def.category.getId())) {
            return true;
        }

        // 比较标签
        Set<String> existingTagNames = existingTool.getTags() != null
                ? existingTool.getTags().stream().map(Tag::getName).collect(Collectors.toSet())
                : new HashSet<>();
        Set<String> defTagNames = def.tags != null
                ? def.tags.stream().map(Tag::getName).collect(Collectors.toSet())
                : new HashSet<>();
        if (!existingTagNames.equals(defTagNames)) return true;

        return false;
    }

    /**
     * 构建工具定义列表
     */
    private List<ToolDefinition> buildToolDefinitions(Map<String, Category> categories, Map<String, Tag> tags) {
        List<ToolDefinition> defs = new ArrayList<>();

        Category devCategory = categories.get("dev");
        Category imageCategory = categories.get("image");
        Category docCategory = categories.get("document");
        Category cryptoCategory = categories.get("crypto");
        Category textCategory = categories.get("text");
        Category numberCategory = categories.get("number");
        Category mediaCategory = categories.get("media");
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



        defs.add(new ToolDefinition("yaml-properties-converter", "YAML/Properties互转", "YAML格式与Java Properties配置文件双向转换",
                devCategory, "ArrowRightLeft", "#2563EB", "#DBEAFE", false, true,
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
                imageCategory, "Image", "#15803D", "#BBF7D0", false, true,
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

        // ========== 文档工具 (橙色系) ==========
        defs.add(new ToolDefinition("pdf-to-word", "PDF转Word", "PDF文档转换为Word格式",
                docCategory, "FileText", "#EA580C", "#FFEDD5", true, true,
                buildInstructions("上传PDF", "选择要转换的PDF文件",
                        "开始转换", "点击转换按钮，等待处理完成",
                        "下载Word", "转换完成后下载.docx文件"),
                new HashSet<>(Arrays.asList(hotTag, vipTag, docTag))));

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
                cryptoCategory, "Key", "#991B1B", "#FEE2E2", false, true,
                buildInstructions("选择密钥长度", "1024/2048/3072/4096 bit，推荐 2048",
                        "选择格式", "PKCS#1 或 PKCS#8 格式",
                        "生成密钥", "点击生成按钮获取公私钥对"),
                new HashSet<>(Arrays.asList(devTag))));

        defs.add(new ToolDefinition("hmac-generator", "HMAC生成器", "生成 HMAC 消息认证码，支持多种哈希算法",
                cryptoCategory, "Hash", "#DC2626", "#FEE2E2", false, true,
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

        // ========== 数字工具 (琥珀/黄色系) ==========
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
                lifeCategory, "Banknote", "#BE123C", "#FECDD3", false, true,
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
                devCategory, "Clock", "#2563EB", "#DBEAFE", false, true,
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
