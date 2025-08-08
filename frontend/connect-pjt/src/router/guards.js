import { getAccessToken } from '@/api/axios'

export const setupGuards = router => {
  router.beforeEach(to => {
    const needsAuth = to.matched.some(r => r.meta?.requiresAuth)
    const token = getAccessToken()

    if (needsAuth && !token) {
      return { path: '/login', query: { redirect: to.fullPath } }
    }

    // 로그인 상태에서 /login 가면 메인으로 보내고 싶다면 주석 해제
    // if (to.name === 'onboarding' && token) {
    //   return { path: '/mainpage' }
    // }
  })
}
