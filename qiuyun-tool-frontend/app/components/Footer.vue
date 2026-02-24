<script setup lang="ts">
import { 
  Wrench, 
  Github, 
  Twitter, 
  Mail,
  MessageCircle,
  Heart
} from 'lucide-vue-next'

const footerLinks = {
  tools: [
    { name: '开发工具', path: '/category/dev' },
    { name: '图片工具', path: '/category/image' },
    { name: '文档转换', path: '/category/document' },
    { name: '加密工具', path: '/category/crypto' },
  ],
  about: [
    { name: '关于我们', path: '/about' },
    { name: '联系我们', path: '/contact' },
    { name: '隐私政策', path: '/privacy' },
    { name: '使用条款', path: '/terms' },
  ],
  support: [
    { name: '帮助中心', path: '/help' },
    { name: '问题反馈', path: '/feedback' },
    { name: 'API文档', path: '/api' },
  ]
}

const showFeedbackModal = ref(false)
</script>

<template>
  <footer class="border-t border-border/40 bg-background">
    <div class="container mx-auto px-4 py-12">
      <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-5 gap-8">
        <!-- Brand -->
        <div class="lg:col-span-2">
          <NuxtLink to="/" class="flex items-center gap-2 mb-4">
            <div class="w-8 h-8 rounded-lg bg-primary flex items-center justify-center">
              <Wrench class="w-5 h-5 text-primary-foreground" />
            </div>
            <span class="text-xl font-bold text-foreground">秋云工具</span>
          </NuxtLink>
          <p class="text-sm text-muted-foreground mb-4 max-w-xs">
            程序员常用工具集合，提供开发工具、图片处理、文档转换、加密解密等实用功能。
          </p>
          <div class="flex items-center gap-4">
            <a href="#" class="text-muted-foreground hover:text-foreground transition-colors">
              <Github class="w-5 h-5" />
            </a>
            <a href="#" class="text-muted-foreground hover:text-foreground transition-colors">
              <Twitter class="w-5 h-5" />
            </a>
            <a href="#" class="text-muted-foreground hover:text-foreground transition-colors">
              <Mail class="w-5 h-5" />
            </a>
          </div>
        </div>

        <!-- Tools -->
        <div>
          <h3 class="font-semibold text-foreground mb-4">工具分类</h3>
          <ul class="space-y-2">
            <li v-for="link in footerLinks.tools" :key="link.path">
              <NuxtLink 
                :to="link.path" 
                class="text-sm text-muted-foreground hover:text-foreground transition-colors"
              >
                {{ link.name }}
              </NuxtLink>
            </li>
          </ul>
        </div>

        <!-- About -->
        <div>
          <h3 class="font-semibold text-foreground mb-4">关于</h3>
          <ul class="space-y-2">
            <li v-for="link in footerLinks.about" :key="link.path">
              <NuxtLink 
                :to="link.path" 
                class="text-sm text-muted-foreground hover:text-foreground transition-colors"
              >
                {{ link.name }}
              </NuxtLink>
            </li>
          </ul>
        </div>

        <!-- Support -->
        <div>
          <h3 class="font-semibold text-foreground mb-4">支持</h3>
          <ul class="space-y-2">
            <li v-for="link in footerLinks.support" :key="link.path">
              <NuxtLink 
                :to="link.path" 
                class="text-sm text-muted-foreground hover:text-foreground transition-colors"
              >
                {{ link.name }}
              </NuxtLink>
            </li>
          </ul>
        </div>
      </div>

      <!-- Bottom -->
      <div class="border-t border-border/40 mt-12 pt-8 flex flex-col md:flex-row items-center justify-between gap-4">
        <p class="text-sm text-muted-foreground">
          © 2024 秋云工具. All rights reserved.
        </p>
        <p class="text-sm text-muted-foreground flex items-center gap-1">
          Made with <Heart class="w-4 h-4 text-red-500 fill-red-500" /> for developers
        </p>
      </div>
    </div>

    <!-- Feedback Button -->
    <button 
      @click="showFeedbackModal = true"
      class="fixed bottom-6 right-6 z-40 flex items-center gap-2 px-4 py-2 bg-primary text-primary-foreground rounded-full shadow-lg hover:bg-primary/90 transition-colors"
    >
      <MessageCircle class="w-4 h-4" />
      <span class="text-sm font-medium hidden sm:inline">问题反馈</span>
    </button>

    <!-- Feedback Modal -->
    <FeedbackModal v-model:open="showFeedbackModal" />
  </footer>
</template>
