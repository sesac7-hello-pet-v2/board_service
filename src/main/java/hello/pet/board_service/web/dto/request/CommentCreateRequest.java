package hello.pet.board_service.web.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Schema(description = "댓글 생성 요청")
public record CommentCreateRequest(
	@NotNull(message = "게시글 ID는 필수입니다.")
	@NotBlank(message = "게시글 ID는 공백일 수 없습니다.")
	@Schema(description = "게시글 ID", example = "648a1b2c3d4e5f6789012345")
	String postId,

	@NotNull(message = "댓글 내용은 필수입니다.")
	@NotBlank(message = "댓글 내용은 공백일 수 없습니다.")
	@Size(min = 1, max = 500, message = "댓글은 1자 이상 500자 이하로 작성해주세요.")
	@Schema(description = "댓글 내용", example = "정말 귀여운 강아지네요!")
	String content
) {
}
