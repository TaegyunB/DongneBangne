import axios from 'axios'

const api = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080',
  withCredentials: true,
})

// ▼ 추가: access_token 쿠키 → Authorization 헤더 자동 세팅
function getCookie(name) {
  return document.cookie
    .split('; ')
    .find(v => v.startsWith(name + '='))?.split('=')[1]
}

api.interceptors.request.use(cfg => {
  // 이미 Authorization 있으면 건드리지 않음
  if (!cfg.headers?.Authorization) {
    const raw = getCookie('access_token') || localStorage.getItem('access_token') || sessionStorage.getItem('access_token')
    if (raw) {
      const token = decodeURIComponent(raw)
      cfg.headers = cfg.headers || {}
      cfg.headers.Authorization = `Bearer ${token}`
      // 선택: 서버가 AJAX 요청 구분할 때 도움됨
      cfg.headers['X-Requested-With'] = 'XMLHttpRequest'
    }
  }
  return cfg
})

// 기존 응답 인터셉터 유지 + 302 → 로그인 리다이렉트 방지 보완
api.interceptors.response.use(
  response => response,
  error => {
    // 302로 OAuth 로그인 보내는 경우도 로그인 페이지로 보내기
    if (error.response && error.response.status === 302) {
      const loc = String(error.response.headers?.location || '')
      if (loc.includes('/oauth2/authorization')) {
        if (window.location.pathname !== '/login') window.location.href = '/login'
        return Promise.reject(error)
      }
    }

    // Spring Security가 인증 실패시 /login HTML 반환하는 경우 감지
    if (
      error.response &&
      typeof error.response.data === 'string' &&
      error.response.data.includes('<form') &&
      error.response.status === 200
    ) {
      if (window.location.pathname !== '/login') {
        window.location.href = '/login'
      }
      return Promise.reject(error)
    }

    // 401 Unauthorized
    if (error.response && error.response.status === 401) {
      if (window.location.pathname !== '/login') {
        window.location.href = '/login'
      }
      return Promise.reject(error)
    }

    return Promise.reject(error)
  }
)

export default api
