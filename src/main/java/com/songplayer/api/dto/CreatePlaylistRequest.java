package com.songplayer.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** Request body for creating a playlist. */
@Schema(description = "Request body for creating a new playlist")
public record CreatePlaylistRequest(
        @Schema(description = "Playlist name", example = "Favoritos", maxLength = 120)
        @NotBlank @Size(max = 120) String name,

        @Schema(description = "Optional description", example = "Melhores músicas do momento", maxLength = 1000)
        @Size(max = 1_000) String description,

        @Schema(description = "URL of the cover image", example = "https://example.com/cover.jpg", maxLength = 2048)
        @Size(max = 2_048) String coverImageUrl
) {
}
