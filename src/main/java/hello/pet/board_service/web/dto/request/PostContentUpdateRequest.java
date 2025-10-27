package hello.pet.board_service.web.dto.request;

import jakarta.validation.constraints.NotBlank;

public record PostContentUpdateRequest(
	@NotBlank(message = "글의 내용을 입력해 주세요.")
	String content
) {
}