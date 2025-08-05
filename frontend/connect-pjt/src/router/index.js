import { createRouter, createWebHistory } from 'vue-router'
import ChallengeView from '../views/ChallengeView.vue'
import Start from '@/views/start.vue'
import Onboarding from '@/views/onboarding.vue'
import SeniorCenter from '@/views/seniorCenter.vue'
import SeniorCenterProfile from '@/views/seniorCenterProfile.vue'
import ChallengeCreateView from '@/views/ChallengeCreateView.vue'
import ChallengeFinishView from '@/views/ChallengeFinishView.vue'
import MainPage from '@/views/MainpageView.vue'
import CommunityBoards from '@/views/communityBoards.vue'
import CommunityWrite from '@/views/communityWrite.vue'
import CommunityDetail from '@/views/communityDetail.vue'
import CommunityEdit from '@/views/communityEdit.vue'
import RankingBoard from '@/views/rankingBoard.vue'
import GameView from '@/views/GameView.vue'

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
      component: Start
    },
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
      path: '/boards',
      name: 'Boards',
      component: CommunityBoards,
    },
    {
      path: '/boards/write',
      name: 'communitysWrite',
      component: CommunityWrite,
    },
    {
      path: '/boards/:id',
      name: 'communityDetail',
      component: CommunityDetail,
    },
    {
      path: '/boards/:id/edit',
      name: 'communityEdit',
      component: CommunityEdit,
    },
    {
      path: '/rankings',
      name: 'Rankings',
      component: RankingBoard,
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
      path: '/admin/game',
      name: 'game',
      component: GameView,
    }
  ]
})

export default router