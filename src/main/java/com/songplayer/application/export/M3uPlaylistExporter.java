package com.songplayer.application.export;

import com.songplayer.persistence.entity.ArtistEntity;
import com.songplayer.persistence.entity.PlaylistEntity;
import com.songplayer.persistence.entity.SongEntity;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;

import static com.songplayer.application.export.ExportResult.M3U_EXPORT_TYPE;

/** Exports a playlist as an M3U extended playlist file. */
@Component
public class M3uPlaylistExporter implements PlaylistExporter {
    private static final String M3U_CONTENT_TYPE = "audio/x-mpegurl;charset=UTF-8";

    @Override
    public String format() { return M3U_EXPORT_TYPE; }

    @Override
    public ExportResult export(PlaylistEntity playlist) {
        String entries = playlist.getItems().stream()
                .map(item -> {
                    SongEntity song = item.getSong();
                    String artistNames = song.getArtists().stream()
                            .map(ArtistEntity::getName)
                            .sorted()
                            .collect(Collectors.joining(", "));
                    return "#EXTINF:%d,%s - %s%n%s".formatted(
                            song.getDurationSeconds(),
                            artistNames,
                            song.getTitle(),
                            song.getUri());
                })
                .collect(Collectors.joining("\n"));

        String content = "#EXTM3U%n#PLAYLIST:%s%n%n%s%n".formatted(playlist.getName(), entries);
        return new ExportResult(M3U_CONTENT_TYPE, content);
    }
}
