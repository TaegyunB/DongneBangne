import { createRouter, createWebHistory } from 'vue-router'
import MissionView from '../views/MissionView.vue'
import Onboarding from '@/views/onboarding.vue'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/challenges',
      name: 'mission',
      component: MissionView,
    },
    {
      path: '/login',
      name: 'onboarding',
      component: Onboarding
    },
    {
      path: '/admin/challenges',
      name: 'missionCreate',
      component: MissionCreateView,
    }
  ],

  
})

export default router
//