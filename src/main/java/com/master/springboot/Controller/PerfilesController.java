package com.master.springboot.Controller;

import com.master.springboot.Models.Perfiles;
import com.master.springboot.service.ServicePerfiles;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/perfiles")
public class PerfilesController {

    @Autowired
    private ServicePerfiles servicePerfiles;

    @GetMapping
    public ResponseEntity<?> listar() {
        try {
            List<Perfiles> lista = servicePerfiles.findAll();
            Map<String, Object> resp = new HashMap<>();
            resp.put("success", true);
            resp.put("data", lista);
            resp.put("total", lista.size());
            return ResponseEntity.ok(resp);
        } catch (Exception e) {
            return error("Error al listar perfiles: " + e.getMessage(),
                         HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/activos")
    public ResponseEntity<?> listarActivos() {
        try {
            List<Perfiles> lista = servicePerfiles.findActivos();
            Map<String, Object> resp = new HashMap<>();
            resp.put("success", true);
            resp.put("data", lista);
            return ResponseEntity.ok(resp);
        } catch (Exception e) {
            return error("Error: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> obtener(@PathVariable UUID id) {
        Perfiles p = servicePerfiles.findById(id);
        if (p == null) return error("Perfil no encontrado", HttpStatus.NOT_FOUND);
        return ResponseEntity.ok(p);
    }

    @PostMapping
    public ResponseEntity<?> crear(@RequestBody Perfiles perfil) {
        try {
            if (perfil.getNombre() == null || perfil.getNombre().isBlank())
                return error("El nombre es obligatorio", HttpStatus.BAD_REQUEST);

            perfil.setId(null);
            perfil.setNombre(perfil.getNombre().trim().toUpperCase());
            if (perfil.getEstado() == null) perfil.setEstado("ACTIVO");

            Perfiles guardado = servicePerfiles.save(perfil);
            Map<String, Object> resp = new HashMap<>();
            resp.put("success", true);
            resp.put("message", "Perfil creado correctamente");
            resp.put("data", guardado);
            return ResponseEntity.status(HttpStatus.CREATED).body(resp);
        } catch (Exception e) {
            e.printStackTrace();
            return error("Error al crear perfil: " + e.getMessage(),
                         HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> actualizar(@PathVariable UUID id,
                                        @RequestBody Perfiles datos) {
        try {
            Perfiles existente = servicePerfiles.findById(id);
            if (existente == null)
                return error("Perfil no encontrado", HttpStatus.NOT_FOUND);

            existente.setNombre(datos.getNombre().trim().toUpperCase());
            existente.setDescripcion(datos.getDescripcion());
            if (datos.getEstado() != null) existente.setEstado(datos.getEstado());

            Perfiles actualizado = servicePerfiles.save(existente);
            Map<String, Object> resp = new HashMap<>();
            resp.put("success", true);
            resp.put("message", "Perfil actualizado correctamente");
            resp.put("data", actualizado);
            return ResponseEntity.ok(resp);
        } catch (Exception e) {
            return error("Error al actualizar perfil: " + e.getMessage(),
                         HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> eliminar(@PathVariable UUID id) {
        try {
            if (servicePerfiles.findById(id) == null)
                return error("Perfil no encontrado", HttpStatus.NOT_FOUND);
            servicePerfiles.delete(id);
            Map<String, Object> resp = new HashMap<>();
            resp.put("success", true);
            resp.put("message", "Perfil eliminado correctamente");
            return ResponseEntity.ok(resp);
        } catch (Exception e) {
            return error("Error al eliminar perfil: " + e.getMessage(),
                         HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    private ResponseEntity<Map<String, Object>> error(String msg, HttpStatus status) {
        Map<String, Object> resp = new HashMap<>();
        resp.put("success", false);
        resp.put("message", msg);
        return ResponseEntity.status(status).body(resp);
    }
}
