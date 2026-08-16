package com.songplayer.api;

import com.songplayer.api.dto.AddSongsFromArtistRequest;
import com.songplayer.api.dto.AddSongRequest;
import com.songplayer.api.dto.AddSongsRequest;
import com.songplayer.api.dto.CreatePlaylistRequest;
import com.songplayer.api.dto.PlaylistResponse;
import com.songplayer.api.dto.RecommendationResponse;
import com.songplayer.api.dto.ReorderPlaylistRequest;
import com.songplayer.api.dto.UpdatePlaylistRequest;
import com.songplayer.api.error.ApiError;
import com.songplayer.domain.ShuffleMode;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.validation.annotation.Validated;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;

/** HTTP contract for playlist management. */
@Tag(name = "Playlists", description = "Playlist lifecycle, contents, queue generation and exports")
@RequestMapping("/api/v1/playlists")
@Validated
public interface PlaylistApi {

    @Operation(summary = "Create a playlist")
    @ApiResponse(responseCode = "201", description = "Playlist created")
    @ApiResponse(responseCode = "400", description = "Validation error",
        content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiError.class)))
    @ApiResponse(responseCode = "401", description = "Authentication required", content = @Content)
    @PostMapping
    ResponseEntity<PlaylistResponse> create(@Valid @RequestBody CreatePlaylistRequest request);

    @Operation(summary = "List playlists")
    @ApiResponse(responseCode = "200", description = "Playlists returned successfully")
    @ApiResponse(responseCode = "401", description = "Authentication required", content = @Content)
    @GetMapping
    List<PlaylistResponse> list();

    @Operation(summary = "Get a playlist by ID")
    @ApiResponse(responseCode = "200", description = "Playlist found")
    @ApiResponse(responseCode = "401", description = "Authentication required", content = @Content)
    @ApiResponse(responseCode = "404", description = "Playlist not found",
        content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiError.class)))
    @GetMapping("/{playlistId}")
    PlaylistResponse getById(@PathVariable UUID playlistId);

    @Operation(summary = "Update playlist metadata")
    @ApiResponse(responseCode = "200", description = "Playlist updated")
    @ApiResponse(responseCode = "400", description = "Validation error",
        content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiError.class)))
    @ApiResponse(responseCode = "401", description = "Authentication required", content = @Content)
    @ApiResponse(responseCode = "404", description = "Playlist not found",
        content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiError.class)))
    @PutMapping("/{playlistId}")
    PlaylistResponse update(@PathVariable UUID playlistId, @Valid @RequestBody UpdatePlaylistRequest request);

    @Operation(summary = "Delete a playlist")
    @ApiResponse(responseCode = "204", description = "Playlist deleted", content = @Content)
    @ApiResponse(responseCode = "401", description = "Authentication required", content = @Content)
    @ApiResponse(responseCode = "404", description = "Playlist not found",
        content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiError.class)))
    @DeleteMapping("/{playlistId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void delete(@PathVariable UUID playlistId);

    @Operation(summary = "Add a catalog song to a playlist")
    @ApiResponse(responseCode = "201", description = "Song added to playlist")
    @ApiResponse(responseCode = "400", description = "Validation error",
        content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiError.class)))
    @ApiResponse(responseCode = "401", description = "Authentication required", content = @Content)
    @ApiResponse(responseCode = "404", description = "Playlist or song not found",
        content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiError.class)))
    @PostMapping("/{playlistId}/songs")
    ResponseEntity<PlaylistResponse> addSong(@PathVariable UUID playlistId, @Valid @RequestBody AddSongRequest request);

    @Operation(summary = "Add multiple catalog songs to a playlist (bulk)")
    @ApiResponse(responseCode = "201", description = "Songs added to playlist")
    @ApiResponse(responseCode = "400", description = "Validation error",
        content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiError.class)))
    @ApiResponse(responseCode = "401", description = "Authentication required", content = @Content)
    @ApiResponse(responseCode = "404", description = "Playlist or song not found",
        content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiError.class)))
    @PostMapping("/{playlistId}/songs/batch")
    ResponseEntity<PlaylistResponse> addSongs(@PathVariable UUID playlistId, @Valid @RequestBody AddSongsRequest request);

    @Operation(summary = "Add all songs from an artist to a playlist")
    @ApiResponse(responseCode = "201", description = "Artist songs added to playlist")
    @ApiResponse(responseCode = "400", description = "Validation error",
        content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiError.class)))
    @ApiResponse(responseCode = "401", description = "Authentication required", content = @Content)
    @ApiResponse(responseCode = "404", description = "Playlist or artist not found",
        content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiError.class)))
    @PostMapping("/{playlistId}/songs/artist")
    ResponseEntity<PlaylistResponse> addSongsFromArtist(@PathVariable UUID playlistId, @Valid @RequestBody AddSongsFromArtistRequest request);

    @Operation(summary = "Remove a song occurrence from a playlist")
    @ApiResponse(responseCode = "204", description = "Song removed", content = @Content)
    @ApiResponse(responseCode = "401", description = "Authentication required", content = @Content)
    @ApiResponse(responseCode = "404", description = "Playlist or item not found",
        content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiError.class)))
    @DeleteMapping("/{playlistId}/items/{playlistItemId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void removeSong(@PathVariable UUID playlistId, @PathVariable UUID playlistItemId);

    @Operation(summary = "Replace playlist item ordering")
    @ApiResponse(responseCode = "200", description = "Order updated")
    @ApiResponse(responseCode = "400", description = "Invalid ordering",
        content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiError.class)))
    @ApiResponse(responseCode = "401", description = "Authentication required", content = @Content)
    @ApiResponse(responseCode = "404", description = "Playlist not found",
        content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiError.class)))
    @PutMapping("/{playlistId}/order")
    PlaylistResponse reorder(@PathVariable UUID playlistId, @Valid @RequestBody ReorderPlaylistRequest request);

    @Operation(summary = "Generate a playback queue using the selected shuffle strategy")
    @ApiResponse(responseCode = "200", description = "Queue generated")
    @ApiResponse(responseCode = "401", description = "Authentication required", content = @Content)
    @ApiResponse(responseCode = "404", description = "Playlist not found",
        content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiError.class)))
    @GetMapping("/{playlistId}/queue")
    List<UUID> queue(@PathVariable UUID playlistId, @RequestParam(defaultValue = "RANDOM") ShuffleMode strategy);

    @Operation(summary = "Get catalog recommendations compatible with playlist content")
    @ApiResponse(responseCode = "200", description = "Recommendations returned")
    @ApiResponse(responseCode = "400", description = "Invalid limit parameter",
        content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiError.class)))
    @ApiResponse(responseCode = "401", description = "Authentication required", content = @Content)
    @ApiResponse(responseCode = "404", description = "Playlist not found",
        content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiError.class)))
    @GetMapping("/{playlistId}/recommendations")
    RecommendationResponse recommendations(@PathVariable UUID playlistId,
                                           @RequestParam(defaultValue = "20") @Min(1) @Max(100) int limit);

    @Operation(summary = "Export playlist in a supported format",
        description = "Supported formats: `json` (default), `m3u`. The response Content-Type reflects the chosen format.")
    @ApiResponse(responseCode = "200", description = "Export generated")
    @ApiResponse(responseCode = "400", description = "Unsupported export format",
        content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiError.class)))
    @ApiResponse(responseCode = "401", description = "Authentication required", content = @Content)
    @ApiResponse(responseCode = "404", description = "Playlist not found",
        content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiError.class)))
    @GetMapping(value = "/{playlistId}/export", produces = {"application/json", "audio/x-mpegurl"})
    ResponseEntity<Object> export(@PathVariable UUID playlistId, @RequestParam(defaultValue = "json") String format);
}
