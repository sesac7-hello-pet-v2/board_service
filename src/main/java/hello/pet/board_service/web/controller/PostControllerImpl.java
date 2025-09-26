package hello.pet.board_service.web.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import hello.pet.board_service.service.PostService;
import hello.pet.board_service.web.dto.request.PostCreateRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/posts")
@RequiredArgsConstructor
public class PostControllerImpl implements PostController {
	private final PostService service;

	@Override
	@PostMapping
	public ResponseEntity<?> createPost(@Valid @RequestBody PostCreateRequest request) {
		service.save(request);
		return ResponseEntity.ok().build();
	}
}
