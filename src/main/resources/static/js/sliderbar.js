// sliderbar.js - Menú jerárquico dinámico
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
        
        // Si no existe el item de Módulos, crearlo
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

        // Guardar el elemento "Inicio"
        const inicioItem = navMenu.querySelector('li:first-child');
        
        // Limpiar el menú
        navMenu.innerHTML = '';
        
        // Reagregar "Inicio"
        navMenu.appendChild(inicioItem);
        
        const paginaActual = window.location.pathname;
        
        // ═══════════════════════════════════════════════════════════════
        // LISTA DE MÓDULOS QUE DEBEN IR AL DROPDOWN (los demás van directo)
        // ═══════════════════════════════════════════════════════════════
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
        
        console.log('📊 Módulos directos:', modulosDirectos.length, '| En dropdown:', modulosDropdown.length);
        console.log('📋 Directos:', modulosDirectos.map(m => m.nombreMostrar));
        console.log('📁 Dropdown:', modulosDropdown.map(m => m.nombreMostrar));
        
        // Agregar módulos directos al navbar (ej: prueba)
        modulosDirectos.forEach(modulo => {
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
        
        // Agregar módulos al dropdown "Módulos"
        if (modulosDropdown.length > 0) {
            let html = '';
            modulosDropdown.forEach(modulo => {
                html += this.renderizarModulo(modulo, paginaActual);
            });
            dropdown.innerHTML = html;
            navMenu.appendChild(navItem);
            navItem.style.display = 'flex';
            console.log('✅ Dropdown renderizado con', modulosDropdown.length, 'elementos');
        } else {
            navItem.style.display = 'none';
        }
        
        // Marcar ítem activo
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

// Inicializar cuando el DOM esté listo
document.addEventListener('DOMContentLoaded', () => {
    const sidebar = new SidebarManager();
    sidebar.inicializar();
});