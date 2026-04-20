package com.master.springboot.Controller;

import com.master.springboot.Models.Modulos;
import com.master.springboot.service.ServiceModulos;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/modulos")
public class ModulosController {

    @Autowired
    private ServiceModulos serviceModulos;

    // ── GET /api/modulos ──────────────────────────────────────
    @GetMapping
    public ResponseEntity<?> listar() {
        try {
            List<Modulos> modulos = serviceModulos.findAll();
            Map<String, Object> resp = new HashMap<>();
            resp.put("success", true);
            resp.put("data", modulos);
            resp.put("total", modulos.size());
            return ResponseEntity.ok(resp);
        } catch (Exception e) {
            return error("Error al listar módulos: " + e.getMessage(),
                         HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // ── GET /api/modulos/{id} ─────────────────────────────────
    @GetMapping("/{id}")
    public ResponseEntity<?> obtener(@PathVariable UUID id) {
        try {
            Modulos m = serviceModulos.findById(id);
            if (m == null) return error("Módulo no encontrado", HttpStatus.NOT_FOUND);
            return ResponseEntity.ok(m);
        } catch (Exception e) {
            return error("Error al obtener módulo: " + e.getMessage(),
                         HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/padres")
    public ResponseEntity<?> listarPadres() {
        try {
            List<Modulos> modulos = serviceModulos.findAll();
            List<Map<String, Object>> padres = new ArrayList<>();

            for (Modulos m : modulos) {
                Map<String, Object> item = new HashMap<>();
                item.put("id", m.getId());
                item.put("nombreMostrar", m.getNombreMostrar());
                padres.add(item);
            }

            Map<String, Object> resp = new HashMap<>();
            resp.put("success", true);
            resp.put("data", padres);
            return ResponseEntity.ok(resp);
        } catch (Exception e) {
            return error("Error: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // ── POST /api/modulos ─────────────────────────────────────
    // ── POST /api/modulos ─────────────────────────────────────
    @PostMapping
    public ResponseEntity<?> crear(@RequestBody Modulos modulo) {
        try {
            // Validaciones
            if (modulo.getNombre() == null || modulo.getNombre().isBlank())
                return error("El campo 'nombre' es obligatorio", HttpStatus.BAD_REQUEST);
            if (modulo.getNombreMostrar() == null || modulo.getNombreMostrar().isBlank())
                return error("El campo 'nombreMostrar' es obligatorio", HttpStatus.BAD_REQUEST);

            // Nombre único
            if (serviceModulos.existsByNombre(modulo.getNombre().trim()))
                return error("Ya existe un módulo con ese nombre", HttpStatus.CONFLICT);

            modulo.setId(null);
            String nombreOriginal = modulo.getNombre().trim().toUpperCase();
            modulo.setNombre(nombreOriginal);
            if (modulo.getOrden() == null) modulo.setOrden(0);

            // ═══════════════════════════════════════════════════════════════
            // DETECCIÓN AUTOMÁTICA DEL PADRE POR NOMBRE (formato: padre.hijo)
            // ═══════════════════════════════════════════════════════════════

            // Si el nombre contiene un punto, buscar/crear el padre
            if (nombreOriginal.contains(".")) {
                String nombrePadre = nombreOriginal.substring(0, nombreOriginal.indexOf('.'));

                // Buscar si el padre ya existe
                Optional<Modulos> padreOpt = serviceModulos.findByNombre(nombrePadre);

                if (padreOpt.isPresent()) {
                    modulo.setPadre(padreOpt.get());
                    System.out.println("✅ Padre encontrado: " + nombrePadre + " -> " + nombreOriginal);
                } else {
                    // Crear el padre automáticamente
                    Modulos nuevoPadre = new Modulos();
                    nuevoPadre.setNombre(nombrePadre);
                    nuevoPadre.setNombreMostrar(nombrePadre);
                    nuevoPadre.setIcono("fa-folder");
                    nuevoPadre.setRuta("");  // Sin ruta = carpeta
                    nuevoPadre.setOrden(0);

                    Modulos padreGuardado = serviceModulos.save(nuevoPadre);
                    modulo.setPadre(padreGuardado);
                    System.out.println("📁 Padre creado automáticamente: " + nombrePadre + " -> " + nombreOriginal);
                }
            }
            // Si NO se especificó padre manualmente y no tiene punto, queda como raíz (padre = null)

            Modulos guardado = serviceModulos.save(modulo);

            Map<String, Object> resp = new HashMap<>();
            resp.put("success", true);
            resp.put("message", "Módulo creado correctamente");
            resp.put("data", guardado);
            return ResponseEntity.status(HttpStatus.CREATED).body(resp);

        } catch (Exception e) {
            e.printStackTrace();
            return error("Error al crear módulo: " + e.getMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    // ── PUT /api/modulos/{id} ─────────────────────────────────
    @PutMapping("/{id}")
    public ResponseEntity<?> actualizar(@PathVariable UUID id,
                                        @RequestBody Modulos datos) {
        try {
            Modulos existente = serviceModulos.findById(id);
            if (existente == null)
                return error("Módulo no encontrado", HttpStatus.NOT_FOUND);

            // Validaciones
            if (datos.getNombre() == null || datos.getNombre().isBlank())
                return error("El campo 'nombre' es obligatorio", HttpStatus.BAD_REQUEST);
            if (datos.getNombreMostrar() == null || datos.getNombreMostrar().isBlank())
                return error("El campo 'nombreMostrar' es obligatorio", HttpStatus.BAD_REQUEST);

            String nuevoNombre = datos.getNombre().trim().toUpperCase();

            // Nombre único (excluyendo el actual)
            if (serviceModulos.existsByNombreAndIdNot(nuevoNombre, id))
                return error("Ya existe otro módulo con ese nombre", HttpStatus.CONFLICT);

            existente.setNombre(nuevoNombre);
            existente.setNombreMostrar(datos.getNombreMostrar().trim());
            existente.setIcono(datos.getIcono());
            existente.setRuta(datos.getRuta());
            if (datos.getOrden() != null) existente.setOrden(datos.getOrden());

            // ═══════════════════════════════════════════════════════════════
            // DETECCIÓN AUTOMÁTICA DEL PADRE POR NOMBRE (formato: padre.hijo)
            // ═══════════════════════════════════════════════════════════════

            // Si el nombre contiene un punto, buscar/crear el padre
            if (nuevoNombre.contains(".")) {
                String nombrePadre = nuevoNombre.substring(0, nuevoNombre.indexOf('.'));

                // Evitar que un módulo sea padre de sí mismo
                if (nombrePadre.equalsIgnoreCase(existente.getNombre())) {
                    return error("Un módulo no puede ser padre de sí mismo", HttpStatus.BAD_REQUEST);
                }

                // Buscar si el padre ya existe
                Optional<Modulos> padreOpt = serviceModulos.findByNombre(nombrePadre);

                if (padreOpt.isPresent()) {
                    existente.setPadre(padreOpt.get());
                    System.out.println("✅ Padre encontrado: " + nombrePadre + " -> " + nuevoNombre);
                } else {
                    // Crear el padre automáticamente
                    Modulos nuevoPadre = new Modulos();
                    nuevoPadre.setNombre(nombrePadre);
                    nuevoPadre.setNombreMostrar(nombrePadre);
                    nuevoPadre.setIcono("fa-folder");
                    nuevoPadre.setRuta("");  // Sin ruta = carpeta
                    nuevoPadre.setOrden(0);

                    Modulos padreGuardado = serviceModulos.save(nuevoPadre);
                    existente.setPadre(padreGuardado);
                    System.out.println("📁 Padre creado automáticamente: " + nombrePadre + " -> " + nuevoNombre);
                }
            } else {

                 existente.setPadre(null);
            }

            Modulos actualizado = serviceModulos.save(existente);

            Map<String, Object> resp = new HashMap<>();
            resp.put("success", true);
            resp.put("message", "Módulo actualizado correctamente");
            resp.put("data", actualizado);
            return ResponseEntity.ok(resp);

        } catch (Exception e) {
            e.printStackTrace();
            return error("Error al actualizar módulo: " + e.getMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // ── DELETE /api/modulos/{id} ──────────────────────────────
    @DeleteMapping("/{id}")
    public ResponseEntity<?> eliminar(@PathVariable UUID id) {
        try {
            Modulos existente = serviceModulos.findById(id);
            if (existente == null)
                return error("Módulo no encontrado", HttpStatus.NOT_FOUND);

            serviceModulos.delete(id);

            Map<String, Object> resp = new HashMap<>();
            resp.put("success", true);
            resp.put("message", "Módulo eliminado correctamente");
            return ResponseEntity.ok(resp);

        } catch (Exception e) {
            e.printStackTrace();
            return error("Error al eliminar módulo: " + e.getMessage(),
                         HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // ── Helper ───────────────────────────────────────────────
    private ResponseEntity<Map<String, Object>> error(String msg, HttpStatus status) {
        Map<String, Object> resp = new HashMap<>();
        resp.put("success", false);
        resp.put("message", msg);
        return ResponseEntity.status(status).body(resp);
    }
}
