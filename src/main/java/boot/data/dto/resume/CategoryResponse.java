package boot.data.dto.resume;

public record CategoryResponse(
        Integer id,
        String name,
        Integer skillCount // withCounts=true 일 때만 채움(그 외 null)
) {}