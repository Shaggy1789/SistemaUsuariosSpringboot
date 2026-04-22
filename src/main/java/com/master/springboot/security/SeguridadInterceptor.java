package com.master.springboot.security;

import com.master.springboot.Models.Usuarios;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * Interceptor de seguridad basado en sesión + perfil.
 *
 * Reglas:
 *  - Cualquier ruta protegida requiere sesión activa → redirige a /login
 *  - Las rutas de administración (/permisos, /modulos, /perfiles)
 *    requieren perfil ADMINISTRADOR → redirige a / con error 403
 */
@Component
public class SeguridadInterceptor implements HandlerInterceptor {

    // Rutas que requieren SOLO estar autenticado
    private static final String[] RUTAS_PROTEGIDAS = {
            "/", "/usuarios", "/galeria","/mis-permisos"
    };

    // Rutas que requieren ser ADMINISTRADOR
    private static final String[] RUTAS_ADMIN = {
            "/permisos", "/modulos", "/perfiles"
    };

    // 🔥 NUEVO: Rutas de SOLO CONSULTA (cualquier usuario autenticado)
    private static final String[] RUTAS_CONSULTA = {
            "/ver-permisos", "/ver-modulos", "/ver-perfiles"
    };

    @Override
    public boolean preHandle(HttpServletRequest request,
                             HttpServletResponse response,
                             Object handler) throws Exception {

        String uri = request.getRequestURI();
        HttpSession session = request.getSession(false);
        Usuarios usuario = session != null
                ? (Usuarios) session.getAttribute("usuario")
                : null;

        // 1. Verificar rutas de administración
        if (coincide(uri, RUTAS_ADMIN)) {
            if (usuario == null) {
                response.sendRedirect("/login");
                return false;
            }
//            if (!esAdministrador(usuario)) {
//                // Tiene sesión pero no es admin → redirigir al dashboard con aviso
//                response.sendRedirect("/?acceso=denegado");
//                return false;
//            }
        }

        // 🔥 NUEVO: 1.5 Verificar rutas de consulta (solo lectura)
        if (coincide(uri, RUTAS_CONSULTA)) {
            if (usuario == null) {
                response.sendRedirect("/login");
                return false;
            }
            // Permitir acceso a cualquier usuario autenticado (no requiere ser admin)
        }

        // 2. Verificar rutas protegidas generales
        if (coincide(uri, RUTAS_PROTEGIDAS) && usuario == null) {
            response.sendRedirect("/login");
            return false;
        }

        return true;
    }

    // ── Helpers ──────────────────────────────────────────────

    private boolean esAdministrador(Usuarios usuario) {
        if (usuario.getPerfil() == null) return false;
        String nombre = usuario.getPerfil().getNombre();
        return nombre != null && nombre.equalsIgnoreCase("ADMINISTRADOR");
    }

    private boolean coincide(String uri, String[] rutas) {
        for (String ruta : rutas) {
            if (uri.equals(ruta) || uri.startsWith(ruta + "/")) return true;
        }
        return false;
    }
}