//userRole을 저장하기 위한 Store
import { defineStore } from 'pinia'
import api from '@/api/axios'

export const useUserStore = defineStore('user', {
  state: () => ({
    userRole: null,
    isLoading: false,
    error: null
  }),

  getters: {
    isAdmin: (state) => state.userRole === 'ADMIN',
    isMember: (state) => state.userRole === 'MEMBER'
  },

  actions: {
    async fetchUserRole() {
      if (this.userRole) return this.userRole

      this.isLoading = true
      this.error = null

      try {
        const response = await api.get('/api/v1/main/me')
        this.userRole = response.data.userRole
        console.log("UserRole:", this.userRole)
        return this.userRole
      } catch (error) {
        console.error('Error fetching user info:', error)
        this.error = error
        this.userRole = error.response?.status === 401 ? null : 'MEMBER'
        return this.userRole
      } finally {
        this.isLoading = false
      }
    },

    clearUser() {
      this.userRole = null
      this.error = null
    }
  }
})