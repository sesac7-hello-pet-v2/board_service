package hello.pet.board_service.infrastructure.handler;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import hello.pet.board_service.infrastructure.exception.HelloPetException;
import hello.pet.board_service.web.dto.common.ExceptionResponse;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {
	/**
	 * HelloPetException을 처리하여 표준화된 에러 응답과 예외에 지정된 HTTP 상태를 반환한다.
	 *
	 * @param e 처리할 HelloPetException 인스턴스 — 응답의 HTTP 상태, 코드, 메시지를 제공한다.
	 * @return 응답 본문에 ExceptionResponse(예외의 상태, 코드, 메시지)를 담고 예외가 지정한 HTTP 상태를 설정한 ResponseEntity
	 */
	@ExceptionHandler(HelloPetException.class)
	public ResponseEntity<?> handleCoTaskException(HelloPetException e) {
		log.error("CoTaskException occurred: {}", e.getMessage(), e);
		return ResponseEntity.status(e.getStatus())
			.body(
				ExceptionResponse.of(
					e.getStatus(), e.getCode(), e.getMessage()
				)
			);
	}
}
