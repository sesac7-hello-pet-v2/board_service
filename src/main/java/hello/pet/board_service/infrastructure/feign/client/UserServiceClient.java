package hello.pet.board_service.infrastructure.feign.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import hello.pet.board_service.infrastructure.config.feign.OpenFeignConfig;
import hello.pet.board_service.infrastructure.feign.dto.response.UserDetailResponse;

@FeignClient(
	name = "user-service",
	url = "${spring.cloud.openfeign.user-service:http://localhost:8086}",
	configuration = OpenFeignConfig.class
)
public interface UserServiceClient {

	@GetMapping(
		value = "/internal/v1/users/{userId}",
		consumes = MediaType.APPLICATION_JSON_VALUE,
		produces = MediaType.APPLICATION_JSON_VALUE
	)
	ResponseEntity<UserDetailResponse> getUserDetail(@PathVariable("userId") Long userId);

}