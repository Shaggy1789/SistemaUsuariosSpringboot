// com/master/springboot/service/MenuService.java
package com.master.springboot.service;

import com.master.springboot.Models.Modulos;
import com.master.springboot.Models.Usuarios;
import com.master.springboot.Repository.ModulosRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class MenuService {

    @Autowired
    private ModulosRepository modulosRepository;

    @Autowired
    private HttpSession httpSession;

    public List<Modulos> construirMenuUsuario() {
        // Obtener usuario de la sesión
        Usuarios usuario = (Usuarios) httpSession.getAttribute("usuario");

        if (usuario == null || usuario.getPerfil() == null) {
            return new ArrayList<>();
        }

        // Obtener módulos con permiso CONSULTAR
        List<Modulos> modulosConPermiso = modulosRepository
                .findModulosConPermisoConsulta(usuario.getPerfil().getId());  // ← Ya es UUID

        // Construir árbol
        return construirArbol(modulosConPermiso);
    }

    private List<Modulos> construirArbol(List<Modulos> modulos) {
        // Mapa para acceso rápido por ID (UUID, no Integer)
        Map<UUID, Modulos> mapaModulos = modulos.stream()
                .collect(Collectors.toMap(Modulos::getId, m -> m));

        List<Modulos> raices = new ArrayList<>();

        for (Modulos modulo : modulos) {
            if (modulo.getPadre() == null) {
                raices.add(modulo);
            } else {
                Modulos padre = mapaModulos.get(modulo.getPadre());
                if (padre != null) {
                    padre.getHijos().add(modulo);
                }
            }
        }

        // Ordenar por campo 'orden'
        raices.sort(Comparator.comparing(Modulos::getOrden,
                Comparator.nullsLast(Comparator.naturalOrder())));

        for (Modulos raiz : raices) {
            raiz.getHijos().sort(Comparator.comparing(Modulos::getOrden,
                    Comparator.nullsLast(Comparator.naturalOrder())));
        }

        return raices;
    }
}