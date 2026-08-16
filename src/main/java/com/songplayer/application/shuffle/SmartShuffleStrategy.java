package com.songplayer.application.shuffle;

import com.songplayer.persistence.entity.ArtistEntity;
import com.songplayer.persistence.entity.PlaylistItemEntity;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.stereotype.Component;

/**
 * Avoids consecutive songs by the same artist using round-robin interleaving
 * of per-artist buckets.
 */
@Component
public class SmartShuffleStrategy implements ShuffleStrategy {
    @Override
    public List<UUID> shuffle(List<PlaylistItemEntity> items) {
        Map<UUID, List<UUID>> byArtist = new LinkedHashMap<>();
        for (PlaylistItemEntity item : items) {
            UUID artistKey = item.getSong().getArtists().stream()
                    .min(Comparator.comparing(ArtistEntity::getName))
                    .map(ArtistEntity::getId)
                    .orElse(item.getSong().getId());
            byArtist.computeIfAbsent(artistKey, k -> new ArrayList<>()).add(item.getSong().getId());
        }
        byArtist.values().forEach(Collections::shuffle);

        List<UUID> result = new ArrayList<>(items.size());
        List<List<UUID>> queues = new ArrayList<>(byArtist.values());
        while (queues.stream().anyMatch(q -> !q.isEmpty())) {
            for (List<UUID> queue : queues) {
                if (!queue.isEmpty()) result.add(queue.remove(0));
            }
        }
        return result;
    }
}
