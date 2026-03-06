package com.medicalml.security;

import io.jsonwebtoken.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Date;
import java.util.UUID;

/**
 * JWT Token Provider — handles creation, parsing, and validation of JSON Web
 * Tokens.
 *
 * Uses RS256 (RSA + SHA-256) asymmetric signing:
 * - Private key: Used to SIGN tokens (kept secret on the server).
 * - Public key: Used to VERIFY token signatures (can be shared safely).
 *
 * Token types:
 * - Access token: Short-lived (default: 15 minutes), contains username + role.
 * - Refresh token: Long-lived (default: 7 days), contains only username.
 *
 * Note: In production, the RSA key pair should be loaded from a persistent
 * keystore (e.g., AWS KMS, HashiCorp Vault) rather than generated at startup.
 */
@Component
public class JwtTokenProvider {

    /** RSA private key for signing tokens. */
    private final PrivateKey privateKey;

    /** RSA public key for verifying token signatures. */
    private final PublicKey publicKey;

    /** Access token expiry in milliseconds (default: 900000 = 15 minutes). */
    @Value("${jwt.access-token-expiry:900000}")
    private long accessTokenExpiry;

    /** Refresh token expiry in milliseconds (default: 604800000 = 7 days). */
    @Value("${jwt.refresh-token-expiry:604800000}")
    private long refreshTokenExpiry;

    /**
     * Constructor — generates a 2048-bit RSA key pair on startup.
     * This means tokens are invalidated every time the server restarts (acceptable
     * for dev).
     */
    public JwtTokenProvider() {
        try {
            KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
            generator.initialize(2048);
            KeyPair keyPair = generator.generateKeyPair();
            this.privateKey = keyPair.getPrivate();
            this.publicKey = keyPair.getPublic();
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate RSA key pair", e);
        }
    }

    /**
     * Generate a short-lived access token for API authentication.
     *
     * Token claims:
     * - sub (subject): username
     * - role: user's role (e.g., "ADMIN", "USER")
     * - type: "access"
     * - jti: unique token ID (UUID)
     * - iat: issued at timestamp
     * - exp: expiration timestamp
     *
     * @param username The authenticated user's username
     * @param role     The user's role
     * @return Signed JWT access token string
     */
    public String generateAccessToken(String username, String role) {
        return Jwts.builder()
                .subject(username)
                .claim("role", role)
                .claim("type", "access")
                .id(UUID.randomUUID().toString())
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + accessTokenExpiry))
                .signWith(privateKey, Jwts.SIG.RS256)
                .compact();
    }

    /**
     * Generate a long-lived refresh token for obtaining new access tokens.
     *
     * Contains only the username (no role) since the role is looked up fresh
     * from the database during token refresh.
     *
     * @param username The authenticated user's username
     * @return Signed JWT refresh token string
     */
    public String generateRefreshToken(String username) {
        return Jwts.builder()
                .subject(username)
                .claim("type", "refresh")
                .id(UUID.randomUUID().toString())
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + refreshTokenExpiry))
                .signWith(privateKey, Jwts.SIG.RS256)
                .compact();
    }

    /**
     * Parse and verify a JWT token, extracting its claims payload.
     *
     * Verifies the token's RS256 signature using the public key and checks expiry.
     * Throws JwtException if the token is invalid, expired, or tampered with.
     *
     * @param token The JWT token string
     * @return The token's Claims (payload containing sub, role, exp, etc.)
     */
    public Claims parseToken(String token) {
        return Jwts.parser()
                .verifyWith(publicKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /**
     * Check whether a token is valid (properly signed and not expired).
     *
     * @param token The JWT token string to validate
     * @return true if the token is valid, false otherwise
     */
    public boolean validateToken(String token) {
        try {
            parseToken(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    /**
     * Extract the username (subject claim) from a token.
     *
     * @param token The JWT token string
     * @return The username stored in the token's subject claim
     */
    public String getUsernameFromToken(String token) {
        return parseToken(token).getSubject();
    }

    /**
     * Extract the user role from a token.
     *
     * @param token The JWT token string
     * @return The role stored in the token's "role" claim (e.g., "ADMIN", "USER")
     */
    public String getRoleFromToken(String token) {
        return parseToken(token).get("role", String.class);
    }
}
