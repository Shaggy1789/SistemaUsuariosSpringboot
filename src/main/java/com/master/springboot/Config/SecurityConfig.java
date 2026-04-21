// com/master/springboot/config/SecurityConfig.java
package com.master.springboot.config;

import com.master.springboot.security.JwtAuthenticationFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Autowired
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // Deshabilitar CSRF (necesario para APIs REST)
                .csrf(csrf -> csrf.disable())

                // Mantener STATELESS para JWT (no interfiere con HttpSession)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // Configurar rutas públicas y protegidas
                .authorizeHttpRequests(auth -> auth
                        // ✅ TODAS las rutas son PÚBLICAS por defecto
                        .requestMatchers(
                                "/**",                    // TODO el sistema
                                "/api/**",                // Todas las APIs
                                "/css/**",                // Estilos
                                "/js/**",                 // Scripts
                                "/images/**",             // Imágenes
                                "/login",                 // Página de login
                                "/api/login",             // Login normal (HttpSession)
                                "/api/auth/login",        // Login JWT
                                "/error",                 // Página de error
                                "/health"                 // Health check
                        ).permitAll()
                        // Si quieres proteger rutas específicas con JWT, descomenta:
                        // .requestMatchers("/api/usuarios/**").authenticated()
                        .anyRequest().permitAll()
                )

                // Deshabilitar formulario de login por defecto de Spring Security
                .formLogin(form -> form.disable())

                // Deshabilitar autenticación básica HTTP
                .httpBasic(basic -> basic.disable())

                // Agregar filtro JWT (solo actuará si hay token en el header)
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}