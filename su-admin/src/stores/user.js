import { defineStore } from 'pinia'
import { ref, computed } from 'vue'

export const useUserStore = defineStore('user', () => {
  // 状态
  const token = ref(localStorage.getItem('token') || '')
  const userInfo = ref(JSON.parse(localStorage.getItem('userInfo') || 'null'))

  // 计算属性
  const isLoggedIn = computed(() => !!token.value)
  const userName = computed(() => userInfo.value?.name || '')
  const userId = computed(() => userInfo.value?.id || null)

  // 设置Token
  function setToken(newToken) {
    token.value = newToken
    localStorage.setItem('token', newToken)
  }

  // 设置用户信息
  function setUserInfo(info) {
    userInfo.value = info
    localStorage.setItem('userInfo', JSON.stringify(info))
  }

  // 登录
  function login(data) {
    setToken(data.token)
    setUserInfo(data.user)
  }

  // 登出
  function logout() {
    token.value = ''
    userInfo.value = null
    localStorage.removeItem('token')
    localStorage.removeItem('userInfo')
  }

  // 检查登录状态
  function checkAuth() {
    return !!token.value
  }

  return {
    token,
    userInfo,
    isLoggedIn,
    userName,
    userId,
    setToken,
    setUserInfo,
    login,
    logout,
    checkAuth
  }
})
