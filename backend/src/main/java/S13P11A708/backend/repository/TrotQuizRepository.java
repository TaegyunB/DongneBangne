package S13P11A708.backend.repository;

import S13P11A708.backend.domain.TrotQuiz;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface TrotQuizRepository extends JpaRepository<TrotQuiz, Long> {

    @Query(value = """
        SELECT * FROM trot_quiz 
        ORDER BY RAND()
        LIMIT :roundCount
    """, nativeQuery = true)
    List<TrotQuiz> findRandomQuestions(
            @Param("roundCount") int roundCount
    );
}
