package com.songplayer.application.export;

import com.songplayer.persistence.entity.ArtistEntity;
import com.songplayer.persistence.entity.GenreEntity;
import com.songplayer.persistence.entity.PlaylistEntity;
import com.songplayer.persistence.entity.PlaylistItemEntity;
import com.songplayer.persistence.entity.SongEntity;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;

import static com.songplayer.application.export.ExportResult.CSV_EXPORT_TYPE;

/**
 * Exports a playlist as RFC 4180-compliant CSV.
 *
 * <p>Columns: {@code position,title,artists,genres,duration_seconds,uri}
 * <ul>
 *   <li>Artists are sorted alphabetically and joined with {@code |}.
 *   <li>Genres are sorted alphabetically and joined with {@code |}.
 *   <li>Fields that contain commas or double-quotes are quoted per RFC 4180
 *       (double-quotes inside values are escaped as {@code ""}).
 * </ul>
 */
@Component
public class CsvPlaylistExporter implements PlaylistExporter {

    private static final String CSV_CONTENT_TYPE = "text/csv;charset=UTF-8";
    private static final String HEADER = "position,title,artists,genres,duration_seconds,uri";

    @Override
    public String format() { return CSV_EXPORT_TYPE; }

    @Override
    public ExportResult export(PlaylistEntity playlist) {
        StringBuilder sb = new StringBuilder(HEADER).append("\n");

        for (PlaylistItemEntity item : playlist.getItems()) {
            SongEntity song = item.getSong();

            String artists = song.getArtists().stream()
                    .map(ArtistEntity::getName)
                    .sorted()
                    .collect(Collectors.joining("|"));

            String genres = song.getGenres().stream()
                    .map(GenreEntity::getName)
                    .sorted()
                    .collect(Collectors.joining("|"));

            sb.append(item.getPosition()).append(",")
              .append(csvField(song.getTitle())).append(",")
              .append(csvField(artists)).append(",")
              .append(csvField(genres)).append(",")
              .append(song.getDurationSeconds()).append(",")
              .append(csvField(song.getUri())).append("\n");
        }

        return new ExportResult(CSV_CONTENT_TYPE, sb.toString());
    }

    /** Wraps a field in double-quotes if it contains a comma or double-quote; escapes inner quotes. */
    private static String csvField(String value) {
        if (value == null || value.isEmpty()) return "";
        boolean needsQuoting = value.contains(",") || value.contains("\"") || value.contains("\n");
        if (!needsQuoting) return value;
        return "\"" + value.replace("\"", "\"\"") + "\"";
    }
}
