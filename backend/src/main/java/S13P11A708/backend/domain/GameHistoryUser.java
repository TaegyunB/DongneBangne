package S13P11A708.backend.domain;

import S13P11A708.backend.domain.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Builder
@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "game_history_user")
public class GameHistoryUser extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "game_history_user_id")
    private Long Id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "game_history_id")
    private GameHistory gameHistory;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "senior_center_id")
    private SeniorCenter seniorCenter;

    @Column(name = "correct_count")
    private Integer correctCount;

    @Column(name = "hint_used")
    private Integer hintUsed;

    @Column(name = "is_winner")
    private Boolean isWinner;

    public static GameHistoryUser of(GameHistory history,
                                     User user,
                                     SeniorCenter seniorCenter,
                                     int correctCount,
                                     int hintUsed,
                                     boolean isWinner) {
        return GameHistoryUser.builder()
                .gameHistory(history)
                .user(user)
                .seniorCenter(seniorCenter)
                .correctCount(correctCount)
                .hintUsed(hintUsed)
                .isWinner(isWinner)
                .build();
    }
}
