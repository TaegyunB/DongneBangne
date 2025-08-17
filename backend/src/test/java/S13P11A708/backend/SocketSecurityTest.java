package S13P11A708.backend;

import S13P11A708.backend.controller.GameSocketController;
import S13P11A708.backend.domain.User;
import S13P11A708.backend.domain.enums.GameMessageType;
import S13P11A708.backend.domain.enums.UserRole;
import S13P11A708.backend.dto.webSocket.GameAnsRequestMessage;
import S13P11A708.backend.dto.webSocket.GameAnsSocketMessage;
import S13P11A708.backend.security.CustomOAuth2User;
import S13P11A708.backend.security.UserDto;
import S13P11A708.backend.service.GameService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verifyNoMoreInteractions;


@ExtendWith(MockitoExtension.class)
public class SocketSecurityTest {
    @Mock GameService gameService;
    GameSocketController controller;

    @BeforeEach
    void setUp() { controller = new GameSocketController(gameService); }

    @Test
    void submitAnswer_extracts_userId_from_Principal() {
        long roomId = 1L;
        long userId = 101L;
        String answer = "고향역";

        var dto = UserDto.builder()
                .userId(userId)
                .kakaoId("kakao123")
                .nickname("닉")
                .profileImage("img")
                .userRole(UserRole.MEMBER)
                .build();

        var principal = new UsernamePasswordAuthenticationToken(
                new CustomOAuth2User(dto), "N/A", List.of()
        );

        controller.submitAnswer(new GameAnsRequestMessage(answer, roomId, GameMessageType.ANSWER_SUBMIT, userId), principal);

        // ✅ 정확히 한 번, 정확한 인자
        verify(gameService, times(1)).handleAnswer(roomId, userId, answer);
        verifyNoMoreInteractions(gameService);
    }

    @Test
    void submitAnswer_throws_when_principal_missing() {
        var ex = assertThrows(IllegalStateException.class, () ->
                controller.submitAnswer(new GameAnsRequestMessage("고향역", 1L, GameMessageType.ANSWER_RESULT, 101L), null)
        );
        System.out.println(ex.getMessage());
        assertTrue(ex.getMessage().contains("없습니다"));
    }
}
