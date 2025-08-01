package S13P11A708.backend.domain;

import S13P11A708.backend.domain.common.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
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

    @Builder.Default
    private Integer point = 300;

    @Column(name = "challenge_image")
    private String challengeImage;

    @Column(name = "image_description")
    private String imageDescription;

    @Builder.Default
    @Column(name = "is_success")
    private Boolean isSuccess = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "senior_center_id")
    private SeniorCenter seniorCenter;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ai_news_id")
    private AiNews aiNews;

    // JPA 라이프사이클 콜백으로 자동 설정
    @Override
    protected void onPrePersist() {
        LocalDateTime now = LocalDateTime.now();
        this.year = now.getYear();
        this.month = now.getMonthValue();
    }

    //==Setter 메서드==//
    /**
     * 이미지 업로드 setter
     */
    public void updateChallengeImage(String challengeImage) {
        this.challengeImage = challengeImage;
    }

    /**
     * 이미지 설명 등록
     */
    public void updateImageDescription(String imageDescription) {
        this.imageDescription = imageDescription;
    }

    public void updateSuccessStatus(Boolean isSuccess) {
        this.isSuccess = isSuccess;
    }



 }
