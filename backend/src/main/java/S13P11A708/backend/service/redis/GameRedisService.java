package S13P11A708.backend.service.redis;

import S13P11A708.backend.domain.enums.GameStatus;
import S13P11A708.backend.domain.redis.GameStatusRedis;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class GameRedisService {

    private final RedisTemplate<String, Object> redisTemplate;

    private String getKey(Long roomId) {
        return "game:room:" + roomId;
    }

    /**
     * roomId를 Redis key로 변환하여, 게임 상태 전체를 redis에 저장
     * 60분이 지나면 자동으로 삭제되도록 설정
     */
    public void saveGameStatus(Long roomId, GameStatusRedis status) {
        redisTemplate.opsForValue().set(getKey(roomId), status, Duration.ofMinutes(60));
    }

    /**
     * roomId로 게임상태를 Redis에서 조회
     */
    public GameStatusRedis getGameStatus(Long roomId) {
        Object data = redisTemplate.opsForValue().get(getKey(roomId));
        return (data instanceof GameStatusRedis) ? (GameStatusRedis) data : null;
    }

    /**
     * 게임이 끝나면, Redis에 저장된 게임 상태 데이터 삭제
     */
    public void deleteGameStatus(Long roomId) {
        redisTemplate.delete(getKey(roomId));
    }

    /**
     * roomId로 GameStatusRedis 가져오기
     */
    public GameStatusRedis getGameStatusRedis(Long roomId) {
        Object data = redisTemplate.opsForValue().get(getKey(roomId));

        if (data instanceof GameStatusRedis) {
            return (GameStatusRedis) data;
        }

        if (data instanceof Map) {
            ObjectMapper mapper = new ObjectMapper();
            return mapper.convertValue(data, GameStatusRedis.class);
        }

        return null;
    }

    /**
     * 현재 라운드에 해당하는 문제의 id를 redis 상태에 반영
     */
    public void updateCurrentQuestion(Long roomId, Long questionId) {
        GameStatusRedis status = getGameStatusRedis(roomId);
        if (status != null) {
            status.updateCurrentQuestionId(questionId);
            saveGameStatus(roomId, status);
        }
    }

    /**
     * 유저가 맞춘 문제 갯수 올리기
     */
    public void increaseScore(Long roomId, Long userId) {
        GameStatusRedis status = getGameStatusRedis(roomId);
        if (status != null) {
            if (status.getUser1().getUserId().equals(userId)) {
                status.getUser1().updateCorrectCount(status.getUser1().getCorrectCount() + 1);
            } else if (status.getUser2().getUserId().equals(userId)) {
                status.getUser2().updateCorrectCount(status.getUser2().getCorrectCount() + 1);
            }
            saveGameStatus(roomId, status);
        }
    }

    /**
     * hint 사용한 유저는 사용했음을 게임상태 redis에 표시
     */
    public void markHintUsed(Long roomId, Long userId) {
        GameStatusRedis status = getGameStatus(roomId);
        if (status != null) {
            if (status.getUser1().getUserId().equals(userId)) {
                status.getUser1().updateHintUsed(true);
            } else if (status.getUser2().getUserId().equals(userId)) {
                status.getUser2().updateHintUsed(true);
            }
            saveGameStatus(roomId, status);
        }
    }

    /**
     * 해당 라운드가 끝나면 redis를 다음 라운드에 맞게 수정
     * hint 사용 여부 초기화
     * round 수 +1 증가
     */
    public void advanceRound(Long roomId) {
        GameStatusRedis status = getGameStatus(roomId);
        if (status != null) {
            status.nextRound(status.getRound() + 1);
            status.getUser1().updateHintUsed(false);
            status.getUser2().updateHintUsed(false);
            saveGameStatus(roomId, status);
        }
    }

    /**
     * 게임이 종료되면 게임상태를 finished로 바꾼다.
     */
    public void finishGame(Long roomId) {
        GameStatusRedis status = getGameStatus(roomId);
        if (status != null) {
            status.updateStatus(GameStatus.FINISHED);
            saveGameStatus(roomId, status);
        }
    }

}
