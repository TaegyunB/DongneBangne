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
public class GameStartRedisTest {

    @Container
    static GenericContainer<?> redis =
            new GenericContainer<>("redis:7-alpine").withExposedPorts(6379);

    @DynamicPropertySource
    static void redisProps(DynamicPropertyRegistry r) {
        r.add("spring.data.redis.host", () -> redis.getHost());
        r.add("spring.data.redis.port", () -> redis.getMappedPort(6379));
    }

    @Autowired
    GameRedisService gameRedisService;

    // 더미 엔티티/DTO
    private TrotQuiz quiz(long id, String answer, String url) {
        TrotQuiz q = new TrotQuiz();
        // 필요 필드만 세팅 – 너의 엔티티에 맞게 수정
        q.setId(id);
        q.setAnswer(answer);
        q.setUrl(url);
        return q;
    }

    @Test
    void initGame_savesInitialStateToRedis() {
        // given
        Long roomId = 777L;
        int totalRound = 3;
        Long user1 = 11L, user2 = 22L;
        Long point1 = 150L, point2 = 90L;
        List<Long> quizIds = List.of(101L, 102L, 103L);
        TrotQuiz first = quiz(101L, "사랑의트로트", "https://youtu.be/abc");

        // when
        gameRedisService.initGame(roomId, totalRound, user1, point1, user2, point2, quizIds, first);

        // then
        GameStatusRedis s = gameRedisService.getGameStatusRedis(roomId);
        assertNotNull(s);
        assertEquals(roomId, s.getRoomId());
        assertEquals(GameStatus.PROGRESS, s.getStatus());
        assertEquals(1, s.getRound());
        assertEquals(totalRound, s.getTotalRound());
        assertEquals(quizIds, s.getQuizIdList());
        assertEquals(first.getId(), s.getCurrentQuizId());
        assertEquals(first.getAnswer(), s.getCurrentAnswer());
        assertEquals(first.getUrl(), s.getCurrentUrl());

        assertNotNull(s.getUser1());
        assertEquals(user1, s.getUser1().getUserId());
        assertEquals(point1, s.getUser1().getPoint());
        assertEquals(0, s.getUser1().getCorrectCount());
        assertFalse(s.getUser1().isHintUsed());
        assertFalse(s.getUser1().isAnswered());

        assertNotNull(s.getUser2());
        assertEquals(user2, s.getUser2().getUserId());
        assertEquals(point2, s.getUser2().getPoint());
        assertEquals(0, s.getUser2().getCorrectCount());
        assertFalse(s.getUser2().isHintUsed());
        assertFalse(s.getUser2().isAnswered());
    }

}
