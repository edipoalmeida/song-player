package com.songplayer.persistence.repository;

import com.songplayer.persistence.entity.ArtistEntity;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

/** Artist catalog lookups. */
public interface ArtistRepository extends JpaRepository<ArtistEntity, UUID> {
    Optional<ArtistEntity> findById(UUID artistId);

    Optional<ArtistEntity> findByNameIgnoreCase(String name);
}
