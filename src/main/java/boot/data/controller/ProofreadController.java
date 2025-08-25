
package boot.data.controller;

import boot.data.dto.proofread.ProofreadRequest;
import boot.data.dto.proofread.ProofreadResponse;
import boot.data.service.proofread.ProofreadService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/proofread")
@RequiredArgsConstructor
public class ProofreadController {

    private final ProofreadService proofreadService;

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ProofreadResponse proofread(@Valid @RequestBody ProofreadRequest req) {
        return proofreadService.proofread(req.getText());
    }
}
