package com.songplayer.application.export;

import com.songplayer.application.mapper.PlaylistMapper;
import com.songplayer.persistence.entity.PlaylistEntity;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;

import static com.songplayer.application.export.ExportResult.JSON_EXPORT_TYPE;

/** Exports a playlist as the API's JSON response shape. */
@Component
public class JsonPlaylistExporter implements PlaylistExporter {
    private final PlaylistMapper playlistMapper;

    public JsonPlaylistExporter(PlaylistMapper playlistMapper) {
        this.playlistMapper = playlistMapper;
    }

    @Override
    public String format() { return JSON_EXPORT_TYPE; }

    @Override
    public ExportResult export(PlaylistEntity playlist) {
        return new ExportResult(MediaType.APPLICATION_JSON_VALUE, playlistMapper.toResponse(playlist));
    }
}
