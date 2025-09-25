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
	;

	private final HttpStatus status;
	private final String code;
	private final String message;

	public String getExplainError() throws NoSuchFieldException {
		// 1. CoTaskExceptionCode 클래스에서 현재 enum 상수의 필드를 찾음
		Field field = HelloPetExceptionCode.class.getField(this.name());
		ExplainError annotation = field.getAnnotation(ExplainError.class);
		return Objects.nonNull(annotation) ? annotation.value() : this.message;
	}
}
