package com.songplayer.persistence.repository;

import com.songplayer.persistence.entity.PlaylistItemEntity;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

/** Playlist item queries in playlist order. */
public interface PlaylistItemRepository extends JpaRepository<PlaylistItemEntity, UUID> {
    List<PlaylistItemEntity> findByPlaylistIdOrderByPositionAsc(UUID playlistId);
}
