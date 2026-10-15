# firmware_design.md — Componente Firmware (Emisor Arduino)

## Component Design

Firmware del dispositivo emisor iBeacon. En cada iteración del `loop()` mide un
valor, lo publica como anuncio iBeacon BLE y hace parpadear un LED. Globales:
`elLED`, `elPuerto`, `elPublicador`, `elMedidor`.

```text
                 --------- MandarBeaconArduino ---------
                 |
                 | Globales::elLED: LED
                 | Globales::elPuerto: PuertoSerie
                 | Globales::elPublicador: Publicador
                 | Globales::elMedidor: Medidor
                 | Loop::cont: N
                 |
                 |
                 setup() -->
                 |
                 |
                 loop() -->
                 |
                 ----------------------------------------
```

### Clases y métodos (diseño lógico)

| Clase | Métodos | Firma lógica |
| :--- | :--- | :--- |
| `LED` | `brillar` | `ms: N --> brillar()` |
| `PuertoSerie` | `esperarDisponible`, `escribir` | `escribir(texto: Text) --> ` |
| `Medidor` | `iniciarMedidor`, `medirCO2`, `medirTemperatura` | `medirCO2() --> Z` ; `medirTemperatura() --> Z` |
| `Publicador` | `encenderEmisora`, `publicarCO2`, `publicarTemperatura`, `publicarManual` | `valorCO2: Z, contador: N, tiempoEspera: Z --> publicarCO2()` ; `valorTemperatura: Z, contador: N, tiempoEspera: Z --> publicarTemperatura()` ; `contador: N, tiempoEspera: Z --> publicarManual()` |
| `EmisoraBLE` | `encenderEmisora`, `detenerAnuncio`, `estaAnunciando`, `emitirAnuncioIBeacon`, `emitirAnuncioIBeaconLibre` | `uuid: [ N ]_16, major: Z, minor: Z, rssi: N --> emitirAnuncioIBeacon()` ; `estaAnunciando() --> B` |
| `ServicioEnEmisora` | `anyadirCaracteristica`, `activarServicio` | `servicio --> anyadirCaracteristica()` |

El valor de la medición se transporta como `minor` (parte baja) y el tipo de
magnitud como byte alto de `major`, coherente con la app Android
(`TramaIBeacon`).

## Design Clarifications

- El firmware es un emisor autónomo: no habla con el backend directamente (lo hace
  la app Android que escucha sus anuncios).
- `publicarManual` no recibe el valor por parámetro: emite el valor de prueba fijo
  `MINOR_MANUAL` (499) para el Sprint 0; el tipo de magnitud MANUAL (14) viaja en el
  byte alto de `major`.
- Las clases de comunicación BLE se apoyan en la librería `bluefruit`.

## General Rules

- **Programming Language:** C++ (Arduino / Adafruit Bluefruit).
- **Function/Method Headers:** cada función/método debe incluir su diseño lógico en un
  bloque de comentario delimitado por líneas de guiones (`--------------------`).
- **Code Readability:** el código debe ser lo más claro y autoexplicativo posible, con el
  mínimo de comentarios en línea.
- **Automated Testing:** por limitaciones de hardware, la verificación es manual (monitor
  serie); se documentan los casos de prueba del `loop()`.
