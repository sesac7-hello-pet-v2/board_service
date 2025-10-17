package hello.pet.board_service.web.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(description = "게시물 좋아요 요청")
public record PostLikeRequest(
	@NotNull(message = "사용자 ID는 필수입니다")
	@Schema(description = "사용자 ID", example = "1")
	Long userId
) {
}
