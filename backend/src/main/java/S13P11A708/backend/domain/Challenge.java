package S13P11A708.backend.domain;

import S13P11A708.backend.domain.common.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor
@Table(name = "challenge")
public class Challenge extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "challenge_id")
    private Long id;

    @Column(name = "challenge_title", nullable = false)
    private String challengeTitle;

    @Column(name = "challenge_place")
    private String challengePlace;

    private String description;

    private Integer year;

    private Integer month;

    private Integer point;

    @Column(name = "challenge_image")
    private String challengeImage;

    @Column(name = "image_description")
    private String imageDescription;

    @Column(name = "is_success")
    private Boolean isSuccess = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "senior_center_id")
    private SeniorCenter seniorCenter;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ai_news_id")
    private AiNews aiNews;
}
