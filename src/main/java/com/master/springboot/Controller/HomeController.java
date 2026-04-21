package com.master.springboot.Controller;

import com.master.springboot.Models.Usuarios;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class HomeController {

    // ── LOGIN ─────────────────────────────────────────────────
    @GetMapping("/login")
    public String mostrarLogin(HttpSession session, Model model) {
        // Si ya tiene sesión, mandar directo al dashboard
        if (session.getAttribute("usuario") != null) {
            return "redirect:/";
        }
        model.addAttribute("titulo", "Iniciar Sesión — Santa Mónica");
        return "login1";
    }

    // Mantener /login1 como alias por compatibilidad
    @GetMapping("/login1")
    public String mostrarLogin1(HttpSession session, Model model) {
        return mostrarLogin(session, model);
    }

    // ── DASHBOARD (INDEX) ─────────────────────────────────────
    @GetMapping("/")
    public String mostrarDashboard(HttpSession session, Model model) {
        // Redirigir al login si no hay sesión
        if (session.getAttribute("usuario") == null) {
            return "redirect:/login";
        }
        Usuarios usuario = (Usuarios) session.getAttribute("usuario");
        model.addAttribute("titulo", "Dashboard — Santa Mónica");
        model.addAttribute("usuario", usuario);
        return "dashboard";
    }

    // ── USUARIOS ──────────────────────────────────────────────
    @GetMapping("/usuarios")
    public String mostrarUsuarios(HttpSession session, Model model) {
        if (session.getAttribute("usuario") == null) {
            return "redirect:/login";
        }
        model.addAttribute("titulo", "Usuarios — Santa Mónica");
        return "usuarios";
    }

    @GetMapping("/prueba1.1")
    public String mostrarPrueba(HttpSession session, Model model) {
        if (session.getAttribute("usuario") == null) {
            return "redirect:/login";
        }
        model.addAttribute("titulo", "Usuarios — Santa Mónica");
        return "prueba1.1";
    }
    @GetMapping("/prueba1.2")
    public String mostrarPrueba2(HttpSession session, Model model) {
        if (session.getAttribute("usuario") == null) {
            return "redirect:/login";
        }
        model.addAttribute("titulo", "Usuarios — Santa Mónica");
        return "prueba1.2";
    }

    @GetMapping("/prueba2.1")
    public String mostrarPrueba3(HttpSession session, Model model) {
        if (session.getAttribute("usuario") == null) {
            return "redirect:/login";
        }
        model.addAttribute("titulo", "Usuarios — Santa Mónica");
        return "prueba2.1";
    }

    @GetMapping("/prueba2.2")
    public String mostrarPrueba4(HttpSession session, Model model) {
        if (session.getAttribute("usuario") == null) {
            return "redirect:/login";
        }
        model.addAttribute("titulo", "Usuarios — Santa Mónica");
        return "prueba2.2";
    }

    // ── MÓDULOS ───────────────────────────────────────────────
    @GetMapping("/modulos")
    public String mostrarModulos(HttpSession session, Model model) {
        if (session.getAttribute("usuario") == null) {
            return "redirect:/login";
        }
        model.addAttribute("titulo", "Módulos — Santa Mónica");
        return "modulos";
    }

    // ── PERFILES ──────────────────────────────────────────────
    @GetMapping("/perfil")
    public String mostrarPerfiles(HttpSession session, Model model) {
        if (session.getAttribute("usuario") == null) return "redirect:/login";
        model.addAttribute("titulo", "Perfiles — Santa Mónica");
        return "perfiles";
    }

    // ── CONFIGURACIÓN (módulos) ───────────────────────────────
    @GetMapping("/config")
    public String mostrarConfiguracion(HttpSession session, Model model) {
        if (session.getAttribute("usuario") == null) return "redirect:/login";
        model.addAttribute("titulo", "Configuración — Santa Mónica");
        return "configuracion";
    }


    // ── GALERÍA ───────────────────────────────────────────────
    @GetMapping("/galeria")
    public String galeria(HttpSession session) {
        if (session.getAttribute("usuario") == null) {
            return "redirect:/login";
        }
        return "galeria";
    }

    // ── ERROR ─────────────────────────────────────────────────
    @GetMapping("/error")
    public String mostrarError(
            @RequestParam(required = false) String origen,
            @RequestParam(required = false) String mensaje,
            HttpServletRequest request,
            Model model) {

        // Detectar origen desde el referer si no viene en el param
        if (origen == null || origen.isEmpty()) {
            String refer = request.getHeader("referer");
            if (refer != null && !refer.isEmpty()) {
                if      (refer.contains("/login"))    origen = "login";
                else if (refer.contains("/modulos"))  origen = "modulos";
                else if (refer.contains("/usuarios")) origen = "usuarios";
                else if (refer.contains("/perfiles")) origen = "perfiles";
                else if (refer.contains("/permisos")) origen = "permisos";
                else                                  origen = "login";
            } else {
                origen = "login";
            }
        }

        model.addAttribute("origen", origen);
        model.addAttribute("mensaje", mensaje != null ? mensaje : "Algo inesperado ocurrió.");
        model.addAttribute("titulo", "Error — Santa Mónica");
        return "error";
    }

    // ── LOGOUT ────────────────────────────────────────────────
    @GetMapping("/logout")
    public String logout(HttpSession session, RedirectAttributes redirectAttributes) {
        session.invalidate();
        redirectAttributes.addFlashAttribute("mensajeLogout", "Sesión cerrada correctamente.");
        return "redirect:/login";
    }
}
