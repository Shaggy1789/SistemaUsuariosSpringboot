package com.master.springboot.Controller;

import com.master.springboot.Models.PerfilPermisos;
import com.master.springboot.Models.TipoPermiso;
import com.master.springboot.Repository.TiposPermisoRepository;
import com.master.springboot.service.ServicePerfilPermisos;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/perfil-permisos")
public class PerfilPermisosController {

    @Autowired
    private ServicePerfilPermisos servicePerfilPermisos;

    @Autowired
    private TiposPermisoRepository tiposPermisoRepository;

    // ── GET /api/perfil-permisos/{perfilId}
    // Devuelve todos los permisos de un perfil agrupados por módulo
    @GetMapping("/{perfilId}")
    public ResponseEntity<?> obtenerPorPerfil(@PathVariable UUID perfilId) {
        try {
            List<PerfilPermisos> lista = servicePerfilPermisos.findByPerfilId(perfilId);

            // Agrupar por módulo para facilitar el uso en frontend
            Map<String, Object> porModulo = new LinkedHashMap<>();
            for (PerfilPermisos pp : lista) {
                String moduloId = pp.getModulo().getId().toString();
                if (!porModulo.containsKey(moduloId)) {
                    Map<String, Object> info = new LinkedHashMap<>();
                    info.put("moduloId", moduloId);
                    info.put("moduloNombre", pp.getModulo().getNombreMostrar());
                    info.put("permisos", new ArrayList<>());
                    porModulo.put(moduloId, info);
                }
                @SuppressWarnings("unchecked")
                List<Map<String, String>> permisos =
                        (List<Map<String, String>>) ((Map<?, ?>) porModulo.get(moduloId)).get("permisos");

                Map<String, String> p = new HashMap<>();
                p.put("id", pp.getTipoPermiso().getId().toString());
                p.put("nombre", pp.getTipoPermiso().getNombre());
                p.put("nombreMostrar", pp.getTipoPermiso().getNombreMostrar());
                permisos.add(p);
            }

            Map<String, Object> resp = new HashMap<>();
            resp.put("success", true);
            resp.put("data", new ArrayList<>(porModulo.values()));
            return ResponseEntity.ok(resp);

        } catch (Exception e) {
            return error("Error al obtener permisos: " + e.getMessage(),
                         HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // ── GET /api/perfil-permisos/{perfilId}/{moduloId}
    // Permisos de un perfil sobre un módulo específico
    @GetMapping("/{perfilId}/{moduloId}")
    public ResponseEntity<?> obtenerPorPerfilYModulo(@PathVariable UUID perfilId,
                                                      @PathVariable UUID moduloId) {
        try {
            List<PerfilPermisos> lista =
                    servicePerfilPermisos.findByPerfilIdAndModuloId(perfilId, moduloId);

            List<Map<String, String>> permisos = new ArrayList<>();
            for (PerfilPermisos pp : lista) {
                Map<String, String> p = new HashMap<>();
                p.put("id", pp.getTipoPermiso().getId().toString());
                p.put("nombre", pp.getTipoPermiso().getNombre());
                p.put("nombreMostrar", pp.getTipoPermiso().getNombreMostrar());
                permisos.add(p);
            }

            Map<String, Object> resp = new HashMap<>();
            resp.put("success", true);
            resp.put("data", permisos);
            return ResponseEntity.ok(resp);

        } catch (Exception e) {
            return error("Error al obtener permisos: " + e.getMessage(),
                         HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // ── GET /api/perfil-permisos/tipos
    // Lista todos los tipos de permiso disponibles
    @GetMapping("/tipos")
    public ResponseEntity<?> listarTipos() {
        try {
            List<TipoPermiso> tipos = tiposPermisoRepository.findAll();
            Map<String, Object> resp = new HashMap<>();
            resp.put("success", true);
            resp.put("data", tipos);
            return ResponseEntity.ok(resp);
        } catch (Exception e) {
            return error("Error al listar tipos de permiso: " + e.getMessage(),
                         HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // ── POST /api/perfil-permisos/{perfilId}/{moduloId}
    // Body: { "tiposPermisoIds": ["uuid1", "uuid2", ...] }
    // Reemplaza todos los permisos del perfil sobre ese módulo
    @PostMapping("/{perfilId}/{moduloId}")
    public ResponseEntity<?> guardar(@PathVariable UUID perfilId,
                                     @PathVariable UUID moduloId,
                                     @RequestBody Map<String, List<String>> body) {
        try {
            List<String> rawIds = body.getOrDefault("tiposPermisoIds", Collections.emptyList());
            List<UUID> ids = new ArrayList<>();
            for (String s : rawIds) ids.add(UUID.fromString(s));

            servicePerfilPermisos.guardarPermisosDeModulo(perfilId, moduloId, ids);

            Map<String, Object> resp = new HashMap<>();
            resp.put("success", true);
            resp.put("message", "Permisos guardados correctamente");
            return ResponseEntity.ok(resp);

        } catch (Exception e) {
            e.printStackTrace();
            return error("Error al guardar permisos: " + e.getMessage(),
                         HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // ── DELETE /api/perfil-permisos/{perfilId}
    // Elimina TODOS los permisos de un perfil
    @DeleteMapping("/{perfilId}")
    public ResponseEntity<?> eliminarPorPerfil(@PathVariable UUID perfilId) {
        try {
            servicePerfilPermisos.eliminarPermisosDePerfil(perfilId);
            Map<String, Object> resp = new HashMap<>();
            resp.put("success", true);
            resp.put("message", "Permisos del perfil eliminados");
            return ResponseEntity.ok(resp);
        } catch (Exception e) {
            return error("Error al eliminar permisos: " + e.getMessage(),
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
