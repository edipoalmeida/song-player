package com.songplayer.domain;

/** Possible states of the application-wide music player. */
public enum PlaybackStatus {
    STOPPED,
    PLAYING,
    /** Playback at 2× speed: every real second counts as 2 song-seconds. */
    PLAYING_2X,
    PAUSED;

    /** Returns {@code true} for any playing variant (1× or 2×). */
    public boolean isActive() {
        return this == PLAYING || this == PLAYING_2X;
    }
}
