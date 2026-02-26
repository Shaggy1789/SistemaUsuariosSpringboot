package com.master.springboot.Controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class HomeController {
    @GetMapping("/login")
    public String mostrarLogin(Model model,
                               @RequestParam(required = false) String error,
                               @RequestParam(required = false) String registro,
                               @RequestParam(required = false) String usuario,
                               HttpServletRequest request) {

        System.out.println("=== INICIO mostrarLogin ===");
        System.out.println("Request URI: " + request.getRequestURI());
        System.out.println("Method: " + request.getMethod());
        System.out.println("Parameters - error: " + error);
        System.out.println("Parameters - registro: " + registro);
        System.out.println("Parameters - usuario: " + usuario);

        try {
            model.addAttribute("titulo", "Inicio de Sesión");
            System.out.println("Título agregado al modelo");

            if (error != null) {
                model.addAttribute("error", "Error en el inicio de sesión");
                System.out.println("Error agregado al modelo");
            }
            if (registro != null && registro.equals("exitoso")) {
                model.addAttribute("mensajeExito", "¡Registro exitoso! Ahora puedes iniciar sesión.");
                System.out.println("Mensaje de éxito agregado al modelo");
            }
            if (usuario != null) {
                model.addAttribute("usuarioRegistrado", usuario);
                System.out.println("Usuario registrado agregado al modelo");
            }

            System.out.println("Intentando retornar vista: login");
            return "login";

        } catch (Exception e) {
            System.err.println("!!! EXCEPCIÓN EN mostrarLogin !!!");
            e.printStackTrace();
            return "error";
        } finally {
            System.out.println("=== FIN mostrarLogin ===");
        }
    }

    @GetMapping("/login1")
    public String mostrarLogin1(Model model){
        model.addAttribute("titulo","iniciando login");
        return "login1";
    }
    //Index
    @GetMapping("/")
    public String mostrarDashboard(Model model) {
        model.addAttribute("titulo", "Dashboard");
        return "Dashboard"; // Renderiza dashboard.html
    }

    @GetMapping("/usuarios")
    public String mostrarUsuarios(Model model) {
        model.addAttribute("titulo", "Usuarios");
        return "usuarios";
    }

    @GetMapping("/error")
    public String mostrarError(
            @RequestParam(required = false) String origen,
            @RequestParam(required = false) String mensaje,
            HttpServletRequest request,
            Model model
    ) {
        if (origen == null || origen.isEmpty()) {
            String refer = request.getHeader("referer");
            if (refer == null) {  // ← CORREGIDO
                origen = "login";
            } else if (refer.isEmpty()) {  // ← CORREGIDO
                if (refer.contains("/login")) {
                    origen = "login";
                } else if (refer.contains("registro")) {
                    origen = "registro";
                } else if (refer.contains("/dashboard")) {
                    origen = "dashboard";
                } else {
                    origen = "login";
                }
            } else {
                origen = "login";
            }
        }
        model.addAttribute("origen", origen);
        model.addAttribute("mensaje", mensaje != null ? mensaje : "Algo inesperado ocurrió. ¡No te preocupes!");
        model.addAttribute("titulo", "¡Oops! Algo salió mal");
        return "error";
    }

    //Registro
    @GetMapping("/registro")
    public String mostrarRegistro(Model model) {
        model.addAttribute("titulo", "Registro de Usuarios");
        return "registro"; // Renderiza registro.html
    }

    //Hola Mundo
    @GetMapping("/holamundo")
    public String holamundo(Model model) {
        model.addAttribute("mensaje", "Hola Mundo desde Springboot");
        return "hola"; // Renderiza hola.html
    }

    //Cerrar sesion
    @GetMapping("/logout")
    public String logout(HttpSession session, RedirectAttributes redirectAttributes) {
        session.invalidate();
        redirectAttributes.addFlashAttribute("mensajeLogout", "Adiooos :D");
        return "redirect:/login";
    }

    @GetMapping("/galeria")
    public String galeria() {
        return "galeria";
    }
}
