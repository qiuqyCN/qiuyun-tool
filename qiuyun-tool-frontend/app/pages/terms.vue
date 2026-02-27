<template>
  <div class="min-h-screen bg-background">
    <div class="mx-auto px-4 sm:px-6 lg:px-8 py-12">
      <div class="text-center mb-12">
        <div class="inline-flex items-center justify-center w-16 h-16 rounded-2xl bg-primary/10 mb-6">
          <FileText class="w-8 h-8 text-primary" />
        </div>
        <h1 class="text-3xl sm:text-4xl font-bold mb-4">使用条款</h1>
        <p class="text-muted-foreground">Terms of Service</p>
        <p class="text-sm text-muted-foreground mt-2">最后更新日期：2026年2月26日</p>
      </div>

      <div class="prose prose-sm max-w-none text-muted-foreground space-y-8">
        <div class="bg-card border border-border/40 rounded-xl p-6">
          <p class="text-base leading-relaxed">
            欢迎使用秋云工具！在使用我们的服务之前，请仔细阅读以下使用条款。通过访问或使用秋云工具，您表示同意接受这些条款的约束。如果您不同意这些条款，请不要使用我们的服务。
          </p>
        </div>

        <section>
          <div class="flex items-center gap-3 mb-4">
            <div class="w-10 h-10 rounded-lg bg-primary/10 flex items-center justify-center">
              <CheckCircle class="w-5 h-5 text-primary" />
            </div>
            <h2 class="text-xl font-bold text-foreground">一、服务接受</h2>
          </div>
          <div class="pl-13 space-y-3">
            <p>通过使用秋云工具，您确认并同意：</p>
            <ul class="list-disc list-inside space-y-2 ml-4">
              <li>您已达到所在司法管辖区规定的法定成年年龄，或已获得父母或监护人的同意使用本服务。</li>
              <li>您提供的所有信息都是真实、准确、完整和最新的。</li>
              <li>您将遵守所有适用的法律法规和本使用条款。</li>
              <li>您不会将服务用于任何非法或未经授权的目的。</li>
            </ul>
          </div>
        </section>

        <Separator />

        <section>
          <div class="flex items-center gap-3 mb-4">
            <div class="w-10 h-10 rounded-lg bg-primary/10 flex items-center justify-center">
              <User class="w-5 h-5 text-primary" />
            </div>
            <h2 class="text-xl font-bold text-foreground">二、账户注册与安全</h2>
          </div>
          <div class="pl-13 space-y-3">
            <p>部分功能可能需要您注册账户。您同意：</p>
            <ul class="list-disc list-inside space-y-2 ml-4">
              <li>对您的账户密码保密，并对账户下的所有活动负责。</li>
              <li>在发现任何未经授权使用账户的情况时立即通知我们。</li>
              <li>不得转让、出售或出借您的账户给他人使用。</li>
              <li>我们有权在合理怀疑账户被滥用时暂停或终止您的账户。</li>
            </ul>
          </div>
        </section>

        <Separator />

        <section>
          <div class="flex items-center gap-3 mb-4">
            <div class="w-10 h-10 rounded-lg bg-primary/10 flex items-center justify-center">
              <Ban class="w-5 h-5 text-primary" />
            </div>
            <h2 class="text-xl font-bold text-foreground">三、禁止行为</h2>
          </div>
          <div class="pl-13 space-y-3">
            <p>使用秋云工具时，您不得：</p>
            <ul class="list-disc list-inside space-y-2 ml-4">
              <li>违反任何适用的法律法规。</li>
              <li>侵犯他人的知识产权、隐私权或其他合法权益。</li>
              <li>上传、传播或存储任何恶意软件、病毒或其他有害代码。</li>
              <li>试图未经授权访问我们的系统或其他用户的账户。</li>
              <li>干扰或破坏服务的正常运行。</li>
              <li>使用自动化工具（如机器人、爬虫）大量访问服务。</li>
              <li>将服务用于任何欺诈、垃圾邮件或网络钓鱼活动。</li>
            </ul>
          </div>
        </section>

        <Separator />

        <section>
          <div class="flex items-center gap-3 mb-4">
            <div class="w-10 h-10 rounded-lg bg-primary/10 flex items-center justify-center">
              <Copyright class="w-5 h-5 text-primary" />
            </div>
            <h2 class="text-xl font-bold text-foreground">四、知识产权</h2>
          </div>
          <div class="pl-13 space-y-3">
            <p>关于知识产权，您需要了解：</p>
            <ul class="list-disc list-inside space-y-2 ml-4">
              <li>秋云工具及其所有相关内容（包括但不限于文字、图形、标志、图标、图像、音频、软件）均受版权、商标和其他知识产权法律保护。</li>
              <li>您仅获得使用服务的有限许可，不得复制、修改、分发或创建衍生作品。</li>
              <li>您保留您提交到服务的任何内容的知识产权，但授予我们使用、修改和展示该内容的许可，以便提供服务。</li>
              <li>我们尊重他人的知识产权，如果您认为您的权利受到侵犯，请联系我们。</li>
            </ul>
          </div>
        </section>

        <Separator />

        <section>
          <div class="flex items-center gap-3 mb-4">
            <div class="w-10 h-10 rounded-lg bg-primary/10 flex items-center justify-center">
              <AlertTriangle class="w-5 h-5 text-primary" />
            </div>
            <h2 class="text-xl font-bold text-foreground">五、免责声明</h2>
          </div>
          <div class="pl-13 space-y-3">
            <p>在使用我们的服务时，请注意以下免责条款：</p>
            <ul class="list-disc list-inside space-y-2 ml-4">
              <li>服务按"现状"和"可用性"提供，我们不提供任何明示或暗示的保证。</li>
              <li>我们不保证服务将始终可用、无错误或安全。</li>
              <li>对于因使用或无法使用服务而导致的任何直接、间接、附带、特殊或后果性损害，我们不承担责任。</li>
              <li>您使用服务的风险由您自行承担。</li>
            </ul>
          </div>
        </section>

        <Separator />

        <section>
          <div class="flex items-center gap-3 mb-4">
            <div class="w-10 h-10 rounded-lg bg-primary/10 flex items-center justify-center">
              <RefreshCw class="w-5 h-5 text-primary" />
            </div>
            <h2 class="text-xl font-bold text-foreground">六、服务变更与终止</h2>
          </div>
          <div class="pl-13 space-y-3">
            <p>我们保留以下权利：</p>
            <ul class="list-disc list-inside space-y-2 ml-4">
              <li>随时修改、暂停或终止服务（或其任何部分），无论是否提前通知。</li>
              <li>在任何时候以任何理由终止或暂停您的账户访问权限。</li>
              <li>定期更新和改进服务功能。</li>
              <li>对服务进行维护，期间服务可能暂时不可用。</li>
            </ul>
          </div>
        </section>

        <Separator />

        <section>
          <div class="flex items-center gap-3 mb-4">
            <div class="w-10 h-10 rounded-lg bg-primary/10 flex items-center justify-center">
              <Edit3 class="w-5 h-5 text-primary" />
            </div>
            <h2 class="text-xl font-bold text-foreground">七、条款修改</h2>
          </div>
          <div class="pl-13 space-y-3">
            <p>我们可能会不时更新这些使用条款：</p>
            <ul class="list-disc list-inside space-y-2 ml-4">
              <li>修改后的条款将在网站上发布时生效。</li>
              <li>重大变更将通过网站公告或电子邮件通知您。</li>
              <li>继续使用服务即表示您接受修改后的条款。</li>
              <li>建议您定期查看本页面以了解最新条款。</li>
            </ul>
          </div>
        </section>

        <Separator />

        <section>
          <div class="flex items-center gap-3 mb-4">
            <div class="w-10 h-10 rounded-lg bg-primary/10 flex items-center justify-center">
              <Scale class="w-5 h-5 text-primary" />
            </div>
            <h2 class="text-xl font-bold text-foreground">八、适用法律</h2>
          </div>
          <div class="pl-13 space-y-3">
            <p>本使用条款受中华人民共和国法律管辖。因本条款引起的或与本条款相关的任何争议，应首先通过友好协商解决。协商不成的，任何一方均可向有管辖权的人民法院提起诉讼。</p>
          </div>
        </section>

        <Separator />

        <section>
          <div class="flex items-center gap-3 mb-4">
            <div class="w-10 h-10 rounded-lg bg-primary/10 flex items-center justify-center">
              <MessageCircle class="w-5 h-5 text-primary" />
            </div>
            <h2 class="text-xl font-bold text-foreground">九、联系我们</h2>
          </div>
          <div class="pl-13 space-y-3">
            <p>如果您对这些使用条款有任何疑问，请通过以下方式联系我们：</p>
            <div class="bg-muted/50 rounded-lg p-4 mt-4">
              <p class="flex items-center gap-2">
                <Mail class="w-4 h-4 text-primary" />
                <span>邮箱：97208294@qq.com</span>
              </p>
            </div>
          </div>
        </section>
      </div>

      <div class="mt-12 pt-8 border-t border-border/40 text-center">
        <p class="text-sm text-muted-foreground">
          感谢您仔细阅读我们的使用条款。祝您使用愉快！
        </p>
        <div class="mt-6 flex justify-center gap-4">
          <NuxtLink to="/privacy">
            <Button variant="outline" size="sm">
              <Shield class="w-4 h-4 mr-2" />
              隐私政策
            </Button>
          </NuxtLink>
          <NuxtLink to="/contact">
            <Button variant="outline" size="sm">
              <MessageCircle class="w-4 h-4 mr-2" />
              联系我们
            </Button>
          </NuxtLink>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { FileText, CheckCircle, User, Ban, Copyright, AlertTriangle, RefreshCw, Edit3, Scale, MessageCircle, Mail, Shield } from 'lucide-vue-next'

useHead({
  title: '使用条款 - 秋云工具',
  meta: [
    { name: 'description', content: '秋云工具使用条款，请仔细阅读并遵守相关规定。' }
  ]
})
</script>