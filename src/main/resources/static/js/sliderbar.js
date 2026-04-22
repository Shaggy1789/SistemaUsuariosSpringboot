// sliderbar.js - Versión que crea padres virtuales si no existen
class SidebarManager {
    constructor() {
        this.navMenuSelector = '.topnav-menu';
        this.dropdownId = 'navModulosDropdown';
        this.navItemId = 'navModulosItem';
        
        // Definir agrupaciones (padre -> [hijos])
        this.agrupaciones = {
            'prueba1': ['prueba1.1', 'prueba1.2', 'prueba1.3', 'prueba1.4'],
            'prueba2': ['prueba2.1', 'prueba2.2', 'prueba2.3', 'prueba2.4']
        };
        
        // Módulos que van dentro de Seguridad
        this.seguridadNames = ['Usuarios', 'Perfil', 'Permisos Perfil', 'Modulos'];
    }

    async inicializar() {
        try {
            if (typeof permisosUI !== 'undefined') {
                await permisosUI.inicializar();
            }
            
            const response = await fetch('/api/menu/usuario');
            const data = await response.json();
            if (!data.success) throw new Error(data.message);
            
            let modulos = data.data || [];
            
            console.log('📥 Módulos recibidos:', modulos.map(m => m.nombreMostrar || m.nombre));
            
            // Agrupar módulos
            modulos = this.agruparModulos(modulos);
            
            console.log('📋 Módulos agrupados:', modulos.map(m => ({
                nombre: m.nombreMostrar,
                hijos: m.hijos?.map(h => h.nombreMostrar || h.nombre) || []
            })));
            
            this.renderizarMenu(modulos);
        } catch (error) {
            console.error('Error:', error);
        }
    }

    agruparModulos(modulos) {
        // Crear mapa para búsqueda rápida
        const mapaModulos = new Map();
        for (const modulo of modulos) {
            const nombre = (modulo.nombreMostrar || modulo.nombre || '').toLowerCase();
            mapaModulos.set(nombre, modulo);
        }
        
        const modulosUsados = new Set();
        const resultado = [];
        
        // 1. Procesar agrupaciones (prueba1, prueba2) - CREAR PADRES VIRTUALES
        for (const [padreNombre, hijosNombres] of Object.entries(this.agrupaciones)) {
            const padreLower = padreNombre.toLowerCase();
            const hijosEncontrados = [];
            
            // Buscar hijos existentes
            for (const hijoNombre of hijosNombres) {
                const hijoLower = hijoNombre.toLowerCase();
                const moduloHijo = mapaModulos.get(hijoLower);
                if (moduloHijo) {
                    hijosEncontrados.push(moduloHijo);
                    modulosUsados.add(hijoLower);
                }
            }
            
            // Si tiene al menos un hijo, crear el padre (virtual o real)
            if (hijosEncontrados.length > 0) {
                let moduloPadre = mapaModulos.get(padreLower);
                
                if (!moduloPadre) {
                    // Crear padre virtual si no existe
                    moduloPadre = {
                        id: `virtual-${padreNombre}`,
                        nombre: padreNombre.toUpperCase(),
                        nombreMostrar: padreNombre,
                        ruta: '#',
                        icono: 'fa-folder',
                        orden: 0,
                        virtual: true
                    };
                }
                
                // Agregar hijos al padre
                moduloPadre.hijos = hijosEncontrados;
                resultado.push(moduloPadre);
                modulosUsados.add(padreLower);
            }
        }
        
        // 2. Procesar módulos de Seguridad
        const seguridadModulos = [];
        for (const nombreSeguridad of this.seguridadNames) {
            const nombreLower = nombreSeguridad.toLowerCase();
            const modulo = mapaModulos.get(nombreLower);
            if (modulo && !modulosUsados.has(nombreLower)) {
                seguridadModulos.push(modulo);
                modulosUsados.add(nombreLower);
            }
        }
        
        // 3. Agregar módulos no usados (que sobran)
        for (const [nombre, modulo] of mapaModulos) {
            if (!modulosUsados.has(nombre)) {
                resultado.push(modulo);
            }
        }
        
        // 4. Agregar Seguridad como grupo
        if (seguridadModulos.length > 0) {
            resultado.push({
                id: 'seguridad-group',
                nombre: 'SEGURIDAD_GROUP',
                nombreMostrar: 'Seguridad',
                ruta: '#',
                icono: 'fa-shield-alt',
                orden: 0,
                hijos: seguridadModulos
            });
        }
        
        return resultado;
    }

    renderizarMenu(modulos) {
        const navMenu = document.querySelector(this.navMenuSelector);
        if (!navMenu) return;

        const inicioItem = navMenu.querySelector('li:first-child');
        navMenu.innerHTML = '';
        navMenu.appendChild(inicioItem);

        if (!modulos || modulos.length === 0) return;

        for (const modulo of modulos) {
            const li = document.createElement('li');
            const tieneHijos = modulo.hijos && modulo.hijos.length > 0;
            
            if (tieneHijos) {
                // Módulo con dropdown
                li.innerHTML = `
                    <span>${modulo.nombreMostrar} <i class="fas fa-chevron-down arrow"></i></span>
                    <div class="dropdown-nav">
                        ${modulo.hijos.map(h => {
                            const nombreHijo = h.nombreMostrar || h.nombre || '';
                            const rutaHijo = h.ruta || '#';
                            const iconoHijo = h.icono || 'fa-circle';
                            return `<a href="${rutaHijo}"><i class="fas ${iconoHijo} me-2"></i>${nombreHijo}</a>`;
                        }).join('')}
                    </div>
                `;
            } else {
                // Módulo sin hijos
                const ruta = modulo.ruta || '#';
                const icono = modulo.icono || 'fa-circle';
                const nombre = modulo.nombreMostrar || modulo.nombre || '';
                li.innerHTML = `<a href="${ruta}"><i class="fas ${icono} me-2"></i>${nombre}</a>`;
            }
            navMenu.appendChild(li);
        }
    }
}

document.addEventListener('DOMContentLoaded', () => {
    new SidebarManager().inicializar();
});