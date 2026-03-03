import { useUserStore } from '~/stores/userStore'

export default defineNuxtPlugin(() => {
  const config = useRuntimeConfig()
  const userStore = useUserStore()

  const apiFetch = $fetch.create({
    baseURL: config.public.apiBaseUrl,
    credentials: 'include',

    onRequest({ options }) {
      // 添加 Token
      const token = userStore.token
      if (token) {
        const headers = new Headers(options.headers)
        headers.set('Authorization', `Bearer ${token}`)
        options.headers = headers
      }
    },

    async onResponseError({ response, options, request }) {
      // Token 过期，尝试刷新
      const url = request.toString()
      if (response.status === 401 && !url.includes('/auth/refresh')) {
        const refreshResult = await userStore.refreshAccessToken()

        if (refreshResult.success) {
          // 刷新成功，重试原请求
          const newToken = userStore.token
          if (newToken) {
            const headers = new Headers(options.headers)
            headers.set('Authorization', `Bearer ${newToken}`)
            options.headers = headers
            // 重新发起请求
            return $fetch(url, options as any)
          }
        } else {
          // 刷新失败，跳转登录
          navigateTo('/login')
        }
      }
    }
  })

  return {
    provide: {
      api: apiFetch
    }
  }
})
