package S13P11A708.backend;

import S13P11A708.backend.domain.TrotQuiz;
import S13P11A708.backend.domain.enums.GameStatus;
import S13P11A708.backend.domain.game.GameStatusRedis;
import S13P11A708.backend.service.redis.GameRedisService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

@SpringBootTest
@Testcontainers
public class GameEndRedisTest {

    @Container
    static GenericContainer<?> redis = new GenericContainer<>("redis:7-alpine").withExposedPorts(6379);

    @DynamicPropertySource
    static void redisProps(DynamicPropertyRegistry r) {
        r.add("spring.data.redis.host", () -> redis.getHost());
        r.add("spring.data.redis.port", () -> redis.getMappedPort(6379));
    }

    @Autowired
    GameRedisService gameRedisService;

    private TrotQuiz quiz(long id, String answer, String url) {
        TrotQuiz q = new TrotQuiz();
        q.setId(id); q.setAnswer(answer); q.setUrl(url);
        return q;
    }

    @Test
    void finishGame_deletesKey_and_isIdempotent() {
        // given: 방 생성
        Long roomId = 9001L, u1 = 1L, u2 = 2L;
        gameRedisService.initGame(
                roomId, 2, u1, 100L, u2, 80L,
                List.of(11L, 22L), quiz(11L, "정답1", "url1"));

        // sanity check: 시작 상태 저장되어 있음
        GameStatusRedis before = gameRedisService.getGameStatusRedis(roomId);
        assertNotNull(before);
        assertEquals(1, before.getRound());
        assertEquals(GameStatus.PROGRESS, before.getStatus());

        // when: 종료
        gameRedisService.finishGame(roomId);

        // then: 키가 삭제되어 더 이상 조회되지 않음
        GameStatusRedis after = gameRedisService.getGameStatusRedis(roomId);
        assertNull(after, "finishGame 후에는 Redis 키가 없어야 합니다");

        // and: 멱등성 — 여러 번 호출해도 문제 없어야 함
        gameRedisService.finishGame(roomId); // 두 번째 호출
        assertNull(gameRedisService.getGameStatusRedis(roomId));
    }

    @Test
    void finishGame_blocksFurtherOperations_like_hint_or_count() {
        Long roomId = 9002L, u1 = 1L, u2 = 2L;
        gameRedisService.initGame(
                roomId, 1, u1, 50L, u2, 50L,
                List.of(99L), quiz(99L, "끝", "url"));

        // 종료
        gameRedisService.finishGame(roomId);
        assertNull(gameRedisService.getGameStatusRedis(roomId));

        // 이후 동작들이 효과 없어야 함(내부에서 null 처리)
        gameRedisService.increaseCount(roomId, u1);
        assertNull(gameRedisService.getGameStatusRedis(roomId));

        boolean canUse = gameRedisService.canUseHint(roomId, u1);
        assertFalse(canUse, "키 삭제 후에는 힌트 사용 불가여야 함");
    }
}
