package com.master.springboot.service;

import com.master.springboot.Models.Usuarios;
import com.master.springboot.Repository.UsuariosRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class ServiceUsuarioJPA implements ServiceUsuarios {

    @Autowired
    private UsuariosRepository repository;

    @Override
    public List<Usuarios> findAll() {
        return repository.findAll();
    }

    @Override
    public Usuarios findById(UUID id) {
        return repository.findById(id).orElse(null);
    }

    @Override
    public Usuarios findByUsuario(String usuario) {
        return repository.findByUsuario(usuario).orElse(null);
    }

    @Override
    public boolean existsByUsuario(String usuario) {
        return repository.existsByUsuario(usuario);
    }

    @Override
    public boolean existsByUsuarioAndIdNot(String usuario, UUID id) {
        return repository.existsByUsuarioAndIdNot(usuario, id);
    }

    @Override
    public List<Usuarios> buscar(String query, UUID perfilId) {
        boolean hayQuery   = query    != null && !query.isBlank();
        boolean hayPerfil  = perfilId != null;

        if (hayQuery && hayPerfil) {
            return repository.buscarPorTextoyPerfil(query, perfilId);
        } else if (hayQuery) {
            return repository.buscarPorTexto(query);
        } else if (hayPerfil) {
            return repository.findByPerfilId(perfilId);
        } else {
            return repository.findAll();
        }
    }

    @Override
    public Usuarios save(Usuarios usuario) {
        if (usuario.getId() == null) {
            // Nuevo usuario: estado por defecto
            if (usuario.getEstado() == null) {
                usuario.setEstado("ACTIVO");
            }
            return repository.save(usuario);
        }

        // Actualizar usuario existente
        Usuarios existente = repository.findById(usuario.getId()).orElse(null);
        if (existente != null) {
            existente.setUsuario(usuario.getUsuario());
            existente.setEmail(usuario.getEmail());

            if (usuario.getEstado() != null) {
                existente.setEstado(usuario.getEstado());
            }
            if (usuario.getPerfil() != null) {
                existente.setPerfil(usuario.getPerfil());
            }
            // Solo actualizar contraseña si se proporcionó una nueva
            if (usuario.getPassword() != null && !usuario.getPassword().isEmpty()) {
                existente.setPassword(usuario.getPassword());
            }
            return repository.save(existente);
        }

        return repository.save(usuario);
    }

    @Override
    public void delete(UUID id) {
        repository.deleteById(id);
    }
}
