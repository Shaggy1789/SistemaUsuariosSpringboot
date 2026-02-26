package com.master.springboot.Repository;

import com.master.springboot.Models.Foto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;


@Repository
public interface FotoRepository extends JpaRepository<Foto, Integer> {
    List<Foto> findByUsuarioIdusuario(int idusuario);
}
