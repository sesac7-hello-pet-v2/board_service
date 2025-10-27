package hello.pet.board_service.infrastructure.feign.dto.response;

public record UserDetailResponse(
	String email,
	String nickname,
	String username,
	String address,
	String profileUrl,
	String phoneNumber
) {
}
