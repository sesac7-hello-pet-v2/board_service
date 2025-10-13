package hello.pet.board_service.web.dto.request;

import jakarta.annotation.Nullable;
import jakarta.validation.constraints.Min;

public record PostGetRequest(
	@Min(
		value = 1,
		message = "페이지는 1보다 작을 수 없습니다."
	)
	Integer page,
	@Min(
		value = 1,
		message = "데이터의 양은 1보다 작을 수 없습니다."
	)
	Integer size,
	@Nullable
	Long userId
) {
	// Compact Constructor를 사용하여 null 값일 때 기본값 설정
	public PostGetRequest {
		if (page == null) {
			page = 1;
		}

		if (size == null) {
			size = 10;
		}
	}

	public int getPageBase() {
		return this.page - 1;
	}
}
