package com.songplayer.domain;

import java.util.List;
import java.util.UUID;

/** Catalog song used by playlists, recommendations and playback. */
public record Song(
        UUID id,
        String title,
        List<Artist> artists,
        List<Genre> genres,
        int durationSeconds,
        String uri,
        String albumTitle
) {
}
