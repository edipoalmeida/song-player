package com.songplayer.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

/** Request body for adding every song by an artist to a playlist. */
@Schema(description = "Request body for adding all songs by a given artist to a playlist")
public record AddSongsFromArtistRequest(
        @Schema(description = "ID of the artist whose songs will be added", example = "00000000-0000-0000-0000-00000000002f")
        @NotNull UUID artistId
) {
}
