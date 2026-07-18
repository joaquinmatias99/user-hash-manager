# User Hash Manager - Backend (Spring Boot + MySQL + BCrypt Hashing)

Proyecto didactico en Spring Boot y MySQL para analizar el almacenamiento de contrasenas usando el algoritmo BCrypt.

---

## Requisitos

* Docker / Docker Desktop
* Java JDK 11 (o superior)
* Postman (para probar la API)

---

## Ejecucion

### 1. Base de datos
Iniciar el contenedor de MySQL en el puerto 3307:
```bash
docker compose up -d
```

Conexion MySQL:
* Host: localhost:3307
* Database: user_hash_db
* User/Password: admin / admin

### 2. Servidor backend
Ejecutar la aplicacion:

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

## Endpoints (/api/users)

| Metodo | Endpoint | Descripcion | Body (JSON) |
| :--- | :--- | :--- | :--- |
| POST | /register | Registro de usuario | {"username": "...", "password": "...", "email": "..."} |
| POST | /login | Autenticacion | {"username": "...", "password": "..."} |
| GET | /{username} | Detalle de perfil | N/A |
| GET | / | Listado completo | N/A |
| PUT | /{username} | Actualizacion de datos | {"email": "...", "password": "..."} |
| DELETE | /{username} | Eliminar registro | N/A |

---

## Estructura del Hash de BCrypt

En la base de datos, la columna `password_hash` guarda un formato como este:
`$2a$10$vI8aWBnW3fID.veFTAO.e.zPHzq4C8C.g24f0n.G12b`

Composicion:
* `$2a$`: Version del algoritmo.
* `$10$`: Costo (2^10 = 1024 iteraciones).
* `vI8aWBnW3fID.veFTAO.e.`: Sal (salt) de 22 caracteres generada de manera aleatoria.
* `zPHzq4C8C.g24f0n.G12b`: Hash de la contrasena (31 caracteres).

BCrypt no requiere una columna `salt` separada en la base de datos porque la sal esta integrada en la misma cadena.

---

## Columna raw_password_debug_only

La columna `raw_password_debug_only` almacena la contrasena en texto plano.
* **Nota:** Esto es solo para comparar de forma visual la contraseña plana con la cadena generada de BCrypt en este entorno local de pruebas. En produccion esta columna no debe existir.

---

## Calibracion del Costo

El factor de costo determina las vueltas de procesamiento del hash.
* Se debe medir el rendimiento en el servidor destino. El calculo de un hash individual debe tardar entre **100ms y 500ms**.
* Menos de 100ms debilita la seguridad contra ataques de fuerza bruta. Mas de 500ms sobrecarga el CPU si hay multiples logins simultaneos.
* A medida que los procesadores y las tarjetas graficas avanzan, el costo debe subirse para mantener el tiempo de calculo constante frente a atacantes con mejor hardware.
