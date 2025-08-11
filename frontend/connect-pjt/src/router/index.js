import { createRouter, createWebHistory } from 'vue-router'
import ChallengeView from '../views/ChallengeView.vue'
import Start from '@/views/start.vue'
import Onboarding from '@/views/onboarding.vue'
import ChallengeCreateView from '@/views/ChallengeCreateView.vue'
import ChallengeFinishView from '@/views/ChallengeFinishView.vue'
import SeniorCenter from '@/views/seniorCenter.vue'
import SeniorCenterProfile from '@/views/seniorCenterProfile.vue'
import MainPage from '@/views/MainpageView.vue'
import CommunityBoards from '@/views/communityBoards.vue'
import RankingBoard from '@/views/rankingBoard.vue'
import GameView from '@/views/GameView.vue'
import WebRTCTestView from '@/views/WebRTCTestView.vue'
import AiNewsView from '@/views/AiNewsView.vue'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/challenges',
      name: 'challenge',
      component: ChallengeView,
    },
    {
      path: '/',
      name: 'start',
      component: Start,
      meta: { hideToolbar: true },
    },
    {
      path: '/login',
      name: 'onboarding',
      component: Onboarding,
      meta: { hideToolbar: true },
    },
    {
      path: '/senior-center',
      name: 'seniorCenter',
      component: SeniorCenter,
      meta: { hideToolbar: true },
    },
    {
      path: '/senior-center/profile',
      name: 'seniorCenterProfile',
      component: SeniorCenterProfile,
      meta: { hideToolbar: true },
    },
    { 
      path: '/mainpage',
      name: 'mainPage',
      component: MainPage,
      meta: { hideToolbar: true },
    },
    { 
      path: '/rankings',
      name: 'rankings',
      component: RankingBoard,
    },
    { 
      path: '/boards',
      name: 'boards',
      component: CommunityBoards,
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
      path: '/games',
      name: 'games',
      component: GameView,
      meta: { hideToolbar: true },
    },
    {
      path: '/webrtc',
      name: 'webrtc',
      component: WebRTCTestView,
    },
    {
      path: '/news',
      name: 'news',
      component: AiNewsView,
    }
  ]
})

export default router