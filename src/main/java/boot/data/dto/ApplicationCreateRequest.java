package boot.data.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ApplicationCreateRequest {
    @NotNull
    private Long jobId; //posting_id
    
    @NotNull
    private Long resumeId;  //이력서 id


    //일단 놔두는것들
    private String name;
    private String email;
    private String phone;
    private String coverLetter;
    private String linkGithub;
    private String linkLinkedIn;
    private String linkPortfolio;
    private String expectedSalary;
    private String availableFrom;
}
