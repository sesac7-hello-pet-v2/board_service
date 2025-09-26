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

	@Override
	public void save(PostCreateRequest request) {
		/* TODO: API Gateway 구현이 완료가 되면 사용자의 ID를 통한 사용자의 정보를 담는 것도 좋을 것 같음.
		현재는 테스트를 위한 코드임
		 */
		repository.save(
			Post.builder()
				.userId(request.userId())
				.content(request.content())
				.build()
		);
	}
}
