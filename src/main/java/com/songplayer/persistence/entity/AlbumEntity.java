package com.songplayer.persistence.entity;

import jakarta.persistence.*;
import java.util.UUID;
import org.hibernate.annotations.UuidGenerator;

/** Persisted album for catalog songs. */
@Entity
@Table(name = "albums")
public class AlbumEntity {
    @Id @GeneratedValue @UuidGenerator
    private UUID id;

    @Column(nullable = false, length = 300)
    private String title;

    @Column(name = "release_year", nullable = false)
    private int releaseYear;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "artist_id", nullable = false)
    private ArtistEntity artist;

    protected AlbumEntity() {}

    public AlbumEntity(String title, int releaseYear, ArtistEntity artist) {
        this.title = title;
        this.releaseYear = releaseYear;
        this.artist = artist;
    }

    public UUID getId() { return id; }
    public String getTitle() { return title; }
    public int getReleaseYear() { return releaseYear; }
    public ArtistEntity getArtist() { return artist; }
}
