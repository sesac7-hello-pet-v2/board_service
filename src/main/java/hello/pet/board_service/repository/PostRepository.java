package hello.pet.board_service.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

import hello.pet.board_service.entity.Post;

public interface PostRepository extends MongoRepository<Post, String> {
	Page<Post> findByUserId(Long userId, Pageable pageable);
}
