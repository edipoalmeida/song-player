package com.songplayer.application;

import com.songplayer.api.dto.PlaybackStateResponse;
import com.songplayer.application.shuffle.ShuffleStrategyFactory;
import com.songplayer.domain.PlaybackState;
import com.songplayer.domain.ShuffleMode;
import com.songplayer.persistence.entity.PlaylistEntity;
import com.songplayer.persistence.repository.PlaylistRepository;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Orchestrates player commands against playlists and shuffle strategies. */
@Service
public class PlayerService {
    private final PlayerState playerState;
    private final PlaylistRepository playlistRepository;
    private final ShuffleStrategyFactory shuffleStrategyFactory;

    public PlayerService(PlayerState playerState, PlaylistRepository playlistRepository,
                         ShuffleStrategyFactory shuffleStrategyFactory) {
        this.playerState = playerState;
        this.playlistRepository = playlistRepository;
        this.shuffleStrategyFactory = shuffleStrategyFactory;
    }

    public PlaybackStateResponse state() {
        return toResponse(playerState.get());
    }

    /** Loads a playlist into the global player and starts playback from the generated queue. */
    @Transactional(readOnly = true)
    public PlaybackStateResponse playPlaylist(UUID playlistId, ShuffleMode strategy) {
        PlaylistEntity playlist = playlistRepository.findDetailedById(playlistId)
                .orElseThrow(() -> new ResourceNotFoundException("Playlist", playlistId));

        Map<UUID, Integer> durationBySongId = new HashMap<>();
        playlist.getItems().forEach(item ->
                durationBySongId.putIfAbsent(item.getSong().getId(), item.getSong().getDurationSeconds()));

        List<UUID> shuffledIds = shuffleStrategyFactory.forMode(strategy).shuffle(playlist.getItems());
        List<PlayerState.QueueItem> queue = shuffledIds.stream()
                .map(id -> new PlayerState.QueueItem(id, durationBySongId.getOrDefault(id, 0)))
                .toList();

        return toResponse(playerState.load(playlistId, queue, strategy));
    }

    public PlaybackStateResponse play()                     { return toResponse(playerState.play()); }
    public PlaybackStateResponse playDouble()               { return toResponse(playerState.playDouble()); }
    public PlaybackStateResponse pause()                    { return toResponse(playerState.pause()); }
    public PlaybackStateResponse stop()                     { return toResponse(playerState.stop()); }
    public PlaybackStateResponse next()                     { return toResponse(playerState.next()); }
    public PlaybackStateResponse previous()                 { return toResponse(playerState.previous()); }
    public PlaybackStateResponse seek(int positionSeconds)  { return toResponse(playerState.seek(positionSeconds)); }

    private PlaybackStateResponse toResponse(PlaybackState state) {
        return new PlaybackStateResponse(state.status(), state.playlistId(), state.currentSongId(),
                state.positionSeconds(), state.shuffleMode(), state.updatedAt());
    }
}
