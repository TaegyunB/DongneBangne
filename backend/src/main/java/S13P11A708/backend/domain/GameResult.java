package S13P11A708.backend.domain;

import S13P11A708.backend.domain.common.BaseEntity;
import S13P11A708.backend.domain.enums.GameStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor
@Table(name = "game_result")
public class GameResult extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "game_result_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "game_type_id")
    private GameType gameType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "player1_id")
    private SeniorCenter player1;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "player2_id")
    private SeniorCenter player2;

    @ManyToOne(fetch = FetchType.LAZY)
    private SeniorCenter winner;

    @Column(name = "earned_point")
    private Integer earnedPoint;

    @Enumerated(EnumType.STRING)
    private GameStatus status;

}
