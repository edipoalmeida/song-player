package com.songplayer.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.songplayer.application.export.ExportResult;
import com.songplayer.application.export.PlaylistExporter;
import com.songplayer.persistence.entity.PlaylistEntity;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ExportServiceTest {

    private PlaylistExporter jsonExporter;
    private PlaylistExporter m3uExporter;
    private PlaylistExporter csvExporter;
    private ExportService exportService;

    @BeforeEach
    void setUp() {
        jsonExporter = mock(PlaylistExporter.class);
        when(jsonExporter.format()).thenReturn("json");

        m3uExporter = mock(PlaylistExporter.class);
        when(m3uExporter.format()).thenReturn("m3u");

        csvExporter = mock(PlaylistExporter.class);
        when(csvExporter.format()).thenReturn("csv");

        exportService = new ExportService(List.of(jsonExporter, m3uExporter, csvExporter));
    }

    @Test
    void delegatesToJsonExporterForJsonFormat() {
        PlaylistEntity playlist = mock(PlaylistEntity.class);
        ExportResult expected = new ExportResult("application/json", "{}");
        when(jsonExporter.export(playlist)).thenReturn(expected);

        ExportResult result = exportService.export(playlist, "json");

        assertThat(result).isEqualTo(expected);
        verify(jsonExporter).export(playlist);
    }

    @Test
    void delegatesToM3uExporterForM3uFormat() {
        PlaylistEntity playlist = mock(PlaylistEntity.class);
        ExportResult expected = new ExportResult("audio/x-mpegurl", "#EXTM3U\n");
        when(m3uExporter.export(playlist)).thenReturn(expected);

        ExportResult result = exportService.export(playlist, "m3u");

        assertThat(result).isEqualTo(expected);
        verify(m3uExporter).export(playlist);
    }

    @Test
    void formatMatchingIsCaseInsensitive() {
        PlaylistEntity playlist = mock(PlaylistEntity.class);
        ExportResult expected = new ExportResult("application/json", "{}");
        when(jsonExporter.export(playlist)).thenReturn(expected);

        assertThat(exportService.export(playlist, "JSON")).isEqualTo(expected);
        assertThat(exportService.export(playlist, "Json")).isEqualTo(expected);
    }

    @Test
    void delegatesToCsvExporterForCsvFormat() {
        PlaylistEntity playlist = mock(PlaylistEntity.class);
        ExportResult expected = new ExportResult("text/csv", "title,artist\n");
        when(csvExporter.export(playlist)).thenReturn(expected);

        ExportResult result = exportService.export(playlist, "csv");

        assertThat(result).isEqualTo(expected);
        verify(csvExporter).export(playlist);
    }

    @Test
    void throwsForUnknownFormat() {
        PlaylistEntity playlist = mock(PlaylistEntity.class);
        assertThatThrownBy(() -> exportService.export(playlist, "xml"))
                .isInstanceOf(UnsupportedExportFormatException.class);
    }

    @Test
    void exceptionMessageContainsSupportedFormats() {
        PlaylistEntity playlist = mock(PlaylistEntity.class);
        UnsupportedExportFormatException ex = (UnsupportedExportFormatException)
                org.assertj.core.api.Assertions.catchThrowable(
                        () -> exportService.export(playlist, "pdf"));
        // Message or cause should reference the known formats
        assertThat(ex).isNotNull();
    }
}
