package com.songplayer.persistence.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UuidGenerator;

/** Persisted playlist with ordered items and audit timestamps. */
@Entity
@Table(name = "playlists")
public class PlaylistEntity {
    @Id @GeneratedValue @UuidGenerator
    private UUID id;

    @Column(nullable = false, length = 120)
    private String name;

    @Column(length = 1_000)
    private String description;

    @Column(name = "cover_image_url", length = 2_048)
    private String coverImageUrl;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @OneToMany(mappedBy = "playlist", cascade = {CascadeType.PERSIST, CascadeType.MERGE}, orphanRemoval = true)
    @OrderBy("position ASC")
    private List<PlaylistItemEntity> items = new ArrayList<>();

    protected PlaylistEntity() {
    }

    public PlaylistEntity(String name, String description, String coverImageUrl) {
        this.name = name;
        this.description = description;
        this.coverImageUrl = coverImageUrl;
        this.updatedAt = Instant.now();
    }

    public void updateMetadata(String name, String description, String coverImageUrl) {
        this.name = name;
        this.description = description;
        this.coverImageUrl = coverImageUrl;
        touch();
    }

    public void addItem(PlaylistItemEntity item) {
        items.add(item);
    }

    public boolean removeItem(UUID playlistItemId) {
        return items.removeIf(item -> item.getId().equals(playlistItemId));
    }

    public void touch() {
        this.updatedAt = Instant.now();
    }

    public UUID getId() { return id; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public String getCoverImageUrl() { return coverImageUrl; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public List<PlaylistItemEntity> getItems() { return items; }
}
