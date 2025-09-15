package com.example.tracklytics.artist;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

@RestController
@RequestMapping("/api/artists")
public class ArtistController {

    @Autowired
    private ArtistService artistService;

    @GetMapping("/{spotifyId}")
    public Optional<Artist> findBySpotifyId(@PathVariable String spotifyId) {
        return artistService.findBySpotifyId(spotifyId);
    }
}
