// ===========================================================================
//  Minerva Maravilla Vidaurre
//  05/10/2026
// ===========================================================================
#ifndef SERVICIO_EMISORA_H_INCLUIDO
#define SERVICIO_EMISORA_H_INCLUIDO

#include <vector>


// ===========================================================================
//  DISEÑO LÓGICO  --  ServicioEnEmisora.h
// ===========================================================================
//
//
//     CallbackCaracteristicaEscrita =
//         ( conn_handle: N, chr: BLECharacteristic, data: N, len: N )
//
//
//                 --------- ServicioEnEmisora ------------------
//                 |
//                 |
//                 |
//     p: [ T ]
//        n: Z --> alReves() --> [ T ]
//                 |
//                 |
//     pString: Text
//     pUint: [ N ]
//   tamMax: Z --> stringAUint8AlReves() --> [ N ]
//                 |
//                 |
//                 | uuidServicio: [ N ]_16
//                 | elServicio: BLEService
//                 | lasCaracteristicas: [ ServicioEnEmisora::Caracteristica ]
//                 |
//                 |
//   nombreServicio_: Text
//             --> ServicioEnEmisora() -->
//                 |
//                 |
//                 escribeUUID() -->
//                 |
//                 |
  //  car: ServicioEnEmisora::Caracteristica
//             --> anyadirCaracteristica() -->
//                 |
//                 |
//  BLEService
//             <-- activarServicio() <--
//                 |
//                 -------------------------------------
//
// ---------------------------------------------------------------------------
//  ServicioEnEmisora::Caracteristica
// ---------------------------------------------------------------------------
//
//                 --------- Caracteristica ------------------
//                 |
//                 | uuidCaracteristica: [ N ]_16
//                 | laCaracteristica: BLECharacteristic
//                 |
//                 |
//   nombreCaracteristica_: Text
//             --> Caracteristica() -->
//                 |
//                 |
//   nombreCaracteristica_: Text
//   props: N
//   permisoRead: BleSecurityMode
//   permisoWrite: BleSecurityMode
//   tam: N
//             --> Caracteristica() -->
//                 |
//                 |
//                 |   props: N  --> asignarPropiedades() -->
//                 |
//                 |
//                 |   permisoRead, permisoWrite: BleSecurityMode
//                 |                  --> asignarPermisos() -->
//                 |
//                 |
//                 |   tam: N  --> asignarTamanyoDatos() -->
//                 |
//                 |
//   props: N
//   permisoRead: BleSecurityMode
//   permisoWrite: BleSecurityMode
//   tam: N
//             --> asignarPropiedadesPermisosYTamanyoDatos() -->
//                 |
//                 |
//   str: Text --> escribirDatos() --> N
//                 |
//                 |
//   str: Text --> notificarDatos() --> N
//                 |
//                 |
//  cb: CallbackCaracteristicaEscrita
//             --> instalarCallbackCaracteristicaEscrita() -->
//                 |
//                 |
//                 activar() -->
//                 |
//                 -------------------------------------
//
// ---------------------------------------------------------------------------
//  Las dos funciones libres de la banda MÓDULO
// ---------------------------------------------------------------------------
//
//  alReves( p, n )
//     Invierte in-place el contenido de un array de 'n' elementos genéricos y
//     devuelve el mismo array.  Uso: colocar un UUID escrito en texto en el
//     orden de bytes que exige el BLE.
//
//  stringAUint8AlReves( pString, pUint, tamMax )
//     Copia el texto 'pString' en el array de bytes 'pUint' escribiendo de
//     forma INVERSA y alineada a la derecha respecto a 'tamMax'.  Trunca el
//     texto si es más largo que 'tamMax'.  Devuelve el array destino 'pUint'.
//
//  Ambas son funciones PURAS: devuelven un resultado y no tocan estado de
//  ninguna instancia.  Por eso llevan flecha de salida además de la de
//  entrada.
//
// ---------------------------------------------------------------------------
//  Notas de ingeniería inversa (diseño <--> código)
// ---------------------------------------------------------------------------
//
//   1. El diagrama muestra 'activarServicio() --> BLEService'.  El código
//      devuelve void; la conversión a BLEService se hace aparte.  Se
//      documenta tal y como aparece en el diagrama y se señala la diferencia.
//
//   2. Los métodos asignarPropiedades(), asignarPermisos() y
//      asignarTamanyoDatos() aparecen en el lado DERECHO de la pared en el
//      diagrama (lectura) pero en el código son privados y mutadores
//      (llaman a setProperties/setPermission/setMaxLen).  En esta
//      transcripción se ubican dentro de la caja como PRIVADOS y se les
//      asigna '--> ' (mutador), que es lo que hace el código.
//
//   3. El diagrama incluye el constructor de Conversion (operador BLEService&)
//      como si fuera un método público más; en el código es un operador de
//      conversión.  Se documenta aparte al final de la clase.
//
//   4. El diagrama sitúa alReves() y stringAUint8AlReves() FUERA de la caja
//      de ServicioEnEmisora, porque en un croquis de flujo de datos se dibujan
//      aparte.  Aquí se han integrado en la caja del módulo, en la banda
//      "MÓDULO", ya que en el código son funciones libres del mismo fichero.
//
//   4. 'escribeUUID()' y 'activar()' escriben directamente con Serial y con
//      Globales::elPuerto; son efectos de traza, no transformaciones.
//
// ===========================================================================


// ----------------------------------------------------
//     p: [ T ]
//     n: Z    --> alReves()  --> [ T ]
// Pone el array al revés en ese mismo array
// ----------------------------------------------------
template< typename T >
T *  alReves( T * p, int n ) {
  T aux;

  for( int i=0; i < n/2; i++ ) {
	aux = p[i];
	p[i] = p[n-i-1];
	p[n-i-1] = aux;
  }
  return p;
}

// ----------------------------------------------------
//     pString: Text
//     pUint: [ N ]
//   tamMax: Z --> stringAUint8AlReves() --> [ N ]
// ----------------------------------------------------
uint8_t * stringAUint8AlReves( const char * pString, uint8_t * pUint, int tamMax ) {

	int longitudString =  strlen( pString );
	int longitudCopiar = ( longitudString > tamMax ? tamMax : longitudString );
	// copio nombreServicio -> uuidServicio pero al revés
	for( int i=0; i<=longitudCopiar-1; i++ ) {
	  pUint[ tamMax-i-1 ] = pString[ i ];
	}

	return pUint;
} 

// ----------------------------------------------------------
// ----------------------------------------------------------
class ServicioEnEmisora {

public:
  // .........................................................
  // CallbackCaracteristicaEscrita = ( conn_handle, chr, data, len )
  // .........................................................
  using CallbackCaracteristicaEscrita = void ( uint16_t conn_handle,
											   BLECharacteristic * chr,
											   uint8_t * data, uint16_t len); 
  
  // =========================================================
  //  Clase Caracteristica
  // =========================================================
  class Caracteristica {
  private:
	uint8_t uuidCaracteristica[16] = { // el uuid se copia aquí (al revés) a partir de un string-c
	  // least signficant byte, el primero
	  '0', '1', '2', '3', 
	  '4', '5', '6', '7', 
	  '8', '9', 'A', 'B', 
	  'C', 'D', 'E', 'F'
	};

	BLECharacteristic laCaracteristica;

  public:

	// .........................................................
	//   nombreCaracteristica_: Text  --> Caracteristica() -->
	//
	// Construye la característica a partir de su nombre.  El nombre se
	// convierte a UUID (16 bytes) con stringAUint8AlReves(), en orden
	// inverso, y se pasa al BLECharacteristic.
	// .........................................................
	Caracteristica( const char * nombreCaracteristica_ ):
		laCaracteristica( stringAUint8AlReves( nombreCaracteristica_, &uuidCaracteristica[0], 16 ) ) { }

	// .........................................................
	//   nombreCaracteristica_: Text
	//   props: N
	//   permisoRead: BleSecurityMode
	//   permisoWrite: BleSecurityMode
	//   tam: N   
	//					--> Caracteristica() -->
	//
	// Igual que el anterior pero además configura de golpe las
	// propiedades, los permisos de lectura/escritura y el tamaño máximo de
	// datos.  Delega en el otro constructor y luego llama a
	// asignarPropiedadesPermisosYTamanyoDatos().
	// .........................................................
	Caracteristica( const char * nombreCaracteristica_ ,
					uint8_t props,
					SecureMode_t permisoRead,
					SecureMode_t permisoWrite, 
					uint8_t tam ) 
	  :  Caracteristica( nombreCaracteristica_ ) // llamada al otro constructor
	{
	  (*this).asignarPropiedadesPermisosYTamanyoDatos( props, permisoRead, permisoWrite, tam );
	}

  private:
	// .........................................................
	//   props: N  --> asignarPropiedades() -->
	// .........................................................
	void asignarPropiedades ( uint8_t props ) {
	  (*this).laCaracteristica.setProperties( props );
	}

	// .........................................................
	//   permisoWrite: BleSecurityMode
	//   permisoRead: BleSecurityMode  
	//																--> asignarPermisos() -->
	// .........................................................
	void asignarPermisos( SecureMode_t permisoRead, SecureMode_t permisoWrite ) {
	  (*this).laCaracteristica.setPermission( permisoRead, permisoWrite );
	} // ()

	// .........................................................
	//   tam: N  --> asignarTamanyoDatos() -->
	// .........................................................
	void asignarTamanyoDatos( uint8_t tam ) {
	  (*this).laCaracteristica.setMaxLen( tam );
	}

  public:
	// .........................................................
	//   props: N
	//   permisoRead: BleSecurityMode
	//   permisoWrite: BleSecurityMode
	//   tam: N  --> asignarPropiedadesPermisosYTamanyoDatos() -->
	//
	// Asigna de una propiedades, permisos y tamaño.
	// .........................................................
	void asignarPropiedadesPermisosYTamanyoDatos( uint8_t props,
												 SecureMode_t permisoRead,
												 SecureMode_t permisoWrite, 
												 uint8_t tam ) {
	  asignarPropiedades( props );
	  asignarPermisos( permisoRead, permisoWrite );
	  asignarTamanyoDatos( tam );
	} 
											 

	// .........................................................
	//   str: Text  --> escribirDatos() --> N
	//
	// Escribe un valor en la característica y devuelve el número de bytes realmente escritos.
	// .........................................................
	uint16_t escribirDatos( const char * str ) {
	  uint16_t r = (*this).laCaracteristica.write( str );
	  return r;
	}

	// .........................................................
	//   str: Text  --> notificarDatos() --> N
	//
	// Envía una notificación con el valor indicado y devuelve el número de bytes notificados.
	// .........................................................
	uint16_t notificarDatos( const char * str ) {
	  uint16_t r = laCaracteristica.notify( &str[0] );
	  return r;
	}

	// .........................................................
	//   cb: CallbackCaracteristicaEscrita
	//             --> instalarCallbackCaracteristicaEscrita() -->
	//
	// Registra el callback que se disparará cuando un cliente escriba en esta característica.
	// .........................................................
	void instalarCallbackCaracteristicaEscrita( CallbackCaracteristicaEscrita cb ) {
	  (*this).laCaracteristica.setWriteCallback( cb );
	}

	// .........................................................
	//   activar() -->
	//
	// Registra la característica en el stack BLE (begin) y deja traza del código de error por el puerto serie.
	// .........................................................
	void activar() {
	  err_t error = (*this).laCaracteristica.begin();
	  Globales::elPuerto.escribir(  " (*this).laCaracteristica.begin(); error = " );
	  Globales::elPuerto.escribir(  error );
	}

  }; // class Caracteristica
  
  // --------------------------------------------------------
  // --------------------------------------------------------
private:
  
  uint8_t uuidServicio[16] = { // el uuid se copia aquí (al revés) a partir de un string-c
	// least signficant byte, el primero
	'0', '1', '2', '3', 
	'4', '5', '6', '7', 
	'8', '9', 'A', 'B', 
	'C', 'D', 'E', 'F'
  };

  BLEService elServicio;
  std::vector< Caracteristica * > lasCaracteristicas;

public:
  
  // .........................................................
  //   nombreServicio_: Text  --> ServicioEnEmisora() -->
  //
  // Construye el servicio GATT.  El nombre se convierte a UUID de 16 bytes (en orden inverso) y se pasa al BLEService.
  // .........................................................
  ServicioEnEmisora( const char * nombreServicio_ )
	: elServicio( stringAUint8AlReves( nombreServicio_, &uuidServicio[0], 16 ) ) { } 
   
  // .........................................................
  //   escribeUUID() -->
  // .........................................................
  void escribeUUID() {
	Serial.println ( "**********" );
	for (int i=0; i<= 15; i++) {
	  Serial.print( (char) uuidServicio[i] );
	}
	Serial.println ( "\n**********" );
  }

  // .........................................................
  //   car: ServicioEnEmisora::Caracteristica
  //             --> anyadirCaracteristica() -->
  //
  // Añade una característica (por referencia) al vector del servicio.
  // .........................................................
  void anyadirCaracteristica( Caracteristica & car ) {
	(*this).lasCaracteristicas.push_back( & car );
  }

  // .........................................................
  //            BLEService <-- activarServicio() <--
  //
  // Registra el servicio en el stack BLE (begin), deja traza del error y activa una a una todas las características añadidas.  
  // .........................................................
  void activarServicio( ) {
	err_t error = (*this).elServicio.begin();
	Serial.print( " (*this).elServicio.begin(); error = " );
	Serial.println( error );

	for( auto pCar : (*this).lasCaracteristicas ) {
	  (*pCar).activar();
	}
  }

  // .........................................................
  //  CONVERSIÓN DE TIPO:  ServicioEnEmisora  --> BLEService
  //
	//  Permite pasar este objeto allí donde la biblioteca espera un BLEService
	//  (por ejemplo, EmisoraBLE::anyadirServicio()).
  // .........................................................
  operator BLEService&() {
	return elServicio;
  } 
	
}; 

#endif