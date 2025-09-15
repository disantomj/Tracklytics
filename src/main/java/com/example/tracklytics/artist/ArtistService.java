package com.example.tracklytics.artist;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.Optional;

@Service
public class ArtistService {

    @Autowired
    private final ArtistRepository artistRepository;

    public ArtistService(ArtistRepository artistRepository) {
        this.artistRepository = artistRepository;
    }

    public Optional<Artist> findBySpotifyId(String spotifyId) {
        return artistRepository.findBySpotifyId(spotifyId);
    }

    public Artist saveArtist(Artist artist) {
        return artistRepository.save(artist);
    }

    public boolean existsBySpotifyId(String spotifyId) {
        return artistRepository.findBySpotifyId(spotifyId).isPresent();
    }

}



