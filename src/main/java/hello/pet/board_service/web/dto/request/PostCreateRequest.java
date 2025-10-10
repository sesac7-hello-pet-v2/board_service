package hello.pet.board_service.web.dto.request;

import java.util.List;

import org.springframework.web.multipart.MultipartFile;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

public record PostCreateRequest(
	@NotNull(message = "로그인을 사용자만 글을 작성할 수 있습니다. <br> 다시 로그인을 진행해 주세요.")
	Long userId,
	@NotBlank(message = "글의 내용을 입력해 주세요.")
	String content,
	@NotNull(message = "게시할 파일 리스트는 필수입니다.")
	@NotEmpty(message = "게시글에는 최소한 하나의 파일(사진)이 필요합니다.")
	List<MultipartFile> file
) {
}
