package com.master.springboot.Config;

import com.master.springboot.security.SeguridadInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Autowired
    private SeguridadInterceptor seguridadInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(seguridadInterceptor)
                // Rutas que intercepta
                .addPathPatterns(
                    "/",
                    "/usuarios/**",
                    "/perfiles/**",
                    "/permisos/**",
                    "/modulos/**",
                    "/galeria/**"
                )
                // Excluir recursos estáticos y API
                .excludePathPatterns(
                    "/login", "/login1",
                    "/logout",
                    "/api/**",
                    "/css/**", "/js/**", "/images/**",
                    "/webjars/**", "/error"
                );
    }
}
