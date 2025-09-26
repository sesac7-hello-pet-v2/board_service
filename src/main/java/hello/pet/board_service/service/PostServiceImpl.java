package hello.pet.board_service.service;

import org.springframework.stereotype.Service;

import hello.pet.board_service.entity.Post;
import hello.pet.board_service.repository.PostRepository;
import hello.pet.board_service.web.dto.request.PostCreateRequest;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PostServiceImpl implements PostService {
	private final PostRepository repository;

	/**
	 * 요청 데이터를 사용해 새 게시글 엔티티를 생성하여 영속화한다.
	 *
	 * @param request 게시글 생성에 필요한 사용자 ID와 내용이 담긴 요청 객체
	 */
	@Override
	public void save(PostCreateRequest request) {
		repository.save(
			Post.builder()
				.userId(request.userId())
				.content(request.content())
				.build()
		);
	}
}
