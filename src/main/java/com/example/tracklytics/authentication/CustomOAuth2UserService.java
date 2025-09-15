package com.example.tracklytics;

import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.Collections;

public class CustomOAuth2UserService implements OAuth2UserService<OAuth2UserRequest, OAuth2User> {

    private final DefaultOAuth2UserService delegate = new DefaultOAuth2UserService();

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        try {
            System.out.println("Loading user from Spotify...");
            System.out.println("Access Token: " + userRequest.getAccessToken().getTokenValue());

            OAuth2User oauth2User = delegate.loadUser(userRequest);

            System.out.println("Successfully loaded user attributes: " + oauth2User.getAttributes());

            // Spotify uses 'id' as the unique identifier
            return new DefaultOAuth2User(
                    Collections.singleton(() -> "USER"),
                    oauth2User.getAttributes(),
                    "id"
            );

        } catch (Exception e) {
            System.err.println("Error loading user from Spotify: " + e.getMessage());
            e.printStackTrace();
            throw new OAuth2AuthenticationException("Failed to load user from Spotify: " + e.getMessage());
        }
    }
}