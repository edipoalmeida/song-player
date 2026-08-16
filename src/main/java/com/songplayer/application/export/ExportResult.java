package com.songplayer.application.export;

/** Export payload plus its response media type. */
public record ExportResult(String contentType, Object body) {
    public static final String JSON_EXPORT_TYPE = "json";
    public static final String M3U_EXPORT_TYPE = "m3u";
    public static final String CSV_EXPORT_TYPE = "csv";

}
