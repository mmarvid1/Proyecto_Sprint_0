# frontend_business_logic_design.md — Proxy de Lógica de Negocio de Cliente

## Component Design

Define la interfaz de lógica de negocio que consumen las GUI. Presenta a las GUI
la **misma interfaz limpia de dominio** que la lógica de negocio del backend
(`business_logic_design.md`), pero su implementación es un **proxy/fake** que
encapsula las llamadas de comunicación al backend. La GUI nunca hace HTTP: solo
llama a estos objetos.

Implementaciones:

- Web: `src/frontend_business_logic/logica_fake.js` (objeto `LogicaFake`, usa `fetch`).
- Android: `src/android/app/src/main/java/.../logicafake/LogicaFake.java` (usa
  `PeticionarioREST` como transporte encapsulado y expone `ResultadoMedicion`, un callback
  de dominio).

### Tipos de dominio

```text
Medicion = ( tipo_medicion: Text, valor: Z, identificador_placa: Text, fecha_hora: Text )
```

### Interfaz (idéntica a la del backend, subconjunto usado por la GUI)

| Método | Firma lógica | Paridad |
| :--- | :--- | :--- |
| `recuperarMedicion` | `recuperarMedicion() --> [ Medicion ]` | = backend `recuperarMedicion` |
| `guardarMedicion` | `medicion: Medicion --> guardarMedicion() --> Medicion` | = backend `guardarMedicion`: la `Medicion` resultante se entrega de forma asíncrona vía callback de dominio (`ResultadoMedicion`) |

### Clase `LogicaFake` (web)

```text
                   ----------------- LogicaFake (web) -----------------
                   |
                   | URL_MEDICIONES: Text   ( const )
                   |
                   |
                   recuperarMedicion() <-- mediciones: [ Medicion ]
                   |
                   ------------------------------------------------
```

### Encapsulación de la comunicación

Aunque la GUI ve solo tipos de dominio, la implementación del proxy realiza la
llamada real:

- Web: `fetch("GET", URL_MEDICIONES)` contra
  `/Proyecto_Sprint0/src/communication/public/mediciones`, y limpia el JSON a `Medicion`.
- Android: `PeticionarioREST.hacerPeticionREST("POST", URL_SERVIDOR, cuerpoJSON, ...)`;
  el proxy traduce el código HTTP/cuerpo a `ResultadoMedicion.alGuardar()` /
  `ResultadoMedicion.alFallar()`, que es lo único que ve la GUI.

Los detalles de transporte (verbo, URL, JSON, códigos de estado) quedan confinados
dentro del proxy y nunca se filtran a la GUI.

## Design Clarifications

- El subconjunto lo marca la GUI: la web solo necesita `recuperarMedicion`, la app
  Android solo necesita `guardarMedicion`.
- Los nombres de método, parámetros y tipos coinciden con los del backend para
  permitir intercambiar proxy y lógica real sin tocar la GUI.
- El proxy valida la forma de la respuesta y rechaza/avisa con mensajes de dominio.

## General Rules

- **Programming Language:** JavaScript (web) y Java (Android) para el proxy; el diseño
  lógico es agnóstico al lenguaje.
- **Function/Method Headers:** cada función/método debe incluir su diseño lógico en un
  bloque de comentario delimitado por líneas de guiones (`--------------------`).
- **Code Readability:** el código debe ser lo más claro y autoexplicativo posible, con el
  mínimo de comentarios en línea.
- **Automated Testing:** deben generarse pruebas unitarias para el proxy con el transporte
  simulado (fetch mockeado en web, `LogicaFakeTest` en Android).
