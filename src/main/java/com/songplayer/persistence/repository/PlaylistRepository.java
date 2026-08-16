package com.songplayer.persistence.repository;

import com.songplayer.persistence.entity.PlaylistEntity;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/** Playlist queries with items and songs preloaded for API use. */
public interface PlaylistRepository extends JpaRepository<PlaylistEntity, UUID> {
    /** Loads one playlist with its ordered items and songs. */
    @EntityGraph(attributePaths = {"items", "items.song"})
    @Query("select p from PlaylistEntity p where p.id = :id")
    Optional<PlaylistEntity> findDetailedById(@Param("id") UUID id);

    /** Lists playlists with their ordered items and songs. */
    @EntityGraph(attributePaths = {"items", "items.song"})
    @Query("select distinct p from PlaylistEntity p")
    List<PlaylistEntity> findAllDetailed();
}
