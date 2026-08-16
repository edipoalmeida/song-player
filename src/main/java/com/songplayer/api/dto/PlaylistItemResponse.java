package com.songplayer.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.UUID;

/** Playlist item payload with resolved song details. */
@Schema(description = "A song entry inside a playlist, with its position and full song details")
public record PlaylistItemResponse(
        @Schema(description = "Unique identifier of this playlist entry") UUID id,
        @Schema(description = "Full song details") SongResponse song,
        @Schema(description = "Zero-based position of this item in the playlist", example = "0") int position
) {
}
