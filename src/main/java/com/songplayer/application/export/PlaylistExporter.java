package com.songplayer.application.export;

import com.songplayer.persistence.entity.PlaylistEntity;

/** Strategy for serialising a playlist into a specific wire format. */
public interface PlaylistExporter {
    /** Lower-case format key, e.g. {@code "json"} or {@code "m3u"}. */
    String format();

    ExportResult export(PlaylistEntity playlist);
}
