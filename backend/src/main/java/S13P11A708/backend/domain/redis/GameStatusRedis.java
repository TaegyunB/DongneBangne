package S13P11A708.backend.domain.redis;

import S13P11A708.backend.domain.enums.GameStatus;
import lombok.*;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GameStatusRedis {
    private Long roomId;
    private int round;
    private int totalRound;
    private Long currentQuestionId;
    private GameStatus status;
    private PlayerStatus user1;
    private PlayerStatus user2;

    public void updateCurrentQuestionId(Long currentQuestionId){
        this.currentQuestionId = currentQuestionId;
    }

    public void nextRound(int round){
        this.round = round;
    }

    public void updateStatus(GameStatus status){
        this.status = status;
    }
}
