package com.example.tracklytics.track;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TrackRepository extends JpaRepository<Track, Integer> {

    Optional<Track> findBySpotifyId(String spotifyId);
}
