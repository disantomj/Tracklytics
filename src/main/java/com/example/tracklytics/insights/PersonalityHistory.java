package com.example.tracklytics.insights;

import com.example.tracklytics.user.User;
import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name= "personality_history")
public class PersonalityHistory {

    @Id
    @GeneratedValue
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    private Instant analyzedAt;

    private String primaryPersonality;
    private int diversityScore;
    private int mainstreamScore;
    private String listeningMood;
    private String artistLoyalty;
    private String trackLengthPreference;

    // Constructors
    public PersonalityHistory() {}

    public PersonalityHistory(User user, MusicPersonality personality) {
        this.user = user;
        this.analyzedAt = Instant.now();
        this.primaryPersonality = personality.getPrimaryPersonality();
        this.diversityScore = personality.getDiversityScore();
        this.mainstreamScore = personality.getMainstreamScore();
        this.listeningMood = personality.getListeningMood();
        this.artistLoyalty = personality.getArtistLoyalty();
        this.trackLengthPreference = personality.getTrackLengthPreference();
    }

    // Getters and setters
    public Integer getId() { return id; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public Instant getAnalyzedAt() { return analyzedAt; }
    public void setAnalyzedAt(Instant analyzedAt) { this.analyzedAt = analyzedAt; }

    public String getPrimaryPersonality() { return primaryPersonality; }
    public void setPrimaryPersonality(String primaryPersonality) { this.primaryPersonality = primaryPersonality; }

    public int getDiversityScore() { return diversityScore; }
    public void setDiversityScore(int diversityScore) { this.diversityScore = diversityScore; }

    public int getMainstreamScore() { return mainstreamScore; }
    public void setMainstreamScore(int mainstreamScore) { this.mainstreamScore = mainstreamScore; }

    public String getListeningMood() { return listeningMood; }
    public void setListeningMood(String listeningMood) { this.listeningMood = listeningMood; }

    public String getArtistLoyalty() { return artistLoyalty; }
    public void setArtistLoyalty(String artistLoyalty) { this.artistLoyalty = artistLoyalty; }

    public String getTrackLengthPreference() { return trackLengthPreference; }
    public void setTrackLengthPreference(String trackLengthPreference) { this.trackLengthPreference = trackLengthPreference; }
}

