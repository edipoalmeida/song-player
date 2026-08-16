package com.songplayer.api;

import com.songplayer.api.dto.PlaybackStateResponse;
import com.songplayer.api.error.ApiError;
import com.songplayer.domain.ShuffleMode;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import org.springframework.validation.annotation.Validated;
import java.util.UUID;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

/** Contract for the one application-wide player. */
@Tag(name = "Player", description = "Centralized playback state and controls")
@RequestMapping("/api/v1/player")
@Validated
public interface PlayerApi {

    @Operation(summary = "Read global player state")
    @ApiResponse(responseCode = "200", description = "Current player state")
    @ApiResponse(responseCode = "401", description = "Authentication required", content = @Content)
    @GetMapping
    PlaybackStateResponse state();

    @Operation(summary = "Load and start a playlist in the global player")
    @ApiResponse(responseCode = "200", description = "Playback started")
    @ApiResponse(responseCode = "401", description = "Authentication required", content = @Content)
    @ApiResponse(responseCode = "404", description = "Playlist not found",
        content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiError.class)))
    @PostMapping("/playlists/{playlistId}/play")
    PlaybackStateResponse playPlaylist(@PathVariable UUID playlistId,
                                       @RequestParam(defaultValue = "RANDOM") ShuffleMode strategy);

    @Operation(summary = "Resume playback")
    @ApiResponse(responseCode = "200", description = "Playback resumed")
    @ApiResponse(responseCode = "401", description = "Authentication required", content = @Content)
    @ApiResponse(responseCode = "409", description = "Cannot resume — invalid player state",
        content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiError.class)))
    @PostMapping("/play")
    PlaybackStateResponse play();

    @Operation(summary = "Pause playback")
    @ApiResponse(responseCode = "200", description = "Playback paused")
    @ApiResponse(responseCode = "401", description = "Authentication required", content = @Content)
    @ApiResponse(responseCode = "409", description = "Cannot pause — invalid player state",
        content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiError.class)))
    @PostMapping("/pause")
    PlaybackStateResponse pause();

    @Operation(summary = "Stop playback and clear the active song")
    @ApiResponse(responseCode = "200", description = "Playback stopped")
    @ApiResponse(responseCode = "401", description = "Authentication required", content = @Content)
    @ApiResponse(responseCode = "409", description = "Cannot stop — invalid player state",
        content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiError.class)))
    @PostMapping("/stop")
    PlaybackStateResponse stop();

    @Operation(summary = "Advance to the next song")
    @ApiResponse(responseCode = "200", description = "Advanced to next song")
    @ApiResponse(responseCode = "401", description = "Authentication required", content = @Content)
    @ApiResponse(responseCode = "409", description = "No active playlist loaded",
        content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiError.class)))
    @PostMapping("/next")
    PlaybackStateResponse next();

    @Operation(summary = "Return to the previous song")
    @ApiResponse(responseCode = "200", description = "Returned to previous song")
    @ApiResponse(responseCode = "401", description = "Authentication required", content = @Content)
    @ApiResponse(responseCode = "409", description = "No active playlist loaded",
        content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiError.class)))
    @PostMapping("/previous")
    PlaybackStateResponse previous();

    @Operation(summary = "Seek in the current song",
        description = "Sets the playback position in the currently active song.")
    @ApiResponse(responseCode = "200", description = "Position updated")
    @ApiResponse(responseCode = "400", description = "Invalid position value",
        content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiError.class)))
    @ApiResponse(responseCode = "401", description = "Authentication required", content = @Content)
    @ApiResponse(responseCode = "409", description = "No song is currently playing",
        content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiError.class)))
    @PostMapping("/seek")
    PlaybackStateResponse seek(@RequestParam @NotNull @PositiveOrZero Integer positionSeconds);
}