<script setup lang="ts">
import { ChevronRight, Target, Users, Zap, Heart } from 'lucide-vue-next'

useHead({
  title: '关于我们 - 秋云工具',
  meta: [
    { name: 'description', content: '了解秋云工具的使命、愿景和团队，我们致力于为开发者提供高效便捷的工具服务。' }
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
          <span class="text-foreground">关于我们</span>
        </div>
        <h1 class="text-4xl font-bold text-foreground mb-4">关于秋云工具</h1>
        <p class="text-xl text-muted-foreground max-w-2xl">
          我们致力于为开发者提供高效、便捷的工具服务，让开发工作更加轻松愉快。
        </p>
      </div>
    </div>

    <div class="container mx-auto px-4 py-12">
      <!-- Mission & Vision -->
      <div class="grid grid-cols-1 md:grid-cols-2 gap-8 mb-16">
        <div class="border border-border/40 rounded-xl p-8">
          <div class="w-12 h-12 rounded-lg bg-primary/10 flex items-center justify-center mb-4">
            <Target class="w-6 h-6 text-primary" />
          </div>
          <h2 class="text-2xl font-bold mb-4">我们的使命</h2>
          <p class="text-muted-foreground leading-relaxed">
            秋云工具的使命是为开发者和程序员提供一站式的在线工具平台。我们深知开发过程中会遇到各种繁琐的格式转换、编码解码、数据处理等需求，因此我们精心打造了这些工具，帮助开发者节省时间，提高工作效率。
          </p>
        </div>
        <div class="border border-border/40 rounded-xl p-8">
          <div class="w-12 h-12 rounded-lg bg-primary/10 flex items-center justify-center mb-4">
            <Zap class="w-6 h-6 text-primary" />
          </div>
          <h2 class="text-2xl font-bold mb-4">我们的愿景</h2>
          <p class="text-muted-foreground leading-relaxed">
            成为国内领先的开发者工具平台，为百万开发者提供优质的工具服务。我们希望通过持续创新和优化，让每一位开发者都能享受到高效、便捷的工具服务，从而专注于更有价值的创造性工作。
          </p>
        </div>
      </div>

      <!-- Core Values -->
      <div class="mb-16">
        <h2 class="text-2xl font-bold text-center mb-8">核心价值观</h2>
        <div class="grid grid-cols-1 md:grid-cols-3 gap-6">
          <div class="text-center p-6">
            <div class="w-16 h-16 rounded-2xl bg-linear-to-br from-primary/20 to-primary/10 flex items-center justify-center mx-auto mb-4">
              <Zap class="w-8 h-8 text-primary" />
            </div>
            <h3 class="text-lg font-semibold mb-2">高效便捷</h3>
            <p class="text-sm text-muted-foreground">
              工具即开即用，无需安装，让开发工作更加高效
            </p>
          </div>
          <div class="text-center p-6">
            <div class="w-16 h-16 rounded-2xl bg-linear-to-br from-primary/20 to-primary/10 flex items-center justify-center mx-auto mb-4">
              <Heart class="w-8 h-8 text-primary" />
            </div>
            <h3 class="text-lg font-semibold mb-2">用户至上</h3>
            <p class="text-sm text-muted-foreground">
              以用户需求为导向，持续优化产品体验
            </p>
          </div>
          <div class="text-center p-6">
            <div class="w-16 h-16 rounded-2xl bg-linear-to-br from-primary/20 to-primary/10 flex items-center justify-center mx-auto mb-4">
              <Users class="w-8 h-8 text-primary" />
            </div>
            <h3 class="text-lg font-semibold mb-2">开放共享</h3>
            <p class="text-sm text-muted-foreground">
              免费使用基础功能，让工具服务惠及更多开发者
            </p>
          </div>
        </div>
      </div>

      <!-- Stats -->
      <div class="border border-border/40 rounded-xl p-8 mb-16">
        <div class="grid grid-cols-2 md:grid-cols-4 gap-8 text-center">
          <div>
            <div class="text-3xl font-bold text-primary mb-2">50+</div>
            <div class="text-sm text-muted-foreground">在线工具</div>
          </div>
          <div>
            <div class="text-3xl font-bold text-primary mb-2">100万+</div>
            <div class="text-sm text-muted-foreground">月活跃用户</div>
          </div>
          <div>
            <div class="text-3xl font-bold text-primary mb-2">1000万+</div>
            <div class="text-sm text-muted-foreground">月处理请求</div>
          </div>
          <div>
            <div class="text-3xl font-bold text-primary mb-2">99.9%</div>
            <div class="text-sm text-muted-foreground">服务可用性</div>
          </div>
        </div>
      </div>

      <!-- Team -->
      <div class="mb-16">
        <h2 class="text-2xl font-bold text-center mb-8">团队介绍</h2>
        <p class="text-center text-muted-foreground max-w-2xl mx-auto mb-8">
          秋云工具由一群热爱技术的开发者创立，团队成员来自知名互联网公司，拥有丰富的产品研发经验。
        </p>
        <div class="flex justify-center">
          <div class="text-center">
            <div class="w-24 h-24 rounded-full bg-linear-to-br from-primary/20 to-primary/10 flex items-center justify-center mx-auto mb-4">
              <Users class="w-12 h-12 text-primary" />
            </div>
            <h3 class="font-semibold">专业技术团队</h3>
            <p class="text-sm text-muted-foreground">10+ 年行业经验</p>
          </div>
        </div>
      </div>

      <!-- Contact CTA -->
      <div class="text-center">
        <h2 class="text-2xl font-bold mb-4">加入我们</h2>
        <p class="text-muted-foreground mb-6">
          如果你也热爱技术，想要为开发者社区做出贡献，欢迎联系我们
        </p>
        <NuxtLink to="/contact">
          <Button size="lg">联系我们</Button>
        </NuxtLink>
      </div>
    </div>
  </div>
</template>
