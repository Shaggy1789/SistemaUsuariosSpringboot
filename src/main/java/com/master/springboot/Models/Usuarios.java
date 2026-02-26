package com.master.springboot.Models;

import jakarta.persistence.*;
import lombok.*;
import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.List;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "usuarios")
public class Usuarios {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer idusuario;

    private String nombreusuario;
    private String apellidopaterno;
    private String apellidomaterno;
    private String email;
    private String password;
    private Long telefono;

    @ManyToOne
    @JoinColumn(name = "roles")
    private Roles role;

    @OneToMany(mappedBy = "usuario", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore
    private List<Foto> fotos;

    public Roles getRole() {
        return role;
    }
}