package com.songplayer.application;

/** Thrown when an export format is requested that has no registered exporter. */
public class UnsupportedExportFormatException extends RuntimeException {
    public UnsupportedExportFormatException(String format, Iterable<String> supported) {
        super("Unsupported export format '" + format + "'. Supported: " + supported);
    }
}
