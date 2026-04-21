// com/master/springboot/security/JwtAuthenticationFilter.java
package com.master.springboot.security;

import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    @Autowired
    private JwtUtil jwtUtil;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String authHeader = request.getHeader("Authorization");

        // ✅ Si NO hay token, simplemente continuar (NO bloquear)
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        // Si hay token, validarlo
        String token = authHeader.substring(7);
        Claims claims = jwtUtil.validarToken(token);

        if (claims != null && !jwtUtil.tokenExpirado(token)) {
            String username = claims.getSubject();
            String perfil = (String) claims.get("perfil");

            // Crear autenticación (solo para contexto de Spring Security)
            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(
                            username,
                            null,
                            Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + perfil))
                    );
            SecurityContextHolder.getContext().setAuthentication(authentication);
        }

        // ✅ SIEMPRE continuar (incluso si el token es inválido)
        filterChain.doFilter(request, response);
    }
}