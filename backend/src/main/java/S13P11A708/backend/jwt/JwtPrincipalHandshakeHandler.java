package S13P11A708.backend.jwt;

import S13P11A708.backend.domain.enums.UserRole;
import S13P11A708.backend.security.CustomOAuth2User;
import S13P11A708.backend.security.UserDto;
import lombok.NonNull;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.support.DefaultHandshakeHandler;

import java.security.Principal;
import java.util.Map;

@Component
public class JwtPrincipalHandshakeHandler extends DefaultHandshakeHandler {

    @Override
    protected Principal determineUser(@NonNull ServerHttpRequest request,
                                      @NonNull WebSocketHandler wsHandler,
                                      @NonNull Map<String, Object> attributes) {
        Long userId = (Long) attributes.get("ws_user_id");
        UserRole role = (UserRole) attributes.get("ws_user_role");

        if (userId == null || role == null) {
            // HandshakeInterceptor에서 인증을 못 심었을 때 방어
            throw new IllegalStateException("웹소켓 Principal 생성 실패: 인증 속성 없음");
        }

        var dto = UserDto.builder()
                .userId(userId)
                .userRole(role)
                .build();

        var principal = new CustomOAuth2User(dto);
        // Authentication을 Principal로 사용해도 됨 (Spring이 그대로 전달)
        return new UsernamePasswordAuthenticationToken(principal, "N/A", principal.getAuthorities());
    }


}
