import { createRouter, createWebHistory } from 'vue-router'
import MissionView from '../views/MissionView.vue'
import Onboarding from '../views/onboarding.vue'
import MissionCreateView from '@/views/MissionCreateView.vue'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/login',
      name: 'onboarding',
      component: Onboarding
    },
    {

    },
    {
      path: '/challenges',
      name: 'mission',
      component: MissionView,
    },
    {
      path: '/admin/challenges',
      name: 'missionCreate',
      component: MissionCreateView,
    },
  ],

  
})

export default router
//