package S13P11A708.backend.service;

import S13P11A708.backend.domain.SeniorCenter;
import S13P11A708.backend.repository.SeniorCenterRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SeniorCenterService {

    private final SeniorCenterRepository seniorCenterRepository;

    public List<SeniorCenter> searchCenters(String type, String keyword) {
        if ("name".equalsIgnoreCase(type)) {
            return seniorCenterRepository.findByCenterNameContainingIgnoreCase(keyword);
        } else if ("address".equalsIgnoreCase(type)) {
            return seniorCenterRepository.findByAddressContainingIgnoreCase(keyword);
        }
        return List.of();
    }
}
