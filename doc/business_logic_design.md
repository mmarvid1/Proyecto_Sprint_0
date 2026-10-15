# business_logic_design.md — Componente de Lógica de Negocio

## Component Design

La clase `LogicaNegocio` (PHP) implementa la lógica de negocio real del backend.
Es la **única capa que accede a la base de datos**: valida las mediciones, las guarda
y devuelve el listado completo. No conoce nada del transporte (no usa `HttpRequest`,
`HttpResponse`, códigos HTTP, rutas ni cabeceras).

### Tipos de dominio

```text
Medicion = ( tipo_medicion: Text, valor: Z, identificador_placa: Text, fecha_hora: Text )

LN_RANGE_VALOR = [ Z ]_2      // rango valido de "valor" (SMALLINT SIGNED)
```

### Clase `LogicaNegocio`

```text
                      ----------------- LogicaNegocio -----------------
                      |
                      | LN_RANGE_VALOR: [ Z ]_2    ( const )
                      |
                      |
 medicion: Medicion   --> guardarMedicion() --> resultado: Medicion
                      |
                      |
                      recuperarMedicion() <-- mediciones: [ Medicion ]
                      |
                      |
                      -------------------------------------------
```

### Firmas de los métodos (diseño lógico)

| Método | Firma lógica | Semántica |
| :--- | :--- | :--- |
| `guardarMedicion` | `medicion: Medicion --> guardarMedicion() --> Medicion` | Valida `valor` (obligatorio, entero, en rango y positivo) e `identificador_placa` (obligatorio); inserta en la tabla `mediciones` y devuelve la `Medicion` completa con la `fecha_hora` real que puso la BBDD. |
| `recuperarMedicion` | `recuperarMedicion() --> [ Medicion ]` | Devuelve todas las mediciones ordenadas de la más reciente a la más antigua (lista vacía si no hay ninguna). |

Métodos auxiliares privados (extraídos del diseño): validación de entero
(`esEnteroValido`), de rango (`estaEnRango`), de cadenas opcionales
(`opcionalCadena`) y de cadenas obligatorias (`obligatorioCadena`).

### Conocimiento de la base de datos

La lógica opera sobre la tabla `mediciones` definida en
[`database_design.md`](database_design.md): columnas `tipo_medicion, valor,
identificador_placa, fecha_hora`. Las operaciones son:

- `INSERT INTO mediciones (tipo_medicion, valor, identificador_placa) VALUES (...)` — la
  `fecha_hora` la genera la BBDD (`CURRENT_TIMESTAMP(3)`).
- `SELECT tipo_medicion, valor, identificador_placa, fecha_hora FROM mediciones
  ORDER BY fecha_hora DESC`.
- Tras insertar, relee la última fila de esa placa para obtener la `fecha_hora` real:
  `SELECT ... FROM mediciones WHERE identificador_placa = :placa ORDER BY fecha_hora DESC LIMIT 1`.

### Independencia de la comunicación

La firma usa exclusivamente tipos de dominio (`Medicion`, `N`, `Z`, `Text`, `[ ]`).
No aparecen tipos de transporte, ni códigos de estado, ni nociones de HTTP/CLI/
WebSocket. El mapeo de errores a respuestas de red es responsabilidad exclusiva del
componente de comunicación.

## Design Clarifications

- `valor` se acepta como entero, cadena de dígitos o float entero, y se normaliza a `Z`.
- `identificador_placa` es obligatorio; si falta o no es texto no vacío, se rechaza.
- El metadato `tipo_medicion` vacío o ausente se guarda como `NULL`.
- Tras insertar, se relee la última fila de esa placa para obtener la `fecha_hora` real de la BBDD.
- La lógica es agnóstica al lenguaje de transporte por diseño (requisito del checklist).

## General Rules

- **Programming Language:** PHP 8 (backend).
- **Function/Method Headers:** cada función/método debe incluir su diseño lógico en un
  bloque de comentario delimitado por líneas de guiones (`--------------------`).
- **Code Readability:** el código debe ser lo más claro y autoexplicativo posible, con el
  mínimo de comentarios en línea.
- **Automated Testing:** deben generarse pruebas unitarias/integración para `guardarMedicion`
  y `recuperarMedicion` (casos: valor válido, valor ausente, valor fuera de rango,
  listado vacío).
