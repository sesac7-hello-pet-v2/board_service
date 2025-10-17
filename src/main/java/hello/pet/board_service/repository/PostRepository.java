package hello.pet.board_service.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.mongodb.repository.Update;

import hello.pet.board_service.entity.Post;

public interface PostRepository extends MongoRepository<Post, String> {
	Page<Post> findByUserId(Long userId, Pageable pageable);

	@Query("{ '_id': ?0 }")
	@Update("{ '$addToSet': { 'likedUserIds': ?1 }, '$inc': { 'likeCount': 1 } }")
	long addLike(String postId, Long userId);

	@Query("{ '_id': ?0 }")
	@Update("{ '$pull': { 'likedUserIds': ?1 }, '$inc': { 'likeCount': -1 } }")
	long removeLike(String postId, Long userId);
}
