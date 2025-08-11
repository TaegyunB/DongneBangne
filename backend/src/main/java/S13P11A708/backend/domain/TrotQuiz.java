package S13P11A708.backend.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class TrotQuiz {
    @Id
    @GeneratedValue
    private Long id;

    private String title;
    private String answer;
    private String url;
    private String musicEra;
    private String category;
}
