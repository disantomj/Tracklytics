package com.example.tracklytics.authentication;

import com.example.tracklytics.user.User;
import com.example.tracklytics.user.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2RefreshToken;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;

@Service
public class CustomOAuth2UserService implements OAuth2UserService<OAuth2UserRequest, OAuth2User> {

    @Autowired
    private UserService userService;

    @Autowired
    private OAuth2AuthorizedClientService authorizedClientService;

    private final DefaultOAuth2UserService delegate = new DefaultOAuth2UserService();

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        try {
            System.out.println("Loading user from Spotify...");

            OAuth2User oauth2User = delegate.loadUser(userRequest);
            Map<String, Object> attributes = oauth2User.getAttributes();

            System.out.println("Successfully loaded user attributes: " + attributes);

            // Extract user info from Spotify
            String spotifyId = (String) attributes.get("id");
            String email = (String) attributes.get("email");
            String displayName = (String) attributes.get("display_name");

            // Get access token from the request
            String accessToken = userRequest.getAccessToken().getTokenValue();
            Instant tokenExpiry = userRequest.getAccessToken().getExpiresAt();

            // Try to get refresh token from authorized client
            String refreshToken = null;
            try {
                // Create a temporary authorized client to access refresh token
                OAuth2AuthorizedClient authorizedClient = new OAuth2AuthorizedClient(
                        userRequest.getClientRegistration(),
                        spotifyId,
                        userRequest.getAccessToken()
                );

                // Check if there's a refresh token
                OAuth2RefreshToken refreshTokenObj = authorizedClient.getRefreshToken();
                if (refreshTokenObj != null) {
                    refreshToken = refreshTokenObj.getTokenValue();
                    System.out.println("Refresh token captured successfully");
                } else {
                    System.out.println("No refresh token available - this is normal for some OAuth2 flows");
                }
            } catch (Exception e) {
                System.out.println("Could not retrieve refresh token: " + e.getMessage());
            }

            // Check if user exists, if not create new user
            User user;
            Optional<User> existingUser = userService.findBySpotifyId(spotifyId);

            if (existingUser.isPresent()) {
                System.out.println("Existing user found, updating info...");
                user = existingUser.get();
                // Update user info in case it changed
                user.setEmail(email);
                user.setDisplayName(displayName);
            } else {
                System.out.println("New user, creating account...");
                user = new User(spotifyId, displayName, email);
            }

            // Set token information
            user.setAccessToken(accessToken);
            user.setTokenExpiry(tokenExpiry);
            user.setRefreshToken(refreshToken);

            System.out.println("Token info - Access: " + (accessToken != null ? "Present" : "Missing") +
                    ", Refresh: " + (refreshToken != null ? "Present" : "Missing") +
                    ", Expires: " + tokenExpiry);

            // Save user to database
            User savedUser = userService.saveUser(user);
            System.out.println("User saved with ID: " + savedUser.getId());

            return new DefaultOAuth2User(
                    Collections.singleton(() -> "USER"),
                    attributes,
                    "id"
            );

        } catch (Exception e) {
            System.err.println("Error processing OAuth2 user: " + e.getMessage());
            e.printStackTrace();
            throw new OAuth2AuthenticationException("Failed to process user: " + e.getMessage());
        }
    }
}