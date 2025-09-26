package hello.pet.board_service.web.controller;

import org.springframework.http.ResponseEntity;

import hello.pet.board_service.web.dto.request.PostCreateRequest;

public interface PostController {
	ResponseEntity<?> createPost(PostCreateRequest request);
}
