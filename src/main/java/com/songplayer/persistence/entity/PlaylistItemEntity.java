package com.songplayer.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.util.UUID;
import org.hibernate.annotations.UuidGenerator;

/** Persisted song occurrence within a playlist. */
@Entity
@Table(name = "playlist_items", uniqueConstraints = @UniqueConstraint(name = "uk_playlist_item_position", columnNames = {"playlist_id", "position"}))
public class PlaylistItemEntity {
    @Id @GeneratedValue @UuidGenerator
    private UUID id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "playlist_id", nullable = false)
    private PlaylistEntity playlist;

    @ManyToOne(optional = false)
    @JoinColumn(name = "song_id", nullable = false)
    private SongEntity song;

    @Column(nullable = false)
    private int position;

    protected PlaylistItemEntity() {
    }

    public PlaylistItemEntity(PlaylistEntity playlist, SongEntity song, int position) {
        this.playlist = playlist;
        this.song = song;
        this.position = position;
    }

    public void changePosition(int position) {
        this.position = position;
    }

    public UUID getId() { return id; }
    public PlaylistEntity getPlaylist() { return playlist; }
    public SongEntity getSong() { return song; }
    public int getPosition() { return position; }
}
