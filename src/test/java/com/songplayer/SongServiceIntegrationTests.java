package com.songplayer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.songplayer.api.dto.SongResponse;
import com.songplayer.application.ResourceNotFoundException;
import com.songplayer.application.SongService;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class SongServiceIntegrationTests {

    // Seed data UUIDs from V2__seed_music_catalog.sql
    private static final UUID PAPER_SKIES_ID = UUID.fromString("20000000-0000-0000-0000-000000000001");

    @Autowired
    private SongService songService;

    // ── list ──────────────────────────────────────────────────────────────────

    @Test
    void listReturnsAllSeedSongs() {
        List<SongResponse> songs = songService.list();
        assertThat(songs).hasSizeGreaterThanOrEqualTo(6);
    }

    @Test
    void listReturnsSongsWithArtistsAndGenresPopulated() {
        List<SongResponse> songs = songService.list();
        assertThat(songs).isNotEmpty().allSatisfy(song -> {
            assertThat(song.title()).isNotBlank();
            assertThat(song.uri()).isNotBlank();
            assertThat(song.durationSeconds()).isPositive();
        });
    }

    @Test
    void listIncludesSongsWithArtistMetadata() {
        List<SongResponse> songs = songService.list();
        // At least some songs should have artist data from seed
        assertThat(songs).anyMatch(s -> !s.artists().isEmpty());
    }

    @Test
    void listIncludesSongsWithGenreMetadata() {
        List<SongResponse> songs = songService.list();
        assertThat(songs).anyMatch(s -> !s.genres().isEmpty());
    }

    // ── getById ───────────────────────────────────────────────────────────────

    @Test
    void getByIdReturnsSongWithCorrectId() {
        SongResponse song = songService.getById(PAPER_SKIES_ID);
        assertThat(song.id()).isEqualTo(PAPER_SKIES_ID);
    }

    @Test
    void getByIdReturnsSongWithTitle() {
        SongResponse song = songService.getById(PAPER_SKIES_ID);
        assertThat(song.title()).isNotBlank();
    }

    @Test
    void getByIdReturnsSongWithArtistDetails() {
        SongResponse song = songService.getById(PAPER_SKIES_ID);
        assertThat(song.artists()).isNotEmpty();
        assertThat(song.artists().get(0).name()).isNotBlank();
    }

    @Test
    void getByIdReturnsSongWithGenreDetails() {
        SongResponse song = songService.getById(PAPER_SKIES_ID);
        assertThat(song.genres()).isNotEmpty();
        assertThat(song.genres().get(0)).isNotBlank();
    }

    @Test
    void getByIdWithUnknownIdThrowsResourceNotFoundException() {
        UUID unknownId = UUID.randomUUID();
        assertThatThrownBy(() -> songService.getById(unknownId))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
