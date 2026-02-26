package com.master.springboot.Controller;

import com.master.springboot.Models.Foto;
import com.master.springboot.Models.Usuarios;
import com.master.springboot.service.ServiceFotos;
import com.master.springboot.service.ServiceUsuarios;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/fotos")
public class FotosController {

    @Autowired
    private ServiceFotos serviceFotos;

    @Autowired
    private ServiceUsuarios serviceUsuarios;

    @GetMapping
    public ResponseEntity<?> obtenerTodas(HttpSession session) {
        try {
            Usuarios usuario = (Usuarios) session.getAttribute("usuario");

            if (usuario == null) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", true);
                response.put("data", new ArrayList<>());
                return ResponseEntity.ok(response);
            }

            List<Foto> fotos = serviceFotos.findByUsuarioIdusuario(usuario.getIdusuario());

            // Crear una lista SIN los datos binarios
            List<Map<String, Object>> fotosSinDatos = new ArrayList<>();
            for (Foto foto : fotos) {
                Map<String, Object> fotoMap = new HashMap<>();
                fotoMap.put("id", foto.getId());
                fotoMap.put("nombre", foto.getNombre());
                fotoMap.put("descripcion", foto.getDescripcion());
                fotoMap.put("tipo", foto.getTipo());
                fotoMap.put("tamanio", foto.getTamanio());
                fotoMap.put("fechaSubida", foto.getFechaSubida());
                // NO incluir foto.getDatos() aquí
                fotosSinDatos.add(fotoMap);
            }

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", fotosSinDatos);  // ← Usar la lista sin datos
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Error al cargar fotos: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> obtenerPorId(@PathVariable int id) {
        try {
            Foto foto = serviceFotos.findById(id);
            if (foto == null) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("message", "Foto no encontrada");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }
            return ResponseEntity.ok(foto);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Error: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @GetMapping("/imagen/{id}")
    public ResponseEntity<byte[]> obtenerImagen(@PathVariable int id) {
        try {
            Foto foto = serviceFotos.findById(id);
            if (foto == null) {
                return ResponseEntity.notFound().build();
            }

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.parseMediaType(foto.getTipo()));
            headers.setContentDispositionFormData("inline", foto.getNombre());

            return new ResponseEntity<>(foto.getDatos(), headers, HttpStatus.OK);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping("/subir")
    public ResponseEntity<?> subirFoto(
            @RequestParam("archivo") MultipartFile archivo,
            @RequestParam(required = false) String descripcion,
            HttpSession session) {

        Map<String, Object> response = new HashMap<>();

        System.out.println("=== SUBIR FOTO ===");
        System.out.println("Archivo recibido: " + (archivo != null ? archivo.getOriginalFilename() : "null"));
        System.out.println("Tamaño: " + (archivo != null ? archivo.getSize() : 0));
        System.out.println("Descripción: " + descripcion);

        try {
            // 1. Verificar sesión
            Usuarios usuarioSesion = (Usuarios) session.getAttribute("usuario");

            if (usuarioSesion == null) {
                response.put("success", false);
                response.put("message", "Debes iniciar sesión para continuar");
                return ResponseEntity.status(401).body(response);
            }

            // 2. Buscar usuario en BD
            Usuarios usuario = serviceUsuarios.findById(usuarioSesion.getIdusuario());

            if (usuario == null) {
                response.put("success", false);
                response.put("message", "Usuario no encontrado");
                return ResponseEntity.status(401).body(response);
            }

            // 3. Validar archivo
            if (archivo == null || archivo.isEmpty()) {
                System.out.println("Error: Archivo vacío");
                response.put("success", false);
                response.put("message", "El archivo está vacío");
                return ResponseEntity.badRequest().body(response);
            }

            // 4. Validar tipo
            String contentType = archivo.getContentType();
            System.out.println("ContentType: " + contentType);

            if (contentType == null || !contentType.startsWith("image/")) {
                System.out.println("Error: No es imagen");
                response.put("success", false);
                response.put("message", "Solo se permiten imágenes");
                return ResponseEntity.badRequest().body(response);
            }

            // 5. Validar tamaño
            if (archivo.getSize() > 15 * 1024 * 1024) {
                System.out.println("Error: Excede 10MB");
                response.put("success", false);
                response.put("message", "Maximum upload size exceeded");
                return ResponseEntity.badRequest().body(response);
            }

            // 6. Guardar foto
            System.out.println("Guardando foto para usuario: " + usuario.getNombreusuario());
            Foto fotoGuardada = serviceFotos.save(archivo, descripcion, usuario);
            System.out.println("Foto guardada con ID: " + fotoGuardada.getId());

            response.put("success", true);
            response.put("message", "Foto subida correctamente");
            Map<String, Object> fotoResponse = new HashMap<>();
            fotoResponse.put("id", fotoGuardada.getId());
            fotoResponse.put("nombre", fotoGuardada.getNombre());
            fotoResponse.put("descripcion", fotoGuardada.getDescripcion());
            fotoResponse.put("tipo", fotoGuardada.getTipo());
            fotoResponse.put("tamanio", fotoGuardada.getTamanio());
            fotoResponse.put("fechaSubida", fotoGuardada.getFechaSubida());
// NO incluyas fotoGuardada.getDatos() aquí

            response.put("foto", fotoResponse);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            System.err.println("ERROR al subir foto:");
            e.printStackTrace();

            // Asegurar que siempre devolvemos JSON válido
            response = new HashMap<>();  // Crear nueva respuesta por si acaso
            response.put("success", false);
            response.put("message", "Error al subir foto: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .contentType(MediaType.APPLICATION_JSON)  // Forzar JSON
                    .body(response);
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> actualizarDescripcion(@PathVariable int id, @RequestParam String descripcion) {
        Map<String, Object> response = new HashMap<>();

        try {
            Foto fotoActualizada = serviceFotos.update(id, descripcion);
            if (fotoActualizada == null) {
                response.put("success", false);
                response.put("message", "Foto no encontrada");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }

            response.put("success", true);
            response.put("message", "Descripción actualizada");
            response.put("foto", fotoActualizada);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error al actualizar: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> eliminarFoto(@PathVariable int id) {
        Map<String, Object> response = new HashMap<>();

        try {
            Foto foto = serviceFotos.findById(id);
            if (foto == null) {
                response.put("success", false);
                response.put("message", "Foto no encontrada");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }

            serviceFotos.deleteById(id);

            response.put("success", true);
            response.put("message", "Foto eliminada correctamente");
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error al eliminar: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
}