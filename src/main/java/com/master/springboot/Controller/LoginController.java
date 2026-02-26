package com.master.springboot.Controller;

import com.master.springboot.Models.Roles;
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

@RestController
@RequestMapping("/api")
public class LoginController {

    @Autowired
    private ServiceUsuarios serviceUsuarios;

    @Autowired
    private AuthCaptchaService authCaptchaService;

    @PostMapping("/login")
    public ResponseEntity<?> procesarLogin(
            @RequestParam String nombreusuario,
            @RequestParam String password,
            @RequestParam("g-recaptcha-response") String recaptchaResponse,
            HttpSession session) {

        Map<String, Object> response = new HashMap<>();

        if (!authCaptchaService.verifyRecaptcha(recaptchaResponse)) {
            response.put("success", false);
            response.put("message", "Error en el inicio de sesión");
            response.put("error", "captcha");
            return ResponseEntity.badRequest().body(response);
        }

        List<Usuarios> usuarios = serviceUsuarios.findAll();

        for (Usuarios usuario : usuarios) {
            if (usuario.getNombreusuario().equals(nombreusuario)) {
                String hashedPassword = md5(password);

                if (usuario.getPassword() != null &&
                        usuario.getPassword().equals(hashedPassword)) {

                    session.setAttribute("usuario", usuario);

                    response.put("success", true);
                    response.put("message", "Login exitoso");
                    response.put("redirect", "/");

                    Map<String, Object> datosUsuario = new HashMap<>();
                    datosUsuario.put("id", usuario.getIdusuario());
                    datosUsuario.put("nombre", usuario.getNombreusuario());
                    datosUsuario.put("email", usuario.getEmail());
                    response.put("usuario", datosUsuario);

                    return ResponseEntity.ok(response);
                } else {
                    response.put("success", false);
                    response.put("message", "Error en el inicio de sesión");
                    response.put("error", "password");
                    return ResponseEntity.badRequest().body(response);
                }
            }
        }

        response.put("success", false);
        response.put("message", "Error en el inicio de sesión");
        response.put("error", "usuario");
        return ResponseEntity.badRequest().body(response);
    }

    @PostMapping("/registro")
    public ResponseEntity<?> procesarRegistro(
            @RequestParam(required = false) Integer idusuario,
            @RequestParam String nombre,
            @RequestParam String apellidopaterno,
            @RequestParam String apellidomaterno,
            @RequestParam String email,
            @RequestParam(required = false) String password,
            @RequestParam long telefono,
            @RequestParam(value = "g-recaptcha-response", required = false) String recaptchaResponse) {

        Map<String, Object> response = new HashMap<>();

        if (idusuario != null && idusuario > 0) {
            try {
                Usuarios usuarioExistente = serviceUsuarios.findById(idusuario);
                if (usuarioExistente == null) {
                    response.put("success", false);
                    response.put("message", "Usuario no encontrado");
                    return ResponseEntity.badRequest().body(response);
                }

                usuarioExistente.setNombreusuario(nombre);
                usuarioExistente.setApellidopaterno(apellidopaterno);
                usuarioExistente.setApellidomaterno(apellidomaterno);
                usuarioExistente.setEmail(email);
                usuarioExistente.setTelefono(telefono);

                if (password != null && !password.isEmpty()) {
                    usuarioExistente.setPassword(md5(password));
                }

                serviceUsuarios.save(usuarioExistente);

                response.put("success", true);
                response.put("message", "Usuario actualizado correctamente");
                return ResponseEntity.ok(response);

            } catch (Exception e) {
                e.printStackTrace();
                response.put("success", false);
                response.put("message", "Error al actualizar: " + e.getMessage());
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
            }
        } else {
            if (!authCaptchaService.verifyRecaptcha(recaptchaResponse)) {
                response.put("success", false);
                response.put("message", "CAPTCHA inválido, por favor trata de nuevo");
                response.put("error", "captcha");
                return ResponseEntity.badRequest().body(response);
            }

            List<Usuarios> usuarios = serviceUsuarios.findAll();
            for (Usuarios usuario : usuarios) {
                if (usuario.getNombreusuario().equals(nombre)) {
                    response.put("success", false);
                    response.put("message", "El usuario ya existe");
                    response.put("error", "usuario_existente");
                    return ResponseEntity.badRequest().body(response);
                }
            }

            try {
                Usuarios nuevoUsuario = new Usuarios();
                nuevoUsuario.setNombreusuario(nombre);
                nuevoUsuario.setApellidopaterno(apellidopaterno);
                nuevoUsuario.setApellidomaterno(apellidomaterno);
                nuevoUsuario.setEmail(email);
                nuevoUsuario.setPassword(md5(password));
                nuevoUsuario.setTelefono(telefono);

                Roles role = new Roles();
                role.setId(3);
                role.setNombre("Usuario");
                nuevoUsuario.setRole(role);

                serviceUsuarios.save(nuevoUsuario);

                response.put("success", true);
                response.put("message", "¡Registro exitoso!");
                response.put("redirect", "/");
                response.put("registro", "exitoso");
                response.put("usuario", nombre);
                return ResponseEntity.status(HttpStatus.CREATED).body(response);

            } catch (Exception e) {
                e.printStackTrace();
                response.put("success", false);
                response.put("message", "Error al registrar: " + e.getMessage());
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
            }
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpSession session) {
        session.invalidate();
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Adiooos :D");
        response.put("redirect", "/login");
        return ResponseEntity.ok(response);
    }

    @GetMapping("/sesion")
    public ResponseEntity<?> obtenerSesion(HttpSession session) {
        Map<String, Object> response = new HashMap<>();
        Usuarios usuario = (Usuarios) session.getAttribute("usuario");

        if (usuario != null) {
            response.put("success", true);
            response.put("usuario", usuario);
            response.put("autenticado", true);
            response.put("mensaje", "Sesión activa");
        } else {
            response.put("success", true);
            response.put("autenticado", false);
            response.put("mensaje", "No hay sesión activa");
        }
        return ResponseEntity.ok(response);
    }

    private String md5(String input) {
        try {
            java.security.MessageDigest md = java.security.MessageDigest.getInstance("MD5");
            byte[] messageDigest = md.digest(input.getBytes());
            java.math.BigInteger no = new java.math.BigInteger(1, messageDigest);
            String hashtext = no.toString(16);
            while (hashtext.length() < 32) {
                hashtext = "0" + hashtext;
            }
            return hashtext;
        } catch (java.security.NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }
}