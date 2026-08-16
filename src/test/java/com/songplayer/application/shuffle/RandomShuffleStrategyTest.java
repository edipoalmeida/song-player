package com.songplayer.application.shuffle;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.songplayer.persistence.entity.PlaylistItemEntity;
import com.songplayer.persistence.entity.SongEntity;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class RandomShuffleStrategyTest {

    private final RandomShuffleStrategy strategy = new RandomShuffleStrategy();

    @Test
    void resultContainsAllSongIds() {
        List<UUID> songIds = List.of(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID());
        List<PlaylistItemEntity> items = buildItems(songIds);

        List<UUID> result = strategy.shuffle(items);

        assertThat(result).containsExactlyInAnyOrderElementsOf(songIds);
    }

    @Test
    void emptyPlaylistReturnsEmptyList() {
        assertThat(strategy.shuffle(List.of())).isEmpty();
    }

    @Test
    void singleSongReturnsSingleEntry() {
        UUID id = UUID.randomUUID();
        List<UUID> result = strategy.shuffle(buildItems(List.of(id)));
        assertThat(result).containsExactly(id);
    }

    @Test
    void resultIsPermutationOfInput() {
        List<UUID> songIds = List.of(
                UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(),
                UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID());
        List<UUID> result = strategy.shuffle(buildItems(songIds));

        assertThat(result).hasSize(songIds.size())
                .containsExactlyInAnyOrderElementsOf(songIds);
    }

    // ── helpers ───────────────────────────────────────────────────────────────

    private static List<PlaylistItemEntity> buildItems(List<UUID> songIds) {
        int[] pos = {0};
        return songIds.stream().map(id -> {
            SongEntity song = mock(SongEntity.class);
            when(song.getId()).thenReturn(id);
            PlaylistItemEntity item = mock(PlaylistItemEntity.class);
            when(item.getSong()).thenReturn(song);
            when(item.getPosition()).thenReturn(pos[0]++);
            return item;
        }).toList();
    }
}
