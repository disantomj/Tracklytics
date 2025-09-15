package com.example.tracklytics.track;

import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class TrackService {

    private final TrackRepository trackRepository;

    public TrackService(TrackRepository trackRepository) {
        this.trackRepository = trackRepository;
    }

    public Optional<Track> findBySpotifyId(String spotifyId) {
        return trackRepository.findBySpotifyId(spotifyId);
    }

    public Track saveTrack(Track track) {
        return trackRepository.save(track);
    }

    public boolean existsBySpotifyId(String spotifyId) {
        return trackRepository.findBySpotifyId(spotifyId).isPresent();
    }
}
