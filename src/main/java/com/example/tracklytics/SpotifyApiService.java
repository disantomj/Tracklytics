package com.example.tracklytics;

import com.example.tracklytics.user.User;
import com.example.tracklytics.user.UserService;
import com.example.tracklytics.artist.ArtistService;
import com.example.tracklytics.track.TrackService;
import com.example.tracklytics.artist.Artist;
import com.example.tracklytics.track.Track;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class SpotifyApiService {

    private final RestTemplate restTemplate = new RestTemplate();
    private final TrackService trackService;
    private final ArtistService artistService;
    private final UserService userService;

    public SpotifyApiService(TrackService trackService, ArtistService artistService, UserService userService) {
        this.trackService = trackService;
        this.artistService = artistService;
        this.userService = userService;
    }

    @Transactional
    public void fetchAndSaveUserTopTracks(User user) {
        String url = "https://api.spotify.com/v1/me/top/tracks?limit=50";

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(user.getAccessToken());
        HttpEntity<String> entity = new HttpEntity<>(headers);

        try {
            ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.GET, entity, Map.class);
            Map<String, Object> responseBody = response.getBody();

            if (responseBody != null && responseBody.containsKey("items")) {
                List<Map<String, Object>> tracks = (List<Map<String, Object>>) responseBody.get("items");

                // Build new track set
                Set<Track> newTopTracks = new HashSet<>();
                for (Map<String, Object> trackData : tracks) {
                    Track track = saveTrackFromSpotifyData(trackData);
                    if (track != null) {
                        newTopTracks.add(track);
                    }
                }

                // Update user's tracks and save
                user.updateTopTracks(newTopTracks);
                user.setLastSyncTime(Instant.now());
                userService.saveUser(user);

                System.out.println("Saved " + tracks.size() + " tracks for user: " + user.getDisplayName());
            }
        } catch (Exception e) {
            System.err.println("Error fetching top tracks: " + e.getMessage());
            throw new RuntimeException("Failed to fetch top tracks", e);
        }
    }

    @Transactional
    public void fetchAndSaveUserTopArtists(User user) {
        String url = "https://api.spotify.com/v1/me/top/artists?limit=50";

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(user.getAccessToken());
        HttpEntity<String> entity = new HttpEntity<>(headers);

        try {
            ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.GET, entity, Map.class);
            Map<String, Object> responseBody = response.getBody();

            if (responseBody != null && responseBody.containsKey("items")) {
                List<Map<String, Object>> artists = (List<Map<String, Object>>) responseBody.get("items");

                // Build new artist set
                Set<Artist> newTopArtists = new HashSet<>();
                for (Map<String, Object> artistData : artists) {
                    Artist artist = saveArtistFromSpotifyData(artistData);
                    if (artist != null) {
                        newTopArtists.add(artist);
                    }
                }

                // Update user's artists and save
                user.updateTopArtists(newTopArtists);
                user.setLastSyncTime(Instant.now());
                userService.saveUser(user);

                System.out.println("Saved " + artists.size() + " artists for user: " + user.getDisplayName());
            }
        } catch (Exception e) {
            System.err.println("Error fetching top artists: " + e.getMessage());
            throw new RuntimeException("Failed to fetch top artists", e);
        }
    }

    private Track saveTrackFromSpotifyData(Map<String, Object> trackData) {
        String spotifyId = (String) trackData.get("id");
        String name = (String) trackData.get("name");
        Integer durationMs = (Integer) trackData.get("duration_ms");
        Integer popularity = (Integer) trackData.get("popularity");

        // Get album info
        Map<String, Object> album = (Map<String, Object>) trackData.get("album");
        String albumName = album != null ? (String) album.get("name") : null;

        // Check if track already exists, if not create it
        Optional<Track> existingTrack = trackService.findBySpotifyId(spotifyId);

        Track track;
        if (existingTrack.isPresent()) {
            track = existingTrack.get();
            // Update with latest info
            track.setName(name);
            track.setAlbumName(albumName);
            track.setDurationMs(durationMs);
            track.setPopularity(popularity);
        } else {
            track = new Track(spotifyId, name, albumName);
            track.setDurationMs(durationMs);
            track.setPopularity(popularity);
        }

        return trackService.saveTrack(track);
    }

    private Artist saveArtistFromSpotifyData(Map<String, Object> artistData) {
        String spotifyId = (String) artistData.get("id");
        String name = (String) artistData.get("name");
        Integer popularity = (Integer) artistData.get("popularity");

        // Get genres
        List<String> genreList = (List<String>) artistData.get("genres");
        String genres = genreList != null ? String.join(",", genreList) : null;

        // Check if artist already exists, if not create it
        Optional<Artist> existingArtist = artistService.findBySpotifyId(spotifyId);

        Artist artist;
        if (existingArtist.isPresent()) {
            artist = existingArtist.get();
            // Update with latest info
            artist.setName(name);
            artist.setPopularity(popularity);
            artist.setGenres(genres);
        } else {
            artist = new Artist(spotifyId, name);
            artist.setPopularity(popularity);
            artist.setGenres(genres);
        }

        return artistService.saveArtist(artist);
    }

    public void fetchAndSaveTrackAudioFeatures(User user) {
        // Get track IDs from user's top tracks
        List<String> trackIds = user.getTopTracks().stream()
                .map(track -> track.getSpotifyId())
                .collect(Collectors.toList());

        if (trackIds.isEmpty()) {
            System.out.println("No tracks to analyze for audio features");
            return;
        }

        // Test with just one track ID first
        String testTrackId = trackIds.get(0);
        String url = "https://api.spotify.com/v1/audio-features/" + testTrackId;

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(user.getAccessToken());
        HttpEntity<String> entity = new HttpEntity<>(headers);

        try {
            ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.GET, entity, Map.class);
            System.out.println("Audio features response: " + response.getBody());
        } catch (Exception e) {
            System.err.println("Error fetching audio features: " + e.getMessage());
        }
    }
}
