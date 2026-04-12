package com.master.springboot.Repository;

import com.master.springboot.Models.TiposPermiso;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface TiposPermisoRepository extends JpaRepository<TiposPermiso, UUID> {
}
