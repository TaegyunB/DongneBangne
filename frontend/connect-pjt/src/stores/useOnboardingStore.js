import { defineStore } from 'pinia'

const KEY = 'onboarding_v1_seen'

export const useOnboardingStore = defineStore('onboarding', {
  state: () => ({
    seen: typeof window !== 'undefined' ? !!localStorage.getItem(KEY) : true
  }),
  actions: {
    markSeen() {
      this.seen = true
      localStorage.setItem(KEY, '1')
    },
    reset() {
      this.seen = false
      localStorage.removeItem(KEY)
    }
  }
})
