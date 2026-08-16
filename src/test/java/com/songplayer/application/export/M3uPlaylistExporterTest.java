package com.songplayer.application.export;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.songplayer.persistence.entity.ArtistEntity;
import com.songplayer.persistence.entity.PlaylistEntity;
import com.songplayer.persistence.entity.PlaylistItemEntity;
import com.songplayer.persistence.entity.SongEntity;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class M3uPlaylistExporterTest {

    private final M3uPlaylistExporter exporter = new M3uPlaylistExporter();
    private PlaylistEntity playlist;

    @BeforeEach
    void setUp() {
        playlist = mock(PlaylistEntity.class);
        when(playlist.getName()).thenReturn("My Playlist");
    }

    @Test
    void contentTypeIsM3u() {
        when(playlist.getItems()).thenReturn(List.of());
        assertThat(exporter.export(playlist).contentType()).isEqualTo("audio/x-mpegurl;charset=UTF-8");
    }

    @Test
    void exportFormatIsM3u() {
        assertThat(exporter.format()).isEqualTo("m3u");
    }

    @Test
    void emptyPlaylistContainsOnlyHeader() {
        when(playlist.getItems()).thenReturn(List.of());
        String body = (String) exporter.export(playlist).body();

        assertThat(body).startsWith("#EXTM3U\n")
                .contains("#PLAYLIST:My Playlist")
                .doesNotContain("#EXTINF");
    }

    @Test
    void singleSongProducesExtInfAndUri() {
        ArtistEntity artist = mock(ArtistEntity.class);
        when(artist.getName()).thenReturn("Aurora Lane");

        SongEntity song = mock(SongEntity.class);
        when(song.getId()).thenReturn(UUID.randomUUID());
        when(song.getTitle()).thenReturn("Paper Skies");
        when(song.getDurationSeconds()).thenReturn(210);
        when(song.getUri()).thenReturn("https://cdn.example.com/paper-skies.mp3");
        when(song.getArtists()).thenReturn(Set.of(artist));
        when(song.getGenres()).thenReturn(Set.of());

        PlaylistItemEntity item = mock(PlaylistItemEntity.class);
        when(item.getSong()).thenReturn(song);
        when(item.getPosition()).thenReturn(0);

        when(playlist.getItems()).thenReturn(List.of(item));

        String body = (String) exporter.export(playlist).body();

        assertThat(body).contains("#EXTM3U")
                .contains("#EXTINF:210,Aurora Lane - Paper Skies")
                .contains("https://cdn.example.com/paper-skies.mp3");
    }

    @Test
    void multipleSongsProduceMultipleExtInfEntries() {
        List<PlaylistItemEntity> items = List.of(
                buildItem("Song A", "Artist A", 180, "https://cdn.example.com/a.mp3"),
                buildItem("Song B", "Artist B", 240, "https://cdn.example.com/b.mp3"),
                buildItem("Song C", "Artist C", 300, "https://cdn.example.com/c.mp3"));

        when(playlist.getItems()).thenReturn(items);
        String body = (String) exporter.export(playlist).body();

        assertThat(body)
                .contains("#EXTINF:180,Artist A - Song A")
                .contains("#EXTINF:240,Artist B - Song B")
                .contains("#EXTINF:300,Artist C - Song C")
                .contains("https://cdn.example.com/a.mp3")
                .contains("https://cdn.example.com/b.mp3")
                .contains("https://cdn.example.com/c.mp3");
    }

    @Test
    void songWithNoArtistProducesEmptyArtistField() {
        SongEntity song = mock(SongEntity.class);
        when(song.getId()).thenReturn(UUID.randomUUID());
        when(song.getTitle()).thenReturn("Instrumental");
        when(song.getDurationSeconds()).thenReturn(150);
        when(song.getUri()).thenReturn("https://cdn.example.com/inst.mp3");
        when(song.getArtists()).thenReturn(Set.of());
        when(song.getGenres()).thenReturn(Set.of());

        PlaylistItemEntity item = mock(PlaylistItemEntity.class);
        when(item.getSong()).thenReturn(song);
        when(item.getPosition()).thenReturn(0);

        when(playlist.getItems()).thenReturn(List.of(item));
        String body = (String) exporter.export(playlist).body();

        assertThat(body).contains("#EXTINF:150, - Instrumental");
    }

    // ── helpers ───────────────────────────────────────────────────────────────

    private static PlaylistItemEntity buildItem(String title, String artistName, int duration, String uri) {
        ArtistEntity artist = mock(ArtistEntity.class);
        when(artist.getName()).thenReturn(artistName);

        SongEntity song = mock(SongEntity.class);
        when(song.getId()).thenReturn(UUID.randomUUID());
        when(song.getTitle()).thenReturn(title);
        when(song.getDurationSeconds()).thenReturn(duration);
        when(song.getUri()).thenReturn(uri);
        when(song.getArtists()).thenReturn(Set.of(artist));
        when(song.getGenres()).thenReturn(Set.of());

        PlaylistItemEntity item = mock(PlaylistItemEntity.class);
        when(item.getSong()).thenReturn(song);
        return item;
    }
}
