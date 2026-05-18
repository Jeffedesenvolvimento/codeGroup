package br.com.codegroup.config;

import br.com.codegroup.service.TokenService;
import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FeignConfig {

    private final TokenService tokenService;

    public FeignConfig(TokenService tokenService) {
        this.tokenService = tokenService;
    }

    @Bean
    public RequestInterceptor jwtRequestInterceptor() {
        return requestTemplate -> {
            String token = tokenService.getToken();
            requestTemplate.header("Authorization", "Bearer " + token);
        };
    }
}

