package com.master.springboot.service;

import com.master.springboot.Models.Usuarios;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public interface ServiceUsuarios {

    List<Usuarios> findAll();

    Usuarios findById(UUID id);

    Usuarios findByUsuario(String usuario);  // para el login

    Usuarios save(Usuarios usuario);

    void delete(UUID id);

    boolean existsByUsuario(String usuario);

    boolean existsByUsuarioAndIdNot(String usuario, UUID id);

    List<Usuarios> buscar(String query, UUID perfilId);
}
