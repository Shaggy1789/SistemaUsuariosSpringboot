// permisos-ui.js - Control de visibilidad de botones según permisos
class PermisosUI {
    constructor() {
        this.permisosUsuario = new Map(); // moduloId -> Set de permisos
        this.inicializado = false;
        this.esAdmin = false; // ← bandera para ADMIN
    }

    async inicializar() {
        if (this.inicializado) return;
        
        try {
            const usuario = await this.obtenerUsuarioActual();
            if (!usuario || !usuario.perfilId) return;
            
            // ✅ Verificar si es ADMINISTRADOR
            const perfilNombre = (usuario.perfilNombre || '').toUpperCase();
            this.esAdmin = (perfilNombre === 'ADMINISTRADOR' || perfilNombre === 'ADMIN');
            
            if (this.esAdmin) {
                console.log('👑 Usuario ADMIN - todos los botones visibles');
                // No necesita cargar permisos porque verá todo
            } else {
                await this.cargarPermisos(usuario.perfilId);
            }
            
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
        // ✅ Si es ADMIN, tiene todos los permisos
        if (this.esAdmin) return true;
        
        // Buscar el ID del módulo por su nombre
        const moduloId = this.obtenerModuloId(moduloNombre);
        if (!moduloId) return false;
        
        const permisos = this.permisosUsuario.get(moduloId);
        if (!permisos) return false;
        
        // Buscar el ID del tipo de permiso
        const tipoPermisoId = this.obtenerTipoPermisoId(tipoPermiso);
        return permisos.has(tipoPermisoId);
    }

    // Mapeo de nombres de módulos a IDs
    obtenerModuloId(nombreModulo) {
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
            
            if (btnNuevo) {
                btnNuevo.style.display = this.tienePermiso('USUARIOS', 'CREAR') ? 'flex' : 'none';
            }
            
            btnsEditar.forEach(btn => {
                btn.style.display = this.tienePermiso('USUARIOS', 'EDITAR') ? 'inline-block' : 'none';
            });
            
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
        
        // ═══════════════════════════════════════════════════════════════
        // ✅ NUEVO: Módulo de Permisos - SOLO LECTURA para no ADMIN
        // ═══════════════════════════════════════════════════════════════
        if (moduloActual === 'permisos') {
            if (!this.esAdmin) {
                console.log('🔒 Usuario NO ADMIN - Permisos en modo SOLO LECTURA');
                
                // Deshabilitar todos los checkboxes
                const checkboxes = document.querySelectorAll('.perm-check');
                checkboxes.forEach(cb => {
                    cb.disabled = true;
                    cb.style.cursor = 'not-allowed';
                    cb.style.opacity = '0.6';
                });
                
                // Ocultar botones de acción
                const btnGuardar = document.getElementById('btnGuardar');
                const btnTodos = document.querySelector('button[onclick*="marcarTodos(true)"]');
                const btnNinguno = document.querySelector('button[onclick*="marcarTodos(false)"]');
                const btnCancelar = document.querySelector('button[onclick*="cancelar()"]');
                const selectorPerfil = document.getElementById('perfilSelect');
                const btnBuscar = document.getElementById('btnBuscar');
                
                if (btnGuardar) btnGuardar.style.display = 'none';
                if (btnTodos) btnTodos.style.display = 'none';
                if (btnNinguno) btnNinguno.style.display = 'none';
                if (btnCancelar) btnCancelar.style.display = 'none';
                
                // Mantener visible el selector y botón buscar para consultar
                if (selectorPerfil) selectorPerfil.disabled = false;
                if (btnBuscar) btnBuscar.style.display = 'flex';
                
                // Agregar mensaje de "Solo lectura"
                const headerActions = document.querySelector('.permisos-header-actions');
                if (headerActions) {
                    const soloLecturaBadge = document.createElement('span');
                    soloLecturaBadge.className = 'admin-badge';
                    soloLecturaBadge.style.marginLeft = '10px';
                    soloLecturaBadge.innerHTML = '<i class="fas fa-eye"></i> Solo lectura';
                    headerActions.appendChild(soloLecturaBadge);
                }
            } else {
                console.log('👑 Usuario ADMIN - Permisos en modo EDICIÓN');
                
                // Asegurar que todo esté habilitado para ADMIN
                const checkboxes = document.querySelectorAll('.perm-check');
                checkboxes.forEach(cb => {
                    cb.disabled = false;
                    cb.style.cursor = 'pointer';
                    cb.style.opacity = '1';
                });
                
                const btnGuardar = document.getElementById('btnGuardar');
                const btnTodos = document.querySelector('button[onclick*="marcarTodos(true)"]');
                const btnNinguno = document.querySelector('button[onclick*="marcarTodos(false)"]');
                
                if (btnGuardar) btnGuardar.style.display = 'flex';
                if (btnTodos) btnTodos.style.display = 'inline-flex';
                if (btnNinguno) btnNinguno.style.display = 'inline-flex';
            }
        }
    }
}

// Inicializar automáticamente
const permisosUI = new PermisosUI();

document.addEventListener('DOMContentLoaded', async () => {
    await permisosUI.inicializar();
    
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