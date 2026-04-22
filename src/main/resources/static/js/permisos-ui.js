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
        'USUARIOS': 'd89ce43d-8b6d-4022-84bd-391e1e45dc5a',  // usuarios
        'PERFIL': 'b8709c84-731d-4758-8a4f-47a226350c37',    // Perfil
        'PERMISOS': 'cc07f175-c76b-434c-88a5-5c38b4e885d1',   // Permisos Perfil
        'MODULOS': '3f5cb7a3-daa4-4598-b03b-a18bd5102044',    // Modulos
        'PRUEBA1': '34ef9528-0736-4e57-b94a-ee4c5e70f180',    // prueba1
        'PRUEBA2': '711663ba-b4b2-49b2-8f38-3024907183bf',    // prueba2
        'PRUEBA1.1': '4ee84804-d66b-4300-b594-6a9238aa0249',  // prueba1.1
        'PRUEBA1.2': '2ebb4cf2-0108-462c-8e05-3604d01c86eb',  // prueba1.2
        'PRUEBA1.3': '1fe12fe9-e04c-4277-8f30-ecc9ce16dc83',  // prueba1.3
        'PRUEBA1.4': '4aac39d9-417a-4280-bdbb-72bc010e63a8',  // prueba1.4
        'PRUEBA2.1': '972d3e7a-e8d5-4770-902b-3f8b29370ce4',  // prueba2.1
        'PRUEBA2.2': 'd33c7bb2-5d2c-407f-bc9f-ee64583fba44',  // prueba2.2
        'PRUEBA2.3': '41c0c119-2468-443c-bf76-02ec258c88d3',  // prueba2.3
        'PRUEBA2.4': '87303e3a-cea9-4cd4-a5dc-995db81107ac'   // prueba2.4
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