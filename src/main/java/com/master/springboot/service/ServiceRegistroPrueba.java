// com/master/springboot/service/ServiceRegistroPrueba.java
package com.master.springboot.service;

import com.master.springboot.Models.RegistroPrueba;
import com.master.springboot.Repository.RegistroPruebaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class ServiceRegistroPrueba {

    @Autowired
    private RegistroPruebaRepository repository;

    public List<RegistroPrueba> findAll() {
        return repository.findAllByOrderByCreadoEnDesc();
    }

    public Optional<RegistroPrueba> findById(UUID id) {
        return repository.findById(id);
    }

    public List<RegistroPrueba> buscar(String query) {
        if (query == null || query.trim().isEmpty()) {
            return findAll();
        }
        return repository.findByNombreContainingIgnoreCase(query.trim());
    }

    public List<RegistroPrueba> findByEstado(String estado) {
        return repository.findByEstado(estado);
    }

    public RegistroPrueba save(RegistroPrueba registro) {
        if (registro.getId() == null) {
            registro.setCreadoEn(OffsetDateTime.now());
        }
        registro.setActualizadoEn(OffsetDateTime.now());
        return repository.save(registro);
    }

    public void delete(UUID id) {
        repository.deleteById(id);
    }

    public boolean existsByNombre(String nombre) {
        return repository.existsByNombre(nombre);
    }

    public boolean existsByNombreAndIdNot(String nombre, UUID id) {
        return repository.existsByNombreAndIdNot(nombre, id);
    }
}