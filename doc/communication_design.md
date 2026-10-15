# communication_design.md — Componente de Comunicación (REST)

## Component Design

El backend expone una API REST sobre Apache (XAMPP). El *front controller*
`src/communication/public/index.php` recibe todas las peticiones (gracias a
`.htaccess`) y las despacha al método correspondiente de `ServidorREST`
(`src/communication/rest/ServidorREST.php`), que valida el formato, delega en la
lógica de negocio y emite la respuesta JSON. Es la única capa que conoce HTTP.

### Endpoints

| Verbo | Ruta | Entrada | Salida (200) | Errores |
| :--- | :--- | :--- | :--- | :--- |
| `POST` | `/mediciones` | JSON `{ tipo_medicion?, valor, identificador_placa }` | `{ "ok": true }` | `400` cuerpo inválido / `400` lógica / `500` BBDD |
| `GET` | `/mediciones` | — | `{ "mediciones": [ Medicion ] }` | `500` BBDD |
| `*` | cualquier otra | — | — | `404` `{ "error": "ruta no encontrada" }` |

URL canónica bajo XAMPP:
`http://localhost/Proyecto_Sprint0/src/communication/public/mediciones`.

### Clase `ServidorREST`

```text
                   ----------------- ServidorREST -----------------
                   |
                   |
 cuerpo: Text   --> guardarMedicion() -->
                   |
                   |
                   recuperarMedicion() -->
                   |
                   |
                   rutaNoEncontrada() -->
                   |
                   ---------------------------------------------
```

### Flujo de dependencias

```text
index.php  -->  ServidorREST  -->  LogicaNegocio  -->  config.php (PDO)  -->  mediciones_db
 (router)        (REST/JSON)        (negocio)          (conexion)            (BBDD)
```

`ServidorREST` no contiene SQL: traduce la petición HTTP a llamadas de
`LogicaNegocio` y los errores de dominio a códigos HTTP (400/404/500).

## Design Clarifications

- El recurso se deduce del último segmento de la URL (`basename`), de modo que
  funciona con el prefijo del *junction* de XAMPP.
- Todas las respuestas son `application/json; charset=utf-8` y sin caché (`no-store`).
- Los errores de validación de dominio (`InvalidArgumentException`) se traducen a `400`.
- Las listas vacías se serializan como `[]` (no se fuerza objeto JSON).

## General Rules

- **Programming Language:** PHP 8 sobre Apache (XAMPP) con `mod_rewrite`.
- **Function/Method Headers:** cada función/método debe incluir su diseño lógico en un
  bloque de comentario delimitado por líneas de guiones (`--------------------`).
- **Code Readability:** el código debe ser lo más claro y autoexplicativo posible, con el
  mínimo de comentarios en línea.
- **Automated Testing:** deben generarse pruebas unitarias/integración para las rutas
  (`POST`/`GET`/`404`) y el contrato JSON de cada respuesta.
