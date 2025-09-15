package com.example.tracklytics.user;

import com.example.tracklytics.artist.Artist;
import com.example.tracklytics.track.Track;
import jakarta.persistence.*;

import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, unique = true)
    private String spotifyId;

    private String displayName;
    private String email;

    @Column(length = 500)
    private String accessToken;

    @Column(length = 500)
    private String refreshToken;

    private Instant tokenExpiry;

    @ManyToMany(fetch = FetchType.EAGER, cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinTable(
            name = "user_top_tracks",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "track_id")
    )
    private Set<Track> topTracks = new HashSet<>();

    @ManyToMany(fetch = FetchType.EAGER, cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinTable(
            name = "user_top_artists",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "artist_id")
    )
    private Set<Artist> topArtists = new HashSet<>();

    private Instant lastSyncTime;

    // private String profileImageUrl;

    public User() {}

    public User(String spotifyId, String displayName, String email) {
        this.spotifyId = spotifyId;
        this.displayName = displayName;
        this.email = email;
    }

    public Integer getId() {
        return id;
    }

    public String getSpotifyId() {
        return spotifyId;
    }

    public void setSpotifyId(String spotifyId) {
        this.spotifyId = spotifyId;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    public Instant getTokenExpiry() {
        return tokenExpiry;
    }

    public void setTokenExpiry(Instant tokenExpiry) {
        this.tokenExpiry = tokenExpiry;
    }

    public Set<Track> getTopTracks() {
        return topTracks;
    }

    public void setTopTracks(Set<Track> topTracks) {
        this.topTracks = topTracks;
    }

    public Set<Artist> getTopArtists() {
        return topArtists;
    }

    public void setTopArtists(Set<Artist> topArtists) {
        this.topArtists = topArtists;
    }

    public Instant getLastSyncTime() {
        return lastSyncTime;
    }

    public void setLastSyncTime(Instant lastSyncTime) {
        this.lastSyncTime = lastSyncTime;
    }

    // Helper methods for managing relationships
    public void addTopTrack(Track track) {
        topTracks.add(track);
    }

    public void addTopArtist(Artist artist) {
        topArtists.add(artist);
    }

    public void clearTopTracks() {
        topTracks.clear();
    }

    public void clearTopArtists() {
        topArtists.clear();
    }

    public void updateTopTracks(Set<Track> newTracks) {
        this.topTracks.clear();
        this.topTracks.addAll(newTracks);
    }

    public void updateTopArtists(Set<Artist> newArtists) {
        this.topArtists.clear();
        this.topArtists.addAll(newArtists);
    }
}