package com.example.tracklytics.insights;

public class MusicPersonality {
    private String primaryPersonality;
    private String description;
    private int mainstreamScore;  // 0-100
    private int diversityScore;   // 0-100
    private String listeningMood;
    private String artistLoyalty;
    private String trackLengthPreference;

    public MusicPersonality() {}

    public String getPrimaryPersonality() { return primaryPersonality; }
    public void setPrimaryPersonality(String primaryPersonality) { this.primaryPersonality = primaryPersonality; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public int getMainstreamScore() { return mainstreamScore; }
    public void setMainstreamScore(int mainstreamScore) { this.mainstreamScore = mainstreamScore; }

    public int getDiversityScore() { return diversityScore; }
    public void setDiversityScore(int diversityScore) { this.diversityScore = diversityScore;
    }

    public String getListeningMood() { return listeningMood; }
    public void setListeningMood(String listeningMood) { this.listeningMood = listeningMood; }

    public String getArtistLoyalty() { return artistLoyalty; }
    public void setArtistLoyalty(String artistLoyalty) { this.artistLoyalty = artistLoyalty; }

    public String getTrackLengthPreference() { return trackLengthPreference; }
    public void setTrackLengthPreference(String trackLengthPreference) { this.trackLengthPreference = trackLengthPreference; }
}