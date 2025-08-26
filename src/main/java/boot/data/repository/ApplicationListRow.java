package boot.data.repository;

import java.sql.Timestamp;

public interface ApplicationListRow {
    Long getId();
    Long getPostingId();
    Long getResumeId();
    Long getUserId();
    String getStatus();
    Timestamp getAppliedAt();
    Timestamp getViewedAt();

    // from user_profiles + users + resumes
    String getApplicantName();
    String getApplicantEmail();
    String getResumeTitle();

    // 선택: 대표 포트폴리오 URL (없으면 null)
    String getResumePortfolioUrl();
    
}