package S13P11A708.backend.jwt;

import S13P11A708.backend.domain.enums.UserRole;
import S13P11A708.backend.security.CustomOAuth2User;
import S13P11A708.backend.security.UserDto;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

public class JWTFilter extends OncePerRequestFilter {

    private final JWTUtil jwtUtil;

    public JWTFilter(JWTUtil jwtUtil){
        this.jwtUtil = jwtUtil;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        String uri = request.getRequestURI();

        boolean skip = uri.startsWith("/api/v1/senior-centers")
            || uri.startsWith("/oauth2")
            || uri.startsWith("/login")
            || uri.equals("/") || uri.startsWith("/signal");

        System.out.println("🚫 shouldNotFilter → " + uri + " → " + skip);
        return skip;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        System.out.println("\n==== [JWTFilter] doFilterInternal 진입! ====");
        //cookie들을 불러온 뒤 access_token Key에 담긴 쿠키를 찾음
        String authorization = null;

        //쿠키에서 JWT 토큰 꺼내기
//        Cookie[] cookies = request.getCookies();
//        for (Cookie cookie : cookies) {
//            System.out.println(cookie.getName());
//            if (cookie.getName().equals("Authorization")) {
//                authorization = cookie.getValue();
//            }
//        }
        Cookie[] cookies = request.getCookies();
        System.out.println("==== 쿠키 전체 확인 ====");
        // 1. 쿠키 전체 출력
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                System.out.println(cookie.getName() + "=" + cookie.getValue());
                if (cookie.getName().equals("access_token")) {
                    authorization = cookie.getValue();
                }
            }
        } else {
            System.out.println("쿠키 없음!");
        }

        //Authorization 헤더 검증
        if (authorization == null) {

            System.out.println("token null");
            filterChain.doFilter(request, response);

            //조건이 해당되면 메소드 종료 (필수)
            return;
        }
        System.out.println("[JWTFilter] access_token 토큰 추출: " + authorization);
        //토큰
        String token = authorization;

        //토큰 소멸 시간 검증
        if (jwtUtil.isExpired(token)) {

            System.out.println("token expired");
            filterChain.doFilter(request, response);

            //조건이 해당되면 메소드 종료 (필수)
            return;
        }

        //토큰이 없거나 만료되었으면, 필터 체인만 진행하고 인증하지 않음
        System.out.println("토큰 만료 아님, 인증 계속 진행");
        //토큰에서 username과 role 획득
        Long userId = jwtUtil.getUserId(token);
        String role = jwtUtil.getRole(token);

        String enumRole = role.replace("ROLE_", "");

        //userDTO를 생성하여 값 set
        UserDto userDTO = new UserDto();
        userDTO.setUserId(userId);
        userDTO.setUserRole(UserRole.valueOf(enumRole));

        //UserDetails에 회원 정보 객체 담기
        CustomOAuth2User customOAuth2User = new CustomOAuth2User(userDTO);

        //스프링 시큐리티 인증 토큰 생성
        Authentication authToken = new UsernamePasswordAuthenticationToken(customOAuth2User, null, customOAuth2User.getAuthorities());
        //세션에 사용자 등록
        SecurityContextHolder.getContext().setAuthentication(authToken);

        filterChain.doFilter(request, response);
    }
}
