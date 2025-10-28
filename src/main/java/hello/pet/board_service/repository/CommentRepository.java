package hello.pet.board_service.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import hello.pet.board_service.entity.Comment;

public interface CommentRepository extends MongoRepository<Comment, String> {

	// 특정 게시글의 댓글 조회 (삭제되지 않은 댓글만, 페이징)
	@Query("{'postId': ?0, 'deleted': false}")
	Page<Comment> findByPostIdAndNotDeleted(String postId, Pageable pageable);

	// 특정 게시글의 댓글 개수 조회 (삭제되지 않은 댓글만)
	@Query(value = "{'postId': ?0, 'deleted': false}", count = true)
	long countByPostIdAndNotDeleted(String postId);

	// 특정 사용자의 댓글 조회 (삭제되지 않은 댓글만, 페이징)
	@Query("{'userId': ?0, 'deleted': false}")
	Page<Comment> findByUserIdAndNotDeleted(Long userId, Pageable pageable);

	// 댓글 ID로 조회 (삭제되지 않은 댓글만)
	@Query("{'id': ?0, 'deleted': false}")
	Optional<Comment> findByIdAndNotDeleted(String commentId);

	// 특정 게시글의 댓글들을 일괄 삭제 처리 (게시글 삭제 시 사용)
	@Query("{'postId': ?0}")
	List<Comment> findAllByPostId(String postId);
}
