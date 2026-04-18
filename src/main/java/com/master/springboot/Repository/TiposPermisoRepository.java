package com.master.springboot.Repository;

import com.master.springboot.Models.TipoPermiso;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface TiposPermisoRepository extends JpaRepository<TipoPermiso, UUID> {
}
