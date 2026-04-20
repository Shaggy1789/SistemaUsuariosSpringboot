// sliderbar.js - Versión que USA el navModulosItem EXISTENTE en el HTML
class SidebarManager {
    constructor() {
        this.navMenuSelector = '.topnav-menu';
        this.dropdownId = 'navModulosDropdown';
        this.navItemId = 'navModulosItem';
    }

    async inicializar() {
        try {
            const response = await fetch('/api/menu/usuario');
            const data = await response.json();
            if (!data.success) throw new Error(data.message);
            this.renderizarMenu(data.data || []);
        } catch (error) {
            console.error('Error:', error);
        }
    }

    ordenar(lista) {
        return lista.sort((a, b) => {
            const nombreA = (a.nombreMostrar || a.nombre || '').toLowerCase();
            const nombreB = (b.nombreMostrar || b.nombre || '').toLowerCase();
            return nombreA.localeCompare(nombreB, 'es', { sensitivity: 'base' });
        });
    }

    renderizarMenu(modulos) {
        const navMenu = document.querySelector(this.navMenuSelector);
        if (!navMenu) return;

        // Guardar "Inicio"
        const inicioItem = navMenu.querySelector('li:first-child');
        
        // Guardar el navModulosItem EXISTENTE (el que está en el HTML)
        let navItem = document.getElementById(this.navItemId);
        let dropdown = document.getElementById(this.dropdownId);

        // Limpiar menú
        navMenu.innerHTML = '';
        navMenu.appendChild(inicioItem);

        // Ordenar hijos
        modulos.forEach(m => {
            if (m.hijos) m.hijos = this.ordenar(m.hijos);
        });

        // Separar módulos
        const administracion = modulos.find(m => m.nombreMostrar === 'Administración');
        const otros = modulos.filter(m => m.nombreMostrar !== 'Administración');

        // 1. Agregar módulos directos (prueba1, prueba2, prueba3, etc.)
        this.ordenar(otros).forEach(modulo => {
            const li = document.createElement('li');
            const tieneHijos = modulo.hijos && modulo.hijos.length > 0;
            
            if (tieneHijos) {
                li.innerHTML = `
                    <span>${modulo.nombreMostrar} <i class="fas fa-chevron-down arrow"></i></span>
                    <div class="dropdown-nav">
                        ${this.ordenar(modulo.hijos).map(h => 
                            `<a href="${h.ruta || '#'}"><i class="fas ${h.icono || 'fa-circle'} me-2"></i>${h.nombreMostrar}</a>`
                        ).join('')}
                    </div>
                `;
            } else {
                li.innerHTML = `
                    <a href="${modulo.ruta || '#'}">
                        <i class="fas ${modulo.icono || 'fa-circle'} me-2"></i>${modulo.nombreMostrar}
                    </a>
                `;
            }
            navMenu.appendChild(li);
        });

        // 2. USAR el navModulosItem EXISTENTE para Administración
        if (!navItem) {
            // Si no existe, crearlo (por si acaso)
            navItem = document.createElement('li');
            navItem.id = this.navItemId;
            navItem.innerHTML = `
                <span>Administración <i class="fas fa-chevron-down arrow"></i></span>
                <div class="dropdown-nav" id="${this.dropdownId}"></div>
            `;
        }
        
        dropdown = navItem.querySelector('.dropdown-nav');
        
        if (administracion && administracion.hijos?.length > 0) {
            // Limpiar dropdown existente
            dropdown.innerHTML = '';
            
            // Agregar hijos de Administración DIRECTAMENTE (sin submenú anidado)
            this.ordenar(administracion.hijos).forEach(h => {
                const a = document.createElement('a');
                a.href = h.ruta || '#';
                a.innerHTML = `<i class="fas ${h.icono || 'fa-circle'} me-2"></i>${h.nombreMostrar}`;
                dropdown.appendChild(a);
            });
            
            navMenu.appendChild(navItem);
            navItem.style.display = 'flex';
        } else {
            navItem.style.display = 'none';
        }
    }
}

document.addEventListener('DOMContentLoaded', () => {
    new SidebarManager().inicializar();
});