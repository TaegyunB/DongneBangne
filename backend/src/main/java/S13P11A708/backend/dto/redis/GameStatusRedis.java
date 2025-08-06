package S13P11A708.backend.dto.redis;

import S13P11A708.backend.domain.enums.GameStatus;
import lombok.*;

import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GameStatusRedis {
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

    //PlayerStatus에서 사용자 정보 가져오기
    public PlayerStatus getPlayerStatus(Long userId) {
        if (user1.getUserId().equals(userId)) return user1;
        if (user2.getUserId().equals(userId)) return user2;
        throw new IllegalArgumentException("해당 userId는 게임방에 없습니다.");
    }

    public boolean bothPlayerAnswered() {
        return user1.isAnswered() && user2.isAnswered();
    }

    public void updateCurrentQuestionId(Long currentQuestionId){
        this.currentQuizId = currentQuizId;
    }

    public void nextRound(int round){
        this.round = round;
    }

    public void updateStatus(GameStatus status){
        this.status = status;
    }
}
