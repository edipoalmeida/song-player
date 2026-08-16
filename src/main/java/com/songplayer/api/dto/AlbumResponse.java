package com.songplayer.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.UUID;

/** Album payload embedded in song responses. */
@Schema(description = "Album summary embedded in song responses")
public record AlbumResponse(
        @Schema(description = "Unique album identifier") UUID id,
        @Schema(description = "Album title", example = "A Night at the Opera") String title,
        @Schema(description = "Year the album was released", example = "1975") int releaseYear
) {}
