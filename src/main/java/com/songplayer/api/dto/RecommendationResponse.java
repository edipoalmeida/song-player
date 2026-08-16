package com.songplayer.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

/** Recommended songs for a playlist. */
@Schema(description = "Catalog songs recommended as additions to the playlist")
public record RecommendationResponse(
        @Schema(description = "List of recommended catalog songs") List<SongResponse> songs
) {
}
