package S13P11A708.backend.jwt;

import S13P11A708.backend.domain.enums.UserRole;
import jakarta.servlet.http.Cookie;
import lombok.RequiredArgsConstructor;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;

import java.util.Map;

@Component
@RequiredArgsConstructor
public class JwtHandshakeInterceptor implements org.springframework.web.socket.server.HandshakeInterceptor {

    private final JWTUtil jwtUtil;

    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response,
                                   WebSocketHandler wsHandler, Map<String, Object> attributes) {

        if (request instanceof ServletServerHttpRequest servletReq) {
            var http = servletReq.getServletRequest();

            String token = findCookie(http.getCookies(), "access_token"); // ★ 쿠키명 맞추세요
            if (token == null || jwtUtil.isExpired(token)) {
                // 인증 없으면 접속 자체를 막고 싶으면 false/예외. 허용하고 싶으면 true 리턴.
                throw new IllegalStateException("웹소켓 핸드셰이크: JWT 없음/만료");
            }

            Long userId = jwtUtil.getUserId(token);
            String role = jwtUtil.getRole(token); // "ROLE_MEMBER"
            UserRole userRole = UserRole.valueOf(role.replace("ROLE_", ""));

            // 이후 단계에서 Principal을 만들 수 있도록 재료를 저장
            attributes.put("ws_user_id", userId);
            attributes.put("ws_user_role", userRole);
        }
        return true;
    }

    @Override public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response,
                                         WebSocketHandler wsHandler, @Nullable Exception exception) {}

    private static String findCookie(@Nullable Cookie[] cookies, String name) {
        if (cookies == null) return null;
        for (Cookie c : cookies) {
            if (name.equals(c.getName())) return c.getValue();
        }
        return null;
    }

}
