//userRole을 저장하기 위한 Store
import { defineStore } from 'pinia'
import axios from 'axios'

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
      // 이미 있으면 재사용
      if (this.userRole) {
        console.log('UserRole already exists:', this.userRole)
        return this.userRole
      }

      this.isLoading = true
      this.error = null

      try {
        console.log('Fetching user role from API...')
        const response = await axios.get('/api/v1/main/me', {
          withCredentials: true,
          headers: { 'Content-Type': 'application/json' }
        })
        
        this.userRole = response.data.userRole
        console.log('User role fetched from API:', this.userRole)
        return this.userRole
        
      } catch (error) {
        console.error('Error fetching user info:', error)
        this.error = error
        this.userRole = 'MEMBER' // 기본값
        return this.userRole
      } finally {
        this.isLoading = false
      }
    },

    setUserRole(role) {
      console.log('Setting user role:', role)
      this.userRole = role
    },

    clearUser() {
      this.userRole = null
      this.error = null
    }
  }
})