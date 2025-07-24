package S13P11A708.backend.domain;

import S13P11A708.backend.domain.common.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor
@Table(name = "game_type")
public class GameType extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "game_type_id")
    private Long id;

    @Column(name = "game_name", nullable = false)
    private String gameName;

    @Column(name = "game_point", nullable = false)
    private Integer gamePoint;

}
