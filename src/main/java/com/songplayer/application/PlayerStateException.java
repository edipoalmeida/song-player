package com.songplayer.application;

/** Thrown when a player operation is incompatible with the current playback state. */
public class PlayerStateException extends RuntimeException {
    public PlayerStateException(String message) {
        super(message);
    }
}
