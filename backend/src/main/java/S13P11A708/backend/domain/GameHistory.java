package S13P11A708.backend.domain;

import S13P11A708.backend.domain.common.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "game_history")
public class GameHistory extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "game_history_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "game_room_id")
    private GameRoom gameRoomId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "winner_user_id")
    private User winnerUserId;

    @Column(name = "game_round")
    private Integer gameRound;

    @Column(name = "music_era")
    private String musicEra;

    @Column(name = "category")
    private String category;

    @Column(name = "started_at")
    private LocalDateTime startedAt;

    @Column(name = "ended_at")
    private LocalDateTime endedAt;

    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "gameHistory", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<GameHistoryUser> historyUsers = new ArrayList<>();

    public static GameHistory of(GameRoom room,
                                 User winner,
                                 LocalDateTime startedAt,
                                 LocalDateTime endedAt,
                                 Integer round,
                                 String era,
                                 String category) {
        return GameHistory.builder()
                .gameRoomId(room)
                .winnerUserId(winner)
                .startedAt(startedAt)
                .endedAt(endedAt)
                .gameRound(round)
                .musicEra(era)
                .category(category)
                .build();
    }

}
