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
