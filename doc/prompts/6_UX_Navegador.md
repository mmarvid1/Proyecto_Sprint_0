# PROMPT 6 — UX en el Navegador (Web)

> Documento-prompt para una IA implementadora. Debe **crear la UX del navegador** y entregarla
> funcionando + pruebas automáticas en verde. La web debe verse **igual** a la imagen adjunta.
>
> **Ante cualquier duda o falta de información: PÁRATE y pregunta al responsable del proyecto
> ANTES de escribir o modificar nada.** No asumas ni inventes.

---

## 1. Rol y tarea

Eres un ingeniero frontend (HTML + CSS + JS vanilla, sin frameworks).

**Tarea:** implementar la **UX del panel de mediciones** (sección 3, marcada `(new)`) que consume
`LogicaFake.recuperarMedicion()` (Prompt 5) y pinta el resultado **exactamente igual a la imagen
del diseño que se adjunta a este prompt**.

## 2. Contexto del proyecto

- El servidor REST devuelve mediciones `{ id, uuid, major, minor, tx_power, nombre_emisora, fecha_hora }`.
- El valor de la medición es **`minor`** (p.ej. 1234); la emisora es **`nombre_emisora`**
  (p.ej. "Minerva_ELBACON").
- `LogicaFake.recuperarMedicion()` ya existe (Prompt 5) y resuelve con el array de Mediciones.
- **Imagen de diseño (OBLIGATORIA):** junto a este prompt se entrega una **imagen** con el diseño
  de la pantalla. Es la **fuente de verdad visual** (estructura, colores, tipografías, textos,
  disposición y estados). Si la imagen **no está presente**, **PÁRATE y pídela al responsable**:
  nunca inventes un diseño por tu cuenta.

## 3. Diseño a implementar

### 3.1 Diseño de pantalla

La pantalla se hará **copiando la imagen adjunta**: misma estructura, mismos colores, mismas
tipografías, mismos textos, mismos espacios y estados. Si algo de la imagen no se ve claro,
pregunta antes de continuar. No hay ningún otro "diseño de texto": lo único válido es la imagen.

### 3.2 Objeto NUEVO `UX`

```text
      ------------------- UX (NEW) -------------------
      |
      | contenedor: Element (el <main#contenedorMediciones>)
      | intervaloMs: N        (const = 5000)     // cada cuanto se refresca solo,
      |                                          //   sin boton 'Recargar'
      |
      |
      --> iniciar() -->                       // (new) estado 'cargando' + 1a carga
      |                                      //       + arranca el refresco periodico
      |
      |
      --> recargarMediciones() -->            // (new) vuelve a pedir al servidor
      |
      |
mediciones: [Medicion] --> pintarMediciones() -->   // (new)
      |
      |
tipo: Text, mensaje: Text --> pintarEstado() -->     // (new) 'cargando'|'vacio'|'error'
      |
      ----------------------------------------------------------------
```

### 3.3 Ficha de funciones

| Función | Firma | Comportamiento |
| :--- | :--- | :--- |
| `iniciar()` | `-->` | Pinta "Cargando mediciones..." y llama a `recargarMediciones()`. Después arranca un **refresco periódico** cada `intervaloMs` (5000 ms) que llama a `recargarMediciones()` solo. **No hay botón**: la página se actualiza sola. |
| `recargarMediciones()` | `-->` | async. Evita lanzarse si ya hay una petición en curso (no solapar). `await LogicaFake.recuperarMedicion()` → `pintarMediciones(...)`; en `catch` → `pintarEstado("error", <mensaje>)`. |
| `pintarMediciones()` | `mediciones: [Medicion]` | `[]` → `pintarEstado("vacio")`. Con datos → pinta cada Medicion tal y como muestra la imagen adjunta: al menos `minor` (valor), `nombre_emisora` y `fecha_hora` en español (`28/09/2026 09:00:00`), más reciente primero. |
| `pintarEstado()` | `tipo: Text, mensaje: Text` | Cargando / "No hay mediciones todavía." / error con su mensaje. |

### 3.4 HTML y CSS

- `index.html` (semántico, `lang="es"`, `viewport`): `<header>` con el título (sin botón) + `<main id="contenedorMediciones">`; `<script>` en orden `logica_fake.js` → `ux.js`.
- `css/estilo.css`: colores, tipografías (fallback `Arial, Helvetica, sans-serif`), tamaños, radios y espaciados **copiados de la imagen adjunta**; clases `.medicion`, `.medicion-minor`, `.medicion-emisora`, `.medicion-fecha`, `.estado`, `.error`. Responsive básico (columnas en móvil).
- Comentario en CSS indicando qué bloque de la imagen replica cada estilo.

## 4. Ficheros a entregar

En `web/`: `index.html`, `css/estilo.css`, `js/ux.js` (junto a los del Prompt 5).

## 5. Requisitos de código y de comentarios (OBLIGATORIOS)

1. Comentarios **en español**, simples.
   - Cabecera de cada fichero: qué hace y a qué objetivo del proyecto sirve.
   - Cada función con 2–4 líneas antes de la firma: `PROPOSITO` / `PARAM.` / `RETORNA`.
2. Fidelidad al estilo del proyecto: separadores `// ----`; logs `console.log('>>>>', ...)`.

## 6. Pruebas automáticas (OBLIGATORIAS)

### 6.1 `web/tests/ux.test.js` (Jest con `jest-environment-jsdom`)
Montar un `<main id="contenedorMediciones">` mínimo.

1. **Pinta un historial**: `UX.pintarMediciones([{minor:1234, nombre_emisora:"Minerva_ELBACON", fecha_hora:"2026-09-28 09:00:00"}])` → el DOM contiene `1234`, `Minerva_ELBACON` y `28/09/2026`.
2. **Vacío** → el texto contiene "No hay mediciones".
3. **Error** → `UX.pintarEstado("error","servidor caido")` → DOM contiene "servidor caido".
4. **`recargarMediciones` usa LogicaFake**: con `LogicaFake.recuperarMedicion` stub que resuelve `[{minor:1234,...}]`, `UX.recargarMediciones()` pinta 1234.
5. **Refresco periódico**: `UX.iniciar()` llama a `recargarMediciones` al arrancar y, con temporizadores falsos (Jest fake timers), vuelve a llamarla al pasar `intervaloMs`. No hay ningún botón en el HTML.

### 6.2 Verificación visual manual
1. Servidor REST + BBDD con datos: Apache (XAMPP) sirviendo `backend/public` y BBDD importada por phpMyAdmin (Prompts 1 y 3).
2. Abrir `http://localhost/<carpeta>/` y comparar **con la imagen adjunta**: debe verse igual.
3. Enviar un 1234 desde Android y esperar el refresco periódico (máx. `intervaloMs`) → la nueva tarjeta aparece sola, sin pulsar nada.

### 6.3 Ejecución
```bash
cd web
npm test
```

## 7. Verificación y entrega

- [ ] La web reproduce **la imagen adjunta** (colores, tipografía, disposición, textos).
- [ ] **Sin botón**: auto-carga al abrir y refresco periódico cada `intervaloMs` (5000 ms); estados cargando / vacío / error visibles; mínimo `minor`, `nombre_emisora`, `fecha_hora` por tarjeta (más reciente primero).
- [ ] HTML+CSS+JS vanilla, sin librerías de UI. Comentarios en español.
- [ ] `npm test` en verde. Resumen final: ficheros y comandos.
- [ ] Si la imagen no estaba al empezar y se pidió: confirmar que se hizo antes de escribir nada.