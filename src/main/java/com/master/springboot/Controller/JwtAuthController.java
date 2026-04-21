// com/master/springboot/Controller/JwtAuthController.java
package com.master.springboot.Controller;

import com.master.springboot.Models.Usuarios;
import com.master.springboot.security.JwtUtil;
import com.master.springboot.service.AuthCaptchaService;
import com.master.springboot.service.ServiceUsuarios;
import io.jsonwebtoken.Claims;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class JwtAuthController {

    @Autowired
    private ServiceUsuarios serviceUsuarios;

    @Autowired
    private AuthCaptchaService authCaptchaService;

    @Autowired
    private JwtUtil jwtUtil;

    // ── POST /api/auth/login ───────────────────────────────────────
    @PostMapping("/login")
    public ResponseEntity<?> loginJWT(
            @RequestParam String usuario,
            @RequestParam String password,
            @RequestParam("g-recaptcha-response") String recaptchaResponse) {

        Map<String, Object> response = new HashMap<>();

        // 1. Verificar reCAPTCHA
        if (!authCaptchaService.verifyRecaptcha(recaptchaResponse)) {
            response.put("success", false);
            response.put("message", "Error en la verificación de seguridad.");
            response.put("error", "captcha");
            return ResponseEntity.badRequest().body(response);
        }

        // 2. Buscar usuario en BD
        List<Usuarios> usuarios = serviceUsuarios.findAll();

        for (Usuarios u : usuarios) {
            if (u.getUsuario().equals(usuario)) {

                // 3. Validar estado - SOLO USUARIOS ACTIVOS
                if (u.getEstado() == null || !"ACTIVO".equalsIgnoreCase(u.getEstado())) {
                    response.put("success", false);
                    response.put("message", "Tu cuenta está INACTIVA. Contacta al administrador.");
                    response.put("error", "inactivo");
                    return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
                }

                // 4. Verificar contraseña (MD5)
                String hashedPassword = md5(password);
                if (u.getPassword() != null && u.getPassword().equals(hashedPassword)) {

                    // 5. Generar JWT
                    String perfilNombre = u.getPerfil() != null ? u.getPerfil().getNombre() : "SIN_PERFIL";
                    String token = jwtUtil.generarToken(
                            u.getId().toString(),
                            u.getUsuario(),
                            perfilNombre,
                            u.getEstado()
                    );

                    // 6. Construir respuesta
                    Map<String, Object> datosUsuario = new HashMap<>();
                    datosUsuario.put("id", u.getId().toString());
                    datosUsuario.put("usuario", u.getUsuario());
                    datosUsuario.put("email", u.getEmail());
                    datosUsuario.put("estado", u.getEstado());
                    datosUsuario.put("perfilId", u.getPerfil() != null ? u.getPerfil().getId().toString() : null);
                    datosUsuario.put("perfilNombre", perfilNombre);

                    response.put("success", true);
                    response.put("message", "Login exitoso");
                    response.put("token", token);
                    response.put("tokenType", "Bearer");
                    response.put("expiresIn", 86400); // 24 horas en segundos
                    response.put("usuario", datosUsuario);
                    return ResponseEntity.ok(response);

                } else {
                    response.put("success", false);
                    response.put("message", "Contraseña incorrecta.");
                    response.put("error", "password");
                    return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
                }
            }
        }

        // Usuario no encontrado
        response.put("success", false);
        response.put("message", "El usuario no existe.");
        response.put("error", "usuario");
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
    }

    // ── POST /api/auth/refresh ─────────────────────────────────────
    @PostMapping("/refresh")
    public ResponseEntity<?> refreshToken(@RequestHeader("Authorization") String authHeader) {
        Map<String, Object> response = new HashMap<>();

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            response.put("success", false);
            response.put("message", "Token no proporcionado");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }

        String token = authHeader.substring(7);
        Claims claims = jwtUtil.validarToken(token);

        if (claims == null) {
            response.put("success", false);
            response.put("message", "Token inválido");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }

        // Generar nuevo token con los mismos datos
        String newToken = jwtUtil.generarToken(
                (String) claims.get("userId"),
                claims.getSubject(),
                (String) claims.get("perfil"),
                (String) claims.get("estado")
        );

        response.put("success", true);
        response.put("token", newToken);
        response.put("tokenType", "Bearer");
        response.put("expiresIn", 86400);
        return ResponseEntity.ok(response);
    }

    // ── GET /api/auth/validate ─────────────────────────────────────
    @GetMapping("/validate")
    public ResponseEntity<?> validateToken(@RequestHeader(value = "Authorization", required = false) String authHeader) {
        Map<String, Object> response = new HashMap<>();

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            response.put("success", true);
            response.put("valid", false);
            response.put("message", "Token no proporcionado");
            return ResponseEntity.ok(response);
        }

        String token = authHeader.substring(7);
        Claims claims = jwtUtil.validarToken(token);

        if (claims != null && !jwtUtil.tokenExpirado(token)) {
            response.put("success", true);
            response.put("valid", true);
            response.put("username", claims.get("username"));
            response.put("perfil", claims.get("perfil"));
            response.put("userId", claims.get("userId"));
            return ResponseEntity.ok(response);
        } else {
            response.put("success", true);
            response.put("valid", false);
            response.put("message", "Token inválido o expirado");
            return ResponseEntity.ok(response);
        }
    }

    // ── MD5 helper ────────────────────────────────────────────
    private String md5(String input) {
        try {
            java.security.MessageDigest md = java.security.MessageDigest.getInstance("MD5");
            byte[] messageDigest = md.digest(input.getBytes());
            java.math.BigInteger no = new java.math.BigInteger(1, messageDigest);
            String hashtext = no.toString(16);
            while (hashtext.length() < 32) hashtext = "0" + hashtext;
            return hashtext;
        } catch (java.security.NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }
}