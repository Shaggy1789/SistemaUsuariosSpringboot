package com.master.springboot.service;

import com.master.springboot.Models.Modulos;
import com.master.springboot.Models.Usuarios;
import com.master.springboot.Repository.ModulosRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@Transactional(readOnly = true)
public class MenuService {

    @Autowired
    private ModulosRepository modulosRepository;

    @Autowired
    private HttpSession httpSession;

    public List<Modulos> construirMenuUsuario() {
        Usuarios usuario = (Usuarios) httpSession.getAttribute("usuario");

        if (usuario == null || usuario.getPerfil() == null) {
            return new ArrayList<>();
        }

        List<Modulos> modulosConPermiso;

        // Si es ADMIN, obtener TODOS los módulos
        if (esAdmin(usuario)) {
            modulosConPermiso = modulosRepository.findAll();  // Todos los módulos
        } else {
            modulosConPermiso = modulosRepository
                    .findModulosConPermisoConsulta(usuario.getPerfil().getId());
        }

        return construirArbol(modulosConPermiso);
    }

    private boolean esAdmin(Usuarios u) {
        return u.getPerfil() != null &&
                (u.getPerfil().getNombre().equalsIgnoreCase("ADMIN") ||
                        u.getPerfil().getNombre().equalsIgnoreCase("ADMINISTRADOR"));
    }

    private List<Modulos> construirArbol(List<Modulos> modulos) {
        Map<UUID, Modulos> mapa = new HashMap<>();
        List<Modulos> raices = new ArrayList<>();

        // Primera pasada: mapear todos los módulos por ID
        for (Modulos m : modulos) {
            mapa.put(m.getId(), m);
            // Inicializar lista de hijos
            m.setHijos(new ArrayList<>());
        }

        // Segunda pasada: construir jerarquía basada en padre_id (NO en el objeto padre)
        for (Modulos m : modulos) {
            // Verificar si tiene padre_id en la base de datos
            UUID padreId = obtenerPadreIdReal(m);

            if (padreId == null) {
                // Es un módulo raíz
                raices.add(m);
                System.out.println("📁 Raíz: " + m.getNombreMostrar());
            } else {
                // Es un hijo, buscar su padre en el mapa
                Modulos padre = mapa.get(padreId);
                if (padre != null) {
                    padre.getHijos().add(m);
                    System.out.println("   └─ Hijo: " + m.getNombreMostrar() + " -> Padre: " + padre.getNombreMostrar());
                } else {
                    // Si el padre no está en la lista de módulos con permiso, tratarlo como raíz
                    raices.add(m);
                    System.out.println("⚠️ Padre no encontrado para: " + m.getNombreMostrar() + ", tratando como raíz");
                }
            }
        }

        // Ordenar raíces y hijos
        raices.sort(Comparator.comparing(Modulos::getOrden, Comparator.nullsLast(Comparator.naturalOrder())));
        for (Modulos raiz : raices) {
            raiz.getHijos().sort(Comparator.comparing(Modulos::getOrden, Comparator.nullsLast(Comparator.naturalOrder())));
        }

        return raices;
    }

    /**
     * Obtiene el padre_id real desde la base de datos, evitando proxies de Hibernate
     */
    private UUID obtenerPadreIdReal(Modulos modulo) {
        // Intentar obtener el ID del objeto padre
        if (modulo.getPadre() != null) {
            try {
                return modulo.getPadre().getId();
            } catch (Exception e) {
                // Si falla por lazy loading, retornar null
                return null;
            }
        }
        return null;
    }
}