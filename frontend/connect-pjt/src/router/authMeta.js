// 기본: 인증 필요
export const defaultAuthMeta = { requiresAuth: true }

// 공개 페이지만 false로 지정
export const authOverrides = {
  onboarding: { requiresAuth: false }, // /login
  start: { requiresAuth: false }       // /
}

export const applyAuth = (routes, defaults = defaultAuthMeta, overrides = authOverrides) => {
  const mapRoute = r => {
    const meta = { ...defaults, ...(r.meta || {}), ...(overrides[r.name] || {}) }
    const children = r.children ? r.children.map(mapRoute) : undefined
    return { ...r, meta, ...(children ? { children } : {}) }
  }
  return routes.map(mapRoute)
}