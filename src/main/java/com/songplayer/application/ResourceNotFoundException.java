package com.songplayer.application;

import java.util.UUID;

/** Thrown when a requested resource ID does not exist. */
public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String resource, UUID id) {
        super(resource + " with id " + id + " was not found");
    }
}
