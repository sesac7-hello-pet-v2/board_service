package hello.pet.board_service.web.dto.request;

import java.util.List;

import org.springframework.web.multipart.MultipartFile;

import jakarta.validation.constraints.NotBlank;

public record PostCreateRequest(
	@NotBlank(message = "글의 내용을 입력해 주세요.")
	String content,
	List<MultipartFile> images
) {
}
