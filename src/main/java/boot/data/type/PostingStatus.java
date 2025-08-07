package boot.data.type;

public enum PostingStatus {
	  DRAFT,      // 임시 저장 (아직 공개되지 않은 상태)
	    OPEN,       // 공고 중 (사용자에게 노출되어 지원 가능한 상태)
	    CLOSED,     // 채용 마감 (기업이 수동으로 마감 처리한 상태)
	    EXPIRED     // 기간 만료 (설정한 공고 마감일이 지나 자동 마감된 상태)
}
