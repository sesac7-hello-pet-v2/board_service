package hello.pet.board_service.web.controller;

import org.springframework.http.ResponseEntity;

import hello.pet.board_service.web.dto.request.PostCreateRequest;
import hello.pet.board_service.web.dto.request.PostPageRequest;

public interface PostController {
	/**
 * 새 게시물 생성을 처리한다.
 *
 * @param request 게시물 생성에 필요한 데이터(예: 제목, 내용, 작성자 등)를 담은 요청 DTO
 * @return 생성 결과를 담은 HTTP 응답 엔티티 — 성공 시 생성된 게시물 정보 또는 적절한 상태 코드를 포함한다
 */
ResponseEntity<?> createPost(PostCreateRequest request);

	ResponseEntity<?> getPosts(PostPageRequest pageRequest);
}
