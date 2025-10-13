package hello.pet.board_service.service;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
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
import hello.pet.board_service.infrastructure.feign.dto.response.ImageUploadResponse;
import hello.pet.board_service.infrastructure.utils.Constants;
import hello.pet.board_service.repository.PostRepository;
import hello.pet.board_service.web.dto.request.PostCreateRequest;
import hello.pet.board_service.web.dto.request.PostEditRequest;
import hello.pet.board_service.web.dto.request.PostGetRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class PostServiceImpl implements PostService {
	private final PostRepository repository;
	private final ImageServiceClient imageServiceClient;

	@Override
	@Transactional
	public void save(PostCreateRequest request) {
		Post post = repository.save(
			Post.builder()
				.userId(request.userId())
				.content(request.content())
				.build()
		);

		if (!CollectionUtils.isEmpty(request.file())) {
			List<PostImage> postImages = uploadImage(
				request.userId(),
				post.getId(),
				request.file()
			);
			post.setImages(postImages);
			repository.save(post);
		}
	}

	@Override
	public Page<Post> findAllPost(PostGetRequest request) {
		Sort sort = Sort.by(Sort.Direction.DESC, "createdAt");
		Pageable pageable = PageRequest.of(
			request.getPageBase(), request.size(), sort
		);

		Page<Post> all;
		if (request.userId() == null) {
			all = repository.findAll(pageable);
		} else {
			all = repository.findByUserId(request.userId(), pageable);
		}
		all.getContent().forEach(this::exchangeImageUrl);
		return all;
	}

	@Override
	public Post findOne(String id) {
		Post post = findPostById(id);
		exchangeImageUrl(post);
		return post;
	}

	@Override
	public String editPostContentById(String id, PostEditRequest request) {
		Post post = findPostById(id);
		post.setContent(request.content());
		if (request.deleteImageOrders() != null && !request.deleteImageOrders().isEmpty()) {
			Set<Integer> ordersToDelete = new HashSet<>(request.deleteImageOrders());
			LocalDateTime now = LocalDateTime.now();
			post.getImages().forEach(image -> {
				if (ordersToDelete.contains(image.getDisplayOrder())) {
					image.setDisplayOrder(-1);
					image.setDeletedDate(now);
				}
			});
		}
		Post saved = repository.save(post);

		return saved.getId();
	}

	private Post findPostById(String id) {
		return repository.findById(id)
			.orElseThrow(
				() -> new HelloPetException(HelloPetExceptionCode.NOT_FOUND_POST_BY_ID)
			);
	}

	private void exchangeImageUrl(Post post) {
		post.getImages().forEach(image -> {
			if (image != null) {
				image.setS3Key(Constants.S3_IMAGE_BUCKET_URL + image.getS3Key());
			}
		});
	}

	private List<PostImage> uploadImage(Long userId, String postId, List<MultipartFile> images) {
		if (CollectionUtils.isEmpty(images)) {
			return Collections.emptyList();
		}

		List<PostImage> postImages = new LinkedList<>();
		AtomicInteger order = new AtomicInteger(0);

		images.forEach(image -> {
			String uploadImage = uploadImage(userId, postId, image);
			postImages.add(PostImage.builder()
				.s3Key(uploadImage)
				.displayOrder(order.getAndIncrement())
				.build());
		});
		return postImages;
	}

	// S3 키 하나를 업로드하고 반환하는 메서드 (로직 분리)
	private String uploadImage(Long userId, String postId, MultipartFile file) {
		ImageUploadResponse body;

		try {
			// Feign 클라이언트 호출
			ResponseEntity<ImageUploadResponse> response = imageServiceClient.uploadImage(userId, postId, file);
			body = response.getBody(); // 응답 본문을 추출
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

		return body.s3Key();
	}
}
