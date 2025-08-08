package S13P11A708.backend;

import S13P11A708.backend.domain.TrotQuiz;
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
public class GameHintRedisTest {

    @Container
    static GenericContainer<?> redis = new GenericContainer<>("redis:7-alpine").withExposedPorts(6379);

    @DynamicPropertySource
    static void props(DynamicPropertyRegistry r) {
        r.add("spring.data.redis.host", () -> redis.getHost());
        r.add("spring.data.redis.port", () -> redis.getMappedPort(6379));
    }

    @Autowired
    GameRedisService gameRedisService;

    private TrotQuiz quiz(long id, String ans, String url) {
        TrotQuiz q = new TrotQuiz();
        q.setId(id); q.setAnswer(ans); q.setUrl(url);
        return q;
    }

    @Test
    void hint_flow_pointDeduct_flagSet_countIncreased_and_reuseBlocked() {
        // given
        Long roomId = 1001L, u1 = 1L, u2 = 2L;
        // HINT_COST = 20 이므로 50 포인트면 사용 가능
        gameRedisService.initGame(roomId, 1, u1, 50L, u2, 10L,
                List.of(1L), quiz(1L, "사랑", "url"));

        // when - 사용 전 체크
        assertTrue(gameRedisService.canUseHint(roomId, u1));

        // 포인트 차감
        assertTrue(gameRedisService.deductPointForHint(roomId, u1));
        // 사용 표시
        gameRedisService.markHintUsed(roomId, u1);

        // then - 상태 확인
        GameStatusRedis s = gameRedisService.getGameStatusRedis(roomId);
        assertEquals(30L, s.getUser1().getPoint());          // 50 - 20
        assertTrue(s.getUser1().isHintUsed());
        assertEquals(1, s.getUser1().getHintUsedCount());

        // 이미 사용했으면 재사용 불가
        assertFalse(gameRedisService.canUseHint(roomId, u1));
        // 부족 포인트 유저는 처음부터 불가
        assertFalse(gameRedisService.canUseHint(roomId, u2));
        assertFalse(gameRedisService.deductPointForHint(roomId, u2));
    }

    @Test
    void hint_pointExactlyCost_isAllowedOnce() {
        Long roomId = 1002L, u1 = 1L, u2 = 2L;
        gameRedisService.initGame(roomId, 1, u1, 20L, u2, 0L,
                List.of(1L), quiz(1L, "정답", "url"));

        assertTrue(gameRedisService.canUseHint(roomId, u1));
        assertTrue(gameRedisService.deductPointForHint(roomId, u1));
        gameRedisService.markHintUsed(roomId, u1);

        GameStatusRedis s = gameRedisService.getGameStatusRedis(roomId);
        assertEquals(0L, s.getUser1().getPoint());
        assertTrue(s.getUser1().isHintUsed());
        assertEquals(1, s.getUser1().getHintUsedCount());

        // 재사용 불가 (포인트 0 + 이미 사용)
        assertFalse(gameRedisService.canUseHint(roomId, u1));
    }

}
