package boot.data.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import boot.data.dto.UserProfilesDto;
import boot.data.entity.Regions;
import boot.data.entity.UserProfiles;
import boot.data.entity.Users;
import boot.data.repository.RegionRepository;
import boot.data.repository.UserProfilesRepository;
import boot.data.repository.UsersRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserProfilesService {

    private final UserProfilesRepository profilesRepo;
    private final UsersRepository usersRepo;
    private final RegionRepository regionsRepo;

    @Transactional(readOnly = true)
    public UserProfilesDto getOrDefault(Long userId) {
        return profilesRepo.findByUserId(userId)
            .map(UserProfilesDto::from)
            .orElse(new UserProfilesDto(userId, null, null, null, null, null, null, null, null, null));
    }

    @Transactional
    public UserProfilesDto upsert(Long userId, UserProfilesDto dto) {
        UserProfiles profile = profilesRepo.findByUserId(userId).orElseGet(() -> {
            Users u = usersRepo.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));
            UserProfiles p = new UserProfiles();
            p.setUser(u);
            p.setUserId(userId);
            p.markNew(); // ✅ 신규 INSERT 유도
            return p;
        });

        // 필드 세팅
        profile.setName(dto.name());
        profile.setPhone(dto.phone());

        // (호환) 연도 컬럼은 프론트가 null 보내도록 권장
        profile.setBirthYear(dto.birthYear());

        // ✅ 생년월일(LocalDate) 저장
        profile.setBirthDate(dto.birthDate());

        profile.setProfileImageUrl(dto.profileImageUrl());
        profile.setHeadline(dto.headline());
        profile.setSummary(dto.summary());

        // 지역 매핑: id 우선, 없으면 name으로
        Regions region = null;
        if (dto.regionId() != null) {
            region = regionsRepo.findById(dto.regionId())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 지역입니다."));
        } else if (dto.regionName() != null && !dto.regionName().isBlank()) {
            String name = dto.regionName().trim();
            region = regionsRepo.findFirstByNameIgnoreCase(name)
                .or(() -> regionsRepo.findFirstByName(name))
                .orElse(null);
        }
        profile.setRegion(region);

        // 새 엔티티면 persist, 기존이면 dirty checking (save는 둘 다 안전)
        return UserProfilesDto.from(profilesRepo.save(profile));
    }
}
