package com.master.springboot.Controller;

import com.master.springboot.Models.Perfiles;
import com.master.springboot.Models.Usuarios;
import com.master.springboot.service.ServicePerfiles;
import com.master.springboot.service.ServiceUsuarios;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
public class UsuariosController {

    @Autowired
    private ServiceUsuarios serviceUsuarios;

    @Autowired
    private ServicePerfiles servicePerfiles;

    // ── GET /api/usuarios ─────────────────────────────────────
    @GetMapping("/api/usuarios")
    public ResponseEntity<?> mostrarUsuarios() {
        try {
            List<Usuarios> usuarios = serviceUsuarios.findAll();
            Map<String, Object> resp = new HashMap<>();
            resp.put("success", true);
            resp.put("data", usuarios);
            resp.put("Total", usuarios.size());
            return ResponseEntity.ok(resp);
        } catch (Exception e) {
            return error("Error al obtener usuarios: " + e.getMessage(),
                         HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // ── GET /api/usuario/{id} ─────────────────────────────────
    @GetMapping("/api/usuario/{id}")
    public ResponseEntity<?> obtenerPorId(@PathVariable UUID id) {
        try {
            Usuarios u = serviceUsuarios.findById(id);
            if (u == null) return error("Usuario no encontrado", HttpStatus.NOT_FOUND);
            return ResponseEntity.ok(u);
        } catch (Exception e) {
            return error("Error: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // ── GET /api/usuarios/buscar ──────────────────────────────
    @GetMapping("/api/usuarios/buscar")
    public ResponseEntity<?> buscarUsuarios(
            @RequestParam(required = false) String query,
            @RequestParam(required = false) UUID perfilId) {
        try {
            List<Usuarios> usuarios = serviceUsuarios.buscar(query, perfilId);

            Map<String, Object> resp = new HashMap<>();
            resp.put("success", true);
            resp.put("data", usuarios);
            resp.put("Total", usuarios.size());
            return ResponseEntity.ok(resp);

        } catch (Exception e) {
            return error("Error en la búsqueda: " + e.getMessage(),
                         HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


    // ── POST /api/usuarios/{id}/foto ─────────────────────────
    @PostMapping("/api/usuarios/{id}/foto")
    public ResponseEntity<?> subirFoto(@PathVariable UUID id,
                                       @RequestParam("foto") org.springframework.web.multipart.MultipartFile foto) {
        try {
            Usuarios usuario = serviceUsuarios.findById(id);
            if (usuario == null) {
                return error("Usuario no encontrado", HttpStatus.NOT_FOUND);
            }

            // Validar que sea imagen
            String contentType = foto.getContentType();
            if (contentType == null || !contentType.startsWith("image/")) {
                return error("Solo se permiten imágenes", HttpStatus.BAD_REQUEST);
            }

            // Validar tamaño (máximo 2MB)
            if (foto.getSize() > 2 * 1024 * 1024) {
                return error("La imagen no debe exceder 2MB", HttpStatus.BAD_REQUEST);
            }

            // Convertir a Base64
            byte[] fotoBytes = foto.getBytes();
            String fotoBase64 = java.util.Base64.getEncoder().encodeToString(fotoBytes);

            usuario.setFoto(fotoBase64);
            usuario.setFotoTipo(contentType);
            serviceUsuarios.save(usuario);

            Map<String, Object> resp = new HashMap<>();
            resp.put("success", true);
            resp.put("message", "Foto actualizada correctamente");
            return ResponseEntity.ok(resp);

        } catch (Exception e) {
            return error("Error al subir foto: " + e.getMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // ── DELETE /api/usuarios/{id}/foto ───────────────────────
    @DeleteMapping("/api/usuarios/{id}/foto")
    public ResponseEntity<?> eliminarFoto(@PathVariable UUID id) {
        try {
            Usuarios usuario = serviceUsuarios.findById(id);
            if (usuario == null) {
                return error("Usuario no encontrado", HttpStatus.NOT_FOUND);
            }

            usuario.setFoto(null);
            usuario.setFotoTipo(null);
            serviceUsuarios.save(usuario);

            Map<String, Object> resp = new HashMap<>();
            resp.put("success", true);
            resp.put("message", "Foto eliminada correctamente");
            return ResponseEntity.ok(resp);

        } catch (Exception e) {
            return error("Error al eliminar foto: " + e.getMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // ── GET /api/usuarios/{id}/foto ──────────────────────────
    @GetMapping("/api/usuarios/{id}/foto")
    public ResponseEntity<?> obtenerFoto(@PathVariable UUID id) {
        try {
            Usuarios usuario = serviceUsuarios.findById(id);
            if (usuario == null) {
                return error("Usuario no encontrado", HttpStatus.NOT_FOUND);
            }

            Map<String, Object> resp = new HashMap<>();
            resp.put("success", true);
            resp.put("hasFoto", usuario.getFoto() != null);
            resp.put("foto", usuario.getFoto());
            resp.put("fotoTipo", usuario.getFotoTipo());
            return ResponseEntity.ok(resp);

        } catch (Exception e) {
            return error("Error al obtener foto: " + e.getMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // ── POST /api/usuarios ─────────────────────────────────────
    @PostMapping("/api/usuarios")
    public ResponseEntity<?> crearUsuario(@RequestBody Map<String, Object> body) {
        Map<String, Object> resp = new HashMap<>();
        try {
            String usuarioNombre = getString(body, "usuario");
            String email         = getString(body, "email");
            String password      = getString(body, "password");
            String estado        = getString(body, "estado");
            Object perfilRaw     = body.get("perfilId");

            // Validaciones
            if (usuarioNombre == null || usuarioNombre.isBlank()) {
                return error("El nombre de usuario es requerido", HttpStatus.BAD_REQUEST);
            }
            if (password == null || password.isBlank()) {
                return error("La contraseña es requerida", HttpStatus.BAD_REQUEST);
            }
            if (serviceUsuarios.existsByUsuario(usuarioNombre.trim())) {
                return error("El nombre de usuario ya está en uso", HttpStatus.CONFLICT);
            }

            Usuarios nuevo = new Usuarios();
            nuevo.setUsuario(usuarioNombre.trim());
            nuevo.setEmail(email != null ? email.trim() : null);
            nuevo.setEstado(estado != null ? estado.trim() : "ACTIVO");
            nuevo.setPassword(md5(password));

            if (perfilRaw != null) {
                UUID perfilId = UUID.fromString(perfilRaw.toString());
                Perfiles perfil = servicePerfiles.findById(perfilId);
                if (perfil == null) {
                    return error("Perfil no encontrado", HttpStatus.BAD_REQUEST);
                }
                nuevo.setPerfil(perfil);
            }

            Usuarios guardado = serviceUsuarios.save(nuevo);

            resp.put("success", true);
            resp.put("message", "Usuario creado correctamente");
            resp.put("data", guardado);
            return ResponseEntity.status(HttpStatus.CREATED).body(resp);

        } catch (Exception e) {
            e.printStackTrace();
            return error("Error al crear usuario: " + e.getMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // ── PUT /api/usuarios/{id} ────────────────────────────────
    @PutMapping("/api/usuarios/{id}")
    public ResponseEntity<?> actualizarUsuario(@PathVariable UUID id,
                                               @RequestBody Map<String, Object> body) {
        Map<String, Object> resp = new HashMap<>();
        try {
            Usuarios existente = serviceUsuarios.findById(id);
            if (existente == null)
                return error("Usuario no encontrado", HttpStatus.NOT_FOUND);

            String usuarioNombre = getString(body, "usuario");
            String email         = getString(body, "email");
            String password      = getString(body, "password");
            String estado        = getString(body, "estado");
            Object perfilRaw     = body.get("perfilId");

            // Validar nombre único (excluyendo el actual)
            if (usuarioNombre != null && !usuarioNombre.isBlank()) {
                if (serviceUsuarios.existsByUsuarioAndIdNot(usuarioNombre.trim(), id))
                    return error("El nombre de usuario ya está en uso", HttpStatus.CONFLICT);
                existente.setUsuario(usuarioNombre.trim());
            }
            if (email   != null && !email.isBlank())   existente.setEmail(email.trim());
            if (estado  != null && !estado.isBlank())  existente.setEstado(estado.trim());
            if (password != null && !password.isBlank()) existente.setPassword(md5(password));

            if (perfilRaw != null) {
                UUID perfilId = UUID.fromString(perfilRaw.toString());
                Perfiles perfil = servicePerfiles.findById(perfilId);
                if (perfil == null)
                    return error("Perfil no encontrado", HttpStatus.BAD_REQUEST);
                existente.setPerfil(perfil);
            }

            Usuarios actualizado = serviceUsuarios.save(existente);

            resp.put("success", true);
            resp.put("message", "Usuario actualizado correctamente");
            resp.put("data", actualizado);
            return ResponseEntity.ok(resp);

        } catch (Exception e) {
            e.printStackTrace();
            return error("Error al actualizar: " + e.getMessage(),
                         HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // ── DELETE /api/usuarios/{id} ─────────────────────────────
    @DeleteMapping("/api/usuarios/{id}")
    public ResponseEntity<?> eliminarUsuario(@PathVariable UUID id) {
        try {
            if (serviceUsuarios.findById(id) == null)
                return error("Usuario no encontrado", HttpStatus.NOT_FOUND);

            serviceUsuarios.delete(id);

            Map<String, Object> resp = new HashMap<>();
            resp.put("success", true);
            resp.put("message", "Usuario eliminado correctamente");
            return ResponseEntity.ok(resp);

        } catch (Exception e) {
            e.printStackTrace();
            return error("Error al eliminar usuario: " + e.getMessage(),
                         HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // ── Helpers ──────────────────────────────────────────────
    private String getString(Map<String, Object> body, String key) {
        Object val = body.get(key);
        return val != null ? val.toString() : null;
    }

    private ResponseEntity<Map<String, Object>> error(String msg, HttpStatus status) {
        Map<String, Object> resp = new HashMap<>();
        resp.put("success", false);
        resp.put("message", msg);
        return ResponseEntity.status(status).body(resp);
    }

    private String md5(String input) {
        try {
            java.security.MessageDigest md = java.security.MessageDigest.getInstance("MD5");
            byte[] digest = md.digest(input.getBytes());
            java.math.BigInteger no = new java.math.BigInteger(1, digest);
            String hash = no.toString(16);
            while (hash.length() < 32) hash = "0" + hash;
            return hash;
        } catch (java.security.NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }
}
