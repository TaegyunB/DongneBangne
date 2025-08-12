package S13P11A708.backend;

import S13P11A708.backend.controller.GameSocketController;
import S13P11A708.backend.domain.TrotQuiz;
import S13P11A708.backend.domain.enums.GameMessageType;
import S13P11A708.backend.domain.enums.UserRole;
import S13P11A708.backend.domain.game.GameStatusRedis;
import S13P11A708.backend.domain.game.PlayerStatus;
import S13P11A708.backend.dto.webSocket.GameAnsSocketMessage;
import S13P11A708.backend.repository.TrotQuizRepository;
import S13P11A708.backend.security.CustomOAuth2User;
import S13P11A708.backend.security.UserDto;
import S13P11A708.backend.service.GameService;
import S13P11A708.backend.service.redis.GameRedisService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.verifyNoMoreInteractions;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class SocketControllerTest {

    @MockitoBean
    GameService gameService;

    @MockitoBean
    GameRedisService gameRedisService;
    @MockitoBean
    TrotQuizRepository trotQuizRepository;
    @MockitoBean
    SimpMessagingTemplate messagingTemplate;

    // 실제 코드 + 스파이(로그 찍힘)
    @Autowired
    GameSocketController controller;

    @Test
    void submitAnswer_calls_real_service() {

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

        GameStatusRedis state = GameStatusRedis.builder()
                .roomId(roomId).round(1).totalRound(2)
                .currentAnswer(answer).quizIdList(List.of(10L, 20L)).build();

        when(gameRedisService.getGameStatusRedis(roomId)).thenReturn(state);
        when(gameRedisService.getPlayer(roomId, userId)).thenReturn(PlayerStatus.simple(userId, roomId, false));

        // 점수 증가/라운드 전환은 부수효과만 있으면 되므로 doNothing
        doNothing().when(gameRedisService).increaseCount(roomId, userId);
        doAnswer(inv -> { // 다음 라운드로 전환만 흉내
            state.setRound(2); state.setCurrentAnswer("다음정답"); return null;
        }).when(gameRedisService).advanceRound(eq(roomId), any(TrotQuiz.class));

        when(trotQuizRepository.findById(20L))
                .thenReturn(Optional.of(TrotQuiz.builder().id(20L).url("https://y.t/next").build()));

        // 브로드캐스트는 실제 송신 말고 그냥 통과
        doNothing().when(messagingTemplate).convertAndSend(anyString(), any(Object.class));

        controller.submitAnswer(new GameAnsSocketMessage(GameMessageType.ANSWER_SUBMIT, 1L, "고향역"), principal);

        verify(gameService, timeout(1000)).handleAnswer(1L, 101L, "고향역");
    }
}
