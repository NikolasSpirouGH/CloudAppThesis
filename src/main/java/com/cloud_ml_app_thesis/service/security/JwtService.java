package com.cloud_ml_app_thesis.service.security;

import com.cloud_ml_app_thesis.config.security.RsaKeyProperties;
import io.jsonwebtoken.Jwts;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import io.jsonwebtoken.*;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.Date;
import java.util.List;


@Service
public class JwtService {
    private final RSAPublicKey publicKey;
    private final RSAPrivateKey privateKey;

    // Configure the expiration(ms) from application.yaml file.
    private final long jwtExpirationMs;

    public JwtService(
            RsaKeyProperties rsaKeyProperties,
            @Value("${jwt.expiration}") long jwtExpirationMs
    ){
        this.publicKey = rsaKeyProperties.getPublicKey();
        this.privateKey = rsaKeyProperties.getPrivateKey();
        this.jwtExpirationMs = jwtExpirationMs;
    }

    /**
     *  Create JWT token containing username and roles
     */
    public String generateToken(String username, List<String> roles) {
        long now = System.currentTimeMillis();
        //TODO tokens containing manager or admin role can last shorter
        return Jwts.builder()
                .subject(username)
                .claim("roles", roles)
                .issuedAt(new Date(now))
                .expiration(new Date(now + jwtExpirationMs))
                .signWith(privateKey) // Direct signing with the private key
                .compact();
    }


    /**
     * Parse the JWT token and extract claims.
     *
     * @param token the JWT token
     * @return the claims in the token
     */
    public Claims parseToken(String token) {
        try {
            // Create a JwtParser instance
            JwtParserBuilder parser = Jwts.parser().verifyWith(publicKey); // Verify the signature using the public key

            // Parse the token and extract the claims
            Jws<Claims> jwsClaims = parser.build().parseSignedClaims(token);
            return jwsClaims.getPayload();
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse or validate JWT token", e);
        }
    }
}
