package com.songplayer.persistence.repository;

import com.songplayer.persistence.entity.SongEntity;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/** Catalog song queries and recommendation lookups. */
public interface SongRepository extends JpaRepository<SongEntity, UUID> {
    List<SongEntity> findTop20ByGenresNameIgnoreCaseOrderByTitleAsc(String genre);
    List<SongEntity> findByArtistsId(UUID artistId);

    /**
     * Returns songs that share at least one genre with the given set, excluding
     * already-playlist songs, ranked by number of matching genres descending.
     * Filtering and ranking happen entirely in the database; only the top
     * {@code pageable.getPageSize()} rows are returned.
     */
    @Query("SELECT s, COUNT(g) FROM SongEntity s JOIN s.genres g " +
           "WHERE s.id NOT IN :excludedIds AND g.name IN :genreNames " +
           "GROUP BY s ORDER BY COUNT(g) DESC")
    List<Object[]> findRecommendationsRanked(
            @Param("excludedIds") Set<UUID> excludedIds,
            @Param("genreNames") Set<String> genreNames,
            Pageable pageable);
}
