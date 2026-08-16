package com.songplayer.domain;

import java.time.Instant;
import java.util.UUID;

/** Snapshot of the singleton player state. */
public record PlaybackState(
        PlaybackStatus status,
        UUID playlistId,
        UUID currentSongId,
        int positionSeconds,
        ShuffleMode shuffleMode,
        Instant updatedAt
) {
}
