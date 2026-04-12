package com.master.springboot.service;

import com.master.springboot.Models.Modulos;
import com.master.springboot.Repository.ModulosRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class ServiceModulos {

    @Autowired
    private ModulosRepository modulosRepository;

    public List<Modulos> findAll() {
        return modulosRepository.findAllByOrderByOrdenAsc();
    }

    public Modulos findById(UUID id) {
        return modulosRepository.findById(id).orElse(null);
    }

    public Modulos save(Modulos modulo) {
        return modulosRepository.save(modulo);
    }

    public void delete(UUID id) {
        modulosRepository.deleteById(id);
    }

    public boolean existsByNombre(String nombre) {
        return modulosRepository.existsByNombre(nombre);
    }

    public boolean existsByNombreAndIdNot(String nombre, UUID id) {
        return modulosRepository.existsByNombreAndIdNot(nombre, id);
    }

    public Optional<Modulos> findByNombre(String nombre) {
        return modulosRepository.findByNombre(nombre);
    }
}
