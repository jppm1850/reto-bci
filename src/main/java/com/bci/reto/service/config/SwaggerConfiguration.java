package com.bci.reto.service.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "API BCI",
                version = "1.0",
                description = "Documentation APIs v1.0"
        )
)
public class SwaggerConfiguration {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new io.swagger.v3.oas.models.info.Info()
                        .title("API BCI")
                        .version("1.0")
                        .description("Documentation APIs v1.0")
                        .contact(new Contact()
                                .name("BCI")
                                .email("support@bci.com")));
    }
}
