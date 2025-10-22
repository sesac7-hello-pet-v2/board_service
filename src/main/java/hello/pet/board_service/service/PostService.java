package hello.pet.board_service.service;

import org.springframework.data.domain.Page;

import hello.pet.board_service.web.dto.request.PostCreateRequest;
import hello.pet.board_service.web.dto.request.PostEditRequest;
import hello.pet.board_service.web.dto.request.PostGetRequest;
import hello.pet.board_service.web.dto.response.PostLikeResponse;
import hello.pet.board_service.web.dto.response.PostResponse;

public interface PostService {
	/**
 * 새 게시물을 저장한다.
 *
 * @param request 게시물 생성에 필요한 데이터가 담긴 요청 DTO
	 * @param userId API Gateway에서 제공하는 사용자 ID
 */
	void save(PostCreateRequest request, Long userId);

	Page<PostResponse> findAllPost(PostGetRequest request, Long currentUserId);

	PostResponse findOne(String id);

	PostResponse findOne(String id, Long currentUserId);

	String editPostContentById(String id, PostEditRequest request, Long userId);

	void deletePostById(String id, Long userId);

	PostLikeResponse likePost(String id, Long userId);
}
