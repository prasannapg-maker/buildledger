package com.buildledger.vendor.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI buildLedgerOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("BuildLedger - Vendor Onboarding & Profile Management API")
                        .description("API documentation for the Vendor Onboarding module of BuildLedger. " +
                                "Covers vendor registration, document management, and internal service APIs.")
                        .version("1.0.0"));
    }
}
