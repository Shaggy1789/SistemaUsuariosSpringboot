package com.master.springboot.Controller;
import com.master.springboot.Models.Roles;
import com.master.springboot.service.ServiceRoles;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class RolesController {
    @Autowired
    ServiceRoles serviceRoles;

    @GetMapping("/api/roles")
    public List<Roles> MostrarRoles(){
        System.out.println("Mostrando Roles..");
        return serviceRoles.findAll();
    }

}
