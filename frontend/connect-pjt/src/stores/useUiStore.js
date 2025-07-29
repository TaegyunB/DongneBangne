import { defineStore } from 'pinia'

export const useUiStore = defineStore('ui', {
  state: () => ({
    showLogo: true,
    showMenu: true,
    showProfile: true,
    welcomeText: ''
  })
})
