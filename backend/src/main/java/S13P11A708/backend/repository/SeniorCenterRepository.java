package S13P11A708.backend.repository;

import S13P11A708.backend.domain.SeniorCenter;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SeniorCenterRepository extends JpaRepository<SeniorCenter, Long> {

    /**
     * 월별 포인트 기준 내림차순 정렬
     */
    @Query("SELECT sc FROM SeniorCenter sc ORDER BY sc.totalPoint DESC, sc.id ASC")
    List<SeniorCenter> orderSeniorCenterByTotalPointDesc();

    /**
     * 페이징 처리된 월별 포인트 기준 내림차순 정렬
     */
    @Query("SELECT sc FROM SeniorCenter sc ORDER BY sc.totalPoint DESC, sc.id ASC ")
    Page<SeniorCenter> orderSeniorCenterByTotalPointDesc(Pageable pageable);

}
