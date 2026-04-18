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

        System.out.println("=== MenuService.construirMenuUsuario ===");
        System.out.println("Usuario: " + (usuario != null ? usuario.getUsuario() : "null"));

        if (usuario == null || usuario.getPerfil() == null) {
            System.out.println("Usuario o perfil null, retornando vacío");
            return new ArrayList<>();
        }

        System.out.println("Perfil ID: " + usuario.getPerfil().getId());

        // Obtener todos los módulos con permiso CONSULTAR
        List<Modulos> modulosConPermiso = modulosRepository
                .findModulosConPermisoConsulta(usuario.getPerfil().getId());

        System.out.println("Módulos encontrados: " + modulosConPermiso.size());

        // Filtrar solo los padres (módulos sin padre)
        List<Modulos> menuArbol = new ArrayList<>();
        for (Modulos modulo : modulosConPermiso) {
            if (modulo.getPadre() == null) {
                menuArbol.add(modulo);
            }
        }

        System.out.println("Módulos padre: " + menuArbol.size());

        // Ordenar por el campo 'orden'
        menuArbol.sort(Comparator.comparing(Modulos::getOrden,
                Comparator.nullsLast(Comparator.naturalOrder())));

        return menuArbol;
    }
}