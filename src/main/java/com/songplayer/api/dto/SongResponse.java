package com.songplayer.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import java.util.UUID;

/** Catalog song payload returned by the API. */
@Schema(description = "Catalog song with full metadata")
public record SongResponse(
        @Schema(description = "Unique song identifier") UUID id,
        @Schema(description = "Song title", example = "Bohemian Rhapsody") String title,
        @Schema(description = "Artists credited on this song") List<ArtistResponse> artists,
        @Schema(description = "Genre names associated with this song", example = "[\"Rock\", \"Classic Rock\"]") List<String> genres,
        @Schema(description = "Duration in seconds", example = "354") int durationSeconds,
        @Schema(description = "Playback URI / streaming URL", example = "https://cdn.example.com/songs/bohemian-rhapsody.mp3") String uri,
        @Schema(description = "Album the song belongs to") AlbumResponse album
) {
}
