import axios from 'axios'

const api = axios.create({
  baseURL: import.meta.env.VITE_API_URL || 'http://localhost:8080',
  withCredentials: true,
})

api.interceptors.response.use(
  response => response,
  error => {
    // Spring Security가 인증 실패시 /login HTML 반환하는 경우 감지
    if (
      error.response &&
      typeof error.response.data === 'string' &&
      error.response.data.includes('<form') &&
      error.response.status === 200
    ) {
      // 무한 리다이렉트 방지
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
