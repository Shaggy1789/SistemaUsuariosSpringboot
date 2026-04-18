package com.master.springboot.Controller;

import com.master.springboot.Models.Modulos;
import com.master.springboot.Models.Usuarios;
import com.master.springboot.service.MenuService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/menu")
public class MenuController {

    @Autowired
    private MenuService menuService;

    @Autowired
    private HttpSession httpSession;

    @GetMapping("/usuario")
    public ResponseEntity<?> obtenerMenuUsuario() {
        try {
            Usuarios usuario = (Usuarios) httpSession.getAttribute("usuario");

            if (usuario == null) {
                Map<String, Object> resp = new HashMap<>();
                resp.put("success", false);
                resp.put("message", "No hay sesión activa");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(resp);
            }

            List<Modulos> menu = menuService.construirMenuUsuario();

            Map<String, Object> resp = new HashMap<>();
            resp.put("success", true);
            resp.put("data", menu);
            return ResponseEntity.ok(resp);

        } catch (Exception e) {
            Map<String, Object> resp = new HashMap<>();
            resp.put("success", false);
            resp.put("message", "Error al obtener menú: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(resp);
        }
    }
}