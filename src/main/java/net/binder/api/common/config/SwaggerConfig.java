package net.binder.api.common.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.security.SecurityScheme.Type;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;

@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "Binder",
                description = "Binder API 명세서"))
public class SwaggerConfig {

    private final String serverUrl;

    public SwaggerConfig(@Value("${swagger.server.url}") String serverUrl) {
        this.serverUrl = serverUrl;
    }

    @Bean
    public OpenAPI openAPI() {
        String jwtSchemaName = HttpHeaders.AUTHORIZATION;

        SecurityRequirement securityRequirement = new SecurityRequirement()
                .addList(jwtSchemaName);

        Components components = new Components()
                .addSecuritySchemes(jwtSchemaName, new SecurityScheme()
                        .type(Type.HTTP)
                        .scheme("bearer")
                        .bearerFormat("JWT"));

        return new OpenAPI()
                .addServersItem(new Server().url(serverUrl))
                .addSecurityItem(securityRequirement)
                .components(components);
    }

}
