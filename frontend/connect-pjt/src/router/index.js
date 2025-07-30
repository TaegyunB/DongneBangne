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
      path: '/admin/challenges',
      name: 'challengeCreate',
      component: ChallengeCreateView,
    },
    {
      path: '/admin/challenges/:challengeId',
      name: 'challengeFinish',
      component: ChallengeFinishView,
      props: true
    }
  ],

  
})

export default router
//