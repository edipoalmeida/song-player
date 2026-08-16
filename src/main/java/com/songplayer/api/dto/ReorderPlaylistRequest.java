package com.songplayer.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import java.util.List;
import java.util.UUID;

/** Request body for replacing playlist item positions. */
@Schema(description = "Request body for reordering all items in a playlist")
public record ReorderPlaylistRequest(
        @Schema(description = "Complete list of item positions. Every item currently in the playlist must be included.")
        @NotEmpty List<@Valid ItemPosition> items
) {
    @Schema(description = "Associates a playlist item with its desired zero-based position")
    public record ItemPosition(
            @Schema(description = "ID of the playlist item", example = "00000000-0000-0000-0000-000000000003")
            @NotNull UUID playlistItemId,

            @Schema(description = "Zero-based target position", example = "0", minimum = "0")
            @PositiveOrZero int position
    ) {
    }
}
