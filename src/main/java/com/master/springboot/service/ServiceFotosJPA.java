package com.master.springboot.service;

import com.master.springboot.Models.Foto;
import com.master.springboot.Models.Usuarios;
import com.master.springboot.Repository.FotoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

@Service
public class ServiceFotosJPA implements ServiceFotos{

    @Autowired
    private FotoRepository repository;

    @Override
    public List<Foto> findAll(){
        return repository.findAll();
    }

    @Override
    public Foto findById(int id){
        return repository.findById(id).orElse(null);
    }

    @Override
    public Foto save(MultipartFile archivo, String descripcion, Usuarios usuarios) throws IOException{
        Foto foto = new Foto();
        foto.setNombre(archivo.getOriginalFilename());
        foto.setDescripcion(descripcion);
        foto.setTipo(archivo.getContentType());
        foto.setTamanio(archivo.getSize());
        foto.setDatos(archivo.getBytes());

        if(usuarios == null){
            throw new RuntimeException("El usuario no puede ser nulo");
        }

        foto.setUsuario(usuarios);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        foto.setFechaSubida(sdf.format(new Date()));

        return repository.save(foto);
    }

    @Override
    public Foto update(int id, String descripcion){
        Foto foto = repository.findById(id).orElse(null);
        if (foto != null){
            foto.setDescripcion(descripcion);
            return repository.save(foto);
        }
        return null;
    }

    @Override
    public void deleteById(int id){
        repository.deleteById(id);
    }

    @Override
    public List<Foto> findByUsuarioIdusuario(int idusuario){
        return repository.findByUsuarioIdusuario(idusuario);
    }

}
