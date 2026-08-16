package com.songplayer.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** Request body for updating playlist metadata. */
@Schema(description = "Request body for updating playlist metadata")
public record UpdatePlaylistRequest(
        @Schema(description = "Playlist name", example = "Favoritos Atualizados", maxLength = 120)
        @NotBlank @Size(max = 120) String name,

        @Schema(description = "Optional description", example = "Músicas preferidas — edição especial", maxLength = 1000)
        @Size(max = 1_000) String description,

        @Schema(description = "URL of the cover image", example = "https://example.com/new-cover.jpg", maxLength = 2048)
        @Size(max = 2_048) String coverImageUrl
) {
}
