package hello.pet.board_service.infrastructure.feign.dto.request;

import lombok.Builder;

@Builder
public record ImageDeleteRequest(
	String deleteS3Key
) {
}
