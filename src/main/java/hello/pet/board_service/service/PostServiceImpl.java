package hello.pet.board_service.service;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.web.multipart.MultipartFile;

import hello.pet.board_service.entity.Post;
import hello.pet.board_service.entity.PostImage;
import hello.pet.board_service.infrastructure.exception.HelloPetException;
import hello.pet.board_service.infrastructure.exception.HelloPetExceptionCode;
import hello.pet.board_service.infrastructure.feign.client.ImageServiceClient;
import hello.pet.board_service.infrastructure.feign.client.UserServiceClient;
import hello.pet.board_service.infrastructure.feign.dto.response.ImageUploadResponse;
import hello.pet.board_service.infrastructure.feign.dto.response.UserDetailResponse;
import hello.pet.board_service.repository.PostRepository;
import hello.pet.board_service.web.dto.request.PostContentUpdateRequest;
import hello.pet.board_service.web.dto.request.PostCreateRequest;
import hello.pet.board_service.web.dto.request.PostGetRequest;
import hello.pet.board_service.web.dto.response.PostLikeResponse;
import hello.pet.board_service.web.dto.response.PostResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class PostServiceImpl implements PostService {
	private final PostRepository repository;
	private final ImageServiceClient imageServiceClient;
	private final UserServiceClient userServiceClient;

	@Override
	@Transactional
	public void save(PostCreateRequest request, Long userId) {
		log.info("게시글 생성 시작 - userId: {}, content length: {}, images count: {}",
			userId, request.content().length(),
			request.images() != null ? request.images().size() : 0);

		Post post = repository.save(
			Post.builder()
				.userId(userId)
				.content(request.content())
				.build()
		);

		log.info("게시글 저장 완료 - postId: {}", post.getId());

		if (request.images() != null && !request.images().isEmpty()) {
			// 빈 파일 필터링
			List<MultipartFile> validImages = request.images().stream()
				.filter(file -> file != null && !file.isEmpty())
				.toList();

			if (!validImages.isEmpty()) {
				log.info("이미지 업로드 시작 - 전체 이미지: {}, 유효한 이미지: {}",
					request.images().size(), validImages.size());
				List<PostImage> postImages = uploadImage(post, validImages);
				log.info("이미지 업로드 완료 - 업로드된 이미지 개수: {}", postImages.size());
				post.setImages(postImages);
				repository.save(post);
				log.info("이미지 정보가 포함된 게시글 저장 완료");
			} else {
				log.info("유효한 이미지가 없습니다. (빈 파일들만 있음)");
			}
		} else {
			log.info("업로드할 이미지가 없습니다.");
		}
	}

	@Override
	public Page<PostResponse> findAllPost(PostGetRequest request, Long currentUserId) {
		Sort sort = Sort.by(Sort.Direction.DESC, "createdAt");
		Pageable pageable = PageRequest.of(
			request.getPageBase(), request.size(), sort
		);

		log.info("게시글 조회 - currentUserId: {}", currentUserId);

		Page<Post> all;
		if (request.userId() != null) {
			// 특정 사용자의 게시글 조회
			all = repository.findByUserId(request.userId(), pageable);
		} else {
			// 전체 게시글 조회
			all = repository.findAll(pageable);
		}

		// 각 게시글의 사용자 정보를 가져와서 PostResponse 생성
		return all.map(post -> {
			UserDetailResponse userDetail = getUserDetail(post.getUserId());
			return PostResponse.from(post, currentUserId, userDetail);
		});
	}

	@Override
	public PostResponse findOne(String id) {
		Post post = findPostById(id);
		UserDetailResponse userDetail = getUserDetail(post.getUserId());
		return PostResponse.from(post, null, userDetail);
	}

	@Override
	public PostResponse findOne(String id, Long currentUserId) {
		Post post = findPostById(id);
		UserDetailResponse userDetail = getUserDetail(post.getUserId());
		return PostResponse.from(post, currentUserId, userDetail);
	}


	@Override
	@Transactional
	public String updatePostContent(String id, PostContentUpdateRequest request, Long userId) {
		Post post = findPostById(id);

		// 권한 검증: 게시글 작성자만 수정 가능
		if (!post.getUserId().equals(userId)) {
			throw new HelloPetException(HelloPetExceptionCode.FORBIDDEN);
		}

		// 내용만 업데이트 (이미지는 수정하지 않음)
		post.setContent(request.content());
		Post saved = repository.save(post);

		return saved.getId();
	}

	/**
	 * 게시글을 삭제합니다.
	 * 이미지 삭제가 실패하더라도 게시글 삭제는 완료됩니다.
	 * (이미지 삭제 실패는 로그로 기록되며, S3에 고아 객체가 남을 수 있습니다)
	 *
	 * @param id 삭제할 게시글 ID
	 */
	@Override
	@Transactional
	public void deletePostById(String id, Long userId) {
		Post post = findPostById(id);

		// 권한 검증: 게시글 작성자만 삭제 가능
		if (!post.getUserId().equals(userId)) {
			throw new HelloPetException(HelloPetExceptionCode.FORBIDDEN);
		}
		post.getImages().forEach(image -> deleteImage(image.getS3Key()));
		repository.delete(post);
	}

	@Override
	@Transactional
	public PostLikeResponse likePost(String id, Long userId) {

		// 먼저 좋아요 추가 시도
		long addResult = repository.addLike(id, userId);
		boolean isLiked;

		if (addResult > 0) {
			// 추가 성공
			isLiked = true;
		} else {
			// 이미 좋아요가 존재하므로 제거
			repository.removeLike(id, userId);
			isLiked = false;
		}

		Post post = findPostById(id);

		return PostLikeResponse.builder()
			.postId(id)
			.isLiked(isLiked)
			.likeCount(post.getLikeCount())
			.build();
	}

	private Post findPostById(String id) {
		return repository.findById(id)
			.orElseThrow(
				() -> new HelloPetException(HelloPetExceptionCode.NOT_FOUND_POST_BY_ID)
			);
	}

	private UserDetailResponse getUserDetail(Long userId) {
		try {
			ResponseEntity<UserDetailResponse> response = userServiceClient.getUserDetail(userId);
			return response.getBody();
		} catch (Exception e) {
			log.warn("Failed to fetch user detail for userId: {}, error: {}", userId, e.getMessage());
			return null; // 사용자 정보 조회 실패 시 null 반환
		}
	}


	private List<PostImage> uploadImage(Post post, List<MultipartFile> images) {
		if (CollectionUtils.isEmpty(images)) {
			return Collections.emptyList();
		}

		// post.getImages()가 null이 아닌 경우를 가정하고 (Post 엔티티 초기화 시 Linked/ArrayList로 초기화 추천),
		// 만약 null이라면 빈 리스트를 반환하거나 초기화합니다.
		List<PostImage> postImages = post.getImages() != null ? post.getImages() : new LinkedList<>();

		// 기존 이미지 개수(순서의 시작점)로 AtomicInteger 초기화
		AtomicInteger order = new AtomicInteger(postImages.size());

		List<PostImage> newlyUploadedImages = new LinkedList<>();

		images.forEach(image -> {
			String s3Key = uploadImage(post.getUserId(), post.getId(), image);
			newlyUploadedImages.add(PostImage.builder()
				.s3Key(s3Key)
				// postImages.size()부터 순서 할당 시작
				.displayOrder(order.getAndIncrement())
				.build());
		});

		// 새로 업로드된 이미지 리스트만 반환하여 호출 측(save, edit)에서 기존 postImages에 추가하도록 유도
		return newlyUploadedImages;
	}

	// S3 키 하나를 업로드하고 반환하는 메서드 (로직 분리)
	private String uploadImage(Long userId, String postId, MultipartFile file) {
		log.info("이미지 업로드 시작 - userId: {}, postId: {}, fileName: {}, fileSize: {}",
			userId, postId, file.getOriginalFilename(), file.getSize());

		ImageUploadResponse body;

		try {
			// Feign 클라이언트 호출 - feed 타입으로 고정
			ResponseEntity<ImageUploadResponse> response = imageServiceClient.uploadImage(userId, postId, "feed", file);
			body = response.getBody(); // 응답 본문을 추출
			log.info("이미지 업로드 Feign 호출 성공 - fileName: {}, response status: {}",
				file.getOriginalFilename(), response.getStatusCode());
		} catch (feign.FeignException e) {
			// 💡 Feign 통신 오류 (4xx, 5xx 응답 등) 명시적 처리
			log.error("이미지 서비스 Feign 통신 오류 (Status: {}): {}", e.status(), file.getOriginalFilename(), e);
			throw new HelloPetException(HelloPetExceptionCode.IMAGE_UPLOAD_FAIL);
		} catch (Exception e) {
			// 기타 연결/예상치 못한 오류 처리
			log.error("이미지 업로드 중 예상치 못한 오류 발생: {}", file.getOriginalFilename(), e);
			throw new HelloPetException(HelloPetExceptionCode.INTERNAL_SERVER_ERROR);
		}

		// 💡 응답 바디 및 S3 키 유효성 검사 강화
		if (body == null || body.s3Key() == null || body.s3Key().trim().isEmpty()) {
			log.error("이미지 업로드 응답에서 S3 키가 누락되었습니다. 파일명: {}", file.getOriginalFilename());
			// body가 null일 때도 명확한 예외 처리
			throw new HelloPetException(HelloPetExceptionCode.IMAGE_UPLOAD_FAIL);
		}

		log.info("이미지 업로드 성공 - fileName: {}, s3Key: {}", file.getOriginalFilename(), body.s3Key());
		return body.s3Key();
	}

	private void deleteImage(String s3Key) {
		try {
			imageServiceClient.deleteImage(s3Key);
		} catch (feign.FeignException e) {
			// S3 삭제 실패는 로그로 남기고 게시글 수정은 성공으로 간주 (데이터 불일치 발생 가능성 인지)
			log.error("S3 이미지 삭제 Feign 통신 오류 (Status: {}): S3 Key: {}", e.status(), s3Key, e);
			// 이 부분은 비즈니스 정책에 따라 예외를 던지거나 무시할 수 있습니다.
		} catch (Exception e) {
			log.error("S3 이미지 삭제 중 예상치 못한 오류 발생: S3 Key: {}", s3Key, e);
		}
	}
}
