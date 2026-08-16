package com.songplayer.application.shuffle;

import com.songplayer.persistence.entity.PlaylistItemEntity;
import java.util.List;
import java.util.UUID;

/** Selects and orders song IDs from a playlist into a playback queue. */
public interface ShuffleStrategy {
    List<UUID> shuffle(List<PlaylistItemEntity> items);
}
