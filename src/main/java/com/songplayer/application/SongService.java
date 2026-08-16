package com.songplayer.application;

import com.songplayer.api.dto.SongResponse;
import com.songplayer.application.mapper.PlaylistMapper;
import com.songplayer.persistence.repository.SongRepository;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Provides read-only song catalog responses. */
@Service
public class SongService {
    private final SongRepository songRepository;
    private final PlaylistMapper playlistMapper;

    public SongService(SongRepository songRepository, PlaylistMapper playlistMapper) {
        this.songRepository = songRepository;
        this.playlistMapper = playlistMapper;
    }

    @Transactional(readOnly = true)
    public List<SongResponse> list() {
        return songRepository.findAll().stream()
                .map(playlistMapper::toSongResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public SongResponse getById(UUID songId) {
        return playlistMapper.toSongResponse(
                songRepository.findById(songId)
                        .orElseThrow(() -> new ResourceNotFoundException("Song", songId)));
    }
}
