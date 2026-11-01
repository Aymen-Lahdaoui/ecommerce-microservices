package com.ecommerce.cartservice.config;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * Intercepteur Feign qui propage automatiquement le token JWT
 * de la requête entrante vers tous les appels Feign sortants.
 * Cela permet au cart-service d'appeler product-service avec le token
 * du client authentifié, évitant ainsi les erreurs 401.
 */
@Configuration
public class FeignClientInterceptor {

    private static final String AUTHORIZATION_HEADER = "Authorization";

    @Bean
    public RequestInterceptor requestInterceptor() {
        return (RequestTemplate template) -> {
            ServletRequestAttributes attributes =
                    (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();

            if (attributes != null) {
                HttpServletRequest request = attributes.getRequest();
                String authorizationHeader = request.getHeader(AUTHORIZATION_HEADER);

                if (authorizationHeader != null && !authorizationHeader.isBlank()) {
                    template.header(AUTHORIZATION_HEADER, authorizationHeader);
                }
            }
        };
    }
}
