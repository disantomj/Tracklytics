package com.example.tracklytics.user;

import com.example.tracklytics.artist.Artist;
import com.example.tracklytics.track.Track;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

@RestController
@RequestMapping("/api")
public class UserController {

    @Autowired
    private UserService userService;

    // Get authenticated user's top tracks
    @GetMapping("/top-tracks")
    public ResponseEntity<?> getUserTopTracks(@AuthenticationPrincipal OAuth2User principal) {
        if (principal == null) {
            return ResponseEntity.status(401).body(Map.of("error", "Not authenticated"));
        }

        String spotifyId = principal.getAttribute("id");
        Optional<User> userOpt = userService.findBySpotifyId(spotifyId);

        if (userOpt.isEmpty()) {
            return ResponseEntity.status(404).body(Map.of("error", "User not found"));
        }

        User user = userOpt.get();
        Set<Track> topTracks = user.getTopTracks();

        Map<String, Object> response = new HashMap<>();
        response.put("user", user.getDisplayName());
        response.put("trackCount", topTracks.size());
        response.put("tracks", topTracks);
        response.put("lastSync", user.getLastSyncTime());

        return ResponseEntity.ok(response);
    }

    // Get authenticated user's top artists
    @GetMapping("/top-artists")
    public ResponseEntity<?> getUserTopArtists(@AuthenticationPrincipal OAuth2User principal) {
        if (principal == null) {
            return ResponseEntity.status(401).body(Map.of("error", "Not authenticated"));
        }

        String spotifyId = principal.getAttribute("id");
        Optional<User> userOpt = userService.findBySpotifyId(spotifyId);

        if (userOpt.isEmpty()) {
            return ResponseEntity.status(404).body(Map.of("error", "User not found"));
        }

        User user = userOpt.get();
        Set<Artist> topArtists = user.getTopArtists();

        Map<String, Object> response = new HashMap<>();
        response.put("user", user.getDisplayName());
        response.put("artistCount", topArtists.size());
        response.put("artists", topArtists);
        response.put("lastSync", user.getLastSyncTime());

        return ResponseEntity.ok(response);
    }

    // Get user's music summary
    @GetMapping("/music-summary")
    public ResponseEntity<?> getMusicSummary(@AuthenticationPrincipal OAuth2User principal) {
        if (principal == null) {
            return ResponseEntity.status(401).body(Map.of("error", "Not authenticated"));
        }

        String spotifyId = principal.getAttribute("id");
        Optional<User> userOpt = userService.findBySpotifyId(spotifyId);

        if (userOpt.isEmpty()) {
            return ResponseEntity.status(404).body(Map.of("error", "User not found"));
        }

        User user = userOpt.get();

        Map<String, Object> summary = new HashMap<>();
        summary.put("user", user.getDisplayName());
        summary.put("totalTracks", user.getTopTracks().size());
        summary.put("totalArtists", user.getTopArtists().size());
        summary.put("lastSync", user.getLastSyncTime());
        summary.put("hasData", !user.getTopTracks().isEmpty() || !user.getTopArtists().isEmpty());

        return ResponseEntity.ok(summary);
    }

    // Public endpoint to get specific user's data (by spotifyId)
    @GetMapping("/users/{spotifyId}/top-tracks")
    public ResponseEntity<?> getPublicUserTopTracks(@PathVariable String spotifyId) {
        Optional<User> userOpt = userService.findBySpotifyId(spotifyId);

        if (userOpt.isEmpty()) {
            return ResponseEntity.status(404).body(Map.of("error", "User not found"));
        }

        User user = userOpt.get();
        Map<String, Object> response = new HashMap<>();
        response.put("user", user.getDisplayName());
        response.put("trackCount", user.getTopTracks().size());
        response.put("tracks", user.getTopTracks());

        return ResponseEntity.ok(response);
    }

    @GetMapping("/users/{spotifyId}/top-artists")
    public ResponseEntity<?> getPublicUserTopArtists(@PathVariable String spotifyId) {
        Optional<User> userOpt = userService.findBySpotifyId(spotifyId);

        if (userOpt.isEmpty()) {
            return ResponseEntity.status(404).body(Map.of("error", "User not found"));
        }

        User user = userOpt.get();
        Map<String, Object> response = new HashMap<>();
        response.put("user", user.getDisplayName());
        response.put("artistCount", user.getTopArtists().size());
        response.put("artists", user.getTopArtists());

        return ResponseEntity.ok(response);
    }
}
