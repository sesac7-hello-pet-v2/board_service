package hello.pet.board_service.repository;

import org.springframework.data.mongodb.repository.MongoRepository;

import hello.pet.board_service.entity.Post;

public interface PostRepository extends MongoRepository<Post, String> {
}
