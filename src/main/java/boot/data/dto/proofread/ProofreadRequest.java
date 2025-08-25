
package boot.data.dto.proofread;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ProofreadRequest {
    @NotBlank
    private String text;
}
