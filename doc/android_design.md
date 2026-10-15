# android_design.md — Aplicación Android

## Component Design

App Android (Java, Gradle) que escucha anuncios iBeacon del emisor, construye una
`Medicion` y la envía al backend a través de su proxy de lógica de negocio
(`logicafake/LogicaFake`), que encapsula el transporte (`PeticionarioREST`).

### Componentes

| Clase | Responsabilidad |
| :--- | :--- |
| `MainActivity` | GUI y ciclo de escucha de beacons |
| `Medicion` | Agregación de dominio de una medición |
| `TramaIBeacon` | Interpretación de la trama iBeacon (UUID, major, minor, txPower) |
| `Utilidades` | Conversiones de bytes/utilidades |
| `PeticionarioREST` | Transporte HTTP encapsulado (comunicación) |
| `logicafake/LogicaFake` | Proxy de lógica de negocio de cliente |
| `logicafake/ResultadoMedicion` | Callback de dominio que recibe la GUI: `alGuardar(Medicion)` / `alFallar(Text)` |

### Flujo

```text
anuncio iBeacon --> TramaIBeacon --> Medicion --> LogicaFake.guardarMedicion()
                                                       |
                                                       v
                                             PeticionarioREST (POST) --> backend REST
```

`MainActivity` nunca hace HTTP: delega en `LogicaFake` (ver
`frontend_business_logic_design.md`), que a su vez usa `PeticionarioREST`.

Texto de las pantallas: mockup en `doc/diagrams/Wireframes_Mockups/Mockup_App.png`.

## Design Clarifications

- `LogicaFake` convierte los campos de la trama a texto/números y construye el JSON.
- La URL del servidor es configurable (`strings.xml` / `LogicaFake.URL_SERVIDOR`):
  apunta a la IP de la máquina que sirve el backend.
- El transporte (`PeticionarioREST`) queda confinado al proxy, no a la GUI.
- `MainActivity` recibe solo `ResultadoMedicion` (éxito/fallo de dominio); nunca ve
  `RespuestaREST` ni códigos HTTP.

## General Rules

- **Programming Language:** Java (Android SDK, Gradle Kotlin DSL).
- **Function/Method Headers:** cada función/método debe incluir su diseño lógico en un
  bloque de comentario delimitado por líneas de guiones (`--------------------`).
- **Code Readability:** el código debe ser lo más claro y autoexplicativo posible, con el
  mínimo de comentarios en línea.
- **Automated Testing:** tests en `app/src/test/...` (`MedicionTest`, `LogicaFakeTest`).
