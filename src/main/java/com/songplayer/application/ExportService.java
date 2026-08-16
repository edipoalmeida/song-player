package com.songplayer.application;

import com.songplayer.application.export.ExportResult;
import com.songplayer.application.export.PlaylistExporter;
import com.songplayer.persistence.entity.PlaylistEntity;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

/** Delegates playlist export to the {@link PlaylistExporter} registered for the requested format. */
@Service
public class ExportService {
    private final Map<String, PlaylistExporter> exporters;

    public ExportService(List<PlaylistExporter> exporterList) {
        this.exporters = exporterList.stream()
                .collect(Collectors.toMap(PlaylistExporter::format, e -> e));
    }

    public ExportResult export(PlaylistEntity playlist, String format) {
        PlaylistExporter exporter = exporters.get(format.toLowerCase(Locale.ROOT));
        if (exporter == null) {
            throw new UnsupportedExportFormatException(format, new TreeMap<>(exporters).keySet());
        }
        return exporter.export(playlist);
    }
}
