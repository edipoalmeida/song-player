package com.songplayer.api.dto;

import com.songplayer.domain.PlaybackStatus;
import com.songplayer.domain.ShuffleMode;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;
import java.util.UUID;

/** Current global player state returned by player endpoints. */
@Schema(description = "Current state of the global player")
public record PlaybackStateResponse(
        @Schema(description = "Current playback status (STOPPED, PLAYING, PAUSED)") PlaybackStatus status,
        @Schema(description = "ID of the currently loaded playlist, or null if no playlist is loaded") UUID playlistId,
        @Schema(description = "ID of the song currently playing, or null if stopped") UUID currentSongId,
        @Schema(description = "Current playback position in the active song, in seconds", example = "30") int positionSeconds,
        @Schema(description = "Active shuffle strategy") ShuffleMode shuffleMode,
        @Schema(description = "Timestamp of the last state change") Instant updatedAt
) {
}
