// src/main/java/boot/data/service/proofread/ProofreadService.java
package boot.data.service.proofread;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.stereotype.Service;

import boot.data.dto.proofread.ProofreadResponse;

@Service
public class ProofreadService {

    /* ============================== 교정(치환) 규칙 ============================== */
    // ㅎㅎ/ㅋㅋ 제거
    private static final Pattern MULTI_HAHA = Pattern.compile("(ㅋㅋ+|ㅎㅎ+)");
    // "3년6개월" → "3년 6개월"
    private static final Pattern YEAR_MONTH_TIGHT = Pattern.compile("(\\d+)\\s*년\\s*(\\d+)\\s*개월");
    // 연속 공백(스페이스/탭/유니코드 공백 포함) → 하나
    private static final Pattern MULTI_WHITES = Pattern.compile("[\\u00A0\\u2000-\\u200B\\t ]{2,}");
    // 줄 끝 공백 제거
    private static final Pattern TRAIL_SPACE = Pattern.compile("[ \\t\\u00A0\\u2000-\\u200B]+(?=\\r?\\n)");
    // "20 %" → "20%" (퍼센트 앞뒤 불필요 공백 제거)
    private static final Pattern PERCENT_SPACE = Pattern.compile("(\\d+)\\s*%");

    /* ============================== 탐지(분석) 규칙 ============================== */
    // 모호/상투 표현
    private static final List<String> VAGUE_WORDS = List.of("열심히", "최선을", "성실하게", "뭔가", "되게", "정말", "매우", "진짜");
    private static final Pattern VAGUE_PATTERN;
    // 행동 동사(행동 액션 강조)
    private static final List<String> ACTION_VERBS = List.of("개선", "도입", "설계", "구현", "최적화", "자동화", "리팩터링", "모니터링", "장애복구", "튜닝");
    private static final Pattern ACTION_VERB_PATTERN;
    // 문장 분리용 종결 힌트
    private static final Set<String> SENTENCE_END_TOKENS = Set.of("다.", "요.", "함.", "됨.", "임.", "임", "다", "요");

    static {
        // 모호 표현 패턴
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < VAGUE_WORDS.size(); i++) {
            if (i > 0) sb.append("|");
            sb.append(Pattern.quote(VAGUE_WORDS.get(i)));
        }
        VAGUE_PATTERN = Pattern.compile(sb.toString());

        // 행동 동사 패턴
        StringBuilder ab = new StringBuilder();
        for (int i = 0; i < ACTION_VERBS.size(); i++) {
            if (i > 0) ab.append("|");
            ab.append(Pattern.quote(ACTION_VERBS.get(i)));
        }
        ACTION_VERB_PATTERN = Pattern.compile(ab.toString());
    }

    /* ============================== 메인 엔트리 ============================== */
    public ProofreadResponse proofread(String text) {
        if (text == null) text = "";
        final String original = text;

        List<Match> matches = new ArrayList<>();

        /* 1) 교정(치환) 규칙 — 원문 기준 매치 수집 */
        collect(matches, original, MULTI_HAHA, m -> new Replacement(
                "", "구어체 제거", "STYLE"));
        collect(matches, original, YEAR_MONTH_TIGHT, m -> new Replacement(
                m.group(1) + "년 " + m.group(2) + "개월", "기간 표기 띄어쓰기", "SPACING"));
        collect(matches, original, TRAIL_SPACE, m -> new Replacement(
                "", "줄 끝 공백 제거", "SPACING"));
        collect(matches, original, MULTI_WHITES, m -> new Replacement(
                " ", "연속 공백/탭/유니코드 공백 정리", "SPACING"));
        collect(matches, original, PERCENT_SPACE, m -> new Replacement(
                m.group(1) + "%", "퍼센트 표기 공백 제거", "SPACING"));

        /* 2) 탐지(분석) 규칙 — 치환 없이 코칭 이슈만 추가 */
        // 2-1) 모호표현
        collect(matches, original, VAGUE_PATTERN, m -> new Replacement(
                m.group(), "모호한 표현(정량화/구체화 권장)", "TONE"));

        // 2-2) 문장 단위 첨삭: 정량화/행동동사/STAR 구조
        for (Span sent : splitSentencesWithOffsets(original)) {
            String s = sent.text.trim();
            if (s.isEmpty()) continue;

            boolean hasNumber = s.matches(".*(\\d+%|\\d+\\.?\\d*|\\d+명|\\d+건|\\d+회|\\d+ms|\\d+초|\\d+시간).*");
            boolean hasAction = ACTION_VERB_PATTERN.matcher(s).find();

            // 정량화 권장
            if (!hasNumber && s.length() >= 20) {
                // 제안만, 치환 없음
                matches.add(new Match(
                        sent.start, sent.end, original.substring(sent.start, sent.end),
                        new Replacement(
                                s, "정량 지표가 없습니다. 숫자/%, 처리량, 시간·비용 절감 등을 추가하세요.", "CONTENT"
                        )
                ));
            }

            // 행동 동사 권장
            if (!hasAction) {
                matches.add(new Match(
                        sent.start, sent.end, original.substring(sent.start, sent.end),
                        new Replacement(
                                s, "행동(Action) 동사가 약합니다. '개선/도입/구현/최적화' 등 선명한 동사를 추가하세요.", "CONTENT"
                        )
                ));
            }

            // STAR 구성 체크(아주 라이트하게 힌트만)
            boolean hasSituation = s.matches(".*(상황|문제|배경|고객|요구|과제).*");
            boolean hasActionWord = s.matches(".*(조치|역할|담당|실행|시행|수행|개선|도입|구현|설계).*");
            boolean hasResult = s.matches(".*(성과|결과|효과|지표|향상|감소|단축|증가|달성).*");
            int missing = (hasSituation ? 0 : 1) + (hasActionWord ? 0 : 1) + (hasResult ? 0 : 1);
            if (missing >= 2) {
                matches.add(new Match(
                        sent.start, sent.end, original.substring(sent.start, sent.end),
                        new Replacement(
                                s, "STAR 구조가 약합니다. (상황→행동→성과) 흐름으로 2~3문장 보강하세요.", "STRUCTURE"
                        )
                ));
            }
        }

        /* 3) 시작 오프셋 정렬 & 겹침 제거(앞선 규칙 우선) */
        matches.sort(Comparator.comparingInt(a -> a.start));
        List<Match> filtered = new ArrayList<>();
        int lastEnd = -1;
        for (Match mm : matches) {
            if (mm.start >= lastEnd) { filtered.add(mm); lastEnd = mm.end; }
        }

        /* 4) 교정문 생성(치환 규칙은 after 적용, 분석-only는 원문 유지) */
        // 분석-only 판단: after == original 조각이면 유지(즉, 치환 없음)
        StringBuilder corrected = new StringBuilder();
        int cursor = 0;
        List<ProofreadResponse.Issue> issues = new ArrayList<>();
        for (Match mm : filtered) {
            if (cursor < mm.start) corrected.append(original, cursor, mm.start);

            boolean isReplacement = !mm.replacement.after.equals(mm.original) ||
                                    "SPACING".equals(mm.replacement.type) ||
                                    "STYLE".equals(mm.replacement.type);
            if (isReplacement) {
                corrected.append(mm.replacement.after);
            } else {
                corrected.append(mm.original); // 분석-only: 원문 유지
            }

            issues.add(new ProofreadResponse.Issue(
                    mm.start, mm.end, mm.original, mm.replacement.after,
                    mm.replacement.message, mm.replacement.type
            ));
            cursor = mm.end;
        }
        if (cursor < original.length()) corrected.append(original, cursor, original.length());

        /* 5) 변화가 전혀 없으면 최소 힌트 제공 */
        boolean anyChange = !corrected.toString().equals(original);
        if (issues.isEmpty()) {
            issues.add(new ProofreadResponse.Issue(
                    0, 0, "", "", "명백한 오류를 찾지 못했습니다. 정량화/행동동사/STAR 구성 보강을 고려하세요.", "INFO"
            ));
        } else if (!anyChange) {
            // 분석-only만 있었던 케이스: 안내 이슈 하나 추가(가독성)
            issues.add(new ProofreadResponse.Issue(
                    0, 0, "", "", "교정(치환) 사항은 없고 코칭 제안만 존재합니다.", "INFO"
            ));
        }

        return new ProofreadResponse(corrected.toString(), issues);
    }

    /* ============================== 문장 분리(오프셋 보존) ============================== */
    private static class Span {
        final int start, end; final String text;
        Span(int s, int e, String t){ start = s; end = e; text = t; }
    }

    /**
     * 한국어 문장 분리(휴리스틱):
     * - 줄바꿈(\n) 기준 우선 절단
     * - 또는 '.', '!', '?' 도달 시 절단
     * - 또는 "다.", "요.", "함.", "됨." 등 종결 패턴 발견 시 절단
     * 오프셋 범위는 [start, end) 로 반환
     */
    private static List<Span> splitSentencesWithOffsets(String text) {
        List<Span> out = new ArrayList<>();
        int start = 0;
        for (int i = 0; i < text.length(); i++) {
            char ch = text.charAt(i);
            boolean breakNow = false;

            if (ch == '\n' || ch == '\r') {
                breakNow = true;
            } else if (ch == '.' || ch == '!' || ch == '?') {
                breakNow = true;
            } else {
                // 간단한 종결 힌트 (…은 제외, 너무 공격적이면 줄여도 됨)
                if (i >= 1) {
                    String two = text.substring(i - 1, Math.min(i + 1, text.length()));
                    for (String token : SENTENCE_END_TOKENS) {
                        if (two.endsWith(token)) { breakNow = true; break; }
                    }
                }
            }

            if (breakNow) {
                int end = i + 1;
                if (end > start) {
                    out.add(new Span(start, end, text.substring(start, end)));
                }
                start = end;
            }
        }
        if (start < text.length()) {
            out.add(new Span(start, text.length(), text.substring(start)));
        }
        return out;
    }

    /* ============================== 매치 수집 유틸 ============================== */
    private interface ReplacementFactory { Replacement create(Matcher m); }
    private record Replacement(String after, String message, String type) {}
    private static class Match {
        final int start, end; final String original; final Replacement replacement;
        Match(int s, int e, String o, Replacement r){ start=s; end=e; original=o; replacement=r; }
    }
    private static void collect(List<Match> out, String text, Pattern p, ReplacementFactory f){
        Matcher m = p.matcher(text);
        while (m.find()){
            Replacement r = f.create(m);
            if (r == null) continue;
            if (m.end() <= m.start()) continue;
            out.add(new Match(m.start(), m.end(), text.substring(m.start(), m.end()), r));
        }
    }
}
