package com.master.springboot.Models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Usuarios {
    @Id
    private int idusuario;
    private String nombreusuario;
    private String apellidopaterno;
    private String apellidomaterno;
    private String email;
    private String password;
    private long telefono;

    @ManyToOne
    @JoinColumn(name = "roles")
    @JsonIgnoreProperties("usuarios")
    private Roles role;

    @OneToMany(mappedBy = "usuario", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore
    private List<Foto> fotos;

    public Roles getRole() {
        return role;
    }
}
