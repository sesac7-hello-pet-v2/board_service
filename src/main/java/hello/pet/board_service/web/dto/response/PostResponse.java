package hello.pet.board_service.web.dto.response;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Page;

import hello.pet.board_service.entity.Post;
import hello.pet.board_service.entity.PostImage;

public record PostResponse(
	String postId,
	Long userId,
	String content,
	List<String> imageUrls,
	LocalDateTime postedAt
) {
	public static PostResponse from(Post post) {
		return new PostResponse(
			post.getId(),
			post.getUserId(),
			post.getContent(),
			mapToImageUrls(post),
			post.getCreatedAt()
		);
	}

	public static Page<PostResponse> from(Page<Post> posts) {
		return posts.map(PostResponse::from);
	}

	private static List<String> mapToImageUrls(Post post) {
		if (post.getImages() == null) {
			return List.of();
		}

		return post.getImages().stream()
			.filter(image -> image != null && image.getS3Key() != null)
			.map(PostImage::getS3Key)
			.toList();
	}
}
