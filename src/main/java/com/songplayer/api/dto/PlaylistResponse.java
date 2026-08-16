package com.songplayer.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

/** Playlist payload returned by playlist endpoints. */
@Schema(description = "Playlist with its metadata and ordered list of songs")
public record PlaylistResponse(
        @Schema(description = "Unique playlist identifier") UUID id,
        @Schema(description = "Playlist name", example = "Favoritos") String name,
        @Schema(description = "Optional description", example = "Melhores músicas do momento") String description,
        @Schema(description = "URL of the cover image", example = "https://example.com/cover.jpg") String coverImageUrl,
        @Schema(description = "Ordered list of songs in the playlist") List<PlaylistItemResponse> items,
        @Schema(description = "Timestamp when the playlist was created") Instant createdAt,
        @Schema(description = "Timestamp of the last update") Instant updatedAt
) {
}
