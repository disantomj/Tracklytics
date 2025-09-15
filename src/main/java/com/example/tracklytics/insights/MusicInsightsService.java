package com.example.tracklytics.insights;

import com.example.tracklytics.user.User;
import org.springframework.stereotype.Service;
import java.util.*;
import org.springframework.beans.factory.annotation.Autowired;
import java.util.List;

@Service
public class MusicInsightsService {

    public MusicPersonality analyzeMusicPersonality(User user) {
        MusicPersonality personality = new MusicPersonality();

        // Existing analysis
        Set<String> allGenres = extractAllGenres(user);
        int diversityScore = calculateDiversityScore(allGenres.size());
        int mainstreamScore = calculateMainstreamScore(user);
        String listeningMood = analyzeMoodFromGenres(allGenres);
        String artistLoyalty = analyzeArtistLoyalty(user);
        String trackLengthPreference = analyzeTrackLengthPreference(user);

        // Set all the values
        personality.setDiversityScore(diversityScore);
        personality.setMainstreamScore(mainstreamScore);
        personality.setListeningMood(listeningMood);
        personality.setArtistLoyalty(artistLoyalty);
        personality.setTrackLengthPreference(trackLengthPreference);

        String personalityType = determinePersonalityType(diversityScore, mainstreamScore);
        String description = generateDescription(personalityType, allGenres.size(), mainstreamScore);

        personality.setPrimaryPersonality(personalityType);
        personality.setDescription(description);

        return personality;
    }

    private int calculateMainstreamScore(User user) {
        if (user.getTopTracks().isEmpty()) return 50;

        double totalPopularity = user.getTopTracks().stream()
                .mapToInt(track -> track.getPopularity() != null ? track.getPopularity() : 50)
                .average()
                .orElse(50.0);

        return (int) Math.round(totalPopularity);
    }

    private String determinePersonalityType(int diversityScore, int mainstreamScore) {
        if (diversityScore >= 80 && mainstreamScore >= 70) {
            return "The Mainstream Explorer";
        } else if (diversityScore >= 80 && mainstreamScore < 50) {
            return "The Underground Adventurer";
        } else if (diversityScore >= 65 && mainstreamScore >= 70) {
            return "The Popular Wanderer";
        } else if (diversityScore >= 65 && mainstreamScore < 50) {
            return "The Indie Explorer";
        } else if (diversityScore < 40 && mainstreamScore >= 70) {
            return "The Chart Loyalist";
        } else if (diversityScore < 40 && mainstreamScore < 50) {
            return "The Niche Enthusiast";
        } else {
            return "The Balanced Listener";
        }
    }

    private String generateDescription(String personalityType, int genreCount, int mainstreamScore) {
        switch (personalityType) {
            case "The Mainstream Explorer":
                return "You love popular hits across " + genreCount + " genres. You're always up-to-date with what's trending while keeping an open mind!";
            case "The Underground Adventurer":
                return "You're a musical detective, discovering hidden gems across " + genreCount + " genres. Your playlists are full of tracks others haven't found yet.";
            case "The Chart Loyalist":
                return "You know what's hot and you love it! You stick to the hits in your favorite genres and you're never behind on the latest popular songs.";
            case "The Niche Enthusiast":
                return "You've found your musical home in specialized corners of " + genreCount + " genres. Your taste is unique and deeply personal.";
            case "The Hidden Gem Hunter":
                return "You have a sixth sense for finding incredible music before it goes mainstream. Your friends come to you for recommendations.";
            default:
                return "You have a well-rounded musical taste spanning " + genreCount + " genres with a mix of popular and underground tracks.";
        }
    }


    private Set<String> extractAllGenres(User user) {
        Set<String> genres = new HashSet<>();

        user.getTopArtists().forEach(artist -> {
            if (artist.getGenres() != null && !artist.getGenres().isEmpty()) {
                String[] artistGenres = artist.getGenres().split(",");
                for (String genre : artistGenres) {
                    genres.add(genre.trim().toLowerCase());
                }
            }
        });

        return genres;
    }

    private int calculateDiversityScore(int genreCount) {
        // Much tougher scoring - really rewards exceptional diversity
        if (genreCount <= 1) return 5;
        if (genreCount <= 3) return 15;
        if (genreCount <= 6) return 25;
        if (genreCount <= 10) return 35;
        if (genreCount <= 15) return 50;
        if (genreCount <= 25) return 65;
        if (genreCount <= 40) return 80;
        if (genreCount <= 60) return 95;
        return 100; // Only 60+ genres gets perfect score
    }

    public String analyzeMoodFromGenres(Set<String> genres) {
        int energeticCount = 0;
        int chillCount = 0;
        int emotionalCount = 0;

        for (String genre : genres) {
            String lowerGenre = genre.toLowerCase();

            // Energetic genres
            if (lowerGenre.contains("rock") || lowerGenre.contains("punk") ||
                    lowerGenre.contains("metal") || lowerGenre.contains("dance") ||
                    lowerGenre.contains("electronic")) {
                energeticCount++;
            }

            // Chill genres
            if (lowerGenre.contains("ambient") || lowerGenre.contains("jazz") ||
                    lowerGenre.contains("acoustic") || lowerGenre.contains("folk")) {
                chillCount++;
            }

            // Emotional genres
            if (lowerGenre.contains("indie") || lowerGenre.contains("alternative") ||
                    lowerGenre.contains("singer-songwriter") || lowerGenre.contains("blues")) {
                emotionalCount++;
            }
        }

        if (energeticCount > chillCount && energeticCount > emotionalCount) {
            return "High Energy Listener";
        } else if (chillCount > energeticCount && chillCount > emotionalCount) {
            return "Chill Vibes Seeker";
        } else if (emotionalCount > 0) {
            return "Emotional Journey Listener";
        } else {
            return "Eclectic Mood Explorer";
        }
    }

    public String analyzeArtistLoyalty(User user) {
        if (user.getTopArtists().isEmpty()) return "Unknown";

        int totalArtists = user.getTopArtists().size();
        int totalTracks = user.getTopTracks().size();

        if (totalTracks == 0) return "Unknown";

        // Calculate how many tracks per artist on average
        double tracksPerArtist = (double) totalTracks / totalArtists;

        if (tracksPerArtist >= 10.0) {
            return "Deep Diver";
        } else if (tracksPerArtist >= 6.0) {
            return "Focused Fan";
        } else if (tracksPerArtist >= 3.0) {
            return "Balanced Explorer";
        } else {
            return "Variety Seeker";
        }
    }

    public String analyzeTrackLengthPreference(User user) {
        if (user.getTopTracks().isEmpty()) return "Unknown";

        double averageDuration = user.getTopTracks().stream()
                .filter(track -> track.getDurationMs() != null)
                .mapToInt(track -> track.getDurationMs())
                .average()
                .orElse(0.0);

        // Convert to minutes
        double avgMinutes = averageDuration / 60000.0;

        if (avgMinutes >= 5.0) {
            return "Epic Journey Lover";
        } else if (avgMinutes >= 4.0) {
            return "Extended Experience Fan";
        } else if (avgMinutes >= 3.0) {
            return "Standard Length Listener";
        } else {
            return "Quick Hit Enthusiast";
        }
    }

    @Autowired
    private PersonalityHistoryRepository personalityHistoryRepository;

    public MusicPersonality analyzeMusicPersonalityAndSaveHistory(User user) {
        MusicPersonality personality = analyzeMusicPersonality(user);

        // Always save - used for weekly snapshots
        PersonalityHistory history = new PersonalityHistory(user, personality);
        personalityHistoryRepository.save(history);

        return personality;
    }

    public List<PersonalityHistory> getPersonalityHistory(User user) {
        return personalityHistoryRepository.findByUserOrderByAnalyzedAtDesc(user);
    }

    public MusicPersonality analyzeMusicPersonalityOnly(User user) {
        return analyzeMusicPersonality(user);
    }

}