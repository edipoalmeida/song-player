package com.songplayer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.songplayer.api.dto.AddSongRequest;
import com.songplayer.api.dto.CreatePlaylistRequest;
import com.songplayer.api.dto.PlaylistResponse;
import com.songplayer.api.dto.RecommendationResponse;
import com.songplayer.api.dto.ReorderPlaylistRequest;
import com.songplayer.api.dto.UpdatePlaylistRequest;
import com.songplayer.application.PlayerService;
import com.songplayer.domain.PlaybackStatus;
import com.songplayer.application.PlaylistService;
import com.songplayer.application.ResourceNotFoundException;
import com.songplayer.application.UnsupportedExportFormatException;
import com.songplayer.application.export.ExportResult;
import com.songplayer.domain.ShuffleMode;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class PlaylistServiceIntegrationTests {

    // Seed data UUIDs from V2__seed_music_catalog.sql
    private static final UUID SONG_1 = UUID.fromString("20000000-0000-0000-0000-000000000001"); // Paper Skies – Pop, Indie
    private static final UUID SONG_2 = UUID.fromString("20000000-0000-0000-0000-000000000002"); // Neon Circuit – Electronic, Pop
    private static final UUID SONG_3 = UUID.fromString("20000000-0000-0000-0000-000000000003"); // Velvet Underground – Jazz, Indie

    @Autowired
    private PlaylistService playlistService;

    @Autowired
    private PlayerService playerService;

    private UUID playlistId;

    @BeforeEach
    void createPlaylist() {
        playlistId = playlistService.create(new CreatePlaylistRequest("Test Playlist", "desc", null)).id();
    }

    @AfterEach
    void deletePlaylist() {
        try { playlistService.delete(playlistId); } catch (Exception ignored) { /* playlist may have been deleted by the test */ }
    }

    // ── CRUD ──────────────────────────────────────────────────────────────────

    @Test
    void createReturnsPlaylistWithCorrectMetadata() {
        PlaylistResponse response = playlistService.getById(playlistId);
        assertThat(response.name()).isEqualTo("Test Playlist");
        assertThat(response.description()).isEqualTo("desc");
        assertThat(response.items()).isEmpty();
    }

    @Test
    void listIncludesCreatedPlaylist() {
        assertThat(playlistService.list())
                .anyMatch(p -> p.id().equals(playlistId));
    }

    @Test
    void updateMetadataChangesNameAndDescription() {
        PlaylistResponse updated = playlistService.update(playlistId,
                new UpdatePlaylistRequest("Renamed", "new desc", null));
        assertThat(updated.name()).isEqualTo("Renamed");
        assertThat(updated.description()).isEqualTo("new desc");
    }

    @Test
    void deleteRemovesPlaylist() {
        playlistService.delete(playlistId);
        assertThatThrownBy(() -> playlistService.getById(playlistId))
                .isInstanceOf(ResourceNotFoundException.class);
        // skip @AfterEach deletion since already deleted
        playlistId = UUID.randomUUID(); // prevent double-delete
    }

    @Test
    void deleteActivePlaylistStopsPlayer() {
        playerService.playPlaylist(playlistId, ShuffleMode.RANDOM);
        assertThat(playerService.state().playlistId()).isEqualTo(playlistId);

        playlistService.delete(playlistId);

        assertThat(playerService.state().status()).isEqualTo(PlaybackStatus.STOPPED);
        assertThat(playerService.state().playlistId()).isNull();
        playlistId = UUID.randomUUID(); // prevent double-delete in @AfterEach
    }

    @Test
    void deleteNonActivePlaylistDoesNotStopPlayer() {
        UUID otherPlaylistId = playlistService.create(new CreatePlaylistRequest("Other", null, null)).id();
        try {
            playlistService.addSong(playlistId, new AddSongRequest(SONG_1));
            playerService.playPlaylist(playlistId, ShuffleMode.RANDOM);

            playlistService.delete(otherPlaylistId);

            assertThat(playerService.state().playlistId()).isEqualTo(playlistId);
            assertThat(playerService.state().status()).isEqualTo(PlaybackStatus.PLAYING);
        } finally {
            playerService.stop();
            try { playlistService.delete(otherPlaylistId); } catch (Exception ignored) { /* playlist may have been deleted by the test */ }
        }
    }

    @Test
    void getByIdWithUnknownIdThrowsResourceNotFoundException() {
        UUID unknownId = UUID.randomUUID();
        assertThatThrownBy(() -> playlistService.getById(unknownId))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    // ── add / remove songs ────────────────────────────────────────────────────

    @Test
    void addSongAppendsToPlaylist() {
        PlaylistResponse response = playlistService.addSong(playlistId, new AddSongRequest(SONG_1));
        assertThat(response.items()).hasSize(1);
        assertThat(response.items().get(0).song().id()).isEqualTo(SONG_1);
        assertThat(response.items().get(0).position()).isZero();
    }

    @Test
    void addSongPopulatesArtistAndGenreMetadata() {
        PlaylistResponse response = playlistService.addSong(playlistId, new AddSongRequest(SONG_1));
        var item = response.items().get(0);
        assertThat(item.song().title()).isNotBlank();
        assertThat(item.song().artists()).isNotEmpty();
        assertThat(item.song().genres()).isNotEmpty();
    }

    @Test
    void addSongWithUnknownSongIdThrowsResourceNotFoundException() {
        UUID unknownSongId = UUID.randomUUID();
        AddSongRequest request = new AddSongRequest(unknownSongId);
        assertThatThrownBy(() -> playlistService.addSong(playlistId, request))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void removeSongDecreasesItemCount() {
        PlaylistResponse with1 = playlistService.addSong(playlistId, new AddSongRequest(SONG_1));
        playlistService.addSong(playlistId, new AddSongRequest(SONG_2));

        UUID itemId = with1.items().get(0).id();
        playlistService.removeSong(playlistId, itemId);

        PlaylistResponse after = playlistService.getById(playlistId);
        assertThat(after.items()).hasSize(1);
        assertThat(after.items().get(0).song().id()).isEqualTo(SONG_2);
    }

    @Test
    void removeSongReindexesRemainingPositions() {
        playlistService.addSong(playlistId, new AddSongRequest(SONG_1));
        PlaylistResponse with2 = playlistService.addSong(playlistId, new AddSongRequest(SONG_2));
        playlistService.addSong(playlistId, new AddSongRequest(SONG_3));

        // remove middle item (position 1)
        UUID middleItemId = with2.items().get(1).id();
        playlistService.removeSong(playlistId, middleItemId);

        PlaylistResponse after = playlistService.getById(playlistId);
        assertThat(after.items()).hasSize(2);
        assertThat(after.items().get(0).position()).isZero();
        assertThat(after.items().get(1).position()).isOne();
    }

    @Test
    void removeSongWithUnknownItemIdThrowsResourceNotFoundException() {
        UUID unknownItemId = UUID.randomUUID();
        assertThatThrownBy(() -> playlistService.removeSong(playlistId, unknownItemId))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    // ── reorder ───────────────────────────────────────────────────────────────

    @Test
    void reorderSwapsItemPositions() {
        PlaylistResponse withFirstSong = playlistService.addSong(playlistId, new AddSongRequest(SONG_1));
        PlaylistResponse withTwoSongs = playlistService.addSong(playlistId, new AddSongRequest(SONG_2));

        PlaylistResponse reordered = playlistService.reorder(playlistId, new ReorderPlaylistRequest(List.of(
                new ReorderPlaylistRequest.ItemPosition(withFirstSong.items().get(0).id(), 1),
                new ReorderPlaylistRequest.ItemPosition(withTwoSongs.items().get(1).id(), 0)
        )));

        assertThat(reordered.items().get(0).song().id()).isEqualTo(SONG_2);
        assertThat(reordered.items().get(0).position()).isZero();
        assertThat(reordered.items().get(1).song().id()).isEqualTo(SONG_1);
        assertThat(reordered.items().get(1).position()).isOne();
    }

    // ── queue / shuffle ───────────────────────────────────────────────────────

    @Test
    void queueWithRandomStrategyReturnsAllSongIds() {
        playlistService.addSong(playlistId, new AddSongRequest(SONG_1));
        playlistService.addSong(playlistId, new AddSongRequest(SONG_2));
        playlistService.addSong(playlistId, new AddSongRequest(SONG_3));

        List<UUID> queue = playlistService.queue(playlistId, ShuffleMode.RANDOM);

        assertThat(queue).containsExactlyInAnyOrder(SONG_1, SONG_2, SONG_3);
    }

    @Test
    void queueWithSmartStrategyReturnsAllSongIds() {
        playlistService.addSong(playlistId, new AddSongRequest(SONG_1));
        playlistService.addSong(playlistId, new AddSongRequest(SONG_2));
        playlistService.addSong(playlistId, new AddSongRequest(SONG_3));

        List<UUID> queue = playlistService.queue(playlistId, ShuffleMode.SMART);

        assertThat(queue).containsExactlyInAnyOrder(SONG_1, SONG_2, SONG_3);
    }

    @Test
    void queueWithGenreBalancedStrategyReturnsAllSongIds() {
        playlistService.addSong(playlistId, new AddSongRequest(SONG_1));
        playlistService.addSong(playlistId, new AddSongRequest(SONG_2));
        playlistService.addSong(playlistId, new AddSongRequest(SONG_3));

        List<UUID> queue = playlistService.queue(playlistId, ShuffleMode.GENRE_BALANCED);

        assertThat(queue).containsExactlyInAnyOrder(SONG_1, SONG_2, SONG_3);
    }

    @Test
    void queueOnEmptyPlaylistReturnsEmptyList() {
        assertThat(playlistService.queue(playlistId, ShuffleMode.RANDOM)).isEmpty();
    }

    // ── recommendations ───────────────────────────────────────────────────────

    @Test
    void recommendationsReturnsSongsNotInPlaylist() {
        // Add only SONG_1 (Pop, Indie) — everything else should be recommendable
        playlistService.addSong(playlistId, new AddSongRequest(SONG_1));

        RecommendationResponse response = playlistService.recommendations(playlistId, 10);

        assertThat(response.songs()).isNotEmpty();
        assertThat(response.songs()).noneMatch(s -> s.id().equals(SONG_1));
    }

    @Test
    void recommendationsAreGenreCompatible() {
        // SONG_1 is Pop + Indie — recommendations should have Pop or Indie overlap
        playlistService.addSong(playlistId, new AddSongRequest(SONG_1));
        RecommendationResponse response = playlistService.recommendations(playlistId, 5);

        assertThat(response.songs()).isNotEmpty();
        // All recommended songs must share at least one genre with playlist (Pop or Indie)
        response.songs().forEach(song ->
                assertThat(song.genres()).anyMatch(g ->
                        g.equalsIgnoreCase("Pop") || g.equalsIgnoreCase("Indie")));
    }

    @Test
    void recommendationsOnEmptyPlaylistReturnsEmpty() {
        assertThat(playlistService.recommendations(playlistId, 10).songs()).isEmpty();
    }

    @Test
    void recommendationsRespectsLimit() {
        playlistService.addSong(playlistId, new AddSongRequest(SONG_1));
        RecommendationResponse response = playlistService.recommendations(playlistId, 2);
        assertThat(response.songs()).hasSizeLessThanOrEqualTo(2);
    }

    // ── export ────────────────────────────────────────────────────────────────

    @ParameterizedTest
    @CsvSource({
        "json, application/json",
        "m3u, audio/x-mpegurl;charset=UTF-8"
    })
    void exportReturnsCorrectContentType(String format, String expectedContentType) {
        playlistService.addSong(playlistId, new AddSongRequest(SONG_1));
        ExportResult result = playlistService.export(playlistId, format);
        assertThat(result.contentType()).isEqualTo(expectedContentType);
    }

    @Test
    void exportJsonBodyIsPlaylistResponse() {
        playlistService.addSong(playlistId, new AddSongRequest(SONG_1));
        ExportResult result = playlistService.export(playlistId, "json");
        assertThat(result.body()).isInstanceOf(com.songplayer.api.dto.PlaylistResponse.class);
        PlaylistResponse body = (PlaylistResponse) result.body();
        assertThat(body.items()).hasSize(1);
        assertThat(body.items().get(0).song().id()).isEqualTo(SONG_1);
    }

    @Test
    void exportM3uBodyContainsExtM3uHeader() {
        playlistService.addSong(playlistId, new AddSongRequest(SONG_1));
        ExportResult result = playlistService.export(playlistId, "m3u");
        assertThat((String) result.body()).startsWith("#EXTM3U");
    }

    @Test
    void exportM3uBodyContainsExtInfForEachSong() {
        playlistService.addSong(playlistId, new AddSongRequest(SONG_1));
        playlistService.addSong(playlistId, new AddSongRequest(SONG_2));
        String body = (String) playlistService.export(playlistId, "m3u").body();
        long extInfCount = body.lines().filter(l -> l.startsWith("#EXTINF")).count();
        assertThat(extInfCount).isEqualTo(2);
    }

    @Test
    void exportWithUnknownFormatThrowsUnsupportedExportFormatException() {
        assertThatThrownBy(() -> playlistService.export(playlistId, "xml"))
                .isInstanceOf(UnsupportedExportFormatException.class);
    }

    @Test
    void exportIsCaseInsensitive() {
        playlistService.addSong(playlistId, new AddSongRequest(SONG_1));
        ExportResult result = playlistService.export(playlistId, "JSON");
        assertThat(result.contentType()).isEqualTo("application/json");
    }

    @Test
    void exportCsvReturnsTextCsvContentType() {
        playlistService.addSong(playlistId, new AddSongRequest(SONG_1));
        ExportResult result = playlistService.export(playlistId, "csv");
        assertThat(result.contentType()).isEqualTo("text/csv;charset=UTF-8");
    }

    @Test
    void exportCsvBodyContainsHeader() {
        playlistService.addSong(playlistId, new AddSongRequest(SONG_1));
        String body = (String) playlistService.export(playlistId, "csv").body();
        assertThat(body).startsWith("position,title,artists,genres,duration_seconds,uri");
    }

    @Test
    void exportCsvBodyContainsOneRowPerSong() {
        playlistService.addSong(playlistId, new AddSongRequest(SONG_1));
        playlistService.addSong(playlistId, new AddSongRequest(SONG_2));
        String body = (String) playlistService.export(playlistId, "csv").body();
        long dataRows = body.lines().filter(l -> !l.startsWith("position")).count();
        assertThat(dataRows).isEqualTo(2);
    }

    @Test
    void exportCsvRowContainsSongTitle() {
        playlistService.addSong(playlistId, new AddSongRequest(SONG_1));
        String body = (String) playlistService.export(playlistId, "csv").body();
        // Paper Skies is the title of SONG_1 in the seed data
        assertThat(body).contains("Paper Skies");
    }
}
