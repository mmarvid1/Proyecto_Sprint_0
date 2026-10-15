# gui_design.md — Interfaz gráfica web

## Component Design

GUI web de una sola página (`src/gui/index.html` + `src/gui/js/ux.js` +
`src/gui/css/estilo.css`). Muestra el historial de mediciones en una tabla
que se refresca automáticamente cada 5 segundos. No hay botones.

### Pantalla única — Historial de mediciones

- **Cabecera:** título "Mediciones".
- **Contenido (`<main id="contenedorMediciones">`):**
  - estado `cargando`: "Cargando mediciones..." antes de la primera respuesta;
  - estado `vacio`: "No hay mediciones todavía." si la lista está vacía;
  - estado `error`: mensaje del servidor si la petición falla;
  - tabla con columnas `tipo_medicion, valor, identificador_placa, fecha_hora`.

Wireframes y mockups en `doc/diagrams/Wireframes_Mockups/`.

### Clase `UX`

```text
                   ------------------- UX (web) -------------------
                   |
                   | contenedor: DOM
                   | intervaloMs: N   ( const = 5000 )
                   | peticionEnCurso: B
                   |
                   |
                   iniciar() -->
                   |
                   |
                   recargarMediciones() -->
                   |
                   |
                   pintarMediciones(mediciones: [ Medicion ]) -->
                   |
                   |
                   pintarEstado(tipo: Text, mensaje: Text) -->
                   |
                   --------------------------------------------------
```

La GUI solo pinta: obtiene los datos a través de `LogicaFake`
(`frontend_business_logic_design.md`) y nunca realiza peticiones HTTP.

## Design Clarifications

- El refresco es automático (sin control de recarga) para mostrar datos "en vivo".
- La fecha se formatea a `dd/mm/aaaa hh:mm:ss`; los campos nulos se muestran como `-`.
- El valor se colorea según su magnitud (clases `valor-alto` / `valor-medio`).

## General Rules

- **Programming Language:** HTML5 + CSS3 + JavaScript (Vanilla).
- **Function/Method Headers:** cada función/método debe incluir su diseño lógico en un
  bloque de comentario delimitado por líneas de guiones (`--------------------`).
- **Code Readability:** el código debe ser lo más claro y autoexplicativo posible, con el
  mínimo de comentarios en línea.
- **Automated Testing:** existen pruebas de pintado y estados en
  `src/gui/tests/ux.test.js`.
