package S13P11A708.backend.domain;

import S13P11A708.backend.domain.common.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor
@Table(name = "ai_news")
public class AiNews extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ai_news_id")
    private Long id;

    @Column(name = "news_title", nullable = false)
    private String newsTitle;

    @Column(name = "news_content")
    private String newsContent;

    @Column(nullable = false)
    private Integer year;

    @Column(nullable = false)
    private Integer month;

    // 해당 월의 랭킹 정보들
    @OneToMany(mappedBy = "aiNews", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Ranking> rankings = new ArrayList<>();

    // 해당 월의 도전 내용들 =
    @OneToMany(mappedBy = "aiNews", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Challenge> challenges = new ArrayList<>();


}
