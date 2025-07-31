package S13P11A708.backend.repository;

import S13P11A708.backend.domain.SeniorCenter;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SeniorCenterRepository extends JpaRepository<SeniorCenter, Long> {


}
