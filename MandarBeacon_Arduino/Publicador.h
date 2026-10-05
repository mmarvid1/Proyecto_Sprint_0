// ===========================================================================
//  Minerva Maravilla Vidaurre
//  05/10/2026
// ===========================================================================
#ifndef PUBLICADOR_H_INCLUIDO
#define PUBLICADOR_H_INCLUIDO


// ===========================================================================
//  DISEÑO LÓGICO  --  Publicador.h
// ===========================================================================
//
//
//     MedicionesID = { CO2 = 11, TEMPERATURA = 12, RUIDO = 13 }
//
//
//                 --------- Publicador ------------------
//                 |
//                 | MINOR_MANUAL: Z
//                 | TX_POWER: Z
//                 | beaconUUID: [ N ]_16
//                 |
//   laEmisora: EmisoraBLE
//   RSSI: Z
//                 |
//                 |
//                 Publicador() -->
//                 |
//                 |
//                 encenderEmisora() -->
//                 |
//                 |
// valorCO2: Z
// contador: N
// tiempoEspera: Z
//             --> publicarCO2() -->
//                 |
//                 |
// valorTemperatura: Z
// contador: N
// tiempoEspera: Z
//             --> publicarTemperatura() -->
//                 |
//                 -------------------------------------
//
//
// ===========================================================================

class Publicador {

private:
	const int16_t MINOR_MANUAL=100;
	const int8_t TX_POWER= 4;

  uint8_t beaconUUID[16] = { 
	'E', 'P', 'S', 'G', '-', 'G', 'T', 'I', 
	'-', 'P', 'R', 'O', 'Y', '-', '3', 'A'
	};

public:
  EmisoraBLE laEmisora {
	"Minerva_ELBACON", //  nombre emisora
	  0x004c, // fabricanteID (Apple)
	  4 // txPower
	  };
  
  const int RSSI = -53; // por poner algo, de momento no lo uso

public:

  // ............................................................
  //  Identificador de la magnitud medida.  Viaja en el byte alto de 'major'.
  // ............................................................
  enum MedicionesID  {
	CO2 = 11,
	TEMPERATURA = 12,
	RUIDO = 13
  };

  // ............................................................
  //   Publicador() -->
  // ............................................................
  Publicador( ) {
	// No encender la emisora aquí
	// Pondremos un método para llamarlo desde el setup() más tarde
  } // ()

  // ............................................................
  //   encenderEmisora() -->
  // ............................................................
  void encenderEmisora() {
	(*this).laEmisora.encenderEmisora();
  } // ()

  // ............................................................
  //  valorCO2: Z  
  //  contador: N
  //  tiempoEspera: Z
  //                  --> publicarCO2() -->
  //
  //  Observación: 'valorCO2' NO viaja en el anuncio; el minor usa la
  //  constante MINOR_MANUAL.  Es el comportamiento actual del código.
  // ............................................................
  void publicarCO2( int16_t valorCO2, uint8_t contador, long tiempoEspera ) {
	// 1. empezamos anuncio
	uint16_t major = (MedicionesID::CO2 << 8) + contador;
	(*this).laEmisora.emitirAnuncioIBeacon( (*this).beaconUUID, 
											major,
											MINOR_MANUAL, // minor
											TX_POWER // rssi
									);

	// 2. esperamos el tiempo que nos digan
	esperar( tiempoEspera );

	// 3. paramos anuncio
	(*this).laEmisora.detenerAnuncio();
  }

  // ............................................................
  //  valorTemperatura: Z  
  //  contador: N
  //  tiempoEspera: Z
  //                  --> publicarTemperatura() -->
  // ............................................................
  void publicarTemperatura( int16_t valorTemperatura,
							uint8_t contador, long tiempoEspera ) {

	uint16_t major = (MedicionesID::TEMPERATURA << 8) + contador;
	(*this).laEmisora.emitirAnuncioIBeacon( (*this).beaconUUID, 
											major,
											valorTemperatura, // minor
											(*this).RSSI // rssi
									);
	esperar( tiempoEspera );

	(*this).laEmisora.detenerAnuncio();
  }
	
};

#endif