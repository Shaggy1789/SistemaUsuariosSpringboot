// sliderbar.js - Menú jerárquico dinámico (orden alfabético, ignora campo "orden")
class SidebarManager {
    constructor(){
        this.navMenuSelector = '.topnav-menu';
        this.dropdownId = 'navModulosDropdown';
        this.navItemId = 'navModulosItem';
    }

    async inicializar(){
        try{
            const menuData = await this.cargarMenu();
            this.renderizarMenu(menuData);
        }catch(error){
            console.error('Error al cargar el Menu:', error);
            this.mostrarError();
        }
    }

    async cargarMenu(){
        const response = await fetch('/api/menu/usuario');
        const data = await response.json();

        if (!data.success) {
            throw new Error(data.message || 'Error al cargar menú');
        }

        return data.data || [];
    }

    renderizarMenu(modulos){
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
                <span>Módulos <i class="fas fa-chevron-down arrow"></i></span>
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
        
        // ═══════════════════════════════════════════════════════════════
        // ORDENAR ALFABÉTICAMENTE (IGNORANDO EL CAMPO "orden")
        // ═══════════════════════════════════════════════════════════════
        const ordenarAlfabeticamente = (lista) => {
            return lista.sort((a, b) => {
                const nombreA = (a.nombreMostrar || a.nombre || '').toLowerCase();
                const nombreB = (b.nombreMostrar || b.nombre || '').toLowerCase();
                return nombreA.localeCompare(nombreB, 'es', { sensitivity: 'base' });
            });
        };
        
        // Ordenar hijos alfabéticamente
        modulos.forEach(m => {
            if (m.hijos && m.hijos.length > 0) {
                m.hijos = ordenarAlfabeticamente(m.hijos);
            }
        });
        
        // Lista de módulos que van al dropdown
        const nombresDropdown = ['CONFIGURACIÓN', 'CONFIGURACION', 'PERFIL', 'PERMISOS', 'PERMISOS PERFIL', 'USUARIOS'];
        
        const modulosDirectos = [];
        const modulosDropdown = [];
        
        modulos.forEach(m => {
            const nombreUpper = (m.nombreMostrar || m.nombre || '').toUpperCase();
            const debeIrAlDropdown = nombresDropdown.includes(nombreUpper) || (m.hijos && m.hijos.length > 0);
            
            if (debeIrAlDropdown) {
                modulosDropdown.push(m);
            } else {
                modulosDirectos.push(m);
            }
        });
        
        // ORDENAR ALFABÉTICAMENTE ambos grupos
        const directosOrdenados = ordenarAlfabeticamente(modulosDirectos);
        const dropdownOrdenados = ordenarAlfabeticamente(modulosDropdown);
        
        console.log('📊 Módulos directos:', directosOrdenados.length, '| En dropdown:', dropdownOrdenados.length);
        console.log('📋 Directos:', directosOrdenados.map(m => m.nombreMostrar));
        console.log('📁 Dropdown:', dropdownOrdenados.map(m => m.nombreMostrar));
        
        // Agregar módulos directos al navbar
        directosOrdenados.forEach(modulo => {
            const li = document.createElement('li');
            const href = modulo.ruta || '#';
            const activo = paginaActual === href ? ' class="active"' : '';
            const icono = modulo.icono || 'fa-circle';
            const nombre = modulo.nombreMostrar || modulo.nombre;
            
            li.innerHTML = `
                <a href="${href}"${activo}>
                    <i class="fas ${icono} me-2"></i>${nombre}
                </a>
            `;
            navMenu.appendChild(li);
        });
        
        // Agregar módulos al dropdown
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

    renderizarModulo(modulo, paginaActual) {
        const tieneHijos = modulo.hijos && modulo.hijos.length > 0;
        const icono = modulo.icono || 'fa-circle';
        const nombre = modulo.nombreMostrar || modulo.nombre;
        
        if (tieneHijos) {
            let submenuHtml = '';
            modulo.hijos.forEach(hijo => {
                const hrefHijo = hijo.ruta || '#';
                const activoHijo = paginaActual === hrefHijo ? ' active-page' : '';
                const iconoHijo = hijo.icono || 'fa-circle';
                const nombreHijo = hijo.nombreMostrar || hijo.nombre;
                
                submenuHtml += `
                    <a href="${hrefHijo}"${activoHijo}>
                        <i class="fas ${iconoHijo} me-2"></i>${nombreHijo}
                    </a>
                `;
            });
            
            const hrefPadre = modulo.ruta || '#';
            const activoPadre = paginaActual === hrefPadre ? ' active-page' : '';
            
            return `
                <div class="dropdown-nav-group">
                    <a href="${hrefPadre}"${activoPadre}>
                        <i class="fas ${icono} me-2"></i>${nombre}
                        <i class="fas fa-chevron-right ms-auto" style="font-size:0.7rem;"></i>
                    </a>
                    <div class="dropdown-submenu">
                        ${submenuHtml}
                    </div>
                </div>
            `;
        } else {
            const href = modulo.ruta || '#';
            const activo = paginaActual === href ? ' active-page' : '';
            
            return `
                <a href="${href}"${activo}>
                    <i class="fas ${icono} me-2"></i>${nombre}
                </a>
            `;
        }
    }

    marcarActivo(paginaActual) {
        document.querySelectorAll('.topnav-menu > li > a').forEach(link => {
            link.classList.remove('active');
            if (link.getAttribute('href') === paginaActual) {
                link.classList.add('active');
            }
        });
    }

    mostrarError() {
        const navItem = document.getElementById(this.navItemId);
        if (navItem) {
            navItem.style.display = 'none';
        }
    }
}

document.addEventListener('DOMContentLoaded', () => {
    const sidebar = new SidebarManager();
    sidebar.inicializar();
});