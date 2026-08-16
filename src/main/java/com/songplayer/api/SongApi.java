package com.songplayer.api;

import com.songplayer.api.dto.SongResponse;
import com.songplayer.api.error.ApiError;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import java.util.UUID;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

/** Read-only access to the music catalog. */
@Tag(name = "Songs", description = "Music catalog — discovery and lookup")
@RequestMapping("/api/v1/songs")
public interface SongApi {

    @Operation(summary = "List all catalog songs")
    @ApiResponse(responseCode = "200", description = "Catalog songs returned successfully")
    @SecurityRequirements
    @GetMapping
    List<SongResponse> list();

    @Operation(summary = "Get a catalog song by ID")
    @ApiResponse(responseCode = "200", description = "Song found")
    @ApiResponse(responseCode = "404", description = "Song not found", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiError.class)))
    @SecurityRequirements
    @GetMapping("/{songId}")
    SongResponse getById(@PathVariable UUID songId);
}
