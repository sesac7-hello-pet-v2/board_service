package hello.pet.board_service.web.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import hello.pet.board_service.service.PostService;
import hello.pet.board_service.web.dto.request.PostCreateRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/posts")
@RequiredArgsConstructor
public class PostControllerImpl implements PostController {
	private final PostService service;

	/**
	 * 새 게시물을 생성하고 저장한다.
	 *
	 * @param request 생성할 게시물의 데이터가 담긴 요청 객체
	 * @return 요청이 성공하면 HTTP 200 OK 응답(빈 본문)
	 */
	@Override
	@PostMapping
	public ResponseEntity<?> createPost(@Valid @RequestBody PostCreateRequest request) {
		service.save(request);
		return ResponseEntity.ok().build();
	}
}
