// =====================================================================
// FICHERO : ux.test.js
// PROPOSITO: Prueba la UX del navegador (ux.js): que pinta la tabla con
//            los campos de cada medicion, que avisa de los estados
//            vacio y error, y que se refresca sola sin botones.
// USO      : Sprint 0 - Proyecto Beacon. Se ejecuta con Node puro
//            usando un DOM minimo falso, sin jsdom ni librerias.
// Ejecutar  : node web/tests/ux.test.js
// =====================================================================

const { comprobar, igual, cierto, contiene, titulo, resumen } = require('./ayuda.js');

const LogicaFakeReal = require('../js/logica_fake.js');
const UX = require('../js/ux.js');

// ---- 1. DOM minimo falso ----
// ux.js solo necesita crear elementos y meter HTML en un contenedor,
// asi que con estos tres objetos basta (no hace falta jsdom).
function crearContenedor() {
    return {
        innerHTML: '',
        hijos: [],
        appendChild: function (hijo) {
            this.hijos.push(hijo);
            this.innerHTML = hijo.textContent;
        }
    };
}

globalThis.document = {
    createElement: function () {
        return { className: '', textContent: '' };
    },
    getElementById: function () { return crearContenedor(); }
};

// ---- 2. Cada prueba empieza con un contenedor limpio ----
function preparar() {
    UX.contenedor = crearContenedor();
    UX.peticionEnCurso = false;
    // Paramos cualquier refresco automatico de pruebas anteriores.
    if (UX.timer) {
        clearInterval(UX.timer);
        UX.timer = null;
    }
}

titulo('ux.js - Pintado de la tabla y estados');

// =====================================================================
// Prueba 1: pinta un historial con los campos de la medicion
// =====================================================================
comprobar('pinta la tabla con tipo_medicion, valor, nombre_emisora y fecha en espanol', function () {
    preparar();
    UX.pintarMediciones([
        { id: 1, tipo_medicion: 'MANUAL', valor: 1234,
          nombre_emisora: 'Minerva_ELBACON',
          fecha_hora: '2026-09-28 09:00:00' }
    ]);

    const html = UX.contenedor.innerHTML;
    cierto(html.indexOf('<table') !== -1, 'pinta una tabla');
    contiene(html, '1234');                                 // el valor medido
    contiene(html, 'MANUAL');                               // el tipo de medicion
    contiene(html, 'Minerva_ELBACON');                     // la emisora
    contiene(html, '28/09/2026');                           // fecha en espanol
    // Los 5 campos del diseño tienen que estar pintados, cada uno en
    // su celda con su clase "medicion-<campo>" (el valor puede llevar
    // ademas su clase de color: "medicion-valor valor-alto").
    for (const col of ['id', 'tipo_medicion', 'valor',
                       'nombre_emisora', 'fecha_hora']) {
        contiene(html, 'class="medicion-' + col);
    }
    // La tabla no lleva ningun boton: el refresco es automatico.
    igual(html.toLowerCase().indexOf('<button'), -1, 'no hay ningun boton');
});

// =====================================================================
// Prueba 2: la fecha se formatea a dd/mm/aaaa hh:mm:ss
// =====================================================================
comprobar('formatea la fecha del servidor al formato espanol', function () {
    igual(UX.formatearFecha('2026-09-28 09:00:00'), '28/09/2026 09:00:00', 'fecha formateada');
    igual(UX.formatearFecha(null), '-', 'fecha nula');
});

// =====================================================================
// Prueba 3: lista vacia -> mensaje de "no hay mediciones"
// =====================================================================
comprobar('pinta el estado vacio cuando no hay mediciones', function () {
    preparar();
    UX.pintarMediciones([]);

    const hijo = UX.contenedor.hijos[0];
    cierto(hijo !== undefined, 'hay un aviso en el contenedor');
    contiene(hijo.textContent, 'No hay mediciones');
    igual(hijo.className, 'estado estado-vacio', 'clase del aviso');
});

// =====================================================================
// Prueba 4: estado de error con el mensaje del servidor
// =====================================================================
comprobar('pinta el estado de error con el mensaje recibido', function () {
    preparar();
    UX.pintarEstado('error', 'servidor caído');

    const hijo = UX.contenedor.hijos[0];
    cierto(hijo !== undefined, 'hay un aviso en el contenedor');
    contiene(hijo.textContent, 'servidor caído');
    igual(hijo.className, 'estado estado-error', 'clase del aviso');
});

// =====================================================================
// Prueba 5: estado de cargando
// =====================================================================
comprobar('pinta el estado de cargando', function () {
    preparar();
    UX.pintarEstado('cargando', 'Cargando mediciones...');

    const hijo = UX.contenedor.hijos[0];
    contiene(hijo.textContent, 'Cargando');
    igual(hijo.className, 'estado estado-cargando', 'clase del aviso');
});

// =====================================================================
// Prueba 6: recargarMediciones usa LogicaFake y pinta lo que devuelve
// =====================================================================
comprobar('recargarMediciones pide los datos a LogicaFake y los pinta', async function () {
    preparar();

    // Sustituimos la peticion real por una respuesta ya resuelta.
    globalThis.LogicaFake = {
        recuperarMedicion: async function () {
            return [{ id: 9, tipo_medicion: null, valor: 1234,
                      nombre_emisora: 'Minerva_ELBACON',
                      fecha_hora: '2026-09-28 09:00:00' }];
        }
    };

    await UX.recargarMediciones();

    contiene(UX.contenedor.innerHTML, '1234');
    contiene(UX.contenedor.innerHTML, 'Minerva_ELBACON');
    // Un metadato nulo se muestra como guion, no como "null".
    contiene(UX.contenedor.innerHTML, '>-<');

    // Devolvemos la logica real.
    globalThis.LogicaFake = LogicaFakeReal;
});

// =====================================================================
// Prueba 7: si LogicaFake falla, se pinta el estado de error
// =====================================================================
comprobar('recargarMediciones pinta el error si el servidor falla', async function () {
    preparar();

    globalThis.LogicaFake = {
        recuperarMedicion: async function () {
            throw new Error('el servidor no responde');
        }
    };

    await UX.recargarMediciones();

    const hijo = UX.contenedor.hijos[0];
    cierto(hijo !== undefined, 'hay un aviso de error');
    contiene(hijo.textContent, 'el servidor no responde');
    igual(hijo.className, 'estado estado-error', 'clase del aviso');

    globalThis.LogicaFake = LogicaFakeReal;
});

// =====================================================================
// Prueba 8: iniciar() carga al arrancar y programa el refresco periodico
// =====================================================================
comprobar('iniciar hace la primera carga y refresca solo cada intervaloMs', async function () {
    preparar();

    // Contamos cuantas veces se piden los datos.
    let llamadas = 0;
    globalThis.LogicaFake = {
        recuperarMedicion: async function () {
            llamadas++;
            return [];
        }
    };

    UX.intervaloMs = 50;            // acortamos el intervalo para el test
    UX.iniciar();
    igual(llamadas, 1, 'hace la primera carga al arrancar');

    // El aviso de "cargando" se pinta antes de la respuesta.
    igual(UX.contenedor.hijos[0].className, 'estado estado-cargando', 'estado inicial');

    // Esperamos a que el intervalo se dispare varias veces.
    await new Promise(function (r) { setTimeout(r, 260); });

    cierto(llamadas > 1, 'vuelve a pedir datos al pasar el intervalo');

    // No hay ningun boton en la pagina: el refresco es automatico.
    clearInterval(UX.timer);
    UX.timer = null;
    UX.intervaloMs = 5000;

    globalThis.LogicaFake = LogicaFakeReal;
});

resumen('ux.test.js');