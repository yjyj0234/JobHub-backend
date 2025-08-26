
package boot.data.dto.proofread;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ProofreadResponse {
    private final String corrected;
    private final List<Issue> issues;

    @Getter
    @AllArgsConstructor
    public static class Issue {
        private final int start;         // 원문 기준 시작 오프셋
        private final int end;           // 원문 기준 끝 오프셋(미포함)
        private final String original;   // 원문 조각
        private final String suggestion; // 교정안(없으면 "" 가능)
        private final String message;    // 설명
        private final String type;       // 'SPELL' | 'SPACING' | 'STYLE' 등
    }
}
