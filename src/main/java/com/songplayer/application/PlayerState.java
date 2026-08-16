package com.songplayer.application;

import com.songplayer.domain.PlaybackState;
import com.songplayer.domain.PlaybackStatus;
import com.songplayer.domain.ShuffleMode;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.UnaryOperator;
import org.springframework.stereotype.Component;

/**
 * Thread-safe singleton holding the global playback state.
 *
 * <p>All mutations replace the internal {@link InternalState} atomically via
 * {@link AtomicReference#compareAndSet} so no external synchronisation is needed.
 */
@Component
public class PlayerState {

    private record InternalState(PlaybackState playback, List<UUID> queue, int queueIndex) {}

    private final AtomicReference<InternalState> ref = new AtomicReference<>(
            new InternalState(stoppedPlayback(), List.of(), -1));

    private static PlaybackState stoppedPlayback() {
        return new PlaybackState(PlaybackStatus.STOPPED, null, null, 0, ShuffleMode.RANDOM, Instant.now());
    }

    public PlaybackState get() {
        return ref.get().playback();
    }

    public PlaybackState load(UUID playlistId, List<UUID> queue, ShuffleMode shuffleMode) {
        if (queue.isEmpty()) {
            PlaybackState ps = new PlaybackState(
                    PlaybackStatus.STOPPED, playlistId, null, 0, shuffleMode, Instant.now());
            ref.set(new InternalState(ps, List.of(), -1));
            return ps;
        }
        PlaybackState ps = new PlaybackState(
                PlaybackStatus.PLAYING, playlistId, queue.get(0), 0, shuffleMode, Instant.now());
        ref.set(new InternalState(ps, List.copyOf(queue), 0));
        return ps;
    }

    public PlaybackState play() {
        return update(s -> {
            if (s.playback().currentSongId() == null) throw new PlayerStateException("No playlist loaded");
            if (s.playback().status() == PlaybackStatus.PLAYING) return s;
            return new InternalState(withStatus(s.playback(), PlaybackStatus.PLAYING), s.queue(), s.queueIndex());
        });
    }

    public PlaybackState pause() {
        return update(s -> {
            if (s.playback().currentSongId() == null) throw new PlayerStateException("No playlist loaded");
            if (s.playback().status() == PlaybackStatus.PAUSED) return s;
            return new InternalState(withStatus(s.playback(), PlaybackStatus.PAUSED), s.queue(), s.queueIndex());
        });
    }

    public PlaybackState stop() {
        return update(s -> new InternalState(stoppedPlayback(), List.of(), -1));
    }

    public PlaybackState next() {
        return update(s -> {
            if (s.queue().isEmpty()) throw new PlayerStateException("No playlist loaded");
            int idx = (s.queueIndex() + 1) % s.queue().size();
            return new InternalState(atSong(s.playback(), s.queue().get(idx)), s.queue(), idx);
        });
    }

    public PlaybackState previous() {
        return update(s -> {
            if (s.queue().isEmpty()) throw new PlayerStateException("No playlist loaded");
            int idx = s.queueIndex() <= 0 ? s.queue().size() - 1 : s.queueIndex() - 1;
            return new InternalState(atSong(s.playback(), s.queue().get(idx)), s.queue(), idx);
        });
    }

    public PlaybackState seek(int positionSeconds) {
        if (positionSeconds < 0) throw new PlayerStateException("Seek position cannot be negative");
        return update(s -> {
            if (s.playback().currentSongId() == null) throw new PlayerStateException("No song playing");
            PlaybackState ps = new PlaybackState(s.playback().status(), s.playback().playlistId(),
                    s.playback().currentSongId(), positionSeconds, s.playback().shuffleMode(), Instant.now());
            return new InternalState(ps, s.queue(), s.queueIndex());
        });
    }

    private PlaybackState update(UnaryOperator<InternalState> updater) {
        InternalState cur;
        InternalState next;
        do {
            cur = ref.get();
            next = updater.apply(cur);   // may throw PlayerStateException — ref stays unchanged
        } while (!ref.compareAndSet(cur, next));
        return next.playback();
    }

    private static PlaybackState withStatus(PlaybackState p, PlaybackStatus status) {
        return new PlaybackState(status, p.playlistId(), p.currentSongId(),
                p.positionSeconds(), p.shuffleMode(), Instant.now());
    }

    private static PlaybackState atSong(PlaybackState p, UUID songId) {
        return new PlaybackState(PlaybackStatus.PLAYING, p.playlistId(), songId,
                0, p.shuffleMode(), Instant.now());
    }
}
