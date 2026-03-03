import { useUserStore } from '~/stores/userStore'

export default defineNuxtPlugin(async () => {
  const userStore = useUserStore()

  // 从 cookie 获取 token
  const accessTokenCookie = useCookie('accessToken')
  const refreshTokenCookie = useCookie('refreshToken')

  // 如果有 token，尝试恢复登录状态
  if (accessTokenCookie.value && refreshTokenCookie.value) {
    // 先设置 token，这样 API 请求可以带上认证头
    userStore.accessToken = accessTokenCookie.value
    userStore.refreshToken = refreshTokenCookie.value

    try {
      // 获取当前用户信息
      const result = await userStore.fetchCurrentUser()

      if (!result.success) {
        // 获取失败，清除状态
        userStore.clearAuth()
      }
    } catch (error) {
      console.error('Auth init error:', error)
      userStore.clearAuth()
    }
  }
})
