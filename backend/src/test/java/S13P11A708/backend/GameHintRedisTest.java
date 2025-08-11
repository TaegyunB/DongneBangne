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

    @Test
    void hint_exactly20_points_allows_once_only() {
        Long roomId = 7101L, u = 1L, v = 2L;
        gameRedisService.initGame(roomId, 1, u, 20L, v, 0L,
                List.of(101L), quiz(101L, "정답", "url"));

        // 1) 정확히 20 → 사용 가능
        assertTrue(gameRedisService.canUseHint(roomId, u));
        assertTrue(gameRedisService.deductPointForHint(roomId, u));
        gameRedisService.markHintUsed(roomId, u);

        GameStatusRedis s1 = gameRedisService.getGameStatusRedis(roomId);
        assertEquals(0L, s1.getUser1().getPoint());
        assertTrue(s1.getUser1().isHintUsed());
        assertEquals(1, s1.getUser1().getHintUsedCount());

        // 2) 재시도는 불가
        assertFalse(gameRedisService.canUseHint(roomId, u));
        assertFalse(gameRedisService.deductPointForHint(roomId, u));
    }

    @Test
    void deduct_should_not_charge_when_already_hintUsed() {
        Long roomId = 7102L, u = 1L, v = 2L;
        gameRedisService.initGame(roomId, 1, u, 100L, v, 0L,
                List.of(102L), quiz(102L, "정답", "url"));

        // 첫 사용: 차감 + 사용표시
        assertTrue(gameRedisService.deductPointForHint(roomId, u));
        gameRedisService.markHintUsed(roomId, u);

        // 이미 사용 true 상태에서 다시 deduct 시도 → ❗현재 구현은 차감됨 (버그)
        boolean deductedAgain = gameRedisService.deductPointForHint(roomId, u);

        // 기대값: false (이미 사용한 라운드에서는 더 이상 차감되면 안 됨)
        assertFalse(deductedAgain, "이미 hintUsed=true이면 포인트가 다시 차감되면 안 됩니다.");

        GameStatusRedis s = gameRedisService.getGameStatusRedis(roomId);
        // 첫 차감만 반영되어 80이 되어야 함
        assertEquals(80L, s.getUser1().getPoint(), "이 값이 60이면 중복 차감된 것입니다.");
    }

    @Test
    void order_can_deduct_mark_is_ok_but_only_once() {
        Long roomId = 7103L, u = 1L, v = 2L;
        gameRedisService.initGame(roomId, 1, u, 50L, v, 0L,
                List.of(103L), quiz(103L, "정답", "url"));

        // canUseHint 확인 없이 바로 deduct → 허용 (한 번만)
        assertTrue(gameRedisService.deductPointForHint(roomId, u)); // 50 -> 30
        gameRedisService.markHintUsed(roomId, u);

        GameStatusRedis s1 = gameRedisService.getGameStatusRedis(roomId);
        assertEquals(30L, s1.getUser1().getPoint());
        assertTrue(s1.getUser1().isHintUsed());
        assertEquals(1, s1.getUser1().getHintUsedCount());

        // 이후 순서가 바뀌어도(다시 can/deduct) 재사용은 불가
        assertFalse(gameRedisService.canUseHint(roomId, u));
        assertFalse(gameRedisService.deductPointForHint(roomId, u));
    }



}
