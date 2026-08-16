package com.songplayer.domain;

import java.util.UUID;

/** A song in a playlist. Position is zero-based and unique within its playlist. */
public record PlaylistItem(UUID id, Song song, int position) {
}
