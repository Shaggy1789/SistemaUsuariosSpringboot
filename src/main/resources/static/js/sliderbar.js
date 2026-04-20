// sliderbar.js - Versión DINÁMICA (soporta cualquier módulo nuevo)
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

        // Limpiar menú (mantener "Inicio")
        const inicioItem = navMenu.querySelector('li:first-child');
        navMenu.innerHTML = '';
        navMenu.appendChild(inicioItem);

        // Ordenar hijos
        modulos.forEach(m => {
            if (m.hijos) m.hijos = this.ordenar(m.hijos);
        });

        // Separar módulos
        const administracion = modulos.find(m => m.nombreMostrar === 'Administración');
        const otros = modulos.filter(m => m.nombreMostrar !== 'Administración');

        // 1. Agregar módulos directos (los que NO son Administración)
        this.ordenar(otros).forEach(modulo => {
            const li = document.createElement('li');
            const tieneHijos = modulo.hijos && modulo.hijos.length > 0;
            
            if (tieneHijos) {
                li.innerHTML = `
                    <span>${modulo.nombreMostrar} <i class="fas fa-chevron-down arrow"></i></span>
                    <div class="dropdown-nav">
                        ${modulo.hijos.map(h => 
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

        // 2. Agregar dropdown "Seguridad" con Administración dentro
        if (administracion && administracion.hijos?.length > 0) {
            const li = document.createElement('li');
            li.id = this.navItemId;
            li.innerHTML = `
                <span>Seguridad <i class="fas fa-chevron-down arrow"></i></span>
                <div class="dropdown-nav" id="${this.dropdownId}">
                    <div class="dropdown-nav-group">
                        <a href="#"><i class="fas fa-folder me-2"></i>Administración<i class="fas fa-chevron-right ms-auto" style="font-size:0.7rem;"></i></a>
                        <div class="dropdown-submenu">
                            ${this.ordenar(administracion.hijos).map(h => 
                                `<a href="${h.ruta || '#'}"><i class="fas ${h.icono || 'fa-circle'} me-2"></i>${h.nombreMostrar}</a>`
                            ).join('')}
                        </div>
                    </div>
                </div>
            `;
            navMenu.appendChild(li);
            li.style.display = 'flex';
        }
    }
}

document.addEventListener('DOMContentLoaded', () => {
    new SidebarManager().inicializar();
});