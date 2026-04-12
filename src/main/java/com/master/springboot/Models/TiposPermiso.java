package com.master.springboot.Models;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.GenericGenerator;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "tipos_permiso")
public class TiposPermiso {

    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "nombre", nullable = false, unique = true)
    private String nombre;

    @Column(name = "nombre_mostrar", nullable = false)
    private String nombreMostrar;

    @CreationTimestamp
    @Column(name = "creado_en", updatable = false)
    private OffsetDateTime creadoEn;

    // ── Getters & Setters ──────────────────────────────────────

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getNombreMostrar() { return nombreMostrar; }
    public void setNombreMostrar(String nombreMostrar) { this.nombreMostrar = nombreMostrar; }

    public OffsetDateTime getCreadoEn() { return creadoEn; }
    public void setCreadoEn(OffsetDateTime creadoEn) { this.creadoEn = creadoEn; }
}
