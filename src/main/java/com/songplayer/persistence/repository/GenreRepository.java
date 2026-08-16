package com.songplayer.persistence.repository;

import com.songplayer.persistence.entity.GenreEntity;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

/** Genre catalog lookups. */
public interface GenreRepository extends JpaRepository<GenreEntity, UUID> {
    Optional<GenreEntity> findByNameIgnoreCase(String name);
}
