package com.master.springboot.Repository;

import com.master.springboot.Models.Modulos;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ModulosRepository extends JpaRepository<Modulos, UUID> {
    Optional<Modulos> findByNombre(String nombre);
    List<Modulos> findAllByOrderByOrdenAsc();
    boolean existsByNombre(String nombre);
    boolean existsByNombreAndIdNot(String nombre, UUID id);
}
