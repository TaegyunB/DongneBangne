import { createRouter, createWebHistory } from 'vue-router'

import Onboarding from '@/views/onboarding.vue'
import SeniorCenter from '@/views/seniorCenter.vue'
import SeniorCenterProfile from '@/views/seniorCenterProfile.vue'
import ChallengeView from '../views/ChallengeView.vue'
import ChallengeCreateView from '@/views/ChallengeCreateView.vue'
import ChallengeFinishView from '@/views/ChallengeFinishView.vue'
import CommunityBoards from '@/views/communityBoards.vue'
import MainPage from '@/views/mainPage.vue'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/login',
      name: 'onboarding',
      component: Onboarding,
    },
    {
      path: '/senior-center',
      name: 'seniorCenter',
      component: SeniorCenter,
    },
    {
      path: '/senior-center/profile',
      name: 'seniorCenterProfile',
      component: SeniorCenterProfile,
    },
    { 
      path: '/mainpage',
      name: 'mainPage',
      component: MainPage,
    },
    {
      path: '/challenges',
      name: 'challenge',
      component: ChallengeView,
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
    },
    {
      path: '/boards',
      name: 'communityBoards',
      component: CommunityBoards,
    }
  ]
})

export default router