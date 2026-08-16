package com.songplayer.api.controller;

import com.songplayer.api.SongApi;
import com.songplayer.api.dto.SongResponse;
import com.songplayer.application.SongService;
import java.util.List;
import java.util.UUID;
import org.springframework.web.bind.annotation.RestController;

/** HTTP adapter for song catalog endpoints. */
@RestController
public class SongController implements SongApi {
    private final SongService songService;

    public SongController(SongService songService) {
        this.songService = songService;
    }

    @Override public List<SongResponse> list()                  { return songService.list(); }
    @Override public SongResponse getById(UUID songId)          { return songService.getById(songId); }
}
