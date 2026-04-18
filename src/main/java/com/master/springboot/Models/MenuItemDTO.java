// MenuItemDTO.java
package com.master.springboot.Models;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class MenuItemDTO {
    private UUID id;
    private String nombre;
    private String nombreMostrar;
    private String icono;
    private String ruta;
    private Integer orden;
    private List<MenuItemDTO> hijos = new ArrayList<>();

    // Getters y Setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public String getNombreMostrar() { return nombreMostrar; }
    public void setNombreMostrar(String nombreMostrar) { this.nombreMostrar = nombreMostrar; }
    public String getIcono() { return icono; }
    public void setIcono(String icono) { this.icono = icono; }
    public String getRuta() { return ruta; }
    public void setRuta(String ruta) { this.ruta = ruta; }
    public Integer getOrden() { return orden; }
    public void setOrden(Integer orden) { this.orden = orden; }
    public List<MenuItemDTO> getHijos() { return hijos; }
    public void setHijos(List<MenuItemDTO> hijos) { this.hijos = hijos; }
}