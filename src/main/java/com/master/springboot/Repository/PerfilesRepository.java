package com.master.springboot.Repository;

import com.master.springboot.Models.Perfiles;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface PerfilesRepository extends JpaRepository<Perfiles, UUID> {
    List<Perfiles> findByEstado(String estado);
}
