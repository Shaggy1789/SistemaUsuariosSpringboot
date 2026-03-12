    let usuarioIdEliminar = null;
    let modalInstance = null;
    let eliminarModalInstance = null;
    let currentPage = 1;
    let itemsPerPage = 10;
    let allUsuarios = [];

    document.addEventListener('DOMContentLoaded', function() {
        modalInstance = new bootstrap.Modal(document.getElementById('usuarioModal'));
        eliminarModalInstance = new bootstrap.Modal(document.getElementById('eliminarModal'));
        cargarRoles();
        cargarUsuarios();

        document.getElementById('filtrarBtn').addEventListener('click', cargarUsuarios);
        document.getElementById('buscarInput').addEventListener('keypress', function(e) {
            if (e.key === 'Enter') cargarUsuarios();
        });
    });

    function mostrarAlerta(tipo, titulo, mensaje) {
        Swal.fire({
            icon: tipo,
            title: titulo,
            text: mensaje,
            confirmButtonColor: '#4f46e5',
            confirmButtonText: 'Aceptar',
            timer: tipo === 'success' ? 2000 : undefined,
            timerProgressBar: tipo === 'success'
        });
    }

    function soloNumeros(e) {
        const input = e.target;
        input.value = input.value.replace(/[^0-9]/g, '');
        if (input.value.length > 10) {
            input.value = input.value.slice(0, 10);
        }
    }

    function validarTelefono(input) {
        const telefono = input.value;
        const errorSpan = document.getElementById('telefonoError');

        if (telefono.length === 0) {
            errorSpan.textContent = 'El teléfono es requerido';
            input.classList.add('is-invalid');
            input.classList.remove('is-valid');
            return false;
        } else if (telefono.length < 10) {
            errorSpan.textContent = 'El teléfono debe tener 10 dígitos';
            input.classList.add('is-invalid');
            input.classList.remove('is-valid');
            return false;
        } else if (!/^\d+$/.test(telefono)) {
            errorSpan.textContent = 'Solo se permiten números';
            input.classList.add('is-invalid');
            input.classList.remove('is-valid');
            return false;
        } else {
            errorSpan.textContent = '';
            input.classList.remove('is-invalid');
            input.classList.add('is-valid');
            return true;
        }
    }

    function validarEmail(input) {
        const email = input.value;
        const errorSpan = document.getElementById('emailError');
        const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;

        if (!email) {
            errorSpan.textContent = 'El email es requerido';
            input.classList.add('is-invalid');
            input.classList.remove('is-valid');
            return false;
        } else if (!emailRegex.test(email)) {
            errorSpan.textContent = 'Ingresa un email válido (ejemplo@dominio.com)';
            input.classList.add('is-invalid');
            input.classList.remove('is-valid');
            return false;
        } else {
            errorSpan.textContent = '';
            input.classList.remove('is-invalid');
            input.classList.add('is-valid');
            return true;
        }
    }

    function validarTexto(input, campo) {
        const valor = input.value;
        const errorSpan = document.getElementById(input.id + 'Error');

        if (!valor) {
            errorSpan.textContent = `El ${campo} es requerido`;
            input.classList.add('is-invalid');
            input.classList.remove('is-valid');
            return false;
        } else if (valor.length < 3) {
            errorSpan.textContent = `El ${campo} debe tener al menos 3 caracteres`;
            input.classList.add('is-invalid');
            input.classList.remove('is-valid');
            return false;
        } else if (!/^[a-zA-ZáéíóúÁÉÍÓÚñÑ\s]+$/.test(valor)) {
            errorSpan.textContent = 'Solo se permiten letras';
            input.classList.add('is-invalid');
            input.classList.remove('is-valid');
            return false;
        } else {
            errorSpan.textContent = '';
            input.classList.remove('is-invalid');
            input.classList.add('is-valid');
            return true;
        }
    }

    function validarPassword(passwordId, confirmId) {
        const passwordInput = document.getElementById(passwordId);
        const confirmInput = document.getElementById(confirmId);
        const errorSpan = document.getElementById('passwordError');
        const confirmErrorSpan = document.getElementById('confirmPasswordError');

        if (!passwordInput.value) {
            errorSpan.textContent = 'La contraseña es requerida';
            passwordInput.classList.add('is-invalid');
            return false;
        } else if (passwordInput.value.length < 4) {
            errorSpan.textContent = 'La contraseña debe tener al menos 4 caracteres';
            passwordInput.classList.add('is-invalid');
            return false;
        } else if (passwordInput.value !== confirmInput.value) {
            errorSpan.textContent = 'Las contraseñas no coinciden';
            confirmErrorSpan.textContent = 'Las contraseñas no coinciden';
            passwordInput.classList.add('is-invalid');
            confirmInput.classList.add('is-invalid');
            return false;
        } else {
            errorSpan.textContent = '';
            confirmErrorSpan.textContent = '';
            passwordInput.classList.remove('is-invalid');
            confirmInput.classList.remove('is-invalid');
            passwordInput.classList.add('is-valid');
            confirmInput.classList.add('is-valid');
            return true;
        }
    }

    function cargarRoles() {
        fetch('/api/roles')
            .then(response => {
                if(!response.ok){
                    throw new Error(response.statusText);
                }
                return response.json();
            })
            .then(data => {
                console.log('Roles recibidos:', data);
                const rolSelect = document.getElementById('rolSelect');
                const rolModalSelect = document.getElementById('rol');
                rolSelect.innerHTML = '<option value="">Todos los roles</option>';
                rolModalSelect.innerHTML = '<option value="">Seleccionar rol</option>';
                if (Array.isArray(data)) {
                    data.forEach(rol => {
                        rolSelect.innerHTML += `<option value="${rol.id}">${rol.nombre}</option>`;
                        rolModalSelect.innerHTML += `<option value="${rol.id}">${rol.nombre}</option>`;
                    });
                } else if (data.data && Array.isArray(data.data)) {
                    data.data.forEach(rol => {
                        rolSelect.innerHTML += `<option value="${rol.id}">${rol.nombre}</option>`;
                        rolModalSelect.innerHTML += `<option value="${rol.id}">${rol.nombre}</option>`;
                    });
                } else {
                    console.error('Estructura de roles no reconocida:', data);
                    rolSelect.innerHTML = `
                        <option value="">Todos los roles</option>
                        <option value="1">Administrador</option>
                        <option value="2">Usuario</option>
                    `;
                    rolModalSelect.innerHTML = `
                        <option value="">Seleccionar rol</option>
                        <option value="1">Administrador</option>
                        <option value="2">Usuario</option>
                    `;
                }
            })
            .catch(error => {
                console.error('Error al cargar roles:', error);
                document.getElementById('rolSelect').innerHTML = `
                    <option value="">Todos los roles</option>
                    <option value="1">Administrador</option>
                    <option value="2">Usuario</option>
                `;
                document.getElementById('rol').innerHTML = `
                    <option value="">Seleccionar rol</option>
                    <option value="1">Administrador</option>
                    <option value="2">Usuario</option>
                `;
            });
    }

    function cargarUsuarios() {
        const buscar = document.getElementById('buscarInput').value;
        const rol = document.getElementById('rolSelect').value;
        let url = '/api/usuarios/buscar?';
        if (buscar) url += 'query=' + encodeURIComponent(buscar) + '&';
        if (rol) url += 'rolId=' + rol + '&';
        fetch(url)
            .then(response => {
                if(!response.ok){
                    return response.text().then(text => {
                        throw new Error(`Status: ${response.status}, Respuesta: ${text.substring(0,200)}`);
                    });
                }
                return response.json();
            })
            .then(data => {
                if (data && data.data) {
                    allUsuarios = data.data;
                    document.getElementById('totalCount').textContent = data.Total || data.data.length;
                    currentPage = 1;
                    mostrarUsuariosPaginados();
                } else {
                    allUsuarios = [];
                    mostrarUsuarios([]);
                }
            })
            .catch(error => {
                console.error('Error:', error);
                fetch('/api/usuarios')
                    .then(response => response.json())
                    .then(usuarios => {
                        allUsuarios = usuarios;
                        document.getElementById('totalCount').textContent = usuarios.length;
                        currentPage = 1;
                        mostrarUsuariosPaginados();
                    })
                    .catch(err => {
                        document.getElementById('tablaUsuarios').innerHTML = `
                            <tr>
                                <td colspan="7" class="text-center text-danger py-4">
                                    <i class="fas fa-exclamation-circle fa-3x mb-3"></i>
                                    <h5>Error al cargar usuarios</h5>
                                    <p class="text-muted">${error.message}</p>
                                </td>
                            </tr>
                        `;
                    });
            });
    }

    function mostrarUsuariosPaginados() {
        const start = (currentPage - 1) * itemsPerPage;
        const end = start + itemsPerPage;
        const usuariosPagina = allUsuarios.slice(start, end);
        mostrarUsuarios(usuariosPagina);
        actualizarPaginacion();
    }

    function mostrarUsuarios(usuarios) {
        const tbody = document.getElementById('tablaUsuarios');
        if (!usuarios || usuarios.length === 0) {
            tbody.innerHTML = `
                <tr>
                    <td colspan="7" class="text-center py-5">
                        <i class="fas fa-users-slash fa-3x mb-3" style="color: #94a3b8;"></i>
                        <h5 style="color: #64748b;">No se encontraron usuarios</h5>
                    </td>
                </tr>
            `;
            return;
        }
        let html = '';
        usuarios.forEach(u => {
            const rolClass = u.role && u.role.id === 1 ? 'badge-admin' : '';
            const rolNombre = u.role ? u.role.nombre : 'Usuario';
            html += `
                <tr>
                    <td><i class="fas fa-user-circle me-2" style="color: #4f46e5;"></i>${u.nombreusuario || ''}</td>
                    <td>${u.apellidopaterno || ''} ${u.apellidomaterno || ''}</td>
                    <td>${u.email || ''}</td>
                    <td>${u.telefono || ''}</td>
                    <td><span class="role-badge ${rolClass}">${rolNombre}</span></td>
                    <td>
                        <button class="btn btn-sm btn-outline-primary btn-accion me-1" 
                                onclick="abrirModalEditar(${u.idusuario})" title="Editar">
                            <i class="fas fa-edit"></i>
                        </button>
                        <button class="btn btn-sm btn-outline-danger btn-accion" 
                                onclick="abrirModalEliminar(${u.idusuario}, '${u.nombreusuario}')" title="Eliminar">
                            <i class="fas fa-trash"></i>
                        </button>
                    </td>
                </tr>
            `;
        });
        tbody.innerHTML = html;
    }

    function actualizarPaginacion() {
        const totalPages = Math.ceil(allUsuarios.length / itemsPerPage);
        let paginationContainer = document.getElementById('paginationContainer');
        if (!paginationContainer) {
            paginationContainer = document.createElement('div');
            paginationContainer.id = 'paginationContainer';
            paginationContainer.className = 'd-flex justify-content-center mt-4';
            const tableContainer = document.querySelector('.table-responsive');
            tableContainer.parentNode.insertBefore(paginationContainer, tableContainer.nextSibling);
        }
        const pagesToShow = Math.max(1, totalPages);
        let paginationHtml = '<nav><ul class="pagination">';
        paginationHtml += `<li class="page-item ${currentPage === 1 ? 'disabled' : ''}">
            <a class="page-link" href="#" onclick="cambiarPagina(${currentPage - 1}); return false;">Anterior</a>
        </li>`;
        for (let i = 1; i <= pagesToShow; i++) {
            if (i === currentPage) {
                paginationHtml += `<li class="page-item active"><span class="page-link">${i}</span></li>`;
            } else {
                paginationHtml += `<li class="page-item"><a class="page-link" href="#" onclick="cambiarPagina(${i}); return false;">${i}</a></li>`;
            }
        }
        paginationHtml += `<li class="page-item ${currentPage === pagesToShow ? 'disabled' : ''}">
            <a class="page-link" href="#" onclick="cambiarPagina(${currentPage + 1}); return false;">Siguiente</a>
        </li>`;
        paginationHtml += '</ul></nav>';
        paginationHtml += `<div class="ms-3">
            <select class="form-select form-select-sm" onchange="cambiarItemsPorPagina(this.value)" style="width: auto;">
                <option value="5" ${itemsPerPage === 5 ? 'selected' : ''}>5 por página</option>
                <option value="10" ${itemsPerPage === 10 ? 'selected' : ''}>10 por página</option>
                <option value="20" ${itemsPerPage === 20 ? 'selected' : ''}>20 por página</option>
                <option value="50" ${itemsPerPage === 50 ? 'selected' : ''}>50 por página</option>
            </select>
        </div>`;
        paginationContainer.innerHTML = paginationHtml;
    }

    function cambiarPagina(nuevaPagina) {
        const totalPages = Math.ceil(allUsuarios.length / itemsPerPage);
        if (nuevaPagina >= 1 && nuevaPagina <= totalPages) {
            currentPage = nuevaPagina;
            mostrarUsuariosPaginados();
        }
    }

    function cambiarItemsPorPagina(nuevosItems) {
        itemsPerPage = parseInt(nuevosItems);
        currentPage = 1;
        mostrarUsuariosPaginados();
    }

    function abrirModalNuevo() {
        document.getElementById('modalTitleText').textContent = 'Nuevo Usuario';
        document.getElementById('usuarioForm').reset();
        document.getElementById('usuarioId').value = '';
        document.getElementById('password').required = true;
        document.getElementById('confirmPassword').required = true;
        document.getElementById('passwordFields').style.display = 'flex';
        document.querySelectorAll('.is-invalid').forEach(el => el.classList.remove('is-invalid'));
        document.querySelectorAll('.is-valid').forEach(el => el.classList.remove('is-valid'));
        document.querySelectorAll('.invalid-feedback').forEach(el => el.textContent = '');
        modalInstance.show();
    }

    function abrirModalEditar(id) {
        document.getElementById('modalTitleText').textContent = 'Editar Usuario';
        document.getElementById('password').required = false;
        document.getElementById('confirmPassword').required = false;
        document.getElementById('passwordFields').style.display = 'none';
        document.querySelectorAll('.is-invalid').forEach(el => el.classList.remove('is-invalid'));
        document.querySelectorAll('.is-valid').forEach(el => el.classList.remove('is-valid'));
        document.querySelectorAll('.invalid-feedback').forEach(el => el.textContent = '');
        fetch(`/api/usuario/${id}`)
            .then(response => response.json())
            .then(usuario => {
                document.getElementById('usuarioId').value = usuario.idusuario;
                document.getElementById('nombreusuario').value = usuario.nombreusuario;
                document.getElementById('apellidopaterno').value = usuario.apellidopaterno;
                document.getElementById('apellidomaterno').value = usuario.apellidomaterno;
                document.getElementById('email').value = usuario.email;
                document.getElementById('telefono').value = usuario.telefono;
                document.getElementById('rol').value = usuario.role ? usuario.role.id : '';
                modalInstance.show();
            })
            .catch(error => {
                console.error('Error:', error);
                mostrarAlerta('error', 'Error', 'Error al cargar el usuario');
            });
    }

    function guardarUsuario() {
        const id = document.getElementById('usuarioId').value;
        const esNuevo = !id;
        document.querySelectorAll('.invalid-feedback').forEach(el => el.textContent = '');
        document.querySelectorAll('.is-invalid').forEach(el => el.classList.remove('is-invalid'));
        const nombreValido = validarTexto(document.getElementById('nombreusuario'), 'usuario');
        const apellidoPaternoValido = validarTexto(document.getElementById('apellidopaterno'), 'apellido paterno');
        const apellidoMaternoValido = validarTexto(document.getElementById('apellidomaterno'), 'apellido materno');
        const emailValido = validarEmail(document.getElementById('email'));
        const telefonoValido = validarTelefono(document.getElementById('telefono'));
        const rolSelect = document.getElementById('rol');
        const rolError = document.getElementById('rolError');
        if (!rolSelect.value) {
            rolError.textContent = 'Selecciona un rol';
            rolSelect.classList.add('is-invalid');
            return;
        } else {
            rolError.textContent = '';
            rolSelect.classList.remove('is-invalid');
            rolSelect.classList.add('is-valid');
        }
        let passwordValido = true;
        if (esNuevo) {
            passwordValido = validarPassword('password', 'confirmPassword');
        }
        if (!nombreValido || !apellidoPaternoValido || !apellidoMaternoValido || 
            !emailValido || !telefonoValido || !passwordValido) {
            return;
        }
        const usuarioData = {
            nombreusuario: document.getElementById('nombreusuario').value,
            apellidopaterno: document.getElementById('apellidopaterno').value,
            apellidomaterno: document.getElementById('apellidomaterno').value,
            email: document.getElementById('email').value,
            telefono: document.getElementById('telefono').value ? parseInt(document.getElementById('telefono').value) : 0,
            role: rolSelect.value ? { id: parseInt(rolSelect.value) } : null
        };
        if (!esNuevo && id) {
            usuarioData.idusuario = parseInt(id);
        }
        if (esNuevo) {
            usuarioData.password = document.getElementById('password').value;
        }
        const url = esNuevo ? '/api/usuarios' : `/api/usuarios/${id}`;
        const method = esNuevo ? 'POST' : 'PUT';
        fetch(url, {
            method: method,
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(usuarioData)
        })
        .then(response => response.json())
        .then(data => {
            if (data.success) {
                modalInstance.hide();
                cargarUsuarios();
                mostrarAlerta('success', '¡Éxito!', esNuevo ? 'Usuario creado correctamente' : 'Usuario actualizado correctamente');
            } else {
                mostrarAlerta('error', 'Error', data.message || 'Error al guardar el usuario');
            }
        })
        .catch(error => {
            console.error('Error:', error);
            mostrarAlerta('error', 'Error', 'Error al guardar el usuario');
        });
    }

    function abrirModalEliminar(id, nombre) {
        usuarioIdEliminar = id;
        document.getElementById('usuarioAEliminar').textContent = nombre;
        eliminarModalInstance.show();
    }

    function confirmarEliminar() {
        if (!usuarioIdEliminar) return;
        fetch(`/api/usuarios/${usuarioIdEliminar}`, {
            method: 'DELETE'
        })
        .then(response => response.json())
        .then(data => {
            eliminarModalInstance.hide();
            if (data.success) {
                cargarUsuarios();
                mostrarAlerta('success', '¡Éxito!', 'Usuario eliminado correctamente');
            } else {
                mostrarAlerta('error', 'Error', 'Error al eliminar usuario');
            }
        })
        .catch(error => {
            console.error('Error:', error);
            mostrarAlerta('error', 'Error', 'Error al eliminar usuario');
        });
    }

    function togglePassword(inputId) {
        const input = document.getElementById(inputId);
        const icon = input.parentElement.querySelector('.toggle-password i');
        if (input.type === 'password') {
            input.type = 'text';
            icon.classList.remove('fa-eye');
            icon.classList.add('fa-eye-slash');
        } else {
            input.type = 'password';
            icon.classList.remove('fa-eye-slash');
            icon.classList.add('fa-eye');
        }
    }