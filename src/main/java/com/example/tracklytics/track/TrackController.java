package com.example.tracklytics.track;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

@RestController
@RequestMapping("/api/tracks")
public class TrackController {

    @Autowired
    private TrackService trackService;

    @GetMapping("/{spotifyId}")
    public Optional<Track> getTrack(@PathVariable String spotifyId) {
        return trackService.findBySpotifyId(spotifyId);
    }
}