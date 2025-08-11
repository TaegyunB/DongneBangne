package S13P11A708.backend.domain;

import S13P11A708.backend.domain.common.BaseEntity;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Builder
@NoArgsConstructor
@Table(name = "senior_center")
public class SeniorCenter extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "senior_center_id")
    private Long id;

    @Column(name = "admin_user_id")
    private Long adminUserId;

    @Column(name = "center_name", nullable = false)
    private String centerName;

    @Column(name = "address", nullable = false)
    private String address;

    @Column(name = "trot_point")
    @Builder.Default
    private Long trotPoint = 0L;

    @Column(name = "challenge_point")
    @Builder.Default
    private Long challengePoint = 0L;

    @Column(name = "total_point")
    @Builder.Default
    private Long totalPoint = 0L;

    @Column(name = "ranking_year")
    private Integer rankingYear;

    @Column(name = "rankingMonth")
    private Integer rankingMonth;

    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;

    // 경로당 소속 회원들 - mappedBy로 User의 seniorCenter 필드와 연결
    @OneToMany(mappedBy = "seniorCenter", fetch = FetchType.LAZY)
    private List<User> members = new ArrayList<>();

    @OneToMany(mappedBy = "seniorCenter", fetch = FetchType.LAZY)
    private List<Challenge> challenges = new ArrayList<>();

    // JPA 라이프사이클 콜백으로 자동 설정
    @Override
    protected void onPrePersist() {
        LocalDateTime now = LocalDateTime.now();
        this.rankingYear = now.getYear();
        this.rankingMonth = now.getMonthValue();
    }


    //==Setter 메서드==//

    /**
     * 경로당 관리자 등록
     */
    public void setAdminUserId(Long adminUserId) {
        this.adminUserId = adminUserId;
    }

    /**
     * 챌린지 포인트 추가 및 총 포인트 업데이트
     */
    public void addChallengePoint(Integer point) {
        if (point != null && point > 0) {
            this.challengePoint += point;
            updateTotalPoint();
        }
    }

    /**
     * 챌린지 포인트 차감 및 총 포인트 업데이트
     */
    public void subtractChallengePoint(Integer point) {
        if (point != null && point > 0) {
            this.challengePoint -= point;
            updateTotalPoint();
        }
    }

    /**
     * 총 포인트 재계산
     */
    private void updateTotalPoint() {
        this.totalPoint = this.trotPoint + this.challengePoint;
    }
}
