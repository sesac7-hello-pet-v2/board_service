package hello.pet.board_service.web.dto.request;

import jakarta.validation.constraints.Min;

public record PostPageRequest(
	@Min(
		value = 1,
		message = "페이지는 1보다 작을 수 없습니다."
	)
	int page,
	@Min(
		value = 1,
		message = "데이터의 양은 1보다 작을 수 없습니다."
	)
	int size
) {
	public PostPageRequest() {
		this(1, 10);
	}

	public int getPageBase() {
		return this.page - 1;
	}
}
