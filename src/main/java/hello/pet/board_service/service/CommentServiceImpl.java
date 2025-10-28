package hello.pet.board_service.service;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import hello.pet.board_service.entity.Comment;
import hello.pet.board_service.entity.Post;
import hello.pet.board_service.infrastructure.exception.HelloPetException;
import hello.pet.board_service.infrastructure.exception.HelloPetExceptionCode;
import hello.pet.board_service.infrastructure.feign.client.UserServiceClient;
import hello.pet.board_service.infrastructure.feign.dto.response.UserDetailResponse;
import hello.pet.board_service.repository.CommentRepository;
import hello.pet.board_service.repository.PostRepository;
import hello.pet.board_service.web.dto.request.CommentCreateRequest;
import hello.pet.board_service.web.dto.request.CommentGetRequest;
import hello.pet.board_service.web.dto.request.CommentUpdateRequest;
import hello.pet.board_service.web.dto.response.CommentResponse;
import hello.pet.board_service.web.dto.response.PostUserResponse;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CommentServiceImpl implements CommentService {

	private final CommentRepository commentRepository;
	private final PostRepository postRepository;
	private final UserServiceClient userServiceClient;

	@Override
	@Transactional
	public void createComment(Long userId, CommentCreateRequest request) {
		// 게시글 존재 여부 확인
		Post post = postRepository.findById(request.postId())
			.orElseThrow(() -> new HelloPetException(HelloPetExceptionCode.POST_NOT_FOUND));

		// 댓글 생성
		Comment comment = Comment.builder()
			.postId(request.postId())
			.userId(userId)
			.content(request.content())
			.build();

		commentRepository.save(comment);

		// 게시글의 댓글 수 증가
		post.setCommentCount(post.getCommentCount() + 1);
		postRepository.save(post);
	}

	@Override
	public Page<CommentResponse> getCommentsByPostId(String postId, CommentGetRequest request, Long currentUserId) {
		// 게시글 존재 여부 확인
		postRepository.findById(postId)
			.orElseThrow(() -> new HelloPetException(HelloPetExceptionCode.POST_NOT_FOUND));

		// 페이징 설정 (작성일시 오름차순)
		Pageable pageable = PageRequest.of(
			request.getPageForRepository(),
			request.size(),
			Sort.by(Sort.Direction.ASC, "createdAt")
		);

		Page<Comment> comments = commentRepository.findByPostIdAndNotDeleted(postId, pageable);

		return comments.map(comment -> {
			// 각 댓글의 작성자 정보를 개별적으로 조회
			UserDetailResponse userDetail = null;
			try {
				ResponseEntity<UserDetailResponse> response = userServiceClient.getUserDetail(comment.getUserId());
				userDetail = response.getBody();
			} catch (Exception e) {
				// 사용자 정보 조회 실패 시 로그만 남기고 계속 진행
				System.out.println("Failed to fetch user detail for userId: " + comment.getUserId());
			}

			PostUserResponse userResponse = userDetail != null
				? PostUserResponse.from(comment.getUserId(), userDetail)
				: new PostUserResponse(comment.getUserId(), "알 수 없는 사용자", "unknown", null);

			return new CommentResponse(
				comment.getId(),
				comment.getPostId(),
				userResponse,
				comment.getContent(),
				comment.getCreatedAt(),
				comment.getUpdatedAt(),
				currentUserId != null && currentUserId.equals(comment.getUserId())
			);
		});
	}

	@Override
	@Transactional
	public CommentResponse updateComment(String commentId, Long userId, CommentUpdateRequest request) {
		Comment comment = commentRepository.findByIdAndNotDeleted(commentId)
			.orElseThrow(() -> new HelloPetException(HelloPetExceptionCode.COMMENT_NOT_FOUND));

		// 작성자 권한 확인
		if (!comment.getUserId().equals(userId)) {
			throw new HelloPetException(HelloPetExceptionCode.COMMENT_UPDATE_FORBIDDEN);
		}

		// 댓글 내용 수정
		comment.setContent(request.content());
		Comment updatedComment = commentRepository.save(comment);

		// 사용자 정보 조회
		UserDetailResponse userDetail = userServiceClient.getUserDetail(userId).getBody();
		PostUserResponse userResponse = PostUserResponse.from(userId, userDetail);

		return new CommentResponse(
			updatedComment.getId(),
			updatedComment.getPostId(),
			userResponse,
			updatedComment.getContent(),
			updatedComment.getCreatedAt(),
			updatedComment.getUpdatedAt(),
			true
		);
	}

	@Override
	@Transactional
	public void deleteComment(String commentId, Long userId) {
		Comment comment = commentRepository.findByIdAndNotDeleted(commentId)
			.orElseThrow(() -> new HelloPetException(HelloPetExceptionCode.COMMENT_NOT_FOUND));

		// 작성자 권한 확인
		if (!comment.getUserId().equals(userId)) {
			throw new HelloPetException(HelloPetExceptionCode.COMMENT_DELETE_FORBIDDEN);
		}

		// Soft delete 처리
		comment.setDeleted(true);
		commentRepository.save(comment);

		// 게시글의 댓글 수 감소
		Post post = postRepository.findById(comment.getPostId())
			.orElseThrow(() -> new HelloPetException(HelloPetExceptionCode.POST_NOT_FOUND));

		post.setCommentCount(Math.max(0, post.getCommentCount() - 1));
		postRepository.save(post);
	}

	@Override
	@Transactional
	public void deleteCommentsByPostId(String postId) {
		List<Comment> comments = commentRepository.findAllByPostId(postId);
		comments.forEach(comment -> comment.setDeleted(true));
		commentRepository.saveAll(comments);
	}
}
