package hello.pet.board_service.infrastructure.config.feign;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import feign.Logger;
import feign.form.spring.SpringFormEncoder;

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
}
