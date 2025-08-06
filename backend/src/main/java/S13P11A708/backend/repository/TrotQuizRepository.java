package S13P11A708.backend.repository;

import S13P11A708.backend.domain.TrotQuiz;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface TrotQuizRepository extends JpaRepository<TrotQuiz, Long> {

    @Query(value = """
        SELECT * FROM trot_quiz 
        WHERE (:musicEra IS NULL OR music_era = :musicEra)
          AND (:category IS NULL OR category = :category)
        ORDER BY RAND()
        LIMIT :roundCount
    """, nativeQuery = true)
    List<TrotQuiz> findRandomQuestionsByCondition(
            @Param("musicEra") String musicEra,
            @Param("category") String category,
            @Param("roundCount") int roundCount
    );
}
