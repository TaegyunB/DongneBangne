import { createRouter, createWebHistory } from 'vue-router'
import ChallengeView from '../views/ChallengeView.vue'
import Onboarding from '@/views/onboarding.vue'
import ChallengeCreateView from '@/views/ChallengeCreateView.vue'
import ChallengeFinishView from '@/views/ChallengeFinishView.vue'


const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/challenges',
      name: 'challenge',
      component: ChallengeView,
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
      name: 'challengeCreate',
      component: ChallengeCreateView,
    },
    {
      path: '/admin/challenges/:challengeId/complete',
      name: 'challengeFinish',
      component: ChallengeFinishView,
      props: true
    }
  ]
})

export default router