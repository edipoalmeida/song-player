CREATE TABLE artists (
    id UUID PRIMARY KEY,
    name VARCHAR(200) NOT NULL UNIQUE
);

CREATE TABLE albums (
    id UUID PRIMARY KEY,
    title VARCHAR(300) NOT NULL,
    release_year INTEGER NOT NULL CHECK (release_year > 0),
    artist_id UUID NOT NULL REFERENCES artists(id) ON DELETE RESTRICT
);

CREATE TABLE genres (
    id UUID PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE
);

CREATE TABLE songs (
    id UUID PRIMARY KEY,
    title VARCHAR(300) NOT NULL,
    duration_seconds INTEGER NOT NULL CHECK (duration_seconds > 0),
    album_id UUID REFERENCES albums(id) ON DELETE SET NULL,
    uri VARCHAR(2048) NOT NULL UNIQUE
);

CREATE TABLE song_artists (
    song_id UUID NOT NULL REFERENCES songs(id) ON DELETE CASCADE,
    artist_id UUID NOT NULL REFERENCES artists(id) ON DELETE RESTRICT,
    PRIMARY KEY (song_id, artist_id)
);

CREATE TABLE song_genres (
    song_id UUID NOT NULL REFERENCES songs(id) ON DELETE CASCADE,
    genre_id UUID NOT NULL REFERENCES genres(id) ON DELETE RESTRICT,
    PRIMARY KEY (song_id, genre_id)
);

CREATE TABLE playlists (
    id UUID PRIMARY KEY,
    name VARCHAR(120) NOT NULL,
    description VARCHAR(1000),
    cover_image_url VARCHAR(2048),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE playlist_items (
    id UUID PRIMARY KEY,
    playlist_id UUID NOT NULL REFERENCES playlists(id) ON DELETE CASCADE,
    song_id UUID NOT NULL REFERENCES songs(id) ON DELETE RESTRICT,
    position INTEGER NOT NULL CHECK (position >= 0),
    CONSTRAINT uk_playlist_item_position UNIQUE (playlist_id, position)
);

CREATE INDEX idx_songs_title ON songs (title);
CREATE INDEX idx_song_artists_artist_id ON song_artists (artist_id);
CREATE INDEX idx_song_genres_genre_id ON song_genres (genre_id);
CREATE INDEX idx_playlist_items_playlist_position ON playlist_items (playlist_id, position);
CREATE INDEX idx_albums_artist_id ON albums (artist_id);
CREATE INDEX idx_songs_album_id ON songs (album_id);
