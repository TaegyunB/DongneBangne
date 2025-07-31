import { createRouter, createWebHistory } from 'vue-router'
import Start from '@/views/start.vue'
import Onboarding from '../views/onboarding.vue'
import SeniorCenter from '../views/seniorCenter.vue'
import SeniorCenterProfile from '../views/seniorCenterProfile.vue'
import MainPage from '../views/mainPage.vue'
import MissionView from '../views/MissionView.vue'
import MissionCreateView from '@/views/MissionCreateView.vue'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      name: 'start',
      component: Start
    },
    {
      path: '/login',
      name: 'onboarding',
      component: Onboarding
    },
    {
      path: '/senior-center',
      name: 'seniorCenter',
      component: SeniorCenter
    },
    {
      path: '/senior-center/profile',
      name: 'seniorCenterProfile',
      component: SeniorCenterProfile
    },
    { 
      path: '/mainpage',
      name: 'mainPage',
      component: MainPage,
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