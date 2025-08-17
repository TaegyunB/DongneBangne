package S13P11A708.backend.controller;

import S13P11A708.backend.dto.webSocket.GameAnsRequestMessage;
import S13P11A708.backend.dto.webSocket.GameAnsSocketMessage;
import S13P11A708.backend.dto.webSocket.GameHintRequestMessage;
import S13P11A708.backend.dto.webSocket.GameHintSocketMessage;
import S13P11A708.backend.security.CustomOAuth2User;
import S13P11A708.backend.service.GameService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;

import java.security.Principal;

@Slf4j
@RequiredArgsConstructor
@Controller
public class GameSocketController {

    private final GameService gameService;

    /**
     * 정답 제출, 인증 처리
     */
    @MessageMapping("/games/answer")
    public void submitAnswer(GameAnsRequestMessage message, Principal principal) {
        if (principal == null) throw new AccessDeniedException("Unauthenticated");

        Long userId = extractUserIdFromPrincipal(principal);
        Long roomId = message.getRoomId();
        String answer = message.getAnswer();

        log.info("[ANSWER_SUBMIT] roomId: {}, userId: {}, answer: {}", roomId, userId, answer);

        gameService.handleAnswer(roomId, userId, answer);
    }

    /**
     * 힌트 요청, 힌트 보여주기 처리
     */
    @MessageMapping("/games/hint")
    public void requestHint(GameHintRequestMessage message, Principal principal){
        if (principal == null) throw new AccessDeniedException("Unauthenticated");

        Long userId = extractUserIdFromPrincipal(principal);
        Long roomId = message.getRoomId();

        log.info("[HINT_REQUEST] roomId: {}, userId:{}", roomId, userId);

        gameService.handleHint(roomId, userId);
    }

    /**
     * 인증 유저 정보 추출
     */
    private Long extractUserIdFromPrincipal(Principal principal) {
        if (principal == null) throw new AccessDeniedException("Unauthenticated");

        if(principal instanceof Authentication authentication &&
        authentication.getPrincipal() instanceof CustomOAuth2User user) {
            return user.getUserId();
        }
        throw new IllegalStateException("WebSocket 인증된 사용자 정보가 없습니다.");
    }
}
