import { createRouter, createWebHistory } from 'vue-router'
import MissionView from '../views/MissionView.vue'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/challenges',
      name: 'mission',
      component: MissionView,
    },
  ],

  
})

export default router
//