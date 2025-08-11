package S13P11A708.backend.domain;

import S13P11A708.backend.domain.common.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "game_room_user")
public class GameRoomUser extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "game_room_user_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "game_room_id")
    private GameRoom gameRoomId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User userId;

    @Column(name = "ready", nullable = false)
    @Builder.Default
    private boolean ready = false;

    @Builder
    private GameRoomUser(GameRoom gameRoom, User user, boolean ready) {
        this.gameRoomId = gameRoom;
        this.userId = user;
        this.ready = ready;
    }

    /**
     * 유저 준비 상태 수정 Setter
     */
    public void userGetReady(){
        this.ready = !this.ready;
    }
}
