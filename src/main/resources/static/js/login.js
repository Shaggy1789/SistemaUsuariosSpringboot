document.getElementById('loginForm').addEventListener('submit', function(e) {
        e.preventDefault();
        
        console.log('1. Iniciando proceso de login...');
        
        // Ejecutar reCAPTCHA
        grecaptcha.ready(function() {
            console.log('2. reCAPTCHA ready');
            
            grecaptcha.execute('6LdJmHAsAAAAAIxiOLPzSMjlUdd_S_PLSDqg1hbq', {action: 'login'})
            .then(function(token) {
                console.log('3. Token generado, longitud:', token.length);
                
                document.getElementById('recaptchaToken').value = token;
                
                // Crear FormData
                const formData = new FormData(document.getElementById('loginForm'));
                
                // Mostrar los datos que se envían
                console.log('4. Datos del formulario:');
                for (let pair of formData.entries()) {
                    console.log(pair[0] + ': ' + pair[1].substring(0, 50) + '...');
                }
                
                // Enviar con fetch
                fetch('/api/login', {
                    method: 'POST',
                    body: formData
                })
                .then(response => {
                    console.log('5. Respuesta recibida, status:', response.status);
                    console.log('5. Headers:', response.headers.get('content-type'));
                    
                    if (!response.ok) {
                        throw new Error('HTTP error! status: ' + response.status);
                    }
                    return response.json();
                })
                .then(data => {
                    console.log('6. Datos JSON:', data);
                    
                    if (data.success) {
                        console.log('7. Login exitoso, redirigiendo a:', data.redirect);
                        window.location.href = data.redirect || '/dashboard';
                    } else {
                        console.log('7. Login falló:', data.message);
                        alert(data.message);
                    }
                })
                .catch(error => {
                    console.error('8. Error en fetch:', error);
                    alert('Error en la comunicación con el servidor. Mira la consola (F12) para más detalles.');
                });
            })
            .catch(function(error) {
                console.error('Error en reCAPTCHA:', error);
                alert('Error con reCAPTCHA');
            });
        });
    });