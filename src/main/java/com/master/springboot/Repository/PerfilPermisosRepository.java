package com.master.springboot.Repository;

import com.master.springboot.Models.PerfilPermisos;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Repository
public interface PerfilPermisosRepository extends JpaRepository<PerfilPermisos, UUID> {

    // Todos los permisos de un perfil
    @Query("SELECT pp FROM PerfilPermisos pp " +
           "JOIN FETCH pp.modulo m " +
           "JOIN FETCH pp.tipoPermiso tp " +
           "WHERE pp.perfil.id = :perfilId")
    List<PerfilPermisos> findByPerfilId(@Param("perfilId") UUID perfilId);

    // Permisos de un perfil sobre un módulo específico
    @Query("SELECT pp FROM PerfilPermisos pp " +
           "JOIN FETCH pp.tipoPermiso tp " +
           "WHERE pp.perfil.id = :perfilId AND pp.modulo.id = :moduloId")
    List<PerfilPermisos> findByPerfilIdAndModuloId(
            @Param("perfilId") UUID perfilId,
            @Param("moduloId") UUID moduloId);

    // Verificar si un permiso específico existe
    boolean existsByPerfilIdAndModuloIdAndTipoPermisoId(
            UUID perfilId, UUID moduloId, UUID tipoPermisoId);

    // Eliminar todos los permisos de un perfil sobre un módulo
    @Modifying
    @Transactional
    @Query("DELETE FROM PerfilPermisos pp " +
           "WHERE pp.perfil.id = :perfilId AND pp.modulo.id = :moduloId")
    void deleteByPerfilIdAndModuloId(
            @Param("perfilId") UUID perfilId,
            @Param("moduloId") UUID moduloId);

    // Eliminar todos los permisos de un perfil
    @Modifying
    @Transactional
    void deleteByPerfilId(UUID perfilId);
}
