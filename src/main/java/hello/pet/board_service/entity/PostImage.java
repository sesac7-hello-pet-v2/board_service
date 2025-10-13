package hello.pet.board_service.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PostImage {
	@Setter
	private Integer displayOrder;
	@Setter
	private String s3Key;
}
