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

    @Column(name = "ai_description", columnDefinition = "LONGTEXT")
    private String aiDescription;

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
     * 도전 수정 setter
     */
    public void updateChallengeInfo(String challengeTitle, String challengePlace, String description) {
        this.challengeTitle = challengeTitle;
        this.challengePlace = challengePlace;
        this.description = description;
    }

    /**
     * 이미지 업로드 setter
     */
    public void updateChallengeImage(String challengeImage) {
        this.challengeImage = challengeImage;
    }

    /**
     * 이미지 설명 등록 setter
     */
    public void updateImageDescription(String imageDescription) {
        this.imageDescription = imageDescription;
    }

    /**
     * AI 요약 등록 setter
     */
    public void updateAiDescription(String aiDescription) { this.aiDescription = aiDescription; }

    /**
     * 도전 완료 setter
     */
    public void completeChallenge() {
        this.isSuccess = true;
    }

    /**
     * 도전 완료 취소 setter
     */
    public void cancelCompletion() { this.isSuccess = false; }

    /**
     * AI 신문과 연관관계 설정
     */
    public void setAiNews(AiNews aiNews) {
        this.aiNews = aiNews;
    }

}
