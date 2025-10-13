package hello.pet.board_service.web.dto.request;

import java.util.List;

import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotBlank;

public record PostEditRequest(
	@NotBlank(message = "글의 내용을 입력해 주세요.")
	String content,
	@Nullable
	List<Integer> deleteImageOrders
) {
}

