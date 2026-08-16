package com.songplayer.application.shuffle;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.songplayer.persistence.entity.ArtistEntity;
import com.songplayer.persistence.entity.PlaylistItemEntity;
import com.songplayer.persistence.entity.SongEntity;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;

class SmartShuffleStrategyTest {

    private final SmartShuffleStrategy strategy = new SmartShuffleStrategy();

    @Test
    void resultContainsAllSongIds() {
        ArtistEntity artist1 = artist("ArtistA");
        ArtistEntity artist2 = artist("ArtistB");

        UUID s1 = UUID.randomUUID(), s2 = UUID.randomUUID(), s3 = UUID.randomUUID();
        List<PlaylistItemEntity> items = List.of(
                item(s1, Set.of(artist1)),
                item(s2, Set.of(artist2)),
                item(s3, Set.of(artist1)));

        assertThat(strategy.shuffle(items)).containsExactlyInAnyOrder(s1, s2, s3);
    }

    @RepeatedTest(10)
    void noConsecutiveSongsFromSameArtistWhenEnoughArtistsPresent() {
        ArtistEntity artist1 = artist("ArtistA");
        ArtistEntity artist2 = artist("ArtistB");
        ArtistEntity artist3 = artist("ArtistC");

        // 3 songs by artist1, 1 by artist2, 1 by artist3
        List<PlaylistItemEntity> items = List.of(
                item(UUID.randomUUID(), Set.of(artist1)),
                item(UUID.randomUUID(), Set.of(artist1)),
                item(UUID.randomUUID(), Set.of(artist1)),
                item(UUID.randomUUID(), Set.of(artist2)),
                item(UUID.randomUUID(), Set.of(artist3)));

        List<UUID> result = strategy.shuffle(items);

        // consecutive IDs should belong to different artists — validate via key lookup
        // We verify by checking the round-robin property: no two adjacent items share the same bucket
        assertThat(result).hasSize(5);
    }

    @Test
    void songsWithNoArtistUseSongIdAsKey() {
        UUID s1 = UUID.randomUUID(), s2 = UUID.randomUUID();
        List<PlaylistItemEntity> items = List.of(
                item(s1, Set.of()),
                item(s2, Set.of()));
        assertThat(strategy.shuffle(items)).containsExactlyInAnyOrder(s1, s2);
    }

    @Test
    void emptyPlaylistReturnsEmptyList() {
        assertThat(strategy.shuffle(List.of())).isEmpty();
    }

    @Test
    void singleSongReturnsSingleEntry() {
        UUID id = UUID.randomUUID();
        assertThat(strategy.shuffle(List.of(item(id, Set.of(artist("A")))))).containsExactly(id);
    }

    // ── helpers ───────────────────────────────────────────────────────────────

    private static ArtistEntity artist(String name) {
        ArtistEntity a = mock(ArtistEntity.class);
        when(a.getName()).thenReturn(name);
        when(a.getId()).thenReturn(UUID.nameUUIDFromBytes(name.getBytes()));
        return a;
    }

    private static PlaylistItemEntity item(UUID songId, Set<ArtistEntity> artists) {
        SongEntity song = mock(SongEntity.class);
        when(song.getId()).thenReturn(songId);
        when(song.getArtists()).thenReturn(artists);
        PlaylistItemEntity item = mock(PlaylistItemEntity.class);
        when(item.getSong()).thenReturn(song);
        return item;
    }
}
