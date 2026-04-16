package com.master.springboot.Models;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "usuarios")
public class Usuarios {

    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "usuario", nullable = false, unique = true)
    private String usuario;

    @Column(name = "password", nullable = false)
    private String password;

    @Column(name = "email")
    private String email;

    @Column(name = "estado", columnDefinition = "varchar(255) DEFAULT 'ACTIVO'")
    private String estado = "ACTIVO";

    // 🆕 NUEVO: Campo para la foto en Base64
    @Column(name = "foto", columnDefinition = "TEXT")
    private String foto;

    // 🆕 NUEVO: Tipo de imagen (ej: "image/png", "image/jpeg")
    @Column(name = "foto_tipo")
    private String fotoTipo;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "perfil_id",
            foreignKey = @ForeignKey(name = "usuarios_perfil_id_fkey"))
    private Perfiles perfil;

    @CreationTimestamp
    @Column(name = "creado_en", updatable = false)
    private OffsetDateTime creadoEn;

    @UpdateTimestamp
    @Column(name = "actualizado_en")
    private OffsetDateTime actualizadoEn;

    // ── Getters & Setters ──────────────────────────────────────

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public String getUsuario() { return usuario; }
    public void setUsuario(String usuario) { this.usuario = usuario; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }

    // 🆕 Getters y Setters para foto
    public String getFoto() { return foto; }
    public void setFoto(String foto) { this.foto = foto; }

    public String getFotoTipo() { return fotoTipo; }
    public void setFotoTipo(String fotoTipo) { this.fotoTipo = fotoTipo; }

    public Perfiles getPerfil() { return perfil; }
    public void setPerfil(Perfiles perfil) { this.perfil = perfil; }

    public OffsetDateTime getCreadoEn() { return creadoEn; }
    public void setCreadoEn(OffsetDateTime creadoEn) { this.creadoEn = creadoEn; }

    public OffsetDateTime getActualizadoEn() { return actualizadoEn; }
    public void setActualizadoEn(OffsetDateTime actualizadoEn) { this.actualizadoEn = actualizadoEn; }
}