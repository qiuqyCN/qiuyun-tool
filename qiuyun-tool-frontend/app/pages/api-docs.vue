<template>
  <div class="min-h-screen bg-background">
    <div class="max-w-6xl mx-auto px-4 sm:px-6 lg:px-8 py-12">
      <div class="text-center mb-12">
        <div class="inline-flex items-center justify-center w-16 h-16 rounded-2xl bg-primary/10 mb-6">
          <Code2 class="w-8 h-8 text-primary" />
        </div>
        <h1 class="text-3xl sm:text-4xl font-bold mb-4">API 文档</h1>
        <p class="text-muted-foreground">秋云工具 API 接口文档，帮助开发者集成工具服务</p>
      </div>

      <div class="grid grid-cols-1 lg:grid-cols-4 gap-8">
        <div class="lg:col-span-1">
          <div class="sticky top-24 space-y-6">
            <Card>
              <CardHeader>
                <CardTitle class="text-base">文档目录</CardTitle>
              </CardHeader>
              <CardContent class="p-0">
                <nav class="flex flex-col">
                  <button
                    v-for="section in sections"
                    :key="section.id"
                    class="px-4 py-2 text-left text-sm hover:bg-muted transition-colors"
                    :class="{ 'bg-primary/10 text-primary font-medium': activeSection === section.id }"
                    @click="scrollToSection(section.id)"
                  >
                    {{ section.title }}
                  </button>
                </nav>
              </CardContent>
            </Card>

            <Card>
              <CardHeader>
                <CardTitle class="text-base">API 状态</CardTitle>
              </CardHeader>
              <CardContent class="space-y-3">
                <div class="flex items-center justify-between">
                  <span class="text-sm">服务状态</span>
                  <Badge variant="success" class="bg-green-500/10 text-green-500 hover:bg-green-500/20">
                    <span class="w-2 h-2 rounded-full bg-green-500 mr-1"></span>
                    正常运行
                  </Badge>
                </div>
                <div class="flex items-center justify-between">
                  <span class="text-sm">API 版本</span>
                  <Badge variant="secondary">v1.0</Badge>
                </div>
                <div class="flex items-center justify-between">
                  <span class="text-sm">响应时间</span>
                  <span class="text-sm text-muted-foreground">&lt; 100ms</span>
                </div>
              </CardContent>
            </Card>

            <div class="bg-linear-to-br from-primary/5 to-primary/10 rounded-xl p-6">
              <h3 class="font-semibold mb-2">需要帮助？</h3>
              <p class="text-sm text-muted-foreground mb-4">如果在集成过程中遇到问题，欢迎联系我们</p>
              <NuxtLink to="/contact">
                <Button variant="outline" class="w-full" size="sm">
                  <MessageCircle class="w-4 h-4 mr-2" />
                  联系支持
                </Button>
              </NuxtLink>
            </div>
          </div>
        </div>

        <div class="lg:col-span-3 space-y-12">
          <section id="overview" class="scroll-mt-24">
            <h2 class="text-2xl font-bold mb-4">概述</h2>
            <div class="prose prose-sm max-w-none text-muted-foreground space-y-4">
              <p>
                秋云工具 API 提供了一系列实用的开发者工具接口，您可以通过简单的 HTTP 请求调用各种工具功能。
                所有 API 都支持跨域访问（CORS），方便您在前端应用中直接调用。
              </p>
              <div class="bg-muted rounded-lg p-4">
                <p class="font-medium text-foreground mb-2">基础信息</p>
                <ul class="space-y-1">
                  <li><strong>Base URL:</strong> <code>https://api.qiuyuntool.com/v1</code></li>
                  <li><strong>协议:</strong> HTTPS</li>
                  <li><strong>数据格式:</strong> JSON</li>
                  <li><strong>字符编码:</strong> UTF-8</li>
                </ul>
              </div>
            </div>
          </section>

          <Separator />

          <section id="auth" class="scroll-mt-24">
            <h2 class="text-2xl font-bold mb-4">认证</h2>
            <div class="prose prose-sm max-w-none text-muted-foreground space-y-4">
              <p>
                所有 API 请求都需要在请求头中包含 API Key 进行认证。您可以在个人中心的"API 管理"页面获取您的 API Key。
              </p>
              <div class="bg-muted rounded-lg p-4">
                <p class="font-medium text-foreground mb-2">请求头格式</p>
                <pre class="text-sm overflow-x-auto"><code>Authorization: Bearer YOUR_API_KEY</code></pre>
              </div>
              <Alert>
                <AlertCircle class="w-4 h-4" />
                <AlertTitle>安全提示</AlertTitle>
                <AlertDescription>
                  请勿在客户端代码中暴露您的 API Key。建议在服务器端调用 API，或使用环境变量存储密钥。
                </AlertDescription>
              </Alert>
            </div>
          </section>

          <Separator />

          <section id="errors" class="scroll-mt-24">
            <h2 class="text-2xl font-bold mb-4">错误处理</h2>
            <div class="prose prose-sm max-w-none text-muted-foreground space-y-4">
              <p>API 使用标准的 HTTP 状态码表示请求结果：</p>
              <div class="overflow-x-auto">
                <table class="w-full text-sm">
                  <thead>
                    <tr class="border-b">
                      <th class="text-left py-2 px-4">状态码</th>
                      <th class="text-left py-2 px-4">含义</th>
                      <th class="text-left py-2 px-4">说明</th>
                    </tr>
                  </thead>
                  <tbody>
                    <tr class="border-b">
                      <td class="py-2 px-4"><code>200</code></td>
                      <td class="py-2 px-4">成功</td>
                      <td class="py-2 px-4">请求成功完成</td>
                    </tr>
                    <tr class="border-b">
                      <td class="py-2 px-4"><code>400</code></td>
                      <td class="py-2 px-4">请求参数错误</td>
                      <td class="py-2 px-4">请求参数格式不正确或缺少必要参数</td>
                    </tr>
                    <tr class="border-b">
                      <td class="py-2 px-4"><code>401</code></td>
                      <td class="py-2 px-4">未授权</td>
                      <td class="py-2 px-4">API Key 无效或已过期</td>
                    </tr>
                    <tr class="border-b">
                      <td class="py-2 px-4"><code>403</code></td>
                      <td class="py-2 px-4">禁止访问</td>
                      <td class="py-2 px-4">没有权限访问该资源</td>
                    </tr>
                    <tr class="border-b">
                      <td class="py-2 px-4"><code>429</code></td>
                      <td class="py-2 px-4">请求过多</td>
                      <td class="py-2 px-4">超出 API 调用频率限制</td>
                    </tr>
                    <tr>
                      <td class="py-2 px-4"><code>500</code></td>
                      <td class="py-2 px-4">服务器错误</td>
                      <td class="py-2 px-4">服务器内部错误，请稍后重试</td>
                    </tr>
                  </tbody>
                </table>
              </div>
              <p>错误响应格式：</p>
              <pre class="bg-muted rounded-lg p-4 text-sm overflow-x-auto"><code>{
  "error": {
    "code": "INVALID_PARAMETER",
    "message": "请求参数格式不正确",
    "details": "JSON 格式解析失败"
  }
}</code></pre>
            </div>
          </section>

          <Separator />

          <section id="rate-limit" class="scroll-mt-24">
            <h2 class="text-2xl font-bold mb-4">频率限制</h2>
            <div class="prose prose-sm max-w-none text-muted-foreground space-y-4">
              <p>为了保证服务质量，我们对 API 调用进行了频率限制：</p>
              <div class="overflow-x-auto">
                <table class="w-full text-sm">
                  <thead>
                    <tr class="border-b">
                      <th class="text-left py-2 px-4">套餐</th>
                      <th class="text-left py-2 px-4">每分钟限制</th>
                      <th class="text-left py-2 px-4">每日限制</th>
                    </tr>
                  </thead>
                  <tbody>
                    <tr class="border-b">
                      <td class="py-2 px-4">免费版</td>
                      <td class="py-2 px-4">10 次</td>
                      <td class="py-2 px-4">100 次</td>
                    </tr>
                    <tr class="border-b">
                      <td class="py-2 px-4">专业版</td>
                      <td class="py-2 px-4">100 次</td>
                      <td class="py-2 px-4">10,000 次</td>
                    </tr>
                    <tr>
                      <td class="py-2 px-4">企业版</td>
                      <td class="py-2 px-4">1,000 次</td>
                      <td class="py-2 px-4">100,000 次</td>
                    </tr>
                  </tbody>
                </table>
              </div>
              <p>响应头中包含当前限流信息：</p>
              <pre class="bg-muted rounded-lg p-4 text-sm overflow-x-auto"><code>X-RateLimit-Limit: 100
X-RateLimit-Remaining: 95
X-RateLimit-Reset: 1640995200</code></pre>
            </div>
          </section>

          <Separator />

          <section id="endpoints" class="scroll-mt-24">
            <h2 class="text-2xl font-bold mb-4">API 端点</h2>

            <div class="space-y-8">
              <div class="border border-border rounded-lg overflow-hidden">
                <div class="bg-muted px-4 py-3 flex items-center gap-3">
                  <Badge class="bg-green-500 text-white">POST</Badge>
                  <code class="text-sm">/tools/json/format</code>
                </div>
                <div class="p-4 space-y-4">
                  <p class="text-sm text-muted-foreground">格式化 JSON 数据</p>

                  <div>
                    <p class="text-sm font-medium mb-2">请求参数</p>
                    <pre class="bg-muted rounded-lg p-3 text-sm overflow-x-auto"><code>{
  "json": "string",      // 必填，要格式化的 JSON 字符串
  "indent": 2            // 可选，缩进空格数，默认 2
}</code></pre>
                  </div>

                  <div>
                    <p class="text-sm font-medium mb-2">响应示例</p>
                    <pre class="bg-muted rounded-lg p-3 text-sm overflow-x-auto"><code>{
  "success": true,
  "data": {
    "formatted": "{\n  \"name\": \"example\",\n  \"value\": 123\n}"
  }
}</code></pre>
                  </div>
                </div>
              </div>

              <div class="border border-border rounded-lg overflow-hidden">
                <div class="bg-muted px-4 py-3 flex items-center gap-3">
                  <Badge class="bg-green-500 text-white">POST</Badge>
                  <code class="text-sm">/tools/base64/encode</code>
                </div>
                <div class="p-4 space-y-4">
                  <p class="text-sm text-muted-foreground">Base64 编码</p>

                  <div>
                    <p class="text-sm font-medium mb-2">请求参数</p>
                    <pre class="bg-muted rounded-lg p-3 text-sm overflow-x-auto"><code>{
  "text": "string",      // 必填，要编码的文本
  "urlSafe": false       // 可选，是否使用 URL 安全编码
}</code></pre>
                  </div>

                  <div>
                    <p class="text-sm font-medium mb-2">响应示例</p>
                    <pre class="bg-muted rounded-lg p-3 text-sm overflow-x-auto"><code>{
  "success": true,
  "data": {
    "encoded": "SGVsbG8gV29ybGQ="
  }
}</code></pre>
                  </div>
                </div>
              </div>

              <div class="border border-border rounded-lg overflow-hidden">
                <div class="bg-muted px-4 py-3 flex items-center gap-3">
                  <Badge class="bg-green-500 text-white">POST</Badge>
                  <code class="text-sm">/tools/base64/decode</code>
                </div>
                <div class="p-4 space-y-4">
                  <p class="text-sm text-muted-foreground">Base64 解码</p>

                  <div>
                    <p class="text-sm font-medium mb-2">请求参数</p>
                    <pre class="bg-muted rounded-lg p-3 text-sm overflow-x-auto"><code>{
  "text": "string"       // 必填，要解码的 Base64 字符串
}</code></pre>
                  </div>

                  <div>
                    <p class="text-sm font-medium mb-2">响应示例</p>
                    <pre class="bg-muted rounded-lg p-3 text-sm overflow-x-auto"><code>{
  "success": true,
  "data": {
    "decoded": "Hello World"
  }
}</code></pre>
                  </div>
                </div>
              </div>

              <div class="border border-border rounded-lg overflow-hidden">
                <div class="bg-muted px-4 py-3 flex items-center gap-3">
                  <Badge class="bg-green-500 text-white">POST</Badge>
                  <code class="text-sm">/tools/url/encode</code>
                </div>
                <div class="p-4 space-y-4">
                  <p class="text-sm text-muted-foreground">URL 编码</p>

                  <div>
                    <p class="text-sm font-medium mb-2">请求参数</p>
                    <pre class="bg-muted rounded-lg p-3 text-sm overflow-x-auto"><code>{
  "text": "string",      // 必填，要编码的文本
  "component": true      // 可选，是否编码为组件格式
}</code></pre>
                  </div>

                  <div>
                    <p class="text-sm font-medium mb-2">响应示例</p>
                    <pre class="bg-muted rounded-lg p-3 text-sm overflow-x-auto"><code>{
  "success": true,
  "data": {
    "encoded": "Hello%20World"
  }
}</code></pre>
                  </div>
                </div>
              </div>

              <div class="border border-border rounded-lg overflow-hidden">
                <div class="bg-muted px-4 py-3 flex items-center gap-3">
                  <Badge class="bg-green-500 text-white">POST</Badge>
                  <code class="text-sm">/tools/hash/md5</code>
                </div>
                <div class="p-4 space-y-4">
                  <p class="text-sm text-muted-foreground">MD5 哈希计算</p>

                  <div>
                    <p class="text-sm font-medium mb-2">请求参数</p>
                    <pre class="bg-muted rounded-lg p-3 text-sm overflow-x-auto"><code>{
  "text": "string",      // 必填，要计算哈希的文本
  "uppercase": false    // 可选，结果是否大写
}</code></pre>
                  </div>

                  <div>
                    <p class="text-sm font-medium mb-2">响应示例</p>
                    <pre class="bg-muted rounded-lg p-3 text-sm overflow-x-auto"><code>{
  "success": true,
  "data": {
    "hash": "5d41402abc4b2a76b9719d911017c592"
  }
}</code></pre>
                  </div>
                </div>
              </div>

              <div class="border border-border rounded-lg overflow-hidden">
                <div class="bg-muted px-4 py-3 flex items-center gap-3">
                  <Badge class="bg-blue-500 text-white">GET</Badge>
                  <code class="text-sm">/tools/list</code>
                </div>
                <div class="p-4 space-y-4">
                  <p class="text-sm text-muted-foreground">获取工具列表</p>

                  <div>
                    <p class="text-sm font-medium mb-2">查询参数</p>
                    <pre class="bg-muted rounded-lg p-3 text-sm overflow-x-auto"><code>?category=string    // 可选，按分类筛选
?page=1            // 可选，页码，默认 1
?limit=20         // 可选，每页数量，默认 20</code></pre>
                  </div>

                  <div>
                    <p class="text-sm font-medium mb-2">响应示例</p>
                    <pre class="bg-muted rounded-lg p-3 text-sm overflow-x-auto"><code>{
  "success": true,
  "data": {
    "tools": [
      {
        "id": "json-formatter",
        "name": "JSON格式化",
        "description": "格式化和验证JSON数据",
        "category": "formatter",
        "endpoint": "/tools/json/format"
      }
    ],
    "pagination": {
      "page": 1,
      "limit": 20,
      "total": 50
    }
  }
}</code></pre>
                  </div>
                </div>
              </div>
            </div>
          </section>

          <Separator />

          <section id="sdk" class="scroll-mt-24">
            <h2 class="text-2xl font-bold mb-4">SDK</h2>
            <div class="prose prose-sm max-w-none text-muted-foreground space-y-4">
              <p>我们提供多种语言的 SDK，方便您快速集成：</p>

              <Tabs default-value="javascript" class="w-full">
                <TabsList class="grid w-full grid-cols-3">
                  <TabsTrigger value="javascript">JavaScript</TabsTrigger>
                  <TabsTrigger value="python">Python</TabsTrigger>
                  <TabsTrigger value="go">Go</TabsTrigger>
                </TabsList>
                <TabsContent value="javascript">
                  <pre class="bg-muted rounded-lg p-4 text-sm overflow-x-auto"><code>// 安装
npm install @qiuyuntool/sdk

// 使用
import { QiuYunTool } from '@qiuyuntool/sdk';

const client = new QiuYunTool({
  apiKey: 'your-api-key'
});

// JSON 格式化
const result = await client.json.format({
  json: '{"name":"test"}'
});

console.log(result.data.formatted);</code></pre>
                </TabsContent>
                <TabsContent value="python">
                  <pre class="bg-muted rounded-lg p-4 text-sm overflow-x-auto"><code># 安装
pip install qiuyuntool

# 使用
from qiuyuntool import QiuYunTool

client = QiuYunTool(api_key='your-api-key')

# JSON 格式化
result = client.json.format(json='{"name":"test"}')

print(result['data']['formatted'])</code></pre>
                </TabsContent>
                <TabsContent value="go">
                  <pre class="bg-muted rounded-lg p-4 text-sm overflow-x-auto"><code>// 安装
go get github.com/qiuyuntool/sdk-go

// 使用
package main

import (
    "context"
    "fmt"
    "github.com/qiuyuntool/sdk-go"
)

func main() {
    client := qiuyuntool.NewClient("your-api-key")
    
    // JSON 格式化
    result, _ := client.JSON.Format(context.Background(), qiuyuntool.JSONFormatRequest{
        JSON: `{"name":"test"}`,
    })
    
    fmt.Println(result.Data.Formatted)
}</code></pre>
                </TabsContent>
              </Tabs>
            </div>
          </section>

          <Separator />

          <section id="changelog" class="scroll-mt-24">
            <h2 class="text-2xl font-bold mb-4">更新日志</h2>
            <div class="space-y-6">
              <div class="border-l-2 border-primary pl-4">
                <div class="flex items-center gap-2 mb-2">
                  <Badge>v1.0.0</Badge>
                  <span class="text-sm text-muted-foreground">2025-01-15</span>
                </div>
                <ul class="text-sm text-muted-foreground space-y-1">
                  <li>• 正式发布 API v1.0</li>
                  <li>• 支持 JSON 格式化、Base64 编解码、URL 编解码、MD5 哈希等工具</li>
                  <li>• 提供 JavaScript、Python、Go SDK</li>
                </ul>
              </div>

              <div class="border-l-2 border-muted pl-4">
                <div class="flex items-center gap-2 mb-2">
                  <Badge variant="secondary">v0.9.0</Badge>
                  <span class="text-sm text-muted-foreground">2024-12-20</span>
                </div>
                <ul class="text-sm text-muted-foreground space-y-1">
                  <li>• Beta 版本发布</li>
                  <li>• 基础工具接口上线</li>
                </ul>
              </div>
            </div>
          </section>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import {
  Code2,
  AlertCircle,
  MessageCircle
} from 'lucide-vue-next'

useHead({
  title: 'API 文档 - 秋云工具',
  meta: [
    { name: 'description', content: '秋云工具 API 接口文档，帮助开发者集成工具服务。' }
  ]
})

const activeSection = ref('overview')

const sections = [
  { id: 'overview', title: '概述' },
  { id: 'auth', title: '认证' },
  { id: 'errors', title: '错误处理' },
  { id: 'rate-limit', title: '频率限制' },
  { id: 'endpoints', title: 'API 端点' },
  { id: 'sdk', title: 'SDK' },
  { id: 'changelog', title: '更新日志' }
]

const scrollToSection = (id: string) => {
  activeSection.value = id
  const element = document.getElementById(id)
  if (element) {
    element.scrollIntoView({ behavior: 'smooth' })
  }
}
</script>
