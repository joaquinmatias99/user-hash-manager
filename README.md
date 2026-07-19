# User Hash Manager - Backend (Spring Boot + MySQL + BCrypt Hashing)

---

## Parte 1: Contexto y Teoría de Seguridad

### El Problema: Almacenamiento Inseguro
Cuando un usuario se registra en una aplicación, escribe una contraseña. Si guardamos esa contraseña en texto plano en la base de datos, cualquier persona con acceso a ella (desarrolladores, administradores de sistemas o un atacante que logre infiltrarse) podría ver y robar las credenciales de todos.

Dado que las personas suelen reutilizar la misma contraseña en múltiples sitios (como correos, bancos y redes sociales), una filtración de base de datos compromete toda su identidad digital en internet.

### La Solución: Hashing y Criptografía
Para solucionar esto, las aplicaciones modernas **nunca guardan las contraseñas reales**. En su lugar, transforman la contraseña en una firma cifrada irreversible (un hash) antes de escribirla en la base de datos. Si un atacante roba la base de datos, solo verá textos incomprensibles.

---

### Conceptos Clave de Criptografía

#### 1. Hash
Es una función matemática que convierte un texto (como una contraseña) en una firma única e irreversible. No se puede revertir un hash para obtener la contraseña original.

#### 2. Sal (Salt)
Es un texto aleatorio único que se mezcla con la contraseña antes de aplicar el hash. Evita que contraseñas idénticas tengan el mismo hash final en la base de datos, protegiendo contra ataques de diccionario.

#### 3. Costo (Iteraciones)
Es la cantidad de iteraciones de procesamiento que hace el algoritmo. BCrypt repite el cálculo miles de veces de forma intencional para ralentizar el proceso de cómputo para los hackers que intentan descifrar contraseñas por fuerza bruta.

---

### Estructura del Hash de BCrypt (Modular Crypt Format)
En la base de datos, la columna `password_hash` guarda un formato unificado como este:
`$2a$10$vI8aWBnW3fID.veFTAO.e.zPHzq4C8C.g24f0n.G12b`

Composición de la cadena:
* **`$2a$`**: Versión del algoritmo BCrypt.
* **`$10$`**: Factor de costo (2^10 = 1024 iteraciones del algoritmo).
* **`vI8aWBnW3fID.veFTAO.e.`**: Sal (salt) de 22 caracteres generada de manera aleatoria. Al estar integrada en la cadena, no se necesita una columna salt independiente.
* **`zPHzq4C8C.g24f0n.G12b`**: Firma final de la contraseña hasheada (31 caracteres).

---

### Calibración del Costo

* **El rango recomendado:** El cálculo de un hash individual debe tardar entre **100ms y 500ms** en el hardware del servidor destino.
* **El balance de seguridad:** Menos de 100ms debilita la seguridad contra ataques de fuerza bruta. Más de 500ms sobrecarga el CPU si hay múltiples inicios de sesión simultáneos.
* **Evolución del hardware:** A medida que los procesadores y las tarjetas gráficas avanzan, el costo debe subirse (ej: de 10 a 11 o 12) para mantener el tiempo de cálculo constante frente a atacantes con mejor hardware.

---

## Parte 2: El Proyecto y su Arquitectura

Este proyecto implementa **BCrypt** aplicando un desacoplamiento riguroso por capas independientes mediante interfaces.

### Organización del Código (Capas)
* **`model/`**: Contiene `UserEntity.java` (mapeo a la tabla MySQL) y `UserDTO.java` (transferencia de datos en la API).
* **`repository/`**: Dividido en tres partes para evitar dependencias tecnológicas directas:
  - `UserJpaRepository.java` (Spring Data JPA).
  - `UserRepository.java` (Interfaz de persistencia del dominio).
  - `UserRepositoryImpl.java` (Implementación que encapsula a JPA).
* **`service/`**: Contiene la lógica de hasheo (`PasswordHasher` y `PasswordHasherImpl`) y el negocio de usuarios (`UserService` y `UserServiceImpl`).
* **`controller/`**: Controlador REST (`UserController.java`) que expone los endpoints públicos.

---

### Columna Didáctica: `raw_password_debug_only`
Para facilitar el estudio del hashing en la base de datos, la tabla de MySQL contiene la columna `raw_password_debug_only`.
* Al registrar o actualizar un usuario, su contraseña original en texto plano se guarda en esta columna.
* **Nota:** Esto es solo para comparar de forma visual la contraseña plana con la firma de BCrypt en este entorno local de pruebas. En producción esta columna no debe existir bajo ningún concepto.

---

### Actualización Automática de Hash en el Login (Re-hasheo)
Cuando subes el factor de costo en el servidor, las contraseñas viejas quedan con menor seguridad. Para actualizarlas de forma transparente sin obligar al usuario a cambiar su contraseña, implementamos esta técnica en el Login:
1. El usuario inicia sesión con credenciales válidas.
2. El sistema comprueba si el costo del hash guardado en MySQL es menor al costo actual del servidor.
3. Si es menor, genera un nuevo hash con el costo actual y actualiza el registro de forma silenciosa.

Esta funcionalidad está controlada mediante la constante:
`private static final boolean UPGRADE_HASH_ON_LOGIN = true;`

---

## Parte 3: Guía de Uso y Ejecución

### Requisitos
* Docker / Docker Desktop
* Java JDK 11 (o superior)
* Postman (para pruebas de endpoints)

### 1. Levantar la Base de Datos
Iniciar el contenedor de MySQL en el puerto 3307:
```bash
docker compose up -d
```

Datos de conexión:
* Host: localhost:3307
* Database: user_hash_db
* User/Password: admin / admin

### 2. Levantar el Backend
Ejecutar la aplicación usando el Gradle Wrapper local:

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

### Endpoints de la API (/api/users)

| Método | Endpoint | Descripción | Body (JSON) |
| :--- | :--- | :--- | :--- |
| POST | /register | Registro de usuario | {"username": "...", "password": "...", "email": "..."} |
| POST | /login | Autenticación (con re-hasheo automático de costo si aplica) | {"username": "...", "password": "..."} |
| GET | /{username} | Detalle de perfil | N/A |
| GET | / | Listado completo de usuarios | N/A |
| PUT | /{username} | Actualización de datos (email y/o contraseña) | {"email": "...", "password": "..."} |
| DELETE | /{username} | Eliminar registro | N/A |

Para probar la API, importa el archivo `user-hash-manager.postman_collection.json` ubicado en la raíz de este proyecto en tu cliente Postman.
