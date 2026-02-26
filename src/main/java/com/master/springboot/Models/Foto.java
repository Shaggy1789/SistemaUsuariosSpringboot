package com.master.springboot.Models;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "foto")
public class Foto {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    private String nombre;
    private String descripcion;
    private String tipo;
    private long tamanio;

    @JdbcTypeCode(SqlTypes.BINARY)
    @Column(columnDefinition = "BYTEA", nullable = false)
    private byte[] datos;

    private String fechaSubida;

    @ManyToOne
    @JoinColumn(name = "idusuario")
    private Usuarios usuario;
}