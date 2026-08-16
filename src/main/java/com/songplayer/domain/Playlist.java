package com.songplayer.domain;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/** Immutable snapshot of a playlist. */
public record Playlist(
        UUID id,
        String name,
        String description,
        String coverImageUrl,
        List<PlaylistItem> items,
        Instant createdAt,
        Instant updatedAt
) {
}
