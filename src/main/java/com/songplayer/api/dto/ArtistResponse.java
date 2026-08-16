package com.songplayer.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.UUID;

/** Artist payload returned by the API. */
@Schema(description = "Artist summary")
public record ArtistResponse(
        @Schema(description = "Unique artist identifier") UUID id,
        @Schema(description = "Artist name", example = "Queen") String name
) {
}
