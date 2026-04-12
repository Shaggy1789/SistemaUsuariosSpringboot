package com.master.springboot.Repository;

import com.master.springboot.Models.Usuarios;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UsuariosRepository extends JpaRepository<Usuarios, UUID> {

    // Buscar por campo "usuario" (nombre de login)
    Optional<Usuarios> findByUsuario(String usuario);

    // Verificar duplicados
    boolean existsByUsuario(String usuario);
    boolean existsByUsuarioAndIdNot(String usuario, UUID id);

    // Buscar por estado
    List<Usuarios> findByEstado(String estado);

    // Buscar por perfil
    List<Usuarios> findByPerfilId(UUID perfilId);

    // Búsqueda por texto (usuario o email)
    @Query("SELECT u FROM Usuarios u WHERE " +
           "LOWER(u.usuario) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(u.email)   LIKE LOWER(CONCAT('%', :query, '%'))")
    List<Usuarios> buscarPorTexto(@Param("query") String query);

    // Buscar por perfil y texto
    @Query("SELECT u FROM Usuarios u WHERE " +
           "u.perfil.id = :perfilId AND (" +
           "LOWER(u.usuario) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(u.email)   LIKE LOWER(CONCAT('%', :query, '%')))")
    List<Usuarios> buscarPorTextoyPerfil(@Param("query") String query,
                                          @Param("perfilId") UUID perfilId);
}
