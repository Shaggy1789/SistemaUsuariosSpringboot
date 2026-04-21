// com/master/springboot/Controller/RegistroPruebaController.java
package com.master.springboot.Controller;

import com.master.springboot.Models.RegistroPrueba;
import com.master.springboot.service.ServiceRegistroPrueba;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/registros-prueba")
public class RegistroPruebaController {

    @Autowired
    private ServiceRegistroPrueba service;

    // ── GET /api/registros-prueba ─────────────────────────────────
    @GetMapping
    public ResponseEntity<?> listar() {
        try {
            List<RegistroPrueba> registros = service.findAll();
            Map<String, Object> resp = new HashMap<>();
            resp.put("success", true);
            resp.put("data", registros);
            resp.put("total", registros.size());
            return ResponseEntity.ok(resp);
        } catch (Exception e) {
            return error("Error al listar registros: " + e.getMessage(),
                         HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // ── GET /api/registros-prueba/buscar ─────────────────────────
    @GetMapping("/buscar")
    public ResponseEntity<?> buscar(@RequestParam(required = false) String query,
                                    @RequestParam(required = false) String estado) {
        try {
            List<RegistroPrueba> registros;
            
            if (query != null && !query.trim().isEmpty()) {
                registros = service.buscar(query);
            } else {
                registros = service.findAll();
            }
            
            if (estado != null && !estado.trim().isEmpty()) {
                registros = registros.stream()
                        .filter(r -> estado.equalsIgnoreCase(r.getEstado()))
                        .toList();
            }
            
            Map<String, Object> resp = new HashMap<>();
            resp.put("success", true);
            resp.put("data", registros);
            resp.put("total", registros.size());
            return ResponseEntity.ok(resp);
        } catch (Exception e) {
            return error("Error en la búsqueda: " + e.getMessage(),
                         HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // ── GET /api/registros-prueba/{id} ───────────────────────────
    @GetMapping("/{id}")
    public ResponseEntity<?> obtener(@PathVariable UUID id) {
        try {
            Optional<RegistroPrueba> registro = service.findById(id);
            if (registro.isEmpty()) {
                return error("Registro no encontrado", HttpStatus.NOT_FOUND);
            }
            return ResponseEntity.ok(registro.get());
        } catch (Exception e) {
            return error("Error al obtener registro: " + e.getMessage(),
                         HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // ── POST /api/registros-prueba ───────────────────────────────
    @PostMapping
    public ResponseEntity<?> crear(@RequestBody RegistroPrueba registro) {
        try {
            // Validaciones
            if (registro.getNombre() == null || registro.getNombre().isBlank()) {
                return error("El campo 'nombre' es obligatorio", HttpStatus.BAD_REQUEST);
            }

            // Nombre único
            if (service.existsByNombre(registro.getNombre().trim())) {
                return error("Ya existe un registro con ese nombre", HttpStatus.CONFLICT);
            }

            registro.setId(null);
            registro.setNombre(registro.getNombre().trim());
            if (registro.getEstado() == null || registro.getEstado().isBlank()) {
                registro.setEstado("ACTIVO");
            }

            RegistroPrueba guardado = service.save(registro);

            Map<String, Object> resp = new HashMap<>();
            resp.put("success", true);
            resp.put("message", "Registro creado correctamente");
            resp.put("data", guardado);
            return ResponseEntity.status(HttpStatus.CREATED).body(resp);

        } catch (Exception e) {
            e.printStackTrace();
            return error("Error al crear registro: " + e.getMessage(),
                         HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // ── PUT /api/registros-prueba/{id} ───────────────────────────
    @PutMapping("/{id}")
    public ResponseEntity<?> actualizar(@PathVariable UUID id,
                                        @RequestBody RegistroPrueba datos) {
        try {
            Optional<RegistroPrueba> existenteOpt = service.findById(id);
            if (existenteOpt.isEmpty()) {
                return error("Registro no encontrado", HttpStatus.NOT_FOUND);
            }

            RegistroPrueba existente = existenteOpt.get();

            // Validaciones
            if (datos.getNombre() == null || datos.getNombre().isBlank()) {
                return error("El campo 'nombre' es obligatorio", HttpStatus.BAD_REQUEST);
            }

            // Nombre único (excluyendo el actual)
            if (service.existsByNombreAndIdNot(datos.getNombre().trim(), id)) {
                return error("Ya existe otro registro con ese nombre", HttpStatus.CONFLICT);
            }

            existente.setNombre(datos.getNombre().trim());
            if (datos.getEstado() != null && !datos.getEstado().isBlank()) {
                existente.setEstado(datos.getEstado());
            }

            RegistroPrueba actualizado = service.save(existente);

            Map<String, Object> resp = new HashMap<>();
            resp.put("success", true);
            resp.put("message", "Registro actualizado correctamente");
            resp.put("data", actualizado);
            return ResponseEntity.ok(resp);

        } catch (Exception e) {
            e.printStackTrace();
            return error("Error al actualizar registro: " + e.getMessage(),
                         HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // ── DELETE /api/registros-prueba/{id} ────────────────────────
    @DeleteMapping("/{id}")
    public ResponseEntity<?> eliminar(@PathVariable UUID id) {
        try {
            Optional<RegistroPrueba> existente = service.findById(id);
            if (existente.isEmpty()) {
                return error("Registro no encontrado", HttpStatus.NOT_FOUND);
            }

            service.delete(id);

            Map<String, Object> resp = new HashMap<>();
            resp.put("success", true);
            resp.put("message", "Registro eliminado correctamente");
            return ResponseEntity.ok(resp);

        } catch (Exception e) {
            e.printStackTrace();
            return error("Error al eliminar registro: " + e.getMessage(),
                         HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // ── Helper ───────────────────────────────────────────────────
    private ResponseEntity<Map<String, Object>> error(String msg, HttpStatus status) {
        Map<String, Object> resp = new HashMap<>();
        resp.put("success", false);
        resp.put("message", msg);
        return ResponseEntity.status(status).body(resp);
    }
}