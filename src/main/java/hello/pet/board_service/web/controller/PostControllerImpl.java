package hello.pet.board_service.web.controller;

import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import hello.pet.board_service.service.PostService;
import hello.pet.board_service.web.dto.request.PostCreateRequest;
import hello.pet.board_service.web.dto.request.PostEditRequest;
import hello.pet.board_service.web.dto.request.PostGetRequest;
import hello.pet.board_service.web.dto.request.PostLikeRequest;
import hello.pet.board_service.web.dto.response.PostLikeResponse;
import hello.pet.board_service.web.dto.response.PostResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/posts")
@RequiredArgsConstructor
public class PostControllerImpl implements PostController {
	private final PostService service;

	@Override
	@PostMapping
	public ResponseEntity<?> createPost(@Valid @ModelAttribute PostCreateRequest request) {
		service.save(request);
		return ResponseEntity.status(HttpStatus.CREATED).build();
	}

	@Override
	@GetMapping
	public ResponseEntity<Page<PostResponse>> getPosts(@Valid @ModelAttribute PostGetRequest request,
		@RequestHeader(value = "X-User-Id", required = false) Long currentUserId) {
		Page<PostResponse> allPost = service.findAllPost(request, currentUserId);
		return ResponseEntity.ok(allPost);
	}

	@Override
	@GetMapping("/{id}")
	public ResponseEntity<PostResponse> getPost(@PathVariable String id,
		@RequestHeader(value = "X-User-Id", required = false) Long currentUserId) {
		PostResponse post;
		if (currentUserId != null) {
			post = service.findOne(id, currentUserId);
		} else {
			post = service.findOne(id);
		}
		return ResponseEntity.ok(post);
	}

	@Override
	@PutMapping("/{id}")
	public ResponseEntity<String> editPost(@PathVariable String id,
		@Valid @ModelAttribute PostEditRequest request) {
		String editedId = service.editPostContentById(id, request);
		return ResponseEntity.ok(editedId);
	}

	@Override
	@DeleteMapping("/{id}")
	public ResponseEntity<?> deletePost(@PathVariable String id) {
		service.deletePostById(id);
		return ResponseEntity.ok().build();
	}

	@Override
	@PostMapping("/{id}/like")
	public ResponseEntity<PostLikeResponse> likePost(@PathVariable String id,
		@Valid @RequestBody PostLikeRequest request) {
		PostLikeResponse response = service.likePost(id, request);
		return ResponseEntity.ok(response);
	}
}
