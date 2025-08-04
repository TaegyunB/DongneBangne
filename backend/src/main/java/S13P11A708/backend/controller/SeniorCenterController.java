package S13P11A708.backend.controller;

import S13P11A708.backend.domain.SeniorCenter;
import S13P11A708.backend.service.SeniorCenterService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/senior-centers")
public class SeniorCenterController {

    private final SeniorCenterService seniorCenterService;

    // 경로당 이름/주소로 검색 (type: name/address, keyword: 검색어)
    @GetMapping
    public ResponseEntity<List<SeniorCenterDto>> searchCenters(
            @RequestParam("type") String type,
            @RequestParam("keyword") String keyword
    ) {
        System.out.println("==== SeniorCenterController :: searchCenters 진입 ====");
        List<SeniorCenter> centers = seniorCenterService.searchCenters(type, keyword);

        // DTO로 변환 (필요 필드만 보내기)
        List<SeniorCenterDto> dtoList = centers.stream()
                .map(SeniorCenterDto::from)
                .collect(Collectors.toList());

        return ResponseEntity.ok(dtoList);
    }

    // 내부 응답용 DTO 클래스 (id, name, address만)
    public static class SeniorCenterDto {
        public Long id;
        public String name;
        public String address;

        public static SeniorCenterDto from(SeniorCenter center) {
            SeniorCenterDto dto = new SeniorCenterDto();
            dto.id = center.getId();
            dto.name = center.getCenterName();
            dto.address = center.getAddress();
            return dto;
        }
    }
}
