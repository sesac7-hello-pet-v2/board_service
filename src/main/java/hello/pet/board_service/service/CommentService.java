package hello.pet.board_service.service;

import org.springframework.data.domain.Page;

import hello.pet.board_service.web.dto.request.CommentCreateRequest;
import hello.pet.board_service.web.dto.request.CommentGetRequest;
import hello.pet.board_service.web.dto.request.CommentUpdateRequest;
import hello.pet.board_service.web.dto.response.CommentResponse;

public interface CommentService {

	/**
	 * 댓글 작성
	 * @param userId 작성자 ID
	 * @param request 댓글 작성 요청
	 */
	void createComment(Long userId, CommentCreateRequest request);

	/**
	 * 게시글별 댓글 조회 (페이징)
	 * @param postId 게시글 ID
	 * @param request 조회 요청 (페이징 정보)
	 * @param currentUserId 현재 사용자 ID (내 댓글 여부 판단용)
	 * @return 댓글 목록 (페이징)
	 */
	Page<CommentResponse> getCommentsByPostId(String postId, CommentGetRequest request, Long currentUserId);

	/**
	 * 댓글 수정
	 * @param commentId 댓글 ID
	 * @param userId 수정자 ID
	 * @param request 댓글 수정 요청
	 * @return 수정된 댓글 응답
	 */
	CommentResponse updateComment(String commentId, Long userId, CommentUpdateRequest request);

	/**
	 * 댓글 삭제
	 * @param commentId 댓글 ID
	 * @param userId 삭제자 ID
	 */
	void deleteComment(String commentId, Long userId);

	/**
	 * 게시글 삭제 시 해당 게시글의 모든 댓글 삭제 (내부 호출용)
	 * @param postId 게시글 ID
	 */
	void deleteCommentsByPostId(String postId);
}
