package hello.pet.board_service.service;

import hello.pet.board_service.web.dto.request.PostCreateRequest;

public interface PostService {
	void save(PostCreateRequest request);
}
