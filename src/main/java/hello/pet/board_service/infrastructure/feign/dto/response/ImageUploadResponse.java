package hello.pet.board_service.infrastructure.feign.dto.response;

import lombok.Builder;

@Builder
public record ImageUploadResponse(
	String s3Key
) {
}
