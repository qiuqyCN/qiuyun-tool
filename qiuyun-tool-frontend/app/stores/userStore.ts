import { defineStore } from 'pinia'
import type { UserStatus } from '~/types/enums'
import type { ApiResponse } from '~/types/tool'

export interface UserInfo {
  id: number
  username: string
  nickname: string
  email: string
  avatar?: string
  isVip: boolean
  status: UserStatus
  roles: string[]
}

export interface AuthState {
  user: UserInfo | null
  accessToken: string | null
  refreshToken: string | null
  isAuthenticated: boolean
  isLoginModalVisible: boolean
}

interface LoginResponse {
  accessToken: string
  refreshToken: string
  user: UserInfo
}

interface MessageResponse {
  message: string
}

export const useUserStore = defineStore('user', {
  state: (): AuthState => ({
    user: null,
    accessToken: null,
    refreshToken: null,
    isAuthenticated: false,
    isLoginModalVisible: false
  }),

  getters: {
    // 获取用户信息
    currentUser: (state): UserInfo | null => state.user,

    // 是否已登录
    isLoggedIn: (state): boolean => state.isAuthenticated && !!state.user,

    // 是否是VIP
    isVip: (state): boolean => state.user?.isVip ?? false,

    // 获取Token
    token: (state): string | null => state.accessToken
  },

  actions: {
    // 设置认证信息
    setAuth(accessToken: string, refreshToken: string, user: UserInfo) {
      this.accessToken = accessToken
      this.refreshToken = refreshToken
      this.user = user
      this.isAuthenticated = true

      // 存储到 cookie（用于 SSR）
      const accessTokenCookie = useCookie('accessToken', { maxAge: 60 * 60 * 2 }) // 2小时
      const refreshTokenCookie = useCookie('refreshToken', { maxAge: 60 * 60 * 24 * 7 }) // 7天

      accessTokenCookie.value = accessToken
      refreshTokenCookie.value = refreshToken
    },

    // 清除认证信息
    clearAuth() {
      this.accessToken = null
      this.refreshToken = null
      this.user = null
      this.isAuthenticated = false

      // 清除 cookie
      const accessTokenCookie = useCookie('accessToken')
      const refreshTokenCookie = useCookie('refreshToken')

      accessTokenCookie.value = null
      refreshTokenCookie.value = null
    },

    // 登录
    async login(username: string, password: string, rememberMe: boolean = false) {
      const { $api } = useNuxtApp()

      const response = await $api('/auth/login', {
        method: 'POST',
        body: { username, password, rememberMe }
      }) as ApiResponse<LoginResponse>

      if (response.code === 200) {
        const { accessToken, refreshToken, user } = response.data
        this.setAuth(accessToken, refreshToken, user)
        return { success: true, data: response.data }
      }

      return { success: false, message: response.message }
    },

    // 注册
    async register(username: string, email: string, password: string, confirmPassword: string) {
      const { $api } = useNuxtApp()

      const response = await $api('/auth/register', {
        method: 'POST',
        body: { username, email, password, confirmPassword }
      }) as ApiResponse<MessageResponse>

      if (response.code === 200) {
        return { success: true, message: response.data?.message || '注册成功' }
      }

      return { success: false, message: response.message }
    },

    // 退出登录
    async logout() {
      const { $api } = useNuxtApp()

      try {
        await $api('/auth/logout', { method: 'POST' })
      } catch (error) {
        console.error('Logout error:', error)
      } finally {
        this.clearAuth()
      }
    },

    // 获取当前用户信息
    async fetchCurrentUser() {
      const { $api } = useNuxtApp()

      try {
        const response = await $api('/auth/me') as ApiResponse<UserInfo>

        if (response.code === 200) {
          this.user = response.data
          this.isAuthenticated = true
          return { success: true, data: response.data }
        }
      } catch (error) {
        console.error('Fetch user error:', error)
        this.clearAuth()
      }

      return { success: false }
    },

    // 刷新 Token
    async refreshAccessToken() {
      const { $api } = useNuxtApp()

      if (!this.refreshToken) {
        this.clearAuth()
        return { success: false }
      }

      try {
        const response = await $api('/auth/refresh', {
          method: 'POST',
          headers: {
            Authorization: `Bearer ${this.refreshToken}`
          }
        }) as ApiResponse<LoginResponse>

        if (response.code === 200) {
          const { accessToken, refreshToken, user } = response.data
          this.setAuth(accessToken, refreshToken, user)
          return { success: true }
        }
      } catch (error) {
        console.error('Refresh token error:', error)
      }

      this.clearAuth()
      return { success: false }
    },

    // 初始化（从 cookie 恢复登录状态）
    async initialize() {
      const accessTokenCookie = useCookie('accessToken')
      const refreshTokenCookie = useCookie('refreshToken')

      if (accessTokenCookie.value && refreshTokenCookie.value) {
        this.accessToken = accessTokenCookie.value
        this.refreshToken = refreshTokenCookie.value

        // 获取用户信息
        const result = await this.fetchCurrentUser()
        return result.success
      }

      return false
    },

    // 显示/隐藏登录弹窗
    setLoginModalVisible(visible: boolean) {
      this.isLoginModalVisible = visible
    }
  }
})
