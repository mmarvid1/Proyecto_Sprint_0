// ===========================================================================
//  Minerva Maravilla Vidaurre
//  05/10/2026
// ===========================================================================
#ifndef PUERTO_SERIE_H_INCLUIDO
#define PUERTO_SERIE_H_INCLUIDO


// ===========================================================================
//  DISEÑO   --  PuertoSerie.h
// ===========================================================================
//
//
//                 --------- PuertoSerie ------------------
//                 |
//                 |
//                 |
//  baudios: Z --> PuertoSerie() -->
//                 |
//                 |
//                 esperarDisponible() -->
//                 |
//                 |
//  mensaje: T --> escribir() -->
//                 |
//                 -------------------------------------
//
//
// ===========================================================================


class PuertoSerie  {

public:
  // .........................................................
  //   baudios: Z  --> PuertoSerie() -->
  // .........................................................
  PuertoSerie (long baudios) {
	Serial.begin( baudios ); 
  }

  // .........................................................
  //   esperarDisponible() -->
  //
  // Espera a que el puerto serie esté disponible.
  // .........................................................
  void esperarDisponible() {
	/*while ( !Serial ) {
	  delay(10);   
	}*/
  }

  // .........................................................
  //   mensaje: T  --> escribir() -->
  // .........................................................
  template<typename T> 
  
  void escribir (T mensaje) { //'T' es genérico: admite Text, Z, N u otro tipo imprimible.
	Serial.print( mensaje );
  }
  
}; 

#endif