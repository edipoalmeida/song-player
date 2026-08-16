package com.songplayer.application;

import com.songplayer.domain.PlaybackState;
import com.songplayer.domain.PlaybackStatus;
import com.songplayer.domain.ShuffleMode;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.UnaryOperator;
import org.springframework.stereotype.Component;

/**
 * Thread-safe singleton holding the global playback state.
 *
 * <p>Uses a <b>virtual clock</b>: instead of storing a static {@code positionSeconds},
 * the state records when playback started ({@code playbackStartedAt}) and the accumulated
 * offset ({@code seekOffset}). The effective position is computed on every read as
 * {@code seekOffset + elapsed}. When the elapsed time exceeds the current song's duration,
 * {@link #get()} automatically advances to the next song in the queue.
 *
 * <p>All mutations replace the internal {@link InternalState} atomically via
 * {@link AtomicReference#compareAndSet} so no external synchronisation is needed.
 */
@Component
public class PlayerState {

    /** A song entry in the playback queue, including duration for auto-advance. */
    public record QueueItem(UUID songId, int durationSeconds) {}

    private record InternalState(
            PlaybackStatus status,
            UUID playlistId,
            ShuffleMode shuffleMode,
            List<QueueItem> queue,
            int queueIndex,
            Instant playbackStartedAt,
            int seekOffset
    ) {
        UUID currentSongId() {
            return (queue.isEmpty() || queueIndex < 0) ? null : queue.get(queueIndex).songId();
        }

        int currentDuration() {
            return (queue.isEmpty() || queueIndex < 0) ? 0 : queue.get(queueIndex).durationSeconds();
        }
    }

    private final AtomicReference<InternalState> ref = new AtomicReference<>(stopped());

    private static InternalState stopped() {
        return new InternalState(PlaybackStatus.STOPPED, null, ShuffleMode.RANDOM,
                List.of(), -1, null, 0);
    }

    /**
     * Returns the current playback state with a live-computed {@code positionSeconds}.
     * Auto-advances to the next song if the current song's duration has elapsed.
     */
    public PlaybackState get() {
        InternalState s = ref.get();
        if (s.status() != PlaybackStatus.PLAYING || s.queue().isEmpty()) {
            return buildPlaybackState(s, s.seekOffset());
        }
        return resolvePlayingState(s);
    }

    public PlaybackState load(UUID playlistId, List<QueueItem> queue, ShuffleMode shuffleMode) {
        if (queue.isEmpty()) {
            InternalState s = new InternalState(PlaybackStatus.STOPPED, playlistId, shuffleMode,
                    List.of(), -1, null, 0);
            ref.set(s);
            return buildPlaybackState(s, 0);
        }
        InternalState s = new InternalState(PlaybackStatus.PLAYING, playlistId, shuffleMode,
                List.copyOf(queue), 0, Instant.now(), 0);
        ref.set(s);
        return buildPlaybackState(s, 0);
    }

    public PlaybackState play() {
        return update(s -> {
            if (s.currentSongId() == null) throw new PlayerStateException("No playlist loaded");
            if (s.status() == PlaybackStatus.PLAYING) return s;
            return new InternalState(PlaybackStatus.PLAYING, s.playlistId(), s.shuffleMode(),
                    s.queue(), s.queueIndex(), Instant.now(), s.seekOffset());
        });
    }

    public PlaybackState pause() {
        return update(s -> {
            if (s.currentSongId() == null) throw new PlayerStateException("No playlist loaded");
            if (s.status() == PlaybackStatus.PAUSED) return s;
            return new InternalState(PlaybackStatus.PAUSED, s.playlistId(), s.shuffleMode(),
                    s.queue(), s.queueIndex(), null, computePosition(s));
        });
    }

    public PlaybackState stop() {
        return update(s -> stopped());
    }

    public PlaybackState next() {
        return update(s -> {
            if (s.queue().isEmpty()) throw new PlayerStateException("No playlist loaded");
            int idx = (s.queueIndex() + 1) % s.queue().size();
            return new InternalState(PlaybackStatus.PLAYING, s.playlistId(), s.shuffleMode(),
                    s.queue(), idx, Instant.now(), 0);
        });
    }

    public PlaybackState previous() {
        return update(s -> {
            if (s.queue().isEmpty()) throw new PlayerStateException("No playlist loaded");
            int idx = s.queueIndex() <= 0 ? s.queue().size() - 1 : s.queueIndex() - 1;
            return new InternalState(PlaybackStatus.PLAYING, s.playlistId(), s.shuffleMode(),
                    s.queue(), idx, Instant.now(), 0);
        });
    }

    public PlaybackState seek(int positionSeconds) {
        if (positionSeconds < 0) throw new PlayerStateException("Seek position cannot be negative");
        return update(s -> {
            if (s.currentSongId() == null) throw new PlayerStateException("No song playing");
            Instant startedAt = s.status() == PlaybackStatus.PLAYING ? Instant.now() : null;
            return new InternalState(s.status(), s.playlistId(), s.shuffleMode(),
                    s.queue(), s.queueIndex(), startedAt, positionSeconds);
        });
    }

    /**
     * Computes the effective playback state, advancing to the next song(s) if the
     * elapsed time has exceeded the current song's duration.
     */
    private PlaybackState resolvePlayingState(InternalState s) {
        long elapsed = Duration.between(s.playbackStartedAt(), Instant.now()).toSeconds();
        long remaining = s.seekOffset() + elapsed;

        int idx = s.queueIndex();
        int steps = 0;
        while (s.queue().get(idx).durationSeconds() > 0
                && remaining >= s.queue().get(idx).durationSeconds()
                && steps < s.queue().size()) {
            remaining -= s.queue().get(idx).durationSeconds();
            idx = (idx + 1) % s.queue().size();
            steps++;
        }

        if (idx != s.queueIndex()) {
            InternalState advanced = new InternalState(
                    PlaybackStatus.PLAYING, s.playlistId(), s.shuffleMode(),
                    s.queue(), idx, Instant.now(), 0);
            ref.compareAndSet(s, advanced);
        }

        int position = (int) Math.max(0, remaining);
        return new PlaybackState(PlaybackStatus.PLAYING, s.playlistId(),
                s.queue().get(idx).songId(), position, s.shuffleMode(), Instant.now());
    }

    private static int computePosition(InternalState s) {
        if (s.status() != PlaybackStatus.PLAYING || s.playbackStartedAt() == null) {
            return s.seekOffset();
        }
        long elapsed = Duration.between(s.playbackStartedAt(), Instant.now()).toSeconds();
        return (int) (s.seekOffset() + elapsed);
    }

    private static PlaybackState buildPlaybackState(InternalState s, int positionSeconds) {
        return new PlaybackState(s.status(), s.playlistId(), s.currentSongId(),
                positionSeconds, s.shuffleMode(), Instant.now());
    }

    private PlaybackState update(UnaryOperator<InternalState> updater) {
        InternalState cur, next;
        do {
            cur = ref.get();
            next = updater.apply(cur);
        } while (!ref.compareAndSet(cur, next));
        return buildPlaybackState(next, next.seekOffset());
    }
}
