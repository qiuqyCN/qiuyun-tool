<script setup lang="ts">
import { 
  Wrench, 
  Github, 
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
    // { name: 'API文档', path: '/api' },
  ]
}

const showFeedbackModal = ref(false)

const currentYear = new Date().getFullYear()
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
            <a href="https://github.com/qiuqyCN/qiuyun-tool" class="text-muted-foreground hover:text-foreground transition-colors">
              <Github class="w-5 h-5" />
            </a>
             <a href="mailto:97208294@qq.com" class="text-muted-foreground hover:text-foreground transition-colors">
              <Mail class="w-5 h-5" />
            </a>
            <a href="https://space.bilibili.com/4295116" class="text-muted-foreground hover:text-foreground transition-colors">
              <svg class="w-5 h-5" viewBox="0 0 24 24" fill="currentColor">
                <path d="M17.813 4.653h.854c1.51.054 2.769.578 3.773 1.574 1.004.995 1.524 2.249 1.56 3.76v7.36c-.036 1.51-.556 2.769-1.56 3.773s-2.262 1.524-3.773 1.56H5.333c-1.51-.036-2.769-.556-3.773-1.56S.036 18.858 0 17.347v-7.36c.036-1.511.556-2.765 1.56-3.76 1.004-.996 2.262-1.52 3.773-1.574h.774l-1.174-1.12a1.234 1.234 0 0 1-.373-.906c0-.356.124-.659.373-.907l.027-.027c.267-.249.573-.373.92-.373.347 0 .653.124.92.373L9.653 4.44c.071.071.134.142.187.213h4.267a.836.836 0 0 1 .16-.213l2.853-2.747c.267-.249.573-.373.92-.373.347 0 .662.151.929.4.267.249.391.551.391.907 0 .355-.124.657-.373.906zM5.333 7.24c-.746.018-1.373.276-1.88.773-.506.498-.769 1.13-.786 1.894v7.52c.017.764.28 1.395.786 1.893.507.498 1.134.756 1.88.773h13.334c.746-.017 1.373-.275 1.88-.773.506-.498.769-1.129.786-1.893v-7.52c-.017-.765-.28-1.396-.786-1.894-.507-.497-1.134-.755-1.88-.773zM8 11.107c.373 0 .684.124.933.373.25.249.383.569.4.96v1.173c-.017.391-.15.711-.4.96-.249.25-.56.374-.933.374s-.684-.125-.933-.374c-.25-.249-.383-.569-.4-.96V12.44c0-.373.129-.689.386-.947.258-.257.574-.386.947-.386zm8 0c.373 0 .684.124.933.373.25.249.383.569.4.96v1.173c-.017.391-.15.711-.4.96-.249.25-.56.374-.933.374s-.684-.125-.933-.374c-.25-.249-.383-.569-.4-.96V12.44c.017-.391.15-.711.4-.96.249-.249.56-.373.933-.373z"/>
              </svg>
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
          © {{ currentYear }} 秋云工具. All rights reserved.
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
