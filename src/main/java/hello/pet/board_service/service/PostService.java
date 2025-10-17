package hello.pet.board_service.service;

import org.springframework.data.domain.Page;

import hello.pet.board_service.web.dto.request.PostCreateRequest;
import hello.pet.board_service.web.dto.request.PostEditRequest;
import hello.pet.board_service.web.dto.request.PostGetRequest;
import hello.pet.board_service.web.dto.request.PostLikeRequest;
import hello.pet.board_service.web.dto.response.PostLikeResponse;
import hello.pet.board_service.web.dto.response.PostResponse;

public interface PostService {
	/**
 * 새 게시물을 저장한다.
 *
 * @param request 게시물 생성에 필요한 데이터가 담긴 요청 DTO
 */
void save(PostCreateRequest request);

	Page<PostResponse> findAllPost(PostGetRequest request, Long currentUserId);

	PostResponse findOne(String id);

	PostResponse findOne(String id, Long currentUserId);

	String editPostContentById(String id, PostEditRequest request);

	void deletePostById(String id);

	PostLikeResponse likePost(String id, PostLikeRequest request);
}
