package com.songplayer.application.shuffle;

import com.songplayer.persistence.entity.PlaylistItemEntity;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Component;

/** Generates a random playback order from playlist items. */
@Component
public class RandomShuffleStrategy implements ShuffleStrategy {
    @Override
    public List<UUID> shuffle(List<PlaylistItemEntity> items) {
        List<UUID> queue = new ArrayList<>(items.stream().map(i -> i.getSong().getId()).toList());
        Collections.shuffle(queue);
        return queue;
    }
}
