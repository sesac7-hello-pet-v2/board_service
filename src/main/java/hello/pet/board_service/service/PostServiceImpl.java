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
		repository.save(
			Post.builder()
				.userId(request.userId())
				.content(request.content())
				.build()
		);
	}
}
