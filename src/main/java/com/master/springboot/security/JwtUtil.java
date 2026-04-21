// com/master/springboot/security/JwtUtil.java
package com.master.springboot.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Component
public class JwtUtil {
    // Clave secreta (en producción debe estar en application.properties)
    private static final String SECRET_KEY = "SantaMonica2026SecretKeyJWT256Bits!!!";
    private static final long EXPIRATION_TIME = 86400000; // 24 horas

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(SECRET_KEY.getBytes(StandardCharsets.UTF_8));
    }
    /**
     * Genera un token JWT con los datos del usuario
     */
    public String generarToken(String userId, String username, String perfil, String estado) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);
        claims.put("username", username);
        claims.put("perfil", perfil);
        claims.put("estado", estado);

        return Jwts.builder()
                .claims(claims)
                .subject(username)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .signWith(getSigningKey())
                .compact();
    }

    /**
     * Valida y extrae los claims de un token JWT
     */
    public Claims validarToken(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(getSigningKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (JwtException e) {
            return null;
        }
    }
    /**
     * Verifica si un token ha expirado
     */
    public boolean tokenExpirado(String token) {
        Claims claims = validarToken(token);
        return claims == null || claims.getExpiration().before(new Date());
    }

    /**
     * Extrae el username del token
     */
    public String getUsernameFromToken(String token) {
        Claims claims = validarToken(token);
        return claims != null ? claims.getSubject() : null;
    }

    /**
     * Extrae el userId del token
     */
    public String getUserIdFromToken(String token) {
        Claims claims = validarToken(token);
        return claims != null ? (String) claims.get("userId") : null;
    }

    /**
     * Extrae el perfil del token
     */
    public String getPerfilFromToken(String token) {
        Claims claims = validarToken(token);
        return claims != null ? (String) claims.get("perfil") : null;
    }
}