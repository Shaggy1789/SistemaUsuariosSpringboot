package com.master.springboot.Models;

import java.util.List;
import java.util.UUID;

/**
 * DTO que representa los permisos de un perfil sobre un módulo.
 * Usado para enviar/recibir datos de la página de permisos.
 */
public class PerfilPermisoDTO {

    private UUID moduloId;
    private String moduloNombre;
    private String moduloNombreMostrar;
    private String moduloIcono;
    private List<UUID> tiposPermisoIds;   // IDs de permisos que TIENE el perfil

    // ── Getters & Setters ──────────────────────────────────────

    public UUID getModuloId() { return moduloId; }
    public void setModuloId(UUID moduloId) { this.moduloId = moduloId; }

    public String getModuloNombre() { return moduloNombre; }
    public void setModuloNombre(String moduloNombre) { this.moduloNombre = moduloNombre; }

    public String getModuloNombreMostrar() { return moduloNombreMostrar; }
    public void setModuloNombreMostrar(String moduloNombreMostrar) { this.moduloNombreMostrar = moduloNombreMostrar; }

    public String getModuloIcono() { return moduloIcono; }
    public void setModuloIcono(String moduloIcono) { this.moduloIcono = moduloIcono; }

    public List<UUID> getTiposPermisoIds() { return tiposPermisoIds; }
    public void setTiposPermisoIds(List<UUID> tiposPermisoIds) { this.tiposPermisoIds = tiposPermisoIds; }
}
