package boot.data.entity;

import java.time.LocalDate;

import org.springframework.data.domain.Persistable;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PostLoad;
import jakarta.persistence.PostPersist;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import lombok.Data;

@Entity
@Data
@Table(name = "user_profiles")
public class UserProfiles implements Persistable<Long> {

    @Id
    private Long userId;

    // Users 테이블의 기본 키를 외래 키로 사용 (1:1)
    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "user_id")
    private Users user;

    @Column(nullable = false, length = 100)
    private String name;

    private String phone;

    // (호환 유지) 연도만 저장하던 컬럼
    private Short birthYear;

    // ✅ 신규: 생년월일
    @Column(name = "birth_date")
    private LocalDate birthDate;

    @Lob
    private String profileImageUrl;

    @Column(length = 255)
    private String headline;

    @Lob
    private String summary;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "location_region_id")
    private Regions region;

    /* ===== Persistable 구현: 새 엔티티일 때 INSERT 강제 ===== */
    @Transient
    private boolean isNew = true;

    @PostLoad
    @PostPersist
    private void markNotNew() { this.isNew = false; }

    public void markNew() { this.isNew = true; }

    @Override public Long getId() { return userId; }
    @Override public boolean isNew() { return isNew; }
}
