package hello.pet.board_service.web.dto.response;

import hello.pet.board_service.infrastructure.feign.dto.response.UserDetailResponse;

public record PostUserResponse(
	Long userId,
	String nickname,
	String username,
	String profileUrl
) {
	public static PostUserResponse from(Long userId, UserDetailResponse userDetail) {
		if (userDetail == null) {
			return new PostUserResponse(userId, null, null, null);
		}
		return new PostUserResponse(
			userId,
			userDetail.nickname(),
			userDetail.username(),
			userDetail.profileUrl()
		);
	}
}
