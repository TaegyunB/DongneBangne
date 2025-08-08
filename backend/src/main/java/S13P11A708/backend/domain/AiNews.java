package S13P11A708.backend.domain;

import S13P11A708.backend.domain.common.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "ai_news")
public class AiNews extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ai_news_id")
    private Long id;

    @Column(name = "news_title")
    private String newsTitle;

    @Column(nullable = false)
    private Integer year;

    @Column(nullable = false)
    private Integer month;

    @Column(name = "pdf_url")
    private String pdfUrl;

    @Column(name = "is_generated")
    @Builder.Default
    private boolean isGenerated = false;

    // 해당 월의 랭킹 정보들
    // @OneToMany(mappedBy = "aiNews", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    // private List<Ranking> rankings = new ArrayList<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "senior_center_id")
    private SeniorCenter seniorCenter;

    // 해당 월의 도전 내용들
    @OneToMany(mappedBy = "aiNews", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<Challenge> challenges = new ArrayList<>();

    //== Setter 메서드 ==//
    /**
     * PDF URL 업데이트
     */
    public void updatePdfUrl(String pdfUrl) {
        this.pdfUrl = pdfUrl;
    }

    /**
     * PDF 생성 여부 업데이트
     */
    public void updateGenerated(Boolean isGenerated) {
        this.isGenerated = isGenerated;
    }


}
