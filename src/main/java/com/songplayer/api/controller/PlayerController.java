package com.songplayer.api.controller;

import com.songplayer.api.PlayerApi;
import com.songplayer.api.dto.PlaybackStateResponse;
import com.songplayer.application.PlayerService;
import com.songplayer.domain.ShuffleMode;
import java.util.UUID;
import org.springframework.web.bind.annotation.RestController;

/** HTTP adapter for the global player. */
@RestController
public class PlayerController implements PlayerApi {
    private final PlayerService playerService;

    public PlayerController(PlayerService playerService) {
        this.playerService = playerService;
    }

    @Override public PlaybackStateResponse state()                                              { return playerService.state(); }
    @Override public PlaybackStateResponse playPlaylist(UUID playlistId, ShuffleMode strategy)  { return playerService.playPlaylist(playlistId, strategy); }
    @Override public PlaybackStateResponse play()                                               { return playerService.play(); }
    @Override public PlaybackStateResponse pause()                                              { return playerService.pause(); }
    @Override public PlaybackStateResponse stop()                                               { return playerService.stop(); }
    @Override public PlaybackStateResponse next()                                               { return playerService.next(); }
    @Override public PlaybackStateResponse previous()                                           { return playerService.previous(); }
    @Override public PlaybackStateResponse seek(Integer positionSeconds)                        { return playerService.seek(positionSeconds); }
}
