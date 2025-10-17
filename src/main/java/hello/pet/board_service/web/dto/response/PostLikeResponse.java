package hello.pet.board_service.web.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Builder
@Schema(description = "게시물 좋아요 응답")
public record PostLikeResponse(
	@Schema(description = "게시물 ID", example = "64f8a1234567890123456789")
	String postId,
	@Schema(description = "좋아요 여부", example = "true")
	boolean isLiked,
	@Schema(description = "총 좋아요 수", example = "25")
	int likeCount
) {
}
