package com.seek.candidatosmanagementapi.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.servers.Server;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.Components;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * Clase que uso para documentar la API
 *
 * @author Jose Osorio Catalan
 */
@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Candidatos Management API")
                        .version("1.0.0")
                        .description("API para gestionar candidatos en procesos de reclutamiento. " +
                                "Permite crear candidatos, listar todos y obtener métricas de edad.")
                        .contact(new Contact()
                                .name("Jose Osorio Catalan")
                                .email("jossekarlos10@gmail.com")))
                .servers(List.of(
                        new Server().url("http://localhost:8080").description("Desarrollo Local"),
                        new Server().url("http://api-candidatos-env.eba-svqmjzpm.us-east-1.elasticbeanstalk.com").description("Producción")
                ))
                .addSecurityItem(new SecurityRequirement().addList("basicAuth"))
                .components(new Components()
                        .addSecuritySchemes("basicAuth",
                                new SecurityScheme()
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("basic")
                                        .description("Autenticación HTTP Basic. Usuario: admin, Password: admin123")));
    }
}