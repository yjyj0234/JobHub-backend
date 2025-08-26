
package boot.data.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import boot.data.dto.resume.SkillCreateRequest;
import boot.data.entity.Skills;
import boot.data.repository.resume.SkillsRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class SkillService {

    private final SkillsRepository skillsRepository;

    public Skills createOrGet(SkillCreateRequest req) {
        final String rawName = req.getName().trim();
        return skillsRepository.findByNameIgnoreCase(rawName)
                .orElseGet(() -> {
                    Skills s = new Skills();
                    s.setName(rawName);
                    s.setCategoryId(req.getCategoryId());
                    s.setVerified(Boolean.TRUE.equals(req.getIsVerified()));
                    return skillsRepository.save(s);
                });
    }
}
