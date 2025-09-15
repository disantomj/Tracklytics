package com.example.tracklytics.user;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.Map;
import java.util.Optional;

@RestController
public class UserController {

    @Autowired
    private UserService userService;

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
            // FIXED: Use the injected userService instead of creating null UserRepository
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
                            "<p><a href='/user-info'>View Raw Spotify Data</a> | <a href='/profile'>Database Profile</a> | <a href='/'>Home</a></p>" +
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
    public String loginPage(@RequestParam(value = "error", required = false) String error) {
        if (error != null) {
            System.err.println("Login error occurred: " + error);
            return "<h1>Login Error</h1>" +
                    "<p>There was an error during login: " + error + "</p>" +
                    "<p><a href='/oauth2/authorization/spotify'>Try Login Again</a></p>" +
                    "<p><a href='/'>Go Home</a></p>" +
                    "<p><strong>Tip:</strong> If login fails, try again - sometimes it works on the second attempt!</p>";
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
}