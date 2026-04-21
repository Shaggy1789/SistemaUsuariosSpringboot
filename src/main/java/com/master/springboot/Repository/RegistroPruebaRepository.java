// com/master/springboot/Repository/RegistroPruebaRepository.java
package com.master.springboot.Repository;

import com.master.springboot.Models.RegistroPrueba;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface RegistroPruebaRepository extends JpaRepository<RegistroPrueba, UUID> {
    
    List<RegistroPrueba> findAllByOrderByCreadoEnDesc();
    
    List<RegistroPrueba> findByEstado(String estado);
    
    List<RegistroPrueba> findByNombreContainingIgnoreCase(String nombre);
    
    boolean existsByNombre(String nombre);
    
    boolean existsByNombreAndIdNot(String nombre, UUID id);
}