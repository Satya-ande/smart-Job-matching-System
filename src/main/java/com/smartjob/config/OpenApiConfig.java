package com.smartjob.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * OpenAPI / Swagger configuration.
 *
 * Swagger UI available at: http://localhost:8080/swagger-ui.html
 * API docs available at:   http://localhost:8080/v3/api-docs
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI smartJobOpenAPI() {
        return new OpenAPI()
            .info(new Info()
                .title("SmartJob — Smart Job Matching & Skill Gap Analysis API")
                .description("REST API for the Smart Job Matching System V2. "
                    + "Provides candidate management, job posting, intelligent matching "
                    + "using weighted scoring algorithms, skill-gap analysis, "
                    + "and application tracking.")
                .version("2.0.0")
                .contact(new Contact()
                    .name("SmartJob Team"))
                .license(new License()
                    .name("MIT License")))
            .addSecurityItem(new SecurityRequirement().addList("Bearer Token"))
            .components(new io.swagger.v3.oas.models.Components()
                .addSecuritySchemes("Bearer Token", new SecurityScheme()
                    .type(SecurityScheme.Type.HTTP)
                    .scheme("bearer")
                    .bearerFormat("JWT")
                    .description("JWT authentication token")));
    }
}
