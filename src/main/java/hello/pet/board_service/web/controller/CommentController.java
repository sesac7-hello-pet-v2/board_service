package hello.pet.board_service.web.controller;

import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

import hello.pet.board_service.infrastructure.config.swagger.annotation.ApiErrorCodeExamples;
import hello.pet.board_service.infrastructure.exception.HelloPetExceptionCode;
import hello.pet.board_service.web.dto.request.CommentCreateRequest;
import hello.pet.board_service.web.dto.request.CommentGetRequest;
import hello.pet.board_service.web.dto.request.CommentUpdateRequest;
import hello.pet.board_service.web.dto.response.CommentResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@Tag(name = "댓글 API", description = "게시글 댓글 관련 API")
public interface CommentController {

	@Operation(
		summary = "댓글 작성",
		description = "특정 게시글에 댓글을 작성합니다."
	)
	@ApiErrorCodeExamples({
		HelloPetExceptionCode.POST_NOT_FOUND,
		HelloPetExceptionCode.INTERNAL_SERVER_ERROR
	})
	ResponseEntity<Void> createComment(
		@Parameter(description = "게시글 ID", required = true)
		@PathVariable String postId,

		@Parameter(description = "사용자 ID", required = true)
		@RequestHeader("X-User-Id") Long userId,

		@Parameter(description = "댓글 작성 요청", required = true)
		@Valid @RequestBody CommentCreateRequest request
	);

	@Operation(
		summary = "게시글별 댓글 조회",
		description = "특정 게시글의 댓글 목록을 페이징하여 조회합니다."
	)
	@ApiErrorCodeExamples({
		HelloPetExceptionCode.POST_NOT_FOUND,
		HelloPetExceptionCode.INTERNAL_SERVER_ERROR
	})
	ResponseEntity<Page<CommentResponse>> getCommentsByPostId(
		@Parameter(description = "게시글 ID", required = true)
		@PathVariable String postId,

		@Parameter(description = "현재 사용자 ID (인증된 경우)")
		@RequestHeader(value = "X-User-Id", required = false) Long currentUserId,

		@Parameter(description = "페이징 요청")
		@Valid CommentGetRequest request
	);

	@Operation(
		summary = "댓글 수정",
		description = "작성한 댓글의 내용을 수정합니다."
	)
	@ApiErrorCodeExamples({
		HelloPetExceptionCode.COMMENT_NOT_FOUND,
		HelloPetExceptionCode.COMMENT_UPDATE_FORBIDDEN,
		HelloPetExceptionCode.INTERNAL_SERVER_ERROR
	})
	ResponseEntity<CommentResponse> updateComment(
		@Parameter(description = "게시글 ID", required = true)
		@PathVariable String postId,

		@Parameter(description = "댓글 ID", required = true)
		@PathVariable String commentId,

		@Parameter(description = "사용자 ID", required = true)
		@RequestHeader("X-User-Id") Long userId,

		@Parameter(description = "댓글 수정 요청", required = true)
		@Valid @RequestBody CommentUpdateRequest request
	);

	@Operation(
		summary = "댓글 삭제",
		description = "작성한 댓글을 삭제합니다."
	)
	@ApiErrorCodeExamples({
		HelloPetExceptionCode.COMMENT_NOT_FOUND,
		HelloPetExceptionCode.COMMENT_DELETE_FORBIDDEN,
		HelloPetExceptionCode.INTERNAL_SERVER_ERROR
	})
	ResponseEntity<Void> deleteComment(
		@Parameter(description = "게시글 ID", required = true)
		@PathVariable String postId,

		@Parameter(description = "댓글 ID", required = true)
		@PathVariable String commentId,

		@Parameter(description = "사용자 ID", required = true)
		@RequestHeader("X-User-Id") Long userId
	);
}
