package com.buildledger.iam.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class SwaggerConfig {

    @Value("${server.port:8081}")
    private String serverPort;

    private static final String SECURITY_SCHEME_NAME = "BearerAuth";

    @Bean
    public OpenAPI buildLedgerOpenAPI() {
        return new OpenAPI()
                .info(apiInfo())
                .servers(apiServers())
                .addSecurityItem(new SecurityRequirement().addList(SECURITY_SCHEME_NAME))
                .components(new Components()
                        .addSecuritySchemes(SECURITY_SCHEME_NAME,
                                new SecurityScheme()
                                        .name(SECURITY_SCHEME_NAME)
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                                        .description("Enter JWT Bearer token. Example: **Bearer eyJhbGci...**")
                        )
                );
    }

    private Info apiInfo() {
        return new Info()
                .title("BuildLedger IAM Service API")
                .version("1.0.0")
                .description("""
                        **Identity and Access Management** API for BuildLedger – Construction Contract & Vendor Management System.
                        
                        ## Features
                        - JWT Authentication (Access + Refresh tokens)
                        - Role-Based Access Control (RBAC)
                        - Vendor self-registration and document upload
                        - Client registration
                        - Admin user management
                        - Token blacklisting / logout
                        - Password reset flow
                        - Audit logging
                        - Internal microservice token validation
                        
                        ## Roles
                        `ADMIN` | `PROJECT_MANAGER` | `FINANCE_OFFICER` | `COMPLIANCE_OFFICER` | `AUDIT_OFFICER` | `VENDOR` | `CLIENT`
                        """)
                .contact(new Contact()
                        .name("BuildLedger Engineering")
                        .email("engineering@buildledger.com")
                        .url("https://buildledger.com"))
                .license(new License()
                        .name("Proprietary")
                        .url("https://buildledger.com/terms"));
    }

    private List<Server> apiServers() {
        Server localServer = new Server();
        localServer.setUrl("http://localhost:" + serverPort);
        localServer.setDescription("Local Development Server");

        Server stagingServer = new Server();
        stagingServer.setUrl("https://iam-staging.buildledger.com");
        stagingServer.setDescription("Staging Environment");

        return List.of(localServer, stagingServer);
    }
}
