package com.songplayer.application.shuffle;

import com.songplayer.persistence.entity.GenreEntity;
import com.songplayer.persistence.entity.PlaylistItemEntity;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.stereotype.Component;

/**
 * Distributes songs evenly across genres using round-robin interleaving
 * of per-genre buckets.
 */
@Component
public class GenreBalancedShuffleStrategy implements ShuffleStrategy {
    @Override
    public List<UUID> shuffle(List<PlaylistItemEntity> items) {
        Map<String, List<UUID>> byGenre = new LinkedHashMap<>();
        for (PlaylistItemEntity item : items) {
            String genre = item.getSong().getGenres().stream()
                    .map(GenreEntity::getName)
                    .min(String::compareTo)
                    .orElse("__unknown__");
            byGenre.computeIfAbsent(genre, k -> new ArrayList<>()).add(item.getSong().getId());
        }
        byGenre.values().forEach(Collections::shuffle);

        List<UUID> result = new ArrayList<>(items.size());
        List<List<UUID>> queues = new ArrayList<>(byGenre.values());
        while (queues.stream().anyMatch(q -> !q.isEmpty())) {
            for (List<UUID> queue : queues) {
                if (!queue.isEmpty()) result.add(queue.remove(0));
            }
        }
        return result;
    }
}
