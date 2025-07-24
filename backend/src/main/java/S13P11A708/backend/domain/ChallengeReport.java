package S13P11A708.backend.domain;

import S13P11A708.backend.domain.Challenge;
import S13P11A708.backend.domain.common.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor
@Table(name = "challenge_report")
public class ChallengeReport extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "challenge_report_id")
    private Long id;

//    @ManyToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "challenge_id", nullable = false)
//    private Challenge challenge;

//    private Long adminUserId;

    @Column(nullable = false)
    private String reason;
}
