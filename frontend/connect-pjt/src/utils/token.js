// 가장 단순한 버전: localStorage → Cookie 순서로 찾기
export function getAccessToken() {
  const fromLS = localStorage.getItem('access_token')
  if (fromLS) return fromLS

  const m = document.cookie.match(/(?:^|;\s*)access_token=([^;]+)/)
  return m ? decodeURIComponent(m[1]) : null
}