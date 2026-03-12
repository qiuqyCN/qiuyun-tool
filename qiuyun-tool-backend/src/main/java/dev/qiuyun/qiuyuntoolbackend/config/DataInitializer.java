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
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

/**
 * 数据初始化器
 * 应用启动时初始化分类、标签和工具数据
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
        // 检查是否已有数据
        if (categoryRepository.count() > 0) {
            log.info("数据库已有数据，跳过初始化");
            return;
        }

        log.info("开始初始化数据...");

        // 初始化分类
        List<Category> categories = initCategories();

        // 初始化标签
        List<Tag> tags = initTags();

        // 初始化工具
        initTools(categories, tags);

        // 初始化用户数据
        initUsers();

        log.info("数据初始化完成！");
    }

    /**
     * 初始化分类数据
     */
    private List<Category> initCategories() {
        log.info("初始化分类数据...");

        List<Category> categories = Arrays.asList(
                Category.builder()
                        .code("dev")
                        .name("开发工具")
                        .icon("Code")
                        .description("JSON格式化、代码压缩、正则测试等开发常用工具")
                        .sortOrder(1)
                        .isActive(true)
                        .build(),
                Category.builder()
                        .code("image")
                        .name("图片工具")
                        .icon("Image")
                        .description("图片压缩、格式转换、Base64编码等图片处理工具")
                        .sortOrder(2)
                        .isActive(true)
                        .build(),
                Category.builder()
                        .code("document")
                        .name("文档转换")
                        .icon("FileText")
                        .description("PDF转换、Word转换、Markdown编辑等文档工具")
                        .sortOrder(3)
                        .isActive(true)
                        .build(),
                Category.builder()
                        .code("crypto")
                        .name("加密工具")
                        .icon("Lock")
                        .description("MD5加密、Base64、URL编码等加密解密工具")
                        .sortOrder(4)
                        .isActive(true)
                        .build(),
                Category.builder()
                        .code("text")
                        .name("文本工具")
                        .icon("Type")
                        .description("文本对比、字数统计、大小写转换等文本处理工具")
                        .sortOrder(5)
                        .isActive(true)
                        .build(),
                Category.builder()
                        .code("number")
                        .name("数字工具")
                        .icon("Calculator")
                        .description("进制转换、单位换算、随机数生成等数字工具")
                        .sortOrder(6)
                        .isActive(true)
                        .build()
        );

        return categoryRepository.saveAll(categories);
    }

    /**
     * 初始化标签数据
     */
    private List<Tag> initTags() {
        log.info("初始化标签数据...");

        List<Tag> tags = Arrays.asList(
                Tag.builder().name("热门").description("热门工具").isHot(true).build(),
                Tag.builder().name("常用").description("常用工具").isHot(true).build(),
                Tag.builder().name("VIP").description("VIP专属工具").isHot(false).build(),
                Tag.builder().name("开发").description("开发相关").isHot(false).build(),
                Tag.builder().name("图片").description("图片处理").isHot(false).build(),
                Tag.builder().name("文档").description("文档处理").isHot(false).build()
        );

        return tagRepository.saveAll(tags);
    }

    /**
     * 初始化工具数据
     */
    private void initTools(List<Category> categories, List<Tag> tags) {
        log.info("初始化工具数据...");

        Tag hotTag = tags.stream().filter(t -> t.getName().equals("热门")).findFirst().orElse(null);
        Tag commonTag = tags.stream().filter(t -> t.getName().equals("常用")).findFirst().orElse(null);
        Tag vipTag = tags.stream().filter(t -> t.getName().equals("VIP")).findFirst().orElse(null);
        Tag devTag = tags.stream().filter(t -> t.getName().equals("开发")).findFirst().orElse(null);
        Tag imageTag = tags.stream().filter(t -> t.getName().equals("图片")).findFirst().orElse(null);
        Tag docTag = tags.stream().filter(t -> t.getName().equals("文档")).findFirst().orElse(null);

        Category devCategory = categories.stream().filter(c -> c.getCode().equals("dev")).findFirst().orElse(null);
        Category imageCategory = categories.stream().filter(c -> c.getCode().equals("image")).findFirst().orElse(null);
        Category docCategory = categories.stream().filter(c -> c.getCode().equals("document")).findFirst().orElse(null);
        Category cryptoCategory = categories.stream().filter(c -> c.getCode().equals("crypto")).findFirst().orElse(null);
        Category textCategory = categories.stream().filter(c -> c.getCode().equals("text")).findFirst().orElse(null);
        Category numberCategory = categories.stream().filter(c -> c.getCode().equals("number")).findFirst().orElse(null);

        // 开发工具 - 蓝色系（浅色背景）
        Tool jsonFormatter = Tool.builder()
                .code("json-formatter")
                .name("JSON格式化")
                .description("JSON数据的格式化、压缩、转义等操作")
                .category(devCategory)
                .icon("Braces")
                .iconColor("#1E40AF")
                .iconBgColor("#DBEAFE")
                .isVip(false)
                .isActive(true)
                .visitsCount(0L)
                .viewCount(0L)
                .usageCount(0L)
                .rating(BigDecimal.valueOf(0.0))
                .reviewCount(0)
                .favoriteCount(0)
                .instructions("<ol class=\"list-decimal list-inside space-y-2\">\n" +
                        "    <li><strong>格式化</strong>：将压缩的 JSON 数据转换为易读的格式，自动添加缩进和换行</li>\n" +
                        "    <li><strong>压缩</strong>：去除 JSON 中的空白字符，减小数据体积</li>\n" +
                        "    <li><strong>转义</strong>：将 JSON 字符串转义，适用于在代码中使用</li>\n" +
                        "    <li><strong>去转义</strong>：将转义后的 JSON 字符串还原为正常格式</li>\n" +
                        "    <li>支持复制结果到剪贴板或下载为 .json 文件</li>\n" +
                        "  </ol>")
                .tags(new HashSet<>(Arrays.asList(hotTag, commonTag)))
                .build();

        Tool jsonToYaml = Tool.builder()
                .code("json-to-yaml")
                .name("JSON转YAML")
                .description("JSON与YAML格式互相转换")
                .category(devCategory)
                .icon("FileJson")
                .iconColor("#3730A3")
                .iconBgColor("#E0E7FF")
                .isVip(false)
                .isActive(true)
                .visitsCount(0L)
                .viewCount(0L)
                .usageCount(0L)
                .rating(BigDecimal.valueOf(0.0))
                .reviewCount(0)
                .favoriteCount(0)
                .instructions("<ol class=\"list-decimal list-inside space-y-2\">\n" +
                        "    <li><strong>JSON转YAML</strong>：将JSON格式的数据转换为YAML格式，便于配置文件编写</li>\n" +
                        "    <li><strong>YAML转JSON</strong>：将YAML格式的数据转换为JSON格式，便于程序解析</li>\n" +
                        "    <li>支持复杂的嵌套结构转换</li>\n" +
                        "    <li>自动检测输入格式并进行相应转换</li>\n" +
                        "    <li>支持复制结果或下载为文件</li>\n" +
                        "  </ol>")
                .tags(new HashSet<>(Arrays.asList(devTag)))
                .build();

        Tool codeBeautify = Tool.builder()
                .code("code-beautify")
                .name("代码美化")
                .description("HTML/CSS/JavaScript/Java/SQL/XML代码格式化")
                .category(devCategory)
                .icon("Code2")
                .iconColor("#5B21B6")
                .iconBgColor("#EDE9FE")
                .isVip(true)
                .isActive(true)
                .visitsCount(0L)
                .viewCount(0L)
                .usageCount(0L)
                .rating(BigDecimal.valueOf(0.0))
                .reviewCount(0)
                .favoriteCount(0)
                .instructions("<ol class=\"list-decimal list-inside space-y-2\">\n" +
                        "    <li><strong>HTML格式化</strong>：自动缩进和换行，使HTML结构清晰易读</li>\n" +
                        "    <li><strong>CSS格式化</strong>：美化CSS样式代码，规范属性格式</li>\n" +
                        "    <li><strong>JavaScript格式化</strong>：格式化JS代码，提高可读性</li>\n" +
                        "    <li>支持代码压缩功能，减小文件体积</li>\n" +
                        "    <li>支持语法高亮和错误检测</li>\n" +
                        "  </ol>")
                .tags(new HashSet<>(Arrays.asList(vipTag)))
                .build();

        Tool regexTester = Tool.builder()
                .code("regex-tester")
                .name("正则测试")
                .description("在线正则表达式测试工具")
                .category(devCategory)
                .icon("Search")
                .iconColor("#0369A1")
                .iconBgColor("#E0F2FE")
                .isVip(false)
                .isActive(true)
                .visitsCount(0L)
                .viewCount(0L)
                .usageCount(0L)
                .rating(BigDecimal.valueOf(0.0))
                .reviewCount(0)
                .favoriteCount(0)
                .instructions("<ol class=\"list-decimal list-inside space-y-2\">\n" +
                        "    <li><strong>输入正则表达式</strong>：在正则输入框中填写要测试的正则表达式</li>\n" +
                        "    <li><strong>输入测试文本</strong>：在文本框中输入需要匹配的测试内容</li>\n" +
                        "    <li><strong>实时匹配</strong>：系统会实时显示匹配结果和高亮匹配内容</li>\n" +
                        "    <li>支持常用正则表达式预设，一键使用</li>\n" +
                        "    <li>显示匹配分组信息，方便提取数据</li>\n" +
                        "  </ol>")
                .tags(new HashSet<>())
                .build();

        Tool timestampConverter = Tool.builder()
                .code("timestamp-converter")
                .name("时间戳转换")
                .description("Unix时间戳与日期时间互转")
                .category(devCategory)
                .icon("Clock")
                .iconColor("#0E7490")
                .iconBgColor("#CFFAFE")
                .isVip(false)
                .isActive(true)
                .visitsCount(0L)
                .viewCount(0L)
                .usageCount(0L)
                .rating(BigDecimal.valueOf(0.0))
                .reviewCount(0)
                .favoriteCount(0)
                .instructions("<ol class=\"list-decimal list-inside space-y-2\">\n" +
                        "    <li><strong>时间戳转日期</strong>：输入Unix时间戳（秒或毫秒），转换为可读的日期时间</li>\n" +
                        "    <li><strong>日期转时间戳</strong>：选择或输入日期时间，转换为Unix时间戳</li>\n" +
                        "    <li>支持多种日期格式输出</li>\n" +
                        "    <li>自动识别当前时区和UTC时间</li>\n" +
                        "    <li>支持批量转换多个时间戳</li>\n" +
                        "  </ol>")
                .tags(new HashSet<>(Arrays.asList(hotTag)))
                .build();

        Tool base64Codec = Tool.builder()
                .code("base64-codec")
                .name("Base64编解码")
                .description("Base64编码和解码工具")
                .category(devCategory)
                .icon("Binary")
                .iconColor("#0F766E")
                .iconBgColor("#CCFBF1")
                .isVip(false)
                .isActive(true)
                .visitsCount(0L)
                .viewCount(0L)
                .usageCount(0L)
                .rating(BigDecimal.valueOf(0.0))
                .reviewCount(0)
                .favoriteCount(0)
                .instructions("<ol class=\"list-decimal list-inside space-y-2\">\n" +
                        "    <li><strong>Base64编码</strong>：将普通文本转换为Base64编码格式</li>\n" +
                        "    <li><strong>Base64解码</strong>：将Base64编码还原为原始文本</li>\n" +
                        "    <li>支持URL安全的Base64编码</li>\n" +
                        "    <li>支持处理中文字符，自动识别编码</li>\n" +
                        "    <li>支持复制结果或下载为文件</li>\n" +
                        "  </ol>")
                .tags(new HashSet<>())
                .build();

        Tool yamlPropertiesConverter = Tool.builder()
                .code("yaml-properties-converter")
                .name("YAML/Properties互转")
                .description("YAML格式与Java Properties配置文件双向转换")
                .category(devCategory)
                .icon("ArrowRightLeft")
                .iconColor("#4338CA")
                .iconBgColor("#E0E7FF")
                .isVip(false)
                .isActive(true)
                .visitsCount(0L)
                .viewCount(0L)
                .usageCount(0L)
                .rating(BigDecimal.valueOf(0.0))
                .reviewCount(0)
                .favoriteCount(0)
                .instructions("<ol class=\"list-decimal list-inside space-y-2\">\n" +
                        "    <li><strong>选择转换方向</strong>：点击顶部标签切换 Properties→YAML 或 YAML→Properties</li>\n" +
                        "    <li><strong>输入内容</strong>：在输入框中粘贴需要转换的配置内容</li>\n" +
                        "    <li><strong>执行转换</strong>：点击执行按钮进行格式转换</li>\n" +
                        "    <li><strong>切换方向</strong>：点击切换按钮可快速交换输入输出内容并反向转换</li>\n" +
                        "    <li>支持嵌套结构转换（点号分隔键 ↔ YAML层级）</li>\n" +
                        "    <li>支持数组索引格式转换</li>\n" +
                        "    <li>自动识别数值和布尔值类型</li>\n" +
                        "    <li>支持键排序选项</li>\n" +
                        "  </ol>")
                .tags(new HashSet<>(Arrays.asList(devTag)))
                .build();

        // 图片工具 - 绿色系（浅色背景）
        Tool imageCompress = Tool.builder()
                .code("image-compress")
                .name("图片压缩")
                .description("在线图片压缩，支持JPG/PNG/GIF")
                .category(imageCategory)
                .icon("ImageMinus")
                .iconColor("#065F46")
                .iconBgColor("#D1FAE5")
                .isVip(false)
                .isActive(true)
                .visitsCount(0L)
                .viewCount(0L)
                .usageCount(0L)
                .rating(BigDecimal.valueOf(0.0))
                .reviewCount(0)
                .favoriteCount(0)
                .instructions("<ol class=\"list-decimal list-inside space-y-2\">\n" +
                        "    <li><strong>上传图片</strong>：点击上传或拖拽图片文件到指定区域</li>\n" +
                        "    <li><strong>选择压缩质量</strong>：调整压缩比例，平衡画质和文件大小</li>\n" +
                        "    <li><strong>预览对比</strong>：查看压缩前后的画质对比</li>\n" +
                        "    <li>支持JPG、PNG、GIF格式</li>\n" +
                        "    <li>支持批量压缩多张图片</li>\n" +
                        "  </ol>")
                .tags(new HashSet<>(Arrays.asList(hotTag, commonTag)))
                .build();

        Tool imageConvert = Tool.builder()
                .code("image-convert")
                .name("图片格式转换")
                .description("图片格式互相转换")
                .category(imageCategory)
                .icon("ImagePlus")
                .iconColor("#15803D")
                .iconBgColor("#DCFCE7")
                .isVip(true)
                .isActive(true)
                .visitsCount(0L)
                .viewCount(0L)
                .usageCount(0L)
                .rating(BigDecimal.valueOf(0.0))
                .reviewCount(0)
                .favoriteCount(0)
                .instructions("<ol class=\"list-decimal list-inside space-y-2\">\n" +
                        "    <li><strong>上传图片</strong>：选择需要转换格式的图片文件</li>\n" +
                        "    <li><strong>选择目标格式</strong>：选择要转换成的图片格式</li>\n" +
                        "    <li><strong>开始转换</strong>：点击转换按钮，等待处理完成</li>\n" +
                        "    <li>支持JPG、PNG、GIF、WebP、BMP等格式互转</li>\n" +
                        "    <li>支持调整输出图片质量</li>\n" +
                        "  </ol>")
                .tags(new HashSet<>(Arrays.asList(vipTag, imageTag)))
                .build();

        Tool imageToBase64 = Tool.builder()
                .code("image-to-base64")
                .name("图片转Base64")
                .description("图片转换为Base64编码")
                .category(imageCategory)
                .icon("Image")
                .iconColor("#047857")
                .iconBgColor("#D1FAE5")
                .isVip(false)
                .isActive(true)
                .visitsCount(0L)
                .viewCount(0L)
                .usageCount(0L)
                .rating(BigDecimal.valueOf(0.0))
                .reviewCount(0)
                .favoriteCount(0)
                .instructions("<ol class=\"list-decimal list-inside space-y-2\">\n" +
                        "    <li><strong>上传图片</strong>：选择要转换的图片文件</li>\n" +
                        "    <li><strong>自动转换</strong>：系统自动将图片转换为Base64编码</li>\n" +
                        "    <li><strong>复制结果</strong>：一键复制Base64字符串</li>\n" +
                        "    <li>支持生成Data URI格式，可直接用于CSS/HTML</li>\n" +
                        "    <li>支持Base64转图片，还原原始图片</li>\n" +
                        "  </ol>")
                .tags(new HashSet<>(Arrays.asList(imageTag)))
                .build();

        // 文档工具 - 橙色系（浅色背景）
        Tool pdfToWord = Tool.builder()
                .code("pdf-to-word")
                .name("PDF转Word")
                .description("PDF文档转换为Word格式")
                .category(docCategory)
                .icon("FileText")
                .iconColor("#9A3412")
                .iconBgColor("#FFEDD5")
                .isVip(true)
                .isActive(true)
                .visitsCount(0L)
                .viewCount(0L)
                .usageCount(0L)
                .rating(BigDecimal.valueOf(0.0))
                .reviewCount(0)
                .favoriteCount(0)
                .instructions("<ol class=\"list-decimal list-inside space-y-2\">\n" +
                        "    <li><strong>上传PDF</strong>：选择要转换的PDF文件</li>\n" +
                        "    <li><strong>开始转换</strong>：点击转换按钮，等待处理完成</li>\n" +
                        "    <li><strong>下载Word</strong>：转换完成后下载.docx文件</li>\n" +
                        "    <li>保留原文档格式和排版</li>\n" +
                        "    <li>支持多页PDF文档转换</li>\n" +
                        "  </ol>")
                .tags(new HashSet<>(Arrays.asList(hotTag, vipTag, docTag)))
                .build();

        Tool markdownEditor = Tool.builder()
                .code("markdown-editor")
                .name("Markdown编辑器")
                .description("在线Markdown编辑和预览")
                .category(docCategory)
                .icon("FileEdit")
                .iconColor("#C2410C")
                .iconBgColor("#FED7AA")
                .isVip(false)
                .isActive(true)
                .visitsCount(0L)
                .viewCount(0L)
                .usageCount(0L)
                .rating(BigDecimal.valueOf(0.0))
                .reviewCount(0)
                .favoriteCount(0)
                .instructions("<ol class=\"list-decimal list-inside space-y-2\">\n" +
                        "    <li><strong>编辑Markdown</strong>：在左侧编辑器中输入Markdown语法</li>\n" +
                        "    <li><strong>实时预览</strong>：右侧实时显示渲染后的效果</li>\n" +
                        "    <li><strong>工具栏</strong>：使用工具栏快速插入常用格式</li>\n" +
                        "    <li>支持导出为HTML或PDF</li>\n" +
                        "    <li>支持语法高亮和表格编辑</li>\n" +
                        "  </ol>")
                .tags(new HashSet<>(Arrays.asList(docTag)))
                .build();

        // 加密工具 - 红色系（浅色背景）
        Tool md5Encrypt = Tool.builder()
                .code("md5-encrypt")
                .name("MD5加密")
                .description("MD5加密工具，支持32位/16位")
                .category(cryptoCategory)
                .icon("Hash")
                .iconColor("#991B1B")
                .iconBgColor("#FEE2E2")
                .isVip(false)
                .isActive(true)
                .visitsCount(0L)
                .viewCount(0L)
                .usageCount(0L)
                .rating(BigDecimal.valueOf(0.0))
                .reviewCount(0)
                .favoriteCount(0)
                .instructions("<ol class=\"list-decimal list-inside space-y-2\">\n" +
                        "    <li><strong>输入文本</strong>：在输入框中填写需要加密的文本</li>\n" +
                        "    <li><strong>选择位数</strong>：选择32位或16位MD5加密</li>\n" +
                        "    <li><strong>获取结果</strong>：系统自动生成MD5加密字符串</li>\n" +
                        "    <li>支持大写和小写输出格式</li>\n" +
                        "    <li>支持批量加密多个文本</li>\n" +
                        "  </ol>")
                .tags(new HashSet<>(Arrays.asList(hotTag, commonTag)))
                .build();

        Tool urlEncode = Tool.builder()
                .code("url-encode")
                .name("URL编解码")
                .description("URL编码和解码工具")
                .category(cryptoCategory)
                .icon("Link")
                .iconColor("#B91C1C")
                .iconBgColor("#FECACA")
                .isVip(false)
                .isActive(true)
                .visitsCount(0L)
                .viewCount(0L)
                .usageCount(0L)
                .rating(BigDecimal.valueOf(0.0))
                .reviewCount(0)
                .favoriteCount(0)
                .instructions("<ol class=\"list-decimal list-inside space-y-2\">\n" +
                        "    <li><strong>URL编码</strong>：将特殊字符转换为URL安全格式</li>\n" +
                        "    <li><strong>URL解码</strong>：将编码后的URL还原为原始字符串</li>\n" +
                        "    <li>自动识别编码/解码操作</li>\n" +
                        "    <li>支持处理中文字符</li>\n" +
                        "    <li>支持批量处理多个URL</li>\n" +
                        "  </ol>")
                .tags(new HashSet<>())
                .build();

        // 文本工具 - 紫色系（浅色背景）
        Tool textCompare = Tool.builder()
                .code("text-compare")
                .name("文本对比")
                .description("文本差异对比工具")
                .category(textCategory)
                .icon("GitCompare")
                .iconColor("#6B21A8")
                .iconBgColor("#F3E8FF")
                .isVip(false)
                .isActive(true)
                .visitsCount(0L)
                .viewCount(0L)
                .usageCount(0L)
                .rating(BigDecimal.valueOf(0.0))
                .reviewCount(0)
                .favoriteCount(0)
                .instructions("<ol class=\"list-decimal list-inside space-y-2\">\n" +
                        "    <li><strong>输入原文本</strong>：在左侧输入框中填写原始文本</li>\n" +
                        "    <li><strong>输入对比文本</strong>：在右侧输入框中填写对比文本</li>\n" +
                        "    <li><strong>查看差异</strong>：系统自动高亮显示差异部分</li>\n" +
                        "    <li>支持行级和字符级对比</li>\n" +
                        "    <li>支持忽略空格和大小写选项</li>\n" +
                        "  </ol>")
                .tags(new HashSet<>())
                .build();

        Tool wordCount = Tool.builder()
                .code("word-count")
                .name("字数统计")
                .description("统计文本字数、字符数、行数")
                .category(textCategory)
                .icon("Text")
                .iconColor("#7C3AED")
                .iconBgColor("#EDE9FE")
                .isVip(false)
                .isActive(true)
                .visitsCount(0L)
                .viewCount(0L)
                .usageCount(0L)
                .rating(BigDecimal.valueOf(0.0))
                .reviewCount(0)
                .favoriteCount(0)
                .instructions("<ol class=\"list-decimal list-inside space-y-2\">\n" +
                        "    <li><strong>输入文本</strong>：在文本框中输入或粘贴需要统计的内容</li>\n" +
                        "    <li><strong>实时统计</strong>：系统自动显示字数、字符数、行数</li>\n" +
                        "    <li><strong>详细数据</strong>：查看中文字数、英文单词数、标点符号等</li>\n" +
                        "    <li>支持统计选中内容的字数</li>\n" +
                        "    <li>支持清空和复制文本</li>\n" +
                        "  </ol>")
                .tags(new HashSet<>(Arrays.asList(commonTag)))
                .build();

        // 数字工具 - 琥珀色系（浅色背景）
        Tool hexConverter = Tool.builder()
                .code("radix-converter")
                .name("进制转换")
                .description("二进制、八进制、十进制、十六进制互转")
                .category(numberCategory)
                .icon("Binary")
                .iconColor("#92400E")
                .iconBgColor("#FEF3C7")
                .isVip(false)
                .isActive(true)
                .visitsCount(0L)
                .viewCount(0L)
                .usageCount(0L)
                .rating(BigDecimal.valueOf(0.0))
                .reviewCount(0)
                .favoriteCount(0)
                .instructions("<ol class=\"list-decimal list-inside space-y-2\">\n" +
                        "    <li><strong>输入数值</strong>：在输入框中填写要转换的数字</li>\n" +
                        "    <li><strong>选择进制</strong>：选择输入数字的当前进制</li>\n" +
                        "    <li><strong>查看结果</strong>：自动显示其他进制的转换结果</li>\n" +
                        "    <li>支持二进制、八进制、十进制、十六进制互转</li>\n" +
                        "    <li>支持批量转换多个数值</li>\n" +
                        "  </ol>")
                .tags(new HashSet<>())
                .build();

        Tool randomNumber = Tool.builder()
                .code("random-number")
                .name("随机数生成")
                .description("生成随机数、随机密码")
                .category(numberCategory)
                .icon("Dices")
                .iconColor("#B45309")
                .iconBgColor("#FEF9C3")
                .isVip(false)
                .isActive(true)
                .visitsCount(0L)
                .viewCount(0L)
                .usageCount(0L)
                .rating(BigDecimal.valueOf(0.0))
                .reviewCount(0)
                .favoriteCount(0)
                .instructions("<ol class=\"list-decimal list-inside space-y-2\">\n" +
                        "    <li><strong>设置范围</strong>：输入最小值和最大值</li>\n" +
                        "    <li><strong>生成数量</strong>：选择要生成的随机数个数</li>\n" +
                        "    <li><strong>点击生成</strong>：获取随机数结果</li>\n" +
                        "    <li>支持生成随机密码，可设置长度和字符类型</li>\n" +
                        "    <li>支持生成不重复的随机数</li>\n" +
                        "  </ol>")
                .tags(new HashSet<>(Arrays.asList(hotTag)))
                .build();

        List<Tool> tools = Arrays.asList(
                jsonFormatter, jsonToYaml, codeBeautify, regexTester, timestampConverter, base64Codec, yamlPropertiesConverter,
                imageCompress, imageConvert, imageToBase64,
                pdfToWord, markdownEditor,
                md5Encrypt, urlEncode,
                textCompare, wordCount,
                hexConverter, randomNumber
        );

        toolRepository.saveAll(tools);
    }

    /**
     * 初始化用户数据
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
}
