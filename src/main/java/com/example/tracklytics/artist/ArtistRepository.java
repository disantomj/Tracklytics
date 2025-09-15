package com.example.tracklytics.artist;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ArtistRepository extends JpaRepository<Artist, Integer> {

    Optional<Artist> findBySpotifyId(String spotifyId);

    List<Artist> findByPopularityGreaterThan(Integer popularity);

    List<Artist> findByGenresContaining(String genre);
}
