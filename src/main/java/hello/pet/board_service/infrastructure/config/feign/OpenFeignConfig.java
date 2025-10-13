package hello.pet.board_service.infrastructure.config.feign;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import com.fasterxml.jackson.databind.ObjectMapper;

import feign.Logger;
import feign.codec.Encoder;
import feign.form.spring.SpringFormEncoder;
import feign.jackson.JacksonEncoder;

@Configuration
public class OpenFeignConfig {

	@Bean
	public Logger.Level feignLoggerLevel() {
		return Logger.Level.FULL;
	}

	@Bean
	public SpringFormEncoder feignFormEncoder() {
		return new SpringFormEncoder();
	}

	@Bean
	@Primary
	public Encoder feignEncoder(ObjectMapper objectMapper, SpringFormEncoder formEncoder) {
		// JacksonEncoder를 기본 인코더로 사용
		Encoder jacksonEncoder = new JacksonEncoder(objectMapper);

		// SpringFormEncoder에 기본 인코더(Jackson)를 주입
		return new SpringFormEncoder(jacksonEncoder); // <--- 이 부분이 JSON과 Multipart를 모두 처리합니다.
	}
}
