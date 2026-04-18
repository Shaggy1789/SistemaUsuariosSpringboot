package com.master.springboot.service;

import com.master.springboot.Models.Modulos;
import com.master.springboot.Models.PerfilPermisos;
import com.master.springboot.Models.Perfiles;
import com.master.springboot.Models.TipoPermiso;
import com.master.springboot.Repository.ModulosRepository;
import com.master.springboot.Repository.PerfilPermisosRepository;
import com.master.springboot.Repository.PerfilesRepository;
import com.master.springboot.Repository.TiposPermisoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class ServicePerfilPermisos {

    @Autowired
    private PerfilPermisosRepository perfilPermisosRepository;

    @Autowired
    private PerfilesRepository perfilesRepository;

    @Autowired
    private ModulosRepository modulosRepository;

    @Autowired
    private TiposPermisoRepository tiposPermisoRepository;

    public List<PerfilPermisos> findByPerfilId(UUID perfilId) {
        return perfilPermisosRepository.findByPerfilId(perfilId);
    }

    public List<PerfilPermisos> findByPerfilIdAndModuloId(UUID perfilId, UUID moduloId) {
        return perfilPermisosRepository.findByPerfilIdAndModuloId(perfilId, moduloId);
    }

    public boolean tienePermiso(UUID perfilId, UUID moduloId, UUID tipoPermisoId) {
        return perfilPermisosRepository.existsByPerfilIdAndModuloIdAndTipoPermisoId(
                perfilId, moduloId, tipoPermisoId);
    }

    /**
     * Reemplaza todos los permisos de un perfil sobre un módulo.
     * Recibe una lista de IDs de tipo_permiso y los inserta frescos.
     */
    @Transactional
    public void guardarPermisosDeModulo(UUID perfilId, UUID moduloId, List<UUID> tiposPermisoIds) {
        // 1. Borrar permisos anteriores del perfil+módulo
        perfilPermisosRepository.deleteByPerfilIdAndModuloId(perfilId, moduloId);

        if (tiposPermisoIds == null || tiposPermisoIds.isEmpty()) return;

        // 2. Resolver entidades
        Perfiles perfil = perfilesRepository.findById(perfilId)
                .orElseThrow(() -> new RuntimeException("Perfil no encontrado: " + perfilId));
        Modulos modulo = modulosRepository.findById(moduloId)
                .orElseThrow(() -> new RuntimeException("Módulo no encontrado: " + moduloId));

        // 3. Insertar nuevos permisos
        for (UUID tpId : tiposPermisoIds) {
            TipoPermiso tp = tiposPermisoRepository.findById(tpId)
                    .orElseThrow(() -> new RuntimeException("Tipo permiso no encontrado: " + tpId));

            PerfilPermisos pp = new PerfilPermisos();
            pp.setPerfil(perfil);
            pp.setModulo(modulo);
            pp.setTipoPermiso(tp);
            perfilPermisosRepository.save(pp);
        }
    }

    @Transactional
    public void eliminarPermisosDePerfil(UUID perfilId) {
        perfilPermisosRepository.deleteByPerfilId(perfilId);
    }
}
