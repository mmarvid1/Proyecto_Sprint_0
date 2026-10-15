// =====================================================================
// FICHERO : ayuda.js
// PROPOSITO: Ayudante minimo de aserciones para los tests de la web.
//            No usamos ninguna libreria externa (ni Jest) para que
//            "npm test" funcione solo con Node, sin descargas.
// USO      : Sprint 0 - Proyecto Beacon. Lo usan logica_fake.test.js,
//            ux.test.js e integracion.test.js.
// =====================================================================

// Contador de pruebas correctas y de fallos, y cola de tests pendientes.
var estado = { bien: 0, fallos: 0, fallosDetalle: [], cola: [] };

// ---- Funcion: comprobar ----
// PROPOSITO: Encola una prueba. Las pruebas NO se ejecutan aqui sino
//            una detras de otra en ejecutar(), porque varias de ellas
//            tocan el mismo estado global (UX) y si corrieran en
//            paralelo se pisarian entre si.
// PARAM.   : string nombre - nombre de la prueba.
//            function prueba - la prueba; si lanza o rechaza, falla.
// RETORNA  : nada.
function comprobar(nombre, prueba) {
    estado.cola.push({ nombre: nombre, prueba: prueba });
}

// ---- Funcion: ejecutar ----
// PROPOSITO: Ejecuta las pruebas encoladas en orden, una tras otra,
//            esperando a que termine cada una antes de empezar la
//            siguiente (asi funciona tambien con las asincronas).
// PARAM.   : ninguno.
// RETORNA  : Promise - termina cuando todas han pasado o fallado.
// () ----------------------------------------------------------------
async function ejecutar() {
    for (const t of estado.cola) {
        try {
            await t.prueba();
            estado.bien++;
            console.log('  OK    ' + t.nombre);
        } catch (e) {
            registrarFallo(t.nombre, e);
        }
    }
}

// ---- Funcion: registrarFallo ----
// PROPOSITO: Suma un fallo y avisa por pantalla con el motivo.
// PARAM.   : string nombre - nombre de la prueba.
//            Error e - el error lanzado o el motivo del fallo.
// RETORNA  : nada.
function registrarFallo(nombre, e) {
    estado.fallos++;
    const motivo = (e && e.message) ? e.message : String(e);
    estado.fallosDetalle.push(nombre + ' -> ' + motivo);
    console.log('  FALLO ' + nombre + ' -> ' + motivo);
}

// ---- Funcion: igual ----
// PROPOSITO: Comprueba que dos valores son iguales (===).
// PARAM.   : mixed real - valor obtenido.
//            mixed esperado - valor que deberia ser.
//            string que - texto del fallo si no coinciden.
// RETORNA  : nada. Lanza si no son iguales.
function igual(real, esperado, que) {
    if (real !== esperado) {
        throw new Error((que || 'valor') + ': se esperaba ' + JSON.stringify(esperado)
                      + ' pero se obtuvo ' + JSON.stringify(real));
    }
}

// ---- Funcion: cierto ----
// PROPOSITO: Comprueba que una condicion es verdadera.
// PARAM.   : mixed condicion - lo que se comprueba.
//            string que - texto del fallo si es falsa.
// RETORNA  : nada. Lanza si la condicion es falsa.
function cierto(condicion, que) {
    if (!condicion) {
        throw new Error('no se cumple: ' + que);
    }
}

// ---- Funcion: contiene ----
// PROPOSITO: Comprueba que un texto contiene un trozo de texto.
// PARAM.   : string texto - donde se busca.
//            string trozo - lo que tiene que aparecer dentro.
// RETORNA  : nada. Lanza si no lo contiene.
function contiene(texto, trozo) {
    if (String(texto).indexOf(trozo) === -1) {
        throw new Error('"' + texto + '" no contiene "' + trozo + '"');
    }
}

// ---- Funcion: titulo ----
// PROPOSITO: Escribe el titulo del fichero de tests.
// PARAM.   : string nombre - nombre del fichero.
// RETORNA  : nada.
function titulo(nombre) {
    console.log('\n=== ' + nombre + ' ===');
}

// ---- Funcion: resumen ----
// PROPOSITO: Ejecuta las pruebas encoladas y, cuando todas han
//            terminado, muestra el total y fija el codigo de salida del
//            proceso: 0 si todo pasa, 1 si hay algun fallo.
// PARAM.   : string nombre - nombre del fichero.
// RETORNA  : Promise - se puede esperar con await si se quiere.
function resumen(nombre) {
    return ejecutar().then(function () {
        console.log('--- ' + nombre + ': ' + estado.bien + ' correctas, '
                  + estado.fallos + ' fallos ---');
        if (estado.fallos > 0) {
            console.log('Fallos:');
            for (const f of estado.fallosDetalle) {
                console.log('  - ' + f);
            }
            process.exitCode = 1;
        } else {
            console.log('Todo en verde.');
        }
    });
}

// Exportamos el ayudante.
module.exports = { comprobar, ejecutar, igual, cierto, contiene, titulo, resumen };