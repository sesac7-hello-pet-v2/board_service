package hello.pet.board_service.infrastructure.config.swagger.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import hello.pet.board_service.infrastructure.exception.HelloPetExceptionCode;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.ANNOTATION_TYPE})
public @interface ApiErrorCodeExamples {
	HelloPetExceptionCode[] value() default {};
}
