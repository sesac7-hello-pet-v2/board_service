package hello.pet.board_service.entity;

import java.time.LocalDateTime;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.index.IndexDirection;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Document(collection = "comments")
@CompoundIndexes({
	// postId로 먼저 필터링하고, createdAt으로 정렬할 때 효율적
	@CompoundIndex(name = "post_created_idx", def = "{'postId': 1, 'createdAt': 1}", unique = false),
	// userId로 사용자의 댓글을 조회할 때 효율적
	@CompoundIndex(name = "user_created_idx", def = "{'userId': 1, 'createdAt': -1}", unique = false)
})
@Builder
public class Comment {
	@Id
	private String id;

	@Indexed(direction = IndexDirection.ASCENDING)
	private String postId;

	@Indexed(direction = IndexDirection.ASCENDING)
	private Long userId;

	@Setter
	private String content;

	@CreatedDate
	private LocalDateTime createdAt;

	@LastModifiedDate
	private LocalDateTime updatedAt;

	// 댓글이 삭제되었는지 여부 (soft delete)
	@Builder.Default
	@Setter
	private boolean deleted = false;
}
