package com.songplayer.application;

import com.songplayer.persistence.entity.ArtistEntity;
import com.songplayer.persistence.repository.ArtistRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

/** Resolves artists needed by playlist workflows. */
@Service
public class ArtistService {
    private final ArtistRepository artistRepository;

    public ArtistService(ArtistRepository artistRepository) {
        this.artistRepository = artistRepository;
    }

    public Optional<ArtistEntity> findById(UUID id) {
        return artistRepository.findById(id);
    }
}
