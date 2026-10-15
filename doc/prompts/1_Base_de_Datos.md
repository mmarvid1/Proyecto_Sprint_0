# PROMPT 1 — Base de Datos MySQL

> Documento-prompt para una IA implementadora. Debe **generar los scripts SQL** de la base de datos
> del Sprint 0 a partir de la especificación y
> entregarlos funcionando + verificación automática. No debe dejarlos "a medias": los scripts se
> ejecutan de principio a fin sin errores.

> **Ante cualquier duda o falta de información: PÁRATE y pregunta al responsable del proyecto
> ANTES de escribir o modificar nada.** No asumas ni inventes.

---

## 1. Rol y tarea

Eres un DBA de MySQL.

**Tarea:** crear los scripts de la base de datos `mediciones_db` (una **única tabla** `mediciones`)
siguiendo exactamente el diseño de la sección 3. Debes **escribir tú el SQL** de cada fichero:
nadie te da las sentencias listas.

## 2. Contexto del proyecto

- Una placa BLE emite un **iBeacon**; la app Android lo captura y hace **POST** al servidor REST.
- El servidor guarda cada medición en la tabla `mediciones` y la web las recupera con **GET**.
- El valor que transporta el beacon es **`1234`** y viaja en el campo **`minor`**.
- El teléfono envía `{"minor": 1234}` (más metadatos opcionales: `uuid`, `major`, `tx_power`, `nombre_emisora`).
- En el Sprint 0 el resto de metadatos son **opcionales**: la BBDD los admite nulos.

## 3. Diseño de la tabla (especificación)

```text
      ------------------- TABLA mediciones (mediciones_db) -------------------
      |
      | id            : N      (PRIMARY KEY, AUTOINCREMENTAL)   // (new)
      | uuid          : Text          // (new)  
      | major         : Z             // (new) 
      | minor         : Z             // (new)  CLAVE: el valor de la medicion (1234)
      | tx_power      : Z             // (new) 
      | nombre_emisora: Text          // (new)  
      | fecha_hora    : Text          // (new)  la pone la BBDD (CURRENT_TIMESTAMP)
      |
      -------------------------------------------------------------------------
```

Traducción obligatoria a MySQL de cada columna (debes respetarla):

| Columna | Tipo MySQL | Reglas |
| :--- | :--- | :--- |
| `id` | `INT UNSIGNED` | PK, `AUTO_INCREMENT`, `NOT NULL` |
| `uuid` | `VARCHAR(32)` | `NULL` |
| `major` | `SMALLINT SIGNED` | `NULL` |
| `minor` | `SMALLINT SIGNED` | **`NOT NULL`** ← transporta el valor (1234) |
| `tx_power` | `TINYINT` | `NULL` |
| `nombre_emisora` | `VARCHAR(64)` | `NULL` |
| `fecha_hora` | `DATETIME` | `NOT NULL DEFAULT CURRENT_TIMESTAMP` |

> **No existe el campo `valor_medicion`**: el valor de la medición ES el campo `minor`.
> El DDL debe usar `snake_case`, motor `InnoDB` y juego de caracteres `utf8mb4`.

## 4. Ficheros SQL a generar

Genera **3 ficheros** (en la carpeta `backend/db/`):

1. **`crear_bbdd.sql`** — debe:
   - crear la base de datos `mediciones_db` (si ya existe, no debe fallar);
   - seleccionarla;
   - crear la tabla `mediciones` con las 7 columnas de la sección 3;
   - insertar **3 filas de ejemplo** de una medición del beacon con los valores del proyecto:
     `uuid = "EPSG-GTI-PROY-3A"`, `major = 2816`, `minor = 1234`, `tx_power = 4`,
     `nombre_emisora = "Minerva_ELBACON"`, en tres fechas/horas distintas y próximas.
2. **`borrar_bbdd.sql`** — debe borrar la base de datos por completo para poder reconstruirla de cero.
3. **`verificar_bbdd.sql`** — script de prueba (ver sección 6).

No copies código de otro sitio: el SQL de estos ficheros es lo que esta IA debe escribir.

## 5. Comentarios (OBLIGATORIOS)

- Cabecera en cada fichero con qué hace y a qué objetivo del proyecto sirve.
- Cada método con 1–2 líneas antes de la firma: qué hace, qué recibe, qué devuelve.
- Comentarios en **español**.

## 6. Pruebas automáticas (OBLIGATORIAS)

El fichero **`verificar_bbdd.sql`** (que debes escribir) debe ejecutar, en orden y con sus
comentarios, comprobaciones que devuelvan lo esperado:

1. **La tabla existe** en `mediciones_db` (debe confirmarse que existe).
2. **Las 7 columnas** existen con los nombres de la sección 3, en el orden del diseño.
3. **Hay exactamente 3 filas** de ejemplo.
4. **El contenido es correcto**: muestra todas las filas ordenadas de más reciente a más antigua;
   deben verse `minor = 1234`, `EPSG-GTI-PROY-3A` y `Minerva_ELBACON`.

Documenta al final del fichero la **secuencia completa de verificación** (borrar→crear→verificar)
para reconstruir la BBDD desde cero y comprobar que todo pasa, con **XAMPP**:

1. Abre **phpMyAdmin** (`http://localhost/phpmyadmin`).
2. En pestaña **Importar**, ejecuta en este orden: `borrar_bbdd.sql`, `crear_bbdd.sql` y `verificar_bbdd.sql`.
3. Comprueba en phpMyAdmin que `mediciones_db` tiene la tabla `mediciones` con las 7 columnas y las 3 filas.

Alternativa por consola (cliente MySQL de XAMPP, en `C:\xampp\mysql\bin`):

```bash
mysql -u root < backend/db/borrar_bbdd.sql
mysql -u root < backend/db/crear_bbdd.sql
mysql -u root < backend/db/verificar_bbdd.sql
```

## 7. Verificación y entrega

- [ ] Los 3 scripts se ejecutan en orden sin errores.
- [ ] Tabla única `mediciones` con las 7 columnas del diseño.
- [ ] 3 filas de ejemplo con `minor = 1234`.
- [ ] Comentarios en español con cabecera `PROPOSITO/USO`.
- [ ] Resumen final: ficheros generados, comandos usados y salida de `verificar_bbdd.sql`.