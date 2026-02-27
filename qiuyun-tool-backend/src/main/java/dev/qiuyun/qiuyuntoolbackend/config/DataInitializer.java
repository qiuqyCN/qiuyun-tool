package dev.qiuyun.qiuyuntoolbackend.config;

import dev.qiuyun.qiuyuntoolbackend.entity.Category;
import dev.qiuyun.qiuyuntoolbackend.entity.Tag;
import dev.qiuyun.qiuyuntoolbackend.entity.Tool;
import dev.qiuyun.qiuyuntoolbackend.repository.CategoryRepository;
import dev.qiuyun.qiuyuntoolbackend.repository.TagRepository;
import dev.qiuyun.qiuyuntoolbackend.repository.ToolRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
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

        // 开发工具
        Tool jsonFormatter = Tool.builder()
                .code("json-formatter")
                .name("JSON格式化")
                .description("JSON数据的格式化、压缩、转义等操作")
                .category(devCategory)
                .icon("Braces")
                .isVip(false)
                .isActive(true)
                .visitsCount(125432L)
                .rating(BigDecimal.valueOf(4.8))
                .reviewCount(328)
                .tags(new HashSet<>(Arrays.asList(hotTag, commonTag)))
                .build();

        Tool jsonToYaml = Tool.builder()
                .code("json-to-yaml")
                .name("JSON转YAML")
                .description("JSON与YAML格式互相转换")
                .category(devCategory)
                .icon("FileJson")
                .isVip(false)
                .isActive(true)
                .visitsCount(45678L)
                .rating(BigDecimal.valueOf(4.6))
                .reviewCount(128)
                .tags(new HashSet<>(Arrays.asList(devTag)))
                .build();

        Tool codeBeautify = Tool.builder()
                .code("code-beautify")
                .name("代码美化")
                .description("HTML/CSS/JavaScript代码格式化")
                .category(devCategory)
                .icon("Code2")
                .isVip(true)
                .isActive(true)
                .visitsCount(89321L)
                .rating(BigDecimal.valueOf(4.7))
                .reviewCount(256)
                .tags(new HashSet<>(Arrays.asList(vipTag)))
                .build();

        Tool regexTester = Tool.builder()
                .code("regex-tester")
                .name("正则测试")
                .description("在线正则表达式测试工具")
                .category(devCategory)
                .icon("Search")
                .isVip(false)
                .isActive(true)
                .visitsCount(67890L)
                .rating(BigDecimal.valueOf(4.5))
                .reviewCount(189)
                .tags(new HashSet<>())
                .build();

        Tool timestampConverter = Tool.builder()
                .code("timestamp-converter")
                .name("时间戳转换")
                .description("Unix时间戳与日期时间互转")
                .category(devCategory)
                .icon("Clock")
                .isVip(false)
                .isActive(true)
                .visitsCount(98765L)
                .rating(BigDecimal.valueOf(4.9))
                .reviewCount(412)
                .tags(new HashSet<>(Arrays.asList(hotTag)))
                .build();

        Tool base64Codec = Tool.builder()
                .code("base64-codec")
                .name("Base64编解码")
                .description("Base64编码和解码工具")
                .category(devCategory)
                .icon("Binary")
                .isVip(false)
                .isActive(true)
                .visitsCount(76543L)
                .rating(BigDecimal.valueOf(4.6))
                .reviewCount(198)
                .tags(new HashSet<>())
                .build();

        // 图片工具
        Tool imageCompress = Tool.builder()
                .code("image-compress")
                .name("图片压缩")
                .description("在线图片压缩，支持JPG/PNG/GIF")
                .category(imageCategory)
                .icon("ImageMinus")
                .isVip(false)
                .isActive(true)
                .visitsCount(156789L)
                .rating(BigDecimal.valueOf(4.7))
                .reviewCount(523)
                .tags(new HashSet<>(Arrays.asList(hotTag, commonTag)))
                .build();

        Tool imageConvert = Tool.builder()
                .code("image-convert")
                .name("图片格式转换")
                .description("图片格式互相转换")
                .category(imageCategory)
                .icon("ImagePlus")
                .isVip(true)
                .isActive(true)
                .visitsCount(87654L)
                .rating(BigDecimal.valueOf(4.5))
                .reviewCount(234)
                .tags(new HashSet<>(Arrays.asList(vipTag, imageTag)))
                .build();

        Tool imageToBase64 = Tool.builder()
                .code("image-to-base64")
                .name("图片转Base64")
                .description("图片转换为Base64编码")
                .category(imageCategory)
                .icon("Image")
                .isVip(false)
                .isActive(true)
                .visitsCount(65432L)
                .rating(BigDecimal.valueOf(4.4))
                .reviewCount(156)
                .tags(new HashSet<>(Arrays.asList(imageTag)))
                .build();

        // 文档工具
        Tool pdfToWord = Tool.builder()
                .code("pdf-to-word")
                .name("PDF转Word")
                .description("PDF文档转换为Word格式")
                .category(docCategory)
                .icon("FileText")
                .isVip(true)
                .isActive(true)
                .visitsCount(234567L)
                .rating(BigDecimal.valueOf(4.8))
                .reviewCount(892)
                .tags(new HashSet<>(Arrays.asList(hotTag, vipTag, docTag)))
                .build();

        Tool markdownEditor = Tool.builder()
                .code("markdown-editor")
                .name("Markdown编辑器")
                .description("在线Markdown编辑和预览")
                .category(docCategory)
                .icon("FileEdit")
                .isVip(false)
                .isActive(true)
                .visitsCount(45678L)
                .rating(BigDecimal.valueOf(4.6))
                .reviewCount(167)
                .tags(new HashSet<>(Arrays.asList(docTag)))
                .build();

        // 加密工具
        Tool md5Encrypt = Tool.builder()
                .code("md5-encrypt")
                .name("MD5加密")
                .description("MD5加密工具，支持32位/16位")
                .category(cryptoCategory)
                .icon("Hash")
                .isVip(false)
                .isActive(true)
                .visitsCount(112345L)
                .rating(BigDecimal.valueOf(4.7))
                .reviewCount(445)
                .tags(new HashSet<>(Arrays.asList(hotTag, commonTag)))
                .build();

        Tool urlEncode = Tool.builder()
                .code("url-encode")
                .name("URL编解码")
                .description("URL编码和解码工具")
                .category(cryptoCategory)
                .icon("Link")
                .isVip(false)
                .isActive(true)
                .visitsCount(56789L)
                .rating(BigDecimal.valueOf(4.5))
                .reviewCount(223)
                .tags(new HashSet<>())
                .build();

        // 文本工具
        Tool textCompare = Tool.builder()
                .code("text-compare")
                .name("文本对比")
                .description("文本差异对比工具")
                .category(textCategory)
                .icon("GitCompare")
                .isVip(false)
                .isActive(true)
                .visitsCount(34567L)
                .rating(BigDecimal.valueOf(4.4))
                .reviewCount(98)
                .tags(new HashSet<>())
                .build();

        Tool wordCount = Tool.builder()
                .code("word-count")
                .name("字数统计")
                .description("统计文本字数、字符数、行数")
                .category(textCategory)
                .icon("Text")
                .isVip(false)
                .isActive(true)
                .visitsCount(78901L)
                .rating(BigDecimal.valueOf(4.6))
                .reviewCount(234)
                .tags(new HashSet<>(Arrays.asList(commonTag)))
                .build();

        // 数字工具
        Tool hexConverter = Tool.builder()
                .code("hex-converter")
                .name("进制转换")
                .description("二进制、八进制、十进制、十六进制互转")
                .category(numberCategory)
                .icon("Binary")
                .isVip(false)
                .isActive(true)
                .visitsCount(67890L)
                .rating(BigDecimal.valueOf(4.5))
                .reviewCount(156)
                .tags(new HashSet<>())
                .build();

        Tool randomNumber = Tool.builder()
                .code("random-number")
                .name("随机数生成")
                .description("生成随机数、随机密码")
                .category(numberCategory)
                .icon("Dices")
                .isVip(false)
                .isActive(true)
                .visitsCount(89012L)
                .rating(BigDecimal.valueOf(4.7))
                .reviewCount(267)
                .tags(new HashSet<>(Arrays.asList(hotTag)))
                .build();

        List<Tool> tools = Arrays.asList(
                jsonFormatter, jsonToYaml, codeBeautify, regexTester, timestampConverter, base64Codec,
                imageCompress, imageConvert, imageToBase64,
                pdfToWord, markdownEditor,
                md5Encrypt, urlEncode,
                textCompare, wordCount,
                hexConverter, randomNumber
        );

        toolRepository.saveAll(tools);
    }
}
