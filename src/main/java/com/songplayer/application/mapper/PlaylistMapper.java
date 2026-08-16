package com.songplayer.application.mapper;

import com.songplayer.api.dto.AlbumResponse;
import com.songplayer.api.dto.ArtistResponse;
import com.songplayer.api.dto.PlaylistItemResponse;
import com.songplayer.api.dto.PlaylistResponse;
import com.songplayer.api.dto.SongResponse;
import com.songplayer.persistence.entity.GenreEntity;
import com.songplayer.persistence.entity.PlaylistEntity;
import com.songplayer.persistence.entity.PlaylistItemEntity;
import com.songplayer.persistence.entity.SongEntity;
import org.springframework.stereotype.Component;

/** Maps playlist JPA entities to API response DTOs. */
@Component
public class PlaylistMapper {
    public PlaylistResponse toResponse(PlaylistEntity playlist) {
        return new PlaylistResponse(playlist.getId(), playlist.getName(), playlist.getDescription(), playlist.getCoverImageUrl(),
                playlist.getItems().stream().map(this::toItemResponse).toList(), playlist.getCreatedAt(), playlist.getUpdatedAt());
    }

    private PlaylistItemResponse toItemResponse(PlaylistItemEntity item) {
        return new PlaylistItemResponse(item.getId(), toSongResponse(item.getSong()), item.getPosition());
    }

    public SongResponse toSongResponse(SongEntity song) {
        AlbumResponse albumResponse = song.getAlbum() != null
                ? new AlbumResponse(song.getAlbum().getId(), song.getAlbum().getTitle(), song.getAlbum().getReleaseYear())
                : null;
        return new SongResponse(song.getId(), song.getTitle(),
                song.getArtists().stream().map(artist -> new ArtistResponse(artist.getId(), artist.getName())).toList(),
                song.getGenres().stream().map(GenreEntity::getName).toList(),
                song.getDurationSeconds(), song.getUri(), albumResponse);
    }
}
