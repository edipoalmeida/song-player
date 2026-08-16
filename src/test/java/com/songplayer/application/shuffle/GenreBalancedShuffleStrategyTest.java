package com.songplayer.application.shuffle;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.songplayer.persistence.entity.GenreEntity;
import com.songplayer.persistence.entity.PlaylistItemEntity;
import com.songplayer.persistence.entity.SongEntity;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class GenreBalancedShuffleStrategyTest {

    private final GenreBalancedShuffleStrategy strategy = new GenreBalancedShuffleStrategy();

    @Test
    void resultContainsAllSongIds() {
        List<UUID> ids = List.of(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID());
        List<PlaylistItemEntity> items = List.of(
                item(ids.get(0), Set.of(genre("Pop"))),
                item(ids.get(1), Set.of(genre("Jazz"))),
                item(ids.get(2), Set.of(genre("Pop"))));

        assertThat(strategy.shuffle(items)).containsExactlyInAnyOrderElementsOf(ids);
    }

    @Test
    void emptyPlaylistReturnsEmptyList() {
        assertThat(strategy.shuffle(List.of())).isEmpty();
    }

    @Test
    void singleSongReturnsSingleEntry() {
        UUID id = UUID.randomUUID();
        assertThat(strategy.shuffle(List.of(item(id, Set.of(genre("Pop")))))).containsExactly(id);
    }

    @Test
    void songsWithNoGenreAreGroupedTogether() {
        UUID s1 = UUID.randomUUID(), s2 = UUID.randomUUID();
        List<UUID> result = strategy.shuffle(List.of(
                item(s1, Set.of()),
                item(s2, Set.of())));
        assertThat(result).containsExactlyInAnyOrder(s1, s2);
    }

    @Test
    void interleavesTwoGenresEvenly() {
        // 3 Pop songs, 3 Jazz songs — result should alternate genres
        UUID p1 = UUID.randomUUID(), p2 = UUID.randomUUID(), p3 = UUID.randomUUID();
        UUID j1 = UUID.randomUUID(), j2 = UUID.randomUUID(), j3 = UUID.randomUUID();
        GenreEntity pop = genre("Jazz"); // "Jazz" < "Pop" alphabetically → Jazz bucket first
        GenreEntity jazz = genre("Pop");

        List<PlaylistItemEntity> items = List.of(
                item(p1, Set.of(pop)), item(p2, Set.of(pop)), item(p3, Set.of(pop)),
                item(j1, Set.of(jazz)), item(j2, Set.of(jazz)), item(j3, Set.of(jazz)));

        List<UUID> result = strategy.shuffle(items);

        assertThat(result)
                .hasSize(6)
                .containsExactlyInAnyOrder(p1, p2, p3, j1, j2, j3);
    }

    // ── helpers ───────────────────────────────────────────────────────────────

    private static GenreEntity genre(String name) {
        GenreEntity g = mock(GenreEntity.class);
        when(g.getName()).thenReturn(name);
        return g;
    }

    private static PlaylistItemEntity item(UUID songId, Set<GenreEntity> genres) {
        SongEntity song = mock(SongEntity.class);
        when(song.getId()).thenReturn(songId);
        when(song.getGenres()).thenReturn(genres);
        PlaylistItemEntity item = mock(PlaylistItemEntity.class);
        when(item.getSong()).thenReturn(song);
        return item;
    }
}
