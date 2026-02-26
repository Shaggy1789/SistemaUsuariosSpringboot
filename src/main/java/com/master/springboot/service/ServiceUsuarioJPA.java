package com.master.springboot.service;

import com.master.springboot.Models.Usuarios;
import com.master.springboot.Repository.UsuariosRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class ServiceUsuarioJPA implements ServiceUsuarios {

    @Autowired
    private UsuariosRepository repository;

    @Override
    public List<Usuarios> findAll() {
        return repository.findAll();
    }

    @Override
    public Usuarios findById(int id) {
        return repository.findById(id).orElse(null);
    }

    @Override
    public Usuarios save(Usuarios usuario) {
        if (usuario.getIdusuario() == null) {
            return repository.save(usuario);
        } else {
            Usuarios existente = repository.findById(usuario.getIdusuario()).orElse(null);
            if (existente != null) {
                existente.setNombreusuario(usuario.getNombreusuario());
                existente.setApellidopaterno(usuario.getApellidopaterno());
                existente.setApellidomaterno(usuario.getApellidomaterno());
                existente.setEmail(usuario.getEmail());
                existente.setTelefono(usuario.getTelefono());
                existente.setRole(usuario.getRole());
                if (usuario.getPassword() != null && !usuario.getPassword().isEmpty()) {
                    existente.setPassword(usuario.getPassword());
                }
                return repository.save(existente);
            }
            return repository.save(usuario);
        }
    }

    @Override
    public void delete(int id) {
        repository.deleteById(id);
    }
}