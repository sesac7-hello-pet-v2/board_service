package hello.pet.board_service.service;

import hello.pet.board_service.web.dto.request.PostCreateRequest;

public interface PostService {
	/**
 * 새 게시물을 저장한다.
 *
 * @param request 게시물 생성에 필요한 데이터가 담긴 요청 DTO
 */
void save(PostCreateRequest request);
}
