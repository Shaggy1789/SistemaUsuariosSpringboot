package com.master.springboot.Controller;

import com.master.springboot.Models.*;
import com.master.springboot.Repository.TiposPermisoRepository;
import com.master.springboot.service.ServiceModulos;
import com.master.springboot.service.ServicePerfilPermisos;
import com.master.springboot.service.ServicePerfiles;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@Controller
public class PermisosController {

    @Autowired private ServicePerfiles        servicePerfiles;
    @Autowired private ServiceModulos         serviceModulos;
    @Autowired private ServicePerfilPermisos  servicePerfilPermisos;
    @Autowired private TiposPermisoRepository tiposPermisoRepo;

    // ── GET /permisos — Página principal ─────────────────────
    @GetMapping("/permisos")
    public String mostrarPermisos(HttpSession session, Model model) {
        Usuarios usuario = (Usuarios) session.getAttribute("usuario");

        // Doble verificación (el interceptor ya lo hace, pero por seguridad)
        if (usuario == null || !esAdmin(usuario)) {
            return "redirect:/?acceso=denegado";
        }

        model.addAttribute("titulo", "Permisos por Perfil — Santa Mónica");
        return "permisos";
    }

    // ── API: GET /api/permisos/perfiles — Lista de perfiles ──
    @GetMapping("/api/permisos/perfiles")
    @ResponseBody
    public ResponseEntity<?> listarPerfiles(HttpSession session) {
        if (!verificarAdmin(session))
            return forbidden();

        List<Perfiles> perfiles = servicePerfiles.findAll();
        Map<String, Object> resp = new HashMap<>();
        resp.put("success", true);
        resp.put("data", perfiles);
        return ResponseEntity.ok(resp);
    }

    // ── API: GET /api/permisos/tipos — Tipos de permiso ──────
    @GetMapping("/api/permisos/tipos")
    @ResponseBody
    public ResponseEntity<?> listarTipos(HttpSession session) {
        if (!verificarAdmin(session)) return forbidden();

        List<TiposPermiso> tipos = tiposPermisoRepo.findAll();
        Map<String, Object> resp = new HashMap<>();
        resp.put("success", true);
        resp.put("data", tipos);
        return ResponseEntity.ok(resp);
    }

    /**
     * GET /api/permisos/{perfilId}
     * Devuelve todos los módulos con los permisos que tiene ese perfil.
     * Estructura de respuesta:
     * {
     *   "data": [
     *     {
     *       "moduloId": "uuid",
     *       "moduloNombreMostrar": "USUARIOS",
     *       "tiposPermisoIds": ["uuid-agregar", "uuid-editar"]
     *     }, ...
     *   ]
     * }
     */
    @GetMapping("/api/permisos/{perfilId}")
    @ResponseBody
    public ResponseEntity<?> obtenerPermisosDePerfil(@PathVariable UUID perfilId,
                                                     HttpSession session) {
        if (!verificarAdmin(session)) return forbidden();

        try {
            // 1. Todos los módulos ordenados
            List<Modulos> todosModulos = serviceModulos.findAll();

            // 2. Permisos que ya tiene el perfil
            List<PerfilPermisos> permisosPerfil =
                    servicePerfilPermisos.findByPerfilId(perfilId);

            // Agrupar los permisos existentes por moduloId → set de tipoPermisoIds
            Map<UUID, Set<UUID>> permisosPorModulo = new HashMap<>();
            for (PerfilPermisos pp : permisosPerfil) {
                UUID mId = pp.getModulo().getId();
                permisosPorModulo
                        .computeIfAbsent(mId, k -> new HashSet<>())
                        .add(pp.getTipoPermiso().getId());
            }

            // 3. Construir lista de DTOs (uno por módulo)
            List<Map<String, Object>> resultado = new ArrayList<>();
            for (Modulos m : todosModulos) {
                Map<String, Object> item = new LinkedHashMap<>();
                item.put("moduloId",           m.getId().toString());
                item.put("moduloNombre",        m.getNombre());
                item.put("moduloNombreMostrar", m.getNombreMostrar());
                item.put("moduloIcono",         m.getIcono());

                Set<UUID> ids = permisosPorModulo.getOrDefault(m.getId(), Collections.emptySet());
                item.put("tiposPermisoIds",
                        ids.stream().map(UUID::toString).collect(Collectors.toList()));

                resultado.add(item);
            }

            Map<String, Object> resp = new HashMap<>();
            resp.put("success", true);
            resp.put("data", resultado);
            return ResponseEntity.ok(resp);

        } catch (Exception e) {
            e.printStackTrace();
            return error("Error al obtener permisos: " + e.getMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * POST /api/permisos/{perfilId}/{moduloId}
     * Body: { "tiposPermisoIds": ["uuid1", "uuid2", ...] }
     * Reemplaza los permisos del perfil sobre ese módulo.
     */
    @PostMapping("/api/permisos/{perfilId}/{moduloId}")
    @ResponseBody
    public ResponseEntity<?> guardarPermisosDeModulo(@PathVariable UUID perfilId,
                                                     @PathVariable UUID moduloId,
                                                     @RequestBody Map<String, List<String>> body,
                                                     HttpSession session) {
        if (!verificarAdmin(session)) return forbidden();

        try {
            List<String> rawIds = body.getOrDefault("tiposPermisoIds", Collections.emptyList());
            List<UUID> ids = rawIds.stream()
                    .map(UUID::fromString)
                    .collect(Collectors.toList());

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

    /**
     * POST /api/permisos/{perfilId}/guardar-todos
     * Body: [ { "moduloId": "uuid", "tiposPermisoIds": ["uuid",...] }, ... ]
     * Guarda TODOS los módulos de un perfil en una sola llamada (botón "Guardar").
     */
    @PostMapping("/api/permisos/{perfilId}/guardar-todos")
    @ResponseBody
    public ResponseEntity<?> guardarTodosLosPermisos(@PathVariable UUID perfilId,
                                                     @RequestBody List<Map<String, Object>> payload,
                                                     HttpSession session) {
        if (!verificarAdmin(session)) return forbidden();

        try {
            for (Map<String, Object> item : payload) {
                UUID moduloId = UUID.fromString(item.get("moduloId").toString());

                @SuppressWarnings("unchecked")
                List<String> rawIds = (List<String>) item.getOrDefault("tiposPermisoIds",
                        Collections.emptyList());
                List<UUID> ids = rawIds.stream()
                        .map(UUID::fromString)
                        .collect(Collectors.toList());

                servicePerfilPermisos.guardarPermisosDeModulo(perfilId, moduloId, ids);
            }

            Map<String, Object> resp = new HashMap<>();
            resp.put("success", true);
            resp.put("message", "Permisos del perfil actualizados correctamente");
            return ResponseEntity.ok(resp);

        } catch (Exception e) {
            e.printStackTrace();
            return error("Error al guardar: " + e.getMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // ── Helpers ──────────────────────────────────────────────

    private boolean verificarAdmin(HttpSession session) {
        Usuarios u = (Usuarios) session.getAttribute("usuario");
        return u != null && esAdmin(u);
    }

    private boolean esAdmin(Usuarios u) {
        return u.getPerfil() != null &&
                (u.getPerfil().getNombre().equalsIgnoreCase("ADMIN") ||
                        u.getPerfil().getNombre().equalsIgnoreCase("ADMINISTRADOR"));
    }

    private ResponseEntity<Map<String, Object>> forbidden() {
        Map<String, Object> resp = new HashMap<>();
        resp.put("success", false);
        resp.put("message", "Acceso denegado. Solo el Administrador puede gestionar permisos.");
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(resp);
    }

    private ResponseEntity<Map<String, Object>> error(String msg, HttpStatus status) {
        Map<String, Object> resp = new HashMap<>();
        resp.put("success", false);
        resp.put("message", msg);
        return ResponseEntity.status(status).body(resp);
    }
}