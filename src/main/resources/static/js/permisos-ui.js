// permisos-ui.js - Control de visibilidad de botones según permisos
class PermisosUI {
    constructor() {
        this.permisosUsuario = new Map();
        this.inicializado = false;
        this.esAdmin = false;
    }

    async inicializar() {
        if (this.inicializado) return;

        try {
            const usuario = await this.obtenerUsuarioActual();
            if (!usuario || !usuario.perfilId) return;

            const perfilNombre = (usuario.perfilNombre || '').toUpperCase();
            this.esAdmin = (perfilNombre === 'ADMINISTRADOR' || perfilNombre === 'ADMIN');

            if (this.esAdmin) {
                console.log('👑 Usuario ADMIN - todos los botones visibles');
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

    tienePermiso(moduloNombre, tipoPermiso) {
        if (this.esAdmin) return true;

        const moduloId = this.obtenerModuloId(moduloNombre);
        if (!moduloId) {
            console.warn(`⚠️ Módulo no encontrado: ${moduloNombre}`);
            return false;
        }

        const permisos = this.permisosUsuario.get(moduloId);
        if (!permisos) return false;

        const tipoPermisoId = this.obtenerTipoPermisoId(tipoPermiso);
        return permisos.has(tipoPermisoId);
    }

    obtenerModuloId(nombreModulo) {
        const mapeo = {
            'USUARIOS': 'c8089b29-5319-4bcc-ab96-8f4c5098e8cb',
            'PERFILES': '9c493a8c-664f-4baf-a2db-dd47b0d2c41a',
            'PERMISOS': '6454c22f-a9e5-4d37-8703-fb1074ff4e1c',
            'MODULOS': 'd4aa6509-2fb5-4d12-8ca3-c809cc642203',
            'PRUEBA1': '34ef9528-0736-4e57-b94a-ee4c5e70f180',
            'PRUEBA2': '711663ba-b4b2-49b2-8f38-3024907183bf',
            'PRUEBA1.1': '4ee84804-d66b-4300-b594-6a9238aa0249',
            'PRUEBA1.2': '2ebb4cf2-0108-462c-8e05-3604d01c86eb',
            'PRUEBA2.1': '972d3e7a-e8d5-4770-902b-3f8b29370ce4',
            'PRUEBA2.2': 'd33c7bb2-5d2c-407f-bc9f-ee64583fba44'
        };
        return mapeo[nombreModulo] || null;
    }

    obtenerTipoPermisoId(tipo) {
        const mapeo = {
            'VER': '55358577-113a-4b0c-9b30-a913f444bba3',
            'CREAR': '54897ceb-230c-4eea-88d5-3e61471d521a',
            'EDITAR': 'a7959a7c-8d18-4657-b5a7-24b1b0e952c4',
            'ELIMINAR': 'b1044aad-cd23-4345-bee1-5ce1724ab7ca'
        };
        return mapeo[tipo] || tipo;
    }

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

        // Módulo de Configuración
        if (moduloActual === 'configuracion') {
            const btnAdd = document.querySelector('.btn-add');
            const btnsEditar = document.querySelectorAll('.btn-edit');
            const btnsEliminar = document.querySelectorAll('.btn-del');

            if (btnAdd) {
                btnAdd.style.display = this.tienePermiso('MODULOS', 'CREAR') ? 'flex' : 'none';
            }

            btnsEditar.forEach(btn => {
                btn.style.display = this.tienePermiso('MODULOS', 'EDITAR') ? 'inline-block' : 'none';
            });

            btnsEliminar.forEach(btn => {
                btn.style.display = this.tienePermiso('MODULOS', 'ELIMINAR') ? 'inline-block' : 'none';
            });
        }

        // Módulo de Permisos - Solo lectura para no ADMIN
        if (moduloActual === 'permisos') {
            if (!this.esAdmin) {
                console.log('🔒 Usuario NO ADMIN - Permisos en modo SOLO LECTURA');

                const checkboxes = document.querySelectorAll('.perm-check');
                checkboxes.forEach(cb => {
                    cb.disabled = true;
                    cb.style.cursor = 'not-allowed';
                    cb.style.opacity = '0.6';
                });

                const btnGuardar = document.getElementById('btnGuardar');
                if (btnGuardar) btnGuardar.style.display = 'none';
            } else {
                console.log('👑 Usuario ADMIN - Permisos en modo EDICIÓN');
                const checkboxes = document.querySelectorAll('.perm-check');
                checkboxes.forEach(cb => {
                    cb.disabled = false;
                    cb.style.cursor = 'pointer';
                    cb.style.opacity = '1';
                });
            }
        }

        // Módulos de Prueba
        if (moduloActual === 'prueba1' || moduloActual === 'prueba2') {
            const esPrueba1 = moduloActual === 'prueba1';
            const moduloPermiso = esPrueba1 ? 'PRUEBA1' : 'PRUEBA2';

            const puedeCrear = this.tienePermiso(moduloPermiso, 'CREAR');
            const puedeEditar = this.tienePermiso(moduloPermiso, 'EDITAR');
            const puedeEliminar = this.tienePermiso(moduloPermiso, 'ELIMINAR');

            const btnNuevo = document.querySelector('.btn-nuevo');
            if (btnNuevo) btnNuevo.style.display = puedeCrear ? 'flex' : 'none';

            const btnsEditar = document.querySelectorAll('.btn-edit');
            btnsEditar.forEach(btn => {
                btn.style.display = puedeEditar ? 'inline-block' : 'none';
            });

            const btnsEliminar = document.querySelectorAll('.btn-del');
            btnsEliminar.forEach(btn => {
                btn.style.display = puedeEliminar ? 'inline-block' : 'none';
            });

            window.permisosPrueba = { puedeEditar, puedeEliminar, puedeCrear };
        }
    }
}

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
    } else if (path.includes('/prueba1')) {
        permisosUI.aplicarVisibilidad('prueba1');
    } else if (path.includes('/prueba2')) {
        permisosUI.aplicarVisibilidad('prueba2');
    }
});