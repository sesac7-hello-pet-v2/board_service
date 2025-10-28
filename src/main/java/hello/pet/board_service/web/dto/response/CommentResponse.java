package hello.pet.board_service.web.dto.response;

import java.time.LocalDateTime;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "댓글 응답")
public record CommentResponse(
	@Schema(description = "댓글 ID", example = "648a1b2c3d4e5f6789012345")
	String commentId,

	@Schema(description = "게시글 ID", example = "648a1b2c3d4e5f6789012345")
	String postId,

	@Schema(description = "작성자 정보")
	PostUserResponse user,

	@Schema(description = "댓글 내용", example = "정말 귀여운 강아지네요!")
	String content,

	@Schema(description = "작성일시", example = "2024-01-15T10:30:00")
	LocalDateTime createdAt,

	@Schema(description = "수정일시", example = "2024-01-15T11:00:00")
	LocalDateTime updatedAt,

	@Schema(description = "내가 작성한 댓글인지 여부", example = "true")
	boolean isMyComment
) {
}
