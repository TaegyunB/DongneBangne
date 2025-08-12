package S13P11A708.backend;

import S13P11A708.backend.domain.TrotQuiz;
import S13P11A708.backend.domain.enums.GameMessageType;
import S13P11A708.backend.domain.game.GameStatusRedis;
import S13P11A708.backend.dto.webSocket.GameInfoSocketMessage;
import S13P11A708.backend.repository.GameHistoryRepository;
import S13P11A708.backend.repository.GameRoomRepository;
import S13P11A708.backend.repository.TrotQuizRepository;
import S13P11A708.backend.repository.UserRepository;
import S13P11A708.backend.service.GameService;
import S13P11A708.backend.service.UserService;
import S13P11A708.backend.service.redis.GameRedisService;
import S13P11A708.backend.websocket.GameBroadcaster;
import S13P11A708.backend.websocket.GameMessageFactory;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import static org.mockito.Mockito.*;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

@SpringBootTest
@Testcontainers
@Import(GameHandleHintTest.TestConfig.class)
public class GameHandleHintTest {

    @Container
    static GenericContainer<?> redis = new GenericContainer<>("redis:7-alpine").withExposedPorts(6379);

    @DynamicPropertySource
    static void props(DynamicPropertyRegistry r) {
        r.add("spring.data.redis.host", () -> redis.getHost());
        r.add("spring.data.redis.port", () -> redis.getMappedPort(6379));
    }

    @TestConfiguration
    static class TestConfig {
        @Bean @Primary GameMessageFactory messageFactory() { return mock(GameMessageFactory.class); }
        @Bean @Primary GameBroadcaster broadcaster() { return mock(GameBroadcaster.class); }
        @Bean @Primary GameRoomRepository gameRoomRepository() { return mock(GameRoomRepository.class); }
        @Bean @Primary UserRepository userRepository() { return mock(UserRepository.class); }
        @Bean @Primary TrotQuizRepository trotQuizRepository() { return mock(TrotQuizRepository.class); }
        @Bean @Primary UserService userService() { return mock(UserService.class); }
        @Bean @Primary GameHistoryRepository gameHistoryRepository() { return mock(GameHistoryRepository.class); }
    }

    @Autowired GameService gameService;          // ← 스프링이 관리하는 실제 서비스 빈
    @Autowired GameRedisService gameRedisService;// ← 실제 Redis 사용

    // 스텁/검증에 사용할 Mock 빈들 주입
    @Autowired GameMessageFactory messageFactory;
    @Autowired GameBroadcaster broadcaster;       // ⬅️ Mock 빈

    private TrotQuiz quiz(long id, String ans, String url) {
        TrotQuiz q = new TrotQuiz();
        q.setId(id); q.setAnswer(ans); q.setUrl(url);
        return q;
    }
    private GameInfoSocketMessage info(GameMessageType t, Long roomId, String payload) {
        GameInfoSocketMessage m = new GameInfoSocketMessage();
        m.setType(t); m.setRoomId(roomId); m.setPayload(payload);
        return m;
    }

    @Test
    void handleHint_success_sendsFirstChar_toThatUser_and_updatesRedis() {
        Long roomId = 2001L, u1 = 1L, u2 = 2L;
        gameRedisService.initGame(roomId, 1, u1, 50L, u2, 0L,
                List.of(10L), quiz(10L, "사랑해", "url"));

        when(messageFactory.createHintMessage(
                eq(GameMessageType.HINT_RESPONSE), eq(roomId), eq(u1),true, any(String.class)
        )).thenAnswer(inv ->
                info(GameMessageType.HINT_RESPONSE, roomId, inv.getArgument(3, String.class))
        );

        gameService.handleHint(roomId, u1);

        verify(broadcaster, times(1))
                .sendToUser(eq(u1), argThat(msg ->
                        msg.getType()==GameMessageType.HINT_RESPONSE &&
                                "사".equals(msg.getPayload())   // DTO에 payload가 맞는지 확인. message면 바꾸세요.
                ));

        GameStatusRedis s = gameRedisService.getGameStatusRedis(roomId);
        assertEquals(30L, s.getUser1().getPoint());
        assertTrue(s.getUser1().isHintUsed());
        assertEquals(1, s.getUser1().getHintUsedCount());
    }

    @Test
    void handleHint_reject_when_insufficientPoint_or_alreadyUsed() {
        Long roomId = 2002L, u1 = 1L, u2 = 2L;
        gameRedisService.initGame(roomId, 1, u1, 10L, u2, 100L,
                List.of(99L), quiz(99L, "정답", "url"));

        when(messageFactory.createHintMessage(
                eq(GameMessageType.HINT_REJECTED), eq(roomId), anyLong(), true, any(String.class)
        )).thenAnswer(inv ->
                info(GameMessageType.HINT_REJECTED, roomId, inv.getArgument(3, String.class))
        );

        gameService.handleHint(roomId, u1);
        verify(broadcaster, atLeastOnce())
                .sendToUser(eq(u1), argThat(m -> m.getType()==GameMessageType.HINT_REJECTED));

        GameStatusRedis s1 = gameRedisService.getGameStatusRedis(roomId);
        assertEquals(10L, s1.getUser1().getPoint());
        assertFalse(s1.getUser1().isHintUsed());
        assertEquals(0, s1.getUser1().getHintUsedCount());

        when(messageFactory.createHintMessage(
                eq(GameMessageType.HINT_RESPONSE), eq(roomId), eq(u2), true, any(String.class)
        )).thenAnswer(inv ->
                info(GameMessageType.HINT_RESPONSE, roomId, inv.getArgument(3, String.class))
        );

        gameService.handleHint(roomId, u2); // 성공 1회
        gameService.handleHint(roomId, u2); // 재요청 → 거절

        verify(broadcaster, atLeastOnce())
                .sendToUser(eq(u2), argThat(m -> m.getType()==GameMessageType.HINT_REJECTED));

        GameStatusRedis s2 = gameRedisService.getGameStatusRedis(roomId);
        assertTrue(s2.getUser2().isHintUsed());
        assertEquals(1, s2.getUser2().getHintUsedCount());
    }

}
