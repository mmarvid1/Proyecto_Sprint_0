// ===========================================================================
//  Minerva Maravilla Vidaurre
//  05/10/2026
// ===========================================================================
#ifndef EMISORA_H_INCLUIDO
#define EMISORA_H_INCLUIDO

#include "ServicioEnEmisora.h"


// ===========================================================================
//  DISEÑO LÓGICO  --  EmisoraBLE.h
// ===========================================================================
//
//
//     CallbackConexionEstablecida  = ( conn_handle: N )
//     CallbackConexionTerminada    = ( conn_handle: N, reason: N )
//
//
//                 --------- EmisoraBLE ------------------
//                 |
//                 | nombreEmisora: Text
//                 | fabricanteID: N
//                 | txPower: Z
//                 |
//                 |
//   nombreEmisora_: Text
//   fabricanteID_: N
//   txPower_: Z
//             --> EmisoraBLE() -->
//                 |
//                 |
//                 encenderEmisora() -->
//                 |
//                 |
//   cbce: CallbackConexionEstablecida
//   cbct: CallbackConexionTerminada
//             --> encenderEmisora() -->
//                 |
//                 |
//                 detenerAnuncio() -->
//                 |
//                 |
//           B <-- estaAnunciando() <--
//                 |
//                 |
//   beaconUUID: N
//   major: Z
//   minor: Z
//   rssi: N
//             --> emitirAnuncioIBeacon() -->
//                 |
//                 |
//   carga: Text 
//   tamanyoCarga: N
//             --> emitirAnuncioIBeaconLibre() -->
//                 |
//                 |
//  servicio: ServicioEnEmisora
//           B <-- anyadirServicio() <--
//                 |
//                 |
//  servicio: ServicioEnEmisora
//           B <-- anyadirServicioConSusCaracteristicas() <--
//                 |
//                 |
//  servicio: ServicioEnEmisora
//  caracteristica: ServicioEnEmisora::Caracteristica
//  restoCaracteristicas: T
//           B <-- anyadirServicioConSusCaracteristicas() <--
//                 |
//                 |
//  servicio: ServicioEnEmisora
//  restoCaracteristicas: [ ServicioEnEmisora::Caracteristica ]
//           B <-- anyadirServicioConSusCaracteristicasYActivar() <--
//                 |
//                 |
//  cb: CallbackConexionEstablecida
//             --> instalarCallbackConexionEstablecida() -->
//                 |
//                 |
//  cb: CallbackConexionTerminada
//             --> instalarCallbackConexionTerminada() -->
//                 |
//                 |
//  connHandle: N  --> getConexion() <--
//       BLEConnection <--
//                 |
//                 -------------------------------------
//
// ===========================================================================

class EmisoraBLE {
private:

  const char * nombreEmisora;
  const uint16_t fabricanteID;
  const int8_t txPower;

public:

  // .........................................................
  // CallbackConexionEstablecida  = ( conn_handle: N )
  // .........................................................
  using CallbackConexionEstablecida = void ( uint16_t connHandle );
  // .........................................................
  // CallbackConexionTerminada    = ( conn_handle: N, reason: N )
  // .........................................................
  using CallbackConexionTerminada = void ( uint16_t connHandle, uint8_t reason);

  // .........................................................
  //
  //   nombreEmisora_: Text
  //   fabricanteID_: N
  //   txPower_: Z
  //             --> EmisoraBLE() -->
  //
  //  Identidad de la emisora (nombre, fabricante, potencia).  NO
  //  encender el radio aquí: eso se hace con encenderEmisora() para no
  //  intercalar una escritura en el puerto serie antes de configurar Serial.
  // .........................................................
  EmisoraBLE( const char * nombreEmisora_, const uint16_t fabricanteID_,
			  const int8_t txPower_ ) : nombreEmisora( nombreEmisora_ ) , 
        fabricanteID( fabricanteID_ ) , txPower( txPower_ ) {} 
	
  // .........................................................
  //   encenderEmisora() -->
  //
  //  Arranca el hardware de radio (Bluefruit.begin) y, por si acaso, detenemos cualquier anuncio que estuviera activo.
  // .........................................................
  void encenderEmisora() {
	 Bluefruit.begin(); 
	 (*this).detenerAnuncio();
  } 

  // .........................................................
  //   cbce: CallbackConexionEstablecida
  //   cbct: CallbackConexionTerminada
  //             --> encenderEmisora() -->
  //
  //  Sobrecarga: enciende la emisora e instala los dos callbacks de conexión (establecida y terminada).
  // .........................................................
  void encenderEmisora( CallbackConexionEstablecida cbce, CallbackConexionTerminada cbct ) {

	encenderEmisora();

	instalarCallbackConexionEstablecida( cbce );
	instalarCallbackConexionTerminada( cbct );

  }

  // .........................................................
  //   detenerAnuncio() -->
  //
  //  Comprueba si había algún anuncio en curso y si lo hay lo detiene.
  // .........................................................
  void detenerAnuncio() {

	if ( (*this).estaAnunciando() ) {
	  Bluefruit.Advertising.stop(); 
	}

  }
  
  // .........................................................
  //   B <-- estaAnunciando() <--
  //
  //  Consulta si el motor de publicidad está activo.
  // .........................................................
  bool estaAnunciando() {
	return Bluefruit.Advertising.isRunning();
  }

  // .........................................................
  //   beaconUUID: N
  //   major: Z
  //   minor: Z
  //   rssi: N
  //             --> emitirAnuncioIBeacon() -->
  //
  // .........................................................
  void emitirAnuncioIBeacon( uint8_t * beaconUUID, int16_t major, int16_t minor, uint8_t rssi ) {

	(*this).detenerAnuncio();
	
	//sirve para vaciar el bufer publicitario para que asi no pase de los 31 bytes y no lo rechace
	Bluefruit.Advertising.clearData();

	// Se crea el beacon con el UUID, major, minor(con nuestro valor) y txPower
	BLEBeacon elBeacon( beaconUUID, major, minor, rssi );
	elBeacon.setManufacturer( (*this).fabricanteID );


	// Borra el nombre de emisora para luego declarar el nuevo y añadirlo con addName 
	Bluefruit.ScanResponse.clearData();

	Bluefruit.setTxPower( (*this).txPower );
	Bluefruit.setName( (*this).nombreEmisora );
	Bluefruit.ScanResponse.addName();

	// pongo el beacon -> mete el bloque dentro del paquete publicitario
	Bluefruit.Advertising.setBeacon( elBeacon );


	Bluefruit.Advertising.restartOnDisconnect(true); // no hace falta, pero lo pongo
	Bluefruit.Advertising.setInterval(100, 100);    // in unit of 0.625 ms

	// Arranca la publicidad de forma indefinida (0 = tiempo indefinido) hasta que se llama a detenerAnuncio()
	Bluefruit.Advertising.start( 0 ); 
	
  }

  // .........................................................
  // carga: Text 
  // tamanyoCarga: N --> emitirAnuncioIBeaconLibre() --> 
  //
  //  Variante de emisión con CARGA LIBRE: en lugar de respetar el formato
  //  iBeacon estricto, rellena los últimos 21 bytes del paquete con el
  //  contenido de 'carga' (hasta 21 bytes, truncada si excede).
  // .........................................................
  void emitirAnuncioIBeaconLibre( const char * carga, const uint8_t tamanyoCarga ) {

	(*this).detenerAnuncio(); 

	Bluefruit.Advertising.clearData();
	Bluefruit.ScanResponse.clearData(); // hace falta? MIO

	Bluefruit.setName( (*this).nombreEmisora );
	Bluefruit.ScanResponse.addName();

	Bluefruit.Advertising.addFlags(BLE_GAP_ADV_FLAGS_LE_ONLY_GENERAL_DISC_MODE);


	// hasta ahora habrá, supongo, ya puestos los 5 primeros bytes.
	// Poner 4 bytes fijos (company ID, beacon type, longitud) y 21 de carga
	uint8_t restoPrefijoYCarga[4+21] = {
	  0x4c, 0x00, // companyID 2
	  0x02, // ibeacon type 1byte
	  21, // ibeacon length 1byte (dec=21)  longitud del resto // 0x15 // ibeacon length 1byte (dec=21)  longitud del resto
	  '-', '-', '-', '-', 
	  '-', '-', '-', '-', 
	  '-', '-', '-', '-', 
	  '-', '-', '-', '-', 
	  '-', '-', '-', '-', 
	  '-'
	};

	// addData() hay que usarlo sólo una vez. Por eso copio la carga
	// en el anterior array, donde he dejado 21 sitios libres
	memcpy( &restoPrefijoYCarga[4], &carga[0], ( tamanyoCarga > 21 ? 21 : tamanyoCarga ) ); 

	// copio la carga para emitir
	Bluefruit.Advertising.addData( BLE_GAP_AD_TYPE_MANUFACTURER_SPECIFIC_DATA,
								   &restoPrefijoYCarga[0],
								   4+21 );


	Bluefruit.Advertising.restartOnDisconnect(true);
	Bluefruit.Advertising.setInterval(100, 100);    // in unit of 0.625 ms

	Bluefruit.Advertising.setFastTimeout( 1 );      // number of seconds in fast mode

	Bluefruit.Advertising.start( 0 ); //de nuevo, 0 = tiempo indefinido

	Globales::elPuerto.escribir( "emitiriBeacon libre  Bluefruit.Advertising.start( 0 );  \n");
  } 

  // .........................................................
  //  servicio: ServicioEnEmisora -->   anyadirServicio() <--
  //                                    B <-- 
  //
  //  Añade un servicio GATT al anuncio.  Devuelve B = true si se añadió
  //  correctamente; escribe un aviso por el puerto serie si falla.
  //  Usa la conversión implícita de ServicioEnEmisora a BLEService.
  // .........................................................
  bool anyadirServicio( ServicioEnEmisora & servicio ) {

	Globales::elPuerto.escribir( " Bluefruit.Advertising.addService( servicio ); \n");

	bool r = Bluefruit.Advertising.addService( servicio );

	if ( ! r ) {
	  Serial.println( " SERVICION NO AÑADIDO \n");
	}
	

	return r;
  } 

  
  // .........................................................
  //  servicio: ServicioEnEmisora --> anyadirServicioConSusCaracteristicas() <--
  //                                B <-- 

  //  Sobrecarga simple: delega en anyadirServicio().
  // .........................................................
  bool anyadirServicioConSusCaracteristicas( ServicioEnEmisora & servicio ) { 
	return (*this).anyadirServicio( servicio );
  }

  // .........................................................
  //
  //  servicio: ServicioEnEmisora
  //  caracteristica: ServicioEnEmisora::Caracteristica
  //  restoCaracteristicas: T   -->   anyadirServicioConSusCaracteristicas() -->
  //                                 B <--

  //  Añade una característica y delega recursivamente en el resto.
  // .........................................................
  template <typename ... T>
  bool anyadirServicioConSusCaracteristicas( ServicioEnEmisora & servicio,
											 ServicioEnEmisora::Caracteristica & caracteristica,
											T& ... restoCaracteristicas) {

	servicio.anyadirCaracteristica( caracteristica );

	return anyadirServicioConSusCaracteristicas( servicio, restoCaracteristicas... );
	
  }

  // .........................................................
  //
  //  servicio: ServicioEnEmisora
  //  restoCaracteristicas: [ ServicioEnEmisora::Caracteristica ] --> anyadirServicioConSusCaracteristicasYActivar() -->
  //                                                                  B <--
  //
  //  Registra el servicio con todas sus características y lo activa.
  // .........................................................
  template <typename ... T>
  bool anyadirServicioConSusCaracteristicasYActivar( ServicioEnEmisora & servicio, T& ... restoCaracteristicas) {
	bool r = anyadirServicioConSusCaracteristicas( servicio, restoCaracteristicas... );

	servicio.activarServicio();

	return r;
  }

  // .........................................................
  //   cb: CallbackConexionEstablecida
  //             --> instalarCallbackConexionEstablecida() -->
  //
  //  Registra el callback que se ejecutará al establecerse una conexión.
  // .........................................................
  void instalarCallbackConexionEstablecida( CallbackConexionEstablecida cb ) {
	Bluefruit.Periph.setConnectCallback( cb );
  }

  // .........................................................
  //   cb: CallbackConexionTerminada
  //             --> instalarCallbackConexionTerminada() -->
  //
  //  Registra el callback que se ejecutará al terminar una conexión.
  // .........................................................
  void instalarCallbackConexionTerminada( CallbackConexionTerminada cb ) {
	Bluefruit.Periph.setDisconnectCallback( cb );
  }

  // .........................................................
  //  connHandle: N
  //            BLEConnection <-- getConexion() <--
  //
  //  Devuelve el objeto de conexión asociado a un handle dado.
  // .........................................................
  BLEConnection * getConexion( uint16_t connHandle ) {
	return Bluefruit.Connection( connHandle );
  }

};

#endif