package com.example.tracklytics.authentication;

import com.nimbusds.jose.jwk.source.ImmutableSecret;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.stereotype.Service;

import javax.crypto.spec.SecretKeySpec;
import java.time.Instant;

@Service
public class JwtService {

    @Value("${jwt.secret:tracklytics-jwt-secret-key-must-be-at-least-32-characters-long-for-security}")
    private String jwtSecret;

    @Value("${jwt.expiration:86400}") // 24 hours in seconds
    private Long jwtExpiration;

    private JwtEncoder jwtEncoder;
    private JwtDecoder jwtDecoder;

    private void initializeEncoderDecoder() {
        if (jwtEncoder == null) {
            // Use the secret string directly with ImmutableSecret
            var secret = new ImmutableSecret<>(jwtSecret.getBytes());
            this.jwtEncoder = new NimbusJwtEncoder(secret);

            // Create decoder with the same secret
            SecretKeySpec secretKey = new SecretKeySpec(jwtSecret.getBytes(), "HmacSHA256");
            this.jwtDecoder = NimbusJwtDecoder.withSecretKey(secretKey)
                    .macAlgorithm(MacAlgorithm.HS256)
                    .build();
        }
    }

    public String generateToken(String spotifyId, String displayName) {
        initializeEncoderDecoder();

        Instant now = Instant.now();
        Instant expiration = now.plusSeconds(jwtExpiration);

        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer("tracklytics")
                .subject(spotifyId)
                .claim("displayName", displayName)
                .issuedAt(now)
                .expiresAt(expiration)
                .build();

        JwtEncoderParameters parameters = JwtEncoderParameters.from(
                JwsHeader.with(MacAlgorithm.HS256).build(),
                claims
        );

        return jwtEncoder.encode(parameters).getTokenValue();
    }

    public String getSpotifyIdFromToken(String token) {
        initializeEncoderDecoder();

        try {
            Jwt jwt = jwtDecoder.decode(token);
            return jwt.getSubject();
        } catch (JwtException e) {
            throw new RuntimeException("Invalid token", e);
        }
    }

    public boolean validateToken(String token) {
        initializeEncoderDecoder();

        try {
            jwtDecoder.decode(token);
            return true;
        } catch (JwtException e) {
            return false;
        }
    }
}