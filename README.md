# Proyecto Sprint 0 — Beacon

Sistema de medición con beacon BLE: un emisor **Arduino** publica tramas iBeacon, una
app **Android** las escucha y envía las mediciones al **backend**, y una **web** muestra
el historial de mediciones en (casi) tiempo real.

## Flujo de datos

```text
  [Arduino]                 [Android]                    [Backend]                       [Web]
  firmware          app (Java) + logica_fake      communication + business_logic      gui + frontend_business_logic
     |                        |                              |                              |
  anuncio iBeacon ---->  TramaIBeacon ----> Medicion ---> POST /mediciones ---> ServidorREST --> LogicaNegocio --> BBDD
                          (BLE scan)                      (logica_fake.js / .java)
     ...                                                                                              |
                                                     GET /mediciones <--------------------------------+
                                                     (ux.js -> logica_fake.js -> ServidorREST -> LogicaNegocio -> BBDD)
```

- **Escritura:** Arduino emite el iBeacon → Android lo lee y hace `POST` → el backend
  valida y guarda en MySQL/MariaDB.
- **Lectura:** la web hace `GET` cada 5 s y pinta la tabla (sin botones).

## Componentes

| Componente | Carpeta | Diseño | Tecnología |
| :--- | :--- | :--- | :--- |
| Comunicación (REST) | `src/communication/` | `doc/communication_design.md` | PHP 8 + Apache (XAMPP) |
| Lógica de negocio | `src/business_logic/` | `doc/business_logic_design.md` | PHP 8 |
| Base de datos | `src/database/` | `doc/database_design.md` | MySQL / MariaDB |
| Lógica fake cliente | `src/frontend_business_logic/` | `doc/frontend_business_logic_design.md` | JavaScript |
| GUI web | `src/gui/` | `doc/gui_design.md` | HTML + CSS + JS |
| App Android | `src/android/` | `doc/android_design.md` | Java (Gradle) |
| Firmware emisor | `src/firmware/` | `doc/firmware_design.md` | C++ (Arduino) |

## Estructura del repositorio

```text
Proyecto_Sprint0/
├── author.md                        Autor del proyecto
├── README.md                        Este fichero
├── GUIA_PRESENTACION.md             Guion de presentación
├── AGENTS.md                        Criterios de revisión del proyecto
├── Software_Engineering_Spec.md     Especificación de ingeniería de software
├── Database_Design_Spec.md          Especificación de diseño de base de datos
│
├── doc/                             Documentación y diseños
└── src/                             Código fuente por componente
```

### `doc/` — Documentación y diseños

```text
doc/
├── communication_design.md              Diseño del componente de comunicación (REST)
├── business_logic_design.md             Diseño de la lógica de negocio
├── database_design.md                   Diseño de la base de datos (tablas, PK/FK)
├── frontend_business_logic_design.md    Diseño de la lógica fake del cliente
├── gui_design.md                        Descripción de la interfaz web
├── android_design.md                    Diseño de la app Android
├── firmware_design.md                   Diseño del firmware Arduino
│
├── diagrams/                           Diagramas de diseño (.drawio + .png)
│   ├── Diseño_Arduino/                 ServicioEnEmisora, EmisoraBLE, Publicador, Medidor, PuertoSerie, LED, .ino
│   ├── Diseño_Android/                 MainActivity, PeticionarioREST, TramaIBeacon, Utilidades, Medicion, LogicaFake
│   ├── DIseño_BBDD/                    Diseño de la BBDD, Conexiones, RutasREST, LógicaNegocio...
│   └── Wireframes_Mockups/             Wireframes y mockups de app y web
│
└── prompts/                            Prompts usados para generar cada componente (.md + .pdf)
    ├── 1_Base_de_Datos
    ├── 2_Logica_de_Negocio_Backend
    ├── 3_Servidor_REST_Backend
    ├── 4_Logica_Fake_Telefono
    ├── 5_Logica_Fake_Navegador
    └── 6_UX_Navegador
```

### `src/` — Código fuente por componente

```text
src/
├── communication/                       Comunicación externa (REST)
│   ├── public/
│   │   ├── index.php                     Front controller: despacha según verbo y ruta
│   │   └── .htaccess                      Reescribe las URLs hacia index.php
│   └── rest/
│       └── ServidorREST.php               Implementa POST/GET /mediciones (sin SQL)
│
├── business_logic/                       Lógica de negocio (única que habla con la BBDD)
│   ├── LogicaNegocio.php                  guardarMedicion() / recuperarMedicion()
│   └── tests/
│       ├── LogicaNegocioTest.php          Validación + integración (usa mediciones_db_test)
│       └── ayuda.php                      Mini framework de aserciones para PHP
│
├── database/                             Persistencia
│   ├── config/
│   │   └── config.php                     Conexión PDO (conectarBD())
│   └── db/
│       ├── crear_bbdd.sql                 Crea mediciones_db con datos de ejemplo
│       ├── borrar_bbdd.sql                Destruye mediciones_db
│       └── verificar_bbdd.sql             Comprueba columnas, PK y contenido
│
├── frontend_business_logic/              Lógica fake del NAVEGADOR (proxy de comunicación)
│   └── logica_fake.js                     recuperarMedicion(): GET /mediciones
│
├── gui/                                  Interfaz web
│   ├── index.html                         Página única del historial
│   ├── css/
│   │   └── estilo.css                     Aspecto de la tabla y estados
│   ├── js/
│   │   └── ux.js                          Pinta la tabla y se refresca sola
│   ├── package.json                       Script de tests (npm test)
│   └── tests/
│       ├── ayuda.js                       Aserciones para los tests de la web
│       ├── logica_fake.test.js            Tests de LogicaFake (fetch mockeado)
│       ├── ux.test.js                     Tests del pintado de ux.js
│       └── integracion.test.js            Test real contra el REST de XAMPP
│
├── android/                              App Android (Gradle)
│   ├── build.gradle.kts, settings.gradle.kts, gradle.properties
│   └── app/src/
│       ├── main/
│       │   ├── AndroidManifest.xml        Permisos de Bluetooth y ubicación
│       │   ├── java/com/example/mmarvid1/beaconapp/
│       │   │   ├── MainActivity.java       Escanea beacons y envía la medición
│       │   │   ├── Medicion.java           Modelo placa/tipo/valor
│       │   │   ├── TramaIBeacon.java       Parte la trama en uuid/major/minor/txPower
│       │   │   ├── PeticionarioREST.java   Cliente HTTP en segundo plano
│       │   │   ├── Utilidades.java         Conversión de bytes y utilidades
│       │   │   └── logicafake/
│       │   │       └── LogicaFake.java      Proxy: construye el JSON y hace el POST
│       │   └── res/                        Layout, textos, temas, colores
│       └── test/java/.../                  MedicionTest, LogicaFakeTest (JUnit)
│
└── firmware/                             Firmware Arduino
    └── MandarBeacon_Arduino/
        ├── MandarBeacon_Arduino.ino        setup() / loop(): publica en bucle
        ├── EmisoraBLE.h                    Emite anuncios iBeacon
        ├── Publicador.h                    Traduce una medida a iBeacon
        ├── Medidor.h                       Obtiene las magnitudes a publicar
        ├── ServicioEnEmisora.h             Servicio GATT y características
        ├── PuertoSerie.h                   Escritura/lectura por el puerto serie
        └── LED.h                           Control del LED
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

Con **Apache + MySQL** levantados en XAMPP:

```powershell
# 1. Lógica de negocio (PHP) - en src/business_logic/tests
& "M:\xampp\php\php.exe" .\LogicaNegocioTest.php

# 2. Web (JS) - en src/gui
npm.cmd test

# 3. Android - en src/android
.\gradlew.bat testDebugUnitTest --console=plain "-Dorg.gradle.java.home=C:\Program Files\Android\Android Studio\jbr"
```

| Suite | Fichero | Qué comprueba |
| :--- | :--- | :--- |
| PHP | `src/business_logic/tests/LogicaNegocioTest.php` | Validación de `guardarMedicion`/`recuperarMedicion` y su integración con `mediciones_db_test` |
| Web | `src/gui/tests/logica_fake.test.js` | `LogicaFake` del navegador (fetch mockeado) |
| Web | `src/gui/tests/ux.test.js` | Pintado de la tabla y estados de la GUI |
| Web | `src/gui/tests/integracion.test.js` | `GET`/`POST` reales contra el REST de XAMPP |
| Android | `src/android/app/src/test/...` | `MedicionTest` y `LogicaFakeTest` (JUnit, en la JVM) |

> Nota: el firmware Arduino no tiene pruebas automáticas.
