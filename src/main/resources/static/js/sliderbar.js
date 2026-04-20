// sliderbar.js - Menú jerárquico con soporte para múltiples niveles
class SidebarManager {
    constructor() {
        this.navMenuSelector = '.topnav-menu';
        this.dropdownId = 'navModulosDropdown';
        this.navItemId = 'navModulosItem';
    }

    async inicializar() {
        try {
            const menuData = await this.cargarMenu();
            this.renderizarMenu(menuData);
        } catch (error) {
            console.error('Error al cargar el Menu:', error);
            this.mostrarError();
        }
    }

    async cargarMenu() {
        const response = await fetch('/api/menu/usuario');
        const data = await response.json();
        if (!data.success) throw new Error(data.message || 'Error al cargar menú');
        return data.data || [];
    }

    renderizarMenu(modulos) {
        const navMenu = document.querySelector(this.navMenuSelector);
        let navItem = document.getElementById(this.navItemId);
        let dropdown = document.getElementById(this.dropdownId);

        if (!navMenu) {
            console.error('❌ NavMenu no encontrado');
            return;
        }

        if (!navItem) {
            navItem = document.createElement('li');
            navItem.id = this.navItemId;
            navItem.style.display = 'none';
            navItem.innerHTML = `
                <span>Seguridad <i class="fas fa-chevron-down arrow"></i></span>
                <div class="dropdown-nav" id="${this.dropdownId}"></div>
            `;
        }

        dropdown = navItem.querySelector('.dropdown-nav');

        if (!modulos || modulos.length === 0) {
            navItem.style.display = 'none';
            return;
        }

        const inicioItem = navMenu.querySelector('li:first-child');
        navMenu.innerHTML = '';
        navMenu.appendChild(inicioItem);

        const paginaActual = window.location.pathname;

        // Ordenar alfabéticamente
        const ordenarAlfabeticamente = (lista) => {
            return lista.sort((a, b) => {
                const nombreA = (a.nombreMostrar || a.nombre || '').toLowerCase();
                const nombreB = (b.nombreMostrar || b.nombre || '').toLowerCase();
                return nombreA.localeCompare(nombreB, 'es', { sensitivity: 'base' });
            });
        };

        // Ordenar hijos recursivamente
        const ordenarHijos = (m) => {
            if (m.hijos && m.hijos.length > 0) {
                m.hijos = ordenarAlfabeticamente(m.hijos);
                m.hijos.forEach(h => ordenarHijos(h));
            }
        };
        modulos.forEach(m => ordenarHijos(m));

        // Separar módulos: directos vs dropdown
        const modulosDirectos = [];
        const modulosDropdown = [];

        modulos.forEach(m => {
            // Va al dropdown si: NO tiene ruta (es carpeta) O tiene hijos
            const debeIrAlDropdown = (!m.ruta || m.ruta === '') || (m.hijos && m.hijos.length > 0);
            
            if (debeIrAlDropdown) {
                modulosDropdown.push(m);
            } else {
                modulosDirectos.push(m);
            }
        });

        const directosOrdenados = ordenarAlfabeticamente(modulosDirectos);
        const dropdownOrdenados = ordenarAlfabeticamente(modulosDropdown);

        console.log('📊 Directos:', directosOrdenados.map(m => m.nombreMostrar));
        console.log('📁 Dropdown:', dropdownOrdenados.map(m => m.nombreMostrar));

        // Módulos directos al navbar (prueba1)
        directosOrdenados.forEach(modulo => {
            const li = document.createElement('li');
            const href = modulo.ruta || '#';
            const activo = paginaActual === href ? ' class="active"' : '';
            const icono = modulo.icono || 'fa-circle';
            const nombre = modulo.nombreMostrar || modulo.nombre;

            if (modulo.hijos && modulo.hijos.length > 0) {
                // Si tiene hijos, crear dropdown
                li.innerHTML = `
                    <span>${nombre} <i class="fas fa-chevron-down arrow"></i></span>
                    <div class="dropdown-nav" id="dropdown-${modulo.id}">
                        ${modulo.hijos.map(h => this.renderizarEnlace(h, paginaActual)).join('')}
                    </div>
                `;
            } else {
                li.innerHTML = `
                    <a href="${href}"${activo}>
                        <i class="fas ${icono} me-2"></i>${nombre}
                    </a>
                `;
            }
            navMenu.appendChild(li);
        });

        // Agregar dropdown "Seguridad"
        if (dropdownOrdenados.length > 0) {
            let html = '';
            dropdownOrdenados.forEach(modulo => {
                html += this.renderizarModulo(modulo, paginaActual);
            });
            dropdown.innerHTML = html;
            navMenu.appendChild(navItem);
            navItem.style.display = 'flex';
        } else {
            navItem.style.display = 'none';
        }

        this.marcarActivo(paginaActual);
    }

    renderizarEnlace(modulo, paginaActual) {
        const href = modulo.ruta || '#';
        const activo = paginaActual === href ? ' active-page' : '';
        const icono = modulo.icono || 'fa-circle';
        const nombre = modulo.nombreMostrar || modulo.nombre;
        return `<a href="${href}"${activo}><i class="fas ${icono} me-2"></i>${nombre}</a>`;
    }

    renderizarModulo(modulo, paginaActual) {
        const tieneHijos = modulo.hijos && modulo.hijos.length > 0;
        const icono = modulo.icono || 'fa-folder';
        const nombre = modulo.nombreMostrar || modulo.nombre;

        if (tieneHijos) {
            let submenuHtml = '';
            modulo.hijos.forEach(hijo => {
                if (hijo.hijos && hijo.hijos.length > 0) {
                    submenuHtml += this.renderizarModulo(hijo, paginaActual);
                } else {
                    submenuHtml += this.renderizarEnlace(hijo, paginaActual);
                }
            });

            return `
                <div class="dropdown-nav-group">
                    <a href="#"><i class="fas ${icono} me-2"></i>${nombre}<i class="fas fa-chevron-right ms-auto" style="font-size:0.7rem;"></i></a>
                    <div class="dropdown-submenu">${submenuHtml}</div>
                </div>
            `;
        } else {
            return this.renderizarEnlace(modulo, paginaActual);
        }
    }

    marcarActivo(paginaActual) {
        document.querySelectorAll('.topnav-menu > li > a').forEach(link => {
            link.classList.remove('active');
            if (link.getAttribute('href') === paginaActual) link.classList.add('active');
        });
    }

    mostrarError() {
        const navItem = document.getElementById(this.navItemId);
        if (navItem) navItem.style.display = 'none';
    }
}

document.addEventListener('DOMContentLoaded', () => {
    new SidebarManager().inicializar();
});