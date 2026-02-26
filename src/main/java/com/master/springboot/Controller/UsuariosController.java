package com.master.springboot.Controller;

import com.master.springboot.Models.Roles;
import com.master.springboot.Models.Usuarios;
import com.master.springboot.service.ServiceRoles;
import com.master.springboot.service.ServiceUsuarios;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
public class UsuariosController {

    @Autowired
    ServiceUsuarios serviceUsuarios;

    @Autowired
    ServiceRoles serviceRoles;

    // ========== MÉTODOS GET ==========

    @GetMapping("api/usuarios")
    public List<Usuarios> MostrarUsuarios(){
        return serviceUsuarios.findAll();
    }

    @GetMapping("api/usuario/{id}")
    public Usuarios ObtenerPorId(@PathVariable int id){
        return serviceUsuarios.findById(id);
    }

    @GetMapping("/api/usuarios/buscar")
    public ResponseEntity<?> buscarUsuarios(@RequestParam(required = false)String query, @RequestParam(required = false)Integer rolId){
        try{
            List<Usuarios> usuarios = serviceUsuarios.findAll();

            if(query != null && !query.isEmpty()){
                String queryLower = query.toLowerCase();
                usuarios = usuarios.stream()
                        .filter(u ->u.getNombreusuario().toLowerCase().contains(queryLower) ||
                                (u.getEmail() != null && u.getEmail().toLowerCase().contains(queryLower)))
                        .collect(Collectors.toList());
            }

            if(rolId != null){
                usuarios = usuarios.stream().
                        filter(u -> u.getRole() != null && u.getRole().getId() == rolId)
                        .collect(Collectors.toList());
            }

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", usuarios);
            response.put("Total", usuarios.size());
            return ResponseEntity.ok(response);

        }catch(Exception e){
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Error en la busqueda: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    // ========== MÉTODO POST (CREAR) ==========

    @PostMapping("/api/usuarios")
    public ResponseEntity<?> crearUsuario(@RequestBody Usuarios usuario) {
        Map<String, Object> response = new HashMap<>();

        try {
            System.out.println("=== CREAR USUARIO ===");

            // Verificar si el usuario ya existe
            List<Usuarios> usuariosExistentes = serviceUsuarios.findAll();
            for (Usuarios u : usuariosExistentes) {
                if (u.getNombreusuario().equals(usuario.getNombreusuario())) {
                    response.put("success", false);
                    response.put("message", "El nombre de usuario ya existe");
                    return ResponseEntity.badRequest().body(response);
                }
            }

            // Verificar email duplicado
            for (Usuarios u : usuariosExistentes) {
                if (u.getEmail().equals(usuario.getEmail())) {
                    response.put("success", false);
                    response.put("message", "El email ya está registrado");
                    return ResponseEntity.badRequest().body(response);
                }
            }

            // Buscar el rol por ID
            if (usuario.getRole() != null && usuario.getRole().getId() > 0) {
                Roles rol = serviceRoles.findAll().stream()
                        .filter(r -> r.getId() == usuario.getRole().getId())
                        .findFirst()
                        .orElse(null);
                if (rol == null) {
                    response.put("success", false);
                    response.put("message", "Rol no encontrado");
                    return ResponseEntity.badRequest().body(response);
                }
                usuario.setRole(rol);
            }

            // Encriptar contraseña
            if (usuario.getPassword() != null && !usuario.getPassword().isEmpty()) {
                usuario.setPassword(md5(usuario.getPassword()));
            }

            // Guardar el usuario
            Usuarios nuevoUsuario = serviceUsuarios.save(usuario);

            response.put("success", true);
            response.put("message", "Usuario creado correctamente");
            response.put("usuario", nuevoUsuario);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);

        } catch (Exception e) {
            e.printStackTrace();
            response.put("success", false);
            response.put("message", "Error al crear usuario: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    // ========== MÉTODO PUT (ACTUALIZAR) ==========

    @PutMapping("/api/usuarios/{id}")
    public ResponseEntity<?> actualizarUsuario(@PathVariable int id, @RequestBody Usuarios usuarioActualizado) {
        Map<String, Object> response = new HashMap<>();

        try {
            System.out.println("=== ACTUALIZAR USUARIO ID: " + id + " ===");

            // 1. Buscar el usuario existente en la BD por el ID
            Usuarios usuarioExistente = serviceUsuarios.findById(id);

            // 2. Verificar si existe
            if (usuarioExistente == null) {
                response.put("success", false);
                response.put("message", "Usuario no encontrado");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }

            // 3. Actualizar SOLO los campos que vienen del frontend (usuarioActualizado)
            // El usuarioActualizado es el JSON que envía el frontend con los nuevos datos

            // Campos obligatorios
            usuarioExistente.setNombreusuario(usuarioActualizado.getNombreusuario());
            usuarioExistente.setApellidopaterno(usuarioActualizado.getApellidopaterno());
            usuarioExistente.setApellidomaterno(usuarioActualizado.getApellidomaterno());
            usuarioExistente.setEmail(usuarioActualizado.getEmail());
            usuarioExistente.setTelefono(usuarioActualizado.getTelefono());

            // Actualizar rol (viene del frontend como { id: 1 })
            if (usuarioActualizado.getRole() != null) {
                usuarioExistente.setRole(usuarioActualizado.getRole());
            }

            // Actualizar contraseña SOLO si viene en la petición
            if (usuarioActualizado.getPassword() != null && !usuarioActualizado.getPassword().isEmpty()) {
                usuarioExistente.setPassword(md5(usuarioActualizado.getPassword()));
            }

            // 4. Guardar los cambios (UPDATE)
            Usuarios usuarioGuardado = serviceUsuarios.save(usuarioExistente);

            // 5. Preparar respuesta exitosa
            response.put("success", true);
            response.put("message", "Usuario actualizado correctamente");
            response.put("usuario", usuarioGuardado);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            e.printStackTrace();
            response.put("success", false);
            response.put("message", "Error al actualizar: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    // ========== MÉTODO DELETE (ELIMINAR) ==========

    @DeleteMapping("/api/usuarios/{id}")
    public ResponseEntity<?> eliminarUsuario(@PathVariable int id) {
        Map<String, Object> response = new HashMap<>();

        try {
            System.out.println("=== ELIMINAR USUARIO ID: " + id + " ===");

            // Verificar si existe
            Usuarios usuario = serviceUsuarios.findById(id);
            if (usuario == null) {
                response.put("success", false);
                response.put("message", "Usuario no encontrado");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }

            // Eliminar
            serviceUsuarios.delete(id);

            response.put("success", true);
            response.put("message", "Usuario eliminado correctamente");
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            e.printStackTrace();
            response.put("success", false);
            response.put("message", "Error al eliminar usuario: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    // ========== MÉTODO AUXILIAR ==========

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