package com.songplayer.application.export;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.songplayer.persistence.entity.ArtistEntity;
import com.songplayer.persistence.entity.GenreEntity;
import com.songplayer.persistence.entity.PlaylistEntity;
import com.songplayer.persistence.entity.PlaylistItemEntity;
import com.songplayer.persistence.entity.SongEntity;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class CsvPlaylistExporterTest {

    private final CsvPlaylistExporter exporter = new CsvPlaylistExporter();
    private PlaylistEntity playlist;

    @BeforeEach
    void setUp() {
        playlist = mock(PlaylistEntity.class);
        when(playlist.getName()).thenReturn("Road Trip");
    }

    @Test
    void formatIsCsv() {
        assertThat(exporter.format()).isEqualTo("csv");
    }

    @Test
    void contentTypeIsTextCsv() {
        when(playlist.getItems()).thenReturn(List.of());
        assertThat(exporter.export(playlist).contentType()).isEqualTo("text/csv;charset=UTF-8");
    }

    @Test
    void emptyPlaylistContainsOnlyHeader() {
        when(playlist.getItems()).thenReturn(List.of());
        String body = (String) exporter.export(playlist).body();

        List<String> lines = lines(body);
        assertThat(lines).hasSize(1);
        assertThat(lines.get(0)).isEqualTo("position,title,artists,genres,duration_seconds,uri");
    }

    @Test
    void singleSongProducesOneDataRow() {
        var item = buildItem(0, "Paper Skies", Set.of("Aurora Lane"), Set.of("Pop", "Indie"), 214,
                "catalog://songs/paper-skies");
        when(playlist.getItems()).thenReturn(List.of(item));

        List<String> lines = lines((String) exporter.export(playlist).body());

        assertThat(lines).hasSize(2);
        assertThat(lines.get(0)).isEqualTo("position,title,artists,genres,duration_seconds,uri");
        assertThat(lines.get(1)).isEqualTo("0,Paper Skies,Aurora Lane,Indie|Pop,214,catalog://songs/paper-skies");
    }

    @Test
    void multipleArtistsArePipeSeparated() {
        var item = buildItem(0, "Collab Song", Set.of("Artist A", "Artist B"), Set.of("Jazz"), 180,
                "catalog://songs/collab");
        when(playlist.getItems()).thenReturn(List.of(item));

        String row = lines((String) exporter.export(playlist).body()).get(1);
        // artists sorted and pipe-separated; pipe is not a CSV special char — no quoting
        assertThat(row).contains("Artist A|Artist B");
    }

    @Test
    void titleWithCommaIsQuoted() {
        var item = buildItem(0, "Hello, World", Set.of("DJ"), Set.of("Electronic"), 120,
                "catalog://songs/hello-world");
        when(playlist.getItems()).thenReturn(List.of(item));

        String row = lines((String) exporter.export(playlist).body()).get(1);
        assertThat(row).startsWith("0,\"Hello, World\"");
    }

    @Test
    void titleWithDoubleQuoteIsEscaped() {
        var item = buildItem(0, "Say \"Something\"", Set.of("Artist"), Set.of("Pop"), 200,
                "catalog://songs/say-something");
        when(playlist.getItems()).thenReturn(List.of(item));

        String row = lines((String) exporter.export(playlist).body()).get(1);
        assertThat(row).contains("\"Say \"\"Something\"\"\"");
    }

    @Test
    void multipleItemsProduceRowsInOrder() {
        var item0 = buildItem(0, "First Song",  Set.of("A"), Set.of("Pop"),   180, "catalog://a");
        var item1 = buildItem(1, "Second Song", Set.of("B"), Set.of("Jazz"),  240, "catalog://b");
        var item2 = buildItem(2, "Third Song",  Set.of("C"), Set.of("Indie"), 210, "catalog://c");
        when(playlist.getItems()).thenReturn(List.of(item0, item1, item2));

        List<String> lines = lines((String) exporter.export(playlist).body());

        assertThat(lines).hasSize(4);
        assertThat(lines.get(1)).startsWith("0,First Song");
        assertThat(lines.get(2)).startsWith("1,Second Song");
        assertThat(lines.get(3)).startsWith("2,Third Song");
    }

    @Test
    void songWithNoArtistHasEmptyArtistsField() {
        var item = buildItem(0, "Instrumental", Set.of(), Set.of("Classical"), 300,
                "catalog://songs/instrumental");
        when(playlist.getItems()).thenReturn(List.of(item));

        String row = lines((String) exporter.export(playlist).body()).get(1);
        assertThat(row).startsWith("0,Instrumental,,");
    }

    // ── helpers ───────────────────────────────────────────────────────────────

    private static List<String> lines(String body) {
        return Arrays.stream(body.split("\n", -1)).filter(l -> !l.isEmpty()).toList();
    }

    private static PlaylistItemEntity buildItem(int position, String title,
            Set<String> artistNames, Set<String> genreNames,
            int duration, String uri) {

        // Create mocks eagerly (not inside stream) to avoid confusing Mockito's internal state
        // when HashSet.add() calls hashCode() mid-stubbing
        Set<ArtistEntity> artists = new java.util.LinkedHashSet<>();
        for (String name : artistNames) {
            ArtistEntity a = mock(ArtistEntity.class);
            when(a.getName()).thenReturn(name);
            artists.add(a);
        }

        Set<GenreEntity> genres = new java.util.LinkedHashSet<>();
        for (String name : genreNames) {
            GenreEntity g = mock(GenreEntity.class);
            when(g.getName()).thenReturn(name);
            genres.add(g);
        }

        SongEntity song = mock(SongEntity.class);
        when(song.getId()).thenReturn(UUID.randomUUID());
        when(song.getTitle()).thenReturn(title);
        when(song.getDurationSeconds()).thenReturn(duration);
        when(song.getUri()).thenReturn(uri);
        when(song.getArtists()).thenReturn(artists);
        when(song.getGenres()).thenReturn(genres);

        PlaylistItemEntity item = mock(PlaylistItemEntity.class);
        when(item.getSong()).thenReturn(song);
        when(item.getPosition()).thenReturn(position);
        return item;
    }
}
