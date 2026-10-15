# database_design.md — Componente de Base de Datos

## Component Design

La base de datos `mediciones_db` (InnoDB, `utf8mb4`) almacena las mediciones
captadas por el beacon. Contiene una única tabla.

```text
====================================================================================
TABLE: mediciones

DESCRIPTION: Almacena cada medicion captada por la app Android y recuperada por la web.
             La "fecha_hora" (con milisegundos) la genera la propia base de datos.

COLUMNS:

+ tipo_medicion       | VARCHAR(32)     | NULL     | NULL
+ valor               | SMALLINT SIGNED | NOT NULL |
+ identificador_placa | VARCHAR(64)     | NOT NULL |
+ fecha_hora          | DATETIME(3)     | NOT NULL | CURRENT_TIMESTAMP(3)

PRIMARY KEY: (identificador_placa, fecha_hora)

FOREIGN KEYS:

+ (ninguna)

CONSTRAINTS:

+ ENGINE = InnoDB
+ CHARSET = utf8mb4 / COLLATE utf8mb4_unicode_ci
+ valor debe caber en SMALLINT SIGNED (-32768 .. 32767) y ser positivo (lo valida la logica de negocio)
+ fecha_hora se inicializa con CURRENT_TIMESTAMP(3) (precision de milisegundos)
+ La clave primaria es compuesta (identificador_placa, fecha_hora): una
  misma placa no puede repetir fecha/hora.
====================================================================================
```

### Mapa con la lógica de negocio

La tabla `mediciones` se corresponde con la agregación `Medicion` usada por la
lógica de negocio:

| Columna | Tipo lógico | Uso |
| :--- | :--- | :--- |
| `tipo_medicion` | `Text` | Tipo de magnitud (CO2/TEMPERATURA/RUIDO/MANUAL), opcional |
| `valor` | `Z` | Valor medido, obligatorio |
| `identificador_placa` | `Text` | Identificador de la placa emisora, obligatorio (parte de la PK) |
| `fecha_hora` | `Text` | Fecha/hora generada por la BBDD, con milisegundos (parte de la PK) |

Scripts: `src/database/db/crear_bbdd.sql`, `borrar_bbdd.sql`, `verificar_bbdd.sql`.
Conexión: `src/database/config/config.php` (`conectarBD(): PDO`).

## Design Clarifications

- No se usan claves foráneas porque el proyecto tiene una única tabla.
- El metadato `tipo_medicion` admite `NULL` para no obligar a la app a enviarlo siempre.
- `identificador_placa` es obligatorio: forma parte de la clave primaria compuesta.
- Las consultas siempre usan sentencias preparadas; nunca se concatena SQL.
- `fecha_hora` nunca se envía desde el cliente: la pone la BBDD.

## General Rules

- **Programming Language:** SQL (MySQL / MariaDB), conexión mediante PDO desde PHP 8.
- **Function/Method Headers:** cada función/método debe incluir su diseño lógico en un
  bloque de comentario delimitado por líneas de guiones (`--------------------`).
- **Code Readability:** el código debe ser lo más claro y autoexplicativo posible, con el
  mínimo de comentarios en línea.
- **Automated Testing:** deben generarse pruebas unitarias/integración para los métodos y
  funciones críticas (los tests de integración validan el contenido de esta tabla).
