// permisos-ui.js - Control de visibilidad de botones según permisos
class PermisosUI {
    constructor() {
        this.permisosUsuario = new Map(); // moduloId -> Set de permisos
        this.inicializado = false;
    }

    async inicializar() {
        if (this.inicializado) return;
        
        try {
            const usuario = await this.obtenerUsuarioActual();
            if (!usuario || !usuario.perfilId) return;
            
            await this.cargarPermisos(usuario.perfilId);
            this.inicializado = true;
            console.log('✅ Permisos UI cargados');
        } catch (error) {
            console.error('Error cargando permisos:', error);
        }
    }

    async obtenerUsuarioActual() {
        const response = await fetch('/api/sesion');
        const data = await response.json();
        return data.usuario || null;
    }

    async cargarPermisos(perfilId) {
        const response = await fetch(`/api/permisos/${perfilId}`);
        const data = await response.json();
        
        if (data.success) {
            data.data.forEach(item => {
                const permisos = new Set(item.tiposPermisoIds || []);
                this.permisosUsuario.set(item.moduloId, permisos);
            });
        }
    }

    // Verificar si el usuario tiene un permiso específico para un módulo
    tienePermiso(moduloNombre, tipoPermiso) {
        // Buscar el ID del módulo por su nombre
        const moduloId = this.obtenerModuloId(moduloNombre);
        if (!moduloId) return false;
        
        const permisos = this.permisosUsuario.get(moduloId);
        if (!permisos) return false;
        
        // Buscar el ID del tipo de permiso
        const tipoPermisoId = this.obtenerTipoPermisoId(tipoPermiso);
        return permisos.has(tipoPermisoId);
    }

    // Mapeo de nombres de módulos a IDs (se actualiza dinámicamente)
    obtenerModuloId(nombreModulo) {
        // IDs comunes (debes actualizarlos con los tuyos)
        const mapeo = {
            'USUARIOS': 'c8089b29-5319-4bcc-ab96-8f4c5098e8cb',
            'PERFILES': '9c493a8c-664f-4baf-a2db-dd47b0d2c41a',
            'PERMISOS': '6454c22f-a9e5-4d37-8703-fb1074ff4e1c',
            'CONFIGURACION': 'd4aa6509-2fb5-4d12-8ca3-c809cc642203'
        };
        return mapeo[nombreModulo] || null;
    }

    // Mapeo de tipos de permiso a IDs
    obtenerTipoPermisoId(tipo) {
        const mapeo = {
            'VER': 'id-del-permiso-ver',
            'CREAR': 'id-del-permiso-crear',
            'EDITAR': 'id-del-permiso-editar',
            'ELIMINAR': 'id-del-permiso-eliminar'
        };
        return mapeo[tipo] || tipo;
    }

    // Ocultar/Mostrar botones según permisos
    aplicarVisibilidad(moduloActual) {
        // Módulo de Usuarios
        if (moduloActual === 'usuarios') {
            const btnNuevo = document.querySelector('.btn-nuevo');
            const btnsEditar = document.querySelectorAll('.btn-edit');
            const btnsEliminar = document.querySelectorAll('.btn-del');
            
            // Botón "Nuevo Usuario" - requiere CREAR
            if (btnNuevo) {
                btnNuevo.style.display = this.tienePermiso('USUARIOS', 'CREAR') ? 'flex' : 'none';
            }
            
            // Botones Editar - requiere EDITAR
            btnsEditar.forEach(btn => {
                btn.style.display = this.tienePermiso('USUARIOS', 'EDITAR') ? 'inline-block' : 'none';
            });
            
            // Botones Eliminar - requiere ELIMINAR
            btnsEliminar.forEach(btn => {
                btn.style.display = this.tienePermiso('USUARIOS', 'ELIMINAR') ? 'inline-block' : 'none';
            });
        }
        
        // Módulo de Perfiles
        if (moduloActual === 'perfiles') {
            const btnAdd = document.querySelector('.btn-add');
            const btnsEditar = document.querySelectorAll('.btn-edit');
            const btnsEliminar = document.querySelectorAll('.btn-del');
            
            if (btnAdd) {
                btnAdd.style.display = this.tienePermiso('PERFILES', 'CREAR') ? 'flex' : 'none';
            }
            
            btnsEditar.forEach(btn => {
                btn.style.display = this.tienePermiso('PERFILES', 'EDITAR') ? 'inline-block' : 'none';
            });
            
            btnsEliminar.forEach(btn => {
                btn.style.display = this.tienePermiso('PERFILES', 'ELIMINAR') ? 'inline-block' : 'none';
            });
        }
        
        // Módulo de Configuración (Módulos)
        if (moduloActual === 'configuracion') {
            const btnAdd = document.querySelector('.btn-add');
            const btnsEditar = document.querySelectorAll('.btn-edit');
            const btnsEliminar = document.querySelectorAll('.btn-del');
            
            if (btnAdd) {
                btnAdd.style.display = this.tienePermiso('CONFIGURACION', 'CREAR') ? 'flex' : 'none';
            }
            
            btnsEditar.forEach(btn => {
                btn.style.display = this.tienePermiso('CONFIGURACION', 'EDITAR') ? 'inline-block' : 'none';
            });
            
            btnsEliminar.forEach(btn => {
                btn.style.display = this.tienePermiso('CONFIGURACION', 'ELIMINAR') ? 'inline-block' : 'none';
            });
        }
    }
}

// Inicializar automáticamente
const permisosUI = new PermisosUI();

document.addEventListener('DOMContentLoaded', async () => {
    await permisosUI.inicializar();
    
    // Detectar en qué módulo estamos
    const path = window.location.pathname;
    if (path.includes('/usuarios')) {
        permisosUI.aplicarVisibilidad('usuarios');
    } else if (path.includes('/perfiles')) {
        permisosUI.aplicarVisibilidad('perfiles');
    } else if (path.includes('/configuracion')) {
        permisosUI.aplicarVisibilidad('configuracion');
    } else if (path.includes('/permisos')) {
        permisosUI.aplicarVisibilidad('permisos');
    }
});
