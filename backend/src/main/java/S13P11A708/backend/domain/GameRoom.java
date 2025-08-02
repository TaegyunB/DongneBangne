package S13P11A708.backend.domain;

import S13P11A708.backend.domain.common.BaseEntity;
import S13P11A708.backend.domain.enums.GameStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "game_room")
public class GameRoom extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "game_room_id")
    private Long id;

    @Column(name = "room_title")
    private String roomTitle;

    @Column(name = "game_round")
    private Integer gameRound;

    @Column(name = "music_era")
    private String musicEra;

    @Column(name = "category")
    private String category;

    @Enumerated(EnumType.STRING)
    @Column(name = "game_status")
    private GameStatus gameStatus;

    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime createdAt;

    /**
     * 게임방 status 수정 setter
     */
    public void changeGameStatus(GameStatus status){
        this.gameStatus = status;
    }
}
