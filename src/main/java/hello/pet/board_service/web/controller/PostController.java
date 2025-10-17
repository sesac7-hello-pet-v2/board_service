package hello.pet.board_service.web.controller;

import static hello.pet.board_service.infrastructure.exception.HelloPetExceptionCode.*;

import org.springframework.http.ResponseEntity;

import hello.pet.board_service.infrastructure.config.swagger.annotation.ApiErrorCodeExamples;
import hello.pet.board_service.web.dto.request.PostCreateRequest;
import hello.pet.board_service.web.dto.request.PostEditRequest;
import hello.pet.board_service.web.dto.request.PostGetRequest;
import hello.pet.board_service.web.dto.request.PostLikeRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;

public interface PostController {
	/**
	 * 새 게시물 생성을 처리한다.
	 *
	 * @param request 게시물 생성에 필요한 데이터(예: 제목, 내용, 작성자 등)를 담은 요청 DTO
	 * @return 생성 결과를 담은 HTTP 응답 엔티티 — 성공 시 생성된 게시물 정보 또는 적절한 상태 코드를 포함한다
	 */
	@Operation(
		summary = "게시글 생성"
	)
	@ApiResponse(responseCode = "201", description = "게시글 생성이 성공적으로 완료가 될 경우 201 코드를 반환하게 됩니다.")
	ResponseEntity<?> createPost(PostCreateRequest request);

	@Operation(
		summary = "게시글 조회",
		description = "해당 기능을 통해 전체 조회 및 특정 사용자의 게시글을 조회할 수 있습니다."
	)
	@ApiResponse(responseCode = "200", description = "게시글의 조회에 성공할 경우.")
	ResponseEntity<?> getPosts(PostGetRequest request, Long currentUserId);

	@Operation(
		summary = "ID를 통한 게시글 조회",
		description = "게시글의 ID를 통해 게시글의 상세 정보를 조회할 수 있습니다. 현재 사용자의 좋아요 여부도 함께 제공됩니다."
	)
	@ApiErrorCodeExamples({
		NOT_FOUND_POST_BY_ID
	})
	@ApiResponse(responseCode = "200", description = "게시글의 조회에 성공할 경우")
	ResponseEntity<?> getPost(String id, Long currentUserId);

	ResponseEntity<?> editPost(String id, PostEditRequest request);

	ResponseEntity<?> deletePost(String id);

	@Operation(
		summary = "게시글 좋아요",
		description = "게시글에 좋아요를 추가하거나 제거합니다. 이미 좋아요를 누른 상태에서 다시 누르면 좋아요가 해제됩니다."
	)
	@ApiErrorCodeExamples({
		NOT_FOUND_POST_BY_ID
	})
	@ApiResponse(responseCode = "200", description = "좋아요 상태 변경 성공")
	ResponseEntity<?> likePost(String id, PostLikeRequest request);
}
