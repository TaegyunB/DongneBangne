package S13P11A708.backend.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import S13P11A708.backend.domain.AiNews;

@Repository
public interface AiNewsRepository extends JpaRepository<AiNews, Long> {

    /**
     * 특정 경로당의 AI 신문 목록 조회 (최신순)
     */
    @Query("SELECT an FROM AiNews an WHERE an.seniorCenter.id = :seniorCenterId ORDER BY an.createdAt DESC")
    List<AiNews> findBySeniorCenterIdOrderByCreatedAtDesc(
            @Param("seniorCenterId") Long seniorCenterId);

    /**
     * 특정 경로당의 특정 년월 AI 신문 조회
     */
    @Query("SELECT an FROM AiNews an WHERE an.seniorCenter.id = :seniorCenterId AND an.year = :year AND an.month = :month")
    Optional<AiNews> findAiNewsBySeniorCenterIdAndYearAndMonth(
            @Param("seniorCenterId") Long seniorCenterId,
            @Param("year") Integer year,
            @Param("month") Integer month);
}