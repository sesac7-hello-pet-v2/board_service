package hello.pet.board_service.service;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

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
import hello.pet.board_service.repository.PostRepository;
import hello.pet.board_service.web.dto.request.PostCreateRequest;
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
		/* TODO: API Gateway 구현이 완료가 되면 사용자의 ID를 통한 사용자의 정보를 담는 것도 좋을 것 같음.
		현재는 테스트를 위한 코드임
		 */
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

	private List<PostImage> uploadImage(Long userId, String postId, List<MultipartFile> images) {
		if (CollectionUtils.isEmpty(images)) {
			return Collections.emptyList();
		}

		List<PostImage> postImages = new LinkedList<>();
		AtomicInteger order = new AtomicInteger(0);

		images.forEach(image -> {
			try {
				ImageUploadResponse body = imageServiceClient.uploadImage(userId, postId, image).getBody();

				if (body != null) {
					postImages.add(PostImage.builder()
						.s3Key(body.s3Key())
						.displayOrder(order.getAndIncrement())
						.build());
				}
			} catch (Exception e) {
				log.error("이미지 업로드 실패: {}", image.getOriginalFilename(), e);
				throw new HelloPetException(HelloPetExceptionCode.IMAGE_UPLOAD_FAIL);
			}
		});
		return postImages;
	}
}
