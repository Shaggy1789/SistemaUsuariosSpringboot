package com.master.springboot.Repository;

import com.master.springboot.Models.Modulos;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
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

    // Query SIMPLIFICADA - sin CASE WHEN
    @Query("SELECT DISTINCT m FROM Modulos m " +
            "WHERE EXISTS (SELECT 1 FROM PerfilPermisos pp " +
            "            WHERE pp.modulo.id = m.id " +
            "            AND pp.perfil.id = :perfilId " +
            "            AND pp.tipoPermiso.nombre = 'VER') " +
            "ORDER BY m.orden ASC")
    List<Modulos> findModulosConPermisoConsulta(@Param("perfilId") UUID perfilId);
}