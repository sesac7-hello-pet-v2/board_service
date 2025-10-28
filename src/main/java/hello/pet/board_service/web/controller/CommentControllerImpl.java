package hello.pet.board_service.web.controller;

import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import hello.pet.board_service.service.CommentService;
import hello.pet.board_service.web.dto.request.CommentCreateRequest;
import hello.pet.board_service.web.dto.request.CommentGetRequest;
import hello.pet.board_service.web.dto.request.CommentUpdateRequest;
import hello.pet.board_service.web.dto.response.CommentResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/posts/{postId}/comments")
@RequiredArgsConstructor
public class CommentControllerImpl implements CommentController {

	private final CommentService commentService;

	@Override
	@PostMapping
	public ResponseEntity<Void> createComment(
		@PathVariable String postId,
		@RequestHeader("X-User-Id") Long userId,
		@Valid @RequestBody CommentCreateRequest request
	) {
		// postId를 request에 설정 (URL에서 가져온 값 사용)
		CommentCreateRequest updatedRequest = new CommentCreateRequest(postId, request.content());

		commentService.createComment(userId, updatedRequest);
		return ResponseEntity.status(HttpStatus.CREATED).build();
	}

	@Override
	@GetMapping
	public ResponseEntity<Page<CommentResponse>> getCommentsByPostId(
		@PathVariable String postId,
		@RequestHeader(value = "X-User-Id", required = false) Long currentUserId,
		@Valid CommentGetRequest request
	) {
		Page<CommentResponse> responses = commentService.getCommentsByPostId(postId, request, currentUserId);
		return ResponseEntity.ok(responses);
	}

	@Override
	@PutMapping("/{commentId}")
	public ResponseEntity<CommentResponse> updateComment(
		@PathVariable String postId,
		@PathVariable String commentId,
		@RequestHeader("X-User-Id") Long userId,
		@Valid @RequestBody CommentUpdateRequest request
	) {
		CommentResponse response = commentService.updateComment(commentId, userId, request);
		return ResponseEntity.ok(response);
	}

	@Override
	@DeleteMapping("/{commentId}")
	public ResponseEntity<Void> deleteComment(
		@PathVariable String postId,
		@PathVariable String commentId,
		@RequestHeader("X-User-Id") Long userId
	) {
		commentService.deleteComment(commentId, userId);
		return ResponseEntity.noContent().build();
	}
}
