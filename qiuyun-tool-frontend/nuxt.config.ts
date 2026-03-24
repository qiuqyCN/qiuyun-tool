// https://nuxt.com/docs/api/configuration/nuxt-config
import tailwindcss from '@tailwindcss/vite'

export default defineNuxtConfig({
  compatibilityDate: '2025-07-15',
  devtools: { enabled: true },
  css: ['~/assets/css/tailwind.css'],
  vite: {
    plugins: [
      tailwindcss() as any,
    ],
  },
  modules: ['shadcn-nuxt', '@nuxtjs/color-mode', '@pinia/nuxt'],
  colorMode: {
    classSuffix: ''
  },
  shadcn: {
    /**
     * Prefix for all the imported component.
     * @default "Ui"
     */
    prefix: '',
    /**
     * Directory that the component lives in.
     * Will respect the Nuxt aliases.
     * @link https://nuxt.com/docs/api/nuxt-config#alias
     * @default "@/components/ui"
     */
    componentDir: '@/components/ui'
  },
  app: {
    head: {
      title: '秋云工具', // default fallback title
      htmlAttrs: {
        lang: 'zh-CN',
      },
      link: [
        { rel: 'icon', type: 'image/svg+xml', href: '/logo.svg' },
      ],
    },
  },
  runtimeConfig: {
    public: {
      apiBaseUrl: import.meta.env.NUXT_PUBLIC_API_BASE_URL || 'http://localhost:8001'
    }
  },
  // 路由重写：将 /tools/分类/工具 重写为 /分类/工具
  hooks: {
    'pages:extend': (pages) => {
      // 找到所有在 tools/ 目录下的页面
      const toolPages = pages.filter(page => page.path.startsWith('/tools/'))
      
      // 为每个 tools/ 页面创建一个不带 tools/ 前缀的路由
      toolPages.forEach(page => {
        const newPath = page.path.replace('/tools/', '/')
        // 添加新的路由条目
        pages.push({
          ...page,
          path: newPath,
          name: page.name?.replace('tools-', '')
        })
      })
    }
  }
})
