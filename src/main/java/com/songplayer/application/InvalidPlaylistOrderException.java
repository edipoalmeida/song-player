package com.songplayer.application;

/** Thrown when a reorder request is duplicated, incomplete, or non-contiguous. */
public class InvalidPlaylistOrderException extends RuntimeException {
    public InvalidPlaylistOrderException(String message) {
        super(message);
    }
}
