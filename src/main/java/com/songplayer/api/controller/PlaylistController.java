package com.songplayer.api.controller;

import com.songplayer.api.PlaylistApi;
import com.songplayer.api.dto.AddSongRequest;
import com.songplayer.api.dto.AddSongsFromArtistRequest;
import com.songplayer.api.dto.AddSongsRequest;
import com.songplayer.api.dto.CreatePlaylistRequest;
import com.songplayer.api.dto.PlaylistResponse;
import com.songplayer.api.dto.RecommendationResponse;
import com.songplayer.api.dto.ReorderPlaylistRequest;
import com.songplayer.api.dto.UpdatePlaylistRequest;
import com.songplayer.application.PlaylistService;
import com.songplayer.application.export.ExportResult;
import com.songplayer.domain.ShuffleMode;
import java.net.URI;
import java.util.List;
import java.util.UUID;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

/** HTTP adapter for playlist operations. */
@RestController
public class PlaylistController implements PlaylistApi {
    private final PlaylistService playlistService;

    public PlaylistController(PlaylistService playlistService) {
        this.playlistService = playlistService;
    }

    @Override public ResponseEntity<PlaylistResponse> create(CreatePlaylistRequest request) {
        PlaylistResponse body = playlistService.create(request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}").buildAndExpand(body.id()).toUri();
        return ResponseEntity.created(location).body(body);
    }
    @Override public List<PlaylistResponse> list() { return playlistService.list(); }
    @Override public PlaylistResponse getById(UUID playlistId) { return playlistService.getById(playlistId); }
    @Override public PlaylistResponse update(UUID playlistId, UpdatePlaylistRequest request) { return playlistService.update(playlistId, request); }
    @Override public void delete(UUID playlistId) { playlistService.delete(playlistId); }
    @Override public ResponseEntity<PlaylistResponse> addSong(UUID playlistId, AddSongRequest request) {
        PlaylistResponse body = playlistService.addSong(playlistId, request);
        UUID itemId = body.items().get(body.items().size() - 1).id();
        URI location = ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("/api/v1/playlists/{playlistId}/items/{itemId}")
                .buildAndExpand(playlistId, itemId).toUri();
        return ResponseEntity.created(location).body(body);
    }
    @Override public ResponseEntity<PlaylistResponse> addSongs(UUID playlistId, AddSongsRequest request) {
        PlaylistResponse body = playlistService.addSongs(playlistId, request);
        URI location = ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("/api/v1/playlists/{playlistId}")
                .buildAndExpand(playlistId).toUri();
        return ResponseEntity.created(location).body(body);
    }

    @Override public ResponseEntity<PlaylistResponse> addSongsFromArtist(UUID playlistId, AddSongsFromArtistRequest request) {
        PlaylistResponse body = playlistService.addSongsFromArtist(playlistId, request);
        URI location = ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("/api/v1/playlists/{playlistId}")
                .buildAndExpand(playlistId).toUri();
        return ResponseEntity.created(location).body(body);
    }

    @Override public void removeSong(UUID playlistId, UUID playlistItemId) { playlistService.removeSong(playlistId, playlistItemId); }
    @Override public PlaylistResponse reorder(UUID playlistId, ReorderPlaylistRequest request) { return playlistService.reorder(playlistId, request); }

    @Override public List<UUID> queue(UUID playlistId, ShuffleMode strategy) { return playlistService.queue(playlistId, strategy); }
    @Override public RecommendationResponse recommendations(UUID playlistId, int limit) { return playlistService.recommendations(playlistId, limit); }
    @Override public ResponseEntity<Object> export(UUID playlistId, String format) {
        ExportResult result = playlistService.export(playlistId, format);
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(result.contentType()))
                .body(result.body());
    }
}
