package hello.pet.board_service.web.dto.request;

import java.util.List;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record PostCreateRequest(
	@NotBlank(message = "글의 내용을 입력해 주세요.")
	String content,
	@NotNull(message = "이미지 URL 리스트는 필수입니다.")
	List<String> imageUrls
) {
}
