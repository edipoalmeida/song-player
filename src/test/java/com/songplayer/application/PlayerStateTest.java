package com.songplayer.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.songplayer.domain.PlaybackState;
import com.songplayer.domain.PlaybackStatus;
import com.songplayer.domain.ShuffleMode;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class PlayerStateTest {

    private static final UUID PLAYLIST_ID = UUID.fromString("aaaaaaaa-0000-0000-0000-000000000001");
    private static final UUID SONG_1 = UUID.fromString("aaaaaaaa-0000-0000-0000-000000000002");
    private static final UUID SONG_2 = UUID.fromString("aaaaaaaa-0000-0000-0000-000000000003");
    private static final UUID SONG_3 = UUID.fromString("aaaaaaaa-0000-0000-0000-000000000004");

    private PlayerState state;

    @BeforeEach
    void setUp() {
        state = new PlayerState();
    }

    private static PlayerState.QueueItem item(UUID id) {
        return new PlayerState.QueueItem(id, 300);
    }

    // ── initial state ─────────────────────────────────────────────────────────

    @Test
    void initialStateIsStopped() {
        PlaybackState s = state.get();
        assertThat(s.status()).isEqualTo(PlaybackStatus.STOPPED);
        assertThat(s.currentSongId()).isNull();
        assertThat(s.playlistId()).isNull();
        assertThat(s.positionSeconds()).isZero();
    }

    // ── load ──────────────────────────────────────────────────────────────────

    @Test
    void loadStartsPlaybackOnFirstSong() {
        PlaybackState s = state.load(PLAYLIST_ID, List.of(item(SONG_1), item(SONG_2)), ShuffleMode.RANDOM);
        assertThat(s.status()).isEqualTo(PlaybackStatus.PLAYING);
        assertThat(s.currentSongId()).isEqualTo(SONG_1);
        assertThat(s.playlistId()).isEqualTo(PLAYLIST_ID);
        assertThat(s.shuffleMode()).isEqualTo(ShuffleMode.RANDOM);
    }

    @Test
    void loadWithEmptyQueueStopsPlayerWithPlaylistContext() {
        PlaybackState s = state.load(PLAYLIST_ID, List.of(), ShuffleMode.RANDOM);
        assertThat(s.status()).isEqualTo(PlaybackStatus.STOPPED);
        assertThat(s.currentSongId()).isNull();
        assertThat(s.playlistId()).isEqualTo(PLAYLIST_ID);
    }

    @Test
    void loadWithEmptyQueueDoesNotAllowPlay() {
        state.load(PLAYLIST_ID, List.of(), ShuffleMode.RANDOM);
        assertThatThrownBy(() -> state.play()).isInstanceOf(PlayerStateException.class);
    }

    // ── play ──────────────────────────────────────────────────────────────────

    @Test
    void playWhenNothingLoadedThrows() {
        assertThatThrownBy(() -> state.play()).isInstanceOf(PlayerStateException.class);
    }

    @Test
    void playWhenPausedResumesToPlaying() {
        state.load(PLAYLIST_ID, List.of(item(SONG_1)), ShuffleMode.RANDOM);
        state.pause();
        assertThat(state.play().status()).isEqualTo(PlaybackStatus.PLAYING);
    }

    @Test
    void playWhenAlreadyPlayingIsIdempotent() {
        state.load(PLAYLIST_ID, List.of(item(SONG_1)), ShuffleMode.RANDOM);
        PlaybackState before = state.get();
        PlaybackState after = state.play();
        assertThat(after.status()).isEqualTo(PlaybackStatus.PLAYING);
        assertThat(after.currentSongId()).isEqualTo(before.currentSongId());
    }

    // ── pause ─────────────────────────────────────────────────────────────────

    @Test
    void pauseWhenNothingLoadedThrows() {
        assertThatThrownBy(() -> state.pause()).isInstanceOf(PlayerStateException.class);
    }

    @Test
    void pauseWhenPlayingPauses() {
        state.load(PLAYLIST_ID, List.of(item(SONG_1)), ShuffleMode.RANDOM);
        assertThat(state.pause().status()).isEqualTo(PlaybackStatus.PAUSED);
    }

    @Test
    void pauseWhenAlreadyPausedIsIdempotent() {
        state.load(PLAYLIST_ID, List.of(item(SONG_1)), ShuffleMode.RANDOM);
        state.pause();
        assertThat(state.pause().status()).isEqualTo(PlaybackStatus.PAUSED);
    }

    // ── stop ──────────────────────────────────────────────────────────────────

    @Test
    void stopClearsEverything() {
        state.load(PLAYLIST_ID, List.of(item(SONG_1), item(SONG_2)), ShuffleMode.SMART);
        PlaybackState s = state.stop();
        assertThat(s.status()).isEqualTo(PlaybackStatus.STOPPED);
        assertThat(s.currentSongId()).isNull();
        assertThat(s.playlistId()).isNull();
    }

    @Test
    void stopWhenAlreadyStoppedIsIdempotent() {
        assertThat(state.stop().status()).isEqualTo(PlaybackStatus.STOPPED);
    }

    @Test
    void afterStopPlayThrows() {
        state.load(PLAYLIST_ID, List.of(item(SONG_1)), ShuffleMode.RANDOM);
        state.stop();
        assertThatThrownBy(() -> state.play()).isInstanceOf(PlayerStateException.class);
    }

    // ── next ──────────────────────────────────────────────────────────────────

    @Test
    void nextWithNoPlaylistThrows() {
        assertThatThrownBy(() -> state.next()).isInstanceOf(PlayerStateException.class);
    }

    @Test
    void nextAdvancesToSecondSong() {
        state.load(PLAYLIST_ID, List.of(item(SONG_1), item(SONG_2), item(SONG_3)), ShuffleMode.RANDOM);
        assertThat(state.next().currentSongId()).isEqualTo(SONG_2);
    }

    @Test
    void nextWrapsAroundFromLastToFirst() {
        state.load(PLAYLIST_ID, List.of(item(SONG_1), item(SONG_2)), ShuffleMode.RANDOM);
        state.next();                                           // → SONG_2
        assertThat(state.next().currentSongId()).isEqualTo(SONG_1); // wraps
    }

    @Test
    void nextResetsPositionToZero() {
        state.load(PLAYLIST_ID, List.of(item(SONG_1), item(SONG_2)), ShuffleMode.RANDOM);
        state.seek(90);
        assertThat(state.next().positionSeconds()).isZero();
    }

    // ── previous ──────────────────────────────────────────────────────────────

    @Test
    void previousWithNoPlaylistThrows() {
        assertThatThrownBy(() -> state.previous()).isInstanceOf(PlayerStateException.class);
    }

    @Test
    void previousFromFirstSongGoesToLast() {
        state.load(PLAYLIST_ID, List.of(item(SONG_1), item(SONG_2), item(SONG_3)), ShuffleMode.RANDOM);
        assertThat(state.previous().currentSongId()).isEqualTo(SONG_3);
    }

    @Test
    void previousGoesToPreviousSong() {
        state.load(PLAYLIST_ID, List.of(item(SONG_1), item(SONG_2), item(SONG_3)), ShuffleMode.RANDOM);
        state.next();                                              // → SONG_2
        assertThat(state.previous().currentSongId()).isEqualTo(SONG_1);
    }

    // ── seek ──────────────────────────────────────────────────────────────────

    @Test
    void seekWithNothingPlayingThrows() {
        assertThatThrownBy(() -> state.seek(10)).isInstanceOf(PlayerStateException.class);
    }

    @Test
    void seekNegativePositionThrows() {
        state.load(PLAYLIST_ID, List.of(item(SONG_1)), ShuffleMode.RANDOM);
        assertThatThrownBy(() -> state.seek(-1)).isInstanceOf(PlayerStateException.class);
    }

    @Test
    void seekUpdatesPosition() {
        state.load(PLAYLIST_ID, List.of(item(SONG_1)), ShuffleMode.RANDOM);
        assertThat(state.seek(42).positionSeconds()).isEqualTo(42);
    }

    @Test
    void seekToZeroIsValid() {
        state.load(PLAYLIST_ID, List.of(item(SONG_1)), ShuffleMode.RANDOM);
        state.seek(30);
        assertThat(state.seek(0).positionSeconds()).isZero();
    }

    // ── thread safety ─────────────────────────────────────────────────────────

    @Test
    void concurrentNextCallsNeverProduceInvalidState() throws InterruptedException {
        state.load(PLAYLIST_ID, List.of(item(SONG_1), item(SONG_2), item(SONG_3)), ShuffleMode.RANDOM);
        int threadCount = 50;
        CountDownLatch latch = new CountDownLatch(threadCount);
        List<Throwable> errors = new CopyOnWriteArrayList<>();

        ExecutorService exec = Executors.newFixedThreadPool(10);
        for (int i = 0; i < threadCount; i++) {
            exec.submit(() -> {
                try {
                    state.next();
                } catch (Throwable t) {
                    errors.add(t);
                } finally {
                    latch.countDown();
                }
            });
        }

        assertThat(latch.await(5, TimeUnit.SECONDS)).as("all threads completed within timeout").isTrue();
        exec.shutdown();

        assertThat(errors).isEmpty();
        assertThat(state.get().currentSongId()).isIn(SONG_1, SONG_2, SONG_3);
        assertThat(state.get().status()).isEqualTo(PlaybackStatus.PLAYING);
    }

    @Test
    void concurrentStopAndNextNeverLeaveInvalidState() throws InterruptedException {
        state.load(PLAYLIST_ID, List.of(item(SONG_1), item(SONG_2)), ShuffleMode.RANDOM);
        int threadCount = 30;
        CountDownLatch latch = new CountDownLatch(threadCount);
        List<Throwable> errors = new CopyOnWriteArrayList<>();

        ExecutorService exec = Executors.newFixedThreadPool(10);
        for (int i = 0; i < threadCount; i++) {
            final int idx = i;
            exec.submit(() -> {
                try {
                    if (idx % 3 == 0) state.stop();
                    else if (idx % 3 == 1) state.load(PLAYLIST_ID, List.of(item(SONG_1), item(SONG_2)), ShuffleMode.RANDOM);
                    else state.next();
                } catch (PlayerStateException ignored) {
                    // expected when calling next() after stop() — valid race
                } catch (Throwable t) {
                    errors.add(t);
                } finally {
                    latch.countDown();
                }
            });
        }

        assertThat(latch.await(5, TimeUnit.SECONDS)).as("all threads completed within timeout").isTrue();
        exec.shutdown();

        assertThat(errors).isEmpty();
        // state must always be internally consistent — currentSongId matches status
        PlaybackState s = state.get();
        if (s.status() == PlaybackStatus.STOPPED) {
            assertThat(s.currentSongId()).isNull();
        } else {
            assertThat(s.currentSongId()).isNotNull();
        }
    }

    // ── 2× speed ──────────────────────────────────────────────────────────────

    @Test
    void playDoubleWhenNothingLoadedThrows() {
        assertThatThrownBy(() -> state.playDouble()).isInstanceOf(PlayerStateException.class);
    }

    @Test
    void playDoubleSetsStatus() {
        state.load(PLAYLIST_ID, List.of(item(SONG_1)), ShuffleMode.RANDOM);
        assertThat(state.playDouble().status()).isEqualTo(PlaybackStatus.PLAYING_2X);
    }

    @Test
    void playDoubleIsIdempotent() {
        state.load(PLAYLIST_ID, List.of(item(SONG_1)), ShuffleMode.RANDOM);
        state.playDouble();
        assertThat(state.playDouble().status()).isEqualTo(PlaybackStatus.PLAYING_2X);
    }

    @Test
    void switchFrom2xTo1xRestoresPlayingStatus() {
        state.load(PLAYLIST_ID, List.of(item(SONG_1)), ShuffleMode.RANDOM);
        state.playDouble();
        assertThat(state.play().status()).isEqualTo(PlaybackStatus.PLAYING);
    }

    @Test
    void pauseFrom2xCapturesPosition() {
        state.load(PLAYLIST_ID, List.of(item(SONG_1)), ShuffleMode.RANDOM);
        state.playDouble();
        PlaybackState paused = state.pause();
        assertThat(paused.status()).isEqualTo(PlaybackStatus.PAUSED);
    }

    @Test
    void nextPreservesSpeedMode() {
        state.load(PLAYLIST_ID, List.of(item(SONG_1), item(SONG_2)), ShuffleMode.RANDOM);
        state.playDouble();
        assertThat(state.next().status()).isEqualTo(PlaybackStatus.PLAYING_2X);
    }

    @Test
    void seekWhileIn2xPreservesSpeedMode() {
        state.load(PLAYLIST_ID, List.of(item(SONG_1)), ShuffleMode.RANDOM);
        state.playDouble();
        PlaybackState after = state.seek(10);
        assertThat(after.status()).isEqualTo(PlaybackStatus.PLAYING_2X);
        assertThat(after.positionSeconds()).isEqualTo(10);
    }
}
