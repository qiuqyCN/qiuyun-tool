<script setup lang="ts">
import { ChevronRight, Shield, Eye, Lock, Database, Share2, Trash2, Cookie, RefreshCw, MessageCircle } from 'lucide-vue-next'

useHead({
  title: '隐私政策 - 秋云工具',
  meta: [
    { name: 'description', content: '了解秋云工具如何收集、使用和保护您的个人信息。' }
  ]
})
</script>

<template>
  <div class="min-h-screen bg-background">
    <!-- Hero Section -->
    <div class="border-b border-border/40 bg-muted/30">
      <div class="container mx-auto px-4 py-16">
        <div class="flex items-center gap-2 text-sm text-muted-foreground mb-4">
          <NuxtLink to="/" class="hover:text-foreground">首页</NuxtLink>
          <ChevronRight class="w-4 h-4" />
          <span class="text-foreground">隐私政策</span>
        </div>
        <h1 class="text-4xl font-bold text-foreground mb-4">隐私政策</h1>
        <p class="text-xl text-muted-foreground max-w-2xl">
          我们非常重视您的隐私保护，本政策将帮助您了解我们如何收集、使用和保护您的信息
        </p>
        <p class="text-sm text-muted-foreground mt-4">最后更新日期：2026年2月26日</p>
      </div>
    </div>

    <div class="container mx-auto px-4 py-12">
      <div class="mx-auto">
        <!-- Introduction -->
        <div class="prose prose-sm max-w-none text-muted-foreground mb-12">
          <p>
            秋云工具（以下简称"我们"或"本平台"）非常重视用户的隐私和个人信息保护。本隐私政策旨在向您说明我们如何收集、使用、存储、共享和保护您的个人信息。请您在使用我们的服务前，仔细阅读并了解本隐私政策。
          </p>
        </div>

        <!-- Sections -->
        <div class="space-y-12">
          <!-- Section 1 -->
          <section>
            <div class="flex items-center gap-3 mb-4 mt-8">
              <div class="w-10 h-10 rounded-lg bg-primary/10 flex items-center justify-center">
                <Database class="w-5 h-5 text-primary" />
              </div>
              <h2 class="text-xl font-bold">一、信息收集</h2>
            </div>
            <div class="pl-13 prose prose-sm max-w-none text-muted-foreground">
              <p>我们可能收集以下类型的信息：</p>
              <ul>
                <li><strong>账户信息</strong>：当您注册账户时，我们会收集您的用户名、电子邮箱地址和密码。</li>
                <li><strong>使用数据</strong>：我们会记录您使用工具的情况，包括访问时间、使用的工具类型、操作记录等。</li>
                <li><strong>设备信息</strong>：我们可能会收集您的设备类型、操作系统、浏览器类型等技术信息。</li>
                <li><strong>日志信息</strong>：包括IP地址、访问时间、请求内容等服务器日志信息。</li>
              </ul>
            </div>
          </section>

          <!-- Section 2 -->
          <section>
            <div class="flex items-center gap-3 mb-4 mt-8">
              <div class="w-10 h-10 rounded-lg bg-primary/10 flex items-center justify-center">
                <Eye class="w-5 h-5 text-primary" />
              </div>
              <h2 class="text-xl font-bold">二、信息使用</h2>
            </div>
            <div class="pl-13 prose prose-sm max-w-none text-muted-foreground">
              <p>我们使用收集的信息用于以下目的：</p>
              <ul>
                <li>提供、维护和改进我们的服务</li>
                <li>处理您的请求和反馈</li>
                <li>发送服务通知和更新信息</li>
                <li>分析使用趋势，优化用户体验</li>
                <li>防止欺诈和滥用行为</li>
                <li>遵守法律法规要求</li>
              </ul>
            </div>
          </section>

          <!-- Section 3 -->
          <section>
            <div class="flex items-center gap-3 mb-4 mt-8">
              <div class="w-10 h-10 rounded-lg bg-primary/10 flex items-center justify-center">
                <Lock class="w-5 h-5 text-primary" />
              </div>
              <h2 class="text-xl font-bold">三、信息保护</h2>
            </div>
            <div class="pl-13 prose prose-sm max-w-none text-muted-foreground">
              <p>我们采取多种安全措施保护您的个人信息：</p>
              <ul>
                <li>使用加密技术保护数据传输安全</li>
                <li>实施严格的访问控制，限制内部人员访问权限</li>
                <li>定期进行安全审计和漏洞扫描</li>
                <li>对员工进行隐私保护培训</li>
              </ul>
              <p class="mt-4">
                尽管我们采取了合理的安全措施，但互联网传输无法保证100%安全。我们建议您妥善保管账户密码，定期更换密码。
              </p>
            </div>
          </section>

          <!-- Section 4 -->
          <section>
            <div class="flex items-center gap-3 mb-4 mt-8">
              <div class="w-10 h-10 rounded-lg bg-primary/10 flex items-center justify-center">
                <Share2 class="w-5 h-5 text-primary" />
              </div>
              <h2 class="text-xl font-bold">四、信息共享</h2>
            </div>
            <div class="pl-13 prose prose-sm max-w-none text-muted-foreground">
              <p>我们不会出售、出租或以其他方式向第三方披露您的个人信息，除非：</p>
              <ul>
                <li>获得您的明确同意</li>
                <li>为提供您要求的服务而必须共享</li>
                <li>遵守法律法规或政府机关的合法要求</li>
                <li>保护我们的合法权益或用户安全</li>
              </ul>
            </div>
          </section>

          <!-- Section 5 -->
          <section>
            <div class="flex items-center gap-3 mb-4 mt-8">
              <div class="w-10 h-10 rounded-lg bg-primary/10 flex items-center justify-center">
                <Trash2 class="w-5 h-5 text-primary" />
              </div>
              <h2 class="text-xl font-bold">五、数据保留</h2>
            </div>
            <div class="pl-13 prose prose-sm max-w-none text-muted-foreground">
              <p>
                我们仅在实现本隐私政策所述目的所需的期限内保留您的个人信息。当您删除账户或要求我们删除信息时，我们将在合理期限内删除您的个人信息，除非法律法规另有规定。
              </p>
            </div>
          </section>

          <!-- Section 6 -->
          <section>
            <div class="flex items-center gap-3 mb-4 mt-8">
              <div class="w-10 h-10 rounded-lg bg-primary/10 flex items-center justify-center">
                <Shield class="w-5 h-5 text-primary" />
              </div>
              <h2 class="text-xl font-bold">六、您的权利</h2>
            </div>
            <div class="pl-13 prose prose-sm max-w-none text-muted-foreground">
              <p>根据适用的法律法规，您享有以下权利：</p>
              <ul>
                <li><strong>访问权</strong>：您有权访问我们持有的您的个人信息</li>
                <li><strong>更正权</strong>：您有权更正不准确的个人信息</li>
                <li><strong>删除权</strong>：您有权要求删除您的个人信息</li>
                <li><strong>限制处理权</strong>：在某些情况下，您有权限制我们对您个人信息的处理</li>
                <li><strong>数据可携带权</strong>：您有权以结构化、通用的格式获取您的个人信息</li>
              </ul>
            </div>
          </section>

          <!-- Section 7 -->
          <section>
            <div class="flex items-center gap-3 mb-4 mt-8">
              <div class="w-10 h-10 rounded-lg bg-primary/10 flex items-center justify-center">
                <Cookie class="w-5 h-5 text-primary" />
              </div>
              <h2 class="text-xl font-bold">七、Cookie 使用</h2>
            </div>
            <div class="pl-13 prose prose-sm max-w-none text-muted-foreground">
              <p>
                我们使用 Cookie 和类似技术来改善您的浏览体验、分析网站流量和个性化内容。您可以通过浏览器设置管理 Cookie 偏好，但禁用 Cookie 可能会影响某些功能的正常使用。
              </p>
            </div>
          </section>

          <!-- Section 8 -->
          <section>
            <div class="flex items-center gap-3 mb-4 mt-8">
              <div class="w-10 h-10 rounded-lg bg-primary/10 flex items-center justify-center">
                <RefreshCw class="w-5 h-5 text-primary" />
              </div>
              <h2 class="text-xl font-bold">八、政策更新</h2>
            </div>
            <div class="pl-13 prose prose-sm max-w-none text-muted-foreground">
              <p>
                我们可能会不时更新本隐私政策。更新后的政策将在本页面发布，重大变更我们会通过网站公告或邮件通知您。建议您定期查看本政策以了解最新信息。
              </p>
            </div>
          </section>

          <!-- Section 9 -->
          <section>
            <div class="flex items-center gap-3 mb-4 mt-8">
              <div class="w-10 h-10 rounded-lg bg-primary/10 flex items-center justify-center">
                <MessageCircle class="w-5 h-5 text-primary" />
              </div>
              <h2 class="text-xl font-bold">九、联系我们</h2>
            </div>
            <div class="pl-13 prose prose-sm max-w-none text-muted-foreground">
              <p>
                如果您对本隐私政策有任何疑问或建议，或希望行使您的权利，请通过以下方式联系我们：
              </p>
              <ul>
                <li>电子邮箱：97208294@qq.com</li>
              </ul>
            </div>
          </section>
        </div>
      </div>
    </div>
  </div>
</template>
