// ===========================================================================
//  Minerva Maravilla Vidaurre
//  05/10/2026
// ===========================================================================
//  DISEÑO   --  MandarBeacon_Arduino.ino
// ===========================================================================
//
//      elPublicador -> Publicador  traduce una medida a un anuncio iBeacon
//
//                 --------- MandarBeaconArduino ------------------
//                 |
//                 | Globales::elLED: LED
//                 | Globales::elPuerto: PuertoSerie
//                 | Globales::elPublicador: Publicador
//                 | Globales::elMedidor: Medidor
//                 | Loop::cont: N
//                 |
//                 |
//                 inicializarPlaquita() -->
//                 |
//                 |
//                 setup() -->
//                 |
//                 |
//                 lucecitas() -->
//                 |
//                 |
//                 loop() -->
//                 |
//                 -------------------------------------
//
//
// ===========================================================================

#include <bluefruit.h>
#include "LED.h"
#include "PuertoSerie.h"

namespace Globales {
  
  LED elLED (7); //Numero PIN

  PuertoSerie elPuerto (115200); // Velocidad (115200, 9600..)

};

// --------------------------------------------------------------
// --------------------------------------------------------------
#include "EmisoraBLE.h"
#include "Publicador.h"
#include "Medidor.h"

namespace Globales {

  Publicador elPublicador;

  Medidor elMedidor;

};

// --------------------------------------------------------------
//   inicializarPlaquita() -->
// --------------------------------------------------------------
void inicializarPlaquita () {

  // de momento nada

}

// --------------------------------------------------------------
//   setup() -->
// --------------------------------------------------------------
void setup() {

  Globales::elPuerto.esperarDisponible();

  inicializarPlaquita();

  Globales::elPublicador.encenderEmisora();

  Globales::elMedidor.iniciarMedidor();

  esperar( 1000 );

  Globales::elPuerto.escribir( "---- setup(): fin ---- \n " );

}

// --------------------------------------------------------------
//   lucecitas() -->
// --------------------------------------------------------------
inline void lucecitas() {
  using namespace Globales;

  elLED.brillar( 100 ); // 100 encendido
  esperar ( 400 ); //  100 apagado
  elLED.brillar( 100 ); // 100 encendido
  esperar ( 400 ); //  100 apagado
  Globales::elLED.brillar( 100 ); // 100 encendido
  esperar ( 400 ); //  100 apagado
  Globales::elLED.brillar( 1000 ); // 1000 encendido
  esperar ( 1000 ); //  100 apagado
}

namespace Loop {
  uint8_t cont = 0;
};

// ..............................................................
//   loop() -->
// ..............................................................
void loop () {

  using namespace Loop;
  using namespace Globales;

  cont++;

  elPuerto.escribir( "\n---- loop(): empieza " );
  elPuerto.escribir( cont );
  elPuerto.escribir( "\n" );


  lucecitas();

  elPublicador.publicarManual( cont,
							   1000 // intervalo de emisión
							   );
  
  
  esperar( 2000 );

  elPuerto.escribir( "---- loop(): acaba **** " );
  elPuerto.escribir( cont );
  elPuerto.escribir( "\n" );
  
}