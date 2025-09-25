package hello.pet.board_service.infrastructure.exception;

import org.springframework.http.HttpStatus;

import lombok.Getter;

@Getter
public class HelloPetException extends RuntimeException {
	private final HttpStatus status;
	private final String code;
	private final String message;

	public HelloPetException(HelloPetExceptionCode code) {
		this.status = code.getStatus();
		this.code = code.getCode();
		this.message = code.getMessage();
	}
}
