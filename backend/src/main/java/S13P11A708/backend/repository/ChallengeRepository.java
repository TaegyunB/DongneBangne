package S13P11A708.backend.repository;

import S13P11A708.backend.domain.Challenge;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChallengeRepository extends JpaRepository<Challenge, Long> {

    /**
     * 특정 경로당의 특정 년월 도전 목록 조회
     */
    @Query("SELECT c FROM Challenge c WHERE c.seniorCenter.id = :seniorCenterId AND c.year = :year AND c.month = :month")
    List<Challenge> findChallengesByYearAndMonth(
            @Param("seniorCenterId")Long seniorCenterId,
            @Param("year")Integer year,
            @Param("month")Integer month);

    /**
     * 특정 경로당의 특정 년월 성공한 도전 개수 조회
     */
    @Query("SELECT COUNT(c) FROM Challenge c WHERE c.seniorCenter.id = :seniorCenterId " +
            "AND c.year = :year AND c.month = :month AND c.isSuccess = true")
    Long countSuccessChallengeByYearAndMonth(
            @Param("seniorCenterId") Long seniorCenterId,
            @Param("year") Integer year,
            @Param("month") Integer month);

    /**
     * 특정 도전 상세 조회
     */
    @Query("SELECT c FROM Challenge c WHERE c.id = :challengeId AND c.seniorCenter.id = :seniorCenterId")
    Challenge findChallengeByIdAndSeniorCenterId(@Param("challengeId") Long challengeId,
                                                 @Param("seniorCenterId") Long seniorCenterId);

    /**
     * 특정 경로당의 특정 년월 완료된 도전 목록 조회 (AI 신문 생성용)
     */
    @Query("SELECT c FROM Challenge c WHERE c.seniorCenter.id = :seniorCenterId " +
            "AND c.year = :year AND c.month = :month AND c.isSuccess = true " +
            "ORDER BY c.createdAt ASC")
    List<Challenge> findCompletedChallengesByYearAndMonth(
            @Param("seniorCenterId") Long seniorCenterId,
            @Param("year") Integer year,
            @Param("month") Integer month);
}
