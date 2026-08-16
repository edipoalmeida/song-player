package com.songplayer.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** Registers the OpenAPI document metadata and security schemes. */
@Configuration
public class OpenApiConfiguration {

    static final String BASIC_AUTH = "basicAuth";

    @Bean
    OpenAPI musicStreamingOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("Song Player API")
                        .version("v1")
                        .description("""
                                REST API for the Song Player application.

                                **Features**
                                - Music catalog discovery (songs, artists, albums)
                                - Playlist lifecycle management (create, update, delete, reorder)
                                - Playback queue generation with configurable shuffle strategies
                                - Song recommendations based on playlist content
                                - Playlist export (JSON, M3U)
                                - Centralized player state and controls

                                **Authentication**
                                Protected endpoints use HTTP Basic authentication.
                                Public endpoints: `GET /api/v1/songs/**`, `/actuator/health`, `/swagger-ui/**`.
                                """)
                        .contact(new Contact().name("Song Player Team"))
                        .license(new License().name("Internal")))
                .components(new Components()
                        .addSecuritySchemes(BASIC_AUTH, new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("basic")
                                .description("HTTP Basic credentials. Local dev default: `admin` / `admin`.")))
                .addSecurityItem(new SecurityRequirement().addList(BASIC_AUTH));
    }
}
