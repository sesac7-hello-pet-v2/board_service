package hello.pet.board_service.web.dto.response;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Page;

import hello.pet.board_service.entity.Post;
import hello.pet.board_service.infrastructure.feign.dto.response.UserDetailResponse;

public record PostResponse(
	String postId,
	PostUserResponse user,
	String content,
	List<String> imageUrls,
	LocalDateTime postedAt,
	int likeCount,
	int commentCount,
	boolean isLiked
) {
	public static PostResponse from(Post post) {
		return new PostResponse(
			post.getId(),
			PostUserResponse.from(post.getUserId(), null),
			post.getContent(),
			mapToImageUrls(post),
			post.getCreatedAt(),
			post.getLikeCount(),
			post.getCommentCount(),
			false // 기본값, 실제 조회 시 현재 사용자 기준으로 설정
		);
	}

	public static PostResponse from(Post post, Long currentUserId) {
		boolean isLiked = false;
		if (currentUserId != null) {
			isLiked = post.getLikedUserIds() != null && post.getLikedUserIds().contains(currentUserId);
		}
		return new PostResponse(
			post.getId(),
			PostUserResponse.from(post.getUserId(), null),
			post.getContent(),
			mapToImageUrls(post),
			post.getCreatedAt(),
			post.getLikeCount(),
			post.getCommentCount(),
			isLiked
		);
	}

	public static Page<PostResponse> from(Page<Post> posts) {
		return posts.map(PostResponse::from);
	}

	public static Page<PostResponse> from(Page<Post> posts, Long currentUserId) {
		return posts.map(post -> PostResponse.from(post, currentUserId));
	}

	// 사용자 정보를 포함한 PostResponse 생성
	public static PostResponse from(Post post, Long currentUserId, UserDetailResponse userDetail) {
		boolean isLiked = false;
		if (currentUserId != null) {
			isLiked = post.getLikedUserIds() != null && post.getLikedUserIds().contains(currentUserId);
		}
		return new PostResponse(
			post.getId(),
			PostUserResponse.from(post.getUserId(), userDetail),
			post.getContent(),
			mapToImageUrls(post),
			post.getCreatedAt(),
			post.getLikeCount(),
			post.getCommentCount(),
			isLiked
		);
	}

	private static List<String> mapToImageUrls(Post post) {
		if (post.getImages() == null) {
			return List.of();
		}

		return post.getImages().stream()
			.filter(image -> image != null && image.getS3Key() != null)
			.map(image -> hello.pet.board_service.infrastructure.utils.Constants.S3_IMAGE_BUCKET_URL + image.getS3Key())
			.toList();
	}
}
