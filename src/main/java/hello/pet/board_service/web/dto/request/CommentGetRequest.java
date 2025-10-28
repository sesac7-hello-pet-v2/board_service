package hello.pet.board_service.web.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

@Schema(description = "댓글 조회 요청")
public record CommentGetRequest(
	@Min(value = 1, message = "페이지는 1 이상이어야 합니다.")
	@Schema(description = "페이지 번호 (1부터 시작)", example = "1", defaultValue = "1")
	Integer page,

	@Min(value = 1, message = "페이지 크기는 1 이상이어야 합니다.")
	@Max(value = 100, message = "페이지 크기는 100 이하여야 합니다.")
	@Schema(description = "페이지 크기", example = "20", defaultValue = "20")
	Integer size
) {
	public CommentGetRequest {
		// 기본값 설정
		if (page == null)
			page = 1;
		if (size == null)
			size = 20;
	}

	public int getPageForRepository() {
		return page - 1; // Repository는 0-based 페이징
	}
}
