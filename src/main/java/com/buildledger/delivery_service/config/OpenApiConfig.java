package com.buildledger.delivery_service.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("BuildLedger Delivery & Service Tracking API")
                        .version("1.0")
                        .description("API documentation for the Delivery and Service Tracking Microservice of BuildLedger.")
                        .contact(new Contact()
                                .name("BuildLedger Support")
                                .email("support@buildledger.com"))
                        .license(new License()
                                .name("Apache 2.0")
                                .url("http://springdoc.org")));
    }
}
