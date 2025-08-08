// 기본 메타: 툴바는 보임
// 차후에 페이지 별 돋보기 관련 조정도 여기서 가능할 듯
export const defaultMeta = { hideToolbar: false }

// 숨길 라우트만 이름 기준으로 덮어쓰기
// false -> true 값으로 변환하면서 보이지 않도록
export const metaOverrides = {
  onboarding: { hideToolbar: true },      // /login
  mainPage: { hideToolbar: true },        // /mainpage 
  game: { hideToolbar: true },            // /admin/game
  start: {hideToolbar: true},             // /
}

// children까지 재귀적으로 기본메타 + 오버라이드 합치는 헬퍼
export const applyMeta = (routes, defaults = defaultMeta, overrides = metaOverrides) => {
  const mapRoute = r => {
    const meta = { ...defaults, ...(r.meta || {}), ...(overrides[r.name] || {}) }
    const children = r.children ? r.children.map(mapRoute) : undefined
    return { ...r, meta, ...(children ? { children } : {}) }
  }
  return routes.map(mapRoute)
}
