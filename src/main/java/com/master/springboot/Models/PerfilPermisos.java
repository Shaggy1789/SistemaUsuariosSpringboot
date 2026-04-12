package com.master.springboot.Models;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.GenericGenerator;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "perfil_permisos",
    uniqueConstraints = @UniqueConstraint(
        columnNames = {"perfil_id", "modulo_id", "tipo_permiso_id"}
    )
)
public class PerfilPermisos {

    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "perfil_id", nullable = false)
    private Perfiles perfil;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "modulo_id", nullable = false)
    private Modulos modulo;

    // Apunta a tipos_permiso (la tabla con auto-UUID)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tipo_permiso_id", nullable = false,
                foreignKey = @ForeignKey(name = "perfil_permisos_tipo_permiso_id_fkey"))
    private TiposPermiso tipoPermiso;

    @CreationTimestamp
    @Column(name = "creado_en", updatable = false)
    private OffsetDateTime creadoEn;

    // ── Getters & Setters ──────────────────────────────────────

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public Perfiles getPerfil() { return perfil; }
    public void setPerfil(Perfiles perfil) { this.perfil = perfil; }

    public Modulos getModulo() { return modulo; }
    public void setModulo(Modulos modulo) { this.modulo = modulo; }

    public TiposPermiso getTipoPermiso() { return tipoPermiso; }
    public void setTipoPermiso(TiposPermiso tipoPermiso) { this.tipoPermiso = tipoPermiso; }

    public OffsetDateTime getCreadoEn() { return creadoEn; }
    public void setCreadoEn(OffsetDateTime creadoEn) { this.creadoEn = creadoEn; }
}
