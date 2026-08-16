package com.songplayer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.songplayer.api.dto.AddSongRequest;
import com.songplayer.api.dto.CreatePlaylistRequest;
import com.songplayer.api.dto.PlaybackStateResponse;
import com.songplayer.application.PlayerService;
import com.songplayer.application.PlayerStateException;
import com.songplayer.application.PlaylistService;
import com.songplayer.application.ResourceNotFoundException;
import com.songplayer.domain.PlaybackStatus;
import com.songplayer.domain.ShuffleMode;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class PlayerServiceIntegrationTests {

    private static final UUID SONG_1 = UUID.fromString("20000000-0000-0000-0000-000000000001");
    private static final UUID SONG_2 = UUID.fromString("20000000-0000-0000-0000-000000000002");
    private static final UUID SONG_3 = UUID.fromString("20000000-0000-0000-0000-000000000003");

    @Autowired
    private PlayerService playerService;

    @Autowired
    private PlaylistService playlistService;

    private UUID playlistId;

    @BeforeEach
    void setUp() {
        // stop player before each test to reset shared singleton
        playerService.stop();

        playlistId = playlistService.create(new CreatePlaylistRequest("Player Test", null, null)).id();
        playlistService.addSong(playlistId, new AddSongRequest(SONG_1));
        playlistService.addSong(playlistId, new AddSongRequest(SONG_2));
        playlistService.addSong(playlistId, new AddSongRequest(SONG_3));
    }

    @AfterEach
    void tearDown() {
        playerService.stop();
        try { playlistService.delete(playlistId); } catch (Exception ignored) { /* playlist may have been deleted by the test */ }
    }

    // ── state ─────────────────────────────────────────────────────────────────

    @Test
    void initialStateIsStopped() {
        assertThat(playerService.state().status()).isEqualTo(PlaybackStatus.STOPPED);
        assertThat(playerService.state().currentSongId()).isNull();
    }

    // ── playPlaylist ──────────────────────────────────────────────────────────

    @Test
    void playPlaylistStartsPlayback() {
        PlaybackStateResponse state = playerService.playPlaylist(playlistId, ShuffleMode.RANDOM);

        assertThat(state.status()).isEqualTo(PlaybackStatus.PLAYING);
        assertThat(state.playlistId()).isEqualTo(playlistId);
        assertThat(state.currentSongId()).isIn(SONG_1, SONG_2, SONG_3);
    }

    @Test
    void playPlaylistWithSmartShuffleStartsPlayback() {
        PlaybackStateResponse state = playerService.playPlaylist(playlistId, ShuffleMode.SMART);
        assertThat(state.status()).isEqualTo(PlaybackStatus.PLAYING);
        assertThat(state.shuffleMode()).isEqualTo(ShuffleMode.SMART);
    }

    @Test
    void playPlaylistWithGenreBalancedShuffleStartsPlayback() {
        PlaybackStateResponse state = playerService.playPlaylist(playlistId, ShuffleMode.GENRE_BALANCED);
        assertThat(state.status()).isEqualTo(PlaybackStatus.PLAYING);
        assertThat(state.shuffleMode()).isEqualTo(ShuffleMode.GENRE_BALANCED);
    }

    @Test
    void playPlaylistWithUnknownPlaylistIdThrowsResourceNotFoundException() {
        UUID unknownId = UUID.randomUUID();
        assertThatThrownBy(() -> playerService.playPlaylist(unknownId, ShuffleMode.RANDOM))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    // ── play / pause ──────────────────────────────────────────────────────────

    @Test
    void pauseTransitionsToPaused() {
        playerService.playPlaylist(playlistId, ShuffleMode.RANDOM);
        assertThat(playerService.pause().status()).isEqualTo(PlaybackStatus.PAUSED);
    }

    @Test
    void playAfterPauseResumesPlaying() {
        playerService.playPlaylist(playlistId, ShuffleMode.RANDOM);
        playerService.pause();
        assertThat(playerService.play().status()).isEqualTo(PlaybackStatus.PLAYING);
    }

    @Test
    void playWithNothingLoadedThrowsPlayerStateException() {
        // player was stopped in @BeforeEach
        assertThatThrownBy(() -> playerService.play())
                .isInstanceOf(PlayerStateException.class);
    }

    @Test
    void pauseWithNothingLoadedThrowsPlayerStateException() {
        assertThatThrownBy(() -> playerService.pause())
                .isInstanceOf(PlayerStateException.class);
    }

    // ── stop ──────────────────────────────────────────────────────────────────

    @Test
    void stopTransitionsToStoppedAndClearsState() {
        playerService.playPlaylist(playlistId, ShuffleMode.RANDOM);
        PlaybackStateResponse stopped = playerService.stop();

        assertThat(stopped.status()).isEqualTo(PlaybackStatus.STOPPED);
        assertThat(stopped.currentSongId()).isNull();
        assertThat(stopped.playlistId()).isNull();
    }

    @Test
    void stopIsIdempotentWhenAlreadyStopped() {
        assertThat(playerService.stop().status()).isEqualTo(PlaybackStatus.STOPPED);
    }

    // ── next / previous ───────────────────────────────────────────────────────

    @Test
    void nextAdvancesToDifferentSong() {
        PlaybackStateResponse initial = playerService.playPlaylist(playlistId, ShuffleMode.RANDOM);
        UUID firstSong = initial.currentSongId();
        UUID secondSong = playerService.next().currentSongId();
        assertThat(secondSong).isIn(SONG_1, SONG_2, SONG_3)
                .isNotEqualTo(firstSong);
    }

    @Test
    void nextWithNothingLoadedThrowsPlayerStateException() {
        assertThatThrownBy(() -> playerService.next())
                .isInstanceOf(PlayerStateException.class);
    }

    @Test
    void previousWithNothingLoadedThrowsPlayerStateException() {
        assertThatThrownBy(() -> playerService.previous())
                .isInstanceOf(PlayerStateException.class);
    }

    @Test
    void nextResetsPositionToZero() {
        playerService.playPlaylist(playlistId, ShuffleMode.RANDOM);
        playerService.seek(60);
        assertThat(playerService.next().positionSeconds()).isZero();
    }

    @Test
    void previousFromFirstSongGoesToLastSongInQueue() {
        playerService.playPlaylist(playlistId, ShuffleMode.RANDOM);
        UUID prev = playerService.previous().currentSongId();
        assertThat(prev).isIn(SONG_1, SONG_2, SONG_3);
    }

    // ── seek ──────────────────────────────────────────────────────────────────

    @Test
    void seekUpdatesPosition() {
        playerService.playPlaylist(playlistId, ShuffleMode.RANDOM);
        assertThat(playerService.seek(42).positionSeconds()).isEqualTo(42);
    }

    @Test
    void seekWithNegativePositionThrowsPlayerStateException() {
        playerService.playPlaylist(playlistId, ShuffleMode.RANDOM);
        assertThatThrownBy(() -> playerService.seek(-1))
                .isInstanceOf(PlayerStateException.class);
    }

    @Test
    void seekWithNothingLoadedThrowsPlayerStateException() {
        assertThatThrownBy(() -> playerService.seek(30))
                .isInstanceOf(PlayerStateException.class);
    }
}
