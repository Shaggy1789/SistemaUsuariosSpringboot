class SidebarManager {
    constructor(){
        this.sidebarId = 'navMenu';
        this.dropdownId = 'navModulosDropdown';
        this.navItemId = 'navModulosItem';
    }

   async inicializar(){
        try{
            const menuData = await this.cargarMenu();
            this.renderizarMenu(menuData);
        }catch(error){
            console.error('Error al cargar el Menu :', error);
            this.mostrarError();
        }
    }

    async cargarMenu(){
        const response = await fetch('/api/menu/usuario');
        const data = await response.json();

        if (!data.success) {
            throw new Error(data.menssage || 'Error al cargar menú');
        }

        return data.data || [];
    }

    renderizarMenu(modulos){
        const navMenu = document.getElementById(this.sidebarId);
        const navItem = document.getElementById(this.navItemId);
        const dropdown = document.getElementById(this.dropdownId);

        if(!navMenu || !navItem || !dropdown) return;
        
        if (!modulos || modulos.length === 0) {
            navItem.style.display = 'none';
            return;
        }

        const paginaActual = window.location.pathname;
        let html = '';

        modulos.forEach(modulo => {
            html += this.renderizarModulo(modulo, paginaActual);
        });

        dropdown.innerHTML = html;
        navItem.style.display = 'flex';

        //Marcamos item activo en el menu Principal
        this.marcarActivo(paginaActual);
    }
    renderizarModulo(modulo, paginaActual) {
        const tieneHijos = modulo.hijos && modulo.hijos.length > 0;
        const icono = modulo.icono || 'fa-circle';
        const nombre = modulo.nombreMostrar || modulo.nombre;
        
        if (tieneHijos) {
            // Módulo con submenú
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
            // Módulo simple
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
        // Marcar enlaces activos en el navbar principal
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
