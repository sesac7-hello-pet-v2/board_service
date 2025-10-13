package hello.pet.board_service.infrastructure.exception;

import java.lang.reflect.Field;
import java.util.Objects;

import org.springframework.http.HttpStatus;

import hello.pet.board_service.infrastructure.config.swagger.annotation.ExplainError;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum HelloPetExceptionCode {
	@ExplainError("서버 내부 오류가 발생했을 때 반환됩니다.")
	INTERNAL_SERVER_ERROR(
		HttpStatus.INTERNAL_SERVER_ERROR,
		"INTERNAL_SERVER_ERROR",
		"An internal server error occurred."
	),
	@ExplainError("이미지 업로드 실패할 경우 발생하는 에러입니다.")
	IMAGE_UPLOAD_FAIL(
		HttpStatus.INTERNAL_SERVER_ERROR,
		"IMAGE_UPLOAD_FAIL",
		"이미지 업로드 실패"
	),
	@ExplainError("ID를 통한 게시글의 조회에 실패 시 발생하는 에러입니다.")
	NOT_FOUND_POST_BY_ID(
		HttpStatus.NOT_FOUND,
		"POST_NOT_FOUND_BY_ID",
		"해당 ID의 게시글을 조회할 수 없습니다."
	)
	;

	private final HttpStatus status;
	private final String code;
	private final String message;

	/**
	 * 열거형 상수에 연결된 `@ExplainError` 애너테이션의 설명 문구를 반환합니다.
	 *
	 * @return 애너테이션의 `value()`에 해당하는 설명 문자열. 해당 애너테이션이 없으면 enum 인스턴스의 `message` 값을 반환합니다.
	 * @throws NoSuchFieldException 현재 열거형 상수에 대응하는 필드를 찾을 수 없을 경우 발생합니다.
	 */
	public String getExplainError() throws NoSuchFieldException {
		// 1. CoTaskExceptionCode 클래스에서 현재 enum 상수의 필드를 찾음
		Field field = HelloPetExceptionCode.class.getField(this.name());
		ExplainError annotation = field.getAnnotation(ExplainError.class);
		return Objects.nonNull(annotation) ? annotation.value() : this.message;
	}
}
