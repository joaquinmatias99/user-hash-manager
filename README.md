# User Hash Manager - Backend (Spring Boot + MySQL + BCrypt Hashing)

---

## Parte 1: Contexto y Teoria de Seguridad

### El Problema: Almacenamiento Inseguro
Cuando un usuario se registra en una aplicacion, escribe una contrasena. Si guardamos esa contrasena en texto plano en la base de datos, cualquier persona con acceso a ella (desarrolladores, administradores de sistemas o un atacante que logre infiltrarse) podria ver y robar las credenciales de todos.

Dado que las personas suelen reutilizar la misma contrasena en multiples sitios (como correos, bancos y redes sociales), una filtracion de base de datos compromete toda su identidad digital en internet.

### La Solucion: Hashing y Criptografia
Para solucionar esto, las aplicaciones modernas **nunca guardan las contrasenas reales**. En su lugar, transforman la contrasena en una firma cifrada irreversible (un hash) antes de escribirla en la base de datos. Si un atacante roba la base de datos, solo vera textos incomprensibles.

---

### Conceptos Clave de Criptografia

#### 1. Hash
Es una funcion matematica que convierte un texto (como una contrasena) en una firma unica e irreversible. No se puede revertir un hash para obtener la contrasena original.

#### 2. Sal (Salt)
Es un texto aleatorio unico que se mezcla con la contrasena antes de aplicar el hash. Evita que contrasenas identicas tengan el mismo hash final en la base de datos, protegiendo contra ataques de diccionario.

#### 3. Costo (Iteraciones)
Es la cantidad de iteraciones de procesamiento que hace el algoritmo. BCrypt repite el calculo miles de veces de forma intencional para ralentizar el proceso de computo para los hackers que intentan descifrar contrasenas por fuerza bruta.

---

### Estructura del Hash de BCrypt (Modular Crypt Format)
En la base de datos, la columna `password_hash` guarda un formato unificado como este:
`$2a$10$vI8aWBnW3fID.veFTAO.e.zPHzq4C8C.g24f0n.G12b`

Composicion de la cadena:
* **`$2a$`**: Version del algoritmo BCrypt.
* **`$10$`**: Factor de costo (2^10 = 1024 iteraciones del algoritmo).
* **`vI8aWBnW3fID.veFTAO.e.`**: Sal (salt) de 22 caracteres generada de manera aleatoria. Al estar integrada en la cadena, no se necesita una columna salt independiente.
* **`zPHzq4C8C.g24f0n.G12b`**: Firma final de la contrasena hasheada (31 caracteres).

---

### Calibracion del Costo

* **El rango recomendado:** El calculo de un hash individual debe tardar entre **100ms y 500ms** en el hardware del servidor destino.
* **El balance de seguridad:** Menos de 100ms debilita la seguridad contra ataques de fuerza bruta. Mas de 500ms sobrecarga el CPU si hay multiples inicios de sesion simultaneos.
* **Evolucion del hardware:** A medida que los procesadores y las tarjetas graficas avanzan, el costo debe subirse (ej: de 10 a 11 o 12) para mantener el tiempo de calculo constante frente a atacantes con mejor hardware.

---

## Parte 2: El Proyecto y su Arquitectura

Este proyecto implementa **BCrypt** aplicando un desacoplamiento riguroso por capas independientes mediante interfaces.

### Organizacion del Codigo (Capas)
* **`model/`**: Contiene `UserEntity.java` (mapeo a la tabla MySQL) y `UserDTO.java` (transferencia de datos en la API).
* **`repository/`**: Dividido en tres partes para evitar dependencias tecnologicas directas:
  - `UserJpaRepository.java` (Spring Data JPA).
  - `UserRepository.java` (Interfaz de persistencia del dominio).
  - `UserRepositoryImpl.java` (Implementacion que encapsula a JPA).
* **`service/`**: Contiene la logica de hasheo (`PasswordHasher` y `PasswordHasherImpl`) y el negocio de usuarios (`UserService` y `UserServiceImpl`).
* **`controller/`**: Controlador REST (`UserController.java`) que expone los endpoints publicos.

---

### Columna Didactica: `raw_password_debug_only`
Para facilitar el estudio del hashing en la base de datos, la tabla de MySQL contiene la columna `raw_password_debug_only`.
* Al registrar o actualizar un usuario, su contrasena original en texto plano se guarda en esta columna.
* **Nota:** Esto es solo para comparar de forma visual la contrasena plana con la firma de BCrypt en este entorno local de pruebas. En produccion esta columna no debe existir bajo ningun concepto.

---

### Actualizacion Automatica de Hash en el Login (Re-hasheo)
Cuando subes el factor de costo en el servidor, las contrasenas viejas quedan con menor seguridad. Para actualizarlas de forma transparente sin obligar al usuario a cambiar su contraseña, implementamos esta tecnica en el Login:
1. El usuario inicia sesion con credenciales validas.
2. El sistema comprueba si el costo del hash guardado en MySQL es menor al costo actual del servidor.
3. Si es menor, genera un nuevo hash con el costo actual y actualiza el registro de forma silenciosa.

Esta funcionalidad esta controlada mediante la constante:
`private static final boolean UPGRADE_HASH_ON_LOGIN = true;`

---

## Parte 3: Guia de Uso y Ejecucion

### Requisitos
* Docker / Docker Desktop
* Java JDK 11 (o superior)
* Postman (para pruebas de endpoints)

### 1. Levantar la Base de Datos
Iniciar el contenedor de MySQL en el puerto 3307:
```bash
docker compose up -d
```

Datos de conexion:
* Host: localhost:3307
* Database: user_hash_db
* User/Password: admin / admin

### 2. Levantar el Backend
Ejecutar la aplicacion usando el Gradle Wrapper local:

En PowerShell:
```powershell
.\gradlew bootRun
```

En CMD:
```cmd
gradlew.bat bootRun
```

La API correra en `http://localhost:8080`.

---

### Endpoints de la API (/api/users)

| Metodo | Endpoint | Descripcion | Body (JSON) |
| :--- | :--- | :--- | :--- |
| POST | /register | Registro de usuario | {"username": "...", "password": "...", "email": "..."} |
| POST | /login | Autenticacion (con re-hasheo automatico de costo si aplica) | {"username": "...", "password": "..."} |
| GET | /{username} | Detalle de perfil | N/A |
| GET | / | Listado completo de usuarios | N/A |
| PUT | /{username} | Actualizacion de datos (email y/o contrasena) | {"email": "...", "password": "..."} |
| DELETE | /{username} | Eliminar registro | N/A |

Para probar la API, importa el archivo `user-hash-manager.postman_collection.json` ubicado en la raiz de este proyecto en tu cliente Postman.
