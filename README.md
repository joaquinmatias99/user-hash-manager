# User Hash Manager - Backend (Spring Boot + MySQL + BCrypt Hashing)

### El Problema: Almacenamiento Inseguro
Cuando un usuario se registra en una aplicación, escribe una contraseña. Si guardamos esa contraseña en texto plano en la base de datos, cualquier persona con acceso a ella (desarrolladores, administradores de sistemas o un hacker que logre infiltrarse) podría ver y robar las credenciales de todos.

Dado que las personas suelen reutilizar la misma contraseña en múltiples sitios (como correos, bancos y redes sociales), una filtración de base de datos compromete toda su identidad digital en internet.

### La Solución: Hashing y Criptografía
Para solucionar esto, las aplicaciones modernas **nunca guardan las contraseñas reales**. En su lugar, transforman la contraseña en una firma cifrada irreversible (un hash) antes de escribirla en la base de datos. 

Si un atacante roba la base de datos, solo verá textos incomprensibles. No podrá conocer las contraseñas reales ni utilizarlas para ingresar a otros servicios. Este proyecto implementa **BCrypt**, el estándar de la industria que combina hashing, sal y costo adaptativo para resolver este problema.

---

## Conceptos Clave

### 1. Hash
Es una función matemática que convierte un texto (como una contraseña) en una firma única e irreversible. No se puede revertir un hash para obtener la contraseña original.

### 2. Sal (Salt)
Es un texto aleatorio único que se mezcla con la contraseña antes de aplicar el hash. Evita que contraseñas idénticas tengan el mismo hash final en la base de datos, protegiendo contra ataques de diccionario.

### 3. Costo
Es la cantidad de iteraciones de procesamiento que hace el algoritmo. BCrypt repite el cálculo miles de veces de forma intencional. Esto ralentiza el proceso de cómputo para los hackers que intentan descifrar contraseñas por fuerza bruta.

---

## Actualización Automática de Hash (Re-hasheo)

Cuando subes el factor de costo en el servidor (ej: de 10 a 12), las contraseñas viejas quedan con menor seguridad. 

Para actualizarlas de forma transparente, aprovechamos el momento del login:
1. El usuario inicia sesión con sus credenciales válidas.
2. El sistema comprueba si el costo del hash guardado en MySQL es menor al costo actual del servidor.
3. Si es menor, genera un nuevo hash con el costo actual y actualiza el registro de forma silenciosa.

Esta funcionalidad está controlada mediante la constante:
`private static final boolean UPGRADE_HASH_ON_LOGIN = true;`

---

## Requisitos

* Docker / Docker Desktop
* Java JDK 11 (o superior)
* Postman

---

## Ejecución

### 1. Base de datos
Iniciar el contenedor de MySQL en el puerto 3307:
```bash
docker compose up -d
```

Conexión MySQL:
* Host: localhost:3307
* Database: user_hash_db
* User/Password: admin / admin

### 2. Servidor backend
Ejecutar la aplicación:

En PowerShell:
```powershell
.\gradlew bootRun
```

En CMD:
```cmd
gradlew.bat bootRun
```

La API correrá en `http://localhost:8080`.

---

## Endpoints (/api/users)

| Método | Endpoint | Descripción | Body (JSON) |
| :--- | :--- | :--- | :--- |
| POST | /register | Registro de usuario | {"username": "...", "password": "...", "email": "..."} |
| POST | /login | Autenticación (con actualización automática de costo) | {"username": "...", "password": "..."} |
| GET | /{username} | Detalle de perfil | N/A |
| GET | / | Listado completo | N/A |
| PUT | /{username} | Actualización de datos | {"email": "...", "password": "..."} |
| DELETE | /{username} | Eliminar registro | N/A |

---

## Estructura del Hash de BCrypt

En la base de datos, la columna `password_hash` guarda un formato como este:
`$2a$10$vI8aWBnW3fID.veFTAO.e.zPHzq4C8C.g24f0n.G12b`

Composición:
* `$2a$`: Versión del algoritmo.
* `$10$`: Costo (2^10 = 1024 iteraciones).
* `vI8aWBnW3fID.veFTAO.e.`: Sal (salt) de 22 caracteres generada de manera aleatoria.
* `zPHzq4C8C.g24f0n.G12b`: Hash de la contraseña (31 caracteres).

---

## Columna raw_password_debug_only

La columna `raw_password_debug_only` almacena la contraseña en texto plano.
* **Nota:** Esto es solo para comparar de forma visual la contraseña plana con la cadena de BCrypt en este entorno local de pruebas. En producción esta columna no debe existir.

---

## Calibración del Costo

El factor de costo determina las vueltas de procesamiento del hash.
* Se debe medir el rendimiento en el servidor destino. El cálculo de un hash individual debe tardar entre **100ms y 500ms**.
* Menos de 100ms debilita la seguridad contra ataques de fuerza bruta. Más de 500ms sobrecarga el CPU si hay múltiples logins simultáneos.
* A medida que los procesadores y las tarjetas gráficas avanzan, el costo debe subirse para mantener el tiempo de cálculo constante frente a atacantes con mejor hardware.
