package com.pding.paymentservice.security.jwt;

import com.pding.paymentservice.exception.ExpiredJwtTokenException;
import com.pding.paymentservice.exception.InvalidJwtTokenException;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.List;

@Component
public class JwtUtils {
    private static final Logger logger = LoggerFactory.getLogger(JwtUtils.class);

    @Value("${com.pding.jwtSecret}")
    private String jwtSecret;

    @Value("${com.pding.jwtExpirationMs}")
    private int jwtExpirationMs;

    @Value("${com.pding.jwtCookieName}")
    private String jwtCookie;

    public String getUserNameFromJwtToken(String token) {
        //return Jwts.parser().setSigningKey(jwtSecret).parseClaimsJws(token).getBody().getSubject();
        try {
            Claims claims = Jwts.parser()
                    .setSigningKey(key()) // Use the key() method to get the correct key
                    .parseClaimsJws(token)
                    .getBody();

            return claims.getSubject();
        } catch (Exception e) {
            logger.error("Error parsing JWT token: {}", e.getMessage());
            return null;
        }
    }

    public Claims getAllClaimsFromToken(String token) {
        return Jwts.parser()
                .setSigningKey(key())
                .parseClaimsJws(token)
                .getBody();
    }

    public String getUserIdFromToken(String token) {
        Claims claims = getAllClaimsFromToken(token);
        return (String) claims.get("userid");
    }

    public String getUsernameFromToken(String token) {
        Claims claims = getAllClaimsFromToken(token);
        return claims.getSubject();
    }

    public List<String> getUserRolesFromToken(String token) {
        Claims claims = getAllClaimsFromToken(token);
        // Assuming roles are stored as a List of Strings in the claims
        return (List<String>) claims.get("roles");
    }

    private Key key() {
        try {
            return Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
        } catch (Exception e) {
            logger.error(jwtSecret, e.getMessage());
            return null;
        }
    }


    public boolean validateJwtToken(String authToken) {
        try {
            Claims claims = Jwts.parser()
                    .setSigningKey(key()) // Use the key() method to get the correct key
                    .parseClaimsJws(authToken)
                    .getBody();

            if (claims.size() > 0) {
                return true;
            }
        } catch (MalformedJwtException e) {
            logger.error("Invalid JWT token: {}", e.getMessage());
            throw new InvalidJwtTokenException("Invalid JWT token," + e.getMessage());
        } catch (ExpiredJwtException e) {
            logger.error("JWT token is expired: {}", e.getMessage());
            throw new ExpiredJwtTokenException("JWT token is expired," + e.getMessage());
        } catch (UnsupportedJwtException e) {
            logger.error("JWT token is unsupported: {}", e.getMessage());
            throw new InvalidJwtTokenException("Unsupported JWT token," + e.getMessage());
        } catch (IllegalArgumentException e) {
            logger.error("JWT claims string is empty: {}", e.getMessage());
            throw new InvalidJwtTokenException("JWT claims string is empty," + e.getMessage());
        } catch (JwtException e) {
            logger.error("JWT Eception: {}", e.getMessage());
            throw new InvalidJwtTokenException("JWT Eception," + e.getMessage());
        }

        return false;
    }
}