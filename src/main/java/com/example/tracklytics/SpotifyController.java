package com.example.tracklytics;

import com.example.tracklytics.insights.MusicInsightsService;
import com.example.tracklytics.insights.MusicPersonality;
import com.example.tracklytics.insights.PersonalityHistory;
import com.example.tracklytics.user.User;
import com.example.tracklytics.user.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@RestController
public class SpotifyController {

    @Autowired
    private UserService userService;

    @Autowired
    private SpotifyApiService spotifyApiService;

    @Autowired
    private MusicInsightsService musicInsightsService;

    @GetMapping("/")
    public String home() {
        return "<h1>Welcome to Tracklytics</h1>" +
                "<p><a href='/oauth2/authorization/spotify'>Login with Spotify</a></p>" +
                "<p>Make sure to access this page via: http://127.0.0.1:8080</p>" +
                "<p><a href='/debug'>Debug Info</a></p>";
    }

    @GetMapping("/me")
    public String getUser(@AuthenticationPrincipal OAuth2User principal) {
        if (principal == null) {
            System.err.println("Principal is null - user not authenticated");
            return "Not authenticated - <a href='/oauth2/authorization/spotify'>Login with Spotify</a>";
        }

        try {
            // Get user from database
            String spotifyId = principal.getAttribute("id");
            Optional<User> userOpt = userService.findBySpotifyId(spotifyId);

            if (userOpt.isEmpty()) {
                return "User not found in database - <a href='/oauth2/authorization/spotify'>Login Again</a>";
            }

            User user = userOpt.get();
            System.out.println("Displaying user from database: " + user.getDisplayName());

            return String.format(
                    "<h1>Welcome back, %s!</h1>" +
                            "<div style='max-width: 600px; margin: 0 auto; font-family: Arial, sans-serif;'>" +
                            "<h2>Your Profile</h2>" +
                            "<p><strong>Display Name:</strong> %s</p>" +
                            "<p><strong>Email:</strong> %s</p>" +
                            "<p><strong>Spotify ID:</strong> %s</p>" +
                            "<p><strong>Database ID:</strong> %d</p>" +
                            "<p><strong>Access Token:</strong> %s...</p>" +
                            "<p><strong>Refresh Token:</strong> %s</p>" +
                            "<p><strong>Token Expires:</strong> %s</p>" +
                            "<hr>" +
                            "<p><a href='/user-info'>View Raw Spotify Data</a> | <a href='/profile'>Database Profile</a> | <a href='/sync'>Sync My Music Data</a> | <a href='/'>Home</a></p>" +
                            "</div>",
                    user.getDisplayName() != null ? user.getDisplayName() : "Spotify User",
                    user.getDisplayName() != null ? user.getDisplayName() : "N/A",
                    user.getEmail() != null ? user.getEmail() : "N/A",
                    user.getSpotifyId(),
                    user.getId(),
                    user.getAccessToken() != null ? user.getAccessToken().substring(0, 20) : "None",
                    user.getRefreshToken() != null ? "Present" : "Not Available",
                    user.getTokenExpiry() != null ? user.getTokenExpiry().toString() : "N/A"
            );
        } catch (Exception e) {
            System.err.println("Error processing user info: " + e.getMessage());
            e.printStackTrace();
            return "Error processing user info: " + e.getMessage() + " - <a href='/oauth2/authorization/spotify'>Login Again</a>";
        }
    }

    @GetMapping("/profile")
    public String getUserProfile(@AuthenticationPrincipal OAuth2User principal) {
        if (principal == null) {
            return "Not authenticated - <a href='/oauth2/authorization/spotify'>Login with Spotify</a>";
        }

        String spotifyId = principal.getAttribute("id");
        Optional<User> userOpt = userService.findBySpotifyId(spotifyId);

        if (userOpt.isEmpty()) {
            return "User not found in database";
        }

        User user = userOpt.get();

        return String.format(
                "<h1>Database Profile</h1>" +
                        "<div style='max-width: 600px; margin: 0 auto; font-family: Arial, sans-serif;'>" +
                        "<p><strong>Database ID:</strong> %d</p>" +
                        "<p><strong>Spotify ID:</strong> %s</p>" +
                        "<p><strong>Display Name:</strong> %s</p>" +
                        "<p><strong>Email:</strong> %s</p>" +
                        "<p><strong>Has Access Token:</strong> %s</p>" +
                        "<p><strong>Has Refresh Token:</strong> %s</p>" +
                        "<p><strong>Token Expires:</strong> %s</p>" +
                        "<hr>" +
                        "<h3>Token Status</h3>" +
                        "<p>%s</p>" +
                        "<p><a href='/me'>Back to Profile</a> | <a href='/'>Home</a></p>" +
                        "</div>",
                user.getId(),
                user.getSpotifyId(),
                user.getDisplayName(),
                user.getEmail(),
                user.getAccessToken() != null ? "Yes" : "No",
                user.getRefreshToken() != null ? "Yes" : "No",
                user.getTokenExpiry() != null ? user.getTokenExpiry().toString() : "N/A",
                user.getTokenExpiry() != null && user.getTokenExpiry().isAfter(Instant.now()) ?
                        "✅ Token is still valid" :
                        "⚠️ Token may be expired"
        );
    }

    @GetMapping("/login")
    public String loginPage(@RequestParam(value = "error", required = false) String error,
                            @RequestParam(value = "retry", required = false) String retry) {
        if (error != null) {
            System.err.println("Login error occurred: " + error);

            // Auto-retry logic for oauth2_failed
            if ("oauth2_failed".equals(error) && !"true".equals(retry)) {
                return "<h1>Authentication Error - Auto Retrying...</h1>" +
                        "<p>The first login attempt failed (this is a known issue). Automatically retrying...</p>" +
                        "<script>" +
                        "setTimeout(function() {" +
                        "  window.location.href = '/oauth2/authorization/spotify';" +
                        "}, 2000);" + // Wait 2 seconds then auto-retry
                        "</script>" +
                        "<p>If you're not automatically redirected, <a href='/oauth2/authorization/spotify'>click here to retry</a></p>" +
                        "<p><a href='/'>Go Home</a></p>";
            }

            // If retry failed or manual retry
            return "<h1>Login Error</h1>" +
                    "<p>There was an error during login: " + error + "</p>" +
                    "<p><a href='/oauth2/authorization/spotify'>Try Login Again</a></p>" +
                    "<p><a href='/'>Go Home</a></p>" +
                    "<p><strong>Note:</strong> If login fails, try again - sometimes it works on the second attempt!</p>";
        }
        return "<h1>Login Page</h1>" +
                "<p><a href='/oauth2/authorization/spotify'>Login with Spotify</a></p>" +
                "<p><a href='/'>Go Home</a></p>";
    }

    @GetMapping("/debug")
    public String debugInfo() {
        return "<h1>Debug Information</h1>" +
                "<h2>OAuth2 URLs to check:</h2>" +
                "<ul>" +
                "<li><strong>Authorization URL:</strong> <a href='/oauth2/authorization/spotify'>http://127.0.0.1:8080/oauth2/authorization/spotify</a></li>" +
                "<li><strong>Expected Redirect URI in Spotify:</strong> http://127.0.0.1:8080/login/oauth2/code/spotify</li>" +
                "</ul>" +
                "<h2>Spotify App Settings Checklist:</h2>" +
                "<ul>" +
                "<li>Client ID: 5c80f72ddf284e34991b315f35e41d11</li>" +
                "<li>Redirect URI must be EXACTLY: http://127.0.0.1:8080/login/oauth2/code/spotify</li>" +
                "<li>App must be in Development Mode OR your Spotify account must be added as a test user</li>" +
                "</ul>" +
                "<p><a href='/'>Go Home</a></p>";
    }

    @GetMapping("/user-info")
    public Map<String, Object> getUserInfo(@AuthenticationPrincipal OAuth2User principal) {
        return principal != null ? principal.getAttributes() : Map.of("error", "Not authenticated");
    }

    @GetMapping("/sync")
    @Transactional
    public String syncUserData(@AuthenticationPrincipal OAuth2User principal) {
        if (principal == null) {
            return "Not authenticated - <a href='/oauth2/authorization/spotify'>Login with Spotify</a>";
        }

        String spotifyId = principal.getAttribute("id");
        Optional<User> userOpt = userService.findBySpotifyId(spotifyId);

        if (userOpt.isPresent()) {
            User user = userOpt.get();

            // Check if token is expired
            if (user.getTokenExpiry() != null && user.getTokenExpiry().isBefore(Instant.now())) {
                return "<h2>Token Expired</h2>" +
                        "<p>Your Spotify token has expired. Please log in again.</p>" +
                        "<p><a href='/oauth2/authorization/spotify'>Re-authenticate with Spotify</a></p>";
            }

            try {
                spotifyApiService.fetchAndSaveUserTopTracks(user);
                spotifyApiService.fetchAndSaveUserTopArtists(user);
                return "<h2>✅ Data Sync Complete!</h2>" +
                        "<p>Successfully synced your top tracks and artists from Spotify.</p>" +
                        "<p><a href='/me'>Back to Profile</a> | <a href='/'>Home</a></p>";
            } catch (Exception e) {
                System.err.println("Error during sync: " + e.getMessage());
                return "<h2>❌ Sync Failed</h2>" +
                        "<p>Error: " + e.getMessage() + "</p>" +
                        "<p><a href='/oauth2/authorization/spotify'>Try re-authenticating</a> | <a href='/me'>Back to Profile</a></p>";
            }
        }

        return "User not found in database - <a href='/oauth2/authorization/spotify'>Please log in</a>";
    }

    @GetMapping("/music-personality")
    public String getMusicPersonality(@AuthenticationPrincipal OAuth2User principal) {
        if (principal == null) {
            return "Not authenticated - <a href='/oauth2/authorization/spotify'>Login with Spotify</a>";
        }

        String spotifyId = principal.getAttribute("id");
        Optional<User> userOpt = userService.findBySpotifyId(spotifyId);

        if (userOpt.isEmpty()) {
            return "User not found - <a href='/sync'>Sync your data first</a>";
        }

        User user = userOpt.get();

        if (user.getTopArtists().isEmpty()) {
            return "<h2>No Music Data</h2>" +
                    "<p>You need to sync your Spotify data first to see your music personality!</p>" +
                    "<p><a href='/sync'>Sync My Data</a></p>";
        }

        MusicPersonality personality = musicInsightsService.analyzeMusicPersonality(user);

        return String.format(
                "<div style='max-width: 1200px; margin: 0 auto; font-family: Arial, sans-serif; text-align: center;'>" +
                        "<h1>🎵 Your Music Personality</h1>" +
                        "<h2 style='color: #1db954; font-size: 2em;'>%s</h2>" +
                        "<p style='font-size: 1.2em; margin: 20px 0; font-style: italic;'>%s</p>" +
                        "<div style='display: flex; justify-content: space-around; margin: 30px 0; flex-wrap: wrap;'>" +
                        "<div style='background: #f0f0f0; padding: 20px; border-radius: 10px; flex: 1; margin: 0 8px 20px 8px; min-width: 180px; max-width: 220px;'>" +
                        "<h3>Genre Diversity</h3>" +
                        "<div style='font-size: 2em; color: #1db954;'>%d/100</div>" +
                        "<small>%s</small>" +
                        "</div>" +
                        "<div style='background: #f0f0f0; padding: 20px; border-radius: 10px; flex: 1; margin: 0 8px 20px 8px; min-width: 180px; max-width: 220px;'>" +
                        "<h3>Mainstream Appeal</h3>" +
                        "<div style='font-size: 2em; color: #1db954;'>%d/100</div>" +
                        "<small>%s</small>" +
                        "</div>" +
                        "<div style='background: #f0f0f0; padding: 20px; border-radius: 10px; flex: 1; margin: 0 8px 20px 8px; min-width: 180px; max-width: 220px;'>" +
                        "<h3>Listening Mood</h3>" +
                        "<div style='font-size: 1.2em; color: #1db954; margin-top: 10px;'>%s</div>" +
                        "</div>" +
                        "<div style='background: #f0f0f0; padding: 20px; border-radius: 10px; flex: 1; margin: 0 8px 20px 8px; min-width: 180px; max-width: 220px;'>" +
                        "<h3>Artist Loyalty</h3>" +
                        "<div style='font-size: 1.2em; color: #1db954; margin-top: 10px;'>%s</div>" +
                        "</div>" +
                        "<div style='background: #f0f0f0; padding: 20px; border-radius: 10px; flex: 1; margin: 0 8px 20px 8px; min-width: 180px; max-width: 220px;'>" +
                        "<h3>Track Length</h3>" +
                        "<div style='font-size: 1.2em; color: #1db954; margin-top: 10px;'>%s</div>" +
                        "</div>" +
                        "</div>" +
                        "<p><a href='/me'>Back to Profile</a> | <a href='/sync'>Update Data</a></p>" +
                        "</div>",
                personality.getPrimaryPersonality(),
                personality.getDescription(),
                personality.getDiversityScore(),
                personality.getDiversityScore() >= 65 ? "Very diverse" : personality.getDiversityScore() >= 50 ? "Moderately diverse" : "Focused taste",
                personality.getMainstreamScore(),
                personality.getMainstreamScore() >= 70 ? "Loves the hits" : personality.getMainstreamScore() >= 50 ? "Mixed taste" : "Underground vibes",
                personality.getListeningMood(),
                personality.getArtistLoyalty(),
                personality.getTrackLengthPreference()
        );
    }

    @GetMapping("/personality-card")
    public String getPersonalityCard(@AuthenticationPrincipal OAuth2User principal) {
        if (principal == null) {
            return "Not authenticated - <a href='/oauth2/authorization/spotify'>Login with Spotify</a>";
        }

        String spotifyId = principal.getAttribute("id");
        Optional<User> userOpt = userService.findBySpotifyId(spotifyId);

        if (userOpt.isEmpty()) {
            return "User not found - <a href='/sync'>Sync your data first</a>";
        }

        User user = userOpt.get();

        if (user.getTopArtists().isEmpty()) {
            return "No data - <a href='/sync'>Sync first</a>";
        }

        MusicPersonality personality = musicInsightsService.analyzeMusicPersonalityAndSaveHistory(user);

        return String.format(
                "<div style='max-width: 500px; margin: 50px auto; font-family: Arial, sans-serif; background: linear-gradient(135deg, #2c2c2c 0%%, #1a1a1a 100%%); border-radius: 20px; padding: 40px; color: white; text-align: center; box-shadow: 0 10px 30px rgba(0,0,0,0.3);'>" +
                        "<h1 style='margin-bottom: 10px; font-size: 1.5em;'>🎵 Music Personality</h1>" +
                        "<h2 style='color: #1db954; font-size: 1.8em; margin: 20px 0;'>%s</h2>" +
                        "<p style='font-style: italic; margin-bottom: 30px; opacity: 0.9;'>%s</p>" +

                        "<div style='display: grid; grid-template-columns: 1fr 1fr; gap: 15px; margin: 25px 0;'>" +
                        "<div style='background: rgba(255,255,255,0.15); padding: 15px; border-radius: 10px;'>" +
                        "<div style='font-size: 0.8em; margin-bottom: 5px;'>Diversity</div>" +
                        "<div style='font-size: 1.5em; font-weight: bold;'>%d/100</div>" +
                        "</div>" +
                        "<div style='background: rgba(255,255,255,0.15); padding: 15px; border-radius: 10px;'>" +
                        "<div style='font-size: 0.8em; margin-bottom: 5px;'>Mainstream</div>" +
                        "<div style='font-size: 1.5em; font-weight: bold;'>%d/100</div>" +
                        "</div>" +
                        "</div>" +

                        "<div style='display: grid; grid-template-columns: 1fr 1fr 1fr; gap: 10px; margin: 25px 0;'>" +
                        "<div style='background: rgba(255,255,255,0.15); padding: 10px; border-radius: 8px; font-size: 0.85em;'>" +
                        "<div style='font-size: 0.7em; margin-bottom: 3px;'>Mood</div>" +
                        "<div style='font-weight: bold;'>%s</div>" +
                        "</div>" +
                        "<div style='background: rgba(255,255,255,0.15); padding: 10px; border-radius: 8px; font-size: 0.85em;'>" +
                        "<div style='font-size: 0.7em; margin-bottom: 3px;'>Loyalty</div>" +
                        "<div style='font-weight: bold;'>%s</div>" +
                        "</div>" +
                        "<div style='background: rgba(255,255,255,0.15); padding: 10px; border-radius: 8px; font-size: 0.85em;'>" +
                        "<div style='font-size: 0.7em; margin-bottom: 3px;'>Length</div>" +
                        "<div style='font-weight: bold;'>%s</div>" +
                        "</div>" +
                        "</div>" +

                        "<div style='margin-top: 30px; font-size: 0.8em; opacity: 0.7;'>Tracklytics Music Analysis</div>" +
                        "</div>",
                personality.getPrimaryPersonality(),
                personality.getDescription(),
                personality.getDiversityScore(),
                personality.getMainstreamScore(),
                personality.getListeningMood(),
                personality.getArtistLoyalty(),
                personality.getTrackLengthPreference()
        );
    }

    @GetMapping("/personality-history")
    public String getPersonalityHistory(@AuthenticationPrincipal OAuth2User principal) {
        if (principal == null) {
            return "Not authenticated - <a href='/oauth2/authorization/spotify'>Login with Spotify</a>";
        }

        String spotifyId = principal.getAttribute("id");
        Optional<User> userOpt = userService.findBySpotifyId(spotifyId);

        if (userOpt.isEmpty()) {
            return "User not found - <a href='/sync'>Sync your data first</a>";
        }

        User user = userOpt.get();
        List<PersonalityHistory> history = musicInsightsService.getPersonalityHistory(user);

        if (history.isEmpty()) {
            return "<div style='max-width: 800px; margin: 0 auto; font-family: Arial, sans-serif; text-align: center; background: #1a1a1a; color: white; padding: 40px; border-radius: 20px;'>" +
                    "<h1>Your Personality History</h1>" +
                    "<p>No personality history yet. <a href='/music-personality' style='color: #1db954;'>Analyze your personality</a> to start tracking changes!</p>" +
                    "</div>";
        }

        StringBuilder htmlBuilder = new StringBuilder();
        htmlBuilder.append("<div style='max-width: 800px; margin: 0 auto; font-family: Arial, sans-serif; padding: 20px;'>")
                .append("<div style='background: linear-gradient(135deg, #2c2c2c 0%, #1a1a1a 100%); color: white; padding: 30px; border-radius: 20px; text-align: center; margin-bottom: 30px;'>")
                .append("<h1 style='margin: 0; font-size: 2em;'>Your Music Personality Over Time</h1>")
                .append("<p style='color: #ccc; margin: 10px 0 0 0;'>Track how your music taste evolves</p>")
                .append("</div>");

        for (int i = 0; i < history.size(); i++) {
            PersonalityHistory entry = history.get(i);
            boolean isLatest = (i == 0);

            String cardStyle = isLatest ?
                    "background: linear-gradient(135deg, #2c2c2c 0%, #1a1a1a 100%); color: white; border: 2px solid #1db954;" :
                    "background: linear-gradient(135deg, #3a3a3a 0%, #2a2a2a 100%); color: #ccc; border: 1px solid #444;";

            htmlBuilder.append(String.format(
                    "<div style='%s margin-bottom: 20px; padding: 25px; border-radius: 15px; box-shadow: 0 5px 15px rgba(0,0,0,0.3);'>" +
                            "%s" +
                            "<div style='display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px;'>" +
                            "<h2 style='margin: 0; color: %s;'>%s</h2>" +
                            "<span style='opacity: 0.7; font-size: 0.9em;'>%s</span>" +
                            "</div>" +

                            "<div style='display: grid; grid-template-columns: repeat(auto-fit, minmax(120px, 1fr)); gap: 15px;'>" +
                            "<div style='background: rgba(255,255,255,0.1); padding: 15px; border-radius: 10px; text-align: center;'>" +
                            "<div style='font-size: 0.8em; margin-bottom: 8px; opacity: 0.8;'>Diversity</div>" +
                            "<div style='font-size: 1.5em; font-weight: bold;'>%d/100</div>" +
                            "</div>" +
                            "<div style='background: rgba(255,255,255,0.1); padding: 15px; border-radius: 10px; text-align: center;'>" +
                            "<div style='font-size: 0.8em; margin-bottom: 8px; opacity: 0.8;'>Mainstream</div>" +
                            "<div style='font-size: 1.5em; font-weight: bold;'>%d/100</div>" +
                            "</div>" +
                            "<div style='background: rgba(255,255,255,0.1); padding: 15px; border-radius: 10px; text-align: center;'>" +
                            "<div style='font-size: 0.7em; margin-bottom: 5px; opacity: 0.8;'>Mood</div>" +
                            "<div style='font-size: 0.9em; font-weight: bold;'>%s</div>" +
                            "</div>" +
                            "<div style='background: rgba(255,255,255,0.1); padding: 15px; border-radius: 10px; text-align: center;'>" +
                            "<div style='font-size: 0.7em; margin-bottom: 5px; opacity: 0.8;'>Loyalty</div>" +
                            "<div style='font-size: 0.9em; font-weight: bold;'>%s</div>" +
                            "</div>" +
                            "<div style='background: rgba(255,255,255,0.1); padding: 15px; border-radius: 10px; text-align: center;'>" +
                            "<div style='font-size: 0.7em; margin-bottom: 5px; opacity: 0.8;'>Length</div>" +
                            "<div style='font-size: 0.9em; font-weight: bold;'>%s</div>" +
                            "</div>" +
                            "</div>" +
                            "</div>",
                    cardStyle,
                    isLatest ? "<div style='text-align: center; margin-bottom: 15px; color: #1db954; font-weight: bold; font-size: 0.9em; text-transform: uppercase; letter-spacing: 1px;'>CURRENT</div>" : "",
                    isLatest ? "#1db954" : "#ccc",
                    entry.getPrimaryPersonality(),
                    entry.getAnalyzedAt().toString().substring(0, 16).replace("T", " "),
                    entry.getDiversityScore(),
                    entry.getMainstreamScore(),
                    entry.getListeningMood(),
                    entry.getArtistLoyalty(),
                    entry.getTrackLengthPreference()
            ));
        }

        htmlBuilder.append("<div style='text-align: center; margin-top: 40px;'>")
                .append("<a href='/music-personality' style='color: #1db954; margin: 0 10px;'>Update Current Personality</a> | ")
                .append("<a href='/me' style='color: #1db954; margin: 0 10px;'>Back to Profile</a>")
                .append("</div></div>");

        return htmlBuilder.toString();
    }

    @GetMapping("/auth-success")
    public String authSuccess(@RequestParam String token) {
        return String.format(
                "<div style='max-width: 600px; margin: 50px auto; font-family: Arial, sans-serif; text-align: center;'>" +
                        "<h1>Authentication Successful!</h1>" +
                        "<p>Your JWT token has been generated:</p>" +
                        "<div style='background: #f0f0f0; padding: 20px; border-radius: 10px; word-break: break-all; margin: 20px 0;'>" +
                        "<code>%s</code>" +
                        "</div>" +
                        "<p>This token can be used for API authentication.</p>" +
                        "</div>",
                token
        );
    }

    @GetMapping("/api/auth/me")
    @ResponseBody
    public ResponseEntity<?> getCurrentUser(Authentication authentication) {
        if (authentication == null || !(authentication.getPrincipal() instanceof User)) {
            return ResponseEntity.status(401).body(Map.of("error", "Not authenticated"));
        }

        User user = (User) authentication.getPrincipal();

        Map<String, Object> response = new HashMap<>();
        response.put("spotifyId", user.getSpotifyId());
        response.put("displayName", user.getDisplayName());
        response.put("email", user.getEmail());
        response.put("hasData", !user.getTopTracks().isEmpty() || !user.getTopArtists().isEmpty());
        response.put("lastSync", user.getLastSyncTime());

        return ResponseEntity.ok(response);
    }

    @GetMapping("/test-jwt")
    public String testJwtPage(@RequestParam(required = false) String token) {
        return String.format("""
        <div style='max-width: 600px; margin: 50px auto; font-family: Arial;'>
            <h1>JWT API Test</h1>
            <input type='text' id='token' placeholder='Paste JWT token here' value='%s' style='width: 100%%; padding: 10px; margin: 10px 0;'/>
            
            <div style='margin: 20px 0;'>
                <button onclick='testAuthApi()' style='padding: 10px 20px; background: #1db954; color: white; border: none; cursor: pointer; margin-right: 10px;'>Test Auth API</button>
                <button onclick='testPersonalityApi()' style='padding: 10px 20px; background: #764ba2; color: white; border: none; cursor: pointer; margin-right: 10px;'>Test Personality API</button>
                <button onclick='testPersonalityHistoryApi()' style='padding: 10px 20px; background: #2196F3; color: white; border: none; cursor: pointer; margin-right: 10px;'>Test History API</button>
                <button onclick='testSyncApi()' style='padding: 10px 20px; background: #ff6b35; color: white; border: none; cursor: pointer;'>Test Sync API</button>
            </div>
            
            <pre id='result' style='background: #f0f0f0; padding: 20px; margin: 20px 0; min-height: 100px;'></pre>
            
            <script>
                async function testAuthApi() {
                    const token = document.getElementById('token').value;
                    const result = document.getElementById('result');
                    
                    try {
                        const response = await fetch('/api/auth/me', {
                            headers: {
                                'Authorization': 'Bearer ' + token,
                                'Content-Type': 'application/json'
                            }
                        });
                        
                        const data = await response.json();
                        result.textContent = 'Auth API Response:\\n' + JSON.stringify(data, null, 2);
                    } catch (error) {
                        result.textContent = 'Auth API Error: ' + error.message;
                    }
                }
                
                async function testPersonalityApi() {
                    const token = document.getElementById('token').value;
                    const result = document.getElementById('result');
                    
                    try {
                        const response = await fetch('/api/personality', {
                            headers: {
                                'Authorization': 'Bearer ' + token,
                                'Content-Type': 'application/json'
                            }
                        });
                        
                        const data = await response.json();
                        result.textContent = 'Personality API Response:\\n' + JSON.stringify(data, null, 2);
                    } catch (error) {
                        result.textContent = 'Personality API Error: ' + error.message;
                    }
                }
                
                async function testPersonalityHistoryApi() {
                    const token = document.getElementById('token').value;
                    const result = document.getElementById('result');
                    
                    try {
                        const response = await fetch('/api/personality/history', {
                            headers: {
                                'Authorization': 'Bearer ' + token,
                                'Content-Type': 'application/json'
                            }
                        });
                        
                        const data = await response.json();
                        result.textContent = 'History API Response:\\n' + JSON.stringify(data, null, 2);
                    } catch (error) {
                        result.textContent = 'History API Error: ' + error.message;
                    }
                }
                
                async function testSyncApi() {
                    const token = document.getElementById('token').value;
                    const result = document.getElementById('result');
                    
                    try {
                        const response = await fetch('/api/sync', {
                            method: 'POST',
                            headers: {
                                'Authorization': 'Bearer ' + token,
                                'Content-Type': 'application/json'
                            }
                        });
                        
                        const data = await response.json();
                        result.textContent = 'Sync API Response:\\n' + JSON.stringify(data, null, 2);
                    } catch (error) {
                        result.textContent = 'Sync API Error: ' + error.message;
                    }
                }
            </script>
        </div>
        """, token != null ? token : "");
    }

    @GetMapping("/api/personality")
    @ResponseBody
    public ResponseEntity<?> getPersonalityJson(Authentication authentication) {
        if (authentication == null || !(authentication.getPrincipal() instanceof User)) {
            return ResponseEntity.status(401).body(Map.of("error", "Not authenticated"));
        }

        User user = (User) authentication.getPrincipal();

        if (user.getTopArtists().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of(
                    "error", "No music data available",
                    "message", "Please sync your Spotify data first"
            ));
        }

        MusicPersonality personality = musicInsightsService.analyzeMusicPersonality(user);

        Map<String, Object> response = new HashMap<>();
        response.put("primaryPersonality", personality.getPrimaryPersonality());
        response.put("description", personality.getDescription());
        response.put("diversityScore", personality.getDiversityScore());
        response.put("mainstreamScore", personality.getMainstreamScore());
        response.put("listeningMood", personality.getListeningMood());
        response.put("artistLoyalty", personality.getArtistLoyalty());
        response.put("trackLengthPreference", personality.getTrackLengthPreference());
        response.put("analyzedAt", Instant.now().toString());

        return ResponseEntity.ok(response);
    }

    @GetMapping("/api/personality/history")
    @ResponseBody
    public ResponseEntity<?> getPersonalityHistoryJson(Authentication authentication) {
        if (authentication == null || !(authentication.getPrincipal() instanceof User)) {
            return ResponseEntity.status(401).body(Map.of("error", "Not authenticated"));
        }

        User user = (User) authentication.getPrincipal();
        List<PersonalityHistory> history = musicInsightsService.getPersonalityHistory(user);

        if (history.isEmpty()) {
            return ResponseEntity.ok(Map.of(
                    "history", Collections.emptyList(),
                    "message", "No personality history yet"
            ));
        }

        List<Map<String, Object>> historyData = history.stream()
                .map(entry -> {
                    Map<String, Object> entryMap = new HashMap<>();
                    entryMap.put("id", entry.getId());
                    entryMap.put("primaryPersonality", entry.getPrimaryPersonality());
                    entryMap.put("diversityScore", entry.getDiversityScore());
                    entryMap.put("mainstreamScore", entry.getMainstreamScore());
                    entryMap.put("listeningMood", entry.getListeningMood());
                    entryMap.put("artistLoyalty", entry.getArtistLoyalty());
                    entryMap.put("trackLengthPreference", entry.getTrackLengthPreference());
                    entryMap.put("analyzedAt", entry.getAnalyzedAt().toString());
                    entryMap.put("isLatest", history.indexOf(entry) == 0);
                    return entryMap;
                })
                .collect(Collectors.toList());

        return ResponseEntity.ok(Map.of(
                "history", historyData,
                "totalEntries", history.size()
        ));
    }

    @PostMapping("/api/sync")
    @ResponseBody
    public ResponseEntity<?> syncMusicDataJson(Authentication authentication) {
        if (authentication == null || !(authentication.getPrincipal() instanceof User)) {
            return ResponseEntity.status(401).body(Map.of("error", "Not authenticated"));
        }

        User user = (User) authentication.getPrincipal();

        // Check if token is expired
        if (user.getTokenExpiry() != null && user.getTokenExpiry().isBefore(Instant.now())) {
            return ResponseEntity.badRequest().body(Map.of(
                    "error", "Token expired",
                    "message", "Please re-authenticate with Spotify"
            ));
        }

        try {
            spotifyApiService.fetchAndSaveUserTopTracks(user);
            spotifyApiService.fetchAndSaveUserTopArtists(user);

            // Get updated user data
            User updatedUser = userService.findBySpotifyId(user.getSpotifyId()).orElse(user);

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Successfully synced your music data",
                    "tracksCount", updatedUser.getTopTracks().size(),
                    "artistsCount", updatedUser.getTopArtists().size(),
                    "lastSync", updatedUser.getLastSyncTime() != null ? updatedUser.getLastSyncTime().toString() : null
            ));

        } catch (Exception e) {
            System.err.println("Error during sync: " + e.getMessage());
            return ResponseEntity.status(500).body(Map.of(
                    "error", "Sync failed",
                    "message", e.getMessage()
            ));
        }
    }

    @GetMapping("/api/tracks")
    @ResponseBody
    public ResponseEntity<?> getTopTracksJson(Authentication authentication) {
        if (authentication == null || !(authentication.getPrincipal() instanceof User)) {
            return ResponseEntity.status(401).body(Map.of("error", "Not authenticated"));
        }

        User user = (User) authentication.getPrincipal();

        List<Map<String, Object>> tracks = user.getTopTracks().stream()
                .map(track -> {
                    Map<String, Object> trackMap = new HashMap<>();
                    trackMap.put("id", track.getId());
                    trackMap.put("spotifyId", track.getSpotifyId());
                    trackMap.put("name", track.getName());
                    trackMap.put("albumName", track.getAlbumName());
                    trackMap.put("durationMs", track.getDurationMs());
                    trackMap.put("popularity", track.getPopularity());
                    return trackMap;
                })
                .collect(Collectors.toList());

        return ResponseEntity.ok(Map.of(
                "tracks", tracks,
                "totalTracks", tracks.size()
        ));
    }

    @GetMapping("/api/artists")
    @ResponseBody
    public ResponseEntity<?> getTopArtistsJson(Authentication authentication) {
        if (authentication == null || !(authentication.getPrincipal() instanceof User)) {
            return ResponseEntity.status(401).body(Map.of("error", "Not authenticated"));
        }

        User user = (User) authentication.getPrincipal();

        List<Map<String, Object>> artists = user.getTopArtists().stream()
                .map(artist -> {
                    Map<String, Object> artistMap = new HashMap<>();
                    artistMap.put("id", artist.getId());
                    artistMap.put("spotifyId", artist.getSpotifyId());
                    artistMap.put("name", artist.getName());
                    artistMap.put("popularity", artist.getPopularity());
                    artistMap.put("genres", artist.getGenres());
                    return artistMap;
                })
                .collect(Collectors.toList());

        return ResponseEntity.ok(Map.of(
                "artists", artists,
                "totalArtists", artists.size()
        ));
    }

    @GetMapping("/get-token")
    public String getTokenPage() {
        return """
        <!DOCTYPE html>
        <html>
        <head><title>Getting Token...</title></head>
        <body style='font-family: Arial; text-align: center; padding: 50px;'>
            <h2>Getting your authentication token...</h2>
            <p><a href='/oauth2/authorization/spotify'>Click here to authenticate with Spotify</a></p>
            <p>After authentication, copy your token and paste it in React.</p>
        </body>
        </html>
        """;
    }
}