// src/main/java/com/example/ProyectoSpringboot/service/MenuService.java
package com.master.springboot.service;

import com.master.springboot.Models.Modulos;
import com.master.springboot.Models.Usuarios;
import com.master.springboot.Repository.ModulosRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class MenuService {

    @Autowired
    private ModulosRepository moduloRepository;

    @Autowired
    private HttpSession httpSession;

    public List<Modulos> construirMenuUsuario() {
        // Obtener usuario de la sesión como lo haces en tus controladores
        Usuarios usuario = (Usuarios) httpSession.getAttribute("usuario");

        if (usuario == null || usuario.getPerfil() == null) {
            return new ArrayList<>();
        }

        // Obtener TODOS los módulos con permiso CONSULTAR para este perfil
        List<Modulos> todosModulos = moduloRepository
                .findModulosConPermisoConsulta(usuario.getPerfil().getId());

        // Construir árbol: separar padres de hijos
        List<Modulos> menuArbol = new ArrayList<>();
        Map<Integer, Modulos> mapaModulos = new HashMap<>();

        // Primero, mapear todos los módulos por ID
        for (Modulos modulo : todosModulos) {
            mapaModulos.put(modulo.getId(), modulo);
        }

        // Construir jerarquía
        for (Modulos modulo : todosModulos) {
            if (modulo.getPadre() == null) {
                // Es módulo padre
                menuArbol.add(modulo);
            } else {
                // Es hijo, agregarlo al padre correspondiente
                Modulos padre = mapaModulos.get(modulo.getPadre());
                if (padre != null) {
                    padre.getHijos().add(modulo);
                }
            }
        }

        // Ordenar menú por ID (o puedes agregar campo 'orden' después)
        menuArbol.sort(Comparator.comparing(Modulos::getId));

        return menuArbol;
    }
}