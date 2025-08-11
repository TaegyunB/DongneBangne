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
public class GameAnsRedisTest {

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
    void initGame_and_advanceRound_and_finish() {
        Long roomId = 101L;
        Long u1 = 1L, u2 = 2L;
        List<Long> qids = List.of(11L, 22L, 33L);
        TrotQuiz first = quiz(11L, "정답1", "url1");

        //1. 게임초기화
        gameRedisService.initGame(roomId, 3, u1, 100L, u2, 80L, qids, first);
        //2.게임상태 가져오기
        GameStatusRedis s1 = gameRedisService.getGameStatusRedis(roomId);
        assertNotNull(s1);
        assertEquals(1, s1.getRound());
        assertEquals(11L, s1.getCurrentQuizId());
        assertEquals("정답1", s1.getCurrentAnswer());
        assertFalse(s1.getUser1().isAnswered());
        assertFalse(s1.getUser2().isAnswered());
        System.out.println("round=" + s1.getRound() + ", currentQuizId=" + s1.getCurrentQuizId() + ", answer=" + s1.getCurrentAnswer());

        // 정답 카운트 +1
        gameRedisService.increaseCount(roomId, u1);
        GameStatusRedis s2 = gameRedisService.getGameStatusRedis(roomId);
        assertEquals(1, s2.getUser1().getCorrectCount());
        System.out.println("round=" + s2.getRound() + ", currentQuizId=" + s2.getCurrentQuizId() + ", answer=" + s2.getCurrentAnswer());

        // 다음 라운드
        TrotQuiz second = quiz(22L, "정답2", "url2");
        gameRedisService.advanceRound(roomId, second);
        GameStatusRedis s3 = gameRedisService.getGameStatusRedis(roomId);
        System.out.println("round=" + s3.getRound() + ", currentQuizId=" + s3.getCurrentQuizId() + ", answer=" + s3.getCurrentAnswer());
        assertEquals(2, s3.getRound());
        assertEquals(22L, s3.getCurrentQuizId());
        assertEquals("정답2", s3.getCurrentAnswer());
        assertFalse(s3.getUser1().isAnswered());
        assertFalse(s3.getUser2().isAnswered());

        // 종료
        gameRedisService.finishGame(roomId);
        assertNull(gameRedisService.getGameStatusRedis(roomId));
    }
}
