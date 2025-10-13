package hello.pet.board_service.web.controller;

import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import hello.pet.board_service.entity.Post;
import hello.pet.board_service.service.PostService;
import hello.pet.board_service.web.dto.request.PostCreateRequest;
import hello.pet.board_service.web.dto.request.PostEditRequest;
import hello.pet.board_service.web.dto.request.PostGetRequest;
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
	public ResponseEntity<Page<Post>> getPosts(@Valid @ModelAttribute PostGetRequest request) {
		Page<Post> allPost = service.findAllPost(request);
		return ResponseEntity.ok(allPost);
	}

	@Override
	@GetMapping("/{id}")
	public ResponseEntity<Post> getPost(@PathVariable String id) {
		Post post = service.findOne(id);
		return ResponseEntity.ok(post);
	}

	@Override
	@PutMapping("/{id}")
	public ResponseEntity<String> editPostContent(@PathVariable String id,
		@Valid @RequestBody PostEditRequest request) {
		String editedId = service.editPostContentById(id, request);
		return ResponseEntity.ok(editedId);
	}
}
