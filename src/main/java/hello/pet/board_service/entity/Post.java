package hello.pet.board_service.entity;

import java.time.LocalDateTime;
import java.util.List;

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
@Document(collection = "posts")
@CompoundIndexes({
	// userId로 먼저 필터링하고, createdAt으로 정렬할 때 효율적
	@CompoundIndex(name = "user_created_idx", def = "{'userId': 1, 'createdAt': -1}", unique = false)
})
@Builder
public class Post {
	@Id
	private String id;

	@Indexed(direction = IndexDirection.ASCENDING)
	private Long userId;

	@Setter
	private String content;

	@Setter
	private List<PostImage> images;

	@CreatedDate
	private LocalDateTime createdAt;

	@LastModifiedDate
	private LocalDateTime updatedAt;
}
