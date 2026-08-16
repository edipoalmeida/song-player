package com.songplayer.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.hibernate.annotations.UuidGenerator;

/** Persisted catalog artist. */
@Entity
@Table(name = "artists")
public class ArtistEntity {
    @Id @GeneratedValue @UuidGenerator
    private UUID id;

    @Column(nullable = false, unique = true, length = 200)
    private String name;

    @OneToMany(mappedBy = "artist", fetch = FetchType.LAZY)
    @SuppressWarnings("FieldMayBeFinal") // must not be final — Hibernate replaces this with a proxy at runtime
    private List<AlbumEntity> albums = new ArrayList<>();

    protected ArtistEntity() {
    }

    public ArtistEntity(String name) {
        this.name = name;
    }

    public UUID getId() { return id; }
    public String getName() { return name; }
    public List<AlbumEntity> getAlbums() { return albums; }
}
