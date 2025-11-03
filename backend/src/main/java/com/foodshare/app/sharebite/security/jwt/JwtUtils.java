// JwtUtils.java
package com.foodshare.app.sharebite.security.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;

@Component
public class JwtUtils {

    @Value("${sharebite.app.jwtSecret}")
    private String jwtSecret;

    @Value("${sharebite.app.jwtExpirationMs}")
    private int jwtExpirationMs;

    // Use SecretKey instead of java.security.Key for modern JJWT
    private SecretKey getSigningKey() {
        // Correctly decodes the secret key for use in signing
        byte[] keyBytes = Decoders.BASE64.decode(jwtSecret);
        // Uses the appropriate algorithm for HS512
        return Keys.hmacShaKeyFor(keyBytes);
    }

    // Generates a JWT upon successful login (ABSOLUTELY MODERN SYNTAX)
    public String generateJwtToken(Authentication authentication) {
        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();

        return Jwts.builder()
                .subject(userPrincipal.getUsername())
                .issuedAt(new Date())
                .expiration(new Date((new Date()).getTime() + jwtExpirationMs))
                // NEW: Use the secret key directly
                .signWith(getSigningKey())
                .compact();
    }

    // Extracts the username (email) from the token (ABSOLUTELY MODERN SYNTAX)
    public String getUserNameFromJwtToken(String token) {
        // NEW: Use parser().verifyWith(key).build().parseSignedClaims(token)
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getSubject();
    }

    // Validates the token's signature and expiration (ABSOLUTELY MODERN SYNTAX)
    public boolean validateJwtToken(String authToken) {
        try {
            // NEW: Use verifyWith(key)
            Jws<Claims> claims = Jwts.parser()
                    .verifyWith(getSigningKey())
                    .build()
                    .parseSignedClaims(authToken);

            return true;
        } catch (io.jsonwebtoken.security.SecurityException | MalformedJwtException e) {
            System.out.println("Invalid JWT signature or structure: " + e.getMessage());
        } catch (ExpiredJwtException e) {
            System.out.println("JWT token is expired: " + e.getMessage());
        } catch (UnsupportedJwtException e) {
            System.out.println("JWT token is unsupported: " + e.getMessage());
        } catch (IllegalArgumentException e) {
            System.out.println("JWT claims string is empty: " + e.getMessage());
        }
        return false;
    }
}