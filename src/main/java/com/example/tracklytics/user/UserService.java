package com.example.tracklytics.user;

import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public Optional<User> findBySpotifyId(String spotifyId) {
        return userRepository.findBySpotifyId(spotifyId);
    }

    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    public User saveUser(User user) {
        return userRepository.save(user);
    }

    public boolean existsBySpotifyId(String spotifyId) {
        return userRepository.findBySpotifyId(spotifyId).isPresent();
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public List<User> findUsersNeedingSync() {
        // You can implement custom query here if needed
        return userRepository.findAll();
    }
}
