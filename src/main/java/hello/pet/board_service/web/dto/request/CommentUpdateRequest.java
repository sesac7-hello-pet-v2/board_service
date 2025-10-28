package hello.pet.board_service.web.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Schema(description = "댓글 수정 요청")
public record CommentUpdateRequest(
	@NotNull(message = "댓글 내용은 필수입니다.")
	@NotBlank(message = "댓글 내용은 공백일 수 없습니다.")
	@Size(min = 1, max = 500, message = "댓글은 1자 이상 500자 이하로 작성해주세요.")
	@Schema(description = "수정할 댓글 내용", example = "수정된 댓글 내용입니다.")
	String content
) {
}
