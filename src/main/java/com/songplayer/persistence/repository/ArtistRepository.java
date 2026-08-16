package com.songplayer.persistence.repository;

import com.songplayer.persistence.entity.ArtistEntity;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

/** Artist catalog lookups. */
public interface ArtistRepository extends JpaRepository<ArtistEntity, UUID> {
}
