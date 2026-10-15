# Proyecto Sprint 0 — Beacon

Sistema de medición con beacon BLE: un emisor Arduino publica tramas iBeacon, una
app Android las escucha y envía las mediciones al backend, y una web muestra el
historial de mediciones en tiempo (casi) real.

## Componentes

| Componente | Carpeta | Tecnología |
| :--- | :--- | :--- |
| Comunicación (REST) | `src/communication/` | PHP 8 + Apache (XAMPP) |
| Lógica de negocio | `src/business_logic/` | PHP 8 |
| Base de datos | `src/database/` | MySQL / MariaDB |
| Lógica fake de cliente | `src/frontend_business_logic/` | JavaScript / Java |
| GUI web | `src/gui/` | HTML + CSS + JS |
| App Android | `src/android/` | Java (Gradle) |
| Firmware emisor | `src/firmware/` | C++ (Arduino) |

## Estructura

```text
author.md
doc/                          Diseños de cada componente
  communication_design.md
  business_logic_design.md
  database_design.md
  frontend_business_logic_design.md
  firmware_design.md
  diagrams/                   Diagramas .drawio y wireframes
  prompts/                    Prompts usados para generar el proyecto
src/                          Código fuente por componente
```

## Puesta en marcha (XAMPP)

1. Arrancar **Apache** y **MySQL** desde el panel de XAMPP.
2. Importar `src/database/db/crear_bbdd.sql` (crea `mediciones_db` con datos de ejemplo).
3. Servir el proyecto dentro de `htdocs` como `/Proyecto_Sprint0` (junction o copia).
4. Abrir la web en `http://localhost/Proyecto_Sprint0/src/gui/index.html`.

### API REST

- `POST /Proyecto_Sprint0/src/communication/public/mediciones` — guarda una medición.
- `GET  /Proyecto_Sprint0/src/communication/public/mediciones` — devuelve todas las mediciones.

## Pruebas

```bash
cd src/gui
npm test                 # logica_fake + ux + integracion (estos últimos requieren XAMPP)
```

- `src/gui/tests/logica_fake.test.js` — lógica fake del navegador (fetch mockeado).
- `src/gui/tests/ux.test.js` — pintado de la GUI web.
- `src/gui/tests/integracion.test.js` — prueba real contra el servidor REST (requiere XAMPP).
- `src/business_logic/tests/LogicaNegocioTest.php` — validación de `LogicaNegocio` (sin BBDD) y
  pruebas de integración sobre `mediciones_db_test` (requieren MySQL).
- `src/android/app/src/test/...` — tests de instrumentación de la app Android.
