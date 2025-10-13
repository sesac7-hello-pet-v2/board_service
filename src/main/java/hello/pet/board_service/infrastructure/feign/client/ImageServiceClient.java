package hello.pet.board_service.infrastructure.feign.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;

import hello.pet.board_service.infrastructure.config.feign.OpenFeignConfig;
import hello.pet.board_service.infrastructure.feign.dto.response.ImageUploadResponse;

@FeignClient(
	name = "image-service",
	url = "${spring.cloud.openfeign.image-service:http://localhost:8088}",
	configuration = OpenFeignConfig.class
)
public interface ImageServiceClient {

	@PostMapping(value = "/internal/v1/images/post/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	ResponseEntity<ImageUploadResponse> uploadImage(
		@RequestPart("userId") Long userId,
		@RequestPart("postId") String postId,
		@RequestPart("file") MultipartFile file
	);

	@DeleteMapping(value = "/internal/v1/images", consumes = MediaType.APPLICATION_JSON_VALUE)
	ResponseEntity<?> deleteImage(@RequestPart("deleteS3Key") String S3Key);
}
