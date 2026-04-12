package com.master.springboot.service;

import com.master.springboot.Models.Perfiles;
import com.master.springboot.Repository.PerfilesRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class ServicePerfiles {

    @Autowired
    private PerfilesRepository perfilesRepository;

    public List<Perfiles> findAll() {
        return perfilesRepository.findAll();
    }

    public List<Perfiles> findActivos() {
        return perfilesRepository.findByEstado("ACTIVO");
    }

    public Perfiles findById(UUID id) {
        return perfilesRepository.findById(id).orElse(null);
    }

    public Perfiles save(Perfiles perfil) {
        return perfilesRepository.save(perfil);
    }

    public void delete(UUID id) {
        perfilesRepository.deleteById(id);
    }
}
