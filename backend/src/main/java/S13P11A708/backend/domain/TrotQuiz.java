package S13P11A708.backend.domain;

import S13P11A708.backend.domain.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "trot_quiz")
public class TrotQuiz extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "trot_quiz_id")
    private Long id;

    @Column(name = "title")
    private String title;

    @Column(name = "singer")
    private String singer;

    @Column(name = "answer")
    private String answer;

    @Column(name = "url")
    private String url;
}
