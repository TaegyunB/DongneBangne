package S13P11A708.backend.domain.game;

import S13P11A708.backend.domain.enums.GameStatus;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;

import java.time.LocalDateTime;
import java.util.List;

@RedisHash(value="gameStatusRedis", timeToLive = 60)
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GameStatusRedis {
    @Id
    private Long roomId;

    private int round;
    private int totalRound;
    private List<Long> quizIdList;
    private Long currentQuizId;
    private String currentAnswer;
    private String currentUrl;
    private GameStatus status;
    private PlayerStatus user1;
    private PlayerStatus user2;
    private LocalDateTime startedAt;

    //PlayerStatus에서 사용자 정보 가져오기
    public PlayerStatus getPlayerStatus(Long userId) {
        if (user1.getUserId().equals(userId)) return user1;
        if (user2.getUserId().equals(userId)) return user2;
        throw new IllegalArgumentException("해당 userId는 게임방에 없습니다.");
    }

    public boolean bothPlayerAnswered() {
        return user1.isAnswered() && user2.isAnswered();
    }

    public void updateCurrentQuizId(Long quizId){
        this.currentQuizId = quizId;
    }

    public void updateCurrentAnswer(String answer){
        this.currentAnswer = answer;
    }

    public void updateCurrentUrl(String url){
        this.currentUrl = url;
    }

    public void nextRound(int round){
        this.round = round;
    }

    public void updateStatus(GameStatus status){
        this.status = status;
    }
}
