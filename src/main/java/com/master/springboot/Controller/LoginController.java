package com.master.springboot.Controller;

import com.master.springboot.Models.Usuarios;
import com.master.springboot.service.AuthCaptchaService;
import com.master.springboot.service.ServiceUsuarios;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api")
public class LoginController {

    @Autowired
    private ServiceUsuarios serviceUsuarios;

    @Autowired
    private AuthCaptchaService authCaptchaService;

    // ── POST /api/login ───────────────────────────────────────
    @PostMapping("/login")
    public ResponseEntity<?> procesarLogin(
            @RequestParam String usuario,
            @RequestParam String password,
            @RequestParam("g-recaptcha-response") String recaptchaResponse,
            HttpSession session) {

        Map<String, Object> response = new HashMap<>();

        // 1. Verificar reCAPTCHA
        if (!authCaptchaService.verifyRecaptcha(recaptchaResponse)) {
            response.put("success", false);
            response.put("message", "Error en la verificación de seguridad. Intenta de nuevo.");
            response.put("error", "captcha");
            return ResponseEntity.badRequest().body(response);
        }

        // 2. Buscar usuario en BD
        List<Usuarios> usuarios = serviceUsuarios.findAll();

        for (Usuarios u : usuarios) {
            if (u.getUsuario().equals(usuario)) {

                // 3. ✅ VALIDAR ESTADO - SOLO USUARIOS ACTIVOS
                if (u.getEstado() == null || !"ACTIVO".equalsIgnoreCase(u.getEstado())) {
                    response.put("success", false);
                    response.put("message", "Tu cuenta está INACTIVA. Contacta al administrador.");
                    response.put("error", "inactivo");
                    return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
                }

                // 4. Verificar contraseña (MD5)
                String hashedPassword = md5(password);
                if (u.getPassword() != null && u.getPassword().equals(hashedPassword)) {

                    // 5. ✅ Guardar sesión SOLO si está ACTIVO
                    session.setAttribute("usuario", u);
                    session.setMaxInactiveInterval(1800); // 30 minutos

                    // 6. Construir respuesta
                    Map<String, Object> datosUsuario = new HashMap<>();
                    datosUsuario.put("id", u.getId().toString());
                    datosUsuario.put("usuario", u.getUsuario());
                    datosUsuario.put("estado", u.getEstado());
                    if (u.getPerfil() != null) {
                        datosUsuario.put("perfilId", u.getPerfil().getId().toString());
                        datosUsuario.put("perfilNombre", u.getPerfil().getNombre());
                    }

                    response.put("success", true);
                    response.put("message", "Login exitoso");
                    response.put("redirect", "/");
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

    // ── POST /api/logout ──────────────────────────────────────
    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpSession session) {
        session.invalidate();
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Sesión cerrada correctamente");
        response.put("redirect", "/login");
        return ResponseEntity.ok(response);
    }

    // ── GET /api/sesion ───────────────────────────────────────
    @GetMapping("/sesion")
    public ResponseEntity<?> obtenerSesion(HttpSession session) {
        Map<String, Object> response = new HashMap<>();
        Usuarios usuario = (Usuarios) session.getAttribute("usuario");

        if (usuario != null) {
            Map<String, Object> datosUsuario = new HashMap<>();
            datosUsuario.put("id", usuario.getId().toString());
            datosUsuario.put("usuario", usuario.getUsuario());
            datosUsuario.put("estado", usuario.getEstado());
            if (usuario.getPerfil() != null) {
                datosUsuario.put("perfilId", usuario.getPerfil().getId().toString());
                datosUsuario.put("perfilNombre", usuario.getPerfil().getNombre());
            }

            response.put("success", true);
            response.put("autenticado", true);
            response.put("usuario", datosUsuario);
            response.put("mensaje", "Sesión activa");
        } else {
            response.put("success", true);
            response.put("autenticado", false);
            response.put("mensaje", "No hay sesión activa");
        }
        return ResponseEntity.ok(response);
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