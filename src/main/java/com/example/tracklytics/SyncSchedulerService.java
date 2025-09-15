package com.example.tracklytics;

import com.example.tracklytics.insights.MusicInsightsService;
import com.example.tracklytics.user.User;
import com.example.tracklytics.user.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
public class SyncSchedulerService {

    @Autowired
    private UserService userService;

    @Autowired
    private SpotifyApiService spotifyApiService;

    @Autowired
    private MusicInsightsService musicInsightsService;

    // Run every 24 hours (24 * 60 * 60 * 1000 = 86400000 milliseconds)
    @Scheduled(fixedRate = 86400000)
    public void syncAllUsersData() {
        System.out.println("=== Starting scheduled sync for all users ===");

        try {
            List<User> allUsers = userService.getAllUsers();
            int successfulSyncs = 0;
            int failedSyncs = 0;

            for (User user : allUsers) {
                try {
                    // Check if user has valid token
                    if (user.getAccessToken() == null) {
                        System.out.println("Skipping user " + user.getDisplayName() + " - no access token");
                        continue;
                    }

                    // Check if token is expired
                    if (user.getTokenExpiry() != null && user.getTokenExpiry().isBefore(Instant.now())) {
                        System.out.println("Skipping user " + user.getDisplayName() + " - token expired");
                        continue;
                    }

                    // Check if user was synced recently (less than 23 hours ago)
                    if (user.getLastSyncTime() != null &&
                            user.getLastSyncTime().isAfter(Instant.now().minus(23, ChronoUnit.HOURS))) {
                        System.out.println("Skipping user " + user.getDisplayName() + " - synced recently");
                        continue;
                    }

                    System.out.println("Syncing data for user: " + user.getDisplayName());
                    spotifyApiService.fetchAndSaveUserTopTracks(user);
                    spotifyApiService.fetchAndSaveUserTopArtists(user);
                    successfulSyncs++;

                    // Add small delay between users to be nice to Spotify's API
                    Thread.sleep(1000);

                } catch (Exception e) {
                    System.err.println("Failed to sync user " + user.getDisplayName() + ": " + e.getMessage());
                    failedSyncs++;
                }
            }

            System.out.println("=== Scheduled sync complete ===");
            System.out.println("Successful syncs: " + successfulSyncs);
            System.out.println("Failed syncs: " + failedSyncs);
            System.out.println("Total users processed: " + allUsers.size());

        } catch (Exception e) {
            System.err.println("Error during scheduled sync: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Optional: Run a lighter check every hour to sync users who need it
    @Scheduled(fixedRate = 3600000) // 1 hour
    public void quickSyncCheck() {
        System.out.println("Running hourly sync check...");

        try {
            List<User> allUsers = userService.getAllUsers();

            for (User user : allUsers) {
                // Only sync if user hasn't been synced in more than 24 hours
                if (user.getLastSyncTime() == null ||
                        user.getLastSyncTime().isBefore(Instant.now().minus(24, ChronoUnit.HOURS))) {

                    if (user.getAccessToken() != null &&
                            (user.getTokenExpiry() == null || user.getTokenExpiry().isAfter(Instant.now()))) {

                        System.out.println("Quick sync for user: " + user.getDisplayName());
                        spotifyApiService.fetchAndSaveUserTopTracks(user);
                        spotifyApiService.fetchAndSaveUserTopArtists(user);

                        Thread.sleep(1000);
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Error during quick sync check: " + e.getMessage());
        }
    }

    @Scheduled(fixedRate = 604800000) // Weekly (7 days in milliseconds)
    public void weeklyPersonalitySnapshot() {
        System.out.println("=== Starting weekly personality snapshot for all users ===");

        try {
            List<User> allUsers = userService.getAllUsers();
            int successfulSnapshots = 0;

            for (User user : allUsers) {
                try {
                    // Only process users with music data
                    if (user.getTopArtists().isEmpty() || user.getTopTracks().isEmpty()) {
                        System.out.println("Skipping user " + user.getDisplayName() + " - no music data");
                        continue;
                    }

                    // Generate and save weekly personality snapshot
                    musicInsightsService.analyzeMusicPersonalityAndSaveHistory(user);
                    successfulSnapshots++;

                    System.out.println("Saved weekly personality snapshot for: " + user.getDisplayName());

                    // Small delay between users
                    Thread.sleep(500);

                } catch (Exception e) {
                    System.err.println("Failed to create personality snapshot for " + user.getDisplayName() + ": " + e.getMessage());
                }
            }

            System.out.println("=== Weekly personality snapshot complete ===");
            System.out.println("Successful snapshots: " + successfulSnapshots);

        } catch (Exception e) {
            System.err.println("Error during weekly personality snapshot: " + e.getMessage());
        }
    }
}
