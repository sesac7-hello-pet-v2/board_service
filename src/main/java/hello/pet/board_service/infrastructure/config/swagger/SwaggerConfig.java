package hello.pet.board_service.infrastructure.config.swagger;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springdoc.core.customizers.OperationCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.http.HttpStatus;
import org.springframework.web.method.HandlerMethod;

import hello.pet.board_service.infrastructure.config.swagger.annotation.ApiErrorCodeExamples;
import hello.pet.board_service.infrastructure.exception.HelloPetExceptionCode;
import hello.pet.board_service.web.dto.common.ExceptionResponse;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.examples.Example;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.responses.ApiResponses;
import io.swagger.v3.oas.models.servers.Server;
import lombok.Builder;
import lombok.Getter;

@Configuration
public class SwaggerConfig {
	/**
	 * OpenAPI 사양을 설정하여 Swagger/OpenAPI 문서용 빈을 생성합니다.
	 *
	 * <p>API 문서의 기본 메타정보(타이틀, 버전, 설명)를 설정하고 로컬 개발 서버를 서버 목록에 추가합니다.
	 * 설명에는 HTML이 포함될 수 있으며, 생성된 OpenAPI 객체는 Spring의 OpenAPI/Swagger UI에서 사용됩니다.</p>
	 *
	 * @return 구성된 OpenAPI 인스턴스
	 */
	@Bean
	public OpenAPI openAPI() {
		return new OpenAPI()
			.info(new Info()
				.title("게시판(피드) 관련 API 명세서")
				.version("1.0.0")
				.description("Hello Pet(v2) 게시판 서버의 API 문서입니다. <br>" +
					"이 문서는 사용자 서버의 엔드포인트와 요청/응답 형식에 대한 정보를 제공합니다."
				)
			).servers(
				List.of(
					new Server().url("http://localhost:8083").description("로컬 개발 서버")
				)
			);
	}

	/**
	 * 컨트롤러 메서드 또는 그 인터페이스에 선언된 ApiErrorCodeExamples 애노테이션에 따라
	 * OpenAPI Operation에 에러 코드 예시 응답을 주입하는 OperationCustomizer 빈을 반환합니다.
	 *
	 * 상세:
	 * - 반환되는 OperationCustomizer는 각 Operation에 대해 핸들러 메서드에서 ApiErrorCodeExamples를 찾고,
	 *   존재하면 해당 예외 코드들을 기반으로 응답 예시를 생성하여 Operation에 추가합니다.
	 * - 이 커스터마이저는 문서 생성 시점에 Operation 객체를 수정하여 각 HTTP 상태별 에러 예시를 확장합니다.
	 *
	 * @return ApiErrorCodeExamples 애노테이션을 기반으로 Operation의 에러 응답 예시를 동적으로 추가하는 OperationCustomizer 빈
	 */
	@Bean
	public OperationCustomizer operationCustomizer() {
		return (Operation operation, HandlerMethod handlerMethod) -> {
			ApiErrorCodeExamples annotation = findAnnotation(handlerMethod);

			if (annotation != null) {
				generateErrorCodeResponseExample(operation, annotation.value());
			}
			return operation;
		};
	}

	/**
	 * 핸들러 메서드에서 ApiErrorCodeExamples 애노테이션을 찾아 반환합니다.
	 *
	 * 우선 컨트롤러 빈의 인터페이스 타입에 선언된 애노테이션을 확인하고,
	 * 없으면 실제 핸들러 메서드에 선언된 애노테이션을 확인합니다.
	 *
	 * @param handlerMethod 애노테이션을 검색할 스프링 핸들러 메서드
	 * @return 발견된 ApiErrorCodeExamples 인스턴스 또는 없으면 {@code null}
	 */
	private ApiErrorCodeExamples findAnnotation(HandlerMethod handlerMethod) {
		// 메서드 어노테이션을 먼저 찾고, 없으면 클래스 어노테이션을 찾음
		Optional<ApiErrorCodeExamples> annotation = Optional.ofNullable(
			// 1. 메서드에서 어노테이션 찾기
			AnnotatedElementUtils.findMergedAnnotation(
				handlerMethod.getMethod(), ApiErrorCodeExamples.class
			)
		).or(() -> Optional.ofNullable(
			// 2. 클래스에서 어노테이션 찾기
			AnnotatedElementUtils.findMergedAnnotation(
				handlerMethod.getBeanType(), ApiErrorCodeExamples.class
			)
		));
		// 3. 인터페이스에서 어노테이션 찾기
		return annotation.orElseGet(() -> findAnnotationInInterfaces(handlerMethod));
	}

	private ApiErrorCodeExamples findAnnotationInInterfaces(HandlerMethod handlerMethod) {
		Class<?> beanType = handlerMethod.getBeanType();
		return Arrays.stream(beanType.getInterfaces())
			.map(anInterface -> {
				// 1. 인터페이스 자체에 어노테이션이 있는지 확인
				ApiErrorCodeExamples typeAnnotation = AnnotatedElementUtils.findMergedAnnotation(
					anInterface, ApiErrorCodeExamples.class
				);
				if (typeAnnotation != null) {
					return Optional.of(typeAnnotation);
				}

				// 2. 인터페이스의 메서드에 어노테이션이 있는지 확인
				try {
					Method method = anInterface.getMethod(
						handlerMethod.getMethod().getName(),
						handlerMethod.getMethod().getParameterTypes()
					);
					return Optional.ofNullable(
						AnnotatedElementUtils.findMergedAnnotation(method, ApiErrorCodeExamples.class)
					);
				} catch (NoSuchMethodException e) {
					// 인터페이스에 해당 메서드가 없는 경우는 어노테이션이 없는 것으로 간주하고 처리 (에러로 보지 않음)
					return Optional.<ApiErrorCodeExamples>empty();
				}
			})
			.filter(Optional::isPresent) // Optional.empty()인 경우는 필터링
			.map(Optional::get) // 값 추출
			.findFirst() // 첫 번째로 찾은 어노테이션 반환
			.orElse(null); // 모든 인터페이스에서 찾지 못하면 null 반환
	}

	/**
	 * 400 BAD_REQUEST에 해당하는 검증(Validation) 오류의 Swagger Example을 담은 ExampleHolder를 생성하여 반환합니다.
	 *
	 * 반환되는 ExampleHolder는:
	 * - holder: CommonResponse.fail로 감싼 ExceptionResponse(상태 400, 코드 "VALIDATION_ERROR", 메시지 및 필드별 상세 메시지)를 포함하는 Swagger Example
	 * - name: "VALIDATION_ERROR"
	 * - code: 400
	 *
	 * 이 예시는 API 문서에서 입력값 검증 실패 사례를 예시로 보여주기 위해 사용됩니다.
	 *
	 * @return 검증 오류 예시를 담은 ExampleHolder(이름 "VALIDATION_ERROR", HTTP 상태 코드 400)
	 */
	private ExampleHolder getValidationErrorHolder() {
		Example example = new Example();
		example.setValue(
			ExceptionResponse.of(
				HttpStatus.BAD_REQUEST,
				"VALIDATION_ERROR",
				"입력값이 유효하지 않습니다.",
				Map.of(
					"field1", "field1은(는) 필수 입력 항목입니다.",
					"field2", "field2은(는) 0 이상이어야 합니다."
				) // 필드별 메시지 포함
			)
		);
		example.setDescription("입력값이 validation 기준을 통과하지 못했을 때 발생하는 에러 예시입니다.");
		return ExampleHolder.builder()
			.holder(example)
			.name("VALIDATION_ERROR")
			.code(400)
			.build();
	}

	/**
	 * 주어진 예외 코드 배열을 HTTP 상태 코드별로 그룹화하여 ExampleHolder 목록 맵으로 반환합니다.
	 *
	 * 각 ExampleHolder는 해당 예외 코드로부터 생성된 Swagger Example(`getSwaggerExample`)과
	 * 예외 코드 문자열(name), 그리고 예외 코드의 HTTP 상태값(code)을 포함합니다. 결과 맵의 키는
	 * HTTP 상태 코드(int)이며 값은 해당 상태 코드에 매핑된 ExampleHolder 리스트입니다.
	 *
	 * @param codes 그룹화할 HelloPetExceptionCode 배열
	 * @return HTTP 상태 코드별로 그룹화된 ExampleHolder 리스트의 맵
	 */
	private Map<Integer, List<ExampleHolder>> getGroupedExamples(HelloPetExceptionCode[] codes) {
		return Arrays.stream(codes)
			.map(code -> ExampleHolder.builder()
				.holder(getSwaggerExample(code))
				.name(code.name())
				.code(code.getStatus().value())
				.build()
			)
			.collect(Collectors.groupingBy(ExampleHolder::getCode));
	}

	/**
	 * 주어진 HelloPetExceptionCode로부터 Swagger Example 객체를 생성합니다.
	 *
	 * <p>예제 값은 CommonResponse.fail(...) 형태의 실패 응답으로 구성되며,
	 * 내부 값은 ExceptionResponse.of(status, code, message)입니다.
	 * Example의 설명(description)은 먼저 code.getExplainError()를 호출하여 얻고,
	 * 해당 호출이 NoSuchFieldException을 던지면 code.getMessage()로 대체합니다.
	 *
	 * @param code 처리할 예외 코드 (예: 상태, 코드, 메시지를 포함)
	 * @return Swagger용 Example 객체(값은 CommonResponse.fail(...)로 설정되고 description은 설명 문자열)
	 */
	private Example getSwaggerExample(HelloPetExceptionCode code) {
		String explain = "";
		try {
			explain = code.getExplainError();
		} catch (NoSuchFieldException e) {
			explain = code.getMessage();
		}

		ExceptionResponse fail = ExceptionResponse.of(
			code.getStatus(),
			code.getCode(),
			code.getMessage()
		);

		Example example = new Example();
		example.setValue(fail);
		example.setDescription(explain);
		return example;
	}

	/**
	 * 주어진 OpenAPI Operation에 예외 코드 기반의 예제 응답들을 추가합니다.
	 *
	 * <p>전달된 HelloPetExceptionCode 배열을 HTTP 상태 코드별로 그룹화하여 각 상태 코드에 대응하는
	 * application/json 예제들을 ApiResponses로 삽입합니다. 또한 항상 400(BAD_REQUEST) 그룹에는
	 * 유효성 검사 실패 예제(validation error)를 함께 추가합니다. 이 메서드는 전달된
	 * Operation 객체의 응답(ApiResponses)을 직접 변경합니다.</p>
	 *
	 * @param operation 예제 응답을 추가할 OpenAPI Operation 객체
	 * @param codes     문서에 포함할 HelloPetExceptionCode 배열(각 예외는 해당하는 HTTP 상태 코드에 매핑됨)
	 */
	private void generateErrorCodeResponseExample(
		Operation operation,
		HelloPetExceptionCode[] codes
	) {
		ApiResponses responses = operation.getResponses();

		ExampleHolder validationErrorHolder = getValidationErrorHolder();

		// `HelloPetExceptionCode` 배열을 기반으로 `ExampleHolder`를 생성 및 그룹화
		Map<Integer, List<ExampleHolder>> groupedExamples = getGroupedExamples(codes);

		// Validation Error를 400 상태 코드 그룹에 추가
		groupedExamples.computeIfAbsent(400, k -> new java.util.ArrayList<>()).add(validationErrorHolder);

		// 모든 예외를 API 응답에 추가
		groupedExamples.forEach((status, examples) -> {
			final String statusKey = String.valueOf(status);
			ApiResponse apiResponse = Optional.ofNullable(responses.get(statusKey))
				.orElseGet(ApiResponse::new);
			Content content = Optional.ofNullable(apiResponse.getContent())
				.orElseGet(Content::new);
			MediaType mediaType = Optional.ofNullable(content.get("application/json"))
				.orElseGet(MediaType::new);

			examples.forEach(example -> {
				mediaType.addExamples(example.getName(), example.getHolder());
			});

			content.addMediaType("application/json", mediaType);

			apiResponse.setContent(content);
			responses.addApiResponse(String.valueOf(status), apiResponse);
		});
	}
}

@Getter
@Builder
class ExampleHolder {
	private Example holder;
	private String name;
	private int code;
}
