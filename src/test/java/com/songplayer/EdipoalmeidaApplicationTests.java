package com.songplayer;

import static org.junit.jupiter.api.Assertions.assertTrue;

import com.songplayer.persistence.repository.SongRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class SongPlayerApplicationTests {

    @Autowired
    private SongRepository songRepository;

    @Test
    void contextLoads() {
    }

    @Test
    void catalogSeedIsAvailable() {
        assertTrue(songRepository.count() >= 6);
    }

}
