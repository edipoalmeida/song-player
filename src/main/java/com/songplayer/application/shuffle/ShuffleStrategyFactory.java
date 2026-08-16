package com.songplayer.application.shuffle;

import com.songplayer.domain.ShuffleMode;
import org.springframework.stereotype.Component;

/** Maps a {@link ShuffleMode} to the corresponding {@link ShuffleStrategy} bean. */
@Component
public class ShuffleStrategyFactory {
    private final RandomShuffleStrategy random;
    private final SmartShuffleStrategy smart;
    private final GenreBalancedShuffleStrategy genreBalanced;

    public ShuffleStrategyFactory(RandomShuffleStrategy random, SmartShuffleStrategy smart,
                                  GenreBalancedShuffleStrategy genreBalanced) {
        this.random = random;
        this.smart = smart;
        this.genreBalanced = genreBalanced;
    }

    public ShuffleStrategy forMode(ShuffleMode mode) {
        return switch (mode) {
            case RANDOM -> random;
            case SMART -> smart;
            case GENRE_BALANCED -> genreBalanced;
        };
    }
}
