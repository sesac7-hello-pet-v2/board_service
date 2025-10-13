package hello.pet.board_service.service;

import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

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
				post,
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
		// 1. 삭제할 이미지 처리 및 S3 Key 수집
		Set<Integer> ordersToDelete =
			(request.deleteImageOrders() != null) ?
				new HashSet<>(request.deleteImageOrders()) :
				Collections.emptySet();
		List<String> s3KeysToDelete = new LinkedList<>();

		if (!ordersToDelete.isEmpty()) {
			List<PostImage> currentImages = post.getImages();
			// 삭제 대상을 걸러냅니다. (Java 8 filter 사용)
			List<PostImage> retainedImages = currentImages.stream()
				.filter(image -> {
					if (ordersToDelete.contains(image.getDisplayOrder())) {
						s3KeysToDelete.add(image.getS3Key()); // S3 삭제 목록에 추가
						return false; // 리스트에서 제거 (삭제)
					}
					return true; // 리스트에 유지
				})
				.collect(Collectors.toCollection(LinkedList::new));

			// 2. 남은 이미지로 리스트 교체 및 순서 재정렬
			AtomicInteger order = new AtomicInteger(0);
			retainedImages.forEach(image -> image.setDisplayOrder(order.getAndIncrement()));
			post.setImages(retainedImages); // @OneToMany 관계에서 삭제가 일어나도록 유도
		}

		// 3. 추가하는 이미지 업로드 및 리스트에 추가
		List<PostImage> newImages = uploadImage(post, request.file());
		if (!CollectionUtils.isEmpty(newImages)) {
			post.getImages().addAll(newImages); // 기존 리스트에 추가
		}

		// 4. DB 저장 (Dirty Checking으로 변경 사항 반영)
		Post saved = repository.save(post);

		// 5. S3 이미지 삭제 (비동기 처리 고려, 현재는 todo 주석 처리)
		s3KeysToDelete.forEach(imageServiceClient::deleteImage);

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
