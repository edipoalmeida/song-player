package com.songplayer.application;

import com.songplayer.api.dto.AddSongRequest;
import com.songplayer.api.dto.AddSongsFromArtistRequest;
import com.songplayer.api.dto.AddSongsRequest;
import com.songplayer.api.dto.CreatePlaylistRequest;
import com.songplayer.api.dto.PlaylistResponse;
import com.songplayer.api.dto.RecommendationResponse;
import com.songplayer.api.dto.ReorderPlaylistRequest;
import com.songplayer.api.dto.SongResponse;
import com.songplayer.api.dto.UpdatePlaylistRequest;
import com.songplayer.application.export.ExportResult;
import com.songplayer.application.mapper.PlaylistMapper;
import com.songplayer.application.shuffle.ShuffleStrategyFactory;
import com.songplayer.domain.ShuffleMode;
import com.songplayer.persistence.entity.GenreEntity;
import com.songplayer.persistence.entity.PlaylistEntity;
import com.songplayer.persistence.entity.PlaylistItemEntity;
import com.songplayer.persistence.entity.SongEntity;
import com.songplayer.persistence.repository.PlaylistRepository;
import com.songplayer.persistence.repository.SongRepository;
import jakarta.persistence.EntityManager;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Coordinates playlist lifecycle, ordering, recommendations, and export. */
@Service
public class PlaylistService {
    private final PlaylistRepository playlistRepository;
    private final SongRepository songRepository;
    private final PlaylistMapper playlistMapper;
    private final ArtistService artistService;
    private final EntityManager entityManager;
    private final ShuffleStrategyFactory shuffleStrategyFactory;
    private final ExportService exportService;
    private final PlayerState playerState;

    public PlaylistService(PlaylistRepository playlistRepository,
                           SongRepository songRepository, PlaylistMapper playlistMapper, ArtistService artistService,
                           EntityManager entityManager, ShuffleStrategyFactory shuffleStrategyFactory,
                           ExportService exportService, PlayerState playerState) {
        this.playlistRepository = playlistRepository;
        this.songRepository = songRepository;
        this.playlistMapper = playlistMapper;
        this.artistService = artistService;
        this.entityManager = entityManager;
        this.shuffleStrategyFactory = shuffleStrategyFactory;
        this.exportService = exportService;
        this.playerState = playerState;
    }

    @Transactional
    public PlaylistResponse create(CreatePlaylistRequest request) {
        PlaylistEntity playlist = playlistRepository.saveAndFlush(
                new PlaylistEntity(request.name(), request.description(), request.coverImageUrl()));
        return playlistMapper.toResponse(playlist);
    }

    @Transactional(readOnly = true)
    public List<PlaylistResponse> list() {
        return playlistRepository.findAllDetailed().stream().map(playlistMapper::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public PlaylistResponse getById(UUID playlistId) {
        return playlistMapper.toResponse(getDetailedPlaylist(playlistId));
    }

    @Transactional
    public PlaylistResponse update(UUID playlistId, UpdatePlaylistRequest request) {
        PlaylistEntity playlist = getDetailedPlaylist(playlistId);
        playlist.updateMetadata(request.name(), request.description(), request.coverImageUrl());
        entityManager.flush();
        return playlistMapper.toResponse(playlist);
    }

    /** Deletes a playlist and stops playback if it is active. */
    @Transactional
    public void delete(UUID playlistId) {
        PlaylistEntity playlist = getDetailedPlaylist(playlistId);
        playlistRepository.delete(playlist);
        if (playlistId.equals(playerState.get().playlistId())) {
            playerState.stop();
        }
    }

    @Transactional
    public PlaylistResponse addSong(UUID playlistId, AddSongRequest request) {
        PlaylistEntity playlist = getDetailedPlaylist(playlistId);
        SongEntity song = songRepository.findById(request.songId())
                .orElseThrow(() -> new ResourceNotFoundException("Song", request.songId()));
        PlaylistItemEntity item = new PlaylistItemEntity(playlist, song, playlist.getItems().size());
        playlist.addItem(item);
        playlist.touch();
        entityManager.flush();
        return playlistMapper.toResponse(playlist);
    }

    @Transactional
    public PlaylistResponse addSongs(UUID playlistId, AddSongsRequest request) {
        PlaylistEntity playlist = getDetailedPlaylist(playlistId);
        List<SongEntity> songs = songRepository.findAllById(request.songIds());
        Set<UUID> foundIds = songs.stream().map(SongEntity::getId).collect(Collectors.toSet());
        request.songIds().stream()
                .filter(id -> !foundIds.contains(id))
                .findFirst()
                .ifPresent(id -> { throw new ResourceNotFoundException("Song", id); });
        int nextPosition = playlist.getItems().size();
        for (SongEntity song : songs) {
            playlist.addItem(new PlaylistItemEntity(playlist, song, nextPosition++));
        }
        playlist.touch();
        entityManager.flush();
        return playlistMapper.toResponse(playlist);
    }

    @Transactional
    public PlaylistResponse addSongsFromArtist(UUID playlistId, AddSongsFromArtistRequest request) {
        PlaylistEntity playlist = getDetailedPlaylist(playlistId);
        UUID artistId = request.artistId();
        artistService.findById(artistId).orElseThrow(() -> new ResourceNotFoundException("Artist", artistId));
        List<SongEntity> songs = songRepository.findByArtistsId(artistId);
        if (songs.isEmpty()) {
            return playlistMapper.toResponse(playlist);
        }
        int nextPosition = playlist.getItems().size();
        for (SongEntity song : songs) {
            playlist.addItem(new PlaylistItemEntity(playlist, song, nextPosition++));
        }
        playlist.touch();
        entityManager.flush();
        return playlistMapper.toResponse(playlist);
    }

    @Transactional
    public void removeSong(UUID playlistId, UUID playlistItemId) {
        PlaylistEntity playlist = getDetailedPlaylist(playlistId);
        if (!playlist.removeItem(playlistItemId)) {
            throw new ResourceNotFoundException("Playlist item", playlistItemId);
        }
        entityManager.flush();
        playlist.getItems().sort(Comparator.comparingInt(PlaylistItemEntity::getPosition));
        for (int index = 0; index < playlist.getItems().size(); index++) {
            playlist.getItems().get(index).changePosition(index);
        }
        playlist.touch();
    }

    /** Replaces playlist item positions with a zero-based contiguous order. */
    @Transactional
    public PlaylistResponse reorder(UUID playlistId, ReorderPlaylistRequest request) {
        PlaylistEntity playlist = getDetailedPlaylist(playlistId);
        Map<UUID, Integer> requestedPositions = positionsFrom(request);
        validatePositions(playlist, requestedPositions);

        int itemCount = playlist.getItems().size();
        for (PlaylistItemEntity item : playlist.getItems()) {
            item.changePosition(itemCount + item.getPosition());
        }
        entityManager.flush();
        for (PlaylistItemEntity item : playlist.getItems()) {
            item.changePosition(requestedPositions.get(item.getId()));
        }
        playlist.getItems().sort(Comparator.comparingInt(PlaylistItemEntity::getPosition));
        playlist.touch();
        entityManager.flush();
        return playlistMapper.toResponse(playlist);
    }

    private PlaylistEntity getDetailedPlaylist(UUID playlistId) {
        return playlistRepository.findDetailedById(playlistId)
                .orElseThrow(() -> new ResourceNotFoundException("Playlist", playlistId));
    }

    private Map<UUID, Integer> positionsFrom(ReorderPlaylistRequest request) {
        Map<UUID, Integer> positions = new HashMap<>();
        for (ReorderPlaylistRequest.ItemPosition item : request.items()) {
            if (positions.put(item.playlistItemId(), item.position()) != null) {
                throw new InvalidPlaylistOrderException("A playlist item must appear only once in a reorder request");
            }
        }
        return positions;
    }

    private void validatePositions(PlaylistEntity playlist, Map<UUID, Integer> requestedPositions) {
        Set<UUID> existingIds = playlist.getItems().stream().map(PlaylistItemEntity::getId).collect(Collectors.toSet());
        if (!existingIds.equals(requestedPositions.keySet())) {
            throw new InvalidPlaylistOrderException("Reorder request must contain every playlist item exactly once");
        }
        Set<Integer> expected = new HashSet<>();
        for (int position = 0; position < playlist.getItems().size(); position++) {
            expected.add(position);
        }
        if (!expected.equals(new HashSet<>(requestedPositions.values()))) {
            throw new InvalidPlaylistOrderException("Playlist positions must form a contiguous sequence starting at zero");
        }
    }

    /** Generates a playback queue without mutating the player state. */
    @Transactional(readOnly = true)
    public List<UUID> queue(UUID playlistId, ShuffleMode strategy) {
        PlaylistEntity playlist = getDetailedPlaylist(playlistId);
        return shuffleStrategyFactory.forMode(strategy).shuffle(playlist.getItems());
    }

    /** Recommends songs that overlap with the playlist's genre mix. */
    @Transactional(readOnly = true)
    public RecommendationResponse recommendations(UUID playlistId, int limit) {
        PlaylistEntity playlist = getDetailedPlaylist(playlistId);

        Set<UUID> playlistSongIds = playlist.getItems().stream()
                .map(i -> i.getSong().getId()).collect(Collectors.toSet());

        Set<String> genreNames = playlist.getItems().stream()
                .flatMap(i -> i.getSong().getGenres().stream())
                .map(GenreEntity::getName)
                .collect(Collectors.toSet());

        if (genreNames.isEmpty()) return new RecommendationResponse(List.of());

        List<SongResponse> ranked = songRepository
                .findRecommendationsRanked(playlistSongIds, genreNames, PageRequest.of(0, limit))
                .stream()
                .map(row -> playlistMapper.toSongResponse((SongEntity) row[0]))
                .toList();

        return new RecommendationResponse(ranked);
    }

    @Transactional(readOnly = true)
    public ExportResult export(UUID playlistId, String format) {
        return exportService.export(getDetailedPlaylist(playlistId), format);
    }
}
